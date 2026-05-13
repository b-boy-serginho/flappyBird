package com.flappybird;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

public class AppFlappyBird {

    // Tamaño inicial de la ventana
    private static final int ANCHO = 900;
    private static final int ALTO = 700;

    // Poscion horizontal del pajaro en NDC
    private static final float BIRD_X = -0.45f;

    // Tamaño del pajaro
    private static final float BIRD_ANCHO = 0.10f;
    private static final float BIRD_ALTO = 0.10f;

    // Fisica vertical
    private static final float GRAVEDAD = -1.9f;
    private static final float IMPULSO_SALTO = 0.85f;
    private static final float VELOCIDAD_MAX_CAIDA = -1.8f;

    // Parametros de tuberias (Dificultad inicial)
    private static final float BASE_VELOCIDAD_TUBERIAS = 0.62f;
    private static final float BASE_TIEMPO_ENTRE_TUBERIAS = 1.5f;
    private static final float TUBERIA_ANCHO = 0.18f;
    private static final float GAP_ALTO = 0.48f;
    private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;

    // Limites de dificultad
    private static final float MAX_VELOCIDAD_TUBERIAS = 1.6f;
    private static final float MIN_TIEMPO_ENTRE_TUBERIAS = 0.8f;
    private static final float INCREMENTO_VELOCIDAD = 0.08f;
    private static final float DECREMENTO_TIEMPO = 0.1f;
    private static final int PUNTOS_POR_NIVEL = 10;

    // Recursos OPENGL basicos
    private long window;
    private int programa;
    private int vao;        // VAO para rectangulos
    private int vbo;        // VBO para rectangulos
    private int vaoTri;     // VAO para triangulos (pico, cola)
    private int vboTri;     // VBO para triangulos (pico, cola)

    // Uniforms de transformacion, color y rotacion
    private int uOffsetLocation;
    private int uScaleLocation;
    private int uColorLocation;
    private int uColor2Location; // Segundo color para el degradado
    private int uUseGradientLocation; // Bandera para activar degradado (0 o 1)
    private int uRotationLocation; // Ubicacion del uniform de rotacion

    // Estado de los jugadores
    private Pajaro p1;
    private Pajaro p2;
    private boolean isGameOver;
    private boolean prevR;
    private float timerSpawn;

    // Estado de dificultad progresiva
    private int nivelActual;
    private float currentVelTuberias;
    private float currentTiempoSpawn;
    private float bgOffset; // Desplazamiento para el fondo parallax

    // Lista de Obstaculos activos
    private final List<Tuberia> tuberias = new ArrayList<>();

    // RNG para variar la posicion del GAP
    private final Random random = new Random();

    /*  Modelo de una tuberia
        x: Posicion horizontal comun para parte superior / inferior
        gapCentroY: centro vertical del hueco
        puntuada: Evita sumar 2 veces la misma tuberia */

    private static class Tuberia {
        float x;
        float gapCentroY;
        boolean puntuadaP1; // Puntuada por Jugador 1
        boolean puntuadaP2; // Puntuada por Jugador 2

        Tuberia (float x, float gapCentroY){
            this.x = x;
            this.gapCentroY = gapCentroY;
        }
    }

    // Clase para representar a cada jugador/pajaro
    private static class Pajaro {
        float y;
        float velY;
        float alaTimer;
        int puntaje;
        boolean gameOver;
        boolean started;
        boolean prevJump;
        int jumpKey;
        float[] color; // RGB
        String nombre;

        Pajaro(int jumpKey, float[] color, String nombre) {
            this.jumpKey = jumpKey;
            this.color = color;
            this.nombre = nombre;
            reset();
        }

        void reset() {
            y = 0.0f;
            velY = 0.0f;
            alaTimer = 0.0f;
            puntaje = 0;
            gameOver = false;
            started = false;
        }
    }

    public AppFlappyBird() {
        // Inicializar jugadores con sus teclas y colores
        p1 = new Pajaro(GLFW.GLFW_KEY_W, new float[]{0.98f, 0.85f, 0.20f}, "Amarillo");
        p2 = new Pajaro(GLFW.GLFW_KEY_UP, new float[]{0.20f, 0.85f, 0.98f}, "Cian");
    }

    // Flujo principal de la aplicacion
    public void run(){
        init();
        // Estado inicial listo para jugar
        resetGame();
        loop();
        cleanup();
    }

