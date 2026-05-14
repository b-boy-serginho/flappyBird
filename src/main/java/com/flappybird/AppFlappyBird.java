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

public class AppFlappyBird implements InterfaceFlappyBird{

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

    //Constructor
    public AppFlappyBird() {
        // Inicializar jugadores con sus teclas y colores
        p1 = new Pajaro(GLFW.GLFW_KEY_W, new float[] { 0.98f, 0.85f, 0.20f }, "Amarillo");
        p2 = new Pajaro(GLFW.GLFW_KEY_UP, new float[] { 0.20f, 0.85f, 0.98f }, "Cian");
    }

    @Override
    public void actualizar(float dt) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void actualizarLogicaPajaro(Pajaro p, float dt) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void actualizarTitulo() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void cleanup() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public boolean colisionaConTuberia(Pajaro p, Tuberia t) {
        // TODO Auto-generated method stub
        return false;
    }


    @Override
    public void comprobarShader(int shader, String tipo) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void crearGeometrias() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void crearShaders() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarDigito(int d, float x, float y, float s, float r, float g, float b) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarNumero(int num, float cx, float cy, float pixelSize, float r, float g, float b) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarPajaroCompleto(Pajaro p) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarRectangulo(float x, float y, float ancho, float alto, float r, float g, float b,
            float rotation) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarRectanguloDegradado(float x, float y, float ancho, float alto, float r1, float g1, float b1,
            float r2, float g2, float b2, float rotation, int useGradient) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarTextoPixel(String texto, float cx, float cy, float pixelSize, float r, float g, float b) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarTriangulo(float x, float y, float ancho, float alto, float r, float g, float b, float rotation) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void dibujarTrianguloDegradado(float x, float y, float ancho, float alto, float r1, float g1, float b1,
            float r2, float g2, float b2, float rotation, int useGradient) {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void init() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void loop() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void procesarInput() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void render() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void resetGame() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void run() {
        // TODO Auto-generated method stub
        
    }


    @Override
    public void spawnTuberia() {
        // TODO Auto-generated method stub
        
    }

    public static void main(String[] args) {
        new AppFlappyBird().run();
    }
}
