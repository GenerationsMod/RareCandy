/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * MACHINE GENERATED FILE, DO NOT EDIT
 */
package gg.generations.rarecandy.assimp;

import org.lwjgl.system.CallbackI;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.FFICIF;

import static org.lwjgl.system.APIUtil.apiClosureRetP;
import static org.lwjgl.system.APIUtil.apiCreateCIF;
import static org.lwjgl.system.MemoryUtil.memGetAddress;
import static org.lwjgl.system.libffi.LibFFI.FFI_DEFAULT_ABI;
import static org.lwjgl.system.libffi.LibFFI.ffi_type_pointer;

/**
 * <h3>Type</h3>
 * 
 * <pre><code>
 * struct aiFile * (*{@link #invoke}) (
 *     struct aiFileIO *pFileIO,
 *     char const *fileName,
 *     char const *openMode
 * )</code></pre>
 */
@FunctionalInterface
@NativeType("aiFileOpenProc")
public interface AIFileOpenProcI extends CallbackI {

    FFICIF CIF = apiCreateCIF(
        FFI_DEFAULT_ABI,
        ffi_type_pointer,
        ffi_type_pointer, ffi_type_pointer, ffi_type_pointer
    );

    @Override
    default FFICIF getCallInterface() { return CIF; }

    @Override
    default void callback(long ret, long args) {
        long __result = invoke(
            memGetAddress(memGetAddress(args)),
            memGetAddress(memGetAddress(args + POINTER_SIZE)),
            memGetAddress(memGetAddress(args + 2 * POINTER_SIZE))
        );
        apiClosureRetP(ret, __result);
    }

    /**
     * File open procedure
     *
     * @param pFileIO  {@code FileIO} pointer
     * @param fileName name of the file to be opened
     * @param openMode mode in which to open the file
     *
     * @return pointer to an {@link AIFile} structure, or {@code NULL} if the file could not be opened
     */
    @NativeType("struct aiFile *") long invoke(@NativeType("struct aiFileIO *") long pFileIO, @NativeType("char const *") long fileName, @NativeType("char const *") long openMode);

}