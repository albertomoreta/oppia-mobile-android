#!/usr/bin/env bash
if [ "${TEST_SUITE}" = "connectedAndroidTest" ]
then
	echo no | android create avd --force -n test -t $1 --abi $2 --skin WXGA720
	emulator -avd test -no-audio -no-window &
	android-wait-for-emulator
	adb shell input keyevent 82 &
fi
   