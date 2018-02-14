package ca.bc.gov.mobileauthentication.data.models

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
class TokenTest {

    @Test
    fun tokenExpiryAfterCurrentTime() {
        val currentTime = 2000L
        val expiresAt = 3000L
        val token = Token(null, null, null, null,
                null, null, null, null, expiresAt, 4000L)

        val expected = false
        val actual = token.isExpired(currentTime)

        assertEquals(expected, actual)
    }

    @Test
    fun tokenExpiryBeforeCurrentTime() {
        val currentTime = 2000L
        val expiresAt = 1000L
        val token = Token(null, null, null, null,
                null, null, null, null, expiresAt, 4000L)

        val expected = true
        val actual = token.isExpired(currentTime)

        assertEquals(expected, actual)
    }

    @Test
    fun tokenExpiryEqualToCurrentTime() {
        val currentTime = 2000L
        val expiresAt = 2000L
        val token = Token(null, null, null, null,
                null, null, null, null, expiresAt, 4000L)

        val expected = false
        val actual = token.isExpired(currentTime)

        assertEquals(expected, actual)
    }

    @Test
    fun refreshExpiryAfterCurrentTime() {
        val currentTime = 2000L
        val refreshExpiresAt = 3000L
        val token = Token(null, null, null, null,
                null, null, null, null, 0, refreshExpiresAt)

        val expected = false
        val actual = token.isRefreshExpired(currentTime)

        assertEquals(expected, actual)
    }

    @Test
    fun refreshExpiryBeforeCurrentTime() {
        val currentTime = 2000L
        val refreshExpiresAt = 1000L
        val token = Token(null, null, null, null,
                null, null, null, null, 0, refreshExpiresAt)

        val expected = true
        val actual = token.isRefreshExpired(currentTime)

        assertEquals(expected, actual)
    }

    @Test
    fun refreshExpiryEqualToCurrentTime() {
        val currentTime = 2000L
        val refreshExpiresAt = 2000L
        val token = Token(null, null, null, null,
                null, null, null, null, 0, refreshExpiresAt)

        val expected = false
        val actual = token.isRefreshExpired(currentTime)

        assertEquals(expected, actual)
    }

}