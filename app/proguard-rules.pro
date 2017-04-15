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
-keep class com.cryart.sabbathschool.misc.** { *; }


-keepclassmembers class com.cryart.sabbathschool.** {
  *;
}

-dontwarn com.github.pwittchen.reactivenetwork.library.ReactiveNetwork
-dontwarn io.reactivex.functions.Function
-dontwarn rx.internal.util.**
-dontwarn sun.misc.Unsafe

-keep class rx.schedulers.Schedulers {
    public static <methods>;
}
-keep class rx.schedulers.ImmediateScheduler {
    public <methods>;
}
-keep class rx.schedulers.TestScheduler {
    public <methods>;
}
-keep class rx.schedulers.Schedulers {
    public static ** test();
}
-keepclassmembers class rx.internal.util.unsafe.*ArrayQueue*Field* {
    long producerIndex;
    long consumerIndex;
}
-keepclassmembers class rx.internal.util.unsafe.BaseLinkedQueueProducerNodeRef {
    long producerNode;
    long consumerNode;
}