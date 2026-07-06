$modsFolder = "$env:APPDATA\.minecraft\mods"
$jar = "build\libs\vesture-1.0.0-mc26.2.jar"

Write-Host "Building..." -ForegroundColor Cyan
.\gradlew build
if ($LASTEXITCODE -ne 0) {
    Write-Host "Build failed!" -ForegroundColor Red
    pause
    exit 1
}

Write-Host "Deploying to $modsFolder..." -ForegroundColor Cyan
Copy-Item $jar $modsFolder -Force
Write-Host "Done! Restart Minecraft to load changes." -ForegroundColor Green
pause
