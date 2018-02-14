package ca.bc.gov.mobileauthentication.screens.redirect

import ca.bc.gov.mobileauthentication.RxImmediateSchedulerRule
import ca.bc.gov.mobileauthentication.data.models.Token
import ca.bc.gov.mobileauthentication.data.repos.token.TokenRepo
import com.google.gson.Gson
import com.nhaarman.mockito_kotlin.*
import io.reactivex.Observable
import org.junit.After
import org.junit.Test

import org.junit.Before
import org.junit.ClassRule
import org.junit.Assert.*

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
class RedirectPresenterTest {

    companion object {
        @ClassRule
        @JvmField
        val rxSchedulers = RxImmediateSchedulerRule()
    }

    private lateinit var view: RedirectContract.View

    private lateinit var tokenRepo: TokenRepo

    private lateinit var gson: Gson

    private val authEndpoint = "http://helloworld.com"
    private val redirectUri = "hello://world"
    private val clientId = "abc123"
    private val responseType = "code"

    private lateinit var presenter: RedirectPresenter

    @Before
    fun setUp() {
        view = mock()

        tokenRepo = mock()

        gson = mock()

        presenter = RedirectPresenter(view, authEndpoint, redirectUri, clientId, responseType,
                tokenRepo, gson)
    }

    @After
    fun tearDown() {
        TokenRepo.destroyInstance()
    }

    @Test
    fun presenterSet() {
        verify(view).presenter = presenter
    }

    @Test
    fun subscribe() {
        presenter.subscribe()

        verify(view).setUpLoginListener()
    }

    @Test
    fun loginClickedNotLoading() {
        whenever(view.loading).thenReturn(false)

        presenter.loginClicked()

        verify(view).loading
        verify(view).loadWithChrome(any())
    }

    @Test
    fun loginClickedLoading() {
        whenever(view.loading).thenReturn(true)

        presenter.loginClicked()

        verify(view).loading
        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun buildAuthUrl() {
        val expected = "$authEndpoint?response_type=$responseType&client_id=$clientId&redirect_uri=$redirectUri"
        val actual = presenter.buildAuthUrl()

        assertEquals(expected, actual)
    }

    @Test
    fun redirectReceived() {
        val code = "123"
        val token = Token("opensesame",null,null,null,
                null,null,null,null, 3000L, 4000L)
        whenever(tokenRepo.getToken(code)).thenReturn(Observable.just(token))

        val tokenJson = "{ \"accessToken\" : \"opensesame\"}"
        whenever(gson.toJson(token)).thenReturn(tokenJson)

        val redirectUrl = "http://www.foobar.com?code=$code"

        presenter.redirectReceived(redirectUrl)

        verify(view).setResultSuccess(tokenJson)
        verify(view).finish()
    }

    @Test
    fun redirectReceivedNoCode() {
        val redirectUrl = "http://www.foobar.com"

        presenter.redirectReceived(redirectUrl)

        verify(view).presenter = presenter
        verifyNoMoreInteractions(view)
    }

    @Test
    fun getToken() {
        val code = "123"
        val token = Token("opensesame",null,null,null,
                null,null,null,null, 3000L, 4000L)
        whenever(tokenRepo.getToken(code)).thenReturn(Observable.just(token))

        val tokenJson = "{ \"accessToken\" : \"opensesame\"}"
        whenever(gson.toJson(token)).thenReturn(tokenJson)

        presenter.getToken(code)

        verify(view).setResultSuccess(tokenJson)
        verify(view).finish()
    }

    @Test
    fun setViewLoginMode() {
        presenter.setViewLoginMode()

        verify(view).loading = false
        verify(view).hideLoading()
        verify(view).setLoginTextLogin()
    }

    @Test
    fun setViewLoadingMode() {
        presenter.setViewLoadingMode()

        verify(view).loading = true
        verify(view).showLoading()
        verify(view).setLoginTextLoggingIn()
    }

}