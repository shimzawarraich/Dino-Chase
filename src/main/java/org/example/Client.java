package org.example;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.*;
import java.net.*;
import java.util.*;
import javax.swing.Timer;

public class Client {
    private static final String SERVER_IP = "localhost";  // Server IP address
    private static final int PORT = 2002;   // Server port number
    
    private BufferedReader in;   // Input stream from server
    private PrintWriter out;     // Output stream to server

    private JFrame frame = new JFrame("Dino Chase");  // Main game window
    private GamePanel gamePanel;  // Game rendering panel
    private JFrame leaderboardFrame = null;    // Leaderboard display window
    private Menu menu = new Menu(); // Game menu
    private boolean menuActive = true; // Menu visibility flag

    public Cursor regularCursor;  // Default custom curso
    public Cursor hoverCursor; // Hover state cursor
    private JWindow waitingWindow;

    //for Chat
    private JTextArea chatArea; // Chat message display
    private JTextField chatInput; // Chat input field
    private JPanel chatPanel; // Chat panel container
    private boolean gameStarted = false; // Game running flag
    private JPanel mainPanel; // Main content panel
    private boolean chaserPopupShown = false; // Chaser announcement flag


    // Client constructor - sets up the game UI
    public Client() {
        // Initialize game panel and set client reference
        gamePanel = new GamePanel();
        gamePanel.setClient(this);

        // Set up main layout
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.add(gamePanel, BorderLayout.CENTER);
        frame.setContentPane(mainPanel);

        // Configure window properties
        frame.pack();

        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        // Create the chat UI components
        chatArea = new JTextArea(15, 25);
        chatArea.setEditable(false);
        chatArea.setLineWrap(true);
        chatArea.setWrapStyleWord(true);

        // Set up chat input field
        chatInput = new JTextField();
        chatInput.addActionListener(e -> {
            String msg = chatInput.getText().trim();
            if (!msg.isEmpty()) {
                out.println("CHAT:" + msg); // Send chat message to server
                chatInput.setText("");

                // Return focus back to gamePanel so player can move again
                SwingUtilities.invokeLater(() -> {
                    gamePanel.requestFocusInWindow();
                });
            }
        });
        // Configure chat panel layout
        chatPanel = new JPanel(new BorderLayout());
        chatPanel.setPreferredSize(new Dimension(250, 0));
        chatPanel.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        chatPanel.add(chatInput, BorderLayout.SOUTH);

        // Wrap game and chat panel into a horizontal layout
        JPanel mainPanel = new JPanel(new BorderLayout());

        // Game in the center
        mainPanel.add(gamePanel, BorderLayout.CENTER);



        frame.getContentPane().add(gamePanel);

        // size frame to preferred size
        frame.pack();
        frame.setLocationRelativeTo(null);

        // prevents resizing
        frame.setResizable(false);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);


        // Set the client reference in GamePanel.
        gamePanel.setClient(this);
        frame.setCursor(regularCursor);

        // custom cursor
        setCustomCursor(frame);

        // Set up network sender for player movements
        gamePanel.setNetworkSender((id, x, y) -> {
            boolean facingRight = gamePanel.getMyPlayerFacingRight();
            String message = id + "," + x + "," + y + "," + (facingRight ? "1" : "0");
            out.println(message);
        });

