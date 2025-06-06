package Snake_Game;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.util.stream.Collectors;
import java.io.IOException;

class User {
    private String username;
    private String password;

    public User(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public String getUsername() {
        return username;
    }
    public String getPassword() {
        return password;
    }
}

class UserManager {
    private static final String USER_FILE = "users.txt";

    public static void saveUser(User user) throws IOException {
        try (FileWriter fw = new FileWriter(USER_FILE, true)) {
            fw.write(user.getUsername() + "," + user.getPassword() + "\n");
        }
    }

    public static boolean validateLogin(String username, String password) throws IOException {
        try (BufferedReader br = new BufferedReader(new FileReader(USER_FILE))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2 && parts[0].equals(username) && parts[1].equals(password)) {
                    return true;
                }
            }
        }
        return false;
    }
}

class ScoreManager {
    private static final String SCORE_FILE = "scores.txt";

    public static int getHighScore(String username) throws IOException {
        Path scoreFile = Paths.get("highscores.txt");
        if (!Files.exists(scoreFile)) return 0;

        return Files.lines(scoreFile)
                .map(line -> line.split(":"))
                .filter(parts -> parts.length == 2 && parts[0].equals(username))
                .mapToInt(parts -> {
                    try {
                        return Integer.parseInt(parts[1]);
                    }
                    catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(0);
    }


    public static void saveHighScore(String username, int score) throws IOException {
        Path scoreFile = Paths.get("highscores.txt");
        Map<String, Integer> scores = new HashMap<>();

        if (Files.exists(scoreFile)) {
            Files.lines(scoreFile)
                    .map(line -> line.split(":"))
                    .filter(parts -> parts.length == 2)
                    .forEach(parts -> {
                        try {
                            scores.put(parts[0], Math.max(scores.getOrDefault(parts[0], 0), Integer.parseInt(parts[1])));
                        }
                        catch (NumberFormatException e) {
                        }
                    });
        }

        // Update or insert the current user's score
        scores.put(username, Math.max(scores.getOrDefault(username, 0), score));

        // Saves to the file
        BufferedWriter writer = Files.newBufferedWriter(scoreFile);
        for (Map.Entry<String, Integer> entry : scores.entrySet()) {
            writer.write(entry.getKey() + ":" + entry.getValue());
            writer.newLine();
        }
        writer.close();
    }

    public static Map<String, Integer> getLeaderboard() throws IOException {
        Path scoreFile = Paths.get("highscores.txt");
        Map<String, Integer> scores = new HashMap<>();

        if (!Files.exists(scoreFile)) {
            return scores;
        }

        for (String line : Files.readAllLines(scoreFile)) {
            String[] parts = line.split(":");
            if (parts.length == 2) {
                try {
                    String user = parts[0];
                    int score = Integer.parseInt(parts[1]);
                    scores.put(user, Math.max(scores.getOrDefault(user, 0), score));
                }
                catch (NumberFormatException ignored) {}
            }
        }

        // Sort scores by value descending
        return scores.entrySet()
                .stream()
                .sorted((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> a,
                        LinkedHashMap::new
                ));
    }

}

public class LoginFrame extends JFrame {
    public LoginFrame() {
        setTitle("Snake Game - Login");
        setSize(300, 180);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel();
        add(panel);
        placeComponents(panel);
    }

    private void placeComponents(JPanel panel) {
        panel.setLayout(null);

        JLabel userLabel = new JLabel("Username:");
        userLabel.setBounds(10, 10, 80, 25);
        panel.add(userLabel);

        JTextField userText = new JTextField(20);
        userText.setBounds(100, 10, 160, 25);
        panel.add(userText);

        JLabel passwordLabel = new JLabel("Password:");
        passwordLabel.setBounds(10, 40, 80, 25);
        panel.add(passwordLabel);

        JPasswordField passwordText = new JPasswordField(20);
        passwordText.setBounds(100, 40, 160, 25);
        panel.add(passwordText);

        JButton loginButton = new JButton("Login");
        loginButton.setBounds(10, 80, 100, 25);
        panel.add(loginButton);

        JButton registerButton = new JButton("Register");
        registerButton.setBounds(160, 80, 100, 25);
        panel.add(registerButton);

        loginButton.addActionListener(e -> {
            String user = userText.getText();
            String pass = new String(passwordText.getPassword());
            try {
                if (UserManager.validateLogin(user, pass)) {
                    JOptionPane.showMessageDialog(null, "Login Successful!");
                    dispose();
                    SwingUtilities.invokeLater(() -> {
                        Snake game = new Snake(user);
                        game.setVisible(true);
                    });
                }
                else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials!");
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error reading user data.");
            }
        });

        registerButton.addActionListener(e -> {
            String user = userText.getText();
            String pass = new String(passwordText.getPassword());
            try {
                if (!user.isEmpty() && !pass.isEmpty()) {
                    UserManager.saveUser(new User(user, pass));
                    JOptionPane.showMessageDialog(null, "Registration Successful!");
                }
                else {
                    JOptionPane.showMessageDialog(null, "Username and Password cannot be empty.");
                }
            }
            catch (IOException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(null, "Error saving user.");
            }
        });
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }
}
