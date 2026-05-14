package com.flappybird;

public interface InterfaceFlappyBird {

    // Flujo principal de la aplicacion
    void run();

    // Inicializa GLFW/OPENGL + shaders + geometria base
    void init();

    /* Crea shaders 2D
    - Vertex: transforma quad/tri base con escala, offset y rotacion. Pasa Y local para degradados.
    - Fragment: Color uniforme o Degradado vertical interpolado */
    void crearShaders();

    // Verificacion de compilacion GLSL
    void comprobarShader(int shader, String tipo);

    /* Crea geometrias basicas:
    - Quad unitario (para cuerpo, obstaculos, ojos)
    - Triangulo unitario (para pico, cola) */
    void crearGeometrias();

    /* Reinicia el estado de la partida
       Se usa al iniciar app y al reiniciar tras game over */
    void resetGame();

    /* Input de los jugadores
        ESC: Salir
        SPACE: Saltar Jugador 1
        W: Saltar Jugador 2
        R: Reiniciar partida (solo cuando ambos murieron) */
    void procesarInput();

    /* Actualizacion de logica por frame
       -Fisica y colisiones para cada pajaro
       -Spawn y movimiento de tuberias compartido */
    void actualizar(float dt);

    // Logica individual de fisica y colision
    void actualizarLogicaPajaro(Pajaro p, float dt);

    // Crear tuberia nueva en borde derecho con gap vertical aleatorio
    void spawnTuberia();

    /* Colision AABB simplificada
       1. Si no hay overlap horizontal, no colisiona
       2. Si hay overlap horizontal, colisiona si el pajaro esta fuera del gap */
    boolean colisionaConTuberia(Pajaro p, Tuberia t);

    // Renderizado principal de la escena
    void render();
    
    // Dibuja un texto centrado en (cx, cy) usando la fuente pixel
    void dibujarTextoPixel(String texto, float cx, float cy, float pixelSize, float r, float g, float b);
    
    // Dibuja un numero centrado en (cx, cy) usando 7 segmentos (10x14 pixeles base)
    void dibujarNumero(int num, float cx, float cy, float pixelSize, float r, float g, float b);
    
    void dibujarDigito(int d, float x, float y, float s, float r, float g, float b);
    
    // Dibuja un pajaro ensamblando sus diferentes partes
    void dibujarPajaroCompleto(Pajaro p);
    
    // Helper original (color solido)
    void dibujarRectangulo(float x, float y, float ancho, float alto, float r, float g, float b, float rotation);
    
    // Helper para dibujar rectangulos con degradado
    void dibujarRectanguloDegradado(float x, float y, float ancho, float alto, float r1, float g1, float b1, float r2, float g2, float b2, float rotation, int useGradient);
    
    // Helper original (color solido)
    void dibujarTriangulo(float x, float y, float ancho, float alto, float r, float g, float b, float rotation);
    
    // Helper para dibujar triangulos con degradado
    void dibujarTrianguloDegradado(float x, float y, float ancho, float alto, float r1, float g1, float b1, float r2, float g2, float b2, float rotation, int useGradient);
    
    // Actualiza el feedback del titulo de la ventana con puntaje y estado
    void actualizarTitulo();
    
    /* Bucle principal
      -Calcula dt
      -Procesa input 
      -Actualiza logica
      -Renderiza
      -Swap/Poll */
    void loop();
    
    // Limpieza de todos los recursos generados (VAOs, VBOs, Shaders)
    void cleanup();
}
