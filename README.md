# AppTest labjc

App Android para preparar los exámenes tipo test del Servicio Canario de la Salud (SCS). Hay una app por categoría (Auxiliar de Enfermería, Celador…) y todas salen del mismo código.

Hecha con Kotlin y Jetpack Compose. Es la versión 4.0, reconstruida en noviembre de 2026 a partir de la 3.0, cuyo código fuente se perdió.

## Modos

| Modo | Qué hace |
|---|---|
| **Test** | Recorre todas las preguntas en orden y corrige cada una al momento. Guarda el progreso y permite empezar desde cualquier número de pregunta. |
| **Aleatorio** | Todas las preguntas en orden aleatorio. |
| **Repaso** | Solo las preguntas falladas, de más a menos fallos. Una pregunta sale del repaso al acertarla 2 veces seguidas. |
| **Simulacro** | 50 preguntas repartidas por todos los bloques del temario, con un minuto por pregunta y sin corrección hasta entregar, como en un examen real. Muestra la nota (sin penalización por fallos) y el resultado por bloques, y pasa los fallos y las preguntas en blanco al repaso. |
| **Estadísticas** | Preguntas vistas, porcentaje de aciertos, preguntas pendientes de repaso, historial de simulacros y preguntas más falladas. Desde aquí se puede reiniciar todo. |

En todas las preguntas (menos durante el simulacro) está el botón **Reportar error en esta pregunta**, que abre el correo a Contacto@labjc.es con la pregunta ya escrita.

## Categorías (variantes)

Cada categoría es una *product flavor* de Gradle con su propia carpeta:

```
app/src/
├── main/                         Código común, logo y colores de labjc
└── auxiliarEnfermeria/
    ├── assets/questions.json     Banco de preguntas
    └── res/                      Icono y color de fondo del icono
```

### Añadir una categoría

1. Genera su `questions.json` con el conversor (`ConvertidorTxtAJson`).
2. Cópialo a `app/src/<categoria>/assets/questions.json`.
3. En `app/build.gradle.kts`, copia el bloque `create("auxiliarEnfermeria")` y cambia el nombre, el `applicationId` y `app_name`.
4. Si quieres un icono propio, ponlo en `app/src/<categoria>/res/mipmap-*`. Si no, se usa el matraz de labjc.

### Formato de questions.json

```json
{
  "title": "Auxiliar de Enfermería - SCS",
  "blocks": [
    { "name": "Prevención de riesgos laborales", "from": 1, "to": 15 }
  ],
  "questions": [
    {
      "text": "¿Cuál es la Ley de Prevención de Riesgos Laborales?",
      "options": [
        { "text": "Ley 31/1995, de 6 de noviembre.", "isCorrect": false },
        { "text": "Ley 13/1995, de 8 de noviembre.", "isCorrect": false },
        { "text": "Ley 31/1995, de 8 de noviembre.", "isCorrect": true },
        { "text": "Ley 31/1996, de 6 noviembre.", "isCorrect": false }
      ]
    }
  ]
}
```

- Cada pregunta tiene que tener **4 opciones y una sola correcta**. Las que no cumplen eso se descartan; en Logcat (etiqueta `TestViewModel`) se ve cuáles.
- El número de cada pregunta es su posición en el fichero (1, 2, 3…).
- `blocks` es **opcional**: son los bloques temáticos del simulacro, en números de pregunta. Si no existe, o deja preguntas fuera, las preguntas se reparten en bloques consecutivos de 10.

## Compilar

```
./gradlew assembleAuxiliarEnfermeriaDebug      # APK de prueba
./gradlew assembleAuxiliarEnfermeriaRelease    # APK de publicación (necesita la firma)
./gradlew testAuxiliarEnfermeriaDebugUnitTest  # tests
```

Los APK quedan en `app/build/outputs/apk/<categoria>/<tipo>/`.

## Firmar la versión de publicación

Android solo deja actualizar una app si la nueva versión está firmada con **la misma clave** que la anterior. Si la clave se pierde, como pasó con la v3.0, quien tenga la app tiene que desinstalarla y perder sus datos.

1. Crea la clave **una sola vez**, fuera de la carpeta del proyecto:
   ```
   keytool -genkeypair -v -keystore ~/claves/labjc-apps.jks -alias labjc -keyalg RSA -keysize 4096 -validity 10000
   ```
2. Crea `keystore.properties` en la raíz del proyecto. Git lo ignora:
   ```
   storeFile=/home/<usuario>/claves/labjc-apps.jks
   storePassword=...
   keyAlias=labjc
   keyPassword=...
   ```
3. **Haz copia de seguridad del `.jks` y de las contraseñas** en otro sitio: el servidor, un USB o un gestor de contraseñas. Sin ellos no se pueden publicar actualizaciones.

Sin `keystore.properties`, la versión de publicación se compila sin firmar.

## Cambios respecto a la v3.0

- Nuevo modo simulacro por bloques, pantalla de estadísticas mejorada y salida de preguntas del repaso.
- Banco de Auxiliar de Enfermería regenerado desde el texto original. En la v3.0, **12 preguntas no tenían ninguna respuesta correcta**, porque el conversor pegaba "Respuesta Correcta: X" al final de la opción D. Además, 2 enunciados estaban cortados. Eran las preguntas 5, 6, 21, 52, 77, 80, 84, 95, 216, 225, 258, 276, 284 y 290.
- Nuevo `applicationId` (`es.labjc.test.auxiliarenfermeria`), porque la clave de firma de la v3.0 se perdió. Se instala junto a la v3.0, que se puede desinstalar.

## Autor

Juan Carlos Gómez · [labjc.es](https://labjc.es) · Contacto@labjc.es
