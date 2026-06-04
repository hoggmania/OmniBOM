/*
 * MIT License
 *
 * Copyright (c) 2025 James Holland
 */
package org.hoggmania;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/**
 * Implements the FOSSA-style dynamic-library approach:
 * ldd identifies linked shared objects, then dpkg/rpm maps each file back to its owning package.
 */
public class DynamicLibraryDetector {
    private static final Pattern LDD_ARROW_PATTERN = Pattern.compile("^\\s*(\\S+)\\s+=>\\s+(\\S+)\\s+\\(.*$");
    private static final Pattern LDD_DIRECT_PATH_PATTERN = Pattern.compile("^\\s*(/\\S+)\\s+\\(.*$");

    private final CommandRunner commandRunner;

    public DynamicLibraryDetector() {
        this(new ProcessBuilderCommandRunner());
    }

    public DynamicLibraryDetector(CommandRunner commandRunner) {
        this.commandRunner = commandRunner;
    }

    public DynamicLibraryReport analyze(List<Path> targets) throws IOException, InterruptedException {
        List<Path> binaries = expandTargets(targets);
        List<DynamicBinaryReport> reports = new ArrayList<>();
        for (Path binary : binaries) {
            reports.add(analyzeBinary(binary));
        }
        return new DynamicLibraryReport(Instant.now(), reports);
    }

    private List<Path> expandTargets(List<Path> targets) throws IOException {
        Map<Path, Path> unique = new LinkedHashMap<>();
        for (Path target : targets) {
            Path normalized = target.toAbsolutePath().normalize();
            if (Files.isDirectory(normalized)) {
                try (Stream<Path> paths = Files.walk(normalized)) {
                    paths.filter(Files::isRegularFile)
                        .filter(this::looksLikeExecutableTarget)
                        .sorted()
                        .forEach(path -> unique.put(path.toAbsolutePath().normalize(), path.toAbsolutePath().normalize()));
                }
            } else if (Files.isRegularFile(normalized)) {
                unique.put(normalized, normalized);
            }
        }
        return unique.keySet().stream().sorted(Comparator.comparing(Path::toString)).toList();
    }

    private boolean looksLikeExecutableTarget(Path path) {
        if (!System.getProperty("os.name").toLowerCase().contains("win") && !Files.isExecutable(path)) {
            return false;
        }
        String fileName = path.getFileName().toString().toLowerCase();
        int lastDot = fileName.lastIndexOf('.');
        if (lastDot < 0) {
            return true;
        }
        return fileName.endsWith(".exe")
            || fileName.endsWith(".bin")
            || fileName.endsWith(".out")
            || fileName.endsWith(".so")
            || fileName.contains(".so.")
            || fileName.endsWith(".dylib");
    }

    private DynamicBinaryReport analyzeBinary(Path binary) throws IOException, InterruptedException {
        CommandResult ldd = commandRunner.run("ldd", List.of(binary.toString()));
        if (ldd.exitCode() != 0) {
            return new DynamicBinaryReport(binary, false, firstNonBlank(ldd.stderr(), ldd.stdout()), List.of());
        }

        List<DynamicLibraryDependency> libraries = new ArrayList<>();
        for (LinkedLibrary linkedLibrary : parseLddOutput(ldd.stdout())) {
            libraries.add(resolveOwner(linkedLibrary));
        }
        return new DynamicBinaryReport(binary, true, null, libraries);
    }

    private List<LinkedLibrary> parseLddOutput(String stdout) {
        List<LinkedLibrary> libraries = new ArrayList<>();
        for (String line : stdout.split("\\R")) {
            Optional<LinkedLibrary> maybeLibrary = parseLddLine(line);
            maybeLibrary.ifPresent(libraries::add);
        }
        return libraries;
    }

    private Optional<LinkedLibrary> parseLddLine(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()
            || trimmed.startsWith("linux-vdso")
            || trimmed.equals("statically linked")
            || trimmed.contains("=> not found")) {
            return Optional.empty();
        }

        Matcher arrowMatcher = LDD_ARROW_PATTERN.matcher(line);
        if (arrowMatcher.matches()) {
            String libraryName = arrowMatcher.group(1);
            String libraryPath = arrowMatcher.group(2);
            if (libraryPath.startsWith("/")) {
                return Optional.of(new LinkedLibrary(libraryName, libraryPath));
            }
            return Optional.empty();
        }

        Matcher directPathMatcher = LDD_DIRECT_PATH_PATTERN.matcher(line);
        if (directPathMatcher.matches()) {
            String libraryPath = directPathMatcher.group(1);
            return Optional.of(new LinkedLibrary(Path.of(libraryPath).getFileName().toString(), libraryPath));
        }

        return Optional.empty();
    }

    private DynamicLibraryDependency resolveOwner(LinkedLibrary library) throws IOException, InterruptedException {
        Optional<PackageOwner> debOwner = resolveDebianOwner(library.path());
        if (debOwner.isPresent()) {
            PackageOwner owner = debOwner.get();
            return managedDependency(library, owner);
        }

        Optional<PackageOwner> rpmOwner = resolveRpmOwner(library.path());
        if (rpmOwner.isPresent()) {
            PackageOwner owner = rpmOwner.get();
            return managedDependency(library, owner);
        }

        return new DynamicLibraryDependency(
            library.name(),
            library.path(),
            false,
            null,
            null,
            null,
            library.path(),
            "unmanaged dynamically linked dependency");
    }

    private DynamicLibraryDependency managedDependency(LinkedLibrary library, PackageOwner owner) {
        return new DynamicLibraryDependency(
            library.name(),
            library.path(),
            true,
            owner.packageType(),
            owner.packageName(),
            owner.packageVersion(),
            library.path(),
            "managed dynamically linked dependency");
    }

    private Optional<PackageOwner> resolveDebianOwner(String libraryPath) throws InterruptedException {
        CommandResult result;
        try {
            result = commandRunner.run("dpkg", List.of("-S", libraryPath));
        } catch (IOException e) {
            return Optional.empty();
        }
        if (result.exitCode() != 0 || result.stdout().isBlank()) {
            return Optional.empty();
        }
        String line = result.stdout().lines().findFirst().orElse("").trim();
        int colon = line.indexOf(':');
        if (colon <= 0) {
            return Optional.empty();
        }
        String packageName = line.substring(0, colon);
        if (packageName.contains(":")) {
            packageName = packageName.substring(0, packageName.indexOf(':'));
        }
        return Optional.of(new PackageOwner("deb", packageName, null));
    }

    private Optional<PackageOwner> resolveRpmOwner(String libraryPath) throws InterruptedException {
        CommandResult result;
        try {
            result = commandRunner.run("rpm", List.of("-qf", libraryPath));
        } catch (IOException e) {
            return Optional.empty();
        }
        if (result.exitCode() != 0 || result.stdout().isBlank()) {
            return Optional.empty();
        }
        String packageIdentifier = result.stdout().lines().findFirst().orElse("").trim();
        if (packageIdentifier.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(new PackageOwner("rpm", packageIdentifier, null));
    }

    private static String firstNonBlank(String first, String second) {
        if (first != null && !first.isBlank()) {
            return first.trim();
        }
        if (second != null && !second.isBlank()) {
            return second.trim();
        }
        return "ldd failed";
    }

    private record LinkedLibrary(String name, String path) {
    }

    private record PackageOwner(String packageType, String packageName, String packageVersion) {
    }
}
