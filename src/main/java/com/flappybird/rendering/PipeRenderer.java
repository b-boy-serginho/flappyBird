package com.flappybird.rendering;

import com.flappybird.engine.Renderer;
import com.flappybird.game.GameState;
import com.flappybird.game.Tuberia;

import static com.flappybird.game.GameConstants.*;

/**
 * Renderizado de tuberías (obstáculos) con degradado y bordes.
 * Cada tubería se compone de parte superior, inferior y sus caps.
 */
public class PipeRenderer {
    private final Renderer renderer;

    public PipeRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render(GameState state) {
        for (Tuberia t : state.getTuberias()) {
            float gapTop = t.getGapCentroY() + (GAP_ALTO * 0.5f);
            float gapBottom = t.getGapCentroY() - (GAP_ALTO * 0.5f);
            float tx = t.getX();

            // Tubería superior (con degradado y borde)
            float altoSuperior = 1.0f - gapTop;
            if (altoSuperior > 0.0f) {
                float yCentroSup = gapTop + (altoSuperior * 0.5f);
                renderer.drawRectGradient(tx, yCentroSup, TUBERIA_ANCHO, altoSuperior,
                        0.1f, 0.5f, 0.1f, 0.3f, 0.8f, 0.2f, 0.0f, 1);
                renderer.drawRect(tx, gapTop + 0.02f, TUBERIA_ANCHO * 1.1f, 0.04f,
                        0.4f, 0.9f, 0.3f, 0.0f); // Borde (Cap)
            }

            // Tubería inferior
            float altoInferior = gapBottom + 1.0f;
            if (altoInferior > 0.0f) {
                float yCentroInf = -1.0f + (altoInferior * 0.5f);
                renderer.drawRectGradient(tx, yCentroInf, TUBERIA_ANCHO, altoInferior,
                        0.3f, 0.8f, 0.2f, 0.1f, 0.5f, 0.1f, 0.0f, 1);
                renderer.drawRect(tx, gapBottom - 0.02f, TUBERIA_ANCHO * 1.1f, 0.04f,
                        0.4f, 0.9f, 0.3f, 0.0f); // Borde (Cap)
            }
        }
    }
}
