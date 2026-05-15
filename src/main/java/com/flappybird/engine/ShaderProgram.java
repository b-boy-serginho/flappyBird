package com.flappybird.engine;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

/**
 * Compila, enlaza y gestiona un programa de shaders GLSL.
 * Vertex: transforma quad/tri con escala, offset y rotación. Pasa Y local para degradados.
 * Fragment: color uniforme o degradado vertical interpolado.
 */
public class ShaderProgram {
    private int programId;

    public ShaderProgram() {
        String vertexSrc = """
                #version 330 core
                layout(location = 0) in vec3 aPos;
                uniform vec2 uOffset;
                uniform vec2 uScale;
                uniform float uRotation;
                out float localY;
                void main(){
                    localY = aPos.y; // Coordenada local Y (-0.5 a 0.5) para usar en degradados
                    
                    // Aplicar rotacion 2D en el shader
                    float s = sin(uRotation);
                    float c = cos(uRotation);
                    mat2 rot = mat2(c, s, -s, c);
                    
                    // Escalar primero, luego rotar, luego trasladar
                    vec2 pos = aPos.xy * uScale;
                    vec2 finalPos = rot * pos + uOffset;
                    
                    gl_Position = vec4(finalPos, aPos.z, 1.0);
                }
                """;

        // Color solido por Objetos o Degradado
        String fragmentSrc = """
                #version 330 core
                uniform vec3 uColor;
                uniform vec3 uColor2;
                uniform int uUseGradient;
                in float localY;
                out vec4 fragColor;
                void main(){
                    if (uUseGradient == 1) {
                        // Mapear localY de [-0.5, 0.5] a [0.0, 1.0]
                        float factor = localY + 0.5;
                        // uColor en el tope (factor = 1.0), uColor2 en la base (factor = 0.0)
                        vec3 colorGradient = mix(uColor2, uColor, factor);
                        fragColor = vec4(colorGradient, 1.0);
                    } else {
                        fragColor = vec4(uColor, 1.0);
                    }
                }
                """;

        int vertexShader = compileShader(GL20.GL_VERTEX_SHADER, vertexSrc, "Vertex");
        int fragmentShader = compileShader(GL20.GL_FRAGMENT_SHADER, fragmentSrc, "Fragment");

        // Link del programa
        programId = GL20.glCreateProgram();
        GL20.glAttachShader(programId, vertexShader);
        GL20.glAttachShader(programId, fragmentShader);
        GL20.glLinkProgram(programId);

        if (GL20.glGetProgrami(programId, GL20.GL_LINK_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException("Error al linkar: " + GL20.glGetProgramInfoLog(programId));
        }

        // Limpiar objetos shader temporales
        GL20.glDeleteShader(vertexShader);
        GL20.glDeleteShader(fragmentShader);
    }

    /** Verificacion de compilacion GLSL */
    private int compileShader(int type, String source, String name) {
        int shader = GL20.glCreateShader(type);
        GL20.glShaderSource(shader, source);
        GL20.glCompileShader(shader);
        if (GL20.glGetShaderi(shader, GL20.GL_COMPILE_STATUS) == GL11.GL_FALSE) {
            throw new RuntimeException(name + " shader: " + GL20.glGetShaderInfoLog(shader));
        }
        return shader;
    }

    public void use() {
        GL20.glUseProgram(programId);
    }

    public int getUniformLocation(String name) {
        return GL20.glGetUniformLocation(programId, name);
    }

    public void cleanup() {
        GL20.glDeleteProgram(programId);
    }
}
