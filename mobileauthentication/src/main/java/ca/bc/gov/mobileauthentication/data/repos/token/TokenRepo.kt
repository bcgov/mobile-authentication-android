package ca.bc.gov.mobileauthentication.data.repos.token

import ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException
import ca.bc.gov.mobileauthentication.data.models.Token
import io.reactivex.Observable

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
class TokenRepo
private constructor(
        private val remoteDataSource: TokenDataSource,
        private val localDataSource: TokenDataSource
) : TokenDataSource {

    companion object {

        @Volatile
        private var INSTANCE: TokenRepo? = null

        fun getInstance(
                remoteDataSource: TokenDataSource,
                localDataSource: TokenDataSource
        ): TokenRepo = INSTANCE ?: synchronized(this) {
            INSTANCE ?: TokenRepo(remoteDataSource, localDataSource)
                    .also { INSTANCE = it }
        }

        fun destroyInstance() {
            INSTANCE = null
        }
    }

    /**
     * Gets token from remote if code IS NOT null
     * Gets token from local if code IS null
     * Returns token if valid
     * If token from local db refresh is expired then @see ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException will be thrown.
     * If refresh token is not expired and token is expired then token will be refreshed and returned
     */
    override fun getToken(code: String?): Observable<Token> {
        return if (code != null) {
            remoteDataSource.getToken(code)
                    .flatMap { localDataSource.saveToken(it) }
        } else {
            localDataSource.getToken()
                    .flatMap {
                        when {
                            it.isRefreshExpired() -> Observable.error(RefreshExpiredException())
                            it.isExpired() -> refreshToken(it)
                            else -> Observable.just(it)
                        }
                    }
        }
    }

    /**
     * Saves token locally
     */
    override fun saveToken(token: Token): Observable<Token> {
        return localDataSource.saveToken(token)
    }

    /**
     * Refreshes token and saves to local db
     * If passed token refresh token is expired then @see ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException will be thrown.
     */
    override fun refreshToken(token: Token): Observable<Token> {
        return if (token.isRefreshExpired()) Observable.error(RefreshExpiredException())
        else remoteDataSource.refreshToken(token)
                .flatMap { localDataSource.saveToken(it) }
    }

    /**
     * Deletes token from local db
     */
    override fun deleteToken(): Observable<Boolean> {
        return localDataSource.deleteToken()
    }
}