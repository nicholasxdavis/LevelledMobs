# How to Install Java 21 on Windows

## Option 1: Eclipse Adoptium (Recommended - Free)

1. **Download Java 21:**
   - Go to: https://adoptium.net/temurin/releases/?version=21
   - Select:
     - **Version:** 21 (LTS)
     - **Operating System:** Windows
     - **Architecture:** x64
     - **Package Type:** JDK
   - Click **Download**

2. **Install:**
   - Run the downloaded `.msi` installer
   - Follow the installation wizard
   - **Important:** Note the installation path (usually `C:\Program Files\Eclipse Adoptium\jdk-21.0.x+xx`)

3. **Verify Installation:**
   - Open Command Prompt or PowerShell
   - Run: `java -version`
   - You should see version 21.x.x

## Option 2: Microsoft Build of OpenJDK

1. **Download:**
   - Go to: https://learn.microsoft.com/en-us/java/openjdk/download
   - Download Java 21 for Windows x64

2. **Install:**
   - Run the installer
   - Follow the installation steps

## Option 3: Oracle JDK (Requires Account)

1. **Download:**
   - Go to: https://www.oracle.com/java/technologies/downloads/#java21
   - Sign in with Oracle account (free)
   - Download Windows x64 installer

2. **Install:**
   - Run the installer
   - Follow the installation steps

## After Installation:

1. **Set JAVA_HOME (if needed):**
   - Find where Java 21 was installed (usually `C:\Program Files\Java\jdk-21` or `C:\Program Files\Eclipse Adoptium\jdk-21.0.x+xx`)
   - The build script I created will automatically find it, but if it doesn't work, you can:
     - Right-click "This PC" → Properties → Advanced System Settings
     - Click "Environment Variables"
     - Under "System Variables", click "New"
     - Variable name: `JAVA_HOME`
     - Variable value: Path to your Java 21 installation (e.g., `C:\Program Files\Java\jdk-21`)

2. **Build the project:**
   - Run the `build-with-java21.bat` script I created
   - Or manually set JAVA_HOME and run: `gradlew.bat :levelledmobs-plugin:shadowJar`

## Quick Test:

After installing, open a new Command Prompt/PowerShell and run:
```
java -version
```

You should see something like:
```
openjdk version "21.0.x" ...
```

If you see version 25, you may need to:
- Close and reopen your terminal
- Or manually set JAVA_HOME to point to Java 21

