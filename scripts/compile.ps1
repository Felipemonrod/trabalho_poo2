param(
    [string]$JavaFxLib = "",
    [string]$Modules = "javafx.controls,javafx.fxml"
)

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$srcPath = Join-Path $repoRoot "src"
$binPath = Join-Path $repoRoot "bin"
$libRoot = Join-Path $repoRoot "lib"

if (-not (Test-Path $srcPath)) {
    throw "Origem '$srcPath' não encontrada."
}

if (-not (Test-Path $binPath)) {
    New-Item -ItemType Directory -Path $binPath | Out-Null
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
    throw "JavaFX SDK não localizado. Faça o download em https://gluonhq.com/products/javafx/ e extraia para 'lib\\javafx-sdk-XX'. Você também pode passar o caminho manualmente usando -JavaFxLib."
}

if (-not (Test-Path $JavaFxLib)) {
    throw "O caminho informado para JavaFX ('$JavaFxLib') não existe. Verifique o parâmetro -JavaFxLib."
}

$mysqlJar = Get-ChildItem -Path $libRoot -Filter "mysql-connector-*.jar" -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
if (-not $mysqlJar) {
    throw "MySQL Connector/J não encontrado em '$libRoot'. Baixe o arquivo JAR em https://dev.mysql.com/downloads/connector/j/ e coloque-o nessa pasta."
}

$javaFiles = Get-ChildItem -Path $srcPath -Recurse -Filter *.java | ForEach-Object { $_.FullName }
if (-not $javaFiles -or $javaFiles.Count -eq 0) {
    throw "Nenhum arquivo .java encontrado em '$srcPath'."
}

$modulePath = $JavaFxLib
$classPath = (Join-Path $libRoot "*")

$arguments = @(
    "-d", $binPath,
    "-encoding", "UTF-8",
    "-cp", $classPath,
    "--module-path", $modulePath,
    "--add-modules", $Modules
) + $javaFiles

Write-Host "Compilando" $javaFiles.Count "arquivos..."
& javac @arguments
if ($LASTEXITCODE -ne 0) {
    throw "javac retornou código $LASTEXITCODE."
}

Write-Host "Classes geradas em" $binPath
