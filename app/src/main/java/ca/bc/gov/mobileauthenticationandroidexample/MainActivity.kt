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
        val redirectUri = "bcgov://android"

        client = MobileAuthenticationClient(this, baseUrl, realmName,
                authEndpoint, redirectUri, clientId)

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
                Log.d(tag, "Get Success")
                refreshToken()
            }
        })
    }

    private fun refreshToken() {
        client?.refreshToken(object: MobileAuthenticationClient.TokenCallback {
            override fun onError(throwable: Throwable) {
                Log.e(tag, throwable.message)
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
                Log.d(tag, "Refresh Success")
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
                Log.d(tag, "Delete Success")
            }
        })
    }
}
