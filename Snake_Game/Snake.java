package Snake_Game;

import java.awt.EventQueue;
import javax.swing.JFrame;

public class Snake  extends JFrame {
    private String username;

    public Snake(String username) {
        this.username = username;
        initUI();
    }

    public void setUsername(String username) {
        this.username = username;
    }

    private void initUI() {
        add(new Board(username));
        pack();
        setResizable(false);
        setTitle("Snake");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }
}