package com.pixelmongenerations.legacy.inception.rendering.shader;

import com.pixelmongenerations.pixelmonassetutils.scene.material.Material;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.opengl.GL20;
import org.lwjgl.opengl.GL20C;
import org.lwjgl.system.MemoryStack;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class ShaderProgram {
    public static final ShaderProgram POKEMON_SHADER = new ShaderProgram("pokemon");
    public static final ShaderProgram STATIC_SHADER = new ShaderProgram("static");

    private final int id;
    public final Map<String, Uniform> uniforms;

    public ShaderProgram(String shaderName) {
        this.id = GL20C.glCreateProgram();
        this.uniforms = new HashMap<>();

        String vertexShaderText = loadShader(shaderName + "/" + shaderName + ".vs.glsl");
        String fragmentShaderText = loadShader(shaderName + "/" + shaderName + ".fs.glsl");

        addVertShader(vertexShaderText);
        addFragShader(fragmentShaderText);
        addAttribs(vertexShaderText);
        compileShader();

        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer pUniformCount = stack.ints(1);
            GL20C.glGetProgramiv(this.id, GL20C.GL_ACTIVE_UNIFORMS, pUniformCount);
            int uniformCount = pUniformCount.get(0);

            IntBuffer pMaxNameLength = stack.ints(1);
            GL20C.glGetProgramiv(this.id, GL20.GL_ACTIVE_UNIFORM_MAX_LENGTH, pMaxNameLength);

            for (int i = 0; i < uniformCount; i++) {
                IntBuffer pSize = stack.ints(1);
                IntBuffer pType = stack.ints(1);
                String name = GL20C.glGetActiveUniform(this.id, i, pMaxNameLength.get(0), pSize, pType);

                if (name.contains("[")) {
                    name = name.substring(0, name.indexOf('['));
                }

                this.uniforms.put(name, new Uniform(this.id, name, pType.get(0), pSize.get(0)));
            }
        }
    }

    private static String loadShader(String fileName) {
        StringBuilder shaderSource = new StringBuilder();
        BufferedReader shaderReader;
        final String INCLUDE_DIRECTIVE = "#include";

        try {
            shaderReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ShaderProgram.class.getResourceAsStream("/shaders/gfx/" + fileName))));
            String line;

            while ((line = shaderReader.readLine()) != null) {
                if (line.startsWith(INCLUDE_DIRECTIVE)) {
                    shaderSource.append(loadShader(line.substring(INCLUDE_DIRECTIVE.length() + 2, line.length() - 1)));
                } else shaderSource.append(line).append("\n");
            }

            shaderReader.close();
        } catch (Exception e) {
            System.out.println("Failed to load resource /shaders/" + fileName);
            e.printStackTrace();
            System.exit(1);
        }

        return shaderSource.toString();
    }

    public void bind() {
        GL20C.glUseProgram(this.id);
    }

    public void updateUniforms(Matrix4f transform, Material material, Matrix4f projectionMatrix, Matrix4f viewMatrix) {
        for (String uniformName : this.uniforms.keySet()) {
            Uniform uniform = this.uniforms.get(uniformName);

            switch (uniform.type) {
                case GL20.GL_SAMPLER_2D -> {
                    int texSlot = switch (uniformName) {
                        case "diffuse" -> 0;
                        case "normal" -> 1;
                        default -> throw new RuntimeException("Unknown texture type " + uniformName);
                    };

                    material.diffuseTexture.bind(texSlot);
                    uniform.uploadInt(texSlot);
                }

                case GL20.GL_FLOAT_MAT4 -> {
                    if (uniformName.startsWith("MC_")) { // Minecraft Shader params.
                        switch (uniformName.substring("MC_".length())) {
                            case "projection" -> uniform.uploadMat4f(projectionMatrix);
                            case "view" -> uniform.uploadMat4f(viewMatrix);
                            case "model" -> uniform.uploadMat4f(transform);
                            default -> throw new IllegalArgumentException(uniformName.substring("MC_".length()) + " is not a valid component of Minecraft");
                        }
                    }
                }

                case GL20.GL_FLOAT_VEC3 -> {
                    if (uniformName.startsWith("LIGHT_")) { // Lighting Magic
                        switch (uniformName.substring("LIGHT_".length())) {
                            case "pos" -> uniform.uploadVec3f(new Vector3f(0, 0.5f, -2f));
                            case "color" -> uniform.uploadVec3f(new Vector3f(1, 1, 1));
                            default -> throw new IllegalArgumentException(uniformName.substring("LIGHT_".length()) + " is not a valid vec3 component of Lights");
                        }
                    }
                }

                case GL20.GL_FLOAT ->  {
                    if (uniformName.startsWith("LIGHT_")) { // Lighting Magic
                        switch (uniformName.substring("LIGHT_".length())) {
                            case "shineDamper" -> uniform.uploadFloat(60f);
                            case "reflectivity" -> uniform.uploadFloat(0f);
                            default -> throw new IllegalArgumentException(uniformName.substring("LIGHT_".length()) + " is not a valid float component of Lights");
                        }
                    }
                }
            }
        }
    }

    private void addAttribs(String shaderText) {
        final String ATTRIBUTE_KEYWORD = "attribute";
        int attributeStartLocation = shaderText.indexOf(ATTRIBUTE_KEYWORD);
        int attribNumber = 0;
        while (attributeStartLocation != -1) {
            if (!(attributeStartLocation != 0 && (Character.isWhitespace(shaderText.charAt(attributeStartLocation - 1)) || shaderText.charAt(attributeStartLocation - 1) == ';') && Character.isWhitespace(shaderText.charAt(attributeStartLocation + ATTRIBUTE_KEYWORD.length())))) {
                attributeStartLocation = shaderText.indexOf(ATTRIBUTE_KEYWORD, attributeStartLocation + ATTRIBUTE_KEYWORD.length());
                continue;
            }

            int begin = attributeStartLocation + ATTRIBUTE_KEYWORD.length() + 1;
            int end = shaderText.indexOf(";", begin);

            String attributeLine = shaderText.substring(begin, end).trim();
            String attributeName = attributeLine.substring(attributeLine.indexOf(' ') + 1).trim();

            setAttribLocation(attributeName, attribNumber);
            attribNumber++;

            attributeStartLocation = shaderText.indexOf(ATTRIBUTE_KEYWORD, attributeStartLocation + ATTRIBUTE_KEYWORD.length());
        }
    }

    private void addVertShader(String text) {
        addProgram(text, GL20C.GL_VERTEX_SHADER);
    }

    private void addFragShader(String text) {
        addProgram(text, GL20C.GL_FRAGMENT_SHADER);
    }

    private void setAttribLocation(String attributeName, int location) {
        GL20C.glBindAttribLocation(this.id, location, attributeName);
    }

    private void compileShader() {
        GL20C.glLinkProgram(this.id);

        if (GL20C.glGetProgrami(this.id, GL20C.GL_LINK_STATUS) == 0) {
            System.err.println(GL20C.glGetProgramInfoLog(this.id, 1024));
            System.exit(1);
        }

        GL20C.glValidateProgram(this.id);

        if (GL20C.glGetProgrami(this.id, GL20C.GL_VALIDATE_STATUS) == 0) {
            System.err.println(GL20C.glGetProgramInfoLog(this.id, 1024));
            System.exit(1);
        }
    }

    private void addProgram(String text, int type) {
        int shader = GL20C.glCreateShader(type);

        if (shader == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location when adding shader");
            System.exit(1);
        }

        GL20C.glShaderSource(shader, text);
        GL20C.glCompileShader(shader);

        if (GL20C.glGetShaderi(shader, GL20C.GL_COMPILE_STATUS) == 0) {
            System.err.println(GL20C.glGetShaderInfoLog(shader, 1024));
            System.exit(1);
        }

        GL20C.glAttachShader(this.id, shader);
    }
}
