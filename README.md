# OmniBOM

A powerful CLI tool for CI to intelligently analyzing build environments and generating Software Bill of Materials (SBOM) with automatic build system detection.

## Features

- **Build Environment Scanning**: Automatically detects Maven, Gradle, npm, Yarn, pnpm, Python (pip/requirements, Pipenv, Poetry, uv), Go, .NET, Rust, PHP, Ruby, Conda, and standalone binaries
- **Intelligent SBOM Generation**: Runs CycloneDX tooling when available, with Syft-based filesystem/binary scanning as a fallback
- **Dynamic Library Detection**: Uses an `ldd` + package-manager ownership workflow to identify dynamically linked native libraries and emit package URLs (PURLs) where supported
- **Command Progress Bars**: Shows a progress bar as each SBOM command starts and completes
- **File Type Analysis**: Analyzes source code files with percentage breakdowns and rankings
- **Multi-Module Support**: Aggregates Maven multi-module SBOMs and supports Gradle projects with the CycloneDX plugin
- **Cross-Platform Core Scanning**: Works on Windows, Linux, and macOS; native dynamic-library ownership/PURL mapping is Linux-first with Windows caveats documented below
- **Native Image Support**: Optimized for GraalVM native compilation for instant startup
- **JSON Export**: Optional JSON output for scan results and SBOM summaries (`--json`)

## Prerequisites

- **Java 21** (LTS) or later
- **Maven 3.9+** or use the included Maven wrapper (`mvnw`)
- For native builds: **GraalVM 21+** with native-image installed

## Installation

### Clone and Build

```bash
git clone https://github.com/hoggmania/Build.Intel.git
cd omnibom

# Build JVM version
./mvnw clean package

# Build native executable (optional, requires GraalVM)
./mvnw clean package -Dnative
```

## Usage

### Scan Command

The `scan` command analyzes your build environment and source code:

```bash
# JVM mode (console output)
java -jar target/quarkus-app/quarkus-run.jar scan

# JVM mode with JSON output (default: scan-results.json)
java -jar target/quarkus-app/quarkus-run.jar scan --json

# Native executable
./target/omnibom-1.0.0-SNAPSHOT-runner scan

# Specify custom JSON output file
java -jar target/quarkus-app/quarkus-run.jar scan --json --output custom-scan.json
```

**Output:**

- **Console Output**: Always displays formatted results to stdout
- **JSON File**: Generated when `--json` is set (default: `scan-results.json` or `--output`)

**Information Included:**

- Detected build tools with versions (Maven, Gradle, npm, Python, Go)
- Multi-module build detection
- Source file type distribution with percentages
- Ranked file types by count

**Example Output (`--json` enabled):**

