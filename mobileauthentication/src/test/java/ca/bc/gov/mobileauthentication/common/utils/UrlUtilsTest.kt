package ca.bc.gov.mobileauthentication.common.utils

import org.junit.Test

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
class UrlUtilsTest {

    @Test
    fun cleanBaseUrlNoSlash() {
        val baseUrl = "http://foobar.com"
        val expected = "http://foobar.com/"
        val actual = UrlUtils.cleanBaseUrl(baseUrl)

        assertEquals(expected, actual)
    }

    @Test
    fun cleanBaseUrlSlash() {
        val baseUrl = "http://foobar.com/"
        val expected = "http://foobar.com/"
        val actual = UrlUtils.cleanBaseUrl(baseUrl)

        assertEquals(expected, actual)
    }

    @Test
    fun extractCodeSingleQueryParam() {
        val codeUrl = "http://foobar.com?code=123"
        val expected = "123"
        val actual = UrlUtils.extractCode(codeUrl)

        assertEquals(expected, actual)
    }

    @Test
    fun extractCodeSingleMultipleParam() {
        val codeUrl = "http://foobar.com?code=123&moo=abc"
        val expected = "123"
        val actual = UrlUtils.extractCode(codeUrl)

        assertEquals(expected, actual)
    }

    @Test
    fun extractCodeNoCode() {
        val codeUrl = "http://foobar.com"
        val expected = ""
        val actual = UrlUtils.extractCode(codeUrl)

        assertEquals(expected, actual)
    }

}