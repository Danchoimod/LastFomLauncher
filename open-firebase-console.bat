@echo off
echo.
echo ========================================
echo   Opening Firebase Console...
echo ========================================
echo.
echo Project: lastfom-launcher
echo Action: Update Firestore Security Rules
echo.
echo IMPORTANT: Copy these rules and click PUBLISH
echo.
echo rules_version = '2';
echo service cloud.firestore {
echo   match /databases/{database}/documents {
echo     match /users/{userId} {
echo       allow read: if true;
echo       allow create: if request.resource.data.keys().hasAll(['lastAdDate', 'adsWatchedToday']);
echo       allow update: if request.resource.data.diff(resource.data).affectedKeys()
echo                        .hasOnly(['lastAdDate', 'adsWatchedToday', 'lastDailyReward', 'userId', 'coin']);
echo     }
echo     match /marketplace/{itemId} {
echo       allow read: if true;
echo       allow write: if true;
echo     }
echo   }
echo }
echo.
echo Opening browser...
start https://console.firebase.google.com/project/lastfom-launcher/firestore/rules
echo.
echo Done!
pause

