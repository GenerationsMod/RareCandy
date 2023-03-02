package com.pokemod.rarecandy.shader;

import com.pokemod.rarecandy.shader.tokenizer.TokenHandler;
import com.pokemod.rarecandy.shader.tokenizer.TokenIterator;
import com.pokemod.rarecandy.shader.tokenizer.TokenType;

import java.util.ArrayList;

/**
 * Converts Rare Candy Shader's (rc) to GL Shading Language (glsl)
 */
public class RCSLParser extends TokenHandler {

    private final String shaderName;
    private final String shaderCode;

    public RCSLParser(String name, String code) {
        super(new TokenIterator(code.replaceAll("//.+\n", "")));
        this.shaderName = name;
        this.shaderCode = code;
    }

    public RCSLShader parse() {
        var methods = new ArrayList<RCSLMethod>();
        var consts = new ArrayList<String>();

        do {
            if (matchToken(TokenType.CONST, false)) {
                var lineNumber = readToken().pos().endLineNumber();
                var line = shaderCode.split("\n")[lineNumber];
                // The tokenizer fails to handle these numbers properly and since we don't need to properly tokenize
                // this, im just going to read the line because fuck OpenGL, GLSL, and this Tokenizer
                readToken();
                readToken();
                matchToken(TokenType.EQUALS, true);
                readToken();
                consts.add(line);
            } else {
                var methodType = readToken().text();
                var methodName = readToken().text();
                matchToken(TokenType.BRACKET_LEFT, true);
                var method = new RCSLMethod(RCSLMethod.getType(methodName), methodType);

                do {
                    if (matchToken(TokenType.IN, false) || matchToken(TokenType.OUT, false) || matchToken(TokenType.UNIFORM, false)) {
                        var source = readToken().text();
                        var type = readToken().text();
                        var name = readToken().text();
                        method.params.add(new RCSLParam(source, type, name));
                    } else if (matchToken(TokenType.NAME, false)) {
                        var type = readToken().text();
                        var name = readToken().text();
                        method.params.add(new RCSLParam("none", type, name));
                    }
                } while (!matchToken(TokenType.BRACKET_RIGHT, true));

                method.body = readBody(token -> token);
                methods.add(method);
            }
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

        return new RCSLShader(shaderName, vertexCandidates.get(0), fragmentCandidates.get(0), methods, consts);
    }
}
