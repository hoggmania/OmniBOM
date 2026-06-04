/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 */
package org.hoggmania;

import io.quarkus.runtime.annotations.RegisterForReflection;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

@RegisterForReflection
public record DynamicLibraryReport(
    Instant timestamp,
    List<DynamicBinaryReport> binaries
) {
}

@RegisterForReflection
record DynamicBinaryReport(
    Path binaryPath,
    boolean analyzed,
    String error,
    List<DynamicLibraryDependency> libraries
) {
}

@RegisterForReflection
record DynamicLibraryDependency(
    String name,
    String path,
    boolean managed,
    String packageType,
    String packageName,
    String packageVersion,
    String packageUrl,
    String ownerQuery,
    String status
) {
}
