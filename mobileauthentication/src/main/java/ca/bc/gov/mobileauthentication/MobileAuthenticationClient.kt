package ca.bc.gov.mobileauthentication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.preference.PreferenceManager
import ca.bc.gov.mobileauthentication.common.exceptions.TokenNotFoundException
import ca.bc.gov.mobileauthentication.data.AppAuthApi
import ca.bc.gov.mobileauthentication.data.models.Token
import ca.bc.gov.mobileauthentication.data.repos.token.TokenRepo
import ca.bc.gov.mobileauthentication.di.Injection
import ca.bc.gov.mobileauthentication.di.InjectionUtils
import ca.bc.gov.mobileauthentication.screens.redirect.RedirectActivity
import com.google.gson.Gson
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers

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
class MobileAuthenticationClient(
        private val context: Context,
        override val baseUrl: String,
        override val realmName: String,
        override val authEndpoint: String,
        override val redirectUri: String,
        override val clientId: String,
        override val hint: String = ""
) : MobileAuthenticationContract {

    private val disposables = CompositeDisposable()

    private val gson: Gson = Injection.provideGson()
    private val appauthApi: AppAuthApi = AppAuthApi(context, baseUrl, realmName, authEndpoint, redirectUri, clientId, hint)
    private val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context.applicationContext)
    private val tokenRepo: TokenRepo = InjectionUtils.getTokenRepo(appauthApi, sharedPrefs)

    private var passedRequestCode: Int = DEFAULT_REQUEST_CODE

    /**
     * Launches intent to activity which will handle OAuth2 Authorization Code Flow
     */
    override fun authenticate(requestCode: Int) {
        this.passedRequestCode = requestCode

        Intent(context, RedirectActivity::class.java)
                .putExtra(RedirectActivity.BASE_URL, baseUrl)
                .putExtra(RedirectActivity.REALM_NAME, realmName)
                .putExtra(RedirectActivity.AUTH_ENDPOINT, authEndpoint)
                .putExtra(RedirectActivity.REDIRECT_URI, redirectUri)
                .putExtra(RedirectActivity.CLIENT_ID, clientId)
                .putExtra(RedirectActivity.HINT, hint)
                .run { (context as Activity).startActivityForResult(this, requestCode) }
    }

    /**
     * Handles on activity result and determines if the authentication was successful
     * or an error occurred.
     */
    override fun handleAuthResult(requestCode: Int, resultCode: Int, data: Intent?,
                                  tokenCallback: TokenCallback) {
        if (requestCode != passedRequestCode)
            return

        if (resultCode != Activity.RESULT_OK) {
            val message = data?.getStringExtra(ERROR_MESSAGE) ?: "Unknown authentication error"
            tokenCallback.onError(Throwable(message))
            return
        }

        if (data == null) {
            tokenCallback.onError(Throwable("Result OK but authentication response is malformed"))
            return
        }

        handleConsumerResult(data, tokenCallback)
    }

    private fun handleConsumerResult(data: Intent, tokenCallback: TokenCallback) {
        val success = data.getBooleanExtra(SUCCESS, false)
        if (success) {
            val tokenJson = data.getStringExtra(TOKEN_JSON)
            val token: Token = gson.fromJson(tokenJson, Token::class.java)
            tokenCallback.onSuccess(token)
        } else {
            val errorMessage = data.getStringExtra(ERROR_MESSAGE)
            tokenCallback.onError(Throwable(errorMessage))
        }
    }

    /**
     * Gets Token from local storage.
     * Token will be automatically refreshed if refresh token is not expired.
     * If refresh token is expired a @see ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException will be thrown
     * If refresh token does not exist then @see ca.bc.gov.mobileauthentication.common.exceptions.NoRefreshTokenException will be thrown
     * If a token does not exist a @see ca.bc.gov.mobileauthentication.common.exceptions.TokenNotFoundException will be thrown
     */
    override fun getToken(tokenCallback: TokenCallback) {
        tokenRepo.getToken(null)
                .firstElement()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    tokenCallback.onError(it)
                },
                onSuccess = { token ->
                    tokenCallback.onSuccess(token)
                },
                onComplete = {
                    tokenCallback.onError(TokenNotFoundException())
                }
        ).addTo(disposables)
    }

    /**
     * Gets Token from local storage as a RxJava2 Observable
     */
    override fun getTokenAsObservable(): Observable<Token> = tokenRepo.getToken(null)

    /**
     * Refreshes token
     * If refresh token is expired a @see ca.bc.gov.mobileauthentication.common.exceptions.RefreshExpiredException will be thrown
     * If refresh token does not exist then @see ca.bc.gov.mobileauthentication.common.exceptions.NoRefreshTokenException will be thrown
     */
    override fun refreshToken(tokenCallback: TokenCallback) {
        tokenRepo.getToken()
                .flatMap { token -> tokenRepo.refreshToken(token) }
                .firstOrError()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribeBy(
                onError = {
                    tokenCallback.onError(it)
                },
                onSuccess = { token ->
                    tokenCallback.onSuccess(token)
                }
        ).addTo(disposables)
    }

    /**
     * Refreshes Token that is stored in local storage as a RxJava2 Observable
     */
    override fun refreshTokenAsObservable(): Observable<Token> = tokenRepo.getToken(null)
            .flatMap { token -> tokenRepo.refreshToken(token) }

    /**
     * Deletes token from local storage
     */
    override fun deleteToken(deleteCallback: DeleteCallback) {
        tokenRepo.deleteToken()
                .ignoreElements().subscribeBy(
                onError = {
                    deleteCallback.onError(it)
                },
                onComplete = {
                    deleteCallback.onSuccess()
                }
        ).addTo(disposables)
    }

    /**
     * Deletes token from local storage as a RxJava2 Observable
     */
    override fun deleteTokenAsObservable(): Observable<Boolean> = tokenRepo.deleteToken()

    /**
     * Clears all current callbacks
     */
    override fun clear() {
        disposables.clear()
    }

    interface TokenCallback {
        fun onError(throwable: Throwable)
        fun onSuccess(token: Token)
    }

    interface DeleteCallback {
        fun onError(throwable: Throwable)
        fun onSuccess()
    }

    companion object {
        const val DEFAULT_REQUEST_CODE = 1012
        const val APPAUTH_REQUEST_CODE = 2024
        const val SUCCESS = "SUCCESS"
        const val ERROR_MESSAGE = "ERROR_MESSAGE"
        const val TOKEN_JSON = "TOKEN_JSON"
    }
}