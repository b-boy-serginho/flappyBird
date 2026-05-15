package com.flappybird.rendering;

import com.flappybird.engine.Renderer;
import com.flappybird.game.GameState;

/**
 * Renderizado del fondo parallax: cielo, montañas y suelo.
 * El desplazamiento depende del estado del juego (bgOffset).
 */
public class BackgroundRenderer {
    private final Renderer renderer;

    public BackgroundRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render(GameState state) {
        float bgOffset = state.getBgOffset();

        // 1. Cielo (Degradado estático de azul oscuro a celeste)
        renderer.drawRectGradient(0.0f, 0.0f, 2.0f, 2.0f,
                0.05f, 0.2f, 0.5f, 0.5f, 0.8f, 0.95f, 0.0f, 1);

        // 2. Montañas (Se mueven lento: 30% de la velocidad)
        float mntOffset = (bgOffset * 0.3f) % 0.8f;
        for (int i = -3; i <= 3; i++) {
            renderer.drawTriangleGradient(i * 0.8f - mntOffset, -0.6f, 1.0f, 0.8f,
                    0.1f, 0.4f, 0.2f, 0.05f, 0.2f, 0.1f, 0.0f, 1);
        }

        // 3. Suelo (Se mueve a velocidad normal)
        float groundOffset = bgOffset % 0.2f;
        for (int i = -12; i <= 12; i++) {
            float gR = (i % 2 == 0) ? 0.8f : 0.6f;
            float gG = (i % 2 == 0) ? 0.7f : 0.5f;
            renderer.drawRect(i * 0.2f - groundOffset, -0.9f, 0.2f, 0.2f, gR, gG, 0.2f, 0.0f);
        }
    }
}
