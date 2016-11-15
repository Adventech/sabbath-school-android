# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in /Users/vitalik/Library/Android/sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# Add any project specific keep options here:

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:

-keepattributes Signature
-keepattributes *Annotation*

-keep class .R
-keep class **.R$* {
    <fields>;
}

-keep class com.cryart.sabbathschool.behavior.** { *; }
-keep abstract class com.cryart.sabbathschool.behavior.** { *; }
-keep public class com.cryart.sabbathschool.service.** { *; }


-keepclassmembers class com.cryart.sabbathschool.** {
  *;
}