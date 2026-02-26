# File Closer

![Build](https://github.com/maruryota/file_closer_plugin/workflows/Build/badge.svg)
[![Version](https://img.shields.io/jetbrains/plugin/v/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)
[![Downloads](https://img.shields.io/jetbrains/plugin/d/MARKETPLACE_ID.svg)](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID)

<!-- Plugin description -->
**File Closer** is a JetBrains IDE plugin that lets you bulk-close open editor tabs grouped by file extension.

When dozens of tabs pile up, closing them one by one is tedious. File Closer gives you a tree view that groups every open file by its extension, so you can close an entire group with a single click.

### Features

- **Extension-grouped tree view** &mdash; Open files are listed under their extension (e.g. `.kt`, `.xml`, `.md`) with file counts.
- **Bulk close** &mdash; Click the trash icon on an extension group to close all files of that type at once.
- **Single close** &mdash; Expand a group and click the trash icon on an individual file to close just that one.
- **Unsaved-file safety** &mdash; If a file has unsaved changes, a save/discard/cancel dialog appears before closing.
- **Live updates** &mdash; The tree refreshes automatically when you open or close files.
<!-- Plugin description end -->

## Installation

- Using the IDE built-in plugin system:

  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>Marketplace</kbd> > <kbd>Search for "File Closer"</kbd> >
  <kbd>Install</kbd>

- Using JetBrains Marketplace:

  Go to [JetBrains Marketplace](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID) and install it by clicking the <kbd>Install to ...</kbd> button in case your IDE is running.

  You can also download the [latest release](https://plugins.jetbrains.com/plugin/MARKETPLACE_ID/versions) from JetBrains Marketplace and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

- Manually:

  Download the [latest release](https://github.com/maruryota/file_closer_plugin/releases/latest) and install it manually using
  <kbd>Settings/Preferences</kbd> > <kbd>Plugins</kbd> > <kbd>⚙️</kbd> > <kbd>Install plugin from disk...</kbd>

## Usage

1. Open the **File Closer** tool window from the right sidebar.
2. Open files are grouped by extension. Click a group to expand/collapse it.
3. Click the trash icon on an **extension group** to close all files of that type.
4. Click the trash icon on a **single file** to close just that file.

## Development

### Prerequisites

- JDK 17+

```bash
# If only JDK 11 is on PATH, set JAVA_HOME explicitly
export JAVA_HOME=/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
```

### Build

```bash
./gradlew buildPlugin
```

Artifacts are generated in `build/distributions/`.

### Test

```bash
./gradlew test
```

### Run IDE (sandbox)

```bash
./gradlew runIde
```

A sandboxed IntelliJ IDEA instance launches with the plugin pre-installed.

---
Plugin based on the [IntelliJ Platform Plugin Template][template].

[template]: https://github.com/JetBrains/intellij-platform-plugin-template
