# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.


-keep class **ScreenRoute { *; }
-keep class **ScreenRoute$Companion { *; }
-keepclassmembers class **ScreenRoute {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,allowobfuscation,allowshrinking class **ScreenRoute$$serializer { *; }

# Room resolves the generated *_Impl database class by name at runtime.
-keep class * extends androidx.room3.RoomDatabase { <init>(); }

# WorkManager, pulled in transitively by AdMob, ships its own WorkDatabase on the
# Room 2.x namespace, which the rule above does not match.
-keep class * extends androidx.room.RoomDatabase { <init>(); }