    // Inicializa GLFW/OPENGL + shaders + geometria base
    private void init(){
        // Arranque de GLFW
        if (!GLFW.glfwInit()) throw new IllegalStateException("No se pudo inicializar GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Crear la ventana
        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird OPENGL - Multijugador", 0, 0);

        if (window == 0) throw new RuntimeException("No se pudo crear la ventana");

        // Contexto + VSync + Mostrar
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        // Cargar Funciones OPENGL (debe ir antes de cualquier llamada GL)
        GL.createCapabilities();

        // Configurar viewport inicial
        GL11.glViewport(0, 0, ANCHO, ALTO);

        // Callback de redimensionamiento: actualiza el viewport manteniendo aspect ratio
        GLFW.glfwSetFramebufferSizeCallback(window, (win, width, height) -> {
            if (width == 0 || height == 0) return; // Minimizado
            float targetAspect = (float) ANCHO / (float) ALTO;
            float windowAspect = (float) width / (float) height;

            int vpX, vpY, vpW, vpH;
            if (windowAspect > targetAspect) {
                // Ventana mas ancha que el juego: barras laterales (pillarbox)
                vpH = height;
                vpW = (int) (height * targetAspect);
                vpX = (width - vpW) / 2;
                vpY = 0;
            } else {
                // Ventana mas alta que el juego: barras arriba/abajo (letterbox)
                vpW = width;
                vpH = (int) (width / targetAspect);
                vpX = 0;
                vpY = (height - vpH) / 2;
            }
            GL11.glViewport(vpX, vpY, vpW, vpH);
        });

        // Crear pipeline y quad unitario reutilizable
        crearShaders();
        crearGeometrias();
    }

    /* Crea shaders 2D
    - Vertex: transforma quad/tri base con escala, offset y rotacion. Pasa Y local para degradados.
    - Fragment: Color uniforme o Degradado vertical interpolado */
    private void crearShaders(){
        String vertexSrc = """
                #version 330 core
                layout(location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform vec2 uScale;
                uniform float uRotation;
                out float localY;
                void main(){
                    localY = aPos.y; // Coordenada local Y (-0.5 a 0.5) para usar en degradados
                    
                    // Aplicar rotacion 2D en el shader
                    float s = sin(uRotation);
                    float c = cos(uRotation);
                    mat2 rot = mat2(c, s, -s, c);
                    
                    // Escalar primero, luego rotar, luego trasladar
                    vec2 pos = aPos.xy * uScale;
                    vec2 finalPos = rot * pos + uOffset;
                    
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                }
                """;

        // Color solido por Objetos o Degradado
        String fragmentSrc = """
                #version 330 core
                uniform vec3 uColor;
                uniform vec3 uColor2;
                uniform int uUseGradient;
                in float localY;
                out vec4 fragColor;
                void main(){
                    if (uUseGradient == 1) {
                        // Mapear localY de [-0.5, 0.5] a [0.0, 1.0]
                        float factor = localY + 0.5;
                        // uColor en el tope (factor = 1.0), uColor2 en la base (factor = 0.0)
                        vec3 colorGradient = mix(uColor2, uColor, factor);
                        fragColor = vec4(colorGradient, 1.0);
                    } else {
                        fragColor = vec4(uColor, 1.0);
                    }
                }
                """;

        // Compilar vertex shader
        int vertexShader = GL20.glCreateShader(GL20.GL_VERTEX_SHADER);
        GL20.glShaderSource(vertexShader, vertexSrc);
        GL20.glCompileShader(vertexShader);
        comprobarShader(vertexShader, "Vertex");

        //compilar fragment shader
        int fragmentShader = GL20.glCreateShader(GL20.GL_FRAGMENT_SHADER);
        GL20.glShaderSource(fragmentShader, fragmentSrc);
        GL20.glCompileShader(fragmentShader);
        comprobarShader(fragmentShader, "Fragment");

        // Link del programa
        programa = GL20.glCreateProgram();
        GL20.glAttachShader(programa, vertexShader);
        GL20.glAttachShader(programa, fragmentShader);
        GL20.glLinkProgram(programa);

        if (GL20.glGetProgrami(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE){
            throw new RuntimeException("Error al lanzar: " + GL20.glGetProgramInfoLog(programa));
        }

        // Resolver ubicacion de variables uniform en el shader
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");
        uColor2Location = GL20.glGetUniformLocation(programa, "uColor2");
        uUseGradientLocation = GL20.glGetUniformLocation(programa, "uUseGradient");
        uRotationLocation = GL20.glGetUniformLocation(programa, "uRotation");

        if (uOffsetLocation == -1 || uScaleLocation == -1 || uColorLocation == -1 || uRotationLocation == -1){
            throw new RuntimeException("No se pudieron obtener uniforms principales del shader");
        }

        // Limpiar objetos shader temporales
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    // Verificacion de compilacion GLSL
    private void comprobarShader(int shader, String tipo){
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE){
            throw new RuntimeException(tipo + "shader: "+GL20.glGetShaderInfoLog(shader));
        }
    }    

    /* Crea geometrias basicas:
    - Quad unitario (para cuerpo, obstaculos, ojos)
    - Triangulo unitario (para pico, cola) */
    private void crearGeometrias(){
        // --- CONFIGURACION DEL QUAD (Rectangulo) ---
        float[] verticesQuad = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer bufferQuad = BufferUtils.createFloatBuffer(verticesQuad.length);
        bufferQuad.put(verticesQuad).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufferQuad, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // --- CONFIGURACION DEL TRIANGULO (Pico y Cola) ---
        // Triangulo isoceles apuntando a la derecha
        float[] verticesTri = {
            -0.5f, -0.5f, 0.0f,
             0.5f,  0.0f, 0.0f,
            -0.5f,  0.5f, 0.0f
        };

        vaoTri = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoTri);
        vboTri = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTri);
        FloatBuffer bufferTri = BufferUtils.createFloatBuffer(verticesTri.length);
        bufferTri.put(verticesTri).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufferTri, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Desvincular para evitar modificaciones accidentales
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /* Reinicia el estado de la partida
       Se usa al iniciar app y al reiniciar tras game over */
    private void resetGame(){
        p1.reset();
        p2.reset();
        isGameOver = false;
        timerSpawn = 0.0f;
        nivelActual = 1;
        currentVelTuberias = BASE_VELOCIDAD_TUBERIAS;
        currentTiempoSpawn = BASE_TIEMPO_ENTRE_TUBERIAS;
        tuberias.clear();
        actualizarTitulo();
    }

    /* Input de los jugadores
        ESC: Salir
        SPACE: Saltar Jugador 1
        W: Saltar Jugador 2
        R: Reiniciar partida (solo cuando ambos murieron) */
    private void procesarInput(){
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS){
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        // --- INPUT JUGADOR 1 ---
        boolean jump1 = GLFW.glfwGetKey(window, p1.jumpKey) == GLFW.GLFW_PRESS;
        if (jump1 && !p1.prevJump){
            if (!p1.gameOver) {
                p1.started = true;
                p1.velY = IMPULSO_SALTO;
                p1.alaTimer = 0.0f; 
                SoundManager.playJumpSound();
            }
        }
        p1.prevJump = jump1;

        // --- INPUT JUGADOR 2 ---
        boolean jump2 = GLFW.glfwGetKey(window, p2.jumpKey) == GLFW.GLFW_PRESS;
        if (jump2 && !p2.prevJump){
            if (!p2.gameOver) {
                p2.started = true;
                p2.velY = IMPULSO_SALTO;
                p2.alaTimer = 0.0f;
                SoundManager.playJumpSound();
            }
        }
        p2.prevJump = jump2;

        // Reiniciar con R (solo si el juego terminó para ambos)
        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR && isGameOver){
            resetGame();
        }
        prevR = rAhora;
    }

