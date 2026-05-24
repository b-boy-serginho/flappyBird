package com.flappybird.rendering;

import com.flappybird.engine.Renderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Sistema de partículas para efectos visuales:
 * - Explosión de estrellas al anotar un punto (doradas)
 * - Explosión de humo/fragmentos al morir (rojas)
 * Cada partícula tiene posición, velocidad, vida y color.
 */
public class ParticleSystem {
    private final Renderer renderer;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    /** Representa una partícula individual. */
    private static class Particle {
        float x, y;           // Posición
        float vx, vy;         // Velocidad
        float life;           // Vida restante (1.0 → 0.0)
        float maxLife;        // Vida máxima (para calcular alpha)
        float size;           // Tamaño del cuadrado
        float r, g, b;        // Color

        Particle(float x, float y, float vx, float vy, float life, float size, float r, float g, float b) {
            this.x = x; this.y = y;
            this.vx = vx; this.vy = vy;
            this.life = life; this.maxLife = life;
            this.size = size;
            this.r = r; this.g = g; this.b = b;
        }
    }

    public ParticleSystem(Renderer renderer) {
        this.renderer = renderer;
    }

    /**
     * Emite partículas de puntuación (estrellas doradas que suben).
     * Se llama cuando un jugador pasa una tubería.
     */
    public void emitirPuntaje(float x, float y) {
        for (int i = 0; i < 8; i++) {
            float angulo = (float) (Math.PI * 2.0 * i / 8);
            float velocidad = 0.3f + random.nextFloat() * 0.4f;
            float vx = (float) Math.cos(angulo) * velocidad;
            float vy = (float) Math.sin(angulo) * velocidad + 0.2f; // Bias hacia arriba
            float vida = 0.4f + random.nextFloat() * 0.3f;
            float size = 0.008f + random.nextFloat() * 0.008f;
            // Colores dorados/amarillos
            float r = 0.9f + random.nextFloat() * 0.1f;
            float g = 0.7f + random.nextFloat() * 0.3f;
            float b = 0.0f + random.nextFloat() * 0.2f;
            particles.add(new Particle(x, y, vx, vy, vida, size, r, g, b));
        }
    }

    /**
     * Emite partículas de muerte (fragmentos rojos que explotan).
     * Se llama cuando un pájaro muere.
     */
    public void emitirMuerte(float x, float y) {
        for (int i = 0; i < 15; i++) {
            float angulo = random.nextFloat() * (float) (Math.PI * 2.0);
            float velocidad = 0.2f + random.nextFloat() * 0.6f;
            float vx = (float) Math.cos(angulo) * velocidad;
            float vy = (float) Math.sin(angulo) * velocidad;
            float vida = 0.5f + random.nextFloat() * 0.5f;
            float size = 0.01f + random.nextFloat() * 0.012f;
            // Colores rojos/naranjas
            float r = 0.8f + random.nextFloat() * 0.2f;
            float g = 0.1f + random.nextFloat() * 0.3f;
            float b = 0.0f + random.nextFloat() * 0.1f;
            particles.add(new Particle(x, y, vx, vy, vida, size, r, g, b));
        }
    }

    /** Actualiza posición y vida de todas las partículas. */
    public void actualizar(float dt) {
        Iterator<Particle> it = particles.iterator();
        while (it.hasNext()) {
            Particle p = it.next();
            p.x += p.vx * dt;
            p.y += p.vy * dt;
            p.vy -= 1.5f * dt; // Gravedad ligera
            p.life -= dt;
            p.size *= 0.995f; // Encoger gradualmente

            if (p.life <= 0) {
                it.remove();
            }
        }
    }

    /** Dibuja todas las partículas activas con fade-out. */
    public void render() {
        for (Particle p : particles) {
            // Fade-out basado en vida restante
            float alpha = p.life / p.maxLife;
            renderer.drawRect(p.x, p.y, p.size, p.size,
                    p.r * alpha, p.g * alpha, p.b * alpha, 0.0f);
        }
    }

    /** Retorna true si hay partículas activas. */
    public boolean tieneParticulas() {
        return !particles.isEmpty();
    }
}
