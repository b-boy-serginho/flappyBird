package com.flappybird.engine;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

/**
 * Encapsula la creación y gestión de la ventana GLFW.
 * Maneja hints, contexto OpenGL, VSync, redimensionamiento con aspect ratio.
 */
public class Window {
    private long handle;
    private final int initialWidth;
    private final int initialHeight;

    public Window(int width, int height, String title) {
        this.initialWidth = width;
        this.initialHeight = height;

        // Arranque de GLFW
        if (!GLFW.glfwInit()) throw new IllegalStateException("No se pudo inicializar GLFW");

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_VISIBLE, GLFW.GLFW_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GLFW.GLFW_TRUE);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MAJOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_CONTEXT_VERSION_MINOR, 3);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_PROFILE, GLFW.GLFW_OPENGL_CORE_PROFILE);
        GLFW.glfwWindowHint(GLFW.GLFW_OPENGL_FORWARD_COMPAT, GLFW.GLFW_TRUE);

        // Crear la ventana
        handle = GLFW.glfwCreateWindow(width, height, title, 0, 0);
        if (handle == 0) throw new RuntimeException("No se pudo crear la ventana");

        // Contexto + VSync + Mostrar
        GLFW.glfwMakeContextCurrent(handle);
        GLFW.glfwSwapInterval(1);
        GLFW.glfwShowWindow(handle);

        // Cargar funciones OpenGL (debe ir antes de cualquier llamada GL)
        GL.createCapabilities();

        // Configurar viewport inicial, se utiliza para definir el área de dibujo.
        GL11.glViewport(0, 0, width, height);

        // Callback de redimensionamiento: actualiza el viewport manteniendo aspect ratio
        GLFW.glfwSetFramebufferSizeCallback(handle, (win, w, h) -> {
            if (w == 0 || h == 0) return; // Minimizado
            float targetAspect = (float) initialWidth / (float) initialHeight;
            float windowAspect = (float) w / (float) h;

            int vpX, vpY, vpW, vpH;
            if (windowAspect > targetAspect) {
                // Ventana mas ancha que el juego: barras laterales (pillarbox)
                vpH = h;
                vpW = (int) (h * targetAspect);
                vpX = (w - vpW) / 2;
                vpY = 0;
            } else {
                // Ventana mas alta que el juego: barras arriba/abajo (letterbox)
                vpW = w;
                vpH = (int) (w / targetAspect);
                vpX = 0;
                vpY = (h - vpH) / 2;
            }
            GL11.glViewport(vpX, vpY, vpW, vpH);
        });
    }

    public long getHandle() { return handle; }

    public boolean shouldClose() {
        return GLFW.glfwWindowShouldClose(handle);
    }

    public void setShouldClose(boolean value) {
        GLFW.glfwSetWindowShouldClose(handle, value);
    }

    public void setTitle(String title) {
        GLFW.glfwSetWindowTitle(handle, title);
    }

    public boolean isKeyPressed(int key) {
        return GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
    }

    public void swapAndPoll() {
         // Doble buffer: hemos dibujado en un buffer "oculto"; ahora lo mostramos en pantalla
         // e intercambiamos. Así el usuario no ve líneas a medias.
        GLFW.glfwSwapBuffers(handle);
        GLFW.glfwPollEvents();
    }

    public void cleanup() {
        GLFW.glfwDestroyWindow(handle);
        GLFW.glfwTerminate();
    }
}