```bash
OmniBOM Scanner
=====================================

Scanned directory: D:\dev\github\.

Build Systems Detected:

   Maven:
      [Standalone] .\Binary-scanning-examples\source-projects\code-with-quarkus\pom.xml
      [Multi-Module Root] .\Binary-scanning-examples\source-projects\sample-java-maven-multi\pom.xml
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-maven-single\pom.xml
      [Standalone] .\omnibom\pom.xml
      [Standalone] .\cdxgen\test\pom.xml
      [Standalone] .\ClouderaVEX\pom.xml
      [Standalone] .\mvn-repo-vex\pom.xml
      [Standalone] .\picocli\pom.xml
      [Standalone] .\PURL-Service\pom.xml
      [Standalone] .\quarkus-agentic-ai\pom.xml
      [Standalone] .\random-generator\pom.xml
      [Standalone] .\SBOMMerge\pom.xml
      [Standalone] .\scalibr\osv-cli\pom.xml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\enricher\reachability\java\testdata\javareach-test\META-INF\maven\com\example\hello-tester\pom.xml
      [Standalone] .\scalibr\scalibr-java-bindings\pom.xml
      [Standalone] .\ScanStatus\pom.xml
      [Standalone] .\service.purl\pom.xml
      [Standalone] .\sigstore-maven-plugin\pom.xml
      [Standalone] .\sigstore-maven-plugin\src\it\simple-it\pom.xml
      [Standalone] .\sigstore-maven-plugin\src\test\resources\project-to-test\pom.xml
      [Standalone] .\snyk-api-client\pom.xml
      [Standalone] .\utils.snykParser\pom.xml

   Go:
      [Standalone] .\cdxgen\test\gomod\go.mod
      [Standalone] .\cyclonedx-gomod\go.mod
      [Standalone] .\scalibr\scalibr-c-bindings\go.mod
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\go.mod
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\linter\plugger\plugger\testdata\go.mod

   npm:
      [Standalone] .\cdxgen\package.json
      [Standalone] .\cdxgen\test\data\package-json\v1\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2-workspace\app\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2-workspace\package.json
      [Standalone] .\cdxgen\test\data\package-json\v2-workspace\scripts\package.json
      [Standalone] .\cdxgen\test\data\package-json\v3\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\accepts\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\acorn\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\acorn-globals\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\no-person-name\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\window-size\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\deps\with\deps\acorn\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\javascript\packagejson\testdata\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\lockfile\npm\testdata\v1\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\ws\jquery\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\ws\ugh\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\npm\testdata\workspaces\z\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\deepen\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\diamond\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\introduce-vuln\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\non-constraining\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\removed-vuln\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\simple\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\npm\vuln-without-fix\package.json
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\npm\basicrelax\package.json

   Standalone Binaries (Syft):
      [Standalone] .\Binary-scanning-examples\binary-projects\code-with-quarkus-built\code-with-quarkus-1.0.0-SNAPSHOT.jar
      [Standalone] .\cli-test\jars\commons-io-2.14.0.jar
      [Standalone] .\cli-test\jars\cyclonedx-core-java-8.0.3.jar
      [Standalone] .\cli-test\jars\json-schema-validator-1.0.87.jar
      [Standalone] .\cli-test\jars\snakeyaml-2.0.jar
      [Standalone] .\cli-test\jars\woodstox-core-6.5.1.jar
      [Standalone] .\cli-test\jars\woodstox-core-modified.jar
      [Standalone] .\ClouderaVEX\maven-wrapper.jar
      [Standalone] .\quarkus.gradle.kotlin.test\gradle.test\gradle\wrapper\gradle-wrapper.jar
      [Standalone] .\quarkus.gradle.test\gradle.test\gradle\wrapper\gradle-wrapper.jar
      [Standalone] .\random-generator\jfiglet-0.0.9.jar


   .NET:
      [Standalone] .\cdxgen\test\data\Logging.csproj
      [Standalone] .\cdxgen\test\data\sample-dotnet.csproj
      [Standalone] .\cdxgen\test\data\Server.csproj
      [Standalone] .\cdxgen\test\data\WindowsFormsApplication1.csproj
      [Standalone] .\cdxgen\test\sample.csproj

   PHP:
      [Standalone] .\cdxgen\test\data\composer.json

   Gradle:
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\core\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\service\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-multi\web\build.gradle
      [Standalone] .\Binary-scanning-examples\source-projects\sample-java-gradle-single\build.gradle
      [Standalone] .\quarkus.gradle.test\gradle.test\build.gradle
      [Standalone] .\quarkus.gradle.kotlin.test\gradle.test\build.gradle.kts

   Python:
      [Standalone] .\cdxgen\test\data\pyproject.toml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\python\testdata\poetry\pyproject.toml
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\python\relax\poetry\pyproject.toml
      [Standalone] .\cdxgen\contrib\requirements.txt
      [Standalone] .\cdxgen\test\diff\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\extractor\filesystem\language\python\requirementsnet\testdata\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\manifest\python\testdata\requirements\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\deepen\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\diamond\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\introduce\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\max-depth\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\no-fix\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\non-constraining\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\removed\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\internal\strategy\relax\testdata\python\simple\requirements.txt
      [Standalone] .\scalibr\scalibr-c-bindings\osv-scalibr\guidedremediation\testdata\python\relax\requirements\requirements.txt

Tool Versions:
   Maven: [OK]
      Picked up JAVA_TOOL_OPTIONS: --enable-native-access=ALL-UNNAMED
      Apache Maven 3.9.11 (3e54c93a704957b63ee3494413a2b544fd3d825b)
      Maven home: D:\dev\java\M2\maven
      Java version: 25.0.1, vendor: Oracle Corporation, runtime: D:\dev\java\jdk\graalvm\graalvm-jdk-25.0.1+8.1
      Default locale: en_GB, platform encoding: UTF-8
      OS name: "windows 10", version: "10.0", arch: "amd64", family: "windows"
   Go: [OK]
      go version go1.25.4 windows/amd64
   npm: [OK]
      11.6.1
   Standalone Binaries (Syft): [OK]
      Application:   syft
      Version:       1.36.0
      BuildDate:     2025-10-22T20:08:43Z
      GitCommit:     8be463911ce718ff70179ded9a2a4dd37549d374
      GitDescription: v1.36.0
      Platform:      windows/amd64
      GoVersion:     go1.24.7
      Compiler:      gc
      SchemaVersion: 16.0.41
   .NET: [OK]
      9.0.306
   PHP: [NOT FOUND]
      Not installed or inaccessible
   Gradle: [OK]
      Picked up JAVA_TOOL_OPTIONS: --enable-native-access=ALL-UNNAMED
      ------------------------------------------------------------
      Gradle 9.2.0
      ------------------------------------------------------------
      Build time:    2025-10-29 13:53:23 UTC
      Revision:      d9d6bbce03b3d88c67ef5a0ff31f7ae5e332d6bf
      Kotlin:        2.2.20
      Groovy:        4.0.28
      Ant:           Apache Ant(TM) version 1.10.15 compiled on August 25 2024
      Launcher JVM:  25.0.1 (Oracle Corporation 25.0.1+8-LTS-jvmci-b01)
      Daemon JVM:    D:\dev\java\jdk\graalvm\graalvm-jdk-25.0.1+8.1 (no JDK specified, using current Java home)
      OS:            Windows 10 10.0 amd64
   Python: [OK]
      Python 3.14.0

File Type Counts:
   go: 946 (69.05%)
   java: 189 (13.80%)
   Dockerfile/Container: 130 (9.49%)
   js: 34 (2.48%)
   ts: 22 (1.61%)
   sh: 16 (1.17%)
   cmd: 9 (0.66%)
   py: 7 (0.51%)
   ps1: 5 (0.36%)
   bat: 5 (0.36%)
   kts: 2 (0.15%)
   rs: 2 (0.15%)
   clj: 1 (0.07%)
   rb: 1 (0.07%)
   groovy: 1 (0.07%)

[SUCCESS] JSON results written to D:\dev\github\scan-results.json

D:\dev\github>

```

