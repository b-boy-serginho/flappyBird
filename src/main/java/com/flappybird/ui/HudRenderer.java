package com.flappybird.ui;

import com.flappybird.engine.Renderer;
import com.flappybird.game.GameState;
import com.flappybird.game.Pajaro;

import static com.flappybird.game.GameConstants.*;

/**
 * Renderizado del HUD: puntajes, pantalla de Game Over y menú de inicio.
 */
public class HudRenderer {
    private final Renderer renderer;
    private final PixelFont pixelFont;
    private final SevenSegmentDisplay segmentDisplay;

    public HudRenderer(Renderer renderer) {
        this.renderer = renderer;
        this.pixelFont = new PixelFont(renderer);
        this.segmentDisplay = new SevenSegmentDisplay(renderer);
    }

    public void render(GameState state) {
        Pajaro p1 = state.getP1();
        Pajaro p2 = state.getP2();
        Pajaro p3 = state.getP3();

        // --- OVERLAY DE VICTORIA ---
        if (state.isVictoria()) {
            renderer.drawRect(0.0f, 0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f); // Overlay oscuro
            renderer.drawRect(0.0f, 0.05f, 1.4f, 0.75f, 0.08f, 0.18f, 0.08f, 0.0f); // Panel verde

            pixelFont.drawText("VICTORIA", 0.0f, 0.30f, 0.030f, 1.0f, 1.0f, 0.2f);

            // Mostrar el ganador
            Pajaro ganador = state.getGanador();
            float[] cg = ganador.getColor();
            pixelFont.drawText(ganador.getNombre() + " GANA", 0.0f, 0.12f, 0.018f, cg[0], cg[1], cg[2]);
            pixelFont.drawText("PUNTAJE " + ganador.getPuntaje(), 0.0f, -0.02f, 0.015f, 1.0f, 1.0f, 1.0f);

            pixelFont.drawText("PULSA R REINICIAR", 0.0f, -0.18f, 0.012f, 0.5f, 1.0f, 0.5f);
            pixelFont.drawText("PULSA ESC SALIR", 0.0f, -0.28f, 0.012f, 1.0f, 0.5f, 0.5f);

        // --- OVERLAY DE GAME OVER ---
        } else if (state.isGameOver()) {
            renderer.drawRect(0.0f, 0.0f, 2.0f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f); // Overlay oscuro
            renderer.drawRect(0.0f, 0.05f, 1.4f, 0.65f, 0.12f, 0.14f, 0.18f, 0.0f); // Panel

            pixelFont.drawText("GAME OVER", 0.0f, 0.25f, 0.025f, 1.0f, 0.3f, 0.3f);
            pixelFont.drawText("PULSA R REINICIAR", 0.0f, 0.05f, 0.015f, 0.5f, 1.0f, 0.5f);
            pixelFont.drawText("PULSA ESC SALIR", 0.0f, -0.1f, 0.015f, 1.0f, 0.5f, 0.5f);

        } else if (!p1.isStarted() && !p2.isStarted() && !p3.isStarted()) {
            // --- MENU DE INICIO ---
            renderer.drawRect(0.0f, 0.10f, 1.7f, 0.85f, 0.0f, 0.0f, 0.05f, 0.0f);

            pixelFont.drawText("FLAPPY BIRD", 0.0f, 0.45f, 0.022f, 1.0f, 1.0f, 1.0f);
            pixelFont.drawText("PRIMERO A 10 GANA", 0.0f, 0.33f, 0.010f, 1.0f, 1.0f, 0.3f);

            // Jugador 1 (columna izquierda)
            float[] c1 = p1.getColor();
            renderer.drawRect(-0.5f, 0.18f, 0.08f, 0.08f, c1[0], c1[1], c1[2], 0.0f);
            pixelFont.drawText("JUG 1", -0.5f, 0.08f, 0.010f, c1[0], c1[1], c1[2]);
            pixelFont.drawText(" W", -0.5f, -0.02f, 0.009f, 1.0f, 1.0f, 1.0f);

            // Jugador 2 (columna centro)
            float[] c2 = p2.getColor();
            renderer.drawRect(0.0f, 0.18f, 0.08f, 0.08f, c2[0], c2[1], c2[2], 0.0f);
            pixelFont.drawText("JUG 2", 0.0f, 0.08f, 0.010f, c2[0], c2[1], c2[2]);
            pixelFont.drawText("FLECH ARRIBA", 0.0f, -0.02f, 0.009f, 1.0f, 1.0f, 1.0f);

            // Jugador 3 (columna derecha)
            float[] c3 = p3.getColor();
            renderer.drawRect(0.5f, 0.18f, 0.08f, 0.08f, c3[0], c3[1], c3[2], 0.0f);
            pixelFont.drawText("JUG 3", 0.5f, 0.08f, 0.010f, c3[0], c3[1], c3[2]);
            pixelFont.drawText("SPACE", 0.5f, -0.02f, 0.009f, 1.0f, 1.0f, 1.0f);
        }

        // --- PUNTAJE DIGITAL EN PANTALLA ---
        float[] c1 = p1.getColor();
        float[] c2 = p2.getColor();
        float[] c3 = p3.getColor();
        segmentDisplay.drawNumber(p1.getPuntaje(), -0.5f, 0.8f, 0.1f, c1[0], c1[1], c1[2]);
        segmentDisplay.drawNumber(p2.getPuntaje(), 0.0f, 0.8f, 0.1f, c2[0], c2[1], c2[2]);
        segmentDisplay.drawNumber(p3.getPuntaje(), 0.5f, 0.8f, 0.1f, c3[0], c3[1], c3[2]);

        // --- INDICADOR DE NIVEL Y VELOCIDAD (centro superior) ---
        if (p1.isStarted() || p2.isStarted() || p3.isStarted()) {
            int nivel = state.getNivelActual();
            // Texto "NIVEL X" centrado arriba
            String nivelTexto = "NIVEL " + nivel;
            pixelFont.drawText(nivelTexto, 0.0f, 0.92f, 0.008f, 1.0f, 1.0f, 1.0f);

            // Barra de velocidad visual debajo del texto de nivel
            // Progreso: 0% = velocidad base, 100% = velocidad máxima
            float velActual = state.getCurrentVelTuberias();
            float progreso = (velActual - BASE_VELOCIDAD_TUBERIAS) / (MAX_VELOCIDAD_TUBERIAS - BASE_VELOCIDAD_TUBERIAS);
            progreso = Math.max(0.0f, Math.min(1.0f, progreso));

            float barraAncho = 0.3f;
            float barraAlto = 0.012f;
            float barraY = 0.88f;

            // Fondo de la barra (gris oscuro)
            renderer.drawRect(0.0f, barraY, barraAncho, barraAlto, 0.2f, 0.2f, 0.2f, 0.0f);
            // Progreso (verde → amarillo → rojo según dificultad)
            float fillAncho = barraAncho * progreso;
            if (fillAncho > 0.001f) {
                float barR = Math.min(1.0f, progreso * 2.0f);        // 0→1 en la primera mitad
                float barG = Math.min(1.0f, 2.0f - progreso * 2.0f); // 1→0 en la segunda mitad
                float fillX = -barraAncho / 2.0f + fillAncho / 2.0f;
                renderer.drawRect(fillX, barraY, fillAncho, barraAlto, barR, barG, 0.1f, 0.0f);
            }
        }
    }
}
