@echo off
setlocal enabledelayedexpansion

REM Automatically obtain the currently connected emulator.
REM If there are multiple emulators, obtain the last one.
REM Once the simulator restarts, all these mappings will become invalid, and the process must be carried out again.
REM https://chat.deepseek.com/share/tef2wziqksfx3eiazg

set DEVICE=
for /f "tokens=1" %%i in ('adb devices ^| findstr /r "emulator-" ^| findstr /r "device$"') do (
    set DEVICE=%%i
)

if "!DEVICE!"=="" (
    echo No device found! Please check USB connection.
    pause
    exit /b
)

echo Current device: !DEVICE!
echo Configuring port reverse...

REM emall-server-cms
adb -s !DEVICE! reverse tcp:8070 tcp:8070
REM emall-server-client
adb -s !DEVICE! reverse tcp:8068 tcp:8068
REM file-server
adb -s !DEVICE! reverse tcp:8058 tcp:8058

echo Done!
pause