### SBOM Command


The `sbom` command generates SBOMs using the most appropriate tool for each detected build system:

```bash
# Auto-detect and generate SBOMs (default output: generated-sboms/)
java -jar target/quarkus-app/quarkus-run.jar sbom

# Write summary logs as JSON
java -jar target/quarkus-app/quarkus-run.jar sbom --json

# Specify output directory
java -jar target/quarkus-app/quarkus-run.jar sbom --output ./custom-sbom-dir

# Generate and merge multiple SBOMs (for multi-language projects)
java -jar target/quarkus-app/quarkus-run.jar sbom --merge

# Allow installing missing tools
java -jar target/quarkus-app/quarkus-run.jar sbom --allow-tool-install

# Pass through additional args to the underlying tools
java -jar target/quarkus-app/quarkus-run.jar sbom --additional-args "--no-validate"

# SBOM-only mode (Syft filesystem scan)
java -jar target/quarkus-app/quarkus-run.jar sbom --sbom-only --output ./custom-sbom-dir

# Dry run (show what would be executed)
java -jar target/quarkus-app/quarkus-run.jar sbom --dry-run
```

**Output:**

- **Console Output**: Always displays build system detection, command execution, and generation status
- **SBOM Files**: Generated in the specified output directory (default: `generated-sboms/`); JSON for most tools, XML for `cyclonedx-ruby`
- **Summary JSON**: Generated when `--json` is set (`sbom-summary.json` and `sbom-generation-aggregate.json`)

**Key Features:**

