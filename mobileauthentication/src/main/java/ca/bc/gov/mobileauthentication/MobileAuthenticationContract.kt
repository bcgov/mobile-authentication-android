package ca.bc.gov.mobileauthentication

import android.content.Intent
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
interface MobileAuthenticationContract {

    val baseUrl: String
    val realmName: String
    val authEndpoint: String
    val redirectUri: String
    val clientId: String
    val hint: String

    fun authenticate(requestCode: Int = MobileAuthenticationClient.DEFAULT_REQUEST_CODE)

    fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?,
                         tokenCallback: MobileAuthenticationClient.TokenCallback)

    fun getToken(tokenCallback: MobileAuthenticationClient.TokenCallback)
    fun getTokenAsObservable(): Observable<Token>

    fun refreshToken(tokenCallback: MobileAuthenticationClient.TokenCallback)
    fun refreshTokenAsObservable(): Observable<Token>

    fun deleteToken(deleteCallback: MobileAuthenticationClient.DeleteCallback)
    fun deleteTokenAsObservable(): Observable<Boolean>

    fun clear()

}