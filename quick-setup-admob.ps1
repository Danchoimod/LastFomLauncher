# Quick Setup Script for AdMob SSV
# This script helps you quickly configure AdMob SSV integration

Write-Host @"
=======================================================
  🚀 AdMob SSV Quick Setup Script
=======================================================
"@ -ForegroundColor Cyan

# Function to read user input
function Get-UserInput {
    param(
        [string]$Prompt,
        [string]$Default = ""
    )
    
    if ($Default) {
        $input = Read-Host "$Prompt [$Default]"
        if ([string]::IsNullOrWhiteSpace($input)) {
            return $Default
        }
        return $input
    } else {
        return Read-Host $Prompt
    }
}

# Step 1: Collect information
Write-Host "`n📋 Step 1: Collecting Configuration Information`n" -ForegroundColor Yellow

$adUnitId = Get-UserInput "Enter your Rewarded Ad Unit ID (ca-app-pub-XXXXXXXX/YYYYYY)" ""
$vercelUrl = Get-UserInput "Enter your Vercel project URL" "https://your-project.vercel.app"

if ([string]::IsNullOrWhiteSpace($adUnitId)) {
    Write-Host "`n⚠️  Warning: Ad Unit ID is required for production!" -ForegroundColor Red
    Write-Host "Get it from: https://apps.admob.com`n" -ForegroundColor Yellow
    $useTestId = Read-Host "Use Test Ad Unit ID for now? (y/n)"
    
    if ($useTestId -eq "y") {
        $adUnitId = "ca-app-pub-3940256099942544/5224354917"
        Write-Host "✅ Using Test Ad Unit ID`n" -ForegroundColor Green
    } else {
        Write-Host "❌ Cancelled. Please get Ad Unit ID first.`n" -ForegroundColor Red
        exit 1
    }
}

# Step 2: Update getCoin.java
Write-Host "📝 Step 2: Updating getCoin.java...`n" -ForegroundColor Yellow

$getCoinPath = ".\app\src\main\java\org\levimc\launcher\ui\fragment\getCoin.java"

if (Test-Path $getCoinPath) {
    $content = Get-Content $getCoinPath -Raw
    
    # Replace Ad Unit ID
    $pattern = 'private static final String AD_UNIT_ID = "ca-app-pub-[0-9\-]+/[0-9]+";'
    $replacement = "private static final String AD_UNIT_ID = ""$adUnitId"";"
    
    $newContent = $content -replace $pattern, $replacement
    
    Set-Content -Path $getCoinPath -Value $newContent -NoNewline
    
    Write-Host "✅ Updated AD_UNIT_ID in getCoin.java`n" -ForegroundColor Green
} else {
    Write-Host "⚠️  getCoin.java not found at: $getCoinPath`n" -ForegroundColor Yellow
}

# Step 3: Save configuration
Write-Host "💾 Step 3: Saving configuration...`n" -ForegroundColor Yellow

$configContent = @"
# AdMob SSV Configuration (Generated: $(Get-Date -Format "yyyy-MM-dd HH:mm:ss"))

[ADMOB_CONFIG]
REWARDED_AD_UNIT_ID=$adUnitId
VERCEL_API_URL=$vercelUrl/api/verify-reward

[REWARD_CONFIG]
COINS_PER_AD=10
MAX_ADS_PER_DAY=10
DAILY_REWARD_COINS=3

[STATUS]
[x] Step 1: Collected Ad Unit ID
[x] Step 2: Updated getCoin.java
[ ] Step 3: Configure SSV URL in AdMob Console (MANUAL STEP REQUIRED)
[ ] Step 4: Verify FIREBASE_SERVICE_ACCOUNT in Vercel (MANUAL STEP REQUIRED)
[ ] Step 5: Test API endpoint
[ ] Step 6: Build and test app

[NEXT_STEPS]
1. Go to AdMob Console: https://apps.admob.com
2. Settings → Account → Apps → Select your app
3. Configure Server-Side Verification URL: $vercelUrl/api/verify-reward
4. Go to Vercel Dashboard: https://vercel.com
5. Check Environment Variables → FIREBASE_SERVICE_ACCOUNT is set
6. Run test script: .\test-admob-api.ps1 -VercelUrl "$vercelUrl"
7. Build app: .\gradlew assembleDebug
8. Test ads in app and verify coins are added

"@

Set-Content -Path ".\admob-config-generated.txt" -Value $configContent

Write-Host "✅ Configuration saved to: admob-config-generated.txt`n" -ForegroundColor Green

# Step 4: Next steps
Write-Host @"
=======================================================
  ✅ Setup Complete!
=======================================================

Configuration Summary:
  Ad Unit ID : $adUnitId
  Vercel URL : $vercelUrl
  SSV URL    : $vercelUrl/api/verify-reward

⚠️  MANUAL STEPS REQUIRED:

1. Configure SSV URL in AdMob Console:
   → https://apps.admob.com
   → Settings → Account → Apps → Your App
   → Add SSV URL: $vercelUrl/api/verify-reward

2. Verify Firebase Service Account in Vercel:
   → https://vercel.com/dashboard
   → Project Settings → Environment Variables
   → Check FIREBASE_SERVICE_ACCOUNT is set

3. Test API (optional):
   → .\test-admob-api.ps1 -VercelUrl "$vercelUrl"

4. Build app:
   → .\gradlew assembleDebug

5. Test ads and check Firebase for coins

📖 Full documentation: ADMOB_SSV_PRODUCTION_SETUP.md

=======================================================
"@ -ForegroundColor White

Write-Host "`nPress any key to exit..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
