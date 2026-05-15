package com.flappybird.ui;

import com.flappybird.engine.Renderer;

/**
 * Display numérico de 7 segmentos para puntaje en pantalla.
 * Dibuja números grandes usando rectángulos como segmentos.
 */
public class SevenSegmentDisplay {
    private final Renderer renderer;

    public SevenSegmentDisplay(Renderer renderer) {
        this.renderer = renderer;
    }

    /** Dibuja un número centrado en (x, y) con el tamaño y color dados. */
    public void drawNumber(int num, float x, float y, float size, float r, float g, float b) {
        String str = String.valueOf(num);
        float cursorX = x - (str.length() * size * 1.5f) / 2.0f; // Centrar

        for (char c : str.toCharArray()) {
            int d = c - '0';
            drawDigit(d, cursorX, y, size, r, g, b);
            cursorX += size * 1.5f;
        }
    }

    /** Dibuja un dígito individual usando 7 segmentos. */
    private void drawDigit(int d, float x, float y, float s, float r, float g, float b) {
        float w = s * 0.8f;
        float t = s * 0.2f; // Grosor
        float h2 = s; // Medio alto

        // Tabla de verdad para display de 7 segmentos (a,b,c,d,e,f,g)
        boolean[] seg = new boolean[7];
        if(d!=1 && d!=4) seg[0]=true; // a (arriba)
        if(d!=5 && d!=6) seg[1]=true; // b (arr-der)
        if(d!=2) seg[2]=true; // c (abj-der)
        if(d!=1 && d!=4 && d!=7) seg[3]=true; // d (abajo)
        if(d==0 || d==2 || d==6 || d==8) seg[4]=true; // e (abj-izq)
        if(d!=1 && d!=2 && d!=3 && d!=7) seg[5]=true; // f (arr-izq)
        if(d!=0 && d!=1 && d!=7) seg[6]=true; // g (centro)

        if(seg[0]) renderer.drawRect(x, y + h2, w, t, r,g,b, 0);
        if(seg[1]) renderer.drawRect(x + w/2, y + h2/2, t, h2, r,g,b, 0);
        if(seg[2]) renderer.drawRect(x + w/2, y - h2/2, t, h2, r,g,b, 0);
        if(seg[3]) renderer.drawRect(x, y - h2, w, t, r,g,b, 0);
        if(seg[4]) renderer.drawRect(x - w/2, y - h2/2, t, h2, r,g,b, 0);
        if(seg[5]) renderer.drawRect(x - w/2, y + h2/2, t, h2, r,g,b, 0);
        if(seg[6]) renderer.drawRect(x, y, w, t, r,g,b, 0);
    }
}