    /* Actualizacion de logica por frame
       -Fisica y colisiones para cada pajaro
       -Spawn y movimiento de tuberias compartido */
    private void actualizar(float dt){
        // El juego arranca si al menos uno de los jugadores ha saltado
        boolean algunPajaroActivo = p1.started || p2.started;
        if (!algunPajaroActivo || isGameOver) return;

        // --- CALCULO DE DIFICULTAD PROGRESIVA ---
        int maxPuntaje = Math.max(p1.puntaje, p2.puntaje);
        int nuevoNivel = (maxPuntaje / PUNTOS_POR_NIVEL) + 1;
        
        if (nuevoNivel > nivelActual) {
            nivelActual = nuevoNivel;
            // Incrementar velocidad y reducir tiempo de spawn con limites
            currentVelTuberias = Math.min(MAX_VELOCIDAD_TUBERIAS, BASE_VELOCIDAD_TUBERIAS + (nivelActual - 1) * INCREMENTO_VELOCIDAD);
            currentTiempoSpawn = Math.max(MIN_TIEMPO_ENTRE_TUBERIAS, BASE_TIEMPO_ENTRE_TUBERIAS - (nivelActual - 1) * DECREMENTO_TIEMPO);
            actualizarTitulo(); // Reflejar cambio de nivel
        }

        // --- ACTUALIZAR JUGADORES ---
        actualizarLogicaPajaro(p1, dt);
        actualizarLogicaPajaro(p2, dt);

        // Si ambos perdieron, marcar fin de partida global
        if (p1.gameOver && p2.gameOver && !isGameOver) {
            isGameOver = true;
            actualizarTitulo();
            return;
        }

        // --- LOGICA DE TUBERIAS (Solo si alguien sigue vivo) ---
        if (!isGameOver) {
            bgOffset += currentVelTuberias * dt; // Animar fondo parallax
            timerSpawn += dt;
            if(timerSpawn >= currentTiempoSpawn){
                timerSpawn = 0.0f;
                spawnTuberia();
            }

            Iterator<Tuberia> it = tuberias.iterator();
            while (it.hasNext()){
                Tuberia t = it.next();
                t.x -= currentVelTuberias * dt;

                // Puntuar para cada pajaro si pasan la tuberia
                if (!p1.gameOver && t.x + (TUBERIA_ANCHO * 0.5f) < BIRD_X && !t.puntuadaP1){
                    t.puntuadaP1 = true;
                    p1.puntaje++;
                    SoundManager.playScoreSound();
                    actualizarTitulo();
                }
                if (!p2.gameOver && t.x + (TUBERIA_ANCHO * 0.5f) < BIRD_X && !t.puntuadaP2){
                    t.puntuadaP2 = true;
                    p2.puntaje++;
                    SoundManager.playScoreSound();
                    actualizarTitulo();
                }

                if (t.x + (TUBERIA_ANCHO * 0.5f) < -1.3f) it.remove();
            }
        }
    }

