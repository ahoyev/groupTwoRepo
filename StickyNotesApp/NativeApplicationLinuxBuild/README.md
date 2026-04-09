# NativeJavaApp
Scaffold for creating double-clickable apps using Java

This project uses Apache Ant and JDK 25 to compile, bundle, and package a Java application into native installers (.dmg for macOS, .deb for Ubuntu, and .msi for Windows).

## Common Requirements (All Systems)
Before running the build, ensure the following are installed and configured:
- JDK 21 or 25: Must be installed. Although jpackage was introduced in JDK 14, JDK 21+ is recommended for modern macOS/Linux/Windows support.
- Apache Ant: Installed and available in your PATH.
- `JAVA_HOME`: This environment variable must point to your JDK installation directory.
  - Check via: `ant -version` and `java -version`
## macOS Setup
To build the .dmg installer, your Mac needs the following:
- Xcode Command Line Tools: Required for various build utilities.
  - Install via: `xcode-select --install`
- Icon Asset: A file named icon.icns must be in the project root.
- Note on Security: Since the app is not "Signed" with an Apple Developer Certificate, users may need to Right-Click > Open the app the first time to bypass the "Unidentified Developer" warning.
## Ubuntu / Debian Setup
To build the .deb package on Linux, jpackage requires external tools to create the Debian archive structure.

Run the following command to install the necessary packaging dependencies:

```Bash
sudo apt update
sudo apt install ant fakeroot dpkg-dev
```
- fakeroot: Allows the package to be built with correct file permissions without requiring root access.
- dpkg-dev: Provides the core utilities to create Debian packages.
- Icon Asset: A file named icon.png (512x512 recommended) must be in the project root.
## Windows Setup
To build the .msi installer on Windows, ensure the following:
- JDK 21 or 25: Must be installed with JAVA_HOME configured.
- Apache Ant: Installed and available in your PATH.
- WiX Toolset: Required by jpackage to create MSI installers.
  - Download and install from: https://wixtoolset.org/releases/
  - Version 3.11 or higher is recommended.
  - After installation, verify WiX is in your PATH by running: `candle -?`
- Icon Asset: A file named icon.ico must be in the project root.
- Note on Security: Windows may show a SmartScreen warning for unsigned installers. Users will need to click "More info" > "Run anyway" to install the app.
## Project Directory Structure
Ensure your project looks like this for the build.xml to find all resources:

```Plaintext
MyAppName/
├── src/
│   └── Main.java       # Your source code
├── icon.icns           # Required for macOS DMG
├── icon.png            # Required for Ubuntu DEB
├── icon.ico            # Required for Windows MSI
└── build.xml           # The Ant build script
```
## Usage Commands
Open a terminal in the project root and use the following targets:

| Command |	Description |
|-----|-----|
| ant |	The default; runs the package target. |
| ant package |	Automatically detects OS and builds .dmg (Mac), .deb (Linux), or .msi (Windows). |
| ant run	| Compiles and launches the app immediately for testing. |
| ant clean	| Deletes the build/ and dist/ folders to start fresh. |
| ant -p	| Displays a help menu of all available targets. |
## Troubleshooting
<table>
  <tr>
    <td>"jpackage&nbsp;not&nbsp;found": </td>
    <td>Ensure your JAVA_HOME is set to a JDK version 14 or higher. The jpackage binary resides in $JAVA_HOME/bin/.</td>
  </tr>
  <tr>
    <td>Linux Icon Error: </td>
    <td>If the Linux build fails, ensure icon.png is not just a renamed .icns or .jpg. It must be a valid PNG file.</td>
  </tr>
  <tr>
    <td>Windows "candle.exe not found": </td>
    <td>Install WiX Toolset 3.11+ and ensure it's in your PATH. jpackage uses WiX to create MSI installers on Windows.</td>
  </tr>
  <tr>
    <td>Permissions: </td>
    <td>If the generated .app or .deb won't execute, ensure you have the necessary write permissions in the dist/ directory.</td>
  </tr>
</table>

## Automated Releases
This project uses GitHub Actions to automatically build and distribute native installers. The workflow is split into two phases: Build and Release.

### The Build Phase (Continuous Integration)
Every time you push code to the main branch or open a Pull Request:
- GitHub starts a macOS runner, an Ubuntu runner, and a Windows runner.
- All systems compile the code and create their respective installers (.dmg, .deb, and .msi).
- The installers are saved as Artifacts in the GitHub Actions run summary for 90 days.
### The Release Phase (Continuous Deployment)
A formal GitHub Release is only triggered when you push a version tag.
This creates a permanent download page for users.

#### How to trigger a new release:
To release a new version (e.g., version 1.0.1), run the following commands in a terminal:

```Bash
# 1. Tag the current commit
git tag v1.0.1

# 2. Push the tag to GitHub
git push origin v1.0.1
```
### Downloading the Installers
- Go to the Releases section on the right-hand sidebar of this GitHub repository.
- Find the latest version (e.g., v1.0.1).
- Under Assets, you will find:
  - MyAntApp-Installer.dmg (for macOS)
  - MyAntApp-Linux.deb (for Ubuntu/Debian)
  - MyAntApp-Windows.msi (for Windows)

### Changing the Version Number
When ready to bump the version, remember to update the version number in **two** places to keep everything in sync:
- The Git Tag (the v1.0.1 above).
- The **app.version** property at the top of `build.xml`. This ensures that when the user installs the app, the OS sees the correct version number in the "About" or "Get Info" screens.
