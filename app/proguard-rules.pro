# Reglas de R8 para la versión de publicación.

# Gson crea estos objetos por reflexión (questions.json, estadísticas e historial
# guardados en SharedPreferences). Si R8 les cambia el nombre a los campos, el
# JSON deja de coincidir y la app arranca sin preguntas o pierde los datos.
-keep class es.labjc.aplicaciontest.data.** { *; }

# Gson necesita los tipos genéricos de TypeToken (Map<Int, QuestionStat>, List<ExamRecord>).
-keepattributes Signature
-keep class * extends com.google.gson.reflect.TypeToken

# Números de línea en los informes de fallos, sin mostrar el nombre de los ficheros.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
