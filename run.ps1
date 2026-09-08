Param(
    [string]$MainClass = 'com.example.xpath.Main'
)

$here = Split-Path -Parent $MyInvocation.MyCommand.Definition
Push-Location $here

# Preferred defaults from README when environment variables are not set
$defaultJavaHome = 'F:\\java\\openlogic-openjdk-17.0.18+8-windows-x64'
$defaultMvnHome  = 'F:\\java\\apache-maven-3.9.13'

# Respect existing environment variables, otherwise fall back to README values
$env:JAVA_HOME = if ($env:JAVA_HOME) { $env:JAVA_HOME } else { $defaultJavaHome }
$env:MVN_HOME  = if ($env:MVN_HOME)  { $env:MVN_HOME }  else { $defaultMvnHome }

# Prepend JAVA_HOME and MVN_HOME bin directories to PATH for this session
function Prepend-ToPath($dir) {
    if (-not $dir) { return }
    $bin = Join-Path $dir 'bin'
    if (Test-Path $bin) {
        $current = [Environment]::GetEnvironmentVariable('PATH', 'Process')
        # only add if not already present
        if (-not ($current -split ';' | Where-Object { $_ -eq $bin })) {
            [Environment]::SetEnvironmentVariable('PATH', "$bin;$current", 'Process')
        }
    }
}

Prepend-ToPath $env:JAVA_HOME
Prepend-ToPath $env:MVN_HOME

# Diagnostic: show resolved locations (helpful for debugging)
Write-Host "Resolved JAVA_HOME = $($env:JAVA_HOME)"
Write-Host "Resolved MVN_HOME  = $($env:MVN_HOME)"
Write-Host "Checking for mvnw.cmd and mvn on PATH..."

# Check for mvnw in workspace or mvn in PATH or MVN_HOME bin
$mvnwPresent = Test-Path (Join-Path $here 'mvnw.cmd')
$mvnCmd = Get-Command mvn -ErrorAction SilentlyContinue
$mvnHomeCmd = Test-Path (Join-Path $env:MVN_HOME 'bin\mvn.cmd')
if (-not ($mvnwPresent -or $mvnCmd -or $mvnHomeCmd)) {
    Write-Error "Maven not found. Ensure Maven is installed, MVN_HOME is correct, or add the Maven wrapper (mvnw) to the project."
    Write-Host "Looked for:"
    Write-Host "  mvnw.cmd at: $($here)\mvnw.cmd -> $mvnwPresent"
    Write-Host "  mvn on PATH -> $([bool]$mvnCmd)"
    Write-Host "  mvn.cmd at MVN_HOME\bin -> $($env:MVN_HOME)\bin\mvn.cmd -> $mvnHomeCmd"
    Pop-Location
    exit 1
}

function Run-Maven {
    param(
        [Parameter(ValueFromRemainingArguments=$true)]
        [string[]]$Arguments
    )

    if (Test-Path ".\mvnw.cmd") {
        Write-Host "Using mvnw.cmd"
        & .\mvnw.cmd @Arguments
    } elseif (Get-Command mvn -ErrorAction SilentlyContinue) {
        Write-Host "Using mvn from PATH"
        & mvn @Arguments
    } else {
        Write-Error "Maven not found. Install Maven or add it to PATH, or add the Maven wrapper (mvnw)."
        Pop-Location
        exit 1
    }
    if ($LASTEXITCODE -ne 0) {
        Pop-Location
        exit $LASTEXITCODE
    }
}

Write-Host "Packaging project..."
Run-Maven clean package -DskipTests

Write-Host "Running Main: $MainClass"

# Prefer to run the compiled classes directly using the configured JAVA_HOME to avoid
# quoting/argument issues with the Maven exec invocation.
$javaExe = Join-Path $env:JAVA_HOME 'bin\java.exe'
if (-not (Test-Path $javaExe)) {
    $javaExe = 'java'
}

$classesDir = Join-Path $here 'target\classes'
if (-not (Test-Path $classesDir)) {
    Write-Error "Expected classes directory not found: $classesDir"
    Pop-Location
    exit 1
}

Write-Host "Invoking java: $javaExe -cp $classesDir $MainClass"
& $javaExe -cp $classesDir $MainClass
if ($LASTEXITCODE -ne 0) {
    Pop-Location
    exit $LASTEXITCODE
}

Pop-Location
