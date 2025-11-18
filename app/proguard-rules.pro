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
-keep class com.delhivery.axle.api.request.** { *; }







-keep class com.delhivery.axle.api.response.** { *; }

# CRITICAL: Keep response classes WITHOUT obfuscation for Gson to work with Kotlin data classes
# The allowobfuscation flag breaks Kotlin data class constructor parameter matching
-keepclassmembers class com.delhivery.axle.api.response.** {
  <fields>;
  <init>(...);
}

# Ensure @SerializedName annotations are preserved and fields are NOT obfuscated
-keepclassmembers class com.delhivery.axle.api.response.** {
  @com.google.gson.annotations.SerializedName <fields>;
}






# Keep LoginResponse and ensure Gson can deserialize it
-keep class com.delhivery.axle.api.response.LoginResponse { *; }
-keepclassmembers class com.delhivery.axle.api.response.LoginResponse {
    <init>(...);
    <fields>;
}

# Ensure BaseResponse generic type information is preserved
-keep class com.delhivery.axle.api.response.BaseResponse { *; }
-keepclassmembers class com.delhivery.axle.api.response.BaseResponse {
    <init>(...);
    <fields>;
}

# Additional rule: Keep all constructors for response classes (critical for Gson)
-keepclassmembers class com.delhivery.axle.api.response.** {
    <init>(...);
}

# Keep all fields in response classes (Gson needs these for reflection)
-keepclassmembers class com.delhivery.axle.api.response.** {
    <fields>;
}

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
#-keepclassmembers,allowobfuscation class * {
#  @com.google.gson.annotations.SerializedName <fields>;
#}

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





#New proguard rules - v2
# Keep all data classes used by Gson (already have this, but ensure it's comprehensive)
-keep class com.delhivery.axle.data.** { *; }

# Keep UserModel and related classes with all properties
-keep class com.delhivery.axle.data.UserModel { *; }
-keep class com.delhivery.axle.data.SupplierModel { *; }
-keep class com.delhivery.axle.data.ClientModel { *; }

# Keep all fields in data classes (critical for Gson and property access)
-keepclassmembers class com.delhivery.axle.data.** {
    <fields>;
}

# Keep Kotlin data class component methods (for destructuring)
-keepclassmembers class com.delhivery.axle.data.** {
    public <methods>;
}

# Keep all lambda classes that implement functional interfaces
-keep class * implements io.reactivex.functions.BiConsumer { *; }
-keep class * implements io.reactivex.functions.Consumer { *; }

# Keep synthetic methods (Kotlin generates these for lambdas)
-keepclassmembers class * {
    private synthetic <methods>;
}

# Keep all classes in ViewModels (they contain the lambda callbacks)
-keep class com.delhivery.axle.ui.**.*ViewModel { *; }
-keepclassmembers class com.delhivery.axle.ui.**.*ViewModel {
    <methods>;
}

# Additional: Keep all classes that might be used via reflection in RxJava chains
-keepclassmembers class * {
    @kotlin.jvm.JvmStatic <methods>;
}

#======
# Keep Kotlin lambda classes (they're named like ClassName$functionName$lambda$number)
-keep class com.delhivery.axle.ui.auth.AuthenticationViewModel$* { *; }
-keep class com.delhivery.axle.ui.**.*ViewModel$* { *; }

# Keep all inner classes in ViewModels (lambdas become inner classes)
-keep class com.delhivery.axle.ui.**.*ViewModel$*$* { *; }


#=====
# Gson: Keep all data classes with @SerializedName annotations
-keepattributes Signature
-keepattributes *Annotation*
-keepattributes EnclosingMethod

# Keep classes annotated with @SerializedName
#-keepclassmembers,allowobfuscation class * {
#    @com.google.gson.annotations.SerializedName <fields>;
#}

# Keep all data classes used by Gson
-keep class com.delhivery.axle.data.** { *; }

#jwt
# Keep auth0 JWT classes
-keep class com.auth0.android.jwt.** { *; }
-keep class com.auth0.android.jwt.JWT { *; }





# Keep generic type signatures so Gson TypeToken can read them
-keepattributes Signature

# Keep annotations metadata too (optional but useful)
-keepattributes *Annotation*

# Keep Auth0 JWT library classes (prevent aggressive shrinking/obfuscation of the library)
-keep class com.auth0.android.jwt.** { *; }

# Keep Gson TypeToken class (usually not needed but safe)
-keep class com.google.gson.reflect.TypeToken { *; }