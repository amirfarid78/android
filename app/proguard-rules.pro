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
-keep class io.agora.rtc2.**{*;}
# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
-keep class com.google.android.material.** { *; }
-keep class androidx.appcompat.** { *; }

#-keep class com.stripe.android.PaymentConfiguration
#-keep class com.stripe.android.Stripe
#-keep class com.stripe.android.paymentsheet.** { *; }
#-keepclassmembers class com.stripe.android.paymentsheet.** { *; }
#
