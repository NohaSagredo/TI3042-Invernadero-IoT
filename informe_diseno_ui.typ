#set page(
  paper: "a4",
  margin: (x: 1.8cm, top: 1.8cm, bottom: 1.6cm),
  footer: text(size: 8pt, fill: rgb("#6b7280"))[
    #line(length: 100%, stroke: 0.5pt + rgb("#d1d5db"))
    #v(2pt)
    #grid(
      columns: (1fr, auto),
      [Informe Técnico de Diseño UI | Jetpack Compose & Material 3],
      [Página 1 de 1]
    )
  ]
)

#set text(
  font: ("Liberation Sans", "DejaVu Sans"),
  size: 9.5pt,
  lang: "es",
  fill: rgb("#111827")
)
#set par(justify: true, leading: 0.55em)

// Encabezado institucional compacto
#grid(
  columns: (1fr, auto),
  align: (left, right),
  [
    #text(size: 14pt, weight: "bold", fill: rgb("#1b5e20"))[Decisiones de Diseño UI y Arquitectura Declarativa] \
    #text(size: 10pt, weight: "medium", fill: rgb("#374151"))[Aplicación Móvil: Flujos de Perfil, Configuración y Navegación]
  ],
  [
    #text(size: 8.5pt, fill: rgb("#4b5563"))[
      *Autor:* Noha Sagredo \
      *Tecnología:* Jetpack Compose (M3) \
      *Fecha:* 27 de Octubre de 2026
    ]
  ]
)

#v(4pt)
#line(length: 100%, stroke: 1pt + rgb("#1b5e20"))
#v(6pt)

= 1. Enfoque General y Principios de Diseño
La interfaz fue desarrollada bajo el paradigma declarativo de *Jetpack Compose* con especificación *Material 3*. El objetivo central es desacoplar el estado de la representación visual, garantizar accesibilidad táctil (objetivos mínimos de 48×48 dp) y asegurar persistencia ante recomposiciones y cambios de configuración mediante `rememberSaveable`.

= 2. Decisiones Técnicas por Pantalla

== A. Pantalla de Perfil (`PantallaPerfil.kt`)
- *Identidad Visual y Recorte Geométrico:* Se implementó un componente `Image` circular utilizando el modificador `.clip(CircleShape)` con un borde tonal (`2.dp`) ligado a `MaterialTheme.colorScheme.primary`. Esto establece un punto focal claro sin recurrir a librerías externas para la renderización de avatares.
- *Entrada Reactiva de Nombre:* El campo de usuario emplea `OutlinedTextField` de una sola línea, vinculado al estado reactivo mutable. Su almacenamiento en `rememberSaveable` previene la pérdida de datos durante la rotación de pantalla o redimensionamiento en modo multiventana.
- *Ergonomía en Selección de Intereses:* En lugar de ubicar casillas aisladas de difícil pulsación, el `Row` horizontal utiliza un componente modular (`InteresCheckboxItem`) que extiende el área táctil a toda la etiqueta textual mediante `.clickable`. La separación con `Arrangement.SpaceBetween` garantiza distribución simétrica en pantallas de cualquier densidad.
- *Desacoplamiento de Navegación:* El botón "Ir a Configuración" delega la acción a una función de orden superior (`onIrAConfiguracion: () -> Unit`). Esto permite previsualizar la pantalla en `@Preview` sin instanciar controladores de navegación complejos.

== B. Pantalla de Configuración (`PantallaConfiguracion.kt`)
- *Control de Parámetro Continuo:* El control de volumen emplea `Slider` normalizado en el rango `0f..100f`, sincronizado con iconos semánticos automirrored (`VolumeDown` y `VolumeUp`) que respetan la dirección de lectura del sistema (LTR/RTL).
- *Control Binario con Contexto:* El `Checkbox` de notificaciones se encapsula dentro de una tarjeta interactiva de ancho completo (`clickable`), acompañada de subtítulos descriptivos ("Notificaciones habilitadas" / "Silenciado") que eliminan la ambigüedad del estado booleano.
- *Retroalimentación en Tiempo Real (Resumen):* Se diseñó un panel contenedor (`primaryContainer`) que sintetiza los valores activos (`$volumenEntero%` y estado del checkbox). Esto otorga confirmación visual inmediata al usuario sin necesidad de guardar explícitamente.
- *Retorno de Pila:* El botón inferior ejecuta el callback de retorno (`onVolverAlPerfil`), asegurando una navegación intuitiva y coherente con el botón físico/gesto atrás del sistema Android.

== C. Arquitectura de Navegación (`NavegacionApp.kt`)
Se estructuró un grafo de navegación nativo con `NavHost` y `rememberNavController`:
- *Rutas canónicas:* Rutas explícitas en formato cadena (`"perfil"` y `"configuracion"`).
- *Gestión de Pila de Actividades:* La transición de regreso utiliza `navController.popBackStack()`, destruyendo la vista secundaria sin duplicar instancias en la pila de memoria.

#v(4pt)

#table(
  columns: (1.2fr, 2fr, 1.8fr),
  align: (left, left, left),
  stroke: 0.5pt + rgb("#d1d5db"),
  fill: (col, row) => if row == 0 { rgb("#e8f5e9") } else { none },
  [*Componente*], [*Decisión de Diseño*], [*Beneficio Técnico / UX*],
  [`CircleShape`], [Recorte vectorial directo sobre `Image`], [Cero dependencias externas; rendimiento nativo de dibujo.],
  [`rememberSaveable`], [Retención de estados primitivos y colecciones], [Inmunidad a pérdida de datos por ciclo de vida Android.],
  [`AutoMirrored Icons`], [Iconografía adaptativa según dirección de texto], [Cumplimiento estricto de accesibilidad e internacionalización.],
  [`popBackStack()`], [Desapilado formal en retorno de navegación], [Evita fugas de memoria y acumulación de rutas huérfanas.]
)
