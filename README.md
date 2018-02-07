# Mobile Authentication Android

Wraps Red Hat Single Sign-On OAuth2 authorization code flow and token management with an easy to use authentication client.

## Getting Started
The first step is to include MobileAuthenticationAndroid into your project, for example, as a Gradle implementation dependency:

project build.gradle:
```allprojects {
		repositories {
			...
			maven { url 'https://jitpack.io' }
		}
	}```
	
app build.gradle:
```dependencies {
	        implementation "com.github.bcgov:mobile-authentication-android:<LATEST_VERSION>"
	}```

### Prerequisites
A Red Hat Single Sign-On server component that is setup to handle an OAuth2 authorization code flow.
