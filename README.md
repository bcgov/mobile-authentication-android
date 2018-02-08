# Mobile Authentication Android
Wraps Red Hat Single Sign-On OAuth2 authorization code flow and token management with an easy to use authentication client.

## Requirements
MobileAuthentication supports Android API 23 and above so AES encryption can be used.

## Getting Started
project build.gradle:
```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```
	
app build.gradle:
```
dependencies {
	implementation "com.github.bcgov:mobile-authentication-android:<LATEST_VERSION>"
}
```

### Prerequisites
A Red Hat Single Sign-On server component that is setup to handle an OAuth2 authorization code flow.

### Android Manifest
You will need to add this to your AndroidManifest and specify what custom schema you're using in your redirectUri.
```xml
<activity android:name="ca.bc.gov.mobileauthentication.screens.redirect.RedirectActivity"
	android:launchMode="singleInstance">
	<intent-filter android:autoVerify="true">
		<action android:name="android.intent.action.VIEW" />
		<category android:name="android.intent.category.DEFAULT" />
		<category android:name="android.intent.category.BROWSABLE" />
		<data android:scheme="<YOUR_CUSTOM_SCHEME_USED_IN_REDIRECT_URI>" />
	</intent-filter>
</activity>
```

## Usage
There are four main commands for handling tokens that will be called using the MobileAuthenticationClient class.
1. Authenticate
2. Get Token
3. Refresh Token
4. Delete Token

### Mobile Authentication Client
Creating a MobileAuthenticationClient:
```kotlin
val authEndpoint = "<YOUR_BASE_URL>/auth/realms/<YOUR_REALM_NAME>/protocol/openid-connect/auth"
val baseUrl = "<YOUR_BASE_URL>"
val clientId = "<YOUR_CLIENT_ID>"
val realmName = "<YOUR_REALM_NAME>"
val redirectUri = "<YOUR_REDIRECT_URI>"

val client = MobileAuthenticationClient(context, baseUrl, realmName, authEndpoint, redirectUri, clientId)
```
The parameters needed for the client can all be found on your Red Hat Single Sign-On dashboard.
We recommened you use a custom application schema for your redirectUri such as <NAME_OF_YOUR_APP>://android
Please use the context of the activity in which you are going to call authenticate to create the client.

Please remember to call `client.clear()` in either `onPause()`, `onStop()` or `onDestroy()` in order to avoid memory leaks.

### Authenticate
Authenticate will get a fresh token from Red Hat Single Sign-On after the user has entered their login credentials through a Chrome custom tab and store it locally.
The result of the authenticate call can be retrieved in the `onActivityResult()` where the client was created.

Calling authenticate:
```kotlin
client?.authenticate(<OPTIONAL_REQUEST_CODE>)
```

OnActivityResult:
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	super.onActivityResult(requestCode, resultCode, data)
	client?.handleAuthResult(requestCode, resultCode, data, object: MobileAuthenticationClient.TokenCallback {
		override fun onError(throwable: Throwable) {
			Log.e(tag, throwable.message)
		}
		override fun onSuccess(token: Token) {
			Log.d(tag, "Authenticate Success")
		}
	})
}
```

### Get Token
Get token will get the token from local storage.

If the token is expired and the refresh token is NOT expired the token will automatically be refreshed, saved locally and returned.

Exceptions:
1. If there is no token locally a `TokenNotFoundException` will be thrown in the onError of the TokenCallback. This means the user has not yet been authenticated so no token exists locally.
2. If the token's refresh token is expired a `RefreshExpiredException` will be thrown in the onError of the TokenCallback. In this case the user will need to reauthenticate.
3. If the token does not have a refresh token then a `NoRefreshTokenException` will be thrown in the onError of the TokenCallback. This means the Token data being returned does not contain the required refreshToken for this lib to work.

Calling getToken:
```kotlin
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
				Log.e(tag, "No Token was found. Please authenticate first.")
			}
		}
	}

	override fun onSuccess(token: Token) {
		Log.d(tag, "Get Token Success")
	}
})
```

### Refresh Token
Refresh token will refresh the locally stored token.

Exceptions:
1. If the token's refresh token is expired a `RefreshExpiredException` will be thrown in the onError of the TokenCallback. In this case the user will need to reauthenticate.
2. If the token does not have a refresh token then a `NoRefreshTokenException` will be thrown in the onError of the TokenCallback. This means the Token data being returned does not contain the required refreshToken for this lib to work.

Calling refreshToken:
```kotlin
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
```

### Delete Token
Delete token will delete the locally stored token.

Calling deleteToken:
```kotlin
client?.deleteToken(object: MobileAuthenticationClient.DeleteCallback {
    override fun onError(throwable: Throwable) {
        Log.e(tag, throwable.message)
    }
    override fun onSuccess() {
        Log.d(tag, "Delete Token Success")
    }
})
```

### RxJava2
If you are using RxJava2 you can use `getTokenAsObservable()`, `refreshTokenAsObservable()` and `deleteTokenAsObservable()` in replace of the TokenCallback and DeleteCallback.
Note that TokenNotFoundException in getToken will be replaced with NoSuchElementException when using getTokenAsObservable.

## License
```
Copyright 2017 Province of British Columbia

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at 

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License
```