package ca.bc.gov.mobileauthentication.data.repos.token

import ca.bc.gov.mobileauthentication.common.exceptions.InvalidOperationException
import ca.bc.gov.mobileauthentication.common.exceptions.NoCodeException
import ca.bc.gov.mobileauthentication.common.exceptions.NoRefreshTokenException
import ca.bc.gov.mobileauthentication.data.AppAuthApi
import ca.bc.gov.mobileauthentication.data.models.Token
import io.reactivex.Observable
import net.openid.appauth.AuthorizationResponse

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
class TokenRemoteDataSource
private constructor(private val authApi: AppAuthApi) : TokenDataSource {

    companion object {

        @Volatile
        private var INSTANCE: TokenRemoteDataSource? = null

        fun getInstance(authApi: AppAuthApi): TokenRemoteDataSource = INSTANCE ?: synchronized(this) {
            INSTANCE ?: TokenRemoteDataSource(authApi).also { INSTANCE = it }
        }
    }

    /**
     * Exchanges code for token using authentication api
     * Returns error if code is null
     */
    override fun getToken(authResponse: AuthorizationResponse?): Observable<Token> {
        if (authResponse == null)
            return Observable.error(NoCodeException())

        return authApi.getToken(authResponse)
    }

    /**
     * Invalid operation for remote data source
     */
    override fun saveToken(token: Token): Observable<Token> {
        return Observable.error(InvalidOperationException())
    }

    /**
     * Refreshes token using authentication api
     * Returns error if there is no refresh token
     */
    override fun refreshToken(token: Token): Observable<Token> {
        if (token.refreshToken == null)
            return Observable.error(NoRefreshTokenException())

        return authApi.refreshToken(token)
    }
    /**
     * Invalid operation for remote data source
     */
    override fun deleteToken(): Observable<Boolean> {
        return Observable.error(InvalidOperationException())
    }
}