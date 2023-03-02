package com.pokemod.rarecandy.shader;

import com.pokemod.rarecandy.shader.tokenizer.Token;
import com.pokemod.rarecandy.shader.tokenizer.TokenIterator;
import com.pokemod.rarecandy.shader.tokenizer.TokenType;

import java.util.ArrayList;
import java.util.List;

/**
 * Converts Rare Candy Shader's (rc) to GL Shading Language (glsl)
 */
public class RCSLParser {

    private final List<Token> readTokens = new ArrayList<>();
    private final String shaderName;
    private Token tokenToConsume;
    private final TokenIterator tokenIterator;

    public RCSLParser(String name, String code) {
        this.shaderName = name;
        this.tokenIterator = new TokenIterator(code.replaceAll("//.+\n", ""));
    }

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

        System.out.println("e");
    }

    private RCSLShader parse() {
        var methods = new ArrayList<RCSLMethod>();

        do {
            var methodType = readToken().text();
            var methodName = readToken().text();
            matchToken(TokenType.BRACKET_LEFT, true);
            var method = new RCSLMethod(RCSLMethod.getType(methodName), methodType);

            do {
                var source = readToken().text();
                var type = readToken().text();
                var name = readToken().text();
                method.params.add(new RCSLParam(source, type, name));
            } while (!matchToken(TokenType.BRACKET_RIGHT, true));

            int leftBrackets = 0;
            int rightBrackets = 0;
            var tokens = new ArrayList<Token>();

            do {
                var token = readToken();
                switch (token.type()) {
                    case CURLY_BRACKET_LEFT -> leftBrackets++;
                    case CURLY_BRACKET_RIGHT -> rightBrackets++;
                }
                tokens.add(token);
            } while (leftBrackets != rightBrackets);

            var body = new StringBuilder();
            for (var token : tokens) {
                var prefix = switch (token.type()) {
                    case SEMICOLON, COMMA -> "";
                    default -> " ";
                };

                var suffix = switch (token.type()) {
                    case SEMICOLON, CURLY_BRACKET_LEFT -> "\n";
                    default -> "";
                };

                body.append(prefix)
                        .append(token.text())
                        .append(suffix);

                method.methodBody = body.toString();
            }

            methods.add(method);
        } while (!matchToken(TokenType.EOF, false));

        var vertexCandidates = methods.stream()
                .filter(rcslMethod -> rcslMethod.methodType == RCSLMethod.Type.VERTEX)
                .toList();

        var fragmentCandidates = methods.stream()
                .filter(rcslMethod -> rcslMethod.methodType == RCSLMethod.Type.FRAGMENT)
                .toList();

        if (vertexCandidates.size() > 1)
            throw new RuntimeException("More than 1 vertex shader method exists");

        if (fragmentCandidates.size() > 1)
            throw new RuntimeException("More than 1 fragment shader method exists");

        methods.remove(vertexCandidates.get(0));
        methods.remove(fragmentCandidates.get(0));

        return new RCSLShader(shaderName, vertexCandidates.get(0), fragmentCandidates.get(0), methods);
    }

    private Token readToken() {
        if (tokenToConsume != null) {
            var token = tokenToConsume;
            tokenToConsume = null;
            return token;
        }
        var next = tokenIterator.next();
        readTokens.add(next);
        return next;
    }

    public boolean matchToken(TokenType expectedType, boolean consume) {
        var token = readToken();
        if (!consume) tokenToConsume = token;
        return token != null && token.type().equals(expectedType);
    }
}
