# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

-keep class kotlin.** { *; }
-keep class kotlin.Metadata { *; }
-dontwarn kotlin.**
-keepclassmembers class **$WhenMappings {
    <fields>;
}
-keepclassmembers class kotlin.Metadata {
    public <methods>;
}
-assumenosideeffects class kotlin.jvm.internal.Intrinsics {
    static void checkParameterIsNotNull(java.lang.Object, java.lang.String);
}

-keep class com.delhivery.axle.data.** { *; }

-keep class com.squareup.okhttp.** { *; }
-keep interface com.squareup.okhttp.** { *; }

-dontwarn com.squareup.okhttp.**
-dontwarn okio.**
-dontwarn javax.annotation.Nullable
-dontwarn javax.annotation.ParametersAreNonnullByDefault

-keepattributes Signature
-keepattributes *Annotation*
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

-dontwarn okhttp3.**

-keepattributes Signature
# For using GSON @Expose annotation
-keepattributes *Annotation*
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.delhivery.axle.api.response.** { *; }
#-keep class com.delhivery.axle.api.request.** { *; }

-keep class com.google.gson.stream.** { *; }

# Platform calls Class.forName on types which do not exist on Android to determine platform.
-dontnote retrofit2.Platform
# Platform used when running on Java 8 VMs. Will not be used at runtime.
-dontwarn retrofit2.Platform$Java8
# Retain generic type information for use by reflection by converters and adapters.
-keepattributes Signature
# Retain declared checked exceptions for use by a Proxy instance.
-keepattributes Exceptions

#For glide
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}

# Class names are needed in reflection
-keep class com.amazonaws.** { *; }
-keep class com.amazon.** { *; }
-keep class com.amazonaws.services.**.*Handler
-keep class com.amazonaws.services.sqs.QueueUrlHandler  { *; }
-keep class com.amazonaws.javax.xml.transform.sax.*     { public *; }
-keep class com.amazonaws.javax.xml.stream.**           { *; }
-keep class com.amazonaws.services.**.model.*Exception* { *; }

-keep class org.apache.commons.logging.**               { *; }
-keep class org.codehaus.**                             { *; }
# The following are referenced but aren't required to run
-dontwarn com.fasterxml.jackson.**
-dontwarn org.codehaus.jackson.**
-dontwarn org.apache.commons.logging.**
-dontwarn org.apache.commons.logging.impl.**
# Android 6.0 release removes support for the Apache HTTP client
-dontwarn org.apache.http.**
-dontwarn org.apache.http.conn.scheme.**
# The SDK has several references of Apache HTTP client
-dontwarn com.amazonaws.http.**
-dontwarn com.amazonaws.metrics.**

# Suppress warnings for optional AWS Mobile Auth dependencies that are not used
-dontwarn com.amazonaws.mobile.auth.facebook.**
-dontwarn com.amazonaws.mobile.auth.google.**
-dontwarn com.amazonaws.mobile.auth.ui.**
-dontwarn com.amazonaws.mobileconnectors.cognitoauth.**

-keepattributes Signature,*Annotation*

-dontwarn javax.xml.stream.events.**

-keepnames class com.delhivery.axle.ui.home.fragments.** { *; }

-keepclassmembers enum * { *; }
-keep class com.google.code.gson.* { *; }
-keepattributes *Annotation*, Signature, Exception
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep Retrofit service interfaces
-keep interface com.delhivery.axle.api.service.** { *; }

# Keep Retrofit service method signatures
-keepclassmembers,allowobfuscation interface com.delhivery.axle.api.service.** {
    <methods>;
}

# Keep Retrofit annotations
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepattributes Signature
-keepattributes Exceptions

# Keep Retrofit service implementations (generated at runtime)
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}


#Retrofit signature type info added
-keepclassmembers class * {
    @retrofit2.http.* <methods>;
}
-keepattributes Signature
-keepattributes Exceptions


#RxJava rules
# RxJava2 rules - Add these to preserve generic type information
-dontwarn io.reactivex.**
-keep class io.reactivex.** { *; }
-keep interface io.reactivex.** { *; }

# Preserve generic signatures of Single, Observable, etc. for Retrofit
-keepattributes Signature
-keepattributes Exceptions

# Keep RxJava2 classes used by Retrofit adapter
-keepclassmembers class retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory { *; }
-keepclassmembers class retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory$* { *; }



#New rules for kotlin lamdas and rx
# Kotlin Lambda classes for RxJava
# Keep all classes that implement functional interfaces used by RxJava
-keep class * implements io.reactivex.functions.Function { *; }
-keep class * implements io.reactivex.functions.BiFunction { *; }
-keep class * implements io.reactivex.functions.Function3 { *; }
-keep class * implements io.reactivex.functions.Function4 { *; }
-keep class * implements io.reactivex.functions.Function5 { *; }
-keep class * implements io.reactivex.functions.Consumer { *; }
-keep class * implements io.reactivex.functions.BiConsumer { *; }
-keep class * implements io.reactivex.functions.Predicate { *; }

# Keep Kotlin Triple class and its properties
-keep class kotlin.Triple { *; }
-keepclassmembers class kotlin.Triple {
    public <fields>;
    public <methods>;
}

# Keep Kotlin Pair class (also used in your code)
-keep class kotlin.Pair { *; }
-keepclassmembers class kotlin.Pair {
    public <fields>;
    public <methods>;
}

# Keep synthetic classes created for lambdas (Kotlin generates these)
-keepclassmembers class * {
    private synthetic <methods>;
}

# Additional rule: Keep lambda classes in ViewModels
-keepclassmembers class com.delhivery.axle.ui.**.*ViewModel {
    <methods>;
}

# Keep all classes in the auth package that might contain lambdas
-keep class com.delhivery.axle.ui.auth.** { *; }