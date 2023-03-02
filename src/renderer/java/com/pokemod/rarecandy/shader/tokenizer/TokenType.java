package com.pokemod.rarecandy.shader.tokenizer;

public enum TokenType {
    IN("in"),
    OUT("out"),
    CURLY_BRACKET_LEFT("{"),
    CURLY_BRACKET_RIGHT("}"),
    BRACKET_LEFT("("),
    BRACKET_RIGHT(")"),
    COMMA(","),
    RETURN("return"),
    SEMICOLON(";"),
    EQUALS("="),
    NAME(""),
    EOF("");

    public final String symbol;

    TokenType(String symbol) {
        this.symbol = symbol;
    }

    public static TokenType bySymbol(String symbol) {
        for (TokenType tokenType : TokenType.values()) {
            if (tokenType.symbol.equals(symbol)) {
                return tokenType;
            }
        }

        return null;
    }
}
