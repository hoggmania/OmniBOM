# CycloneDX SBOM Generators and Plugins by Language

## Project Integration Notes (omnibom)

- The CLI auto-detects build systems and uses the most appropriate generator for each ecosystem.
- CycloneDX JSON is the default for most tools; `cyclonedx-ruby` emits XML, and Go uses `go list -m -json all` (module inventory, not CycloneDX).
- For multi-language repos, all detected systems are processed in a single run.
- Output directory defaults to `generated-sboms/` (configurable via `--output`).
- `--merge` combines multiple SBOMs into `merged-bom.json`.
- Commands run in the directory containing the build file (not the repo root).
- Wildcard .NET project detection is supported (e.g., `*.csproj`).
- Standalone binaries and ecosystems without dedicated CycloneDX tools are scanned with Syft (CycloneDX JSON).
- Dynamic native-library detection is available separately via `dynamic-libs`; it is Linux-first (`ldd` + `dpkg -S`/`rpm -qf`) and emits PURLs for package-managed libraries when ownership can be resolved.
- `dynamic-libs` has limited Windows support: Git Bash/MSYS/Cygwin `ldd` can detect DLL links, but native Windows package ownership/PURL generation is not implemented, so Windows DLL findings are generally unmanaged with `packageUrl: null`.
- `dynamic-libs` does not currently merge its findings into the CycloneDX SBOM output; treat it as a companion JSON report for native runtime dependency evidence.
- JSON logs and summaries are written when `--json` is set.
- Missing tools can be installed with `--allow-tool-install`.

## CycloneDX SBOM Generators and Plugins by Language

This table lists the primary CycloneDX-supported tools for generating Software Bill of Materials (SBOMs) across major programming languages and ecosystems.

| Language / Ecosystem | Tool / Plugin | Latest Version & Schema Support | Notes on Integration & CI/CD Usage |
|----------------------|--------------|---------------------------------|------------------------------------|
| **Java (Maven)** | [`org.cyclonedx:cyclonedx-maven-plugin`](https://github.com/CycloneDX/cyclonedx-maven-plugin) | Version ~2.9.x; supports CycloneDX schema v1.5+ | Add plugin to `pom.xml`. Common goals: `makeBom`, `makeAggregateBom`. Integrate into Maven build lifecycle (e.g., at `package`). Produces JSON or XML SBOMs. |
| **Java (Gradle)** | [`org.cyclonedx.bom` Gradle plugin](https://plugins.gradle.org/plugin/org.cyclonedx.bom) | Version ~3.1.x; supports JSON + XML | Apply via `plugins { id("org.cyclonedx.bom") version "3.1.x" }`. Run task `cyclonedxBom`. |
| **Go (Modules)** | [`cyclonedx-gomod`](https://github.com/CycloneDX/cyclonedx-gomod) | Supports CycloneDX spec v1.6 | CLI tool for Go modules. Install via `go install github.com/CycloneDX/cyclonedx-gomod/cmd/cyclonedx-gomod@latest`. |
| **Go (Library)** | [`cyclonedx-go`](https://github.com/CycloneDX/cyclonedx-go) | Supports multiple spec versions | Go library for consuming and producing SBOMs. Useful for embedding CycloneDX generation in custom Go tools. |
| **Python** | [`cyclonedx-bom`](https://github.com/CycloneDX/cyclonedx-python) | Supports venv, Poetry, Pipenv, requirements.txt | Install via `pip install cyclonedx-bom`. Run `cyclonedx-py` with the appropriate subcommand. |
| **Node.js (npm CLI)** | [`npm sbom`](https://docs.npmjs.com/cli/v11/commands/npm-sbom) | Emits CycloneDX or SPDX | Built into npm. Use `npm sbom --sbom-format=cyclonedx`. |
| **JavaScript / Webpack** | [`cyclonedx-webpack-plugin`](https://github.com/CycloneDX/cyclonedx-webpack-plugin) | Compatible with CycloneDX spec v1.5+ | Generates SBOMs for bundled JS/TS projects. Add to webpack config; emits CycloneDX JSON file. |
| **.NET / C#** | [`cyclonedx-dotnet`](https://github.com/CycloneDX/cyclonedx-dotnet) | Supports CycloneDX spec v1.4+ | Install via `dotnet tool install --global CycloneDX`. Generates BOM from .NET project or solution. |
| **Rust** | [`cargo-cyclonedx`](https://github.com/CycloneDX/cyclonedx-rust) | Supports v1.5+ | Generates SBOMs from Cargo dependencies. |
| **PHP** | [`cyclonedx-php-composer`](https://github.com/CycloneDX/cyclonedx-php-composer) | Supports v1.5+ | Integrates with Composer projects to output SBOMs. |
| **Ruby** | [`cyclonedx-ruby`](https://github.com/CycloneDX/cyclonedx-ruby-gem) | Supports v1.4+ | Creates SBOMs from Gemfile/Gemfile.lock. Emits XML by default. |
| **C/C++** | [`cyclonedx-cxx`](https://github.com/CycloneDX/cyclonedx-cxx) | Supports v1.5 | SBOM generator for C/C++ projects using compilation databases. |
| **Filesystem / Binaries** | [`syft`](https://github.com/anchore/syft) | Supports CycloneDX JSON | Scans directories, archives, and binaries. Useful as a fallback or for standalone artifacts. |
| **Multi-Ecosystem / CLI** | [`cyclonedx-cli`](https://github.com/CycloneDX/cyclonedx-cli) | Supports conversion, validation, merging, diffing | Multi-purpose CLI for validating or transforming SBOMs. Works across all supported ecosystems. |

---

## Recommended CI/CD Integration Practices

- Generate SBOMs as part of the build stage (e.g., Maven `package`, Gradle `build`, Go `build`, Python `install`).
- Use consistent SBOM formats and schema versions (preferably CycloneDX v1.5 or newer).
- Archive and version SBOMs (e.g., `app-name-version-cyclonedx.json`) as build artifacts.
- Feed SBOMs into downstream tools for vulnerability scanning, license compliance, and governance.
- Automate validation with `cyclonedx-cli validate --input-file bom.json`.
- Include metadata (commit hash, build time, tool version) for traceability.

## Troubleshooting Common Errors

- **Syntax error on token(s), misplaced construct(s):**
  - This usually means a misplaced or extra curly brace (`}`) or code outside a class in a Java file.
  - Ensure all methods and fields are inside the correct class (e.g., `EnvScannerCommand`).
  - The license block at the top of Java files must be inside a `/* ... */` comment, immediately followed by the `package` declaration.
  - No blank lines or stray characters should appear between the license comment and `package`.
- **SBOM not generated:**
  - Check that the required generator is installed and available on your PATH.
  - For standalone binaries and fallback scans, Syft must be installed: https://github.com/anchore/syft
- **Output directory not created:**
  - The tool attempts to create the output directory if it does not exist, but check permissions if this fails.

---

## References

- CycloneDX Tool Center: [https://cyclonedx.org/tool-center/](https://cyclonedx.org/tool-center/)
- CycloneDX Specification: [https://cyclonedx.org/specification/](https://cyclonedx.org/specification/)
- Official GitHub Organization: [https://github.com/CycloneDX](https://github.com/CycloneDX)
