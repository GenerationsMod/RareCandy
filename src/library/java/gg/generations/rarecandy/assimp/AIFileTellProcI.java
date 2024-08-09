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
 * size_t (*{@link #invoke}) (
 *     struct aiFile *pFile
 * )</code></pre>
 */
@FunctionalInterface
@NativeType("aiFileTellProc")
public interface AIFileTellProcI extends CallbackI {

    FFICIF CIF = apiCreateCIF(
        FFI_DEFAULT_ABI,
        ffi_type_pointer,
        ffi_type_pointer
    );

    @Override
    default FFICIF getCallInterface() { return CIF; }

    @Override
    default void callback(long ret, long args) {
        long __result = invoke(
            memGetAddress(memGetAddress(args))
        );
        apiClosureRetP(ret, __result);
    }

    /**
     * File tell procedure.
     *
     * @param pFile file pointer to query
     *
     * @return the current file position
     */
    @NativeType("size_t") long invoke(@NativeType("struct aiFile *") long pFile);

}