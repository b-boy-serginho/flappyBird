package com.flappybird.game;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import static com.flappybird.game.GameConstants.*;

/**
 * Estado global del juego.
 * Gestiona jugadores, tuberías, dificultad progresiva y lógica de actualización.
 */
public class GameState {
    private final Pajaro p1;
    private final Pajaro p2;
    private final Pajaro p3;
    private final List<Tuberia> tuberias = new ArrayList<>();
    private final Random random = new Random();

    private boolean isGameOver;
    private boolean victoria;
    private Pajaro ganador;
    private float timerSpawn;

    // Dificultad progresiva
    private int nivelActual;
    private float currentVelTuberias;
    private float currentTiempoSpawn;
    private float bgOffset; // Desplazamiento para el fondo parallax

    // Eventos del frame actual (para partículas y efectos)
    private final List<float[]> eventosScore = new ArrayList<>();  // {x, y}
    private final List<float[]> eventosMuerte = new ArrayList<>(); // {x, y}

    public GameState(Pajaro p1, Pajaro p2, Pajaro p3) {
        this.p1 = p1;
        this.p2 = p2;
        this.p3 = p3;
        resetGame();
    }

    /** Reinicia el estado de la partida. */
    public void resetGame() {
        p1.reset();
        p2.reset();
        p3.reset();
        isGameOver = false;
        victoria = false;
        ganador = null;
        timerSpawn = 0.0f;
        nivelActual = 1;
        currentVelTuberias = BASE_VELOCIDAD_TUBERIAS;
        currentTiempoSpawn = BASE_TIEMPO_ENTRE_TUBERIAS;
        bgOffset = 0.0f;
        tuberias.clear();
    }

    /**
     * Actualización de lógica por frame.
     * - Dificultad progresiva basada en puntaje
     * - Física y colisiones para cada pájaro
     * - Spawn y movimiento de tuberías compartido
     */
    public void actualizar(float dt) {
        boolean algunPajaroActivo = p1.isStarted() || p2.isStarted() || p3.isStarted();
        if (!algunPajaroActivo || isGameOver || victoria) return;

        // --- VERIFICAR VICTORIA (10 puntos) ---
        final int PUNTAJE_VICTORIA = 10;
        if (p1.getPuntaje() >= PUNTAJE_VICTORIA) {
            victoria = true;
            ganador = p1;
            return;
        }
        if (p2.getPuntaje() >= PUNTAJE_VICTORIA) {
            victoria = true;
            ganador = p2;
            return;
        }
        if (p3.getPuntaje() >= PUNTAJE_VICTORIA) {
            victoria = true;
            ganador = p3;
            return;
        }

        // --- CALCULO DE DIFICULTAD PROGRESIVA ---
        int maxPuntaje1 = Math.max(p1.getPuntaje(), p2.getPuntaje());
        int maxPuntaje2 = Math.max(maxPuntaje1, p3.getPuntaje());
        int nuevoNivel = (maxPuntaje2 / PUNTOS_POR_NIVEL) + 1;

        if (nuevoNivel > nivelActual) {
            nivelActual = nuevoNivel;
            currentVelTuberias = Math.min(MAX_VELOCIDAD_TUBERIAS,
                    BASE_VELOCIDAD_TUBERIAS + (nivelActual - 1) * INCREMENTO_VELOCIDAD);
            currentTiempoSpawn = Math.max(MIN_TIEMPO_ENTRE_TUBERIAS,
                    BASE_TIEMPO_ENTRE_TUBERIAS - (nivelActual - 1) * DECREMENTO_TIEMPO);
        }

        // --- ACTUALIZAR JUGADORES ---
        actualizarPajaro(p1, dt);
        actualizarPajaro(p2, dt);
        actualizarPajaro(p3, dt);

        // Si ambos perdieron, marcar fin de partida global
        if (p1.isGameOver() && p2.isGameOver() && p3.isGameOver() && !isGameOver) {
            isGameOver = true;
            return;
        }

        // --- LOGICA DE TUBERIAS (Solo si alguien sigue vivo) ---
        if (!isGameOver) {
            bgOffset += currentVelTuberias * dt; // Animar fondo parallax

            timerSpawn += dt;
            if (timerSpawn >= currentTiempoSpawn) {
                timerSpawn = 0.0f;
                spawnTuberia();
            }

            Iterator<Tuberia> it = tuberias.iterator();
            while (it.hasNext()) {
                Tuberia t = it.next();
                t.mover(currentVelTuberias, dt);

                // Puntuar para cada pájaro si pasan la tubería
                if (!p1.isGameOver() && t.intentarPuntuar(1, p1)) {
                    p1.incrementarPuntaje();
                    eventosScore.add(new float[]{ p1.getDistanciaPajaros(), p1.getY() });
                }
                if (!p2.isGameOver() && t.intentarPuntuar(2, p2)) {
                    p2.incrementarPuntaje();
                    eventosScore.add(new float[]{ p2.getDistanciaPajaros(), p2.getY() });
                }
                if (!p3.isGameOver() && t.intentarPuntuar(3, p3)) {
                    p3.incrementarPuntaje();
                    eventosScore.add(new float[]{ p3.getDistanciaPajaros(), p3.getY() });
                }

                if (t.fueraDePantalla()) it.remove();
            }
        }
    }

