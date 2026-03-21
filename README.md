# Veltrix

Veltrix is a visual node-based editor for building simple Minecraft Paper plugins without writing code.

## Run

```bash
mvn clean package
java -jar target/veltrix.jar
```

## Release Install

For GitHub Releases, download the distribution zip (not just the single jar):

`veltrix-<version>-dist.zip`

Unzip it, then run the jar from inside the extracted folder so the bundled `libs/` directory is available:

```bash
java -jar veltrix-<version>.jar
```

If you prefer a single raw jar file, use:

`veltrix-<version>-all.jar`

Run it directly:

```bash
java -jar veltrix-<version>-all.jar
```

Note: the `-all.jar` is platform-specific for JavaFX natives. Build and release it on each target OS (Linux/Windows/macOS) for best compatibility.

## What it does

- Build plugin logic using event, logic, action, and data nodes
- Export generated plugin source and `plugin.yml`
- Compile exported plugin with Maven

## v0.2.3 Highlights

Version 0.2.3 introduces a project-first workflow centered around the new home screen.

- New home screen listing all saved projects in one place
- Fast project creation directly from the home screen
- One-click open for existing projects from the home screen
- Automatic project autosave every 10 seconds while editing
- Improved project lifecycle actions (rename, duplicate, reveal folder, delete)
- Persistent app version display on the home screen header

## Home Screen Workflow (v0.2.3)

1. Start Veltrix and land on the home screen.
2. Create a project with name, package, and location.
3. Open a project card to continue editing.
4. Work normally in the editor while Veltrix autosaves every 10 seconds.
5. Use each project card menu for management actions (rename, duplicate, reveal folder, delete).

Notes:

- Autosave updates project data in the background while the editor is open.
- Deleting a project removes it from Veltrix storage and saved graph snapshot data, but does not remove files from the project folder itself.
