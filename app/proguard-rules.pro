# Add project specific ProGuard rules here.
# By default, the flags in this file are appended to flags specified
# in C:\Users\Shawon\AppData\Local\Android\Sdk/tools/proguard/proguard-android.txt
# You can edit the include path and order by changing the proguardFiles
# directive in build.gradle.

# Room
-keepclassmembers class * extends androidx.room.RoomDatabase {
    <init>(...);
}

# Google Drive API & HTTP
-keep class com.google.api.services.drive.** { *; }
-keep class com.google.api.client.** { *; }
-keep class com.google.api.services.drive.model.** { *; }
-dontwarn com.google.api.client.**
-dontwarn com.google.j2objc.annotations.**
-dontwarn javax.annotation.**
-dontwarn org.apache.http.**
-dontwarn org.checkerframework.**

# Kotlin Serialization
-keepattributes *Annotation*, EnclosingMethod, InnerClasses
-keepclassmembers class ** {
    @kotlinx.serialization.SerialName <fields>;
}

# Compose & Material
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.ui.**

# General optimizations
-repackageclasses ''
-allowaccessmodification
-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*
-mergeinterfacesaggressively
-overloadaggressively
-obfuscationdictionary none
