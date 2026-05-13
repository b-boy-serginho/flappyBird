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

    // Parametros de tuberias
    private static final float TUBERIA_ANCHO = 0.18f;
    private static final float GAP_ALTO = 0.48f;
    private static final float VELOCIDAD_TUBERIAS = 0.62f;
    private static final float TIEMPO_ENTRE_TUBERIAS = 1.5f;
    private static final float GAP_MIN_CENTRO = -0.45f;
    private static final float GAP_MAX_CENTRO = 0.45f;

    // Recursos OPENGL basicos
    private long window;
    private int programa;
    private int vao;
    private int vbo;

    // Uniforms de transformacion y color
    private int uOffsetLocation;
    private int uScaleLocation;
    private int uColorLocation;

    // Estado del jugador / juego
    private float birdY;
    private float birdVelY;
    private float timerSpawn;
    private int puntaje;

    private boolean started;
    private boolean gameOver;
    private boolean prevSpace;
    private boolean prevR;

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
        boolean puntuada;

        Tuberia (float x, float gapCentroY){
            this.x = x;
            this.gapCentroY = gapCentroY;
        }
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
        window = GLFW.glfwCreateWindow(ANCHO, ALTO, "Flappy Bird OPENGL", 0, 0);

        if (window == 0) throw new RuntimeException("No se pudo crear la ventana");

        // Contexto + VSync + Mostrar
        GLFW.glfwMakeContextCurrent(window);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(window);

        // Cargar Funciones OPENGL
        GL.createCapabilities();

        // Crear pipeline y quad unitario reutilizable
        crearShaders();
        crearQuadBase();
    }

    /* Crea shaders 2D
    - Vertex: transforma quad base con escala y offset
    - Fragment: Color uniforme */
    private void crearShaders(){
        String vertexSrc = """
                #version 330 core
                layaut(location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform vec2 uScale;
                void main(){
                    vec2 finalPos = aPos.xy * uScale + uOffset;
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                }
                """;

        //Color solido por Objetos
        String fragmentSrc = """
                #version 330 core
                uniform vec3 uColor;
                out vec4 fragColor;
                void main(){
                    fragColor = vec4(uColor, 1,0);
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

        if (GL20.glGetProgram(programa, GL20.GL_LINK_STATUS) == GL11.GL_FALSE){
            throw new RuntimeException("Error al lanzar: " + GL20.glGetProgramInfoLog(programa));
        }

        // Resolver uniforms
        uOffsetLocation = GL20.glGetUniformLocation(programa, "uOffset");
        uScaleLocation = GL20.glGetUniformLocation(programa, "uScale");
        uColorLocation = GL20.glGetUniformLocation(programa, "uColor");

        if (uOffsetLocation == -1 || uScaleLocation == -1 || uColorLocation == -1){
            throw new RuntimeException("No se pudieron obtener uniforms del shader");
        }

        // Limpiar objetos shader temporales
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    // Verificacion de compilacion GLSL
    private void comprobarShader(int shader, String tipo){
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS == GL11.GL_FALSE)){
            throw new RuntimeException(tipo + "shader: "+GL20.glGetShaderInfoLog(shader));
        }
    }    

    /* Crea un rectangulo unitario centrado en origen
    - Rango x, y de -0.5 a +0.5
    - 2 triangulos (6 vertices) 
    - Culaquier objeto 2D se dibuja escalando y moviendo este quad */
    private void crearQuadBase(){
        float[] vertices = {
            -0.5f, -0.5f, 0.0f, // Inferior izquierdo
             0.5f, -0.5f, 0.0f, // Inferior derecho
             0.5f,  0.5f, 0,0f, // Superior derecho
            -0.5f, -0.5f, 0.0f, // Inferior izquierdo
             0.5f,  0.5f, 0,0f, // Superior derecho
            -0.5f,  0.5f, 0.5f, // Superior izquierdo 
        };

        // VAO
        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);

        // VBO
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);

        // Subida de vertices
        FloatBuffer buffer = BufferUtils.createFloatBuffer(vertices.length);
        buffer.put(vertices).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buffer, GL15.GL_STATIC_DRAW);

        // Atributo de vertices (posicion)
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        //Desvincular todo por ahora
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /* Reinicia el estado de la partida
       Se usa al iniciar app y al reiniciar tras game over */
    private void resetGame(){
        birdY = 0.0f;
        birdVelY = 0.0f;
        timerSpawn = 0.0f;
        puntaje = 0;
        started = false;
        gameOver = false;
        tuberias.clear();
        actualizarTitulo()
    }

    /* Input del jugador
        ESC: Salir
        SPACE: Empezar/Saltar
        R: Reiniciar partida manualmente(solo en game over) */
    private void procesarInput(){
        if (GLFW.glfwGetKey(window, GLFW.GLFW_KEY_ESCAPE) == GLFW.GLFW_PRESS){
            GLFW.glfwSetWindowShouldClose(window, true);
        }

        boolean spaceAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_SPACE) == GLFW.GLFW_PRESS;
        if (spaceAhora && !prevSpace){
            if (gameOver) {
                resetGame();
                started = true;
                birdVelY = IMPULSO_SALTO;
            } else {
                started = true;
                birdVelY = IMPULSO_SALTO;
            }
        }
        prevSpace = spaceAhora;

        // Reiniciar con R 
        boolean rAhora = GLFW.glfwGetKey(window, GLFW.GLFW_KEY_R) == GLFW.GLFW_PRESS;
        if (rAhora && !prevR && gameOver){
            resetGame();
        }
        prevR = rAhora;
    }

    /* Actualizacion de logica por frame (dt en segundos)
       -Fisica vertical
       -Spawn y movimiento de tuberias 
       -Puntaje y colisiones */
    private void actualizar(float dt){
        // Si aun no inicio o ya termino, no avanza la simulacion
        if (!started || gameOver) return;

        // Integracion fisica simple
        birdVelY += GRAVEDAD * dt;
        // Limita velocidad de caida para sensacion jugable estable
        if (birdVelY < VELOCIDAD_MAX_CAIDA){
            birdVelY = VELOCIDAD_MAX_CAIDA;
        }
        birdY += birdVelY * dt;

        // Colision contra techo / suelo NDC
        float birdTop = birdY + (BIRD_ALTO * 0.5f);
        float birdBottom = birdY - (BIRD_ALTO * 0.5f);
        if (birdTop >= 1.0f || birdBottom <= -1.0f){
            gameOver = true;
            actualizarTitulo();
            return;
        }

        Iterator<Tuberia> it = tuberias.iterator();
        while (it.hasNext()){
            Tuberia t = it.next();
            // Avance horizontal de obstaculos
            t.x -= VELOCIDAD_TUBERIAS * dt;

            // Puntuar la tuberia cuando ya quedo atras del pajaro
            if (t.x + (TUBERIA_ANCHO * 0.5f) < BIRD_X && !t.puntuada){
                t.puntuada = true;
                puntaje++;
                actualizarTitulo();
            }

            //Detectar colision con tuberia
            if (colisionaConTuberia(t)){
                gameOver = true;
                actualizarTitulo();
                return;
            }

            // Remover tuberias fuera de pantalla para no acumular memoria
            if (t.x + (TUBERIA_ANCHO * 0.5f) < -1.3f) it.remove();
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
    private boolean colisionaConTuberia(Tuberia t){
        float birdLeft = BIRD_X - (BIRD_ANCHO * 0.5f);
        float birdRight = BIRD_X + (BIRD_ANCHO * 0.5f);
        float birdBottom = birdY - (BIRD_ALTO * 0.5f);
        float birdTop = birdY + (BIRD_ALTO * 0.5f);
        float pipeLeft = t.x - (TUBERIA_ANCHO * 0.5f);
        float pipeRight = t.x + (TUBERIA_ANCHO * 0.5f);
        boolean overlapX = birdRight > pipeLeft && birdLeft < pipeRight;

        if (!overlapX)  return false;
        
        float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
        float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }

    /* Render del frame
       -Fondo
       -Tuberias
       -Pajaro
       -Franja Central en game over  */
    private void render(){
        // Cielo
        GL11.glClearColor(0.52f, 0.80f, 0.92f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        
        // Activar pipeline y malla base
        GL20.glUseProgram(programa);
        GL30.glBindVertexArray(vao);

        for (Tuberia t :  tuberias ){
            // Calcular limites verticales del hueco
            float gapTop = t.gapCentroY + (GAP_ALTO * 0.5f);
            float gapBottom = t.gapCentroY - (GAP_ALTO * 0.5f);

            // Dibujar(Tramo) parte superior de la tuberia
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f){
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                dibujarRectangulo(t.x, yCentroSup, TUBERIA_ANCHO, altoSuperior, 0.18f, 0.70f, 0.25f);
            }

            // Tramo parte inferior de la tuberia
            float altoInferior = gapBottom + 1.0f;
            if(altoInferior > 0.0f){
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                dibujarRectangulo(t.x, yCentroInf, TUBERIA_ANCHO, altoInferior, 0.18f, 0.70f, 0.25f);
            }
        }

        // Dibujar el pajaro
        dibujarRectangulo(BIRD_X, birdY, BIRD_ANCHO, BIRD_ALTO, 0.98f, 0.85f, 0.20f);

        // Overlay simple de game over (Sin texto en frameBuffer)
        if (gameOver) dibujarRectangulo(0.0f, 0.0f, 2.0f, 0.22f, 0.15f, 0.18f, 0.22f);
    }

    // Helper de dibujo parametrico de rectangulos usando el quad unitario
    private void dibujarRectangulo(float x, float y, float ancho, float alto, float r, float g, float b){
        // Traslacion del quad
        GL20.glUniform2f(uOffsetLocation, x, y);
        // Escalado del quad
        GL20.glUniform2f(uScaleLocation, ancho, alto);
        // Color del quad
        GL20.glUniform3f(uColorLocation, r, g, b);
        // Dibujar 2 triangulos
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    // Actualiza el feedback del titulo de la ventana con puntaje y estado
    private void actualizarTitulo(){
        String tituloBase = "Flappy Bird OPENGL | Puntos: "+puntaje;
        if (!started) GLFW.glfwSetWindowTitle(window, tituloBase + " | SPACE para iniciar");
        else if (gameOver) GLFW.glfwSetWindowTitle(window, tituloBase + " | GAME OVER - SPACE o R para reiniciar");
        else GLFW.glfwSetWindowTitle(window, tituloBase);
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

    // Limpieza de recursos OPENGL y GLFW
    private void cleanup(){
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vao);
        GL20.glDeleteProgram(programa);
        GLFW.glfwDestroyWindow(window);
        GLFW.glfwTerminate();
    }

    public static void main(String[] args) {
        new AppFlappyBird().run();
    }
}
