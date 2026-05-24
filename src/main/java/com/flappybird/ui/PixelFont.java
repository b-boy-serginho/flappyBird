package com.flappybird.ui;

import com.flappybird.engine.Renderer;

/**
 * Sistema de fuente pixel bitmap (5x7).
 * Cada carácter se define como un array de 7 filas de 5 bits.
 */
public class PixelFont {
    private final Renderer renderer;

    private static final java.util.Map<Character, int[]> GLYPHS = new java.util.HashMap<>();
    static {
        GLYPHS.put('A', new int[]{0b01110,0b10001,0b10001,0b11111,0b10001,0b10001,0b10001});
        GLYPHS.put('B', new int[]{0b11110,0b10001,0b10001,0b11110,0b10001,0b10001,0b11110});
        GLYPHS.put('C', new int[]{0b01110,0b10001,0b10000,0b10000,0b10000,0b10001,0b01110});
        GLYPHS.put('D', new int[]{0b11100,0b10010,0b10001,0b10001,0b10001,0b10010,0b11100});
        GLYPHS.put('E', new int[]{0b11111,0b10000,0b10000,0b11110,0b10000,0b10000,0b11111});
        GLYPHS.put('F', new int[]{0b11111,0b10000,0b10000,0b11110,0b10000,0b10000,0b10000});
        GLYPHS.put('G', new int[]{0b01110,0b10001,0b10000,0b10111,0b10001,0b10001,0b01110});
        GLYPHS.put('H', new int[]{0b10001,0b10001,0b10001,0b11111,0b10001,0b10001,0b10001});
        GLYPHS.put('I', new int[]{0b01110,0b00100,0b00100,0b00100,0b00100,0b00100,0b01110});
        GLYPHS.put('J', new int[]{0b00111,0b00010,0b00010,0b00010,0b00010,0b10010,0b01100});
        GLYPHS.put('K', new int[]{0b10001,0b10010,0b10100,0b11000,0b10100,0b10010,0b10001});
        GLYPHS.put('L', new int[]{0b10000,0b10000,0b10000,0b10000,0b10000,0b10000,0b11111});
        GLYPHS.put('M', new int[]{0b10001,0b11011,0b10101,0b10101,0b10001,0b10001,0b10001});
        GLYPHS.put('N', new int[]{0b10001,0b11001,0b10101,0b10011,0b10001,0b10001,0b10001});
        GLYPHS.put('O', new int[]{0b01110,0b10001,0b10001,0b10001,0b10001,0b10001,0b01110});
        GLYPHS.put('P', new int[]{0b11110,0b10001,0b10001,0b11110,0b10000,0b10000,0b10000});
        GLYPHS.put('R', new int[]{0b11110,0b10001,0b10001,0b11110,0b10100,0b10010,0b10001});
        GLYPHS.put('S', new int[]{0b01111,0b10000,0b10000,0b01110,0b00001,0b00001,0b11110});
        GLYPHS.put('T', new int[]{0b11111,0b00100,0b00100,0b00100,0b00100,0b00100,0b00100});
        GLYPHS.put('U', new int[]{0b10001,0b10001,0b10001,0b10001,0b10001,0b10001,0b01110});
        GLYPHS.put('V', new int[]{0b10001,0b10001,0b10001,0b10001,0b01010,0b01010,0b00100});
        GLYPHS.put('W', new int[]{0b10001,0b10001,0b10001,0b10101,0b10101,0b11011,0b10001});
        GLYPHS.put('Y', new int[]{0b10001,0b01010,0b00100,0b00100,0b00100,0b00100,0b00100});
        GLYPHS.put('0', new int[]{0b01110,0b10001,0b10011,0b10101,0b11001,0b10001,0b01110});
        GLYPHS.put('1', new int[]{0b00100,0b01100,0b00100,0b00100,0b00100,0b00100,0b01110});
        GLYPHS.put('2', new int[]{0b01110,0b10001,0b00001,0b00110,0b01000,0b10000,0b11111});
        GLYPHS.put('3', new int[]{0b01110,0b10001,0b00001,0b00110,0b00001,0b10001,0b01110});
        GLYPHS.put('4', new int[]{0b00010,0b00110,0b01010,0b10010,0b11111,0b00010,0b00010});
        GLYPHS.put('5', new int[]{0b11111,0b10000,0b11110,0b00001,0b00001,0b10001,0b01110});
        GLYPHS.put('6', new int[]{0b01110,0b10001,0b10000,0b11110,0b10001,0b10001,0b01110});
        GLYPHS.put('7', new int[]{0b11111,0b00001,0b00010,0b00100,0b01000,0b01000,0b01000});
        GLYPHS.put('8', new int[]{0b01110,0b10001,0b10001,0b01110,0b10001,0b10001,0b01110});
        GLYPHS.put('9', new int[]{0b01110,0b10001,0b10001,0b01111,0b00001,0b10001,0b01110});
        GLYPHS.put(':', new int[]{0b00000,0b00100,0b00100,0b00000,0b00100,0b00100,0b00000});
        GLYPHS.put(' ', new int[]{0,0,0,0,0,0,0});
    }

    public PixelFont(Renderer renderer) {
        this.renderer = renderer;
    }

    /** Dibuja un texto centrado en (cx, cy) usando la fuente pixel bitmap. */
    public void drawText(String texto, float cx, float cy, float pixelSize, float r, float g, float b) {
        float charWidth = pixelSize * 6; // 5 píxeles + 1 espacio
        float totalWidth = texto.length() * charWidth;
        float startX = cx - totalWidth / 2.0f;
        float startY = cy + (pixelSize * 7) / 2.0f;

        for (int i = 0; i < texto.length(); i++) {
            char c = texto.charAt(i);
            int[] glyph = GLYPHS.get(c);
            if (glyph == null) continue;

            float charX = startX + i * charWidth;
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < 5; col++) {
                    if ((glyph[row] & (1 << (4 - col))) != 0) {
                        float px = charX + col * pixelSize + pixelSize / 2.0f;
                        float py = startY - row * pixelSize - pixelSize / 2.0f;
                        renderer.drawRect(px, py, pixelSize, pixelSize, r, g, b, 0.0f);
                    }
                }
            }
        }
    }
}
