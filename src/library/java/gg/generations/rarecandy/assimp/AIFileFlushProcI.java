/*
 * Copyright LWJGL. All rights reserved.
 * License terms: https://www.lwjgl.org/license
 * MACHINE GENERATED FILE, DO NOT EDIT
 */
package gg.generations.rarecandy.assimp;

import org.lwjgl.system.CallbackI;
import org.lwjgl.system.NativeType;
import org.lwjgl.system.libffi.FFICIF;

import static org.lwjgl.system.APIUtil.apiCreateCIF;
import static org.lwjgl.system.MemoryUtil.memGetAddress;
import static org.lwjgl.system.libffi.LibFFI.*;

/**
 * <h3>Type</h3>
 * 
 * <pre><code>
 * void (*{@link #invoke}) (
 *     struct aiFile *pFile
 * )</code></pre>
 */
@FunctionalInterface
@NativeType("aiFileFlushProc")
public interface AIFileFlushProcI extends CallbackI {

    FFICIF CIF = apiCreateCIF(
        FFI_DEFAULT_ABI,
        ffi_type_void,
        ffi_type_pointer
    );

    @Override
    default FFICIF getCallInterface() { return CIF; }

    @Override
    default void callback(long ret, long args) {
        invoke(
            memGetAddress(memGetAddress(args))
        );
    }

    /**
     * File flush procedure.
     *
     * @param pFile file pointer to flush
     */
    void invoke(@NativeType("struct aiFile *") long pFile);

}