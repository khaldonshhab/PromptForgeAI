$ErrorActionPreference = "Stop"

$RepoRoot = if ($PSScriptRoot) { $PSScriptRoot } else { "C:\PromptForgeAI" }
$BackendDir = Join-Path $RepoRoot "backend"
$EnvPath = Join-Path $BackendDir ".env"
$BackendPort = 8787
$LocalBackendUrl = "http://localhost:$BackendPort"
$EmulatorBackendUrl = "http://10.0.2.2:$BackendPort"

Write-Host "==================================================" -ForegroundColor Cyan
Write-Host "PromptForgeAI local-only Android + Node verification" -ForegroundColor Cyan
Write-Host "==================================================" -ForegroundColor Cyan

function Require-Tool {
    param([string]$Name, [string]$Hint)
    if (-not (Get-Command $Name -ErrorAction SilentlyContinue)) {
        throw "Required tool not found: $Name. $Hint"
    }
}

function Set-ProcessEnvironmentFromEnvFile {
    param([string]$Path)
    if (-not (Test-Path $Path)) { return }
    Get-Content $Path | ForEach-Object {
        if ($_ -match "^\s*#" -or $_ -match "^\s*$") { return }
        $parts = $_ -split "=", 2
        if ($parts.Count -eq 2) {
            $name = $parts[0].Trim()
            $value = $parts[1].Trim()
            [System.Environment]::SetEnvironmentVariable($name, $value, "Process")
        }
    }
}

function Get-AndroidBackendUrl {
    if (-not (Get-Command adb -ErrorAction SilentlyContinue)) {
        return $EmulatorBackendUrl
    }

    $deviceState = & adb devices 2>$null
    if ($deviceState -match "device\s*$|emulator\s*$") {
        & adb reverse tcp:$BackendPort tcp:$BackendPort 2>$null | Out-Null
        return $LocalBackendUrl
    }

    return $EmulatorBackendUrl
}

# 1) Tool checks
Write-Host "[1/7] Checking required tools..." -ForegroundColor Yellow
Require-Tool -Name "node" -Hint "Install Node.js 20+ first."
Require-Tool -Name "java" -Hint "Install Java 17 first."
Require-Tool -Name "gradle" -Hint "Install Gradle 9.x or use the Android Gradle plugin toolchain."

# 2) Ensure backend env exists
Write-Host "[2/7] Ensuring backend .env exists..." -ForegroundColor Yellow
if (-not (Test-Path $EnvPath)) {
    $template = @'
PORT=8787
AI_PROVIDER=gemini
AI_API_URL=https://generativelanguage.googleapis.com/v1beta
AI_API_MODE=gemini
AI_API_KEY=PASTE_GEMINI_API_KEY_HERE
AI_MODEL=gemini-2.5-flash
PF_AUTH_SECRET=REPLACE_WITH_A_LONG_RANDOM_SECRET
PF_ADMIN_USER=admin
PF_ADMIN_PASSWORD=YourStrongPassword123
PF_ADMIN_PASSWORD_HASH=REPLACE_WITH_HASH_FROM_NODE_SCRIPTS_HASH_PASSWORD_MJS
PF_DATA_DIR=C:/PromptForgeAI/backend/data
MAX_BODY_BYTES=1200000
RATE_LIMIT_PER_MINUTE=20
'@
    Set-Content -Path $EnvPath -Value $template -Encoding UTF8
    Write-Host "Created backend/.env from the local-only template." -ForegroundColor Yellow
    throw "Update backend/.env with your real Gemini key and admin password hash, then re-run this script."
}

# 3) Load env values
Write-Host "[3/7] Loading environment variables..." -ForegroundColor Yellow
Set-ProcessEnvironmentFromEnvFile -Path $EnvPath

