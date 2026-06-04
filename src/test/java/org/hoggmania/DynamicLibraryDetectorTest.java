package org.hoggmania;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DynamicLibraryDetectorTest {

    @TempDir
    Path tempDir;

    @Test
    void detectsLinkedLibrariesAndMapsThemToDebianPackages() throws Exception {
        Path binary = Files.writeString(tempDir.resolve("app"), "fake binary");
        FakeCommandRunner runner = new FakeCommandRunner()
            .when("ldd", List.of(binary.toString()), 0, """
                linux-vdso.so.1 (0x00007ffd59dc7000)
                libssl.so.3 => /lib/x86_64-linux-gnu/libssl.so.3 (0x00007f6e00000000)
                libcrypto.so.3 => /lib/x86_64-linux-gnu/libcrypto.so.3 (0x00007f6e00010000)
                libc.so.6 => /lib/x86_64-linux-gnu/libc.so.6 (0x00007f6e00020000)
                /lib64/ld-linux-x86-64.so.2 (0x00007f6e00030000)
                """, "")
            .when("dpkg", List.of("-S", "/lib/x86_64-linux-gnu/libssl.so.3"), 0,
                "libssl3:amd64: /lib/x86_64-linux-gnu/libssl.so.3\n", "")
            .when("dpkg", List.of("-S", "/lib/x86_64-linux-gnu/libcrypto.so.3"), 0,
                "libssl3:amd64: /lib/x86_64-linux-gnu/libcrypto.so.3\n", "")
            .when("dpkg", List.of("-S", "/lib/x86_64-linux-gnu/libc.so.6"), 0,
                "libc6:amd64: /lib/x86_64-linux-gnu/libc.so.6\n", "")
            .when("dpkg", List.of("-S", "/lib64/ld-linux-x86-64.so.2"), 1, "", "no path found")
            .when("rpm", List.of("-qf", "/lib64/ld-linux-x86-64.so.2"), 1, "", "not owned");

        DynamicLibraryDetector detector = new DynamicLibraryDetector(runner);
        DynamicLibraryReport report = detector.analyze(List.of(binary));

        assertEquals(1, report.binaries().size());
        DynamicBinaryReport binaryReport = report.binaries().getFirst();
        assertEquals(binary.toAbsolutePath().normalize(), binaryReport.binaryPath());
        assertEquals(4, binaryReport.libraries().size());

        DynamicLibraryDependency ssl = binaryReport.libraries().stream()
            .filter(lib -> lib.name().equals("libssl.so.3"))
            .findFirst()
            .orElseThrow();
        assertEquals("/lib/x86_64-linux-gnu/libssl.so.3", ssl.path());
        assertTrue(ssl.managed());
        assertEquals("libssl3", ssl.packageName());
        assertEquals("deb", ssl.packageType());
        assertEquals("pkg:deb/libssl3?arch=amd64", ssl.packageUrl());

        DynamicLibraryDependency loader = binaryReport.libraries().stream()
            .filter(lib -> lib.name().equals("ld-linux-x86-64.so.2"))
            .findFirst()
            .orElseThrow();
        assertFalse(loader.managed());
        assertEquals("unmanaged dynamically linked dependency", loader.status());
    }

    @Test
    void reportsLddFailuresWithoutTryingPackageManagers() throws Exception {
        Path binary = Files.writeString(tempDir.resolve("script"), "#!/bin/sh\n");
        FakeCommandRunner runner = new FakeCommandRunner()
            .when("ldd", List.of(binary.toString()), 1, "", "not a dynamic executable\n");

        DynamicLibraryDetector detector = new DynamicLibraryDetector(runner);
        DynamicLibraryReport report = detector.analyze(List.of(binary));

        assertEquals(1, report.binaries().size());
        DynamicBinaryReport binaryReport = report.binaries().getFirst();
        assertFalse(binaryReport.analyzed());
        assertEquals("not a dynamic executable", binaryReport.error());
        assertTrue(binaryReport.libraries().isEmpty());
        assertEquals(1, runner.callsTo("ldd"));
        assertEquals(0, runner.callsTo("dpkg"));
        assertEquals(0, runner.callsTo("rpm"));
    }

    @Test
    void generatesPackageUrlsForRpmOwnedLibraries() throws Exception {
        Path binary = Files.writeString(tempDir.resolve("app"), "fake binary");
        FakeCommandRunner runner = new FakeCommandRunner()
            .when("ldd", List.of(binary.toString()), 0,
                "libcrypto.so.3 => /usr/lib64/libcrypto.so.3 (0x00007f6e00010000)\n", "")
            .when("dpkg", List.of("-S", "/usr/lib64/libcrypto.so.3"), 1, "", "no path found")
            .when("rpm", List.of("-qf", "/usr/lib64/libcrypto.so.3"), 0,
                "openssl-libs-3.0.7-27.el9.x86_64\n", "");

        DynamicLibraryDetector detector = new DynamicLibraryDetector(runner);
        DynamicLibraryReport report = detector.analyze(List.of(binary));

        DynamicLibraryDependency dependency = report.binaries().getFirst().libraries().getFirst();
        assertTrue(dependency.managed());
        assertEquals("rpm", dependency.packageType());
        assertEquals("openssl-libs", dependency.packageName());
        assertEquals("3.0.7-27.el9", dependency.packageVersion());
        assertEquals("pkg:rpm/openssl-libs@3.0.7-27.el9?arch=x86_64", dependency.packageUrl());
    }

    @Test
    void treatsLibrariesAsUnmanagedWhenPackageManagersAreUnavailable() throws Exception {
        Path binary = Files.writeString(tempDir.resolve("app"), "fake binary");
        FakeCommandRunner runner = new FakeCommandRunner()
            .when("ldd", List.of(binary.toString()), 0,
                "libz.so.1 => /lib/x86_64-linux-gnu/libz.so.1 (0x00007f6e00000000)\n", "")
            .missing("dpkg", List.of("-S", "/lib/x86_64-linux-gnu/libz.so.1"))
            .missing("rpm", List.of("-qf", "/lib/x86_64-linux-gnu/libz.so.1"));

        DynamicLibraryDetector detector = new DynamicLibraryDetector(runner);
        DynamicLibraryReport report = detector.analyze(List.of(binary));

        DynamicLibraryDependency dependency = report.binaries().getFirst().libraries().getFirst();
        assertFalse(dependency.managed());
        assertEquals("unmanaged dynamically linked dependency", dependency.status());
    }

    @Test
    void expandsDirectoriesToExecutableFiles() throws Exception {
        Path executable = tempDir.resolve("bin/tool");
        Files.createDirectories(executable.getParent());
        Files.writeString(executable, "fake binary");
        executable.toFile().setExecutable(true, false);
        Files.writeString(tempDir.resolve("notes.txt"), "not executable");

        FakeCommandRunner runner = new FakeCommandRunner()
            .when("ldd", List.of(executable.toString()), 0, "statically linked\n", "");

        DynamicLibraryDetector detector = new DynamicLibraryDetector(runner);
        DynamicLibraryReport report = detector.analyze(List.of(tempDir));

        assertEquals(1, report.binaries().size());
        assertEquals(executable.toAbsolutePath().normalize(), report.binaries().getFirst().binaryPath());
    }
}
