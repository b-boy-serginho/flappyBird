# Cambios Realizados en Flappy Bird OpenGL

Este documento resume las correcciones y mejoras implementadas en el proyecto.

## 1. Correcciones de Errores Iniciales (Bugs de Compilación)

### Shaders
Se corrigieron errores tipográficos en los códigos de los shaders:
```glsl
// Vertex Shader (layout corregido)
layout(location = 0) in vec3 aPos;

// Fragment Shader (1.0 corregido)
fragColor = vec4(uColor, 1.0);
```

### Limpieza de Recursos
```java
// cleanup() corregido para borrar VBO correctamente
GL15.glDeleteBuffers(vbo);
```

---

## 2. Lógica de Juego y Tuberías

### Generación Automática
Se añadió el temporizador en `actualizar(float dt)`:
```java
// Temporizador para generar nuevas tuberias
timerSpawn += dt;
if(timerSpawn >= TIEMPO_ENTRE_TUBERIAS){
    timerSpawn = 0.0f;
    spawnTuberia();
}
```

---

## 3. Mejora del Pájaro (Personaje Detallado)

### Rotación en Vertex Shader
Se añadió soporte para rotación 2D:
```glsl
uniform float uRotation;
void main(){
    float s = sin(uRotation);
    float c = cos(uRotation);
    mat2 rot = mat2(c, s, -s, c);
    
    vec2 pos = aPos.xy * uScale;
    vec2 finalPos = rot * pos + uOffset;
    gl_Position = vec4(finalPos, aPos.z, 1.0);
}
```

### Composición del Pájaro
Se implementó `dibujarPajaroCompleto()` para ensamblar las partes:
```java
private void dibujarPajaroCompleto(){
    float angulo = Math.max(-0.6f, Math.min(0.4f, birdVelY * 0.4f));

    // 1. COLA
    dibujarTriangulo(BIRD_X - 0.04f, birdY, 0.05f, 0.05f, 0.95f, 0.75f, 0.0f, angulo + 3.1415f);

    // 2. CUERPO
    dibujarRectangulo(BIRD_X, birdY, BIRD_ANCHO, BIRD_ALTO, 0.98f, 0.85f, 0.20f, angulo);

    // 3. ALA (Animada)
    float oscilacionAla = (float)Math.sin(alaTimer) * 0.03f;
    dibujarRectangulo(BIRD_X - 0.01f, birdY + oscilacionAla, 0.06f, 0.04f, 0.95f, 0.95f, 0.95f, angulo);

    // 4. OJO (con offset rotado)
    float rx = eyeOffsetX * cosA - eyeOffsetY * sinA;
    float ry = eyeOffsetX * sinA + eyeOffsetY * cosA;
    dibujarRectangulo(BIRD_X + rx, birdY + ry, 0.025f, 0.035f, 1.0f, 1.0f, 1.0f, angulo);

    // 5. PICO
    dibujarTriangulo(BIRD_X + bx, birdY + by, 0.04f, 0.03f, 1.0f, 0.5f, 0.0f, angulo);
}
```

---

## 4. Mejoras Técnicas en OpenGL

### Nuevas Geometrías (VAOs/VBOs)
Se añadieron buffers para triángulos en `crearGeometrias()`:
```java
vaoTri = GL30.glGenVertexArrays();
GL30.glBindVertexArray(vaoTri);
vboTri = GL15.glGenBuffers();
GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTri);
// ... carga de vértices del triángulo ...
```

### Helpers de Dibujo con Rotación
```java
private void dibujarRectangulo(float x, float y, float ancho, float alto, float r, float g, float b, float rotation){
    GL30.glBindVertexArray(vao);
    GL20.glUniform2f(uOffsetLocation, x, y);
    GL20.glUniform2f(uScaleLocation, ancho, alto);
    GL20.glUniform3f(uColorLocation, r, g, b);
    GL20.glUniform1f(uRotationLocation, rotation);
    GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
}

---

## 5. Modo Multijugador (2 Jugadores)

Se ha implementado un modo competitivo local en la misma ventana:

- **Controles Independientes**:
    - **Jugador 1 (Amarillo)**: Tecla `FLECHA ARRIBA`.
    - **Jugador 2 (Cian)**: Tecla `W`.
- **Física y Puntaje Individual**: Cada pájaro tiene su propia velocidad, posición y contador de puntos.
- **Fin de Juego Cooperativo/Competitivo**: La partida solo termina cuando **ambos** pájaros han chocado. Si uno muere, el otro puede seguir jugando para superar el puntaje.
- **Interfaz Dual**: El título de la ventana muestra ambos puntajes por separado en tiempo real.

### Estructura del Jugador
```java
private static class Pajaro {
    float y, velY, alaTimer;
    int puntaje;
    boolean gameOver, started;
    int jumpKey;
    float[] color; // RGB específico para cada jugador
}
```

---

## 6. Dificultad Progresiva

Se ha implementado un sistema que aumenta el desafío conforme los jugadores avanzan:

- **Escalado por Puntaje**: El nivel aumenta cada **10 puntos** (basado en el jugador con mayor puntaje).
- **Incremento de Velocidad**: Las tuberías se mueven más rápido en cada nivel (de `0.62` a `1.6`).
- **Mayor Frecuencia**: El tiempo de espera entre tuberías se reduce (de `1.5s` a `0.8s`).
- **Límites Razonables**: Se han establecido topes máximos para asegurar que el juego siga siendo jugable en niveles altos.

---

## 7. Gran Actualización Estética y Sonora (Overhaul)

Para mejorar la presentación sin añadir dependencias externas pesadas, se ha implementado un sistema **100% procedural**:

### Gráficos Avanzados y Entorno Parallax
- **Shaders de Degradado**: Los shaders ahora soportan interpolación vertical de colores, permitiendo fondos y objetos con volumen 3D.
- **Cielo Dinámico**: El fondo es un degradado continuo que simula la atmósfera.
- **Montañas Parallax**: Se dibujan montañas geométricas en el fondo que se mueven más lento que el juego principal, creando un efecto de profundidad.
- **Suelo en Movimiento**: Una banda inferior rayada que se desplaza a la misma velocidad que las tuberías.
- **Tuberías Clásicas**: Ahora tienen degradados y "bordes" (caps) en los extremos, fieles al juego original.

### Interfaz Digital (HUD Retro)
- **Display 7-Segmentos**: Se implementó una lógica para dibujar números digitales directamente en pantalla usando primitivas. Los puntajes ahora se ven en tiempo real sobre el juego (HUD) con los colores de cada jugador, eliminando la dependencia del título de la ventana.
- **Efecto de Daño**: Cuando un pájaro choca, comienza a parpadear (blink) rápidamente indicando el Game Over.

### Audio Sintetizado (Retro 8-bit)
Se creó la clase `SoundManager` que usa la API nativa de Java (`javax.sound.sampled`) para generar ondas de sonido en tiempo real:
- **Salto**: Una onda senoidal ascendente rápida (Sweep).
- **Punto**: Un sonido muy agudo y corto ("Ding").
- **Choque**: Una onda cuadrada de baja frecuencia saturada para simular ruido ("Crunch").

- **HUD Dinámico**: El nivel actual se muestra en el título de la ventana.

### Variables de Control
```java
private static final float MAX_VELOCIDAD_TUBERIAS = 1.6f;
private static final float MIN_TIEMPO_ENTRE_TUBERIAS = 0.8f;
private static final int PUNTOS_POR_NIVEL = 10;
```
