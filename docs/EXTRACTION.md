# Extraction Guide

## Purpose

Use this guide when moving `adaptive-tv-remote` out of the current workspace into its own Git repository.

## Quick start

From the project directory:

```powershell
scripts\extract-repository.cmd -Destination C:\path\to\adaptive-tv-remote -InitGit
```

Or directly with PowerShell:

```powershell
.\scripts\extract-repository.ps1 -Destination C:\path\to\adaptive-tv-remote -InitGit
```

## What the script does

- copies the project contents to the destination directory
- excludes local-only folders such as `target/`, `.idea/`, and `.vscode/`
- optionally runs `git init` in the extracted directory

## After extraction

- review `README.md`, `pom.xml`, and package coordinates
- create a new remote repository
- push the extracted repository
- enable GitHub Actions
- run `mvnw.cmd -q test` in the extracted repository



