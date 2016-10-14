if [ "${TEST_SUITE}" = "connectedAndroidTest" ]
then
	./gradlew createDebugCoverageReport --stacktrace
fi
   