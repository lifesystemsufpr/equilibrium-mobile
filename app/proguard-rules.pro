# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# ========== CRITICAL: Preserve Generic Type Information ==========
# R8 full mode strips generic signatures - this causes ClassCastException!
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations
-keepattributes AnnotationDefault

# Keep source file names and line numbers for better crash reports
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ========== Retrofit/OkHttp ==========
-dontwarn retrofit2.**
-dontwarn okhttp3.**
-dontwarn okio.**

-keep class retrofit2.** { *; }
-keep class okhttp3.** { *; }
-keep class okio.** { *; }

-keepattributes Exceptions

# Keep Retrofit service interfaces
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

# ========== Gson ==========
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**

-keep class com.google.gson.** { *; }
-keep class * implements com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# Preserve all fields annotated with @SerializedName
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

# Keep generic signature of Call, Response (R8 full mode strips signatures)
-keepattributes Signature,InnerClasses,EnclosingMethod
-keepattributes RuntimeVisibleAnnotations,RuntimeVisibleParameterAnnotations

# ========== ALL Data Transfer Objects ==========
# CRITICAL: Keep ALL DTOs used by Retrofit/Gson

# Data module DTOs
-keep class com.ufpr.equilibrium.data.remote.dto.** { *; }
-keep class com.ufpr.equilibrium.data.remote.PatientsEnvelope { *; }
-keep class com.ufpr.equilibrium.data.remote.PaginationMeta { *; }

# App module DTOs (network package)
-keep class com.ufpr.equilibrium.network.** { *; }

# Feature-specific models used by API
-keep class com.ufpr.equilibrium.feature_professional.PacienteModel { *; }
-keep class com.ufpr.equilibrium.feature_professional.User { *; }
-keep class com.ufpr.equilibrium.feature_professional.PacientesEnvelope { *; }
-keep class com.ufpr.equilibrium.feature_professional.PacienteModelList { *; }
-keep class com.ufpr.equilibrium.feature_professional.Meta { *; }
-keep class com.ufpr.equilibrium.feature_professional.ProfessionalModel { *; }

-keep class com.ufpr.equilibrium.feature_healthUnit.HealthUnit { *; }

-keep class com.ufpr.equilibrium.feature_questionnaire.api.** { *; }
-keep class com.ufpr.equilibrium.feature_questionnaire.payloads.** { *; }

# Keep domain models
-keep class com.ufpr.equilibrium.domain.model.** { *; }

# ========== Kotlin Serialization (if used) ==========
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}
-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

# ========== Kotlin Coroutines ==========
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembers class kotlinx.coroutines.** {
    volatile <fields>;
}

# ========== Hilt/Dagger ==========
-dontwarn com.google.errorprone.annotations.**

# ========== Debugging (ENABLE WHEN DEBUGGING PROGUARD) ==========
# Uncomment to debug ProGuard issues:
# -printconfiguration build/outputs/mapping/configuration.txt
# -printusage build/outputs/mapping/usage.txt
# -printmapping build/outputs/mapping/mapping.txt
