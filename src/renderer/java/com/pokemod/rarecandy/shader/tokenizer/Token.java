package com.pokemod.rarecandy.shader.tokenizer;

public record Token(
        TokenType type,
        String text,
        TokenPosition pos
) {

    public Token(TokenType type, TokenPosition pos) {
        this(type, type.symbol, pos);
    }
}
