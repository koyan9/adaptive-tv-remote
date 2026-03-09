param(
    [string]$Destination,
    [switch]$InitGit
)

$ErrorActionPreference = 'Stop'

$projectRoot = Split-Path -Parent $PSScriptRoot

if ([string]::IsNullOrWhiteSpace($Destination)) {
    throw 'Please provide -Destination <path>.'
}

$resolvedDestination = [System.IO.Path]::GetFullPath($Destination)

if (Test-Path $resolvedDestination) {
    $existingItems = Get-ChildItem -Force $resolvedDestination -ErrorAction SilentlyContinue
    if ($existingItems) {
        throw "Destination '$resolvedDestination' is not empty."
    }
} else {
    New-Item -ItemType Directory -Force -Path $resolvedDestination | Out-Null
}

$exclude = @('.git', 'target', '.idea', '.vscode')

Get-ChildItem -Force $projectRoot | Where-Object {
    $exclude -notcontains $_.Name
} | ForEach-Object {
    $targetPath = Join-Path $resolvedDestination $_.Name
    if ($_.PSIsContainer) {
        Copy-Item $_.FullName $targetPath -Recurse -Force
    } else {
        Copy-Item $_.FullName $targetPath -Force
    }
}

if ($InitGit) {
    Push-Location $resolvedDestination
    try {
        git init | Out-Null
    } finally {
        Pop-Location
    }
}

Write-Host "Standalone repository extracted to: $resolvedDestination"
if ($InitGit) {
    Write-Host 'Initialized a new Git repository.'
}



