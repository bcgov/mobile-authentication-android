package ca.bc.gov.mobileauthenticationandroidexample

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import ca.bc.gov.mobileauthentication.MobileAuthenticationClient
import ca.bc.gov.mobileauthentication.common.exceptions.NoRefreshTokenException
import ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException
import ca.bc.gov.mobileauthentication.common.exceptions.TokenNotFoundException
import ca.bc.gov.mobileauthentication.data.models.Token

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
class MainActivity : AppCompatActivity() {

    private var client: MobileAuthenticationClient? = null

    private val tag = "MOBILE_AUTH"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val authEndpoint = "https://dev-sso.pathfinder.gov.bc.ca/auth/realms/mobile/protocol/openid-connect/auth"
        val baseUrl = "https://dev-sso.pathfinder.gov.bc.ca/"
        val clientId = "secure-image"
        val realmName = "mobile"
        val redirectUri = "secure-image://"
        val hint = "idir"

        client = MobileAuthenticationClient(this, baseUrl, realmName,
                authEndpoint, redirectUri, clientId, hint)

        client?.authenticate()
    }

    override fun onDestroy() {
        super.onDestroy()
        client?.clear()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        client?.handleAuthResult(requestCode, resultCode, data, object: MobileAuthenticationClient.TokenCallback {
            override fun onError(throwable: Throwable) {
                Log.e(tag, throwable.message)
            }
            override fun onSuccess(token: Token) {
                Log.d(tag, "Authenticate Success")
                getToken()
            }
        })
    }

    private fun getToken() {
        client?.getToken(object: MobileAuthenticationClient.TokenCallback {
            override fun onError(throwable: Throwable) {
                when (throwable) {
                    is RefreshExpiredException -> {
                        Log.e(tag, "Refresh token is expired. Please re-authenticate.")
                    }
                    is NoRefreshTokenException -> {
                        Log.e(tag, "No Refresh token associated with Token")
                    }
                    is TokenNotFoundException -> {
                        Log.e(tag, "No Token was found")
                    }
                }
            }

            override fun onSuccess(token: Token) {
                Log.d(tag, "Get Token Success")
                refreshToken()
            }
        })
    }

    private fun refreshToken() {
        client?.refreshToken(object: MobileAuthenticationClient.TokenCallback {
            override fun onError(throwable: Throwable) {
                when (throwable) {
                    is RefreshExpiredException -> {
                        Log.e(tag, "Refresh token is expired. Please re-authenticate.")
                    }
                    is NoRefreshTokenException -> {
                        Log.e(tag, "No Refresh token associated with Token")
                    }
                }
            }

            override fun onSuccess(token: Token) {
                Log.d(tag, "Refresh Token Success")
                deleteToken()
            }
        })
    }

    private fun deleteToken() {
        client?.deleteToken(object: MobileAuthenticationClient.DeleteCallback {
            override fun onError(throwable: Throwable) {
                Log.e(tag, throwable.message)
            }

            override fun onSuccess() {
                Log.d(tag, "Delete Token Success")
            }
        })
    }
}
