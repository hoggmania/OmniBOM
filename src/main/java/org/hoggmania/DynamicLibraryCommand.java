/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 */
package org.hoggmania;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Picocli command for FOSSA-style dynamic linked dependency detection.
 */
@Command(
    name = "dynamic-libs",
    mixinStandardHelpOptions = true,
    description = "Detect dynamic C/C++ libraries using ldd, then map library files to dpkg/rpm packages."
)
public class DynamicLibraryCommand implements Runnable {
    @Parameters(arity = "1..*", description = "Binary file(s) or directorie(s) of binaries to inspect with ldd.")
    List<File> targets = new ArrayList<>();

    @Option(names = {"-o", "--output"}, description = "Write JSON report to this file.")
    File output;

    @Option(names = {"-j", "--json"}, description = "Print report as JSON instead of human-readable text.")
    boolean json;

    @Override
    public void run() {
        try {
            DynamicLibraryDetector detector = new DynamicLibraryDetector();
            List<Path> paths = targets.stream()
                .map(File::toPath)
                .toList();
            DynamicLibraryReport report = detector.analyze(paths);

            ObjectMapper mapper = new ObjectMapper()
                .findAndRegisterModules()
                .enable(SerializationFeature.INDENT_OUTPUT);

            if (output != null) {
                mapper.writeValue(output, report);
                System.out.println("Dynamic library report written to: " + output.getAbsolutePath());
            }

            if (json) {
                System.out.println(mapper.writeValueAsString(report));
            } else {
                printHumanReadable(report);
            }
        } catch (Exception e) {
            throw new RuntimeException("Dynamic library detection failed: " + e.getMessage(), e);
        }
    }

    private void printHumanReadable(DynamicLibraryReport report) {
        System.out.println(ConsoleColors.bold("\n=== Dynamic Library Detection ==="));
        System.out.println("Binaries analyzed: " + report.binaries().size());
        for (DynamicBinaryReport binary : report.binaries()) {
            System.out.println("\n--- " + binary.binaryPath() + " ---");
            if (!binary.analyzed()) {
                System.out.println(ConsoleColors.warning("[SKIP]") + " " + binary.error());
                continue;
            }
            if (binary.libraries().isEmpty()) {
                System.out.println("No dynamic libraries reported by ldd.");
                continue;
            }
            for (DynamicLibraryDependency library : binary.libraries()) {
                if (library.managed()) {
                    System.out.printf(
                        "  %s -> %s [%s package: %s, purl: %s]%n",
                        library.name(),
                        library.path(),
                        library.packageType(),
                        library.packageName(),
                        library.packageUrl());
                } else {
                    System.out.printf(
                        "  %s -> %s [%s]%n",
                        library.name(),
                        library.path(),
                        library.status());
                }
            }
        }
    }
}
