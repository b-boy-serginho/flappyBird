package com.flappybird.rendering;

import com.flappybird.engine.Renderer;
import com.flappybird.game.Pajaro;

import static com.flappybird.game.GameConstants.*;

/**
 * Renderizado del pájaro ensamblando sus 5 partes obligatorias:
 * 1. Cuerpo principal (rectángulo del color del jugador)
 * 2. Pico (triángulo naranja distinguible)
 * 3. Ala visible (rectángulo animado sincronizado con el salto)
 * 4. Cola (triángulo en la parte trasera)
 * 5. Ojo con pupila (esclerótica blanca + pupila negra)
 *
 * Todas las partes rotan coherentemente según la velocidad vertical (pitch).
 * La animación de aleteo se intensifica al saltar y se relaja al caer.
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

        // Efecto visual de daño (Blink rojo)
        if (p.isGameOver() && isGlobalGameOver) {
            float blink = (float) Math.abs(Math.sin(System.currentTimeMillis() / 100.0));
            r = blink;
            g = blink * 0.2f;
            b = blink * 0.2f;
        }

        // --- INCLINACIÓN (pitch) basada en velocidad vertical ---
        // Sube → inclina hacia arriba (positivo), Cae → inclina hacia abajo (negativo)
        float angulo = Math.max(-0.6f, Math.min(0.4f, p.getVelY() * 0.4f));
        float cosA = (float) Math.cos(angulo);
        float sinA = (float) Math.sin(angulo);

        // --- ANIMACIÓN DE ALETEO sincronizada con salto ---
        // alaTimer se resetea a 0 al saltar, generando un aleteo rápido inicial
        // que se desacelera con el tiempo (frecuencia alta al inicio)
        float alaTimer = p.getAlaTimer();
        // Frecuencia alta al inicio (justo después del salto), se estabiliza luego
        float frecuencia = 12.0f + Math.max(0, 8.0f - alaTimer * 4.0f);
        float oscilacionAla = (float) Math.sin(alaTimer * frecuencia) * 0.03f;
        // Amplitud mayor justo después del salto
        float amplitud = 0.03f + Math.max(0, 0.02f - alaTimer * 0.04f);
        oscilacionAla = (float) Math.sin(alaTimer * frecuencia) * amplitud;

        // =============================================
        // 1. COLA — Triángulo trasero (color oscurecido)
        // =============================================
        // La cola se posiciona detrás del cuerpo rotando con el ángulo
        float colaOffsetX = -0.05f;
        float colaOffsetY = 0.0f;
        float colaPx = colaOffsetX * cosA - colaOffsetY * sinA;
        float colaPy = colaOffsetX * sinA + colaOffsetY * cosA;
        renderer.drawTriangle(
                BIRD_X + colaPx, p.getY() + colaPy,
                0.05f, 0.05f,
                r * 0.7f, g * 0.7f, b * 0.7f,
                angulo + 3.1415f); // Apunta hacia atrás

        // =============================================
        // 2. CUERPO PRINCIPAL — Rectángulo del color del jugador
        // =============================================
        renderer.drawRect(BIRD_X, p.getY(), BIRD_ANCHO, BIRD_ALTO, r, g, b, angulo);

        // Detalle: barriga más clara en la parte inferior del cuerpo
        float barrigaOffsetX = 0.0f;
        float barrigaOffsetY = -0.015f;
        float barrigaPx = barrigaOffsetX * cosA - barrigaOffsetY * sinA;
        float barrigaPy = barrigaOffsetX * sinA + barrigaOffsetY * cosA;
        renderer.drawRect(
                BIRD_X + barrigaPx, p.getY() + barrigaPy,
                BIRD_ANCHO * 0.7f, BIRD_ALTO * 0.4f,
                Math.min(1.0f, r * 1.3f), Math.min(1.0f, g * 1.3f), Math.min(1.0f, b * 1.3f),
                angulo);

        // =============================================
        // 3. ALA — Rectángulo animado que rota con el cuerpo
        // =============================================
        // El ala oscila perpendicular al cuerpo (en el eje local Y)
        float alaOffsetX = -0.01f;
        float alaOffsetY = oscilacionAla; // Oscilación sincronizada con salto
        float alaPx = alaOffsetX * cosA - alaOffsetY * sinA;
        float alaPy = alaOffsetX * sinA + alaOffsetY * cosA;
        renderer.drawRect(
                BIRD_X + alaPx, p.getY() + alaPy,
                0.06f, 0.04f,
                0.95f, 0.95f, 0.95f,
                angulo);
        // Detalle interior del ala (línea más oscura)
        renderer.drawRect(
                BIRD_X + alaPx, p.getY() + alaPy,
                0.05f, 0.015f,
                0.8f, 0.8f, 0.8f,
                angulo);

        // =============================================
        // 4. OJO — Esclerótica blanca + pupila negra
        // =============================================
        float eyeOffsetX = 0.025f;
        float eyeOffsetY = 0.02f;
        float eyePx = eyeOffsetX * cosA - eyeOffsetY * sinA;
        float eyePy = eyeOffsetX * sinA + eyeOffsetY * cosA;

        // Esclerótica (fondo blanco del ojo)
        renderer.drawRect(
                BIRD_X + eyePx, p.getY() + eyePy,
                0.028f, 0.038f,
                1.0f, 1.0f, 1.0f,
                angulo);

        // Pupila negra (desplazada ligeramente hacia el frente)
        float pupilaShiftX = 0.005f;
        float pupilaShiftY = 0.0f;
        // Si está cayendo, la pupila mira hacia abajo
        if (p.getVelY() < -0.5f) {
            pupilaShiftY = -0.005f;
        } else if (p.getVelY() > 0.3f) {
            pupilaShiftY = 0.003f; // Si sube, mira ligeramente arriba
        }
        float pupPx = pupilaShiftX * cosA - pupilaShiftY * sinA;
        float pupPy = pupilaShiftX * sinA + pupilaShiftY * cosA;
        renderer.drawRect(
                BIRD_X + eyePx + pupPx, p.getY() + eyePy + pupPy,
                0.012f, 0.018f,
                0.0f, 0.0f, 0.0f,
                angulo);

        // Brillo del ojo (pequeño cuadrado blanco en la pupila)
        float brilloShiftX = 0.008f;
        float brilloShiftY = 0.005f;
        float brilloPx = brilloShiftX * cosA - brilloShiftY * sinA;
        float brilloPy = brilloShiftX * sinA + brilloShiftY * cosA;
        renderer.drawRect(
                BIRD_X + eyePx + brilloPx, p.getY() + eyePy + brilloPy,
                0.005f, 0.005f,
                1.0f, 1.0f, 1.0f,
                angulo);

        // =============================================
        // 5. PICO — Triángulo naranja distinguible
        // =============================================
        float beakOffsetX = 0.048f;
        float beakOffsetY = -0.005f;
        float beakPx = beakOffsetX * cosA - beakOffsetY * sinA;
        float beakPy = beakOffsetX * sinA + beakOffsetY * cosA;
        // Pico superior (naranja)
        renderer.drawTriangle(
                BIRD_X + beakPx, p.getY() + beakPy,
                0.045f, 0.025f,
                1.0f, 0.55f, 0.0f,
                angulo);
        // Pico inferior (naranja oscuro para dar profundidad)
        float beakLowOffsetY = -0.012f;
        float beakLowPx = beakOffsetX * cosA - beakLowOffsetY * sinA;
        float beakLowPy = beakOffsetX * sinA + beakLowOffsetY * cosA;
        renderer.drawTriangle(
                BIRD_X + beakLowPx, p.getY() + beakLowPy,
                0.04f, 0.018f,
                0.9f, 0.4f, 0.0f,
                angulo);
    }
}

 /*
        ╔══════════════════════════════════════════════════════════════════════════════╗
        ║             TABLA DE AJUSTE DE PIEZAS DEL PÁJARO                            ║
        ╠══════════╦════════════════╦═══════════════╦══════════════════════════════════╣
        ║  Parte   ║  OffsetX,Y     ║  Ancho, Alto  ║  Color (R, G, B)                ║
        ╠══════════╬════════════════╬═══════════════╬══════════════════════════════════╣
        ║  Cola    ║  -0.05, 0.0    ║  0.05, 0.05   ║  color × 0.7 (oscurecido)       ║
        ║  Cuerpo  ║  0.0, 0.0      ║  BIRD_ANCHO,  ║  color del jugador              ║
        ║          ║                ║  BIRD_ALTO    ║                                  ║
        ║  Barriga ║  0.0, -0.015   ║  ANCHO×0.7,   ║  color × 1.3 (aclarado)         ║
        ║          ║                ║  ALTO×0.4     ║                                  ║
        ║  Ala     ║  -0.01, oscila ║  0.06, 0.04   ║  0.95, 0.95, 0.95 (blanco)      ║
        ║  Ala det ║  (mismo)       ║  0.05, 0.015  ║  0.80, 0.80, 0.80 (gris)        ║
        ║  Ojo     ║  0.025, 0.02   ║  0.028, 0.038 ║  1.0, 1.0, 1.0 (blanco)         ║
        ║  Pupila  ║  +0.005, var   ║  0.012, 0.018 ║  0.0, 0.0, 0.0 (negro)          ║
        ║  Brillo  ║  +0.008,+0.005 ║  0.005, 0.005 ║  1.0, 1.0, 1.0 (blanco)         ║
        ║  Pico    ║  0.048, -0.005 ║  0.045, 0.025 ║  1.0, 0.55, 0.0 (naranja)       ║
        ║  Pico2   ║  0.048, -0.012 ║  0.04, 0.018  ║  0.9, 0.4, 0.0 (naranja osc)    ║
        ╠══════════╩════════════════╩═══════════════╩══════════════════════════════════╣
        ║  INCLINACIÓN: angulo = clamp(velY × 0.4, -0.6, 0.4)                         ║
        ║  Para más inclinación: subir el multiplicador (ej: 0.6)                      ║
        ║  Para menos: bajarlo (ej: 0.2)                                               ║
        ╠══════════════════════════════════════════════════════════════════════════════╣
        ║  ALETEO: frecuencia = 12 + max(0, 8 - alaTimer×4)                           ║
        ║  Para aleteo más rápido: subir 12 (ej: 16)                                   ║
        ║  Para aleteo más lento: bajar 12 (ej: 8)                                     ║
        ╚══════════════════════════════════════════════════════════════════════════════╝
    */