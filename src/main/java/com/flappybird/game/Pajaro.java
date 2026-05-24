package com.flappybird.game;

import static com.flappybird.game.GameConstants.*;
import com.flappybird.audio.SoundManager;

/**
 * Representa a un jugador/pájaro con su estado completo.
 * Encapsula la física vertical, detección de bordes y gestión de puntaje.
 */
public class Pajaro {
    // Estado de posición y movimiento
    private float distanciaPajaros; //Distacia de los 2 pajaros
    private float y;
    private float velY;
    private float alaTimer;

    // Estado del juego
    private int puntaje;
    private boolean gameOver;
    private boolean started;
    private boolean prevJump;

    // Configuración inmutable del jugador
    private final int jumpKey;
    private final float[] color; // RGB
    private final String nombre;

    public Pajaro(float distanciaPajaros, int jumpKey, float[] color, String nombre) {
        this.distanciaPajaros = distanciaPajaros;
        this.jumpKey = jumpKey;
        this.color = color;
        this.nombre = nombre;
        reset();
    }

    public float getDistanciaPajaros() { return distanciaPajaros;}
    /** Reinicia el estado del pájaro a los valores iniciales. */
    public void reset() {
        y = 0.0f;
        velY = 0.0f;
        alaTimer = 0.0f;
        puntaje = 0;
        gameOver = false;
        started = false;
        prevJump = false;
    }

    /**
     * Procesa el intento de salto según el estado de la tecla.
     * @return true si el salto fue efectuado.
     */
    public boolean intentarSalto(boolean keyPressed) {
        boolean jumped = false;
        if (keyPressed && !prevJump && !gameOver) {
            started = true;
            velY = IMPULSO_SALTO;
            alaTimer = 0.0f;
            jumped = true;
        }
        prevJump = keyPressed;
        return jumped;
    }

    /**
     * Actualiza la física del pájaro: gravedad, velocidad, posición.
     * Detecta colisión con techo/suelo.
     */
    public void actualizarFisica(float dt) {
        if (gameOver) return;

        // Animación de aleteo
        alaTimer += dt * 10.0f;

        // Física
        velY += GRAVEDAD * dt;
        if (velY < VELOCIDAD_MAX_CAIDA) velY = VELOCIDAD_MAX_CAIDA;
        y += velY * dt;

        // Colisión con techo/suelo
        float birdTop = y + (BIRD_ALTO * 0.5f);
        float birdBottom = y - (BIRD_ALTO * 0.5f);
        if (birdTop >= 1.0f || birdBottom <= -1.0f) {
            morir();
        }
    }

    /** Marca al pájaro como muerto y reproduce sonido de impacto. */
    public void morir() {
        if (!gameOver) {
            SoundManager.playHitSound();
            gameOver = true;
        }
    }

    /** Incrementa el puntaje y reproduce sonido de puntuación. */
    public void incrementarPuntaje() {
        puntaje++;
        SoundManager.playScoreSound();
    }

    // --- Getters ---
    public float getY() { return y; }
    public float getVelY() { return velY; }
    public float getAlaTimer() { return alaTimer; }
    public int getPuntaje() { return puntaje; }
    public boolean isGameOver() { return gameOver; }
    public boolean isStarted() { return started; }
    public int getJumpKey() { return jumpKey; }
    public float[] getColor() { return color; }
    public String getNombre() { return nombre; }
}
