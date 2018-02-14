package ca.bc.gov.mobileauthentication.common.utils

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
object UrlUtils {

    /**
     * Checks to see if base url ends with a /
     * If the url ends with a / then return the passed url
     * If the url DOES NOT end with a / then return the passed url concatenated with /
     */
    fun cleanBaseUrl(baseUrl: String): String {
        return if (!baseUrl.endsWith("/")) {
            var cleanedBaseUrl = baseUrl
            cleanedBaseUrl += "/"
            cleanedBaseUrl
        } else {
            baseUrl
        }
    }

    /**
     * Extracts code query param form url by taking a substring between
     * code= and the first & or the end of the String
     */
    fun extractCode(codeUrl: String): String {
        return if (codeUrl.contains("code=".toRegex())) {
            codeUrl.substringAfter("code=").substringBefore("&")
        } else ""
    }

}