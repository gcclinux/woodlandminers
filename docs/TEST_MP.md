## Option: Use Different Player Configs (Better for Testing)
### To have different player names/positions for each instance, modify the config directory:

## Terminal 1 - Server:

> java -jar build/libs/woodlanders-server.jar


## Terminal 2 - Player 1:

> XDG_CONFIG_HOME=/tmp/player1 java -jar build/libs/woodlanders-client.jar

## Terminal 3 - Player 2:

> XDG_CONFIG_HOME=/tmp/player2 java -jar build/libs/woodlanders-client.jar

