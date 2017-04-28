
:argument: apk adb package
:ex: sh grant_permissions.sh <path_to_adb> <package>

set adb=%1
set package=%2

if "%2"=="" (
	echo "No parameters found, run with sh grant_permissions.sh <path_to_adb> <package>"
	exit 
)

"%adb%" devices | findstr /v "List of devices"> devices.txt

setlocal EnableDelayedExpansion

set i=0
FOR /F %%a IN (devices.txt) DO (
    set /A i+=1
    set devices[!i!]=%%a
    echo !devices[%%i]!
)
del devices.txt
set n=%i%

FOR /L %%i IN (1,1,%n%) DO (
	echo Setting permissions to device !devices[%%i]! for package %package%
    "%adb%" -s !devices[%%i]! shell pm grant %package% android.permission.READ_PHONE_STATE
    "%adb%" -s !devices[%%i]! shell pm grant %package% android.permission.WRITE_EXTERNAL_STORAGE
    "%adb%" -s !devices[%%i]! shell pm grant %package% android.permission.SET_ANIMATION_SCALE
)	