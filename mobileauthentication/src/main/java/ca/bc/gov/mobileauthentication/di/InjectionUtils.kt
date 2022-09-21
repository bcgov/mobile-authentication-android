package ca.bc.gov.mobileauthentication.di

import android.content.SharedPreferences
import ca.bc.gov.mobileauthentication.data.AppAuthApi
import ca.bc.gov.mobileauthentication.data.repos.token.SecureSharedPrefs
import ca.bc.gov.mobileauthentication.data.repos.token.TokenLocalDataSource
import ca.bc.gov.mobileauthentication.data.repos.token.TokenRemoteDataSource
import ca.bc.gov.mobileauthentication.data.repos.token.TokenRepo
import com.google.gson.Gson
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
     * Gets Token Repo with standard params
     */
    fun getTokenRepo(
            authApi: AppAuthApi,
            sharedPreferences: SharedPreferences,
            gson: Gson = Injection.provideGson(),
            keyStore: KeyStore = Injection.provideKeyStore(),
            secureSharedPrefs: SecureSharedPrefs = Injection.provideSecureSharedPrefs(keyStore, sharedPreferences)
    ): TokenRepo = TokenRepo.getInstance(
            TokenRemoteDataSource.getInstance(authApi),
            TokenLocalDataSource.getInstance(gson, secureSharedPrefs)
    )
}