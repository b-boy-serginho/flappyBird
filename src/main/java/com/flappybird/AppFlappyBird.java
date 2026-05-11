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
    private float tiemerSpawn;
    private int puntaje;

    private boolean started;
    private boolean gameOver;
    private boolean prevScape;
    private boolean prevR;

    // Lista de Obstaculos activos
    private final List<Tuberia> tuberias = new ArrayList<>();

    // RNG para variar la posicion del GAP
    private final Random random = new Random();

    /*  Modelo de una tuberia
        x: Posicion horizontal comun para parte superior / inferior
        gapCentroY: centro vertical del hueco */




























    // public static void main(String[] args) {
    //     System.out.println("Hola mundo!");
    // }
}
