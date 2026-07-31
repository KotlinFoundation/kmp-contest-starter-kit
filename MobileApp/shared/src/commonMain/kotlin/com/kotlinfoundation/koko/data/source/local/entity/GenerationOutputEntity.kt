@file:OptIn(ExperimentalTime::class, ExperimentalUuidApi::class)

package com.kotlinfoundation.koko.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.kotlinfoundation.koko.domain.model.generation.GenerationFile
import com.kotlinfoundation.koko.domain.model.generation.GenerationInput
import com.kotlinfoundation.koko.domain.model.generation.GenerationOutput
import com.kotlinfoundation.koko.domain.model.generation.GenerationParam
import com.kotlinfoundation.koko.util.file.FileManager
import kotlin.time.Clock
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Room row for a past generation. Input params (incl. file refs) are flattened into one
// string column (input_params_as_string) via GenerationParam.serialize. The toEntity/toModel
// mappers below convert entity <-> domain; toModel needs FileManager to resolve absolute paths.
@Entity(tableName = "generation_outputs")
data class GenerationOutputEntity(
    @PrimaryKey @ColumnInfo(name = "id") val id: String = Uuid.random().toString(),
    @ColumnInfo(name = "user_input") val userInput: String,
    @ColumnInfo(name = "input_params_as_string") val inputParamsAsString: String = "",
    @ColumnInfo(name = "output_file_name") val outputFileName: String? = null,
    @ColumnInfo(name = "output_file_type") val outputFileType: String? = null,
    @ColumnInfo("created_at") val createdAt: Long = Clock.System.now()
        .toEpochMilliseconds(),
    @ColumnInfo(name = "status") val status: GenerationOutput.Status? = null,
)

fun GenerationOutput.toEntity(): GenerationOutputEntity = GenerationOutputEntity(
    id = id,
    userInput = input.userInput,
    inputParamsAsString = GenerationParam.serialize(input.params),
    outputFileType = outputFile?.type?.value,
    outputFileName = outputFile?.fileNameWithExtension,
    createdAt = createdAt,
    status = status,
)

fun GenerationOutputEntity.toModel(fileManager: FileManager): GenerationOutput {
    val inputParams = GenerationParam.deserialize(inputParamsAsString)
    val outputFile = when {
        outputFileName == null || outputFileType == null -> null

        else -> GenerationFile(
            fileNameWithExtension = outputFileName,
            type = GenerationFile.Type.fromValue(outputFileType),
        )
    }

    val generationInput = GenerationInput(
        userInput = userInput,
        params = inputParams.mapValues { (_, param) ->
            param.mapFilePaths { fileManager.getAbsoluteFilePathRelativeToInternal(it) }
        },
    )

    return GenerationOutput(
        id = id,
        input = generationInput,
        outputFile = outputFile,
        output = outputFileName?.let { fileManager.getAbsoluteFilePathRelativeToInternal(it) },
        createdAt = createdAt,
        status = status,
    )
}
