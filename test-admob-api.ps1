# PowerShell script to test AdMob SSV API

param(
    [Parameter(Mandatory=$false)]
    [string]$VercelUrl = "https://lflauncher.vercel.app",
    
    [Parameter(Mandatory=$false)]
    [string]$UserId = "test_user_123",
    
    [Parameter(Mandatory=$false)]
    [switch]$Help
)

if ($Help) {
    Write-Host @"
=======================================================
  AdMob SSV API Test Script
=======================================================

Usage:
  .\test-admob-api.ps1 -VercelUrl <url> -UserId <user_id>

Examples:
  .\test-admob-api.ps1 -VercelUrl "hhttps://lflauncher.vercel.app" -UserId "FwmrB87VvhZaHhJYdBtqF8q8skv2"

Options:
  -VercelUrl    : Your Vercel project URL (required)
  -UserId       : Firebase user ID to test (optional)
  -Help         : Show this help message

=======================================================
"@
    exit 0
}

Write-Host "=======================================================`n" -ForegroundColor Cyan
Write-Host "  AdMob SSV API Test Script`n" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan

# Test 1: Check if API is alive
Write-Host "`n[TEST 1] Checking if API is accessible...`n" -ForegroundColor Yellow

try {
    $response = Invoke-WebRequest -Uri "$VercelUrl/api/verify-reward" -Method GET -ErrorAction Stop
    
    if ($response.StatusCode -eq 400) {
        Write-Host "✅ API is accessible (got expected 400 error for missing params)" -ForegroundColor Green
        Write-Host "Response: $($response.Content)`n" -ForegroundColor Gray
    } else {
        Write-Host "⚠️  Unexpected status code: $($response.StatusCode)" -ForegroundColor Yellow
        Write-Host "Response: $($response.Content)`n" -ForegroundColor Gray
    }
} catch {
    Write-Host "❌ Failed to connect to API" -ForegroundColor Red
    Write-Host "Error: $($_.Exception.Message)`n" -ForegroundColor Red
    Write-Host "Please check:" -ForegroundColor Yellow
    Write-Host "  1. Vercel URL is correct" -ForegroundColor Yellow
    Write-Host "  2. API is deployed on Vercel" -ForegroundColor Yellow
    Write-Host "  3. No typos in URL`n" -ForegroundColor Yellow
    exit 1
}

# Test 2: Check Firebase connection (simulate SSV callback without signature)
Write-Host "[TEST 2] Testing Firebase connection (will fail signature check)...`n" -ForegroundColor Yellow

$timestamp = [int][double]::Parse((Get-Date -UFormat %s))
$testParams = @{
    ad_network = "5450213213286189855"
    ad_unit = "ca-app-pub-3940256099942544/5224354917"
    user_id = $UserId
    reward_amount = "10"
    reward_item = "coins"
    timestamp = $timestamp
    transaction_id = "test_$(Get-Random -Minimum 1000000 -Maximum 9999999)"
    signature = "test_signature"
    key_id = "1"
}

$queryString = ($testParams.GetEnumerator() | ForEach-Object { "$($_.Key)=$($_.Value)" }) -join "&"
$testUrl = "$VercelUrl/api/verify-reward?$queryString"

try {
    $response = Invoke-WebRequest -Uri $testUrl -Method GET -ErrorAction Stop
    
    Write-Host "⚠️  Unexpected success (signature should fail)" -ForegroundColor Yellow
    Write-Host "Response: $($response.Content)`n" -ForegroundColor Gray
} catch {
    $statusCode = $_.Exception.Response.StatusCode.value__
    
    if ($statusCode -eq 401) {
        Write-Host "✅ Signature verification is working (got expected 401 error)" -ForegroundColor Green
        Write-Host "This is expected - real AdMob callbacks will have valid signatures`n" -ForegroundColor Gray
    } elseif ($statusCode -eq 500) {
        Write-Host "⚠️  Server error (500) - Firebase might not be initialized" -ForegroundColor Yellow
        Write-Host "Please check:" -ForegroundColor Yellow
        Write-Host "  1. FIREBASE_SERVICE_ACCOUNT env var is set in Vercel" -ForegroundColor Yellow
        Write-Host "  2. Service account JSON is valid" -ForegroundColor Yellow
        Write-Host "  3. Vercel logs for detailed error`n" -ForegroundColor Yellow
    } else {
        Write-Host "⚠️  Unexpected error: $statusCode" -ForegroundColor Yellow
        Write-Host "Error: $($_.Exception.Message)`n" -ForegroundColor Red
    }
}

# Summary
Write-Host "=======================================================`n" -ForegroundColor Cyan
Write-Host "  Test Summary`n" -ForegroundColor Cyan
Write-Host "=======================================================" -ForegroundColor Cyan
Write-Host "API URL    : $VercelUrl" -ForegroundColor White
Write-Host "Test User  : $UserId" -ForegroundColor White
Write-Host "`nNext Steps:" -ForegroundColor Yellow
Write-Host "  1. Configure SSV URL in AdMob Console" -ForegroundColor White
Write-Host "  2. Replace Test Ad Unit ID with Real Ad Unit ID" -ForegroundColor White
Write-Host "  3. Build and test app with real ads" -ForegroundColor White
Write-Host "  4. Check Vercel logs: $VercelUrl/_logs`n" -ForegroundColor White
Write-Host "=======================================================" -ForegroundColor Cyan
