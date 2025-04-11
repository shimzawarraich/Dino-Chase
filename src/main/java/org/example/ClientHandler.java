package org.example;

import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.util.List;

class ClientHandler implements Runnable {
    //Network communication fields
    private final Socket socket; // Client connection socket
    private final BufferedReader in; //Input stream from client
    private final PrintWriter out; //Output stream to client
    private String username; //Player's chosen username
    private final List<ClientHandler> clients; //List of all connected clients
    private final Map<String, Player> playerStates;  //Curent game state of all players


    public ClientHandler(Socket socket, List<ClientHandler> clients, Map<String, Player> playerStates) throws IOException {
        this.socket = socket;
        this.clients = clients;
        this.playerStates = playerStates;
        //Initialize network streams
        this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        this.out = new PrintWriter(socket.getOutputStream(), true);
    }

    public void run() {
        try {
            //get username from client
            out.println("Enter your username:");
            username = in.readLine();

            //validate username
            if (username == null || username.trim().isEmpty()) {
                out.println("[SERVER] Invalid username. Disconnecting.");
                socket.close();
                return;
            }

            // When a new player joins, broadcast the waiting message
            if (Server.getPlayerCount() < Server.getRequiredPlayers()) {
                // Update all clients on player count
                Server.broadcast("WAITING:" + Server.getRequiredPlayers());
            }
            //synchronize player count selection
            synchronized (Server.class) {
                if (!Server.isPlayerCountChosen()) {
                    out.println("SELECT_PLAYERS");
                    String selection = in.readLine();
                    if (selection != null && selection.startsWith("PLAYERS:")) {
                        //set the required player count for the game
                        int count = Integer.parseInt(selection.substring(8));
                        Server.setRequiredPlayers(count);
                        System.out.println("[SERVER] Player count set to " + count);
                        broadcast("WAITING:" + count);
                    }
                }
            }
            // find valid spawn position for new player
            int x, y;
            Random rand = new Random();
            do {
                x = rand.nextInt(Server.MAP_WIDTH - 40);
                y = rand.nextInt(Server.MAP_HEIGHT - 40);
            } while (!isSpawnWalkable(x, y));

            // create and register new player
            Player newPlayer = new Player(username, x, y, Color.BLUE);

            playerStates.put(username, newPlayer);
            broadcast("[SERVER] " + username + " has joined the game.");

            // check if game can start
            if (Server.getPlayerCount() == Server.getRequiredPlayers()) {
                Server.tryStartGame();
            }
            Server.tryStartGame();
            Server.broadcastPositions();

            // Main message processing loop
            String input;
            boolean gameStarted = false;

            while ((input = in.readLine()) != null) {
                if (input.contains(",")) {
                    // Handle position update
                    String[] parts = input.split(",");
                    if (parts.length >= 4) {
                        // Update player position and facing direction
                        int xPos = Integer.parseInt(parts[1]);
                        int yPos = Integer.parseInt(parts[2]);
                        boolean facingRight = parts[3].equals("1");
                        Player p = playerStates.get(parts[0]);
                        if (p != null) {
                            p.setX(xPos);
                            p.setY(yPos);
                            p.setFacingRight(facingRight);
                        }
                    }

                    // Check for tag collisions and broadcast updates
                    Server.checkForTags();
                    Server.broadcastPositions();
                }

                // Handle chat before game starts
                else if (input.startsWith("CHAT:") && !gameStarted) {
                    String chatMessage = input.substring(5);
                    broadcast("CHAT:" + username + ": " + chatMessage);

                }

                // Handle game start notification
                else if (input.equals("GAME_STARTED")) {
                    gameStarted = true;
                }

                System.out.println("[SERVER] Received from " + username + ": " + input);
            }


        } catch (IOException e) {
            System.out.println("[ERROR] Connection lost with " + username);
        } finally {
            // Clean up resources when client disconnects
            try {
                clients.remove(this);
                if (username != null) {
                    playerStates.remove(username);
                    broadcast("[SERVER] " + username + " has left the game.");
                }
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    private boolean isSpawnWalkable(int x, int y) {
        // the size used for spawn generation
        int playerSize = 40;

        // extra margin to avoid touching players
        int buffer = 5;

        // Check all 4 corners of the player's bounding box are in a walkable area
        for (int dx : new int[]{0, playerSize - 1}) {
            for (int dy : new int[]{0, playerSize - 1}) {
                int checkX = x + dx;
                int checkY = y + dy;
                // Check bounds and walkable terrain
                if (checkX < 0 || checkX >= Server.MAP_WIDTH || checkY < 0 || checkY >= Server.MAP_HEIGHT) {
                    return false;
                }
                if (!Server.walkableMask[checkX][checkY]) {
                    return false;
                }
            }
        }

        // Create a rectangle for the new player's spawn area
        Rectangle spawnRect = new Rectangle(x, y, playerSize, playerSize);
        // Grow the rectangle by the buffer to enforce extra clearance
        spawnRect.grow(buffer, buffer);

        // Check that this spawn does not intersect with any existing player's rectangle
        for (Player p : playerStates.values()) {
            Rectangle playerRect = new Rectangle(p.x(), p.y(), p.size(), p.size());
            // Grow the existing player's rectangle by the same buffer
            playerRect.grow(buffer, buffer);

            if (spawnRect.intersects(playerRect)) {
                return false;
            }
        }

        return true;
    }

    public void send(String msg) {
        out.println(msg);
        out.flush();
    }

    private void broadcast(String message) {
        for (ClientHandler client : clients) {
            client.out.println(message);
        }
    }
}
