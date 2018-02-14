package ca.bc.gov.mobileauthentication.data

import ca.bc.gov.mobileauthentication.data.models.Token
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST
import retrofit2.http.Path

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
interface AuthApi {

    /**
     * OAuth2 get Token call
     */
    @POST("/auth/realms/{realm_name}/protocol/openid-connect/token")
    @FormUrlEncoded
    fun getToken(
            @Path("realm_name") realmName: String,
            @Field("grant_type") grantType: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("client_id") client_id: String,
            @Field("code") code: String
    ): Observable<Token>

    /**
     * OAuth2 refresh Token call
     */
    @POST("/auth/realms/{realm_name}/protocol/openid-connect/token")
    @FormUrlEncoded
    fun refreshToken(
            @Path("realm_name") realmName: String,
            @Field("redirect_uri") redirectUri: String,
            @Field("client_id") client_id: String,
            @Field("refresh_token") refreshToken: String,
            @Field("grant_type") grantType: String = "refresh_token"
    ): Observable<Token>

}