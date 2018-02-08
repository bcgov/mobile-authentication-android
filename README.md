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

## Usage
There are four main commands for handling tokens that will be called using the MobileAuthenticationClient class.
1. Authenticate
2. Get Token
3. Refresh Token
4. Delete Token

### Mobile Authentication Client
Creating a MobileAuthenticationClient:
The parameters needed for the client can all be found on your Red Hat Single Sign-On dashboard.
We recommened you use a custom application schema for your redirectUri such as <NAME_OF_YOUR_APP>://android
```kotlin
val authEndpoint = "<YOUR_BASE_URL>/auth/realms/<YOUR_REALM_NAME>/protocol/openid-connect/auth"
val baseUrl = "<YOUR_BASE_URL>"
val clientId = "<YOUR_CLIENT_ID>"
val realmName = "<YOUR_REALM_NAME>"
val redirectUri = "<YOUR_REDIRECT_URI>"

val client = MobileAuthenticationClient(context, baseUrl, realmName, authEndpoint, redirectUri, clientId)
```

Please remember to call `client.clear()` in either `onPause()`, `onStop()` or `onDestroy()` in order to avoid memory leaks.

### Android Manifest
You will need to add this to your AndroidManifest and specify what custom schema you're using in your redirectUri.
```xml
<activity
	android:name="ca.bc.gov.mobileauthentication.screens.redirect.RedirectActivity"
	android:launchMode="singleInstance">
	<intent-filter android:autoVerify="true">
		<action android:name="android.intent.action.VIEW" />
		<category android:name="android.intent.category.DEFAULT" />
		<category android:name="android.intent.category.BROWSABLE" />
		<data android:scheme="<YOUR_CUSTOM_SCHEME_USED_IN_REDIRECT_URI>" />
	</intent-filter>
</activity>
```



