package com.flappybird.audio;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Gestor de sonidos sintetizados generados por código (Procedural Audio).
 * Permite reproducir efectos estilo 8-bit sin necesidad de cargar archivos .wav externos.
 * Refactorizado para eliminar duplicación usando una interfaz funcional.
 */
public class SoundManager {
    private static final int SAMPLE_RATE = 44100;

    /** Generador de samples para un efecto de sonido. */
    @FunctionalInterface
    interface SampleGenerator {
        byte generate(int sampleIndex, int totalSamples);
    }

    /**
     * Reproduce un sonido en un hilo separado usando el generador proporcionado.
     * @param durationMs Duración del sonido en milisegundos.
     * @param generator Lambda que genera cada sample del sonido.
     */
    private static void playSound(int durationMs, SampleGenerator generator) {
        new Thread(() -> {
            try {
                AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();

                int totalSamples = SAMPLE_RATE * durationMs / 1000;
                byte[] buf = new byte[1];
                for (int i = 0; i < totalSamples; i++) {
                    buf[0] = generator.generate(i, totalSamples);
                    sdl.write(buf, 0, 1);
                }

                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
                // Ignorar fallos de audio si el hardware no está disponible
            }
        }).start();
    }

    /** Reproduce un sonido ascendente corto (clásico salto de plataforma). */
    public static void playJumpSound() {
        playSound(125, (i, total) -> {
            // Aumentar la frecuencia rápidamente (Sweep)
            float freq = 300.0f + (i * 0.15f);
            double angle = i * 2.0 * Math.PI * freq / SAMPLE_RATE;
            // Envolvente de volumen (Fade out)
            double volume = 127.0 * (1.0 - (float) i / total);
            return (byte) (Math.sin(angle) * volume);
        });
    }

    /** Reproduce un sonido agudo y muy corto (tipo moneda / punto). */
    public static void playScoreSound() {
        playSound(100, (i, total) -> {
            double angle = i * 2.0 * Math.PI * 1200.0 / SAMPLE_RATE;
            double volume = 127.0 * (1.0 - (float) i / total);
            return (byte) (Math.sin(angle) * volume);
        });
    }

    /** Reproduce un sonido ruidoso y grave (choque). */
    public static void playHitSound() {
        playSound(250, (i, total) -> {
            // Descenso grave
            float freq = 150.0f - (i * 0.005f);
            double angle = i * 2.0 * Math.PI * freq / SAMPLE_RATE;
            double volume = 80.0 * (1.0 - (float) i / total);
            // Convertir a onda cuadrada (saturada) para efecto de "ruido/crunch"
            double val = Math.sin(angle);
            return (byte) ((val > 0 ? 1 : -1) * volume);
        });
    }
}
