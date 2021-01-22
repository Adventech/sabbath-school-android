
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

-dontwarn android.support.**
-dontwarn androidx.**

-keepattributes *Annotation*
-keepattributes JavascriptInterface

-dontwarn org.conscrypt.**

-dontwarn com.github.pwittchen.reactivenetwork.library.ReactiveNetwork

# Dagger
-dontwarn com.google.errorprone.annotations.*
# https://github.com/google/dagger/issues/2291
-keep class dagger.hilt.android.internal.modules.HiltWrapper_ActivityModule
-keep class dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_LifecycleModule
-keep class dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedLifecycleEntryPoint
-keep class dagger.hilt.android.internal.managers.HiltWrapper_ActivityRetainedComponentManager_ActivityRetainedComponentBuilderEntryPoint

-keep class dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityEntryPoint
-keep class dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_ActivityModule
-keep class dagger.hilt.android.internal.lifecycle.HiltWrapper_DefaultViewModelFactories_FragmentEntryPoint

-keep class dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule
-keep class dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelFactoriesEntryPoint

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

#Required for firebase db issue
-keep class org.json.* { *; }

-keepclassmembers class com.cryart.sabbathschool.** {
  *;
}