- **Auto-detection**: Automatically identifies build systems and selects the most appropriate generator
- **JSON Format**: JSON for most generators; Ruby emits CycloneDX XML via `cyclonedx-ruby`
- **Project-based naming**: SBOM files are automatically named using the project name extracted from build files (e.g., `omnibom-bom.json`, `my-app-bom.json`)
- **SBOM Merging**: Use `--merge` flag to combine multiple SBOMs into a single `merged-bom.json` (useful for polyglot projects)
- **Default output**: All SBOMs are generated in the `generated-sboms/` directory at the project root
- **JSON Summary**: Includes timestamp, build system, project name, working directory, command executed, and list of generated files when `--json` is set

**Project Name Detection:**

- **Maven**: Extracted from `<artifactId>` in `pom.xml`
- **Gradle**: Extracted from `rootProject.name` in `settings.gradle` or directory name
- **npm/Yarn/pnpm**: Extracted from `"name"` field in `package.json`
- **Python**: Extracted from `name=` in `setup.py` or `pyproject.toml`
- **Pipenv/Poetry/uv**: Defaults to the project directory name
- **Go**: Extracted from module name in `go.mod`
- **.NET**: Extracted from `<AssemblyName>` or project filename
- **Rust**: Extracted from `name` in `Cargo.toml`
- **PHP**: Extracted from `"name"` in `composer.json`
- **Ruby**: Extracted from `.gemspec` filename or directory name
- **Conda/Standalone binaries**: Defaults to the project directory name

**Supported Build Systems:**

| Build System | Detection File | Tool | Command |
|--------------|---------------|------|---------|
| Maven | `pom.xml` | cyclonedx-maven-plugin | `mvn cyclonedx:makeAggregateBom` |
| Gradle | `build.gradle(.kts)` | org.cyclonedx.bom | `gradle cyclonedxBom` |
| npm | `package.json` | npm CLI | `npm sbom --sbom-format=cyclonedx` |
| Yarn | `yarn.lock` | Syft | `syft scan dir:<path> -o cyclonedx-json=...` |
| pnpm | `pnpm-lock.yaml` | Syft | `syft scan dir:<path> -o cyclonedx-json=...` |
| Pipenv | `Pipfile.lock` | cyclonedx-py | `cyclonedx-py pipenv` |
| Poetry | `poetry.lock` | cyclonedx-py | `cyclonedx-py poetry` |
| uv | `uv.lock` | uv + cyclonedx-py | `uv run cyclonedx-py environment` |
| Python | `requirements.txt`, `setup.py`, `pyproject.toml` | cyclonedx-py | `cyclonedx-py requirements` |
| Conda | `environment.yml` | Syft | `syft scan dir:<path> -o cyclonedx-json=...` |
| Go | `go.mod` | Go toolchain | `go list -m -json all` |
| .NET | `*.csproj`, `*.vbproj`, `*.fsproj`, `*.sln` | CycloneDX (.NET tool) | `dotnet CycloneDX --output-format json -o <dir> -fn <file>` |
| Rust | `Cargo.toml` | cargo-cyclonedx | `cargo cyclonedx -f json --override-filename <name>` |
| PHP | `composer.json` | cyclonedx-php-composer | `composer CycloneDX:make-sbom` |
| Ruby | `Gemfile` | cyclonedx-ruby | `cyclonedx-ruby -p <dir> -o <file>` |
| Standalone Binaries | `*.jar`, `*.exe`, `*.dll`, ... | Syft | `syft scan dir:<path> -o cyclonedx-json=...` |

For detailed information about CycloneDX plugins for each language, see [cyclonedx_plugins_by_language.md](cyclonedx_plugins_by_language.md).

### Dynamic Library Detection

The `dynamic-libs` subcommand implements a FOSSA-style native dependency workflow for compiled binaries:

```bash
# Print a JSON report for one or more binaries/directories
java -jar target/quarkus-app/quarkus-run.jar dynamic-libs --json /path/to/binary

# Write the report to a file
java -jar target/quarkus-app/quarkus-run.jar dynamic-libs --json --output dynamic-libs.json /path/to/bin-dir
```

How it works:

1. Runs `ldd` against each target binary.
2. Parses dynamically linked shared libraries.
3. Maps each library path back to an owning package with `dpkg -S` or `rpm -qf` when those package managers are available.
4. Emits Package URLs (PURLs) for managed OS-package libraries, for example:
   - `pkg:deb/libssl3?arch=amd64`
   - `pkg:rpm/openssl-libs@3.0.7-27.el9?arch=x86_64`
