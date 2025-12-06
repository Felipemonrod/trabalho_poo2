param(
    [ValidateSet("cli", "gui", "dash")]
    [string]$Mode = "cli",
    [string]$JavaFxLib = "",
    [string]$Modules = "javafx.controls,javafx.fxml",
    [string[]]$JavaArgs = @()
)

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$binPath = Join-Path $repoRoot "bin"
$libRoot = Join-Path $repoRoot "lib"

if (-not (Test-Path $binPath)) {
    throw "Pasta 'bin' não encontrada. Execute scripts/compile.ps1 antes."
}

if (-not (Test-Path $libRoot)) {
    throw "Pasta de bibliotecas '$libRoot' não encontrada."
}

if ([string]::IsNullOrWhiteSpace($JavaFxLib)) {
    $javaFxSdk = Get-ChildItem -Directory -Path $libRoot -Filter "javafx-sdk*" -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
    if ($javaFxSdk) {
        $JavaFxLib = Join-Path $javaFxSdk.FullName "lib"
    }
}

if ([string]::IsNullOrWhiteSpace($JavaFxLib)) {
    throw "JavaFX SDK não localizado. Baixe em https://gluonhq.com/products/javafx/ e extraia para 'lib\\javafx-sdk-XX', ou informe o caminho usando -JavaFxLib."
}

if (-not (Test-Path $JavaFxLib)) {
    throw "O caminho informado para JavaFX ('$JavaFxLib') não existe. Verifique o parâmetro -JavaFxLib."
}

$mysqlJar = Get-ChildItem -Path $libRoot -Filter "mysql-connector-*.jar" -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
if (-not $mysqlJar) {
    throw "MySQL Connector/J não encontrado em '$libRoot'. Baixe o .jar em https://dev.mysql.com/downloads/connector/j/ e salve nessa pasta."
}

$modulePath = $JavaFxLib
$classPath = ($binPath + ";" + (Join-Path $libRoot "*"))

$arguments = @(
    "-cp", $classPath,
    "--module-path", $modulePath,
    "--add-modules", $Modules
)

if ($JavaArgs -and $JavaArgs.Count -gt 0) {
    $arguments += $JavaArgs
}

$arguments += "App"

switch ($Mode) {
    "gui" { $arguments += "--gui" }
    "dash" { $arguments += "--dash" }
}

& java @arguments
if ($LASTEXITCODE -ne 0) {
    throw "java retornou código $LASTEXITCODE."
}
