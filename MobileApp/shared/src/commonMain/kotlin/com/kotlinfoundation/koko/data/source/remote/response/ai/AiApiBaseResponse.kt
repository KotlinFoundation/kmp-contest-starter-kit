package com.kotlinfoundation.koko.data.source.remote.response.ai

import com.kotlinfoundation.koko.data.source.remote.CustomHttpStatusCode
import com.kotlinfoundation.koko.domain.exceptions.CreditRequiredException
import com.kotlinfoundation.koko.domain.exceptions.PurchaseRequiredException
import com.kotlinfoundation.koko.domain.exceptions.UnAuthorizedException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Response envelope for the AI proxy cloud functions. Like [com.kotlinfoundation.koko.data.source.remote.response.BaseApiResponse]
 * but with a `suspend` [handleAsResult] (downloads/file work happens in the success branch).
 * Maps status/custom codes to domain exceptions at the data boundary.
 */
@Serializable
data class AiApiBaseResponse<T>(
    @SerialName("statusCode") val statusCode: Int? = null,
    @SerialName("errorMessage") val errorMessage: String? = null,
    @SerialName("data") val data: T? = null,
) {
    val isSuccessful: Boolean get() = statusCode in 200..299

    suspend fun <A> handleAsResult(onSuccess: suspend (T?) -> Result<A>): Result<A> = when (statusCode) {
        in 200..299 -> {
            onSuccess(data)
        }

        401, 403 -> Result.failure(UnAuthorizedException())

        CustomHttpStatusCode.PURCHASE_REQUIRED -> Result.failure(PurchaseRequiredException())

        CustomHttpStatusCode.CREDIT_REQUIRED -> Result.failure(CreditRequiredException())

        else -> Result.failure(Exception("Server Error: ${errorMessage ?: ""}"))
    }
}
