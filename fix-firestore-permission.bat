rem Quick script to open Firebase Console to configure Firestore Rules
@echo off
echo.
echo ============================================
echo   Fix Firestore Permission Denied Error
echo ============================================
echo.
echo Loi: PERMISSION_DENIED: Missing or insufficient permissions
echo.
echo HUONG DAN:
echo.
echo 1. Mo Firebase Console:
echo    https://console.firebase.google.com/
echo.
echo 2. Chon project: lastfom-launcher
echo.
echo 3. Vao: Firestore Database ^> Rules
echo.
echo 4. Copy mot trong cac rules duoi day:
echo.
echo --------------------------------------------
echo CACH 1: CHO TESTING (CHO PHEP TAT CA)
echo --------------------------------------------
echo rules_version = '2';
echo service cloud.firestore {
echo   match /databases/{database}/documents {
echo     match /{document=**} {
echo       allow read, write: if true;
echo     }
echo   }
echo }
echo.
echo --------------------------------------------
echo CACH 2: BAO MAT (KHUYENG NGHI)
echo --------------------------------------------
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
echo 5. Nhan nut PUBLISH
echo.
echo 6. Doi 1-2 phut de rules duoc ap dung
echo.
echo 7. Test lai app
echo.
echo.
echo Xem chi tiet trong file: FIRESTORE_SECURITY_RULES_FIX.md
echo.
pause

