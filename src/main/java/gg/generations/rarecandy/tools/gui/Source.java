package gg.generations.rarecandy.tools.gui;

public sealed interface Source {
    /** A colour attachment of the G-buffer. */
    record Attachment(int index) implements Source {}

    /** The G-buffer's depth texture. */
    record Depth() implements Source {}

    /** Output of the previous enabled pass, or the G-buffer's albedo if this is the first. */
    record Previous() implements Source {}

    /** A texture from the texture loader, by key. */
    record Named(String key) implements Source {}

    static Source attachment(int index) { return new Attachment(index); }
    static Source depth() { return new Depth(); }
    static Source previous() { return new Previous(); }
    static Source named(String key) { return new Named(key); }
}