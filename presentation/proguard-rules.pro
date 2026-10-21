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
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile

# Retrofit 2.9.0 predates the R8 full-mode rules for suspend return types.
# CustomCallAdapterFactory reads NetworkState<T> through reflection.
-keepattributes Signature,InnerClasses,EnclosingMethod,AnnotationDefault
-keep,allowoptimization,allowshrinking,allowobfuscation class kotlin.coroutines.Continuation
-keep,allowoptimization,allowshrinking,allowobfuscation interface retrofit2.Call
-keep,allowoptimization,allowshrinking,allowobfuscation class retrofit2.Response
-keep,allowoptimization,allowobfuscation class com.aos.util.NetworkState

# Base screens resolve VM from javaClass.genericSuperclass. Preserve the
# hierarchy (including generated Hilt parents) and its type arguments.
# Members can still be shrunk/optimized, and class names can be obfuscated.
-keep,allowobfuscation class com.aos.floney.base.BaseActivity
-keep,allowobfuscation class * extends com.aos.floney.base.BaseActivity
-keep,allowobfuscation class com.aos.floney.base.BaseFragment
-keep,allowobfuscation class * extends com.aos.floney.base.BaseFragment
-keep,allowobfuscation class com.aos.floney.base.BaseDialogFragment
-keep,allowobfuscation class * extends com.aos.floney.base.BaseDialogFragment
-keep,allowobfuscation class com.aos.floney.base.BaseBottomSheetFragment
-keep,allowobfuscation class * extends com.aos.floney.base.BaseBottomSheetFragment
-keep,allowobfuscation class com.aos.floney.base.BaseViewModel
-keep,allowobfuscation class * extends com.aos.floney.base.BaseViewModel
