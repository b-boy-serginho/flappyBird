package com.flappybird.game;

/**
 * Constantes globales del juego centralizadas.
 * Agrupa parámetros de ventana, física, tuberías y dificultad.
 */
public final class GameConstants {
    private GameConstants() {} // No instanciable

    // Tamaño inicial de la ventana
    public static final int ANCHO = 900;
    public static final int ALTO = 700;

    // Posición horizontal del pájaro en NDC
    public static final float BIRD_X = -0.45f;

    // Tamaño del pájaro
    public static final float BIRD_ANCHO = 0.10f;
    public static final float BIRD_ALTO = 0.10f;

    // Física vertical
    public static final float GRAVEDAD = -1.9f;
    public static final float IMPULSO_SALTO = 0.85f;
    public static final float VELOCIDAD_MAX_CAIDA = -1.8f;

    // Parámetros de tuberías (Dificultad inicial)
    public static final float BASE_VELOCIDAD_TUBERIAS = 0.62f;
    public static final float BASE_TIEMPO_ENTRE_TUBERIAS = 1.5f;
    public static final float TUBERIA_ANCHO = 0.18f;
    public static final float GAP_ALTO = 0.48f;
    public static final float GAP_MIN_CENTRO = -0.45f;
    public static final float GAP_MAX_CENTRO = 0.45f;

    // Límites de dificultad
    public static final float MAX_VELOCIDAD_TUBERIAS = 1.6f;
    public static final float MIN_TIEMPO_ENTRE_TUBERIAS = 0.8f;
    public static final float INCREMENTO_VELOCIDAD = 0.08f;
    public static final float DECREMENTO_TIEMPO = 0.1f;
    public static final int PUNTOS_POR_NIVEL = 10;
}
