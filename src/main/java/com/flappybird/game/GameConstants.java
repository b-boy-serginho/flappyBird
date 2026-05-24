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

    public static final float GRAVEDAD = -1.9f;
    public static final float IMPULSO_SALTO = 0.85f;
    public static final float VELOCIDAD_MAX_CAIDA = -1.8f;

    // Parámetros de tuberías (Dificultad inicial)
    // NOTA: Separación entre tuberías = VELOCIDAD × TIEMPO
    //   Ej: 0.62 × 1.5 = 0.93 | 0.40 × 2.3 = 0.92 (misma separación)
    //   Si bajas velocidad, sube el tiempo para que no se peguen.
    public static final float BASE_VELOCIDAD_TUBERIAS = 0.40f; 
    public static final float BASE_TIEMPO_ENTRE_TUBERIAS = 2.30f; 
    public static final float TUBERIA_ANCHO = 0.18f;
    public static final float GAP_ALTO = 0.48f;
    public static final float GAP_MIN_CENTRO = -0.45f;
    public static final float GAP_MAX_CENTRO = 0.45f;

    // Límites de dificultad
    public static final float MAX_VELOCIDAD_TUBERIAS = 0.90f; 
    public static final float MIN_TIEMPO_ENTRE_TUBERIAS = 0.8f;
    public static final float INCREMENTO_VELOCIDAD = 0.04f; 
    public static final float DECREMENTO_TIEMPO = 0.05f;
    public static final int PUNTOS_POR_NIVEL = 10;

    /*
        Variable                         Modo Práctica (Lento)   Modo Equilibrado (Normal)   Modo Arcade (Rápido)
        BASE_VELOCIDAD_TUBERIAS           0.40f                   0.60f                       0.85f
        BASE_TIEMPO_ENTRE_TUBERIAS        2.30f                   1.50f                       1.10f
        MAX_VELOCIDAD_TUBERIAS            0.90f                   1.50f                       2.20f
        INCREMENTO_VELOCIDAD              0.04f                   0.08f                       0.15f
        DECREMENTO_TIEMPO                 0.05f                   0.10f                       0.12f 
    */

     // Física vertical
    /*
        Variable              Flotante (suave)   Estándar (actual)   Pesado (realista)
        GRAVEDAD               -1.2f              -1.9f               -3.0f
        IMPULSO_SALTO           0.60f              0.85f               1.10f
        VELOCIDAD_MAX_CAIDA    -1.2f              -1.8f               -2.5f

        NOTA: GRAVEDAD e IMPULSO deben ajustarse juntos.
        Si subes gravedad (más negativo), sube el impulso para compensar.
        Relación recomendada: IMPULSO ≈ |GRAVEDAD| × 0.45
    */
}
