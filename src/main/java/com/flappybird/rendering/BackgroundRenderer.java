package com.flappybird.rendering;

import com.flappybird.engine.Renderer;
import com.flappybird.game.GameState;

/**
 * Renderizado del fondo parallax con 4 capas:
 * 1. Cielo con degradado vertical
 * 2. Nubes animadas (15% de velocidad — capa más lejana)
 * 3. Montañas con degradado (30% de velocidad)
 * 4. Suelo con cuadrícula alternada (100% de velocidad)
 */
public class BackgroundRenderer {
    private final Renderer renderer;

    // Posiciones predefinidas de nubes (variedad sin usar Random en render)
    private static final float[][] NUBES = {
        // { offsetX, y, anchoBase, altoBase }
        { 0.0f,  0.65f, 0.35f, 0.08f },
        { 0.9f,  0.50f, 0.25f, 0.06f },
        { 1.8f,  0.72f, 0.40f, 0.09f },
        { 2.5f,  0.55f, 0.30f, 0.07f },
        { 3.3f,  0.68f, 0.20f, 0.05f },
        { 4.0f,  0.60f, 0.38f, 0.08f },
    };

    public BackgroundRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    public void render(GameState state) {
        float bgOffset = state.getBgOffset();

        // 1. Cielo (Degradado estático de azul oscuro a celeste)
        renderer.drawRectGradient(0.0f, 0.0f, 2.0f, 2.0f,
                0.05f, 0.2f, 0.5f, 0.5f, 0.8f, 0.95f, 0.0f, 1);

        // 2. Nubes (Se mueven muy lento: 15% de la velocidad — capa más lejana)
        float cloudOffset = (bgOffset * 0.15f) % 4.5f;
        for (float[] nube : NUBES) {
            float nx = nube[0] - cloudOffset;
            float ny = nube[1];
            float nw = nube[2];
            float nh = nube[3];

            // Wrap: si la nube sale por la izquierda, reaparece por la derecha
            if (nx + nw < -1.3f) nx += 4.5f;
            if (nx - nw > 1.3f) continue; // Fuera de pantalla

            // Cada nube se compone de 3 óvalos superpuestos para dar forma orgánica
            // Óvalo central (más grande)
            renderer.drawRect(nx, ny, nw, nh, 0.9f, 0.93f, 0.97f, 0.0f);
            // Óvalo izquierdo (más pequeño y bajo)
            renderer.drawRect(nx - nw * 0.3f, ny - nh * 0.15f, nw * 0.5f, nh * 0.7f,
                    0.88f, 0.91f, 0.96f, 0.0f);
            // Óvalo derecho (más pequeño y bajo)
            renderer.drawRect(nx + nw * 0.28f, ny - nh * 0.1f, nw * 0.45f, nh * 0.75f,
                    0.88f, 0.91f, 0.96f, 0.0f);
        }

        // 3. Montañas (Se mueven lento: 30% de la velocidad)
        float mntOffset = (bgOffset * 0.3f) % 0.8f;
        for (int i = -3; i <= 3; i++) {
            renderer.drawTriangleGradient(i * 0.8f - mntOffset, -0.6f, 1.0f, 0.8f,
                    0.1f, 0.4f, 0.2f, 0.05f, 0.2f, 0.1f, 0.0f, 1);
        }

        // 4. Suelo (Se mueve a velocidad normal — capa más cercana)
        float groundOffset = bgOffset % 0.2f;
        for (int i = -12; i <= 12; i++) {
            float gR = (i % 2 == 0) ? 0.8f : 0.6f;
            float gG = (i % 2 == 0) ? 0.7f : 0.5f;
            renderer.drawRect(i * 0.2f - groundOffset, -0.9f, 0.2f, 0.2f, gR, gG, 0.2f, 0.0f);
        }
    }
}
