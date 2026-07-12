package com.kotlinfoundation.koko.data.source.remote.response

import com.kotlinfoundation.koko.data.source.remote.CustomHttpStatusCode
import com.kotlinfoundation.koko.domain.exceptions.CreditRequiredException
import com.kotlinfoundation.koko.domain.exceptions.PurchaseRequiredException
import com.kotlinfoundation.koko.domain.exceptions.UnAuthorizedException
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Standard envelope for the app's backend responses. [handleAsResult] is the boundary that turns
 * HTTP/custom status codes into domain exceptions (auth/purchase/credit) so the rest of the app
 * only deals with `Result` + domain types.
 */
@Serializable
data class BaseApiResponse<T>(
    @SerialName("statusCode") val statusCode: Int? = null,
    @SerialName("errorMessage") val errorMessage: String? = null,
    @SerialName("data") val data: T? = null,
) {

    val isSuccessful: Boolean get() = statusCode in 200..299

    fun <A> handleAsResult(onSuccess: (T?) -> Result<A>): Result<A> = when (statusCode) {
        in 200..299 -> {
            onSuccess(data)
        }

        401, 403 -> Result.failure(UnAuthorizedException())

        CustomHttpStatusCode.PURCHASE_REQUIRED -> Result.failure(PurchaseRequiredException())

        CustomHttpStatusCode.CREDIT_REQUIRED -> Result.failure(CreditRequiredException())

        else -> Result.failure(Exception("Error: ${errorMessage ?: ""}"))
    }
}
