package com.pokemod.rarecandy.shader;

import com.pokemod.rarecandy.shader.tokenizer.Token;
import com.pokemod.rarecandy.shader.tokenizer.TokenHandler;
import com.pokemod.rarecandy.shader.tokenizer.TokenIterator;
import com.pokemod.rarecandy.shader.tokenizer.TokenType;

import java.util.HashMap;
import java.util.Random;

public class RCSLConverter {
    private static final Random RANDOM = new Random();
    private static final String LEGAL_VAR_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) {
        var shader = new RCSLParser("TestShader", """
                vec4 vertMain(
                    in vec4 inPos,
                    in vec2 inTexCoords,
                    in vec3 inNormal,
                   \s
                    out vec2 outTexCoords,
                    out vec3 outNormal,
                    out vec3 toLightVector,
                    out vec3 toCameraVector,
                   \s
                    uniform mat4 projectionMatrix,
                    uniform mat4 viewMatrix,
                    uniform mat4 modelMatrix,
                    uniform vec3 lightPosition
                ) {
                    vec4 outPos = vec4(modelMatrix * vec4(inPos, 1.0));
                    outTexCoords = inTexCoords;
                    outNormal = mat3(modelMatrix) * inNormal;
                   \s
                    toLightVector = lightPosition - outPos.xyz;
                    toCameraVector = (inverse(viewMatrix) * vec4(0.0, 0.0, 0.0, 1.0)).xyz - outPos.xyz;
                    return projectionMatrix * viewMatrix * outPos;
                }
                                
                const float ambientLight = 0.6;
                                
                vec3 intToColor(int intColor) {
                    return vec3((intColor >> 16 & 255) / 255.0, (intColor >> 8 & 255) / 255.0, (intColor & 255) / 255.0);
                }
                                
                vec4 fragMain(
                    in vec2 outTexCoords,
                    in vec3 outNormal,
                    in vec3 toLightVector,
                    in vec3 toCameraVector,
                   \s
                    uniform int intColor,
                    uniform float shineDamper,
                    uniform float reflectivity,
                    uniform float diffuseColorMix,
                    uniform sampler2D diffuse
                ) {
                    vec4 color = texture2D(diffuse, outTexCoords);
                                
                    vec3 lightColor = intToColor();
                    vec3 pixelmonColor = mix(lightColor, vec3(1.0, 1.0, 1.0), diffuseColorMix);
                    vec3 unitNormal = normalize(outNormal);
                    vec3 unitLightVector = normalize(toLightVector);
                    vec3 lightDir = -unitLightVector;
                    vec3 unitToCameraVector = normalize(toCameraVector);
                                
                    // Diffuse Lighting
                    float rawDiffuse = dot(unitNormal, unitLightVector);
                    float diffuse = max(rawDiffuse, ambientLight);
                    vec3 coloredDiffuse = diffuse * pixelmonColor;
                                
                    // Specular Lighting
                    vec3 reflectedLightDir = reflect(lightDir, unitNormal);
                    float rawSpecularFactor = dot(reflectedLightDir, unitToCameraVector);
                    float specularFactor = max(rawSpecularFactor, 0.0f);
                    float dampedFactor = pow(specularFactor, shineDamper);
                    vec3 finalSpecular = dampedFactor * reflectivity * lightColor;
                                
                    // Output Color pre fixes
                    vec3 correctedColor = coloredDiffuse * color.rgb + finalSpecular;
                                
                    // HDR tonemapping
                    correctedColor = correctedColor / (correctedColor + vec3(1.0));
                                
                    return vec4(correctedColor, color.a);
                }
                                
                               \s""").parse();

        var glslSharedHeader = new StringBuilder("""
                #version 330 core
                #pragma optionNV(strict on)
                """);

        for (var glslConst : shader.consts) {
            glslSharedHeader.append("\n").append(glslConst);
        }

        var vertexShader = new StringBuilder(glslSharedHeader.toString());
        var fragmentShader = new StringBuilder(glslSharedHeader.toString());

        var vsOuts = shader.vertexMethod.params.stream()
                .filter(rcslParam -> rcslParam.target().equals("out"))
                .toList();
        var fsIns = shader.fragmentMethod.params.stream()
                .filter(rcslParam -> rcslParam.target().equals("in"))
                .toList();

        var errorString = String.format("Variable Mismatch! Vertex Shader exports %d variable%s but Fragment Shader takes %d", vsOuts.size(), vsOuts.size() > 1 ? "s" : "", fsIns.size());
        if (vsOuts.size() < fsIns.size()) throw new RuntimeException(errorString);
        else if (vsOuts.size() > fsIns.size()) System.err.println(errorString);

        // map
        var vertexVariableNameMap = new HashMap<String, String>();
        var fragmentVariableNameMap = new HashMap<String, String>();

        for (int i = 0; i < vsOuts.size(); i++) {
            var randomName = generateVariableName();
            vertexVariableNameMap.put(vsOuts.get(i).name(), randomName);
            if (fsIns.size() > i)
                fragmentVariableNameMap.put(shader.fragmentMethod.params.get(i).name(), randomName);
        }

        var refactoredVsBody = new TokenHandler(new TokenIterator(shader.vertexMethod.body)).readBody(token -> {
            if (token.type() == TokenType.NAME && vertexVariableNameMap.containsKey(token.text()))
                return new Token(token.type(), vertexVariableNameMap.get(token.text()), token.pos());

            return token;
        });

        var refactoredFsBody = new TokenHandler(new TokenIterator(shader.fragmentMethod.body)).readBody(token -> {
            if (token.type() == TokenType.NAME && fragmentVariableNameMap.containsKey(token.text()))
                return new Token(token.type(), fragmentVariableNameMap.get(token.text()), token.pos());

            return token;
        });

        // Build Vertex Shader Inputs
        var vsIns = shader.vertexMethod.params.stream()
                .filter(param -> param.target().equals("in"))
                .toList();

        for (int i = 0; i < vsIns.size(); i++) {
            var param = vsIns.get(i);
            vertexShader
                    .append("\nlayout (location = ")
                    .append(i).append(") in ")
                    .append(param.type())
                    .append(" ")
                    .append(param.name());
        }

        System.out.println("e");
    }


    private static String generateVariableName() {
        var out = new StringBuilder();

        for (int i = 0; i < 3; i++) {
            int index = (int) (RANDOM.nextFloat() * LEGAL_VAR_CHARS.length());
            out.append(LEGAL_VAR_CHARS.charAt(index));
        }

        return out.toString();
    }
}