        gamePanel.requestFocusInWindow();
        // Mouse click handler for menu interactions
        gamePanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (menuActive) {
                    if(menu.isPlayClicked(e)){
                        menuActive = false;
                        gamePanel.startGame();
                        SoundPlayer.stopLoop();
                        SoundPlayer.playLoop("lobby.wav");
                        // Start login process in new thread
                        new Thread(() -> {
                            try {
                                handleLogin();
                            } catch (IOException exse) {
                                exse.printStackTrace();
                            }
                        }).start();
                    } else if (menu.isHelpClicked(e)) {
                        // Show help dialog
                        JOptionPane.showMessageDialog(frame, "How to Play:\n" +
                                        "- Use arrow keys to move\n" +
                                        "- Avoid being caught by the chaser\n" +
                                        "- If you're the chaser, try to tag others\n" +
                                        "- Last player who's the chaser loses!",
                                "How to Play", JOptionPane.INFORMATION_MESSAGE);

                    } else if (menu.isLoserboardClicked(e)) {
                        showLoserboard(); // Display loserboard
                    } else if (menu.isQuitClicked(e)) {
                        System.exit(0); // Quit game

                    }
                }
            }
        });
        // Mouse motion handler for menu hover effects
        gamePanel.addMouseMotionListener(new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                if(menuActive){
                    menu.checkHover(e.getPoint());
                    gamePanel.repaint();
                }
            }
        });

        frame.setVisible(true);
        gamePanel.requestFocusInWindow();
    }
    // Displays the loserboard with player statistics
    private void showLoserboard() {
        // Read users data from the file
        File file = new File("users.csv");
        ArrayList<String[]> users = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            reader.readLine(); // Skip header line

            // Read user data from the CSV file
            while ((line = reader.readLine()) != null) {
                String[] userData = line.split(",");
                if (userData.length == 3) {

                    // Add the user data (username, password, losses)
                    users.add(userData); // Add user data to list
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Sort users by losses in descending order
        users.sort((u1, u2) -> Integer.compare(Integer.parseInt(u2[2]), Integer.parseInt(u1[2])));

        // Limit to top 5 users
        StringBuilder loserboard = new StringBuilder("\n\t\t      LOSERBOARD:\n \t\t~~~~~~~~~~~~~~~~~~\n");
        int count = 1;
        for (String[] user : users) {
            // username
            loserboard.append("\t\t").append(count).append(". ").append(user[0])
                    // losses
                    .append(" – ").append(user[2])
                    .append(" losses");
            if (count ==1){
                loserboard.append(" \uD83D\uDC51"); // Add crown emoji for top loser
            }
            loserboard.append("\n\n");
            count++;
        }
        // Display loserboard in Swing UI thread
        SwingUtilities.invokeLater(() -> {
            // Prevent opening a new loserboard window if one is already open
            if (leaderboardFrame != null && leaderboardFrame.isVisible()) {
                return; // If the loserboard window is already open, don't open a new one
            }

            // Create a new JFrame for the loserboard
            leaderboardFrame = new JFrame("🏁 Loserboard 🏁");
            leaderboardFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

            // Increased size for more room
            leaderboardFrame.setSize(500, 350);

            // Create a custom panel with a background color
            JPanel panel = new JPanel();
            panel.setLayout(new BorderLayout());
            panel.setBackground(new Color(220, 180, 90)); // Set the background color

            // Create a JTextPane for better text formatting
            JTextPane loserboardTextPane = new JTextPane();

            // Make the text pane non-editable
            loserboardTextPane.setEditable(false);
            loserboardTextPane.setText(loserboard.toString());

            // Set the font and text color
            loserboardTextPane.setFont(new Font("Arial", Font.PLAIN, 18));
            loserboardTextPane.setForeground(Color.BLACK); // Set text color to white
            loserboardTextPane.setBackground(new Color(220, 180, 90)); // Set background color to match the panel
            loserboardTextPane.setOpaque(true); // Make the background color visible

            // Create a scroll pane and add the JTextPane to it
            JScrollPane scrollPane = new JScrollPane(loserboardTextPane);

            // Border color
            scrollPane.setBorder(BorderFactory.createLineBorder(new Color(100, 60, 20), 3));

            // make sure the loserboard opens scrolled to the top
            loserboardTextPane.select(0, 0);

            // Add the scroll pane to the panel
            panel.add(scrollPane, BorderLayout.CENTER);

            // Add a close button at the bottom
            JButton closeButton = new JButton("Close");
            closeButton.setFont(new Font("Arial", Font.PLAIN, 14));

            // Button color
            closeButton.setBackground(new Color(100, 60, 20));
            closeButton.setForeground(Color.WHITE);

            // Close button action
            closeButton.addActionListener(e -> leaderboardFrame.dispose());
            panel.add(closeButton, BorderLayout.SOUTH);

            // Add the panel to the frame's content pane
            leaderboardFrame.setContentPane(panel);

            // Set the background color of the content pane
            leaderboardFrame.getContentPane().setBackground(new Color(220, 180, 90));

            // Revalidate and repaint the frame
            leaderboardFrame.revalidate();
            leaderboardFrame.repaint();

            // Set the frame's location and make it visible
            leaderboardFrame.setLocationRelativeTo(frame); // Center the window
            leaderboardFrame.setVisible(true);
            leaderboardFrame.toFront();
            leaderboardFrame.requestFocus();
        });


    }


    // helper method for custom cursor
    private void setCustomCursor(Component component) {
        // Load the custom cursor image
        ImageIcon cursorIcon = new ImageIcon(getClass().getClassLoader().getResource("bone-cursor.png"));

        // Check if the image is loaded correctly
        if (cursorIcon.getImageLoadStatus() != MediaTracker.COMPLETE) {
            System.out.println("Cursor image not loaded correctly!");
        }

        //getting the image for the cursor
        Image scaledCursorImage = cursorIcon.getImage().getScaledInstance(35, 35, Image.SCALE_SMOOTH);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Cursor customCursor = toolkit.createCustomCursor(scaledCursorImage, new Point(0, 0), "cursor");

        // Assign the custom cursor to the instance fields
        this.regularCursor = customCursor;
        this.hoverCursor = customCursor;

        // Set the custom cursor for the component
        component.setCursor(customCursor);
    }
    // Main game loop - handles server communication
    private void run() throws IOException {
        SoundPlayer.playLoop("menu.wav"); // Play menu music
        // Establish server connection
        Socket socket = new Socket(SERVER_IP, PORT);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Main loop for receiving and handling game updates
        while (true) {
            String line = in.readLine();
            if (line == null) break;

            // Handle game-specific updates
            handleGameUpdates(line);
        }
    }
    // Handles user login process
    private void handleLogin() throws IOException {
        boolean authenticated = false;
        String username = "";
        // Continue until successful login
        while (!authenticated) {
            String[] credentials = showLoginDialog();
            if (credentials == null) {
                // User canceled, exit the game
                System.exit(0);
            }
            username = credentials[0];
            String password = credentials[1];
            // Check if user exists
            boolean userExists = authenticateUser(username, password);

            if (userExists) {
                // Verify password
                if (authenticatePassword(username, password)) {
                    authenticated = true;
                    gamePanel.setMyId(username);

                    // Send username to the server
                    out.println(username);

                    // Mark client as logged in
                    setLoggedIn(true);

                    // Update UI in Swing thread
                    SwingUtilities.invokeLater(() -> {
                        // Add chat panel if not already present
                        if (chatPanel.getParent() == null) {
                            // add chat
                            mainPanel.add(chatPanel, BorderLayout.WEST);
                        }
                        // Update frame layout
                        frame.setContentPane(mainPanel);

                        // re-size to fit new layout
                        frame.pack();
                        frame.pack();                        // re-size to fit new layout
                        frame.revalidate();
                        frame.repaint();

                        // allow movement
                        gamePanel.requestFocusInWindow();
                    });


                    // Show waiting popup after login
                    if (!gamePanel.isGameStarted() && getWaitingForPlayers() > 0 && waitingWindow == null) {
                        showWaitingPopup();
                    }

                } else {
                    showErrorMessage("Incorrect username or password.");
                }
            } else {
                // Offer registration for new users
                int option = JOptionPane.showConfirmDialog(frame,
                        "Account doesn't exist. Would you like to sign up?",
                        "Account not found",
                        JOptionPane.YES_NO_OPTION
                );
                if (option == JOptionPane.YES_OPTION) {
                    registerUser(username, password);
                    JOptionPane.showMessageDialog(frame, "Registration successful! You can now log in.");
                }
            }
        }
    }

    // Registers a new user
    private void registerUser(String username, String password) throws IOException {
        // Append the new user to the .csv file
        BufferedWriter writer = new BufferedWriter(new FileWriter("users.csv", true));

        // Default 'losses' value is 0
        writer.write(username + "," + password + ",0"); // Default 0 losses
        writer.newLine();
        writer.close();
    }

    // Shows login dialog and returns credentials
    private String[] showLoginDialog() {
        // Create the dialog for username and password input
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.decode("#8b9d87"));  // background colour

        // Set up custom font
        Font font = new Font("Arial", Font.PLAIN, 16);

        JTextField usernameField = new JTextField(20);
        JPasswordField passwordField = new JPasswordField(20);

        // Create labels with styling
        JLabel usernameLabel = new JLabel("Enter your username:");
        usernameLabel.setForeground(Color.decode("#e3fae7"));
        usernameLabel.setFont(font);

        JLabel passwordLabel = new JLabel("Enter your password:");
        passwordLabel.setForeground(Color.decode("#e3fae7"));
        passwordLabel.setFont(font);

        // Add components to the panel
        panel.add(usernameLabel);

        // Add components to panel with spacing
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(usernameField);
        panel.add(Box.createRigidArea(new Dimension(0, 15)));
        panel.add(passwordLabel);
        panel.add(Box.createRigidArea(new Dimension(0, 5)));
        panel.add(passwordField);

        // Show dialog and return results
        int option = JOptionPane.showConfirmDialog(frame, panel, "Login / Sign Up", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

        // If user clicks OK, return the username and password
        if (option == JOptionPane.OK_OPTION) {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            return new String[]{username, password};
        }

        // Cancelled or closed the dialog
        return null;
    }
    // Checks if user exists
    private boolean authenticateUser(String username, String password) throws IOException {
        // Read the user data from the .csv file
        File file = new File("users.csv");

        // No users saved yet, so it's a new user
        if (!file.exists()) {
            return false;
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));

        // Skip the first line (column names)
        reader.readLine();
        String line;
        boolean userExists = false;

        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if (userData.length == 3 && userData[0].equals(username)) {
                userExists = true;
                reader.close();

                // User exists
                return true;
            }
        }
        reader.close();

        // User not found
        return false;
    }
    // Verifies user password
    private boolean authenticatePassword(String username, String password) throws IOException {
        // Read the user data from the .csv file and check the password
        File file = new File("users.csv");
        if (!file.exists()) {
            return false; // No users saved yet, so it's a new user
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));

        // Skip the first line (column names)
        reader.readLine();
        String line;

        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if (userData.length == 3 && userData[0].equals(username)) {
                // Check if the password matches
                if (userData[1].equals(password)) {
                    reader.close();

                    // Password matches
                    return true;
                } else {
                    reader.close();

                    // Incorrect password
                    return false;
                }
            }
        }
        reader.close();

        // User not found
        return false;
    }
    
    // Shows error message dialog
    private void showErrorMessage(String message) {
        // Ensure this UI update happens on the EDT thread
        SwingUtilities.invokeLater(() -> {
            JOptionPane.showMessageDialog(frame, message, "Error", JOptionPane.ERROR_MESSAGE);
        });
    }

    // Add this method to increment losses
    private void incrementLosses(String username) throws IOException {
        File file = new File("users.csv");
        if (!file.exists()) {
            return; // No users file found
        }

        BufferedReader reader = new BufferedReader(new FileReader(file));
        StringBuilder fileContent = new StringBuilder();
        String line;
        boolean userFound = false;

        while ((line = reader.readLine()) != null) {
            String[] userData = line.split(",");
            if (userData.length == 3 && userData[0].equals(username)) {

                // Increment losses
                userData[2] = String.valueOf(Integer.parseInt(userData[2]) + 1); // Increment losses by 1
                userFound = true;
            }
            fileContent.append(String.join(",", userData)).append("\n");
        }
        reader.close();

        // Rewrite the file with updated content
        if (userFound) {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.write(fileContent.toString());
            writer.close();
        }
    }

    // Processes game update messages from server
    private void handleGameUpdates(String line) throws IOException {
        if (line.startsWith("SELECT_PLAYERS")) {
            // Player count selection dialog
            String[] options = {"2", "3", "4"};
            String chosen = (String) JOptionPane.showInputDialog(
                    frame,
                    "Select number of players to start:",
                    "Lobby",
                    JOptionPane.PLAIN_MESSAGE,
                    null,
                    options,
                    options[0]
            );
            if (chosen != null) {
                out.println("PLAYERS:" + chosen);
                setWaitingForPlayers(Integer.parseInt(chosen));
            }
        } else if (line.startsWith("WAITING:")) {
            // Waiting for players message
            String num = line.substring("WAITING:".length());
            try {
                int required = Integer.parseInt(num);
                setWaitingForPlayers(required);

                // show only if logged in and no waiting popup is open
                if (isLoggedIn() && waitingWindow == null) {
                    showWaitingPopup();
                }
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }


        }else if (line.startsWith("CHAT:")) {
                // Chat message handling
                if (!gameStarted) {
                    String chatMsg = line.substring(5);
                    chatArea.append(chatMsg + "\n");
                }

        } else if (line.equals("START_GAME")) {
            // Game start handling
            gameStarted = true;
            chatArea.setText("");
            chatInput.setEditable(false);
            chatInput.setText("Game in progress...");
            SoundPlayer.stopLoop();
            SoundPlayer.playLoop("game.wav");
            gamePanel.startGameTimer();

        } else if (line.startsWith("CHASER:")) {
            // Chaser assignment handling
            if (waitingWindow != null) {
                waitingWindow.dispose();
                waitingWindow = null;
            }

            String payload = line.substring(7);
            String chaser;
            String[] playersArray;

            if (payload.contains("|")) {
                String[] parts = payload.split("\\|");
                chaser = parts[0];
                playersArray = parts[1].split(",");
            } else {
                chaser = payload;
                playersArray = getPlayerNames();
            }

            if (gameStarted) {
                chatArea.append("[UPDATE] " + chaser + " is now the chaser!\n");
            }


            if (!chaserPopupShown) {
                chaserPopupShown = true;

                int targetIndex = 0;
                for (int i = 0; i < playersArray.length; i++) {
                    if (playersArray[i].equals(chaser)) {
                        targetIndex = i;
                        break;
                    }
                }
                SoundPlayer.playOnce("wheel.wav");
                // Show wheel animation for chaser selection
                new Wheel(frame, playersArray, targetIndex, finalSelectedIndex -> {
                    Timer delayPopup = new Timer(200, e -> showChaserPopup(chaser));
                    delayPopup.setRepeats(false);
                    delayPopup.start();
                });
            }

        } else if (line.startsWith("CHASER_UPDATE:")) {
            // Chaser change update
            String newChaser = line.substring("CHASER_UPDATE:".length());
            chatArea.append("[UPDATE] " + newChaser + " is now the chaser!\n");

        } else if (line.equals("GAME_OVER")) {
            // Game over handling
            gamePanel.showGameOver();
            SoundPlayer.stopLoop();

        } else if (line.startsWith("LOSER:")) {
            // Loser announcement
            String loser = line.substring(6);
            incrementLosses(loser);
            JOptionPane.showMessageDialog(frame, loser + " is the loser!", "Game Over", JOptionPane.INFORMATION_MESSAGE);

        } else if (line.startsWith("LEADERBOARD:")) {
            // Leaderboard display
            String board = line.substring("LEADERBOARD:".length());
            String[] lines = board.split(";");
            StringBuilder formatted = new StringBuilder();
            for (String lineEntry : lines) {
                formatted.append(lineEntry.trim()).append("\n");
            }
            showLeaderboard(formatted.toString());

        } else if (line.contains(",")) {
            // Player state updates
            String[] playerChunks = line.split(";");
            Map<String, Player> newState = new HashMap<>();
            for (String chunk : playerChunks) {
                String[] parts = chunk.split(",");
                if (parts.length >= 7) {
                    String id = parts[0];
                    int x = Integer.parseInt(parts[1]);
                    int y = Integer.parseInt(parts[2]);
                    boolean isChaser = parts[3].equals("1");
                    int tagCount = Integer.parseInt(parts[4]);
                    long cooldown = Long.parseLong(parts[5]);
                    boolean facingRight = parts[6].equals("1");

                    // Create/update player object
                    Player p = new Player(id, x, y, Color.BLUE);
                    p.setChaser(isChaser);
                    p.setTagCount(tagCount);
                    p.setCooldownEndTime(cooldown);
                    p.setFacingRight(facingRight);
                    newState.put(id, p);
                }
            }
            gamePanel.updateAllPlayers(newState); // Update game state
        }
    }

    // Displays the game leaderboard
    private void showLeaderboard(String text) {
        // Prevent opening a new leaderboard window if one is already open
        if (leaderboardFrame != null && leaderboardFrame.isVisible()) {
            return; // If leaderboard is already open, don't open a new one
        }

        // Create and display the leaderboard window
        leaderboardFrame = new JFrame("🏁 Final Results");
        leaderboardFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTextArea area = new JTextArea(text);
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 16));

        JScrollPane scrollPane = new JScrollPane(area);
        leaderboardFrame.getContentPane().add(scrollPane);

        // Finalize window
        leaderboardFrame.setSize(400, 300);
        leaderboardFrame.setLocationRelativeTo(null);
        leaderboardFrame.setAlwaysOnTop(true);
        leaderboardFrame.setVisible(true);
        leaderboardFrame.toFront();
        leaderboardFrame.requestFocus();
    }

    // Gets names of all players in game
    public String[] getPlayerNames() {
        String[] names = gamePanel.getAllPlayerNames();
        if (names.length <= 1) {
            // Delay and try again after a short period
            try {
                // sleep half a second
                Thread.sleep(500);
            } catch (InterruptedException ex) {
                ex.printStackTrace();
            }
            names = gamePanel.getAllPlayerNames();
        }
        return names;
    }
    
    // Shows popup announcing new chaser
    private void showChaserPopup(String chaser) {
        JWindow popup = new JWindow();
        popup.setLayout(new BorderLayout());

        JLabel label = new JLabel(chaser + " is the chaser!", SwingConstants.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        label.setOpaque(true);
        label.setBackground(new Color(255, 70, 70));
        label.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 4),
                BorderFactory.createEmptyBorder(20, 40, 20, 40)
        ));

        // Configure popup window
        popup.getContentPane().add(label, BorderLayout.CENTER);
        popup.pack();
        popup.setLocationRelativeTo(frame); // Centered on the game window
        popup.setAlwaysOnTop(true);
        popup.setVisible(true);

        // Auto-dismiss after 1 second
        new Timer(1000, e -> popup.setVisible(false)).start();
    }

    // Waiting players count management
    private int waitingForPlayers;

    public void setWaitingForPlayers(int num) {
        this.waitingForPlayers = num;
    }

    public int getWaitingForPlayers() {
        return waitingForPlayers;
    }

    // Shows waiting for players popup
    private void showWaitingPopup() {
        waitingWindow = new JWindow();
        waitingWindow.setLayout(new BorderLayout());

        // Create waiting message label
        JLabel waitingLabel = new JLabel("Waiting for " + getWaitingForPlayers() + " players...", SwingConstants.CENTER);
        waitingLabel.setFont(new Font("Arial", Font.BOLD, 24));
        waitingLabel.setForeground(Color.WHITE);
        waitingLabel.setOpaque(true);
        waitingLabel.setBackground(new Color(0, 0, 0, 150));
        waitingLabel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.DARK_GRAY, 2),
                BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));

        // Configure waiting window
        waitingWindow.getContentPane().add(waitingLabel, BorderLayout.CENTER);
        waitingWindow.pack();
        waitingWindow.setLocationRelativeTo(frame);
        waitingWindow.setAlwaysOnTop(true);
        waitingWindow.setVisible(true);
    }

    // Login state management
    private boolean loggedIn = false;

    public void setLoggedIn(boolean value) {
        loggedIn = value;
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    // Main entry point
    
    public static void main(String[] args) throws Exception {
        Client client = new Client();
        client.run();
    }


}
