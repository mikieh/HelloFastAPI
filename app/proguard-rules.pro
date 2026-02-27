# Keep Gson serialization models
-keepattributes Signature
-keepattributes *Annotation*

# Gson
-keep class com.callarchive.app.data.remote.gemini.** { *; }

# Retrofit
-keepattributes RuntimeVisibleAnnotations, RuntimeVisibleParameterAnnotations
-keepclassmembers,allowshrinking,allowobfuscation interface * {
    @retrofit2.http.* <methods>;
}

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *

# Google API Client
-keep class com.google.api.services.drive.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.common.**

# OkHttp
-dontwarn okhttp3.internal.platform.**
