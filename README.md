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
