package com.flappybird.game;

import static com.flappybird.game.GameConstants.*;

/**
 * Modelo de una tubería (obstáculo).
 * Cada tubería tiene una posición horizontal y un centro de gap vertical.
 * Incluye detección de colisión AABB con un pájaro.
 */
public class Tuberia {
    private float x;
    private final float gapCentroY;
    private boolean puntuadaP1; // Puntuada por Jugador 1
    private boolean puntuadaP2; // Puntuada por Jugador 2

    public Tuberia(float x, float gapCentroY) {
        this.x = x;
        this.gapCentroY = gapCentroY;
    }

    /** Mueve la tubería horizontalmente según la velocidad y delta time. */
    public void mover(float velocidad, float dt) {
        x -= velocidad * dt;
    }

    /** Retorna true si la tubería ya salió por la izquierda de la pantalla. */
    public boolean fueraDePantalla() {
        return x + (TUBERIA_ANCHO * 0.5f) < -1.3f;
    }

    /**
     * Colisión AABB simplificada.
     * 1. Si no hay overlap horizontal, no colisiona.
     * 2. Si hay overlap horizontal, colisiona si el pájaro está fuera del gap.
     */
    public boolean colisionaCon(Pajaro p) {
        float birdLeft   = BIRD_X - (BIRD_ANCHO * 0.5f);
        float birdRight  = BIRD_X + (BIRD_ANCHO * 0.5f);
        float birdBottom = p.getY() - (BIRD_ALTO * 0.5f);
        float birdTop    = p.getY() + (BIRD_ALTO * 0.5f);
        float pipeLeft   = x - (TUBERIA_ANCHO * 0.5f);
        float pipeRight  = x + (TUBERIA_ANCHO * 0.5f);

        boolean overlapX = birdRight > pipeLeft && birdLeft < pipeRight;
        if (!overlapX) return false;

        float gapTop    = gapCentroY + (GAP_ALTO * 0.5f);
        float gapBottom = gapCentroY - (GAP_ALTO * 0.5f);
        return birdTop > gapTop || birdBottom < gapBottom;
    }

    /**
     * Intenta puntuar para un jugador dado.
     * @param jugador 1 o 2
     * @return true si la puntuación fue otorgada (primera vez que pasa).
     */
    public boolean intentarPuntuar(int jugador) {
        boolean superada = x + (TUBERIA_ANCHO * 0.5f) < BIRD_X;
        if (!superada) return false;

        if (jugador == 1 && !puntuadaP1) {
            puntuadaP1 = true;
            return true;
        }
        if (jugador == 2 && !puntuadaP2) {
            puntuadaP2 = true;
            return true;
        }
        return false;
    }

    // --- Getters ---
    public float getX() { return x; }
    public float getGapCentroY() { return gapCentroY; }
}
