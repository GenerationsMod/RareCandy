package com.pokemod.rarecandy.shader.tokenizer;

import java.util.ArrayList;
import java.util.function.Function;

public class TokenHandler {

    private Token tokenToConsume;
    private final TokenIterator tokenIterator;

    public TokenHandler(TokenIterator tokenIterator) {
        this.tokenIterator = tokenIterator;
    }

    public Token readToken() {
        if (tokenToConsume != null) {
            var token = tokenToConsume;
            tokenToConsume = null;
            return token;
        }

        return tokenIterator.next();
    }

    public boolean matchToken(TokenType expectedType, boolean consume) {
        var token = readToken();
        if (!consume) tokenToConsume = token;
        return token != null && token.type().equals(expectedType);
    }

    public String readBody(Function<Token, Token> tokenVisitor) {
        int leftBrackets = 0;
        int rightBrackets = 0;
        var tokens = new ArrayList<Token>();
        var body = new StringBuilder();

        do {
            var token = readToken();
            switch (token.type()) {
                case CURLY_BRACKET_LEFT -> leftBrackets++;
                case CURLY_BRACKET_RIGHT -> rightBrackets++;
            }

            tokens.add(tokenVisitor.apply(token));
        } while (leftBrackets != rightBrackets);

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
        }

        return body.toString();
    }
}
