# ── Firebase Auth ──
-keep class com.google.firebase.auth.** { *; }
-keepnames class com.google.firebase.auth.** { *; }

# ── Google Play Services (untuk SSL Provider & Auth) ──
-keep class com.google.android.gms.** { *; }
-keepnames class com.google.android.gms.** { *; }
-dontwarn com.google.android.gms.**

# ── OkHttp & gRPC (digunakan Firebase di background) ──
-dontwarn okhttp3.**
-dontwarn io.grpc.**
-keep class io.grpc.** { *; }

# ── Jaga semua Firebase secara umum ──
-keep class com.google.firebase.** { *; }
-keepnames class com.google.firebase.** { *; }

-keep class com.example.smartsholat.** { *; }
-keepclassmembers class com.example.smartsholat.** { *; }
-keepattributes Signature
-keepattributes *Annotation*