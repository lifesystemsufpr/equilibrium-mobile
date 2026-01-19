# Consumer ProGuard rules for data module
# These rules are automatically applied to modules that depend on this module

# Keep all DTOs - used by Retrofit with Gson
-keep class com.ufpr.equilibrium.data.remote.dto.** { *; }

# Keep Gson annotations
-keepattributes *Annotation*
-keepattributes Signature
