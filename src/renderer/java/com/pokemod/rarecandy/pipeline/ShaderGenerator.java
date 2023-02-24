package com.pokemod.rarecandy.pipeline;

import com.pokemod.rarecandy.rendering.RareCandy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShaderGenerator {
    private static final Logger LOGGER = LoggerFactory.getLogger("Shader Gen");
    private static final String BASE_VERTEX_SHADER = """
            #version 330 core
            #pragma optionNV(strict on)
                        
            layout(location = 0) in vec3 positions;
            layout(location = 1) in vec2 diffuseCoords;
            layout(location = 2) in vec3 normals;
                        
            %EXPORT_TO_FS%
            
            %INCLUDE%
                        
            uniform mat4 projectionMatrix;
            uniform mat4 viewMatrix;
            uniform mat4 modelMatrix;
            %UNIFORMS%
                        
            void main() {
                mat4 worldSpace = projectionMatrix * viewMatrix;
                vec4 worldPosition = %POSITION%
                %MAIN%
            }
            """;

    private static final String BASE_FRAGMENT_SHADER = """
            #version 330 core
            #pragma optionNV(strict on)
                        
            %IMPORT_FROM_VS%
            out vec4 outColor;
                        
            %UNIFORMS%
                        
            void main() {
                %MAIN%
            }
            """;

    public static RenderPipeline generate(LightingType lightingType, int lightCount, boolean animate, int ambientLight) {
        var vertexShader = BASE_VERTEX_SHADER;
        var fragmentShader = BASE_FRAGMENT_SHADER;
        if(lightCount > 1 && lightingType == LightingType.BASIC_FAST) LOGGER.warn("Tried creating fast lighting shader with > 1 light. Only 1 light is supported");

        switch (lightingType) {
            case PBR -> {

            }
            case BASIC_FAST -> {
                vertexShader = vertexShader.replace("%EXPORT_TO_FS%", """
                        out vec2 outTexCoords;
                        out vec3 toLightVector;
                        out vec3 toCameraVector;
                        out vec3 outNormal;
                        """);

                vertexShader = vertexShader.replace("%UNIFORMS%", """
                        uniform vec3 lightPosition;
                        """);

                vertexShader = vertexShader.replace("%MAIN%", """
                        
                        """);
            }
        }

        if(animate) {
            vertexShader = vertexShader.replace("%INCLUDE%", """
                    mat4 getBoneTransform() {
                        mat4 boneTransform =
                        boneTransforms[uint(joints.x)] * weights.x + // Bone 1 Transform (Bone Transform * Weight)
                        boneTransforms[uint(joints.y)] * weights.y + // Bone 2 Transform (Bone Transform * Weight)
                        boneTransforms[uint(joints.z)] * weights.z + // Bone 3 Transform (Bone Transform * Weight)
                        boneTransforms[uint(joints.w)] * weights.w ; // Bone 4 Transform (Bone Transform * Weight)
                        return boneTransform;
                    }
                    """);
            vertexShader = vertexShader.replace("%POSITION%", "modelMatrix * vec4(positions, 1.0);");
        } else vertexShader = vertexShader.replace("%POSITION%", "modelMatrix * vec4(positions, 1.0);");


        return new RenderPipeline(null, lightingType);
    }

    public record RenderPipeline(ShaderPipeline shaderPipeline, LightingType lightingType) {
    }

    public enum LightingType {
        EMISSIVE,
        BASIC_FAST,
        PBR
    }
}
