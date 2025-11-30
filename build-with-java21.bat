@echo off
echo Searching for Java 21...

REM Check common Java 21 installation locations
set JAVA21_HOME=

if exist "C:\Program Files\Java\jdk-21" (
    set JAVA21_HOME=C:\Program Files\Java\jdk-21
    goto found
)

if exist "C:\Program Files\Eclipse Adoptium\jdk-21" (
    set JAVA21_HOME=C:\Program Files\Eclipse Adoptium\jdk-21
    goto found
)

if exist "C:\Program Files\Microsoft\jdk-21" (
    set JAVA21_HOME=C:\Program Files\Microsoft\jdk-21
    goto found
)

if exist "%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21" (
    set JAVA21_HOME=%LOCALAPPDATA%\Programs\Eclipse Adoptium\jdk-21
    goto found
)

echo Java 21 not found in common locations.
echo Please set JAVA21_HOME manually or install Java 21.
echo.
echo You can download Java 21 from: https://adoptium.net/temurin/releases/?version=21
pause
exit /b 1

:found
echo Found Java 21 at: %JAVA21_HOME%
set JAVA_HOME=%JAVA21_HOME%
set PATH=%JAVA21_HOME%\bin;%PATH%

echo.
echo Building with Java 21...
echo JAVA_HOME=%JAVA_HOME%
echo.

call gradlew.bat :levelledmobs-plugin:shadowJar --no-daemon

if %ERRORLEVEL% EQU 0 (
    echo.
    echo Build successful! JAR file location:
    dir /b levelledmobs-plugin\build\libs\*.jar
) else (
    echo.
    echo Build failed!
)

pause

