package com.flappybird;

import org.lwjgl.glfw.GLFW;

import com.flappybird.engine.Renderer;
import com.flappybird.engine.ShaderProgram;
import com.flappybird.engine.Window;
import com.flappybird.game.GameState;
import com.flappybird.game.InputHandler;
import com.flappybird.game.Pajaro;
import com.flappybird.rendering.BackgroundRenderer;
import com.flappybird.rendering.BirdRenderer;
import com.flappybird.rendering.ParticleSystem;
import com.flappybird.rendering.PipeRenderer;
import com.flappybird.ui.HudRenderer;

import static com.flappybird.game.GameConstants.*;

/**
 * Punto de entrada y orquestador del juego Flappy Bird multijugador.
 * Crea recursos, ejecuta el game loop y limpia al salir.
 */
public class AppFlappyBird {

    public static void main(String[] args) {
        // --- INICIALIZACIÓN ---
        Window window = new Window(ANCHO, ALTO, "Flappy Bird OPENGL - Multijugador");
        ShaderProgram shader = new ShaderProgram();
        Renderer renderer = new Renderer(shader);

        Pajaro p1 = new Pajaro(GLFW.GLFW_KEY_W, new float[]{0.98f, 0.85f, 0.20f}, "Amarillo");
        Pajaro p2 = new Pajaro(GLFW.GLFW_KEY_UP, new float[]{0.20f, 0.85f, 0.98f}, "Cian");

        // Estado del juego e input
        GameState game = new GameState(p1, p2);
        InputHandler input = new InputHandler(window);

        // Renderers especializados
        BackgroundRenderer bgRenderer = new BackgroundRenderer(renderer);
        PipeRenderer pipeRenderer = new PipeRenderer(renderer);
        BirdRenderer birdRenderer = new BirdRenderer(renderer);
        HudRenderer hudRenderer = new HudRenderer(renderer);
        ParticleSystem particles = new ParticleSystem(renderer);

        // Título inicial
        window.setTitle(game.generarTitulo());

        // --- GAME LOOP ---
        float ultimoTiempo = (float) GLFW.glfwGetTime();
        String ultimoTitulo = "";

        while (!window.shouldClose()) {
            float ahora = (float) GLFW.glfwGetTime();
            float dt = ahora - ultimoTiempo;
            ultimoTiempo = ahora;

            // Input → Lógica → Render
            input.procesar(game);
            game.actualizar(dt);

            // Procesar eventos para partículas
            for (float[] pos : game.getEventosScore()) {
                particles.emitirPuntaje(pos[0], pos[1]);
            }
            for (float[] pos : game.getEventosMuerte()) {
                particles.emitirMuerte(pos[0], pos[1]);
            }
            game.limpiarEventos();
            particles.actualizar(dt);

            // Actualizar título solo cuando cambie
            String nuevoTitulo = game.generarTitulo();
            if (!nuevoTitulo.equals(ultimoTitulo)) {
                window.setTitle(nuevoTitulo);
                ultimoTitulo = nuevoTitulo;
            }

            // Renderizado (orden: fondo → tuberías → pájaros → partículas → HUD)
            renderer.beginFrame();
            bgRenderer.render(game);
            pipeRenderer.render(game);
            birdRenderer.render(p1, game.isGameOver());
            birdRenderer.render(p2, game.isGameOver());
            particles.render();
            hudRenderer.render(game);

            // Presentar frame y leer eventos
            window.swapAndPoll();
        }

        // --- LIMPIEZA ---
        renderer.cleanup();
        shader.cleanup();
        window.cleanup();
    }
}

// Jugadores
        /*
            ╔═══════════════════════════════════════════════════════════════════════╗
            ║                    TABLA DE CONTROLES (GLFW Keys)                    ║
            ╠═══════════════════╦═══════════════════════════════════════════════════╣
            ║  Tecla            ║  Constante GLFW                                  ║
            ╠═══════════════════╬═══════════════════════════════════════════════════╣
            ║  W                ║  GLFW.GLFW_KEY_W                                 ║
            ║  Espacio          ║  GLFW.GLFW_KEY_SPACE                             ║
            ║  Flecha Arriba    ║  GLFW.GLFW_KEY_UP                                ║
            ║  Flecha Abajo     ║  GLFW.GLFW_KEY_DOWN                              ║
            ║  Enter            ║  GLFW.GLFW_KEY_ENTER                             ║
            ║  Shift Izq        ║  GLFW.GLFW_KEY_LEFT_SHIFT                        ║
            ║  A                ║  GLFW.GLFW_KEY_A                                 ║
            ║  Z                ║  GLFW.GLFW_KEY_Z                                 ║
            ╠═══════════════════╩═══════════════════════════════════════════════════╣
            ║                                                                      ║
            ║  COLORES DEL JUGADOR (R, G, B) — Rango: 0.0f a 1.0f                 ║
            ╠══════════════════╦════════════════════════════════════════════════════╣
            ║  Color           ║  Valores RGB                                      ║
            ╠══════════════════╬════════════════════════════════════════════════════╣
            ║  Amarillo (act)  ║  {0.98f, 0.85f, 0.20f}                            ║
            ║  Cian (actual)   ║  {0.20f, 0.85f, 0.98f}                            ║
            ║  Rojo            ║  {0.95f, 0.25f, 0.20f}                            ║
            ║  Verde           ║  {0.20f, 0.90f, 0.30f}                            ║
            ║  Rosa            ║  {0.95f, 0.40f, 0.70f}                            ║
            ║  Naranja         ║  {0.98f, 0.60f, 0.15f}                            ║
            ║  Violeta         ║  {0.65f, 0.30f, 0.95f}                            ║
            ╚══════════════════╩════════════════════════════════════════════════════╝
        */