if (-not $env:AI_API_KEY -or $env:AI_API_KEY -match "PASTE_GEMINI_API_KEY_HERE|REPLACE_WITH") {
    throw "Set a valid AI_API_KEY in backend/.env before running verification."
}
if (-not $env:PF_AUTH_SECRET -or $env:PF_AUTH_SECRET -match "REPLACE_WITH") {
    throw "Set PF_AUTH_SECRET in backend/.env before running verification."
}
if (-not $env:PF_ADMIN_PASSWORD_HASH -or $env:PF_ADMIN_PASSWORD_HASH -match "REPLACE_WITH") {
    throw "Set PF_ADMIN_PASSWORD_HASH by running scripts/hash-password.mjs before running verification."
}
if (-not $env:PF_ADMIN_PASSWORD -or $env:PF_ADMIN_PASSWORD -match "REPLACE_WITH") {
    throw "Set PF_ADMIN_PASSWORD in backend/.env to match the password used when generating PF_ADMIN_PASSWORD_HASH."
}

# 4) Install backend deps and start server
Write-Host "[4/7] Installing backend dependencies..." -ForegroundColor Yellow
Push-Location $BackendDir
npm install
Pop-Location

Write-Host "[4/7] Starting local backend on port $BackendPort..." -ForegroundColor Yellow
$backendProcess = Start-Process -FilePath "node" -ArgumentList "server.mjs" -WorkingDirectory $BackendDir -PassThru -NoNewWindow
Start-Sleep -Seconds 4

# 5) Backend health and real generation
Write-Host "[5/7] Health-checking the local backend..." -ForegroundColor Yellow
$health = Invoke-RestMethod -Method Get -Uri "$LocalBackendUrl/health"
$health | ConvertTo-Json -Depth 5
if (-not $health.ok) {
    throw "Local backend health check failed."
}

Write-Host "[5/7] Verifying real Gemini generation with Arabic input..." -ForegroundColor Yellow
$body = @{
    idea = "أنشئ صورة سينمائية لمسلسل عربي في مدينة دمشق ليلاً، إضاءة دافئة، طابع درامي، شرفة قديمة، شخصيات واقعية"
    platform = "Gemini"
    task = "Image prompt"
    language = "ar"
} | ConvertTo-Json -Compress

$promptResponse = Invoke-RestMethod -Method Post -Uri "$LocalBackendUrl/v1/prompt" -ContentType "application/json" -Body $body
$promptResponse | ConvertTo-Json -Depth 10

if (-not $promptResponse.prompt) {
    throw "Gemini generation returned no prompt."
}

if ($promptResponse.prompt -match "[\u0600-\u06FF]") {
    Write-Host "Warning: the prompt still contains Arabic text; review the output for a clean English prompt." -ForegroundColor Yellow
}

# 6) Android build and install
Write-Host "[6/7] Building Android debug APK..." -ForegroundColor Yellow
Push-Location $RepoRoot
gradle :app:assembleDebug
Pop-Location

$apkPath = Join-Path $RepoRoot "app\build\outputs\apk\debug\app-debug.apk"
$androidBackendUrl = Get-AndroidBackendUrl
Write-Host "Android local backend URL: $androidBackendUrl" -ForegroundColor Green

if (Test-Path $apkPath) {
    if (Get-Command adb -ErrorAction SilentlyContinue) {
        $deviceLines = & adb devices 2>$null
        if ($deviceLines -match "device\s*$|emulator\s*$") {
            Write-Host "[6/7] Installing APK to the connected device/emulator..." -ForegroundColor Yellow
            & adb install -r $apkPath
        }
    }
}

# 7) Admin login verification
Write-Host "[7/7] Verifying local admin login..." -ForegroundColor Yellow
$adminBody = @{
    username = $env:PF_ADMIN_USER
    password = $env:PF_ADMIN_PASSWORD
} | ConvertTo-Json -Compress

$adminResponse = Invoke-RestMethod -Method Post -Uri "$LocalBackendUrl/admin/login" -ContentType "application/json" -Body $adminBody
$adminResponse | ConvertTo-Json -Depth 10

if (-not $adminResponse.token) {
    throw "Admin login failed or token was not returned."
}

Write-Host "" 
Write-Host "==================================================" -ForegroundColor Green
Write-Host "Local-only verification passed up to the app runtime step." -ForegroundColor Green
Write-Host "Use the app with backend URL: $androidBackendUrl" -ForegroundColor Green
Write-Host "==================================================" -ForegroundColor Green

try {
    Stop-Process -Id $backendProcess.Id -Force -ErrorAction SilentlyContinue
} catch {}
