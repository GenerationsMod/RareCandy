package cf.hydos.engine.rendering;

import cf.hydos.engine.rendering.resources.ShaderResource;
import org.joml.Vector3f;
import org.lwjgl.BufferUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL32.GL_GEOMETRY_SHADER;

public class Shader {
    private static final HashMap<String, ShaderResource> s_loadedShaders = new HashMap<>();
    protected static Shader instance;
    private final ShaderResource resource;
    private final String fileName;

    public Shader(String fileName) {
        this.fileName = fileName;

        ShaderResource oldResource = s_loadedShaders.get(fileName);

        if (oldResource != null) {
            resource = oldResource;
            resource.AddReference();
        } else {
            resource = new ShaderResource();

            String vertexShaderText = LoadShader(fileName + ".vs.glsl");
            String fragmentShaderText = LoadShader(fileName + ".fs.glsl");

            AddVertexShader(vertexShaderText);
            AddFragmentShader(fragmentShaderText);

            AddAllAttributes(vertexShaderText);

            CompileShader();

            AddAllUniforms(vertexShaderText);
            AddAllUniforms(fragmentShaderText);

            s_loadedShaders.put(fileName, resource);
        }
    }

    public static Shader AccessShader() {
        return instance;
    }

    private static String LoadShader(String fileName) {
        StringBuilder shaderSource = new StringBuilder();
        BufferedReader shaderReader;
        final String INCLUDE_DIRECTIVE = "#include";

        try {
            shaderReader = new BufferedReader(new InputStreamReader(Shader.class.getResourceAsStream("/shaders/" + fileName)));
            String line;

            while ((line = shaderReader.readLine()) != null) {
                if (line.startsWith(INCLUDE_DIRECTIVE)) {
                    shaderSource.append(LoadShader(line.substring(INCLUDE_DIRECTIVE.length() + 2, line.length() - 1)));
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

    @Override
    protected void finalize() {
        if (resource.RemoveReference() && !fileName.isEmpty()) {
            s_loadedShaders.remove(fileName);
        }
    }

    public void Bind() {
        glUseProgram(resource.GetProgram());
        instance = this;
    }

    public void UpdateUniforms(org.joml.Matrix4f transform, Material material, RenderingEngine renderingEngine) {
        org.joml.Matrix4f worldMatrix = new org.joml.Matrix4f().perspective((float) Math.toRadians(45), (float) Window.GetWidth() / Window.GetHeight(), 0.1f, 1000.0f).lookAt(2.0f, 0.1f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        org.joml.Matrix4f modelViewProjMatrix = worldMatrix.mul(transform);

        for (int i = 0; i < resource.GetUniformNames().size(); i++) {
            String uniformName = resource.GetUniformNames().get(i);
            String uniformType = resource.GetUniformTypes().get(i);

            if (uniformType.equals("sampler2D")) {
                int samplerSlot = renderingEngine.GetSamplerSlot(uniformName);
                material.GetTexture(uniformName).Bind(samplerSlot);
                SetUniformi(uniformName, samplerSlot);
            } else if (uniformName.startsWith("T_")) {
                if (uniformName.equals("T_MVP")) SetUniform(uniformName, modelViewProjMatrix);
                else if (uniformName.equals("T_model")) SetUniform(uniformName, worldMatrix);
                else throw new IllegalArgumentException(uniformName + " is not a valid component of Transform");
            } else if (uniformName.startsWith("R_")) {
                String unprefixedUniformName = uniformName.substring(2);
                if (uniformType.equals("vec3"))
                    SetUniform(uniformName, renderingEngine.GetVector3f(unprefixedUniformName));
                else if (uniformType.equals("float"))
                    SetUniformf(uniformName, renderingEngine.GetFloat(unprefixedUniformName));
                else renderingEngine.UpdateUniformStruct(uniformType);
            } else if (uniformName.startsWith("C_")) {
                if (uniformName.equals("C_eyePos")) SetUniform(uniformName, new Vector3f(0, 0, 0));
                else throw new IllegalArgumentException(uniformName + " is not a valid component of Camera");
            } else {
                if (uniformType.equals("vec3")) SetUniform(uniformName, material.GetVector3f(uniformName));
                else if (uniformType.equals("float")) SetUniformf(uniformName, material.GetFloat(uniformName));
//				else
//					throw new IllegalArgumentException(uniformType + " is not a supported type in Material");
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

            SetAttribLocation(attributeName, attribNumber);
            attribNumber++;

            attributeStartLocation = shaderText.indexOf(ATTRIBUTE_KEYWORD, attributeStartLocation + ATTRIBUTE_KEYWORD.length());
        }
    }

    private HashMap<String, ArrayList<GLSLStruct>> FindUniformStructs(String shaderText) {
        HashMap<String, ArrayList<GLSLStruct>> result = new HashMap<String, ArrayList<GLSLStruct>>();

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
            ArrayList<GLSLStruct> glslStructs = new ArrayList<GLSLStruct>();

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

    private void AddAllUniforms(String shaderText) {
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

            resource.GetUniformNames().add(uniformName);
            resource.GetUniformTypes().add(uniformType);
            Matcher arrayMatcher = Pattern.compile("\\[\\d+\\]").matcher(uniformName);
            if (arrayMatcher.find()) {
                int size = Integer.valueOf(arrayMatcher.group().substring(1, arrayMatcher.group().length() - 1));
                for (int i = 0; i < size; i++)
                    AddUniform(uniformName.replaceAll("\\[\\d+\\]", "[" + i + "]"), uniformType, structs);
            } else AddUniform(uniformName.replaceAll("\\[\\d+\\]", ""), uniformType, structs);

            uniformStartLocation = shaderText.indexOf(UNIFORKEYWORD, uniformStartLocation + UNIFORKEYWORD.length());
        }
    }

    private void AddUniform(String uniformName, String uniformType, HashMap<String, ArrayList<GLSLStruct>> structs) {
        boolean addThis = true;
        ArrayList<GLSLStruct> structComponents = structs.get(uniformType);

        if (structComponents != null) {
            addThis = false;
            for (GLSLStruct struct : structComponents) {
                AddUniform(uniformName + "." + struct.name, struct.type, structs);
            }
        }

        if (!addThis) return;

        int uniformLocation = glGetUniformLocation(resource.GetProgram(), uniformName);

        if (uniformLocation == 0xFFFFFFFF) {
            System.err.println("Error: Could not find uniform: " + uniformName);
            new Exception().printStackTrace();
            System.exit(1);
        }

        resource.GetUniforms().put(uniformName, uniformLocation);
    }

    private void AddVertexShader(String text) {
        AddProgram(text, GL_VERTEX_SHADER);
    }

    private void AddGeometryShader(String text) {
        AddProgram(text, GL_GEOMETRY_SHADER);
    }

    private void AddFragmentShader(String text) {
        AddProgram(text, GL_FRAGMENT_SHADER);
    }

    private void SetAttribLocation(String attributeName, int location) {
        glBindAttribLocation(resource.GetProgram(), location, attributeName);
    }

    private void CompileShader() {
        glLinkProgram(resource.GetProgram());

        if (glGetProgrami(resource.GetProgram(), GL_LINK_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(resource.GetProgram(), 1024));
            System.exit(1);
        }

        glValidateProgram(resource.GetProgram());

        if (glGetProgrami(resource.GetProgram(), GL_VALIDATE_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(resource.GetProgram(), 1024));
            System.exit(1);
        }
    }

    private void AddProgram(String text, int type) {
        int shader = glCreateShader(type);

        if (shader == 0) {
            System.err.println("Shader creation failed: Could not find valid memory location when adding shader");
            System.exit(1);
        }

        glShaderSource(shader, text);
        glCompileShader(shader);

        if (glGetShaderi(shader, GL_COMPILE_STATUS) == 0) {
            System.err.println(glGetShaderInfoLog(shader, 1024));
            System.exit(1);
        }

        glAttachShader(resource.GetProgram(), shader);
    }

    public void SetUniformi(String uniformName, int value) {
        glUniform1i(resource.GetUniforms().get(uniformName), value);
    }

    public void SetUniformf(String uniformName, float value) {
        glUniform1f(resource.GetUniforms().get(uniformName), value);
    }

    public void SetUniform(String uniformName, Vector3f value) {
        glUniform3f(resource.GetUniforms().get(uniformName), value.x(), value.y(), value.z());
    }

    public void SetUniform(String uniformName, org.joml.Matrix4f value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        glUniformMatrix4fv(resource.GetUniforms().get(uniformName), false, buffer);
    }

    private class GLSLStruct {
        public String name;
        public String type;
    }
}
