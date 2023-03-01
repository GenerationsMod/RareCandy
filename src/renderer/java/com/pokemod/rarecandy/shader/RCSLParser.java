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
    private final TokenIterator tokenIterator;

    public RCSLParser(String code) {
        this.tokenIterator = new TokenIterator(code.replaceAll("//.+\n", ""));
    }

    public static void main(String[] args) {
        new RCSLParser("""
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
    }

    private void parse() {
        do {
            var methodType = readToken().text();
            var methodName = readToken().text();
            matchToken(TokenType.BRACKET_LEFT, false);
            var method = new RCSLMethod(RCSLMethod.getType(methodName), methodType);

            do {
                var source = readToken().text();
                var type = readToken().text();
                var name = readToken().text();
                method.params.add(new RCSLParam(source, type, name));
            } while (!matchToken(TokenType.BRACKET_RIGHT));


            System.out.println("e");
        } while (!matchToken(TokenType.EOF, false));

        System.out.println("ok");
    }

    private Token readToken() {
        var next = tokenIterator.next();
        readTokens.add(next);
        return next;
    }

    public boolean matchToken(TokenType expectedType) {
        return matchToken(expectedType, true);
    }

    public boolean matchToken(TokenType expectedType, boolean consume) {
        var token = readToken();

        if (token == null || !token.type().equals(expectedType)) {
            return false;
        } else {
            if (consume) readToken();
            return true;
        }
    }
}