    /** Lógica individual de física y colisión para un pájaro. */
    private void actualizarPajaro(Pajaro p, float dt) {
        boolean estabaVivo = !p.isGameOver();
        p.actualizarFisica(dt);

        // Detectar muerte por bordes (actualizarFisica puede matar al pájaro)
        if (estabaVivo && p.isGameOver()) {
            eventosMuerte.add(new float[]{ p.getDistanciaPajaros(), p.getY() });
            return;
        }
        if (p.isGameOver()) return;

        // Colisión con tuberías
        for (Tuberia t : tuberias) {
            if (t.colisionaCon(p)) {
                p.morir();
                eventosMuerte.add(new float[]{ p.getDistanciaPajaros(), p.getY() });
                return;
            }
        }
    }

    /** Crear tubería nueva en borde derecho con gap vertical aleatorio. */
    private void spawnTuberia() {
        float gapCentro = GAP_MIN_CENTRO + random.nextFloat() * (GAP_MAX_CENTRO - GAP_MIN_CENTRO);
        tuberias.add(new Tuberia(1.2f, gapCentro));
    }

    /** Genera el texto del título de la ventana con puntaje y estado. */
    public String generarTitulo() {
        String titulo = String.format("Flappy Bird MULTI | Nivel: %d | J1: %d | J2: %d | J3: %d",
                nivelActual, p1.getPuntaje(), p2.getPuntaje(), p3.getPuntaje());

        if (isGameOver) {
            titulo += " | GAME OVER - Pulsa R para reiniciar o ESC para salir";
        } else if (!p1.isStarted() && !p2.isStarted()) {
            titulo += " | Pulsa W y FLECHA ARRIBA para iniciar";
        }
        return titulo;
    }

    // --- Getters ---
    public Pajaro getP1() { return p1; }
    public Pajaro getP2() { return p2; }
    public Pajaro getP3() { return p3; }
    public List<Tuberia> getTuberias() { return tuberias; }
    public boolean isGameOver() { return isGameOver; }
    public boolean isVictoria() { return victoria; }
    public Pajaro getGanador() { return ganador; }
    public int getNivelActual() { return nivelActual; }
    public float getBgOffset() { return bgOffset; }
    public float getCurrentVelTuberias() { return currentVelTuberias; }

    /** Eventos de puntuación del frame actual. Consumir después de procesar. */
    public List<float[]> getEventosScore() { return eventosScore; }

    /** Eventos de muerte del frame actual. Consumir después de procesar. */
    public List<float[]> getEventosMuerte() { return eventosMuerte; }

    /** Limpia los eventos del frame. Llamar después de procesarlos. */
    public void limpiarEventos() {
        eventosScore.clear();
        eventosMuerte.clear();
    }
}
