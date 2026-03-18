# Veltrix Wiki Home

Welcome to the Veltrix documentation.

Veltrix is a visual node-based editor for building Minecraft Paper plugins without writing code.

## Quick Start

Build and run Veltrix:

```bash
mvn clean package
java -jar target/veltrix-1.0.0.jar
```

## What You Can Do

- Build plugin logic using event, logic, action, and data nodes
- Export generated plugin source and `plugin.yml`
- Compile exported plugins with Maven

## Documentation Index

- [Node Reference](./NODES.md)
- [Latest Release Notes](../RELEASE.md)
- [Project README](../README.md)

## Core Concepts

- Execution wires control flow between event, logic, and action nodes.
- Data wires pass values between ports.
- Node categories:
  - Events
  - Logic
  - Actions
  - Data

## Need Help?

Start with the Node Reference to understand available nodes and port types, then check release notes for recently added features.
