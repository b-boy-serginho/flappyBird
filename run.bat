@echo off
setlocal

REM === Flappy Bird - Script de compilación y ejecución ===

REM Detectar Java automáticamente
where java >nul 2>nul
if %errorlevel%==0 (
    set "JAVA_CMD=java"
    set "JAVAC_CMD=javac"
) else (
    set "JAVA_CMD=C:\Users\megus\.antigravity\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64\bin\java.exe"
    set "JAVAC_CMD=C:\Users\megus\.antigravity\extensions\redhat.java-1.54.0-win32-x64\jre\21.0.10-win32-x86_64\bin\javac.exe"
)

REM Ruta al repositorio Maven local
set "M2=%USERPROFILE%\.m2\repository"
set "LWJGL=%M2%\org\lwjgl"

REM Classpath: JARs de LWJGL + JOML + natives de Windows
set "CP=target\classes"
set "CP=%CP%;%LWJGL%\lwjgl\3.3.3\lwjgl-3.3.3.jar"
set "CP=%CP%;%LWJGL%\lwjgl-glfw\3.3.3\lwjgl-glfw-3.3.3.jar"
set "CP=%CP%;%LWJGL%\lwjgl-opengl\3.3.3\lwjgl-opengl-3.3.3.jar"
set "CP=%CP%;%LWJGL%\lwjgl-stb\3.3.3\lwjgl-stb-3.3.3.jar"
set "CP=%CP%;%LWJGL%\lwjgl\3.3.3\lwjgl-3.3.3-natives-windows.jar"
set "CP=%CP%;%LWJGL%\lwjgl-glfw\3.3.3\lwjgl-glfw-3.3.3-natives-windows.jar"
set "CP=%CP%;%LWJGL%\lwjgl-opengl\3.3.3\lwjgl-opengl-3.3.3-natives-windows.jar"
set "CP=%CP%;%LWJGL%\lwjgl-stb\3.3.3\lwjgl-stb-3.3.3-natives-windows.jar"
set "CP=%CP%;%M2%\org\joml\joml\1.10.5\joml-1.10.5.jar"

REM Compilar
echo [1/2] Compilando...
if not exist "target\classes" mkdir "target\classes"
dir /s /b src\main\java\*.java > target\sources.txt
"%JAVAC_CMD%" --release 17 -d target\classes -cp "%CP%" @target\sources.txt
if %errorlevel% neq 0 (
    echo ERROR: Fallo en la compilacion.
    pause
    exit /b 1
)

REM Ejecutar
echo [2/2] Ejecutando Flappy Bird...
"%JAVA_CMD%" -cp "%CP%" com.flappybird.AppFlappyBird

endlocal
