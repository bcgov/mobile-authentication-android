package ca.bc.gov.mobileauthentication.data

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import ca.bc.gov.mobileauthentication.common.utils.UrlUtils
import ca.bc.gov.mobileauthentication.data.models.Token
import io.reactivex.Observable
import io.reactivex.schedulers.Schedulers
import net.openid.appauth.*


class AppAuthApi(private val context: Context,
                 baseUrl: String,
                 private val realmName: String,
                 authEndpoint: String,
                 redirectUri: String,
                 private val clientId: String,
                 hint: String = "") {

    companion object {
        const val REFRESH_EXPIRES_IN = "refresh_expires_in"
        const val NOT_BEFORE_POLICY = "not-before-policy"
        const val SESSION_STATE = "session_state"
    }

    private fun buildTokenUrl(baseUrl: String): String {
        return UrlUtils.cleanBaseUrl(baseUrl) + "auth/realms/$realmName/protocol/openid-connect/token"
    }

    private val authorizationConfig: AuthorizationServiceConfiguration
    private val authorizationRequest: AuthorizationRequest

    init {
        authorizationConfig = AuthorizationServiceConfiguration(
                Uri.parse(authEndpoint),
                Uri.parse(buildTokenUrl(baseUrl)))

        val reqBuilder = AuthorizationRequest.Builder(
                authorizationConfig,
                clientId,
                ResponseTypeValues.CODE,
                Uri.parse(redirectUri))

        if (hint.isNotEmpty())
            reqBuilder.setLoginHint(hint)

        authorizationRequest = reqBuilder.build()
    }

    private fun buildRefreshTokenRequest(token: Token): TokenRequest {
        return TokenRequest.Builder(authorizationConfig, clientId)
                .setGrantType(GrantTypeValues.REFRESH_TOKEN)
                .setRefreshToken(token.refreshToken)
                .build()
    }

    private fun convertToToken(tokenResponse: TokenResponse): Token {
        val current = System.currentTimeMillis()
        val currentUnixTime = current / 1000

        val accessExpirationUnixTimeMillis = tokenResponse.accessTokenExpirationTime
        val accessExpirationUnixTime = accessExpirationUnixTimeMillis?.div(1000)
        val accessExpiresInSeconds = accessExpirationUnixTime?.minus(currentUnixTime)
        val accessExpiresInMillis = accessExpiresInSeconds?.times(1000)

        val refreshExpiresInSeconds = tokenResponse.additionalParameters[REFRESH_EXPIRES_IN]?.toLong()
        val refreshExpirationUnixTime = refreshExpiresInSeconds?.plus(currentUnixTime)
        val refreshExpirationUnixTimeMillis = refreshExpirationUnixTime?.times(1000)

        return Token(
                tokenResponse.accessToken,
                accessExpiresInMillis,
                refreshExpiresInSeconds,
                tokenResponse.refreshToken,
                tokenResponse.tokenType,
                tokenResponse.idToken,
                tokenResponse.additionalParameters[NOT_BEFORE_POLICY]?.toLong(),
                tokenResponse.additionalParameters[SESSION_STATE],
                accessExpirationUnixTimeMillis,
                refreshExpirationUnixTimeMillis
        )
    }

    /**
     * Using configuration provided to the Constructor: builds an Intent that will be used to
     * initiate the OAuth authentication flow (using startActivityForResult)
     */
    fun getAuthRequestIntent(customTabsIntent: CustomTabsIntent? = null): Intent {
        val service = AuthorizationService(context)

        return if (customTabsIntent != null)
            service.getAuthorizationRequestIntent(authorizationRequest, customTabsIntent)
        else
            service.getAuthorizationRequestIntent(authorizationRequest)
    }

    private fun performTokenRequest(tokenReq: TokenRequest): Observable<Token> {
        return Observable.create {
            AuthorizationService(context).performTokenRequest(tokenReq) { response, ex ->
                if (response != null)
                    it.onNext(convertToToken(response))
                else if (ex != null)
                    it.onError(ex)
            }
        }.subscribeOn(Schedulers.io())
    }

    /**
     * Makes an OAuth request to exchange an Authorization code for an access token.
     */
    fun getToken(authResponse: AuthorizationResponse): Observable<Token> {
        return performTokenRequest(authResponse.createTokenExchangeRequest())
    }

    /**
     * Using the provided refresh token: makes an OAuth request to exchange for a new access token.
     */
    fun refreshToken(token: Token): Observable<Token> {
        return performTokenRequest(buildRefreshTokenRequest(token))
    }
}