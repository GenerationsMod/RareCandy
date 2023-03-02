package com.pokemod.rarecandy.shader;

import com.pokemod.rarecandy.shader.tokenizer.Token;
import com.pokemod.rarecandy.shader.tokenizer.TokenHandler;
import com.pokemod.rarecandy.shader.tokenizer.TokenIterator;
import com.pokemod.rarecandy.shader.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

public class RCSLConverter {
    private static final Random RANDOM = new Random();
    private static final String LEGAL_VAR_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) {
        var shader = new RCSLParser("TestShader", """
                vec4 vertMain(
                    in vec4 InPosition,
                    in vec2 InUV,
                    out vec2 OutUV
                ) {
                    return InPosition;
                    OutUV = InUV;
                }
                                
                vec4 fragMain(
                    in vec2 UV,
                    uniform sampler2D diffuse
                ) {
                    return texture(diffuse, UV);
                }
                                
                """).parse();

        var glslSharedHeader = """
                #version 330 core
                #pragma optionNV(strict on)
                """;

        var vertexShader = new StringBuilder(glslSharedHeader);
        var fragmentShader = new StringBuilder(glslSharedHeader);

        var vertexOuts = shader.vertexMethod.params.stream()
                .filter(rcslParam -> rcslParam.target().equals("out"))
                .toList();
        var fragmentIns = shader.fragmentMethod.params.stream()
                .filter(rcslParam -> rcslParam.target().equals("in"))
                .toList();

        var errorString = String.format("Variable Mismatch! Vertex Shader exports %d variable%s but Fragment Shader takes %d", vertexOuts.size(), vertexOuts.size() > 1 ? "s" : "", fragmentIns.size());
        if (vertexOuts.size() < fragmentIns.size()) throw new RuntimeException(errorString);
        else if (vertexOuts.size() > fragmentIns.size()) System.err.println(errorString);

        // map
        var vertexVariableNameMap = new HashMap<String, String>();
        var fragmentVariableNameMap = new HashMap<String, String>();

        for (int i = 0; i < vertexOuts.size(); i++) {
            var randomName = generateVariableName();
            vertexVariableNameMap.put(vertexOuts.get(i).name(), randomName);
            if (fragmentIns.size() > i)
                fragmentVariableNameMap.put(shader.fragmentMethod.params.get(i).name(), randomName);
        }

        var refactoredVertexBody = new TokenHandler(new TokenIterator(shader.vertexMethod.methodBody)).readBody(token -> {
            if (token.type() == TokenType.NAME && vertexVariableNameMap.containsKey(token.text()))
                return new Token(token.type(), vertexVariableNameMap.get(token.text()), token.pos());

            return token;
        });

        var refactoredFragmentBody = new TokenHandler(new TokenIterator(shader.fragmentMethod.methodBody)).readBody(token -> {
            if (token.type() == TokenType.NAME && fragmentVariableNameMap.containsKey(token.text()))
                return new Token(token.type(), fragmentVariableNameMap.get(token.text()), token.pos());

            return token;
        });

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
