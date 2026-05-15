package com.flappybird.rendering;

import com.flappybird.engine.Renderer;
import com.flappybird.game.Pajaro;

import static com.flappybird.game.GameConstants.*;

/**
 * Renderizado del pájaro ensamblando sus partes:
 * cola, cuerpo, ala, ojo (esclerótica + pupila) y pico.
 */
public class BirdRenderer {
    private final Renderer renderer;

    public BirdRenderer(Renderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Dibuja un pájaro completo si está activo.
     * @param p Pájaro a dibujar.
     * @param isGlobalGameOver true si la partida terminó para todos.
     */
    public void render(Pajaro p, boolean isGlobalGameOver) {
        // Ocultar temporalmente si murió pero la partida sigue
        if (p.isGameOver() && !isGlobalGameOver) return;

        float r = p.getColor()[0];
        float g = p.getColor()[1];
        float b = p.getColor()[2];

        // Efecto visual de daño (Blink)
        if (p.isGameOver() && isGlobalGameOver) {
            float blink = (float) Math.abs(Math.sin(System.currentTimeMillis() / 100.0));
            r = blink;
            g = blink * 0.2f;
            b = blink * 0.2f; // Flash rojo oscuro
        }

        // Calcular inclinación (pitch) basada en la velocidad vertical del pájaro
        float angulo = Math.max(-0.6f, Math.min(0.4f, p.getVelY() * 0.4f));

        // 1. COLA: Un triángulo del color del pájaro en la parte trasera
        renderer.drawTriangle(BIRD_X - 0.04f, p.getY(), 0.05f, 0.05f,
                r * 0.8f, g * 0.8f, b * 0.8f, angulo + 3.1415f);

        // 2. CUERPO: El rectángulo principal del color del jugador
        renderer.drawRect(BIRD_X, p.getY(), BIRD_ANCHO, BIRD_ALTO, r, g, b, angulo);

        // 3. ALA: Rectángulo blanco que oscila para simular el aleteo
        float oscilacionAla = (float) Math.sin(p.getAlaTimer()) * 0.03f;
        renderer.drawRect(BIRD_X - 0.01f, p.getY() + oscilacionAla, 0.06f, 0.04f,
                0.95f, 0.95f, 0.95f, angulo);

        // 4. OJO: Fondo blanco y pupila negra
        float eyeOffsetX = 0.025f;
        float eyeOffsetY = 0.02f;
        float cosA = (float) Math.cos(angulo);
        float sinA = (float) Math.sin(angulo);
        float rx = eyeOffsetX * cosA - eyeOffsetY * sinA;
        float ry = eyeOffsetX * sinA + eyeOffsetY * cosA;

        renderer.drawRect(BIRD_X + rx, p.getY() + ry, 0.025f, 0.035f,
                1.0f, 1.0f, 1.0f, angulo); // Esclerótica
        renderer.drawRect(BIRD_X + rx + (0.005f * cosA), p.getY() + ry + (0.005f * sinA),
                0.01f, 0.015f, 0.0f, 0.0f, 0.0f, angulo); // Pupila

        // 5. PICO: Un triángulo naranja al frente
        float beakOffsetX = 0.045f;
        float bx = beakOffsetX * cosA;
        float by = beakOffsetX * sinA;
        renderer.drawTriangle(BIRD_X + bx, p.getY() + by, 0.04f, 0.03f,
                1.0f, 0.5f, 0.0f, angulo);
    }
}
