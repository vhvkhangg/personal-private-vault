package com.vhvkhangg.personalprivatevault.rag.internal.application.retrieval;

import java.lang.foreign.*;

/** Captures this controller's exact Windows creation identity without changing the frozen worker. */
public final class RagRetrievalControllerIdentity {
    private RagRetrievalControllerIdentity() {}

    public static long creationFileTime() {
        if (!System.getProperty("os.name").toLowerCase(java.util.Locale.ROOT).contains("win")
                || !RagRetrievalControllerIdentity.class.getModule().isNativeAccessEnabled()) {
            throw new IllegalStateException("Query controller identity is unavailable");
        }
        try (Arena arena = Arena.ofConfined()) {
            var library=SymbolLookup.libraryLookup("Kernel32",arena);
            var linker=Linker.nativeLinker();
            var current=linker.downcallHandle(library.find("GetCurrentProcess").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.ADDRESS));
            var times=linker.downcallHandle(library.find("GetProcessTimes").orElseThrow(),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT,ValueLayout.ADDRESS,ValueLayout.ADDRESS,
                            ValueLayout.ADDRESS,ValueLayout.ADDRESS,ValueLayout.ADDRESS));
            MemorySegment process=(MemorySegment)current.invokeExact(); // pseudo-handle; never close it
            MemorySegment values=arena.allocate(32,8);
            int result=(int)times.invokeExact(process,values,values.asSlice(8),values.asSlice(16),values.asSlice(24));
            if(result==0) throw new IllegalStateException("Controller creation identity unavailable");
            return values.get(ValueLayout.JAVA_LONG,0);
        } catch (RuntimeException failure) { throw failure; }
        catch (Throwable failure) { throw new IllegalStateException("Controller creation identity unavailable"); }
    }
}
