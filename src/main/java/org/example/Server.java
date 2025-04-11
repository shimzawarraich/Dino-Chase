
package org.example;

import java.awt.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;

public class Server {
    public static final int MAP_WIDTH = 600;
    public static final int MAP_HEIGHT = 600;
    private static final int PORT = 2002;
    private static final List<ClientHandler> clients = Collections.synchronizedList(new ArrayList<>());
    private static final Map<String, Player> playerStates = new ConcurrentHashMap<>();
    public static boolean[][] walkableMask;
    private static String chaserId = null;
    private static final ExecutorService pool = Executors.newFixedThreadPool(10);
    private static boolean gameEnded = false;
    private static int requiredPlayers = 0;
    private static boolean playerCountChosen = false;

    public static void main(String[] args) throws IOException {
        loadWalkableMask();
        ServerSocket listener = new ServerSocket(PORT);
        System.out.println("[SERVER] Listening on port " + PORT);

        while (true) {
            Socket clientSocket = listener.accept();
            ClientHandler clientThread = new ClientHandler(clientSocket, clients, playerStates);
            clients.add(clientThread);
            pool.execute(clientThread);
        }
    }

    public static void loadWalkableMask() {
        try {
            BufferedImage mask = ImageIO.read(Server.class.getClassLoader().getResource("mask.png"));
            walkableMask = new boolean[MAP_WIDTH][MAP_HEIGHT];

            for (int x = 0; x < MAP_WIDTH; x++) {
                for (int y = 0; y < MAP_HEIGHT; y++) {
                    int rgb = mask.getRGB(x, y);
                    Color c = new Color(rgb);
                    walkableMask[x][y] = !(c.getRed() > 200 && c.getGreen() < 50 && c.getBlue() < 50);
                }
            }

            System.out.println("[SERVER] Walkable mask loaded.");
        } catch (IOException e) {
            System.err.println("[SERVER ERROR] Failed to load walkable mask.");
            e.printStackTrace();
        }
    }

    public static void tryStartGame() {
        if (requiredPlayers > 0 && playerStates.size() == requiredPlayers && chaserId == null) {
            List<String> ids = new ArrayList<>(playerStates.keySet());
            chaserId = ids.get(new Random().nextInt(ids.size()));
            playerStates.get(chaserId).setChaser(true);
            System.out.println("[GAME] Chaser is " + chaserId);

            StringBuilder names = new StringBuilder();
            for (Player p : playerStates.values()){
                names.append(p.id()).append(",");
            }
            if (names.length() > 0) {
                names.setLength(names.length() - 1);
            }
            String chaserMessage = "CHASER:" + chaserId + "|" + names.toString();
            broadcast(chaserMessage);

            broadcast("START_GAME");
            broadcast("GAME_STARTED");

            ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
            scheduler.schedule(Server::endGame, 34, TimeUnit.SECONDS);
        }
    }

    public static void endGame() {
        if (gameEnded) return;
        gameEnded = true;
        System.out.println("[GAME] Time's up!");
        broadcast("CHAT:[GAME] Time's up!");

        // Determine the loser (the current chaser)
        String loser = chaserId;

        // Broadcast the loser message to all clients
        broadcast("LOSER:" + loser);

        // Update the leaderboard
        List<Player> ranking = new ArrayList<>(playerStates.values());
        ranking.sort((a, b) -> Integer.compare(b.getTagCount(), a.getTagCount()));

        StringBuilder leaderboard = new StringBuilder("LEADERBOARD:");
        for (int i = 0; i < ranking.size(); i++) {
            Player p = ranking.get(i);
            leaderboard.append((i + 1)).append(". ")
                    .append(p.id()).append(" – ")
                    .append(p.getTagCount()).append(" tag(s)");
            if (i < ranking.size() - 1) leaderboard.append(";");
        }

        for (ClientHandler client : clients) {
            client.send(leaderboard.toString());
        }
        broadcast("GAME_OVER");
        String message = leaderboard.toString().trim();
        System.out.println("[SERVER] Sending leaderboard:\n" + message);
        broadcast("CHAT:[SERVER] Sending leaderboard:\n" + message);

        for (ClientHandler client : clients) {
            client.send(message);
        }
    }

    public static void broadcastPositions() {
        if (gameEnded) return;

        StringBuilder sb = new StringBuilder();
        for (Player p : playerStates.values()) {
            sb.append(p.id()).append(",")
                    .append(p.x()).append(",")
                    .append(p.y()).append(",")
                    .append(p.isChaser() ? "1" : "0").append(",")
                    .append(p.getTagCount()).append(",")
                    .append(p.getCooldownEndTime()).append(",")
                    .append(p.isFacingRight() ? "1" : "0").append(";");
        }

        String message = sb.toString();
        for (ClientHandler client : clients) {
            System.out.println("[SERVER] Broadcasting: " + message);

            client.send(message);
        }
    }

    public static void checkForTags() {
        if (chaserId == null || gameEnded) return;
        Player chaser = playerStates.get(chaserId);

        if (chaser.isRecentlyTagged()) {
            // Don't allow tagging while cooling down
            return;
        }

        for (Player p : playerStates.values()) {
            if (!p.id().equals(chaserId)) {
                Rectangle chaserBounds = new Rectangle(chaser.x(), chaser.y(), chaser.size(), chaser.size());
                Rectangle playerBounds = new Rectangle(p.x(), p.y(), p.size(), p.size());

                if (chaserBounds.intersects(playerBounds) && !p.isRecentlyTagged()) {
                    p.setChaser(true);
                    chaser.setChaser(false);
                    chaser.markTagged();
                    chaserId = p.id();

                    System.out.println("[TAG] " + p.id() + " is now the new chaser!");


                    // broadcast a CHASER message (with player list)
                    StringBuilder names = new StringBuilder();
                    for (Player player : playerStates.values()) {
                        names.append(player.id()).append(",");
                    }

                    // remove trailing comma
                    if (names.length() > 0) names.setLength(names.length() - 1);

                    broadcast("CHASER_UPDATE:" + chaserId);
                    broadcast("CHAT:[UPDATE] " + chaserId + " is now the chaser!");

                    break;
                }
            }}}

                public static void broadcast(String msg) {
        for (ClientHandler client : clients) {
            client.send(msg);
        }
    }

    public static boolean isPlayerCountChosen() {
        return playerCountChosen;
    }

    public static synchronized void setRequiredPlayers(int count) {
        if (!playerCountChosen) {
            requiredPlayers = count;
            playerCountChosen = true;
            System.out.println("[SERVER] Required player count set to " + count);
            broadcast("WAITING:" + requiredPlayers);
        }
    }


    public static int getRequiredPlayers() {
        return requiredPlayers;
    }

    public static int getPlayerCount() {
        return playerStates.size();
    }

}