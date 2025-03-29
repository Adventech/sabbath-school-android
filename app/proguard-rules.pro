
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

# Joda Time
-dontwarn org.joda.convert.**
-dontwarn org.joda.time.**
-keep class org.joda.time.** { *; }
-keep interface org.joda.time.** { *; }

-keep @androidx.annotation.Keep class * { *; }

# Keep generic signature of Call, Response (R8 full mode strips signatures from non-kept items).
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response

# Keep all Retrofit interfaces that are used to create service instances.
# https://github.com/square/retrofit/issues/4134
-if interface *
-keepclasseswithmembers,allowobfuscation interface <1> {
  @retrofit2.http.* <methods>;
}

# With R8 full mode generic signatures are stripped for classes that are not
# kept. Suspend functions are wrapped in continuations where the type argument
# is used.
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# Parcelable.
-keepattributes EnclosingClass,InnerClasses
-keep,allowshrinking,allowobfuscation class * implements android.os.Parcelable {}
-keep,allowshrinking,allowobfuscation class * implements android.os.Parcelable$Creator {}

# Compose lifecycle
-if public class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static *** getLocalLifecycleOwner();
}
-keep public class androidx.compose.ui.platform.AndroidCompositionLocals_androidKt {
    public static *** getLocalLifecycleOwner();
}

# Keep all model block-kit API models
-keep class io.adventech.blockkit.model.** { *; }