package ca.bc.gov.mobileauthentication.di

import android.content.SharedPreferences
import ca.bc.gov.mobileauthentication.data.AuthApi
import ca.bc.gov.mobileauthentication.common.Constants
import ca.bc.gov.mobileauthentication.data.AppAuthApi
import ca.bc.gov.mobileauthentication.data.repos.token.SecureSharedPrefs
import ca.bc.gov.mobileauthentication.data.repos.token.TokenLocalDataSource
import ca.bc.gov.mobileauthentication.data.repos.token.TokenRemoteDataSource
import ca.bc.gov.mobileauthentication.data.repos.token.TokenRepo
import com.google.gson.Gson
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.CallAdapter
import retrofit2.Converter
import retrofit2.Retrofit
import java.security.KeyStore

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
object InjectionUtils {

    /**
     * Gets Auth Api with standard params
     */
    fun getAuthApi(
            apiDomain: String,
            gson: Gson = Injection.provideGson(),
            converterFactory: Converter.Factory = Injection.provideConverterFactory(gson),
            callAdapterFactory : CallAdapter.Factory = Injection.provideCallAdapterFactory(),
            readTimeOut: Long = Constants.READ_TIME_OUT,
            connectTimeOut: Long = Constants.CONNECT_TIME_OUT,
            loggingLevel: HttpLoggingInterceptor.Level = HttpLoggingInterceptor.Level.BODY,
            httpLoggingInterceptor: HttpLoggingInterceptor = Injection.provideHttpLoggingInterceptor(
                    loggingLevel),
            okHttpClient: OkHttpClient = Injection.provideOkHttpClient(
                    readTimeOut, connectTimeOut, httpLoggingInterceptor),
            retrofit: Retrofit = Injection.provideRetrofit(
                    apiDomain, okHttpClient, converterFactory, callAdapterFactory)
    ): AuthApi = Injection.provideAuthApi(retrofit)

    /**
     * Gets Token Repo with standard params
     */
    fun getTokenRepo(
            authApi: AppAuthApi,
            realmName: String,
            grantType: String,
            redirectUri: String,
            clientId: String,
            sharedPreferences: SharedPreferences,
            gson: Gson = Injection.provideGson(),
            keyStore: KeyStore = Injection.provideKeyStore(),
            secureSharedPrefs: SecureSharedPrefs = Injection.provideSecureSharedPrefs(keyStore, sharedPreferences)
    ): TokenRepo = TokenRepo.getInstance(
            TokenRemoteDataSource.getInstance(authApi, realmName, grantType, redirectUri, clientId),
            TokenLocalDataSource.getInstance(gson, secureSharedPrefs)
    )
}