    // Logica individual de fisica y colision
    private void actualizarLogicaPajaro(Pajaro p, float dt) {
        if (p.gameOver) return;

        // Animacion de aleteo
        p.alaTimer += dt * 10.0f;

        // Fisica
        p.velY += GRAVEDAD * dt;
        if (p.velY < VELOCIDAD_MAX_CAIDA) p.velY = VELOCIDAD_MAX_CAIDA;
        p.y += p.velY * dt;

        // Colision con techo/suelo
        float birdTop = p.y + (BIRD_ALTO * 0.5f);
        float birdBottom = p.y - (BIRD_ALTO * 0.5f);
        if (birdTop >= 1.0f || birdBottom <= -1.0f){
            if (!p.gameOver) SoundManager.playHitSound();
            p.gameOver = true;
            return;
        }

        // Colision con tuberias
        for (Tuberia t : tuberias) {
            if (colisionaConTuberia(p, t)) {
                if (!p.gameOver) SoundManager.playHitSound();
                p.gameOver = true;
                return;
            }
        }
    }

    // Crear tuberia nueva en borde derecho con gap vertical aleatorio
    private void spawnTuberia(){
        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Tuberia(1.2f, gapCentro));
    }

    /* Colision AABB simplificada
       1. Si no hay overlap horizontal, no colisiona
       2. Si hay overlap horizontal, colisiona si el pajaro esta fuera del gap */
    private boolean colisionaConTuberia(Pajaro p, Tuberia t){
        float birdLeft = BIRD_X - (BIRD_ANCHO * 0.5f);
        float birdRight = BIRD_X + (BIRD_ANCHO * 0.5f);
        float birdBottom = p.y - (BIRD_ALTO * 0.5f);
        float birdTop = p.y + (BIRD_ALTO * 0.5f);
        float pipeLeft = t.x - (TUBERIA_ANCHO * 0.5f);
        float pipeRight = t.x + (TUBERIA_ANCHO * 0.5f);
        boolean overlapX = birdRight > pipeLeft && birdLeft < pipeRight;

        if (!overlapX)  return false;
        
        float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }

    // Renderizado principal de la escena
    private void render(){
        // Limpiar pantalla
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        
        // Activar el programa de shaders
        GL20.glUseProgram(programa);

        // --- FONDO PARALLAX ---
        // 1. Cielo (Degradado estatico de azul oscuro a celeste)
        dibujarRectanguloDegradado(0.0f, 0.0f, 2.0f, 2.0f, 0.05f, 0.2f, 0.5f, 0.5f, 0.8f, 0.95f, 0.0f, 1);

        // 2. Montañas (Se mueven lento: 30% de la velocidad)
        float mntOffset = (bgOffset * 0.3f) % 0.8f;
        for (int i = -3; i <= 3; i++) {
            dibujarTrianguloDegradado(i * 0.8f - mntOffset, -0.6f, 1.0f, 0.8f, 0.1f, 0.4f, 0.2f, 0.05f, 0.2f, 0.1f, 0.0f, 1);
        }

        // 3. Suelo (Se mueve a velocidad normal)
        float groundOffset = bgOffset % 0.2f;
        for (int i = -12; i <= 12; i++) {
            float gR = (i % 2 == 0) ? 0.8f : 0.6f;
            float gG = (i % 2 == 0) ? 0.7f : 0.5f;
            dibujarRectangulo(i * 0.2f - groundOffset, -0.9f, 0.2f, 0.2f, gR, gG, 0.2f, 0.0f);
        }

        // --- TUBERIAS ---
        for (Tuberia t :  tuberias ){
            float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);

            // Tuberia superior (con degradado y borde)
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f){
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                dibujarRectanguloDegradado(t.x, yCentroSup, TUBERIA_ANCHO, altoSuperior, 0.1f, 0.5f, 0.1f, 0.3f, 0.8f, 0.2f, 0.0f, 1);
                dibujarRectangulo(t.x, gapTop + 0.02f, TUBERIA_ANCHO * 1.1f, 0.04f, 0.4f, 0.9f, 0.3f, 0.0f); // Borde (Cap)
            }

            // Tuberia inferior
            float altoInferior = gapBottom + 1.0f;
            if(altoInferior > 0.0f){
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                dibujarRectanguloDegradado(t.x, yCentroInf, TUBERIA_ANCHO, altoInferior, 0.3f, 0.8f, 0.2f, 0.1f, 0.5f, 0.1f, 0.0f, 1);
                dibujarRectangulo(t.x, gapBottom - 0.02f, TUBERIA_ANCHO * 1.1f, 0.04f, 0.4f, 0.9f, 0.3f, 0.0f); // Borde (Cap)
            }
        }

        // --- PAJAROS ---
        dibujarPajaroCompleto(p1);
        dibujarPajaroCompleto(p2);

        // --- HUD Y OVERLAY ---
        if (isGameOver) {
            dibujarRectangulo(0.0f, 0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f); // Overlay oscuro
            dibujarRectangulo(0.0f, 0.05f, 1.4f, 0.65f, 0.12f, 0.14f, 0.18f, 0.0f); // Panel

            dibujarTextoPixel("GAME OVER", 0.0f, 0.25f, 0.025f, 1.0f, 0.3f, 0.3f);
            dibujarTextoPixel("PULSA R REINICIAR", 0.0f, 0.05f, 0.015f, 0.5f, 1.0f, 0.5f);
            dibujarTextoPixel("PULSA ESC SALIR", 0.0f, -0.1f, 0.015f, 1.0f, 0.5f, 0.5f);

        } else if (!p1.started && !p2.started) {
            // Panel central de instrucciones
            dibujarRectangulo(0.0f, 0.15f, 1.7f, 0.7f, 0.0f, 0.0f, 0.05f, 0.0f);

            dibujarTextoPixel("FLAPPY BIRD", 0.0f, 0.42f, 0.022f, 1.0f, 1.0f, 1.0f);

            // Jugador 1 (columna izquierda)
            dibujarRectangulo(-0.4f, 0.22f, 0.08f, 0.08f, p1.color[0], p1.color[1], p1.color[2], 0.0f);
            dibujarTextoPixel("JUGADOR 1", -0.4f, 0.12f, 0.012f, p1.color[0], p1.color[1], p1.color[2]);
            dibujarTextoPixel("TECLA W", -0.4f, 0.01f, 0.01f, 1.0f, 1.0f, 1.0f);

            // Jugador 2 (columna derecha)
            dibujarRectangulo(0.4f, 0.22f, 0.08f, 0.08f, p2.color[0], p2.color[1], p2.color[2], 0.0f);
            dibujarTextoPixel("JUGADOR 2", 0.4f, 0.12f, 0.012f, p2.color[0], p2.color[1], p2.color[2]);
            dibujarTextoPixel("FLECHA ARRIBA", 0.4f, 0.01f, 0.01f, 1.0f, 1.0f, 1.0f);
        }

        // Dibujar Puntaje Digital en Pantalla
        dibujarNumero(p1.puntaje, -0.3f, 0.8f, 0.1f, p1.color[0], p1.color[1], p1.color[2]);
        dibujarNumero(p2.puntaje, 0.3f, 0.8f, 0.1f, p2.color[0], p2.color[1], p2.color[2]);
    }

    // ======== SISTEMA DE FUENTE PIXEL BITMAP (5x7) ========
    // Cada caracter se define como un array de 7 filas de 5 bits.
    private static final java.util.Map<Character, int[]> PIXEL_FONT = new java.util.HashMap<>();
    static {
        PIXEL_FONT.put('A', new int[]{0b01110,0b10001,0b10001,0b11111,0b10001,0b10001,0b10001});
        PIXEL_FONT.put('B', new int[]{0b11110,0b10001,0b10001,0b11110,0b10001,0b10001,0b11110});
        PIXEL_FONT.put('C', new int[]{0b01110,0b10001,0b10000,0b10000,0b10000,0b10001,0b01110});
        PIXEL_FONT.put('D', new int[]{0b11100,0b10010,0b10001,0b10001,0b10001,0b10010,0b11100});
        PIXEL_FONT.put('E', new int[]{0b11111,0b10000,0b10000,0b11110,0b10000,0b10000,0b11111});
        PIXEL_FONT.put('F', new int[]{0b11111,0b10000,0b10000,0b11110,0b10000,0b10000,0b10000});
        PIXEL_FONT.put('G', new int[]{0b01110,0b10001,0b10000,0b10111,0b10001,0b10001,0b01110});
        PIXEL_FONT.put('H', new int[]{0b10001,0b10001,0b10001,0b11111,0b10001,0b10001,0b10001});
        PIXEL_FONT.put('I', new int[]{0b01110,0b00100,0b00100,0b00100,0b00100,0b00100,0b01110});
        PIXEL_FONT.put('J', new int[]{0b00111,0b00010,0b00010,0b00010,0b00010,0b10010,0b01100});
        PIXEL_FONT.put('K', new int[]{0b10001,0b10010,0b10100,0b11000,0b10100,0b10010,0b10001});
        PIXEL_FONT.put('L', new int[]{0b10000,0b10000,0b10000,0b10000,0b10000,0b10000,0b11111});
        PIXEL_FONT.put('M', new int[]{0b10001,0b11011,0b10101,0b10101,0b10001,0b10001,0b10001});
        PIXEL_FONT.put('N', new int[]{0b10001,0b11001,0b10101,0b10011,0b10001,0b10001,0b10001});
        PIXEL_FONT.put('O', new int[]{0b01110,0b10001,0b10001,0b10001,0b10001,0b10001,0b01110});
        PIXEL_FONT.put('P', new int[]{0b11110,0b10001,0b10001,0b11110,0b10000,0b10000,0b10000});
        PIXEL_FONT.put('R', new int[]{0b11110,0b10001,0b10001,0b11110,0b10100,0b10010,0b10001});
        PIXEL_FONT.put('S', new int[]{0b01111,0b10000,0b10000,0b01110,0b00001,0b00001,0b11110});
        PIXEL_FONT.put('T', new int[]{0b11111,0b00100,0b00100,0b00100,0b00100,0b00100,0b00100});
        PIXEL_FONT.put('U', new int[]{0b10001,0b10001,0b10001,0b10001,0b10001,0b10001,0b01110});
        PIXEL_FONT.put('V', new int[]{0b10001,0b10001,0b10001,0b10001,0b01010,0b01010,0b00100});
        PIXEL_FONT.put('W', new int[]{0b10001,0b10001,0b10001,0b10101,0b10101,0b11011,0b10001});
        PIXEL_FONT.put('Y', new int[]{0b10001,0b01010,0b00100,0b00100,0b00100,0b00100,0b00100});
        PIXEL_FONT.put('0', new int[]{0b01110,0b10001,0b10011,0b10101,0b11001,0b10001,0b01110});
        PIXEL_FONT.put('1', new int[]{0b00100,0b01100,0b00100,0b00100,0b00100,0b00100,0b01110});
        PIXEL_FONT.put('2', new int[]{0b01110,0b10001,0b00001,0b00110,0b01000,0b10000,0b11111});
        PIXEL_FONT.put(' ', new int[]{0,0,0,0,0,0,0});
    }

    // Dibuja un texto centrado en (cx, cy) usando la fuente pixel
    private void dibujarTextoPixel(String texto, float cx, float cy, float pixelSize, float r, float g, float b) {
        float charWidth = pixelSize * 6; // 5 pixeles + 1 espacio
        float totalWidth = texto.length() * charWidth;
        float startX = cx - totalWidth / 2.0f;
        float startY = cy + (pixelSize * 7) / 2.0f;

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            int[] glyph = PIXEL_FONT.get(c);
            if (glyph == null) continue;

            float charX = startX + i * charWidth;
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 5; col++) {
                    if ((glyph[row] & (1 << (4 - col))) != 0) {
                        float px = charX + col * pixelSize + pixelSize / 2.0f;
                        float py = startY - row * pixelSize - pixelSize / 2.0f;
                        dibujarRectangulo(px, py, pixelSize, pixelSize, r, g, b, 0.0f);
                    }
                }
            }
        }
    }

    // --- LOGICA DE DIBUJO DE NUMEROS DIGITALES (7 Segmentos) ---
    private void dibujarNumero(int num, float x, float y, float size, float r, float g, float b) {
        String str = String.valueOf(num);
        float cursorX = x - (str.length() * size * 1.5f) / 2.0f; // Centrar
        
        for (char c : str.toCharArray()) {
            int d = c - '0';
            dibujarDigito(d, cursorX, y, size, r, g, b);
            cursorX += size * 1.5f;
        }
    }

    private void dibujarDigito(int d, float x, float y, float s, float r, float g, float b) {
        float w = s * 0.8f;
        float t = s * 0.2f; // Grosor
        float h2 = s; // Medio alto
        
        // Tabla de verdad para display de 7 segmentos (a,b,c,d,e,f,g)
        boolean[] seg = new boolean[7];
        if(d!=1 && d!=4) seg[0]=true; // a (arriba)
        if(d!=5 && d!=6) seg[1]=true; // b (arr-der)
        if(d!=2) seg[2]=true; // c (abj-der)
        if(d!=1 && d!=4 && d!=7) seg[3]=true; // d (abajo)
        if(d==0 || d==2 || d==6 || d==8) seg[4]=true; // e (abj-izq)
        if(d!=1 && d!=2 && d!=3 && d!=7) seg[5]=true; // f (arr-izq)
        if(d!=0 && d!=1 && d!=7) seg[6]=true; // g (centro)

        if(seg[0]) dibujarRectangulo(x, y + h2, w, t, r,g,b, 0);
        if(seg[1]) dibujarRectangulo(x + w/2, y + h2/2, t, h2, r,g,b, 0);
        if(seg[2]) dibujarRectangulo(x + w/2, y - h2/2, t, h2, r,g,b, 0);
        if(seg[3]) dibujarRectangulo(x, y - h2, w, t, r,g,b, 0);
        if(seg[4]) dibujarRectangulo(x - w/2, y - h2/2, t, h2, r,g,b, 0);
        if(seg[5]) dibujarRectangulo(x - w/2, y + h2/2, t, h2, r,g,b, 0);
        if(seg[6]) dibujarRectangulo(x, y, w, t, r,g,b, 0);
    }

    // Dibuja un pajaro ensamblando sus diferentes partes
    private void dibujarPajaroCompleto(Pajaro p){
        if (p.gameOver && !isGameOver) return; // Ocultar temporalmente si murió pero la partida sigue
        
        float r = p.color[0];
        float g = p.color[1];
        float b = p.color[2];

        // Efecto visual de daño (Blink)
        if (p.gameOver && isGameOver) {
            float blink = (float)Math.abs(Math.sin(System.currentTimeMillis() / 100.0));
            r = blink; g = blink * 0.2f; b = blink * 0.2f; // Flash rojo oscuro
        }

        // Calcular inclinacion (pitch) basada en la velocidad vertical del pajaro
        float angulo = Math.max(-0.6f, Math.min(0.4f, p.velY * 0.4f));

        // 1. COLA: Un triangulo del color del pajaro en la parte trasera
        dibujarTriangulo(BIRD_X - 0.04f, p.y, 0.05f, 0.05f, r * 0.8f, g * 0.8f, b * 0.8f, angulo + 3.1415f);

        // 2. CUERPO: El rectangulo principal del color del jugador
        dibujarRectangulo(BIRD_X, p.y, BIRD_ANCHO, BIRD_ALTO, r, g, b, angulo);

        // 3. ALA: Rectangulo blanco que oscila para simular el aleteo
        float oscilacionAla = (float)Math.sin(p.alaTimer) * 0.03f;
        dibujarRectangulo(BIRD_X - 0.01f, p.y + oscilacionAla, 0.06f, 0.04f, 0.95f, 0.95f, 0.95f, angulo);

        // 4. OJO: Fondo blanco y pupila negra
        float eyeOffsetX = 0.025f;
        float eyeOffsetY = 0.02f;
        float cosA = (float)Math.cos(angulo);
        float sinA = (float)Math.sin(angulo);
        float rx = eyeOffsetX * cosA - eyeOffsetY * sinA;
        float ry = eyeOffsetX * sinA + eyeOffsetY * cosA;
        
        dibujarRectangulo(BIRD_X + rx, p.y + ry, 0.025f, 0.035f, 1.0f, 1.0f, 1.0f, angulo); // Esclerotica
        dibujarRectangulo(BIRD_X + rx + (0.005f * cosA), p.y + ry + (0.005f * sinA), 0.01f, 0.015f, 0.0f, 0.0f, 0.0f, angulo); // Pupila

        // 5. PICO: Un triangulo naranja al frente
        float beakOffsetX = 0.045f;
        float bx = beakOffsetX * cosA;
        float by = beakOffsetX * sinA;
        dibujarTriangulo(BIRD_X + bx, p.y + by, 0.04f, 0.03f, 1.0f, 0.5f, 0.0f, angulo);
    }

    // Helper original (color solido)
    private void dibujarRectangulo(float x, float y, float ancho, float alto, float r, float g, float b, float rotation){
        dibujarRectanguloDegradado(x, y, ancho, alto, r, g, b, r, g, b, rotation, 0);
    }

    // Helper para dibujar rectangulos con degradado
    private void dibujarRectanguloDegradado(float x, float y, float ancho, float alto, float r1, float g1, float b1, float r2, float g2, float b2, float rotation, int useGradient){
        GL30.glBindVertexArray(vao);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r1, g1, b1);
        if (uColor2Location != -1) GL20.glUniform3f(uColor2Location, r2, g2, b2);
        if (uUseGradientLocation != -1) GL20.glUniform1i(uUseGradientLocation, useGradient);
        GL20.glUniform1f(uRotationLocation, rotation);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    // Helper original (color solido)
    private void dibujarTriangulo(float x, float y, float ancho, float alto, float r, float g, float b, float rotation){
        dibujarTrianguloDegradado(x, y, ancho, alto, r, g, b, r, g, b, rotation, 0);
    }

    // Helper para dibujar triangulos con degradado
    private void dibujarTrianguloDegradado(float x, float y, float ancho, float alto, float r1, float g1, float b1, float r2, float g2, float b2, float rotation, int useGradient){
        GL30.glBindVertexArray(vaoTri);
        GL20.glUniform2f(uOffsetLocation, x, y);
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        GL20.glUniform3f(uColorLocation, r1, g1, b1);
        if (uColor2Location != -1) GL20.glUniform3f(uColor2Location, r2, g2, b2);
        if (uUseGradientLocation != -1) GL20.glUniform1i(uUseGradientLocation, useGradient);
        GL20.glUniform1f(uRotationLocation, rotation);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
    }

    // Actualiza el feedback del titulo de la ventana con puntaje y estado
    private void actualizarTitulo(){
        String titulo = String.format("Flappy Bird MULTI | Nivel: %d | J1: %d | J2: %d", nivelActual, p1.puntaje, p2.puntaje);
        
        if (isGameOver) {
            titulo += " | GAME OVER - Pulsa R para reiniciar o ESC para salir";
        } else if (!p1.started && !p2.started) {
            titulo += " | Pulsa W y FLECHA ARRIBA para iniciar";
        }
        
        GLFW.glfwSetWindowTitle(window, titulo);
    }

    /* Bucle principal
      -Calcula dt
      -Procesa input 
      -Actualiza logica
      -Renderiza
      -Swap/Poll */
    private void loop(){
        float ultimoTiempo = (float)GLFW.glfwGetTime();
        while (!GLFW.glfwWindowShouldClose(window)){
            float ahora = (float)GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;
            procesarInput();
            actualizar(dt);
            render();

            // Presentar frame y leer eventos
            GLFW.glfwSwapBuffers(window);
            GLFW.glfwPollEvents();
        }
    }

    // Limpieza de todos los recursos generados (VAOs, VBOs, Shaders)
    private void cleanup(){
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vaoTri);
        GL15.glDeleteBuffers(vboTri);
        GL20.glDeleteProgram(programa);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new AppFlappyBird().run();
    }
}
