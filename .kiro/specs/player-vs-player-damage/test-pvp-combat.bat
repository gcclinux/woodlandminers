@echo off
REM Player-vs-Player Combat Testing Script
REM This script helps you quickly start the server and clients for testing

echo ========================================
echo Woodlanders PvP Combat Testing
echo ========================================
echo.
echo This script will help you test the PvP combat feature.
echo.
echo You need to run this script THREE times in separate windows:
echo   1. First run: Start the server
echo   2. Second run: Start Player A client
echo   3. Third run: Start Player B client
echo.
echo ========================================
echo.

:menu
echo Select what to start:
echo   1. Dedicated Server
echo   2. Game Client (Player A)
echo   3. Game Client (Player B)
echo   4. Exit
echo.
set /p choice="Enter your choice (1-4): "

if "%choice%"=="1" goto server
if "%choice%"=="2" goto clientA
if "%choice%"=="3" goto clientB
if "%choice%"=="4" goto end
echo Invalid choice. Please try again.
echo.
goto menu

:server
echo.
echo ========================================
echo Starting Dedicated Server...
echo ========================================
echo Server will start on port 25565
echo Keep this window open!
echo Press Ctrl+C to stop the server
echo ========================================
echo.
java -jar build\libs\woodlanders-server.jar
goto end

:clientA
echo.
echo ========================================
echo Starting Game Client (Player A)
echo ========================================
echo.
echo Instructions:
echo 1. Press ESC to open menu
echo 2. Go to "Player Name" and enter "PlayerA"
echo 3. Press ESC again, go to "Multiplayer" -^> "Connect to Server"
echo 4. Enter IP: localhost
echo 5. Enter Port: 25565
echo 6. Use WASD to move, SPACEBAR to attack
echo ========================================
echo.
java -jar build\libs\woodlanders-client.jar
goto end

:clientB
echo.
echo ========================================
echo Starting Game Client (Player B)
echo ========================================
echo.
echo Instructions:
echo 1. Press ESC to open menu
echo 2. Go to "Player Name" and enter "PlayerB"
echo 3. Press ESC again, go to "Multiplayer" -^> "Connect to Server"
echo 4. Enter IP: localhost
echo 5. Enter Port: 25565
echo 6. Use WASD to move, SPACEBAR to attack
echo ========================================
echo.
java -jar build\libs\woodlanders-client.jar
goto end

:end
echo.
echo Exiting...
pause
