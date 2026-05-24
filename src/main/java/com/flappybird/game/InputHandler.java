package com.flappybird.game;

import org.lwjgl.glfw.GLFW;
import com.flappybird.audio.SoundManager;
import com.flappybird.engine.Window;

/**
 * Procesamiento centralizado de input de teclado.
 * Gestiona salto de ambos jugadores, reinicio y salida.
 */
public class InputHandler {
    private final Window window;
    private boolean prevR;

    public InputHandler(Window window) {
        this.window = window;
    }

    /**
     * Procesa las teclas cada frame.
     * - ESC: Salir
     * - Tecla de salto de cada jugador
     * - R: Reiniciar partida (solo cuando ambos murieron)
     */
    public void procesar(GameState state) {
        // Salir con ESC
        if (window.isKeyPressed(GLFW.GLFW_KEY_ESCAPE)) {
            window.setShouldClose(true);
        }

        // Input de cada jugador
        procesarSalto(state.getP1());
        procesarSalto(state.getP2());
        procesarSalto(state.getP3());

        // Reiniciar con R (solo si el juego terminó para ambos)
        boolean rAhora = window.isKeyPressed(GLFW.GLFW_KEY_R);
        if (rAhora && !prevR && (state.isGameOver() || state.isVictoria())) {
            state.resetGame();
        }
        prevR = rAhora;
    }

    /** Procesa la tecla de salto para un jugador individual. */
    private void procesarSalto(Pajaro p) {
        boolean keyPressed = window.isKeyPressed(p.getJumpKey());
        if (p.intentarSalto(keyPressed)) {
            SoundManager.playJumpSound();
        }
    }
}
