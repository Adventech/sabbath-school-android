
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn android.support.**
-dontwarn androidx.**

-keepattributes *Annotation*
-keepattributes JavascriptInterface

-dontwarn org.conscrypt.**

# Dagger
-dontwarn com.google.errorprone.annotations.*

-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.app.backup.BackupAgent
-keep public class * extends android.preference.Preference
-keep public class * extends androidx.fragment.app.Fragment
-keep public class * extends androidx.navigation.fragment.NavHostFragment

# For native methods, see http://proguard.sourceforge.net/manual/examples.html#native
-keepclasseswithmembernames class * {
    native <methods>;
}

-keep public class androidx.fragment.app.FragmentContainerView {
    public <init>(android.content.Context, android.util.AttributeSet);
}

# For enumeration classes
-keepclassmembers enum * {
    <fields>;
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

-keep class * implements android.os.Parcelable {
  public static final android.os.Parcelable$Creator *;
}

-keepclassmembers class **.R$* {
    public static <fields>;
}
-keep class **.R$* {
    <fields>;
}

-keep class net.danlew.android.joda.R$raw { *; }

-keepclassmembers class com.cryart.sabbathschool.** {
  *;
}

-keep @android.support.annotation.Keep class * {*;}

# Temporary for Glance
-keepclassmembers class * extends androidx.glance.appwidget.protobuf.GeneratedMessageLite {
  <fields>;
}