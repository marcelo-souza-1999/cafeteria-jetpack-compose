# ==============================================================================
# R8 / ProGuard Hardening Rules - Módulo :app (Cafeteria Targaryen)
# ==============================================================================

# 1. Stripping de logs verbosos (Log.v, Log.d, Log.i) em builds Release.
#    Preserva Log.w / Log.e e AppLogger.e para diagnóstico seguro de falhas em produção.
-assumenosideeffects class android.util.Log {
    public static boolean isLoggable(java.lang.String, int);
    public static int v(...);
    public static int d(...);
    public static int i(...);
}

# 2. Retenção de atributos necessários para desofuscação de stacktraces (mapping.txt)
#    e processamento de anotações em runtime, mascarando o nome original dos arquivos.
-keepattributes SourceFile,LineNumberTable,*Annotation*,Signature,InnerClasses,EnclosingMethod
-renamesourcefileattribute SourceFile

# 3. Proteção cirúrgica para DTOs Ktor / kotlinx.serialization
#    Mantém apenas classes explicitamente anotadas com @Serializable e seus serializers gerados.
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault
-keep,includedescriptorclasses @kotlinx.serialization.Serializable class * {
    *;
}
-keepclassmembers class * {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclassmembers class **$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class * {
    @kotlinx.serialization.Serializable <fields>;
}

# 4. Proteção para entidades, DAOs e banco Room 3.0 (construtores gerados e queries)
-keep class * extends androidx.room3.RoomDatabase
-keep @androidx.room3.Entity class *
-keepclassmembers @androidx.room3.Entity class * {
    <fields>;
    <init>(...);
}
-keep @androidx.room3.Dao interface *

# 5. Proteção para AppFunctions Android 16/17 (Schema, Serviço KSP e DTOs serializáveis)
-keep @androidx.appfunctions.AppFunctionSerializable class * {
    *;
}
-keep class * extends androidx.appfunctions.AppFunctionService {
    *;
}
