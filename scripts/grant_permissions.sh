#!/usr/bin/env bash
// grant_permissions.sh
#
# ex: sh grant_permissions.sh <path_to_adb> <package>
#

adb=$1
package=$2

if [ "$#" = 0 ]; then
    echo "No parameters found, run with sh grant_permissions.sh <path_to_adb> <package>"
    exit 0
fi

# get all the connected devices
devices=$($adb devices | grep -v 'List of devices' | cut -f1 | grep '.')

for device in $devices; do
    echo "Setting permissions to device " $device " for package " $package
    $adb -s $device shell pm grant $package android.permission.READ_PHONE_STATE
    $adb -s $device shell pm grant $package android.permission.WRITE_EXTERNAL_STORAGE
    $adb -s $device shell pm grant $package android.permission.SET_ANIMATION_SCALE
done