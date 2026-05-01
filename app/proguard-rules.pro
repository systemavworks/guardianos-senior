# GuardianOS Senior - Reglas ProGuard
-keep class es.guardianos.senior.** { *; }
-keepclassmembers class * { @androidx.compose.runtime.Composable <methods>; }
