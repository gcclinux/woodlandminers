Great question! Here's how to run 2 instances of the game on the same computer for multiplayer testing:

Method 1: Using Gradle (Recommended)
Terminal 1 - Host Player:

gradle run
Then in the game:

Press ESC
Select "Multiplayer"
Select "Host Server"
Note the IP address shown (use localhost or 127.0.0.1)
Terminal 2 - Client Player: Open a new terminal window and run:

gradle run
Then in the game:

Press ESC
Select "Multiplayer"
Select "Connect to Server"
Enter: localhost:25565 (or just localhost)
Press ENTER
Method 2: Using Built JAR Files
First, build the game:

gradle build
Then open two separate command prompt windows:

Window 1 - Host:

java -jar build/libs/your-game.jar
Window 2 - Client:

java -jar build/libs/your-game.jar
Follow the same in-game steps as Method 1.

Method 3: Using the Desktop Launcher Directly
If you have a desktop launcher class, you can run it multiple times:

Window 1:

gradle desktop:run
Window 2:

gradle desktop:run
Tips for Testing:
Window Positioning: Drag the game windows side-by-side so you can see both players at once

Default Port: The server uses port 25565 by default

Connection Address: When connecting, you can use:

localhost
127.0.0.1
localhost:25565 (explicit port)
Testing Scenarios:

Move both players around to see synchronization
Have one player chop trees, see if the other sees it
Test item pickups
Test player health/damage
Try disconnecting one player
Troubleshooting:

If connection fails, make sure the host started the server first
Check that no firewall is blocking port 25565
Look at console output for error messages
Would you like me to create a quick testing guide document or help you set up a specific testing scenario?