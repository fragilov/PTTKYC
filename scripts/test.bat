@echo off
chcp 65001 > nul
cd /d "%~dp0.."
echo =========================================================================
echo   KIEM THU TU DONG - HE THONG QUAN LY LINH KIEN VA GIO HANG
echo =========================================================================

if not exist bin mkdir bin

echo 1. Dang bien dich ma nguon va kiem thu...
javac -encoding UTF-8 -cp "lib/*;bin" -d "bin" src/main/java/com/linhkien/*.java src/main/java/com/linhkien/config/*.java src/main/java/com/linhkien/util/*.java src/main/java/com/linhkien/model/*.java src/main/java/com/linhkien/dao/*.java src/main/java/com/linhkien/service/*.java src/main/java/com/linhkien/api/*.java src/main/java/com/linhkien/ui/*.java src/test/java/com/linhkien/*.java

if errorlevel 1 (
    echo [LOI] Bien dich that bai!
    pause
    exit /b 1
)

copy /Y src\main\resources\* bin\ > nul

echo 2. Dang thuc thi bo kiem thu tu dong...
echo =========================================================================
java "-Dfile.encoding=UTF-8" -cp "bin;lib/*" com.linhkien.AppTest
echo =========================================================================
