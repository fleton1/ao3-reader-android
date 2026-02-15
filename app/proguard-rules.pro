# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.kts.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ==================== KEEP DATA CLASSES ====================
# Keep all data classes and models - they're used for database and network
-keep class com.ao3reader.data.** { *; }
-keep class com.ao3reader.domain.models.** { *; }

# Keep Workers for WorkManager
-keep class com.ao3reader.workers.** { *; }

# ==================== ROOM DATABASE ====================
# Keep Room database classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao class *
-dontwarn androidx.room.paging.**

# Keep Room annotation processors
-keepclassmembers class * {
    @androidx.room.* <methods>;
}

# ==================== HILT DEPENDENCY INJECTION ====================
# Keep Hilt generated classes
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep class * extends dagger.hilt.android.lifecycle.HiltViewModel
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }

# Keep Hilt modules
-keep @dagger.Module class *
-keep @dagger.hilt.InstallIn class *

# Keep injected constructors
-keepclassmembers class * {
    @javax.inject.Inject <init>(...);
}

# Keep Hilt worker factory
-keep class androidx.hilt.work.HiltWorkerFactory
-keep class * extends androidx.hilt.work.HiltWorker

# ==================== OKHTTP & NETWORKING ====================
# OkHttp platform used only on JVM and when Conscrypt and other security providers are available
-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**

# Keep OkHttp classes
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-keep class okio.** { *; }

# A resource is loaded with a relative path so the package of this class must be preserved
-keepnames class okhttp3.internal.publicsuffix.PublicSuffixDatabase

# ==================== JSOUP HTML PARSING ====================
-dontwarn org.jsoup.**
-keep class org.jsoup.** { *; }
-keeppackagenames org.jsoup.nodes

# ==================== KOTLIN COROUTINES ====================
# Keep coroutines classes
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# ServiceLoader support
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}

# Most of volatile fields are updated with AFU and should not be mangled
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ==================== JETPACK COMPOSE ====================
# Keep Compose runtime classes
-keep class androidx.compose.** { *; }
-keep class kotlin.Metadata { *; }

# Keep Compose UI classes
-dontwarn androidx.compose.ui.**

# ==================== WORKMANAGER ====================
# Keep WorkManager classes
-keep class * extends androidx.work.Worker
-keep class * extends androidx.work.CoroutineWorker {
    public <init>(...);
}
-keep class androidx.work.** { *; }

# Keep work parameters
-keep class * extends androidx.work.WorkerParameters

# ==================== KOTLIN SERIALIZATION ====================
# Keep @Serializable classes (if used in future)
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ==================== GENERAL ANDROID ====================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep custom view constructors
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Keep enums
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

# Keep Parcelables
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# ==================== ATTRIBUTES ====================
# Keep important attributes for debugging
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
-keepattributes Signature
-keepattributes Exceptions
-keepattributes InnerClasses
-keepattributes EnclosingMethod

# ==================== OPTIMIZATION ====================
# Optimization is turned on by default
# You can disable it with -dontoptimize

# Allow aggressive optimization
-optimizationpasses 5
-allowaccessmodification

# Don't warn about missing classes
-dontwarn javax.lang.model.element.Modifier
-dontwarn org.slf4j.**

# ==================== DEBUGGING ====================
# Remove logging in release builds
-assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
}

# Keep crash reporting
-keepattributes SourceFile,LineNumberTable
-keep class com.ao3reader.** { *; }

