package com.kotlinfoundation.kmpstarterkit.data.repository

import com.kotlinfoundation.kmpstarterkit.data.BackgroundExecutor
import com.kotlinfoundation.kmpstarterkit.data.source.local.dao.GenerationOutputDao
import com.kotlinfoundation.kmpstarterkit.data.source.local.entity.toEntity
import com.kotlinfoundation.kmpstarterkit.data.source.local.entity.toModel
import com.kotlinfoundation.kmpstarterkit.domain.exceptions.CreditRequiredException
import com.kotlinfoundation.kmpstarterkit.domain.exceptions.PurchaseRequiredException
import com.kotlinfoundation.kmpstarterkit.domain.model.credit.CreditConstants
import com.kotlinfoundation.kmpstarterkit.domain.model.credit.CreditTransaction
import com.kotlinfoundation.kmpstarterkit.domain.model.generation.GenerationInput
import com.kotlinfoundation.kmpstarterkit.domain.model.generation.GenerationOutput
import com.kotlinfoundation.kmpstarterkit.domain.usecase.AiGenerationProvider
import com.kotlinfoundation.kmpstarterkit.util.analytics.Analytics
import com.kotlinfoundation.kmpstarterkit.util.file.FileManager
import com.kotlinfoundation.kmpstarterkit.util.logging.AppLogger
import com.mmk.kmpstorage.core.FileUploadProgress
import com.mmk.kmpstorage.core.KMPStorage
import com.mmk.kmpstorage.core.extensions.putFile
import io.github.vinceglb.filekit.mimeType
import io.github.vinceglb.filekit.nameWithoutExtension
import io.github.vinceglb.filekit.readBytes
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map

/**
 * Orchestrates one AI generation: spend a credit, upload input files to the cloud, call the
 * [AiGenerationService], then cache + persist the [GenerationOutput] locally (Room) so the
 * gallery can observe it. Network work runs through [BackgroundExecutor]. The AI backend is
 * abstracted by [AiGenerationProvider] (Replicate / OpenAI).
 */
class GenerationRepository(
    private val generationOutputDao: GenerationOutputDao,
    private val aiGenerationProvider: AiGenerationProvider,
    private val creditRepository: CreditRepository,
    private val fileManager: FileManager,
    private val analytics: Analytics,
    private val backgroundExecutor: BackgroundExecutor,
) {
    private val cachedOutputMap = linkedMapOf<String, GenerationOutput>()

    fun observeAllGenerationOutput(): Flow<Result<List<GenerationOutput>>> = generationOutputDao.getAllFlow()
        .map { entities -> Result.success(entities.map { it.toModel(fileManager) }) }
        .catch { error ->
            AppLogger.e("Error observing generation output: $error")
            emit(Result.failure(error))
        }

    suspend fun generate(input: GenerationInput): Result<GenerationOutput> = backgroundExecutor.execute {
        creditRepository.useCredits(CreditConstants.COST_GENERATION)
        analytics.logEvent(event = Analytics.EVENT_CLICKED_GENERATE)
        val updatedGenerationInput = input.uploadFilesIntoCloud()

        val aiGenerationOutputResult =
            aiGenerationProvider.generate(input = updatedGenerationInput)

        return@execute aiGenerationOutputResult.map { generationOutput ->
            val output = generationOutput.outputFile?.fileNameWithExtension?.let {
                fileManager.getAbsoluteFilePathRelativeToInternal(it)
            }
            generationOutput.copy(output = output)
        }.onSuccess { generationOutput ->
            cachedOutputMap[generationOutput.id] = generationOutput
            generationOutputDao.upsert(generationOutput.toEntity())
        }.onFailure { error ->
            // The credit was spent up-front, so refund it when generation fails. Billing
            // exceptions are thrown before any credit is deducted, so they need no refund.
            if (error !is PurchaseRequiredException && error !is CreditRequiredException) {
                creditRepository.addCredits(
                    amount = CreditConstants.COST_GENERATION,
                    type = CreditTransaction.Type.ADMIN_ADJUSTMENT,
                    description = "Refund for failed generation",
                )
            }
        }
    }

    suspend fun getGenerationOutputById(id: String): Result<GenerationOutput> = backgroundExecutor.execute {
        val cachedOutput = cachedOutputMap[id]
        if (cachedOutput != null) return@execute Result.success(cachedOutput)

        val entity = generationOutputDao.getById(id = id)
            ?: return@execute Result.failure(Exception("Output is not found"))

        Result.success(entity.toModel(fileManager))
    }

    suspend fun delete(id: String) = backgroundExecutor.execute {
        cachedOutputMap.remove(id)
        generationOutputDao.deleteById(id = id)
        Result.success(Unit)
    }

    private suspend fun GenerationInput.uploadFilesIntoCloud(): GenerationInput = copy(
        params = params.mapValues { (_, param) ->
            param.mapFileUploadUrls { fileNameWithExtension ->
                val fileAbsolutePath =
                    fileManager.getAbsoluteFilePathRelativeToInternal(fileNameWithExtension)
                val file = fileManager.getPlatformFile(fileAbsolutePath)
                val fileBytes = file.readBytes()

                val fileUploadProgress = KMPStorage.putFile {
                    source { bytes(fileBytes) }
                    destination {
                        folder = "temporary_files"
                        fileName = file.nameWithoutExtension
                    }
                    contentType = file.mimeType().toString()
                }
                val uploadedUrl =
                    (fileUploadProgress as? FileUploadProgress.Completed)?.result?.url
                AppLogger.d("File is uploaded in cloud. Url: $uploadedUrl")
                uploadedUrl ?: ""
            }
        },
    )
}
