fastlane documentation
================
# Installation

Make sure you have the latest version of the Xcode command line tools installed:

```
xcode-select --install
```

Install _fastlane_ using
```
[sudo] gem install fastlane -NV
```
or alternatively using `brew cask install fastlane`

# Available Actions
## Android
### android playstore
```
fastlane android playstore
```

### android playmeta
```
fastlane android playmeta
```
Upload meta to the Google Play
### android beta
```
fastlane android beta
```
Submit a new Beta Build to Crashlytics Beta
### android alpha
```
fastlane android alpha
```
Submit a new Alpha Build to Google PlayStore

----

This README.md is auto-generated and will be re-generated every time [fastlane](https://fastlane.tools) is run.
More information about fastlane can be found on [fastlane.tools](https://fastlane.tools).
The documentation of fastlane can be found on [docs.fastlane.tools](https://docs.fastlane.tools).
