package ca.bc.gov.mobileauthentication.data.models

import com.google.gson.annotations.SerializedName

/**
 * Created by Aidan Laing on 2018-01-18.
 *
 */
data class Token(
        @SerializedName("access_token") val accessToken: String?,
        @SerializedName("expires_in") val expiresIn: Long?,
        @SerializedName("refresh_expires_in") val refreshExpiresIn: Long?,
        @SerializedName("refresh_token") val refreshToken: String?,
        @SerializedName("token_type") val bearer: String?,
        @SerializedName("id_token") val idToken: String?,
        @SerializedName("not-before-policy") val notBeforePolicy: Long?,
        @SerializedName("session_state") val sessionState: String?,
        @SerializedName("expires_at") val expiresAt: Long? = System.currentTimeMillis() + ((expiresIn ?: 0) * 1000),
        @SerializedName("refresh_expires_at") val refreshExpiresAt: Long? = System.currentTimeMillis() + ((refreshExpiresIn ?: 0) * 1000)
) {

    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (expiresAt == null) return true
        return expiresAt > currentTime
    }

    fun isRefreshExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        if (refreshExpiresAt == null) return true
        return refreshExpiresAt > currentTime
    }

}