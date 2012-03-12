#!/bin/bash

exec ~/Android/android-sdk-macosx/platform-tools/adb -s emulator-5554 shell "sqlite3 /data/data/com.ja.saillog/databases/SLDB.db $@"
