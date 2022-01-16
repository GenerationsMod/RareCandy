package cf.hydos.engine.rendering;

import cf.hydos.engine.core.Matrix4f;
import cf.hydos.engine.core.Util;
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
    private static final HashMap<String, ShaderResource> s_loadedShaders = new HashMap<String, ShaderResource>();
    protected static Shader instance;
    private final ShaderResource m_resource;
    private final String m_fileName;

    public Shader(String fileName) {
        this.m_fileName = fileName;

        ShaderResource oldResource = s_loadedShaders.get(fileName);

        if (oldResource != null) {
            m_resource = oldResource;
            m_resource.AddReference();
        } else {
            m_resource = new ShaderResource();

            String vertexShaderText = LoadShader(fileName + ".vs");
            String fragmentShaderText = LoadShader(fileName + ".fs");

            AddVertexShader(vertexShaderText);
            AddFragmentShader(fragmentShaderText);

            AddAllAttributes(vertexShaderText);

            CompileShader();

            AddAllUniforms(vertexShaderText);
            AddAllUniforms(fragmentShaderText);

            s_loadedShaders.put(fileName, m_resource);
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
        if (m_resource.RemoveReference() && !m_fileName.isEmpty()) {
            s_loadedShaders.remove(m_fileName);
        }
    }

    public void Bind() {
        glUseProgram(m_resource.GetProgram());
        instance = this;
    }

    public void UpdateUniforms(org.joml.Matrix4f transform, Material material, RenderingEngine renderingEngine) {
        org.joml.Matrix4f worldMatrix = new org.joml.Matrix4f().perspective((float) Math.toRadians(45), (float) Window.GetWidth() / Window.GetHeight(), 0.1f, 1000.0f).lookAt(2.0f, 0.1f, 2.0f, 0.0f, 0.0f, 0.0f, 0.0f, 1.0f, 0.0f);
        org.joml.Matrix4f modelViewProjMatrix = worldMatrix.mul(transform);

        for (int i = 0; i < m_resource.GetUniformNames().size(); i++) {
            String uniformName = m_resource.GetUniformNames().get(i);
            String uniformType = m_resource.GetUniformTypes().get(i);

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

        final String UNIFORM_KEYWORD = "uniform";
        int uniformStartLocation = shaderText.indexOf(UNIFORM_KEYWORD);
        while (uniformStartLocation != -1) {
            if (!(uniformStartLocation != 0 && (Character.isWhitespace(shaderText.charAt(uniformStartLocation - 1)) || shaderText.charAt(uniformStartLocation - 1) == ';') && Character.isWhitespace(shaderText.charAt(uniformStartLocation + UNIFORM_KEYWORD.length())))) {
                uniformStartLocation = shaderText.indexOf(UNIFORM_KEYWORD, uniformStartLocation + UNIFORM_KEYWORD.length());
                continue;
            }

            int begin = uniformStartLocation + UNIFORM_KEYWORD.length() + 1;
            int end = shaderText.indexOf(";", begin);

            String uniformLine = shaderText.substring(begin, end).trim();

            int whiteSpacePos = uniformLine.indexOf(' ');
            String uniformName = uniformLine.substring(whiteSpacePos + 1).trim();
            String uniformType = uniformLine.substring(0, whiteSpacePos).trim();

            m_resource.GetUniformNames().add(uniformName);
            m_resource.GetUniformTypes().add(uniformType);
            Matcher arrayMatcher = Pattern.compile("\\[\\d+\\]").matcher(uniformName);
            if (arrayMatcher.find()) {
                int size = Integer.valueOf(arrayMatcher.group().substring(1, arrayMatcher.group().length() - 1));
                for (int i = 0; i < size; i++)
                    AddUniform(uniformName.replaceAll("\\[\\d+\\]", "[" + i + "]"), uniformType, structs);
            } else AddUniform(uniformName.replaceAll("\\[\\d+\\]", ""), uniformType, structs);

            uniformStartLocation = shaderText.indexOf(UNIFORM_KEYWORD, uniformStartLocation + UNIFORM_KEYWORD.length());
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

        int uniformLocation = glGetUniformLocation(m_resource.GetProgram(), uniformName);

        if (uniformLocation == 0xFFFFFFFF) {
            System.err.println("Error: Could not find uniform: " + uniformName);
            new Exception().printStackTrace();
            System.exit(1);
        }

        m_resource.GetUniforms().put(uniformName, uniformLocation);
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
        glBindAttribLocation(m_resource.GetProgram(), location, attributeName);
    }

    private void CompileShader() {
        glLinkProgram(m_resource.GetProgram());

        if (glGetProgrami(m_resource.GetProgram(), GL_LINK_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(m_resource.GetProgram(), 1024));
            System.exit(1);
        }

        glValidateProgram(m_resource.GetProgram());

        if (glGetProgrami(m_resource.GetProgram(), GL_VALIDATE_STATUS) == 0) {
            System.err.println(glGetProgramInfoLog(m_resource.GetProgram(), 1024));
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

        glAttachShader(m_resource.GetProgram(), shader);
    }

    public void SetUniformi(String uniformName, int value) {
        glUniform1i(m_resource.GetUniforms().get(uniformName), value);
    }

    public void SetUniformf(String uniformName, float value) {
        glUniform1f(m_resource.GetUniforms().get(uniformName), value);
    }

    public void SetUniform(String uniformName, Vector3f value) {
        glUniform3f(m_resource.GetUniforms().get(uniformName), value.x(), value.y(), value.z());
    }

    public void SetUniform(String uniformName, Matrix4f value) {
        glUniformMatrix4fv(m_resource.GetUniforms().get(uniformName), true, Util.CreateFlippedBuffer(value));
    }

    public void SetUniform(String uniformName, org.joml.Matrix4f value) {
        FloatBuffer buffer = BufferUtils.createFloatBuffer(16);
        value.get(buffer);
        glUniformMatrix4fv(m_resource.GetUniforms().get(uniformName), false, buffer);
    }

    private class GLSLStruct {
        public String name;
        public String type;
    }
}