5. Marks libraries as unmanaged when no package owner can be resolved.

Platform support and caveats:

- **Linux with dpkg**: Supported. Debian/Ubuntu-owned libraries are reported with `packageType: "deb"` and `packageUrl` values.
- **Linux with rpm**: Supported. RHEL/Fedora/SUSE-owned libraries are reported with `packageType: "rpm"`, parsed package versions, architecture qualifiers, and `packageUrl` values.
- **Windows with Git Bash/MSYS/Cygwin `ldd`**: Partially supported. DLL links can be detected when `ldd` is available, but Windows package ownership is not resolved through `dpkg`/`rpm`, so findings are usually `managed: false` with `packageUrl: null`.
- **Native Windows DLL/package ownership**: Not currently implemented. Mapping DLLs to WinGet/MSIX/registry package metadata and generating Windows-specific PURLs would require a Windows resolver such as `dumpbin`, `objdump`, or Dependencies.exe plus package metadata lookups.
- **macOS**: Not currently supported by this subcommand because it uses `ldd`. A macOS implementation would need an `otool -L`-based resolver.

The `dynamic-libs` report is currently a separate JSON report, not merged into the generated CycloneDX SBOM files.

**SBOM Features:**

- Automatic multi-module aggregation for Maven and Gradle
- JSON output for most generators; Ruby emits CycloneDX XML
- Version detection and reporting
- Dry-run mode to preview commands
- **Multi-SBOM merging**: Enabled with `--merge` to combine SBOMs from different build systems
- **Intelligent naming**: Uses actual project names instead of generic prefixes (e.g., `quarkus-app-bom.json` instead of `maven-bom.json`)

## Use Cases

### CI/CD Integration

The tool includes GitHub Actions workflows for automated builds:

```yaml
# .github/workflows/ci.yml includes:
- JVM build: Creates uber-jar artifact
- Native build: Creates GraalVM native executable
- Artifact retention: 7 days
```

### Security Auditing

Generate SBOMs for security compliance and vulnerability scanning:

```bash
# Generate SBOM and pipe to vulnerability scanner
java -jar target/quarkus-app/quarkus-run.jar sbom --output ./reports
grype sbom:./reports/*-bom.json
```

### Project Analysis

Analyze legacy projects to understand technology stack:

```bash
# Scan unknown project
cd /path/to/legacy-project
java -jar /path/to/omnibom.jar scan --json analysis.json

# Review detected tools and file distribution
cat analysis.json
```

## Development

### Running in Dev Mode

```bash
./mvnw quarkus:dev
```

In dev mode, you can test commands directly:

```bash
# In another terminal
curl http://localhost:8080 # (if web endpoints added)

# Or use the CLI directly
./mvnw quarkus:dev -Dquarkus.args="scan"
./mvnw quarkus:dev -Dquarkus.args="sbom --dry-run"
```

### Building

```bash
# JVM package (uber-jar)
./mvnw clean package -Dquarkus.package.jar.type=uber-jar

# Native executable (requires GraalVM)
./mvnw clean package -Dnative

# Native in container (no local GraalVM needed)
./mvnw clean package -Dnative -Dquarkus.native.container-build=true
```

## Configuration

Application configuration in `src/main/resources/application.properties`:

```properties
quarkus.package.jar.type=uber-jar
quarkus.banner.enabled=false
```

## JSON Output Schema

The `scan` command with `--json` produces output like:

```json
{
  "buildTools": {
    "maven": {
      "detected": true,
      "versionInfo": "Apache Maven 3.9.6\n..."
    },
    "gradle": {
      "detected": false,
      "versionInfo": ""
    }
  },
  "multiModuleBuilds": {
    "maven": true,
    "gradle": false
  },
  "sourceFiles": {
    ".java": {
      "count": 147,
      "percentage": 68.37
    }
  }
}
```

## Contributing

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Install pre-commit hooks (`pre-commit install`)
4. Commit your changes (`git commit -m 'Add amazing feature'`)
5. Push to the branch (`git push origin feature/amazing-feature`)
6. Open a Pull Request

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Author

Created by **hoggmania** (<hoggmania@gmail.com>)

---
