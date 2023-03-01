package com.pokemod.rarecandy.shader.tokenizer;

public record TokenPosition(
        int startLineNumber,
        int endLineNumber,
        int startColumn,
        int endColumn
) {}
