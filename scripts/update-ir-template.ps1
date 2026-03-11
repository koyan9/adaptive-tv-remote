param(
    [string]$ConfigPath = "src/main/resources/application-real.yml",
    [string]$DocPath = "docs/INTEGRATIONS.md"
)

$lines = Get-Content -Path $ConfigPath
$irStart = $null
$indent = 0
for ($i = 0; $i -lt $lines.Count; $i++) {
    if ($lines[$i].Trim() -eq "ir-codes:") {
        $irStart = $i
        $indent = $lines[$i].IndexOf("i")
        break
    }
}
if ($irStart -eq $null) {
    throw "ir-codes section not found in $ConfigPath"
}

$irLines = @()
for ($j = $irStart; $j -lt $lines.Count; $j++) {
    $line = $lines[$j].TrimEnd()
    if ($j -eq $irStart) {
        $irLines += $line
        continue
    }
    if ($line -eq "") {
        $irLines += $line
        continue
    }
    $currentIndent = $lines[$j].IndexOf($line.TrimStart()[0])
    if ($currentIndent -le $indent) {
        break
    }
    $irLines += $line
}

$dedented = $irLines | ForEach-Object {
    if ($_.Length -gt $indent) {
        $_.Substring($indent)
    } else {
        $_.TrimStart()
    }
}

$startTag = "<!-- IR-CODES-START -->"
$endTag = "<!-- IR-CODES-END -->"
$doc = Get-Content -Path $DocPath -Raw
$block = @(
    $startTag,
    '```yml',
    ($dedented -join "`n"),
    '```',
    $endTag
) -join "`n"

$pattern = "(?s)" + [regex]::Escape($startTag) + ".*?" + [regex]::Escape($endTag)
$updated = [regex]::Replace($doc, $pattern, $block)
Set-Content -Path $DocPath -Value $updated -Encoding UTF8
