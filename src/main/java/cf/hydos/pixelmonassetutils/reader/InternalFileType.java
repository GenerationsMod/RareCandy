package cf.hydos.pixelmonassetutils.reader;

/**
 * Each version of Khronos' Graphics Language Transmission Format.
 */
public enum InternalFileType {
    GRAPHICS_LANGUAGE_BINARY(new GlbReader()),
    GRAPHICS_LANGUAGE_JSON(null);

    public final FileReader reader;

    InternalFileType(FileReader reader) {
        this.reader = reader;
    }
}
