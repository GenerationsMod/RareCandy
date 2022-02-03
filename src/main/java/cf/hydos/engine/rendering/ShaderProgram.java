package cf.hydos.engine.rendering;

import cf.hydos.pixelmonassetutils.scene.material.Material;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL20C;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShaderProgram {
    private final int id;
    private final HashMap<String, Integer> uniforms;
    private final ArrayList<String> uniformNames;
    private final ArrayList<String> uniformTypes;

    public ShaderProgram(String fileName) {
        this.id = GL20C.glCreateProgram();
        this.uniforms = new HashMap<>();
        this.uniformNames = new ArrayList<>();
        this.uniformTypes = new ArrayList<>();

        String vertexShaderText = loadShader(fileName + ".vs.glsl");
        String fragmentShaderText = loadShader(fileName + ".fs.glsl");

        addVertShader(vertexShaderText);
        addFragShader(fragmentShaderText);

        AddAllAttributes(vertexShaderText);

        compileShader();

        loadUniforms(vertexShaderText);
        loadUniforms(fragmentShaderText);
    }

    private static String loadShader(String fileName) {
        StringBuilder shaderSource = new StringBuilder();
        BufferedReader shaderReader;
        final String INCLUDE_DIRECTIVE = "#include";

        try {
            shaderReader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(ShaderProgram.class.getResourceAsStream("/shaders/" + fileName))));
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

    public void updateUniforms(Matrix4f transform, Material material, Matrix4f perspectiveViewMatrix) {
        Matrix4f modelViewProjMatrix = perspectiveViewMatrix.mul(transform);

        for (int i = 0; i < this.uniformNames.size(); i++) {
            String uniformName = this.uniformNames.get(i);
            String uniformType = this.uniformTypes.get(i);

            if (uniformType.equals("sampler2D")) {
                int samplerSlot = switch (uniformName) {
                    case "diffuse" -> 0;
                    case "normal" -> 1;
                    default -> throw new RuntimeException("Unknown texture " + uniformName);
                };
                material.diffuseTexture.bind(samplerSlot);
                setUniformI(uniformName, samplerSlot);
            } else if (uniformName.startsWith("T_")) {
                if (uniformName.equals("T_MVP")) setUniform(uniformName, modelViewProjMatrix);
                else if (uniformName.equals("T_model")) setUniform(uniformName, perspectiveViewMatrix);
                else throw new IllegalArgumentException(uniformName + " is not a valid component of Transform");
            }
        }
    }

    private void AddAllAttributes(String shaderText) {
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

    private HashMap<String, ArrayList<GLSLStruct>> FindUniformStructs(String shaderText) {
        HashMap<String, ArrayList<GLSLStruct>> result = new HashMap<>();

        final String STRUCT_KEYWORD = "struct";
        int structStartLocation = shaderText.indexOf(STRUCT_KEYWORD);
        while (structStartLocation != -1) {
            if (!(structStartLocation != 0 && (Character.isWhitespace(shaderText.charAt(structStartLocation - 1)) || shaderText.charAt(structStartLocation - 1) == ';') && Character.isWhitespace(shaderText.charAt(structStartLocation + STRUCT_KEYWORD.length())))) {
                structStartLocation = shaderText.indexOf(STRUCT_KEYWORD, structStartLocation + STRUCT_KEYWORD.length());
                continue;
            }

            int nameBegin = structStartLocation + STRUCT_KEYWORD.length() + 1;
            int braceBegin = shaderText.indexOf("{", nameBegin);
            int braceEnd = shaderText.indexOf("}", braceBegin);

            String structName = shaderText.substring(nameBegin, braceBegin).trim();
            ArrayList<GLSLStruct> glslStructs = new ArrayList<>();

            int componentSemicolonPos = shaderText.indexOf(";", braceBegin);
            while (componentSemicolonPos != -1 && componentSemicolonPos < braceEnd) {
                int componentNameEnd = componentSemicolonPos + 1;

                while (Character.isWhitespace(shaderText.charAt(componentNameEnd - 1)) || shaderText.charAt(componentNameEnd - 1) == ';')
                    componentNameEnd--;

                int componentNameStart = componentSemicolonPos;

                while (!Character.isWhitespace(shaderText.charAt(componentNameStart - 1))) componentNameStart--;

                int componentTypeEnd = componentNameStart;

                while (Character.isWhitespace(shaderText.charAt(componentTypeEnd - 1))) componentTypeEnd--;

                int componentTypeStart = componentTypeEnd;

                while (!Character.isWhitespace(shaderText.charAt(componentTypeStart - 1))) componentTypeStart--;

                String componentName = shaderText.substring(componentNameStart, componentNameEnd);
                String componentType = shaderText.substring(componentTypeStart, componentTypeEnd);

                GLSLStruct glslStruct = new GLSLStruct();
                glslStruct.name = componentName;
                glslStruct.type = componentType;

                glslStructs.add(glslStruct);

                componentSemicolonPos = shaderText.indexOf(";", componentSemicolonPos + 1);
            }

            result.put(structName, glslStructs);

            structStartLocation = shaderText.indexOf(STRUCT_KEYWORD, structStartLocation + STRUCT_KEYWORD.length());
        }

        return result;
    }

    private void loadUniforms(String shaderText) {
        HashMap<String, ArrayList<GLSLStruct>> structs = FindUniformStructs(shaderText);

        final String UNIFORKEYWORD = "uniform";
        int uniformStartLocation = shaderText.indexOf(UNIFORKEYWORD);
        while (uniformStartLocation != -1) {
            if (!(uniformStartLocation != 0 && (Character.isWhitespace(shaderText.charAt(uniformStartLocation - 1)) || shaderText.charAt(uniformStartLocation - 1) == ';') && Character.isWhitespace(shaderText.charAt(uniformStartLocation + UNIFORKEYWORD.length())))) {
                uniformStartLocation = shaderText.indexOf(UNIFORKEYWORD, uniformStartLocation + UNIFORKEYWORD.length());
                continue;
            }

            int begin = uniformStartLocation + UNIFORKEYWORD.length() + 1;
            int end = shaderText.indexOf(";", begin);

            String uniformLine = shaderText.substring(begin, end).trim();

            int whiteSpacePos = uniformLine.indexOf(' ');
            String uniformName = uniformLine.substring(whiteSpacePos + 1).trim();
            String uniformType = uniformLine.substring(0, whiteSpacePos).trim();

            this.uniformNames.add(uniformName);
            this.uniformTypes.add(uniformType);
            Pattern arrayPattern = Pattern.compile("\\[\\d+]");
            Matcher arrayMatcher = arrayPattern.matcher(uniformName);
            if (arrayMatcher.find()) {
                int size = Integer.parseInt(arrayMatcher.group().substring(1, arrayMatcher.group().length() - 1));
                for (int i = 0; i < size; i++)
                    addUniform(uniformName.replaceAll(arrayPattern.pattern(), "[" + i + "]"), uniformType, structs);
            } else addUniform(uniformName.replaceAll(arrayPattern.pattern(), ""), uniformType, structs);

            uniformStartLocation = shaderText.indexOf(UNIFORKEYWORD, uniformStartLocation + UNIFORKEYWORD.length());
        }
    }

    private void addUniform(String uniformName, String uniformType, HashMap<String, ArrayList<GLSLStruct>> structs) {
        boolean addThis = true;
        ArrayList<GLSLStruct> structComponents = structs.get(uniformType);

        if (structComponents != null) {
            addThis = false;
            for (GLSLStruct struct : structComponents) {
                addUniform(uniformName + "." + struct.name, struct.type, structs);
            }
        }

        if (!addThis) return;

        int uniformLocation = GL20C.glGetUniformLocation(this.id, uniformName);

        if (uniformLocation == 0xFFFFFFFF) {
            System.err.println("Error: Could not find uniform: " + uniformName);
            new Exception().printStackTrace();
            System.exit(1);
        }

        this.uniforms.put(uniformName, uniformLocation);
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

    public void setUniformI(String uniformName, int value) {
        GL20C.glUniform1i(this.uniforms.get(uniformName), value);
    }

    public void setUniform(String uniformName, Matrix4f value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        GL20C.glUniformMatrix4fv(this.uniforms.get(uniformName), false, buffer);
    }

    private static class GLSLStruct {
        public String name;
        public String type;
    }
}
