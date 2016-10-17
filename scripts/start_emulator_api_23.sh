if [ "${TEST_SUITE}" = "connectedAndroidTest" ]
then
	echo no | android create avd --force -n test -t android-23 --abi armeabi-v7a
	emulator -avd test -no-skin -no-audio -no-window &
	android-wait-for-emulator
	adb -s emulator-5554 shell input keyevent 82 &
	adb -s emulator-5554 shell pm grant org.digitalcampus.mobile.learning android.permission.READ_PHONE_STATE
  	adb -s emulator-5554 shell pm grant org.digitalcampus.mobile.learning android.permission.READ_EXTERNAL_STORAGE
  	adb -s emulator-5554 shell pm grant org.digitalcampus.mobile.learning android.permission.WRITE_EXTERNAL_STORAGE
fi
   