package com.flappybird;

/*  Modelo de una tuberia
        x: Posicion horizontal comun para parte superior / inferior
        gapCentroY: centro vertical del hueco
        puntuada: Evita sumar 2 veces la misma tuberia */
public class Tuberia {
    float x;
    float gapCentroY;
    boolean puntuadaP1; // Puntuada por Jugador 1
    boolean puntuadaP2; // Puntuada por Jugador 2

    Tuberia(float x, float gapCentroY) {
        this.x = x;
        this.gapCentroY = gapCentroY;
    }
}
