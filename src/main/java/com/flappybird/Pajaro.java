package com.flappybird;

 // Clase para representar a cada jugador/pajaro
public class Pajaro {
     float y;
        float velY;
        float alaTimer;
        int puntaje;
        boolean gameOver;
        boolean started;
        boolean prevJump;
        int jumpKey;
        float[] color; // RGB
        String nombre;

        Pajaro(int jumpKey, float[] color, String nombre) {
            this.jumpKey = jumpKey;
            this.color = color;
            this.nombre = nombre;
            reset();
        }

        void reset() {
            y = 0.0f;
            velY = 0.0f;
            alaTimer = 0.0f;
            puntaje = 0;
            gameOver = false;
            started = false;
        }
}
