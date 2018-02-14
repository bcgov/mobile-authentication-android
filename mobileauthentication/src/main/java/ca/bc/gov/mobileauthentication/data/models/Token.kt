package ca.bc.gov.mobileauthentication.data.models

import com.google.gson.annotations.SerializedName

/**
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Created by Aidan Laing on 2017-12-12.
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
        @SerializedName("expires_at") var expiresAt: Long?,
        @SerializedName("refresh_expires_at") var refreshExpiresAt: Long?
) {

    fun isExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        val currExpiresAt = expiresAt ?: return true
        return currExpiresAt < currentTime
    }

    fun isRefreshExpired(currentTime: Long = System.currentTimeMillis()): Boolean {
        val currRefreshExpiresAt = refreshExpiresAt ?: return true
        return currRefreshExpiresAt < currentTime
    }

}