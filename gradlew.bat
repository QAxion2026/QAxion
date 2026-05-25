@echo off
REM Lightweight wrapper for Windows environments.
set GRADLE_VERSION=8.7
set DIST_NAME=gradle-%GRADLE_VERSION%-bin
set DIST_URL=https://services.gradle.org/distributions/%DIST_NAME%.zip
set GRADLE_USER_HOME=%CD%\.gradle
set DIST_DIR=%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%
set GRADLE_BIN=%DIST_DIR%\%DIST_NAME%\bin\gradle.bat
if not exist "%GRADLE_BIN%" (
  mkdir "%DIST_DIR%" 2>nul
  powershell -Command "Invoke-WebRequest -Uri '%DIST_URL%' -OutFile '%DIST_DIR%\%DIST_NAME%.zip'"
  powershell -Command "Expand-Archive -Force '%DIST_DIR%\%DIST_NAME%.zip' '%DIST_DIR%'"
)
call "%GRADLE_BIN%" %*
