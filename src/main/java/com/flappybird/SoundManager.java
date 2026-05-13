package com.flappybird;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Gestor de sonidos sintetizados generados por código (Procedural Audio).
 * Permite reproducir efectos estilo 8-bit sin necesidad de cargar archivos .wav externos.
 */
public class SoundManager {
    
    private static final int SAMPLE_RATE = 44100;

    /**
     * Reproduce un sonido ascendente corto (clásico salto de plataforma).
     */
    public static void playJumpSound() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                
                int duration = SAMPLE_RATE / 8; // 125ms
                for (int i = 0; i < duration; i++) {
                    // Aumentar la frecuencia rápidamente (Sweep)
                    float freq = 300.0f + (i * 0.15f);
                    double angle = i * 2.0 * Math.PI * freq / SAMPLE_RATE;
                    // Envolvente de volumen (Fade out)
                    double volume = 127.0 * (1.0 - (float)i / duration);
                    
                    buf[0] = (byte) (Math.sin(angle) * volume);
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

    /**
     * Reproduce un sonido agudo y muy corto (tipo moneda / punto).
     */
    public static void playScoreSound() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                
                int duration = SAMPLE_RATE / 10; // 100ms
                for (int i = 0; i < duration; i++) {
                    float freq = 1200.0f;
                    double angle = i * 2.0 * Math.PI * freq / SAMPLE_RATE;
                    double volume = 127.0 * (1.0 - (float)i / duration);
                    
                    buf[0] = (byte) (Math.sin(angle) * volume);
                    sdl.write(buf, 0, 1);
                }
                
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
            }
        }).start();
    }

    /**
     * Reproduce un sonido ruidoso y grave (choque).
     */
    public static void playHitSound() {
        new Thread(() -> {
            try {
                byte[] buf = new byte[1];
                AudioFormat af = new AudioFormat(SAMPLE_RATE, 8, 1, true, false);
                SourceDataLine sdl = AudioSystem.getSourceDataLine(af);
                sdl.open(af);
                sdl.start();
                
                int duration = SAMPLE_RATE / 4; // 250ms
                for (int i = 0; i < duration; i++) {
                    // Descenso grave
                    float freq = 150.0f - (i * 0.005f);
                    double angle = i * 2.0 * Math.PI * freq / SAMPLE_RATE;
                    double volume = 80.0 * (1.0 - (float)i / duration); // Algo menos de volumen para saturación
                    
                    // Convertir a onda cuadrada (saturada) para efecto de "ruido/crunch"
                    double val = Math.sin(angle);
                    buf[0] = (byte) ((val > 0 ? 1 : -1) * volume);
                    
                    sdl.write(buf, 0, 1);
                }
                
                sdl.drain();
                sdl.stop();
                sdl.close();
            } catch (Exception e) {
            }
        }).start();
    }
}
