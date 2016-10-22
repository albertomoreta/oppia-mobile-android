#!/usr/bin/env bash
if [ "${TEST_SUITE}" = "connectedAndroidTest" ]
then
	echo no | android create avd --force -n test -t $1 --abi $2
	emulator -avd test -no-audio -no-window &
	android-wait-for-emulator
	adb shell input keyevent 82 &
	adb shell pm grant org.digitalcampus.mobile.learning android.permission.READ_PHONE_STATE
  	adb shell pm grant org.digitalcampus.mobile.learning android.permission.READ_EXTERNAL_STORAGE
  	adb shell pm grant org.digitalcampus.mobile.learning android.permission.WRITE_EXTERNAL_STORAGE
fi
   