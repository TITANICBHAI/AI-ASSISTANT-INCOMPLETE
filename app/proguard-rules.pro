# Add project specific ProGuard rules here.

# Keep line numbers for debugging stack traces
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# Keep exception stack traces
-keepattributes *Annotation*
-keepattributes Signature
-keepattributes Exceptions

#=============== TensorFlow Lite =================
# Keep TensorFlow Lite classes
-keep class org.tensorflow.lite.** { *; }
-keep interface org.tensorflow.lite.** { *; }
-dontwarn org.tensorflow.lite.**

# Keep model files and assets
-keep class **.tflite { *; }
-keepclassmembers class * {
    @org.tensorflow.lite.** <fields>;
}

#=============== Room Database ===================
# Keep Room classes
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-dontwarn androidx.room.paging.**

# Keep DAO classes
-keep interface * extends androidx.room.Dao { *; }
-keep class * implements androidx.room.Dao { *; }

# Keep database entities
-keep @androidx.room.Entity class com.aiassistant.data.models.** { *; }
-keep class com.aiassistant.data.** { *; }

#=============== Gson =================
# Gson uses generic type information stored in a class file when working with fields
-keepattributes Signature

# Keep Gson classes
-keep class com.google.gson.** { *; }
-keep class sun.misc.Unsafe { *; }

# Keep model classes used with Gson
-keep class com.aiassistant.**.models.** { *; }
-keep class com.aiassistant.data.models.** { *; }

# Prevent obfuscation of types which use Gson annotations
-keepclassmembers,allowobfuscation class * {
  @com.google.gson.annotations.SerializedName <fields>;
}

#=============== ML Kit =================
-keep class com.google.mlkit.** { *; }
-dontwarn com.google.mlkit.**

#=============== OpenCV =================
-keep class org.opencv.** { *; }
-dontwarn org.opencv.**

#=============== Android SDK =================
# Keep native methods
-keepclasseswithmembernames class * {
    native <methods>;
}

# Keep Parcelable implementations
-keep class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# Keep Serializable classes
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#=============== App Specific =================
# Keep all AI models and managers
-keep class com.aiassistant.core.ai.** { *; }
-keep class com.aiassistant.core.ai.neural.** { *; }

# Keep services
-keep class com.aiassistant.services.** { *; }

# Keep accessibility services
-keep class * extends android.accessibilityservice.AccessibilityService { *; }

# Keep broadcast receivers
-keep class * extends android.content.BroadcastReceiver { *; }

# Keep application class
-keep class com.aiassistant.core.ai.AIAssistantApplication { *; }

# Keep activities and fragments
-keep class * extends android.app.Activity { *; }
-keep class * extends androidx.fragment.app.Fragment { *; }

# Keep ViewModels
-keep class * extends androidx.lifecycle.ViewModel { *; }
-keep class * extends androidx.lifecycle.AndroidViewModel { *; }

#=============== Reflection =================
# Keep classes that use reflection
-keepclassmembers class * {
    public <init>(...);
}

# Keep enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

#=============== Warnings =================
# Suppress warnings for missing classes
-dontwarn java.lang.invoke.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn org.bouncycastle.**
-dontwarn org.openjsse.**
