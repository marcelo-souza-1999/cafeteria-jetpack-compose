# Regras padrão do ProGuard/R8 para o módulo App
# Especifique aqui as regras de ofuscação para as bibliotecas de terceiros se necessário

# Regras do Coroutines e Ktor geralmente são providas nativamente pelo R8 em AGP recentes.
# Mantenha os DTOs do Ktor e Serializable se houver parse via reflexion (não exigido pro kotlinx-serialization em teoria)

-keepattributes *Annotation*, Signature, InnerClasses, EnclosingMethod
