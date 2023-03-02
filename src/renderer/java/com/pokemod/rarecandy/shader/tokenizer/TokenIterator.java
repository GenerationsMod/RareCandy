package com.pokemod.rarecandy.shader.tokenizer;

/**
 * Credit to bedrockk for making A Majority of this class comes from <a href="https://github.com/bedrockk/MoLang">MoLang</a>
 * Most code here is reused from there.
 */

public class TokenIterator {

    private final String code;
    private int index = 0;
    private int currentLine = 0;
    private int lastStep = 0;
    private int lastStepLine = 0;

    public TokenIterator(String code) {
        this.code = code;
    }

    public Token next() {
        while (index < code.length()) {
            if (code.length() > index + 1) { // check tokens with double chars
                var token = TokenType.bySymbol(code.substring(index, index + 2));

                if (token != null && TokenType.bySymbol(code.substring(index + 3, index + 4)) != null) {
                    index += 2;
                    return new Token(token, getPosition());
                }
            }

            var expr = getStringAt(index);
            var tokenType = TokenType.bySymbol(expr);
            if (tokenType != null) {
                index++;
                return new Token(tokenType, getPosition());
            } else if (Character.isLetter(expr.charAt(0))) {
                int nameLength = index + 1;

                while (nameLength < code.length() && (Character.isLetterOrDigit(getStringAt(nameLength).charAt(0)) || getStringAt(nameLength).equals("_") || getStringAt(nameLength).equals(".")))
                    nameLength++;


                var value = code.substring(index, nameLength);
                var token = TokenType.bySymbol(value);

                if (token == null) token = TokenType.NAME;

                index = nameLength;
                return new Token(token, value, getPosition());
            } else if (expr.equals("\n") || expr.equals("\r")) currentLine++;

            index++;
        }

        return new Token(TokenType.EOF, getPosition());
    }

    public void step() {
        lastStep = index;
        lastStepLine = currentLine;
    }

    public TokenPosition getPosition() {
        return new TokenPosition(lastStepLine, currentLine, lastStep, index);
    }

    public String getStringAt(int i) {
        return code.substring(i, i + 1);
    }
}
