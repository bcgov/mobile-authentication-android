package ca.bc.gov.mobileauthentication.data.repos.token

import ca.bc.gov.mobileauthentication.common.exceptions.InvalidOperationException
import ca.bc.gov.mobileauthentication.data.models.Token
import com.google.gson.Gson
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
class TokenLocalDataSource
private constructor(
        private val gson: Gson,
        private val secureSharedPrefs: SecureSharedPrefs
) : TokenDataSource {

    companion object {

        private const val TOKEN_KEY = "TOKEN_KEY"

        @Volatile
        private var INSTANCE: TokenLocalDataSource? = null

        fun getInstance(gson: Gson, secureSharedPrefs: SecureSharedPrefs): TokenLocalDataSource =
                INSTANCE ?: synchronized(this) {
                    INSTANCE ?: TokenLocalDataSource(gson, secureSharedPrefs)
                            .also { INSTANCE = it }
                }
    }

    /**
     * Gets token from local db and returns
     */
    override fun getToken(authResponse: AuthorizationResponse?): Observable<Token> {
        return Observable.create { emitter ->
            val tokenJson = secureSharedPrefs.getString(TOKEN_KEY)
            if (tokenJson.isNotBlank()) {
                val token: Token = gson.fromJson(tokenJson, Token::class.java)
                emitter.onNext(token)
            }
            emitter.onComplete()
        }
    }

    /**
     * Saves token to local db and returns saved version
     */
    override fun saveToken(token: Token): Observable<Token> {
        return Observable.create { emitter ->
            val tokenJson = gson.toJson(token)
            secureSharedPrefs.saveString(TOKEN_KEY, tokenJson)

            val savedTokenJson = secureSharedPrefs.getString(TOKEN_KEY)
            val savedToken: Token = gson.fromJson(savedTokenJson, Token::class.java)
            emitter.onNext(savedToken)
            emitter.onComplete()
        }
    }

    /**
     * Invalid operation for local data source
     */
    override fun refreshToken(token: Token): Observable<Token> {
        return Observable.error(InvalidOperationException())
    }

    /**
     * Deletes token form local db
     */
    override fun deleteToken(): Observable<Boolean> {
        return Observable.create { emitter ->
            secureSharedPrefs.deleteString(TOKEN_KEY)
            emitter.onNext(true)
            emitter.onComplete()
        }
    }
}