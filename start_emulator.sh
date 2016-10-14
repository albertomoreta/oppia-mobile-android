if [ "${TEST_SUITE}" = "connectedAndroidTest" ]
then
	echo no | android create avd --force -n test -t android-23 --abi armeabi-v7a
	emulator -avd test -no-skin -no-audio -no-window &
	android-wait-for-emulator
	adb shell input keyevent 82 &
	adb shell pm grant org.digitalcampus.mobile.learning android.permission.READ_PHONE_STATE
  	adb shell pm grant org.digitalcampus.mobile.learning android.permission.READ_EXTERNAL_STORAGE
  	adb shell pm grant org.digitalcampus.mobile.learning android.permission.WRITE_EXTERNAL_STORAGE
fi
   