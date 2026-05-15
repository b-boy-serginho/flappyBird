package com.flappybird.engine;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL30;

/**
 * Primitivas de dibujo 2D reutilizables.
 * Gestiona VAO/VBO para rectángulos y triángulos, y expone métodos
 * de dibujo con color sólido o degradado.
 */
public class Renderer {
    private final ShaderProgram shader;

    // VAO/VBO para rectángulos y triángulos
    private int vao, vbo;
    private int vaoTri, vboTri;

    // Ubicaciones de uniforms
    private int uOffset, uScale, uColor, uColor2, uUseGradient, uRotation;

    public Renderer(ShaderProgram shader) {
        this.shader = shader;
        resolveUniforms();
        createGeometries();
    }

    private void resolveUniforms() {
        uOffset = shader.getUniformLocation("uOffset");
        uScale = shader.getUniformLocation("uScale");
        uColor = shader.getUniformLocation("uColor");
        uColor2 = shader.getUniformLocation("uColor2");
        uUseGradient = shader.getUniformLocation("uUseGradient");
        uRotation = shader.getUniformLocation("uRotation");

        if (uOffset == -1 || uScale == -1 || uColor == -1 || uRotation == -1) {
            throw new RuntimeException("No se pudieron obtener uniforms principales del shader");
        }
    }

    private void createGeometries() {
        // --- CONFIGURACION DEL QUAD (Rectángulo) ---
        float[] quadVerts = {
            -0.5f, -0.5f, 0.0f,
             0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f, -0.5f, 0.0f,
             0.5f,  0.5f, 0.0f,
            -0.5f,  0.5f, 0.0f,
        };

        vao = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vao);
        vbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vbo);
        FloatBuffer buf = BufferUtils.createFloatBuffer(quadVerts.length);
        buf.put(quadVerts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // --- CONFIGURACION DEL TRIANGULO (Pico y Cola) ---
        float[] triVerts = {
            -0.5f, -0.5f, 0.0f,
             0.5f,  0.0f, 0.0f,
            -0.5f,  0.5f, 0.0f
        };

        vaoTri = GL30.glGenVertexArrays();
        GL30.glBindVertexArray(vaoTri);
        vboTri = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, vboTri);
        FloatBuffer bufTri = BufferUtils.createFloatBuffer(triVerts.length);
        bufTri.put(triVerts).flip();
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, bufTri, GL15.GL_STATIC_DRAW);
        GL20.glVertexAttribPointer(0, 3, GL11.GL_FLOAT, false, 3 * Float.BYTES, 0);
        GL20.glEnableVertexAttribArray(0);

        // Desvincular para evitar modificaciones accidentales
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
        GL30.glBindVertexArray(0);
    }

    /** Limpia pantalla y activa el programa de shaders para un nuevo frame. */
    public void beginFrame() {
        GL11.glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
        shader.use();
    }

    // --- Primitivas de dibujo ---

    /** Dibuja un rectángulo con color sólido. */
    public void drawRect(float x, float y, float w, float h,
                         float r, float g, float b, float rotation) {
        drawRectGradient(x, y, w, h, r, g, b, r, g, b, rotation, 0);
    }

    /** Dibuja un rectángulo con degradado vertical. */
    public void drawRectGradient(float x, float y, float w, float h,
                                 float r1, float g1, float b1,
                                 float r2, float g2, float b2,
                                 float rotation, int useGradient) {
        GL30.glBindVertexArray(vao);
        GL20.glUniform2f(uOffset, x, y);
        GL20.glUniform2f(uScale, w, h);
        GL20.glUniform3f(uColor, r1, g1, b1);
        if (uColor2 != -1) GL20.glUniform3f(uColor2, r2, g2, b2);
        if (uUseGradient != -1) GL20.glUniform1i(uUseGradient, useGradient);
        GL20.glUniform1f(uRotation, rotation);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 6);
    }

    /** Dibuja un triángulo con color sólido. */
    public void drawTriangle(float x, float y, float w, float h,
                             float r, float g, float b, float rotation) {
        drawTriangleGradient(x, y, w, h, r, g, b, r, g, b, rotation, 0);
    }

    /** Dibuja un triángulo con degradado vertical. */
    public void drawTriangleGradient(float x, float y, float w, float h,
                                     float r1, float g1, float b1,
                                     float r2, float g2, float b2,
                                     float rotation, int useGradient) {
        GL30.glBindVertexArray(vaoTri);
        GL20.glUniform2f(uOffset, x, y);
        GL20.glUniform2f(uScale, w, h);
        GL20.glUniform3f(uColor, r1, g1, b1);
        if (uColor2 != -1) GL20.glUniform3f(uColor2, r2, g2, b2);
        if (uUseGradient != -1) GL20.glUniform1i(uUseGradient, useGradient);
        GL20.glUniform1f(uRotation, rotation);
        GL11.glDrawArrays(GL11.GL_TRIANGLES, 0, 3);
    }

    /** Libera los recursos OpenGL (VAOs, VBOs). */
    public void cleanup() {
        GL30.glDeleteVertexArrays(vao);
        GL15.glDeleteBuffers(vbo);
        GL30.glDeleteVertexArrays(vaoTri);
        GL15.glDeleteBuffers(vboTri);
    }
}
