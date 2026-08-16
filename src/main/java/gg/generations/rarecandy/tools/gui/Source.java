package gg.generations.rarecandy.tools.gui;

public sealed interface Source {
    /** A colour attachment of the G-buffer. */
    record Attachment(int index) implements Source {}

    /** The G-buffer's depth texture. */
    enum Depth implements Source { INSTANCE }

    /** Output of the previous enabled pass, or the G-buffer's albedo if this is the first. */
    enum Previous implements Source { INSTANCE }

    /** A texture from the texture loader, by key. */
    record Named(String key) implements Source {}

    /** Output of an earlier pass that declared {@code writesTo(key)}. */
    record Pass(String key) implements Source {}

    static Source attachment(int index) {
        return new Attachment(index);
    }

    static Source depth() {
        return Depth.INSTANCE;
    }

    static Source previous() {
        return Previous.INSTANCE;
    }

    static Source named(String key) {
        return new Named(key);
    }

    static Source pass(String key) {
        return new Pass(key);
    }
}