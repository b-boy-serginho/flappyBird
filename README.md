# 🐦 Flappy Bird — OpenGL Multijugador

Juego Flappy Bird multijugador desarrollado con **Java 17**, **LWJGL 3** (OpenGL + GLFW) y **JOML**.
Soporta dos jugadores simultáneos compitiendo en la misma pantalla.

---

## 🎮 Controles

| Acción | Jugador 1 (Amarillo) | Jugador 2 (Cian) |
|---|---|---|
| Saltar | `W` | `↑` (Flecha Arriba) |
| Reiniciar partida | `R` (cuando ambos pierden) | `R` (cuando ambos pierden) |
| Salir | `ESC` | `ESC` |

---

## 🛠️ Requisitos

- **Java JDK 17** o superior
- **Maven** (solo si se desea usar `mvn`; el script `run.bat` no lo requiere)
- Dependencias descargadas en `~/.m2/repository/` (LWJGL 3.3.3 + JOML 1.10.5)

---

## 🚀 Compilar y Ejecutar

### Opción 1: Script rápido (recomendado)

Desde la raíz del proyecto, ejecutar en **cmd** o **PowerShell**:

```
.\run
```

Este script compila y ejecuta automáticamente el juego.

### Opción 2: Maven

Si Maven está instalado y en el PATH:

```
mvn compile exec:exec
```

---

## 📁 Estructura del Proyecto

```
com.flappybird/
│
├── AppFlappyBird.java                  ← Punto de entrada y orquestador
│
├── engine/                             ← Infraestructura OpenGL reutilizable
│   ├── Window.java                     ← Ventana GLFW, callbacks, aspect ratio
│   ├── ShaderProgram.java              ← Compilación y enlace de shaders GLSL
│   └── Renderer.java                   ← Primitivas de dibujo (rect, triángulo, degradado)
│
├── game/                               ← Lógica del juego (sin dependencia de OpenGL)
│   ├── GameConstants.java              ← Constantes centralizadas (física, dificultad, tamaños)
│   ├── Pajaro.java                     ← Modelo del jugador con física y colisión
│   ├── Tuberia.java                    ← Modelo de obstáculo con detección AABB
│   ├── GameState.java                  ← Estado global, dificultad progresiva, update loop
│   └── InputHandler.java              ← Procesamiento de teclado para ambos jugadores
│
├── rendering/                          ← Renderizado de entidades del juego
│   ├── BackgroundRenderer.java         ← Cielo (degradado), montañas y suelo (parallax)
│   ├── PipeRenderer.java              ← Tuberías con degradado verde y bordes (caps)
│   └── BirdRenderer.java             ← Ensamblaje visual del pájaro (cola, cuerpo, ala, ojo, pico)
│
├── ui/                                 ← Interfaz de usuario (HUD y texto)
│   ├── PixelFont.java                  ← Fuente bitmap 5×7 píxeles por carácter
│   ├── SevenSegmentDisplay.java        ← Display numérico de 7 segmentos para puntaje
│   └── HudRenderer.java              ← Puntajes, pantalla Game Over y menú de inicio
│
└── audio/                              ← Audio procedural
    └── SoundManager.java               ← Efectos 8-bit sintetizados (salto, punto, choque)
```

---

## 🎯 Mecánicas del Juego

- **Dificultad progresiva**: cada 10 puntos las tuberías se mueven más rápido y aparecen con mayor frecuencia.
- **Fondo parallax**: cielo estático, montañas lentas y suelo a velocidad normal.
- **Audio procedural**: sonidos generados por código sin archivos `.wav` externos.
- **Colisión AABB**: detección de colisión simplificada entre pájaro y tuberías.

---

## 📦 Dependencias

| Librería | Versión | Propósito |
|---|---|---|
| LWJGL Core | 3.3.3 | Carga de bibliotecas nativas |
| LWJGL GLFW | 3.3.3 | Ventanas, teclado, ratón |
| LWJGL OpenGL | 3.3.3 | Renderizado 2D (shaders, buffers) |
| LWJGL STB | 3.3.3 | Utilidades de imagen |
| JOML | 1.10.5 | Matemáticas (vectores, matrices) |
