import java.awt.*;
import javax.swing.*;

/**
 * LoginPanel — Displays login and sign-up screens in a stylish design.
 * 
 * Features:
 *  - Two modes: LOGIN and SIGNUP
 *  - Soft, elegant color theme with rounded corners
 *  - Input fields for username and password
 *  - Toggle between login and signup
 */

// Custom rounded button component
class RoundedButton extends JButton {
    private static final int RADIUS = 12;
    
    public RoundedButton(String text) {
        super(text);
        setOpaque(false);
        setContentAreaFilled(false);
        setFocusPainted(false);
        setBorderPainted(false);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
        
        // Draw border
        g2.setColor(getForeground());
        g2.setStroke(new BasicStroke(0.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
        
        super.paintComponent(g);
    }
}

// Custom rounded text field
class RoundedTextField extends JTextField {
    private static final int RADIUS = 10;
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
    }
}

// Custom rounded password field
class RoundedPasswordField extends JPasswordField {
    private static final int RADIUS = 10;
    
    @Override
    protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        // Draw background
        g2.setColor(getBackground());
        g2.fillRoundRect(0, 0, getWidth(), getHeight(), RADIUS, RADIUS);
        
        super.paintComponent(g);
    }
    
    @Override
    protected void paintBorder(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        
        g2.setColor(getForeground());
        g2.setStroke(new BasicStroke(1.5f));
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, RADIUS, RADIUS);
    }
}

public class LoginPanel extends JPanel {
    public interface LoginListener {
        void onLoginSuccess(String username);
    }

    private enum Mode { LOGIN, SIGNUP }

    private UserManager userManager;
    private LoginListener listener;
    private Mode mode = Mode.LOGIN;

    // UI Components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;
    private JLabel statusLabel;
    private JButton loginSignupButton;
    private JButton toggleButton;

    // Black and Blue Color Theme
    private static final Color ACCENT_BLUE = new Color(0, 150, 255);           // Bright cyan blue
    private static final Color LIGHT_BG = new Color(15, 15, 25);               // Very dark black
    private static final Color INPUT_BG = new Color(25, 35, 55);               // Dark blue
    private static final Color TEXT_COLOR = new Color(220, 230, 255);          // Light blue-white
    private static final Color ACCENT_LIGHT = new Color(100, 180, 255);        // Lighter blue

    public LoginPanel(UserManager userManager, LoginListener listener) {
        this.userManager = userManager;
        this.listener = listener;

        setBackground(LIGHT_BG);
        setLayout(null);
        setPreferredSize(new Dimension(800, 600));

        initializeComponents();
    }

    private void initializeComponents() {
        // Title
        JLabel titleLabel = new JLabel("CYBER RELIC ARCADE");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 36));
        titleLabel.setForeground(ACCENT_BLUE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        titleLabel.setBounds(50, 50, 700, 60);
        add(titleLabel);

        // Subtitle
        JLabel subtitleLabel = new JLabel("LOGIN");
        subtitleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        subtitleLabel.setForeground(TEXT_COLOR);
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);
        subtitleLabel.setBounds(50, 110, 700, 40);
        add(subtitleLabel);

        // Username Label
        JLabel userLabel = new JLabel("USERNAME");
        userLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        userLabel.setForeground(ACCENT_BLUE);
        userLabel.setBounds(150, 180, 500, 20);
        add(userLabel);

        // Username Field
        usernameField = new RoundedTextField();
        usernameField.setFont(new Font("Arial", Font.PLAIN, 16));
        usernameField.setBackground(INPUT_BG);
        usernameField.setForeground(ACCENT_BLUE);
        usernameField.setCaretColor(ACCENT_BLUE);
        usernameField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        usernameField.setBounds(150, 205, 500, 40);
        add(usernameField);

        // Password Label
        JLabel passLabel = new JLabel("PASSWORD");
        passLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        passLabel.setForeground(ACCENT_BLUE);
        passLabel.setBounds(150, 260, 500, 20);
        add(passLabel);

        // Password Field
        passwordField = new RoundedPasswordField();
        passwordField.setFont(new Font("Arial", Font.PLAIN, 16));
        passwordField.setBackground(INPUT_BG);
        passwordField.setForeground(ACCENT_BLUE);
        passwordField.setCaretColor(ACCENT_BLUE);
        passwordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        passwordField.setBounds(150, 285, 500, 40);
        add(passwordField);

        // Confirm Password Field (hidden initially for login mode)
        JLabel confirmLabel = new JLabel("CONFIRM PASSWORD");
        confirmLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        confirmLabel.setForeground(ACCENT_BLUE);
        confirmLabel.setBounds(150, 340, 500, 20);
        confirmLabel.setVisible(false);
        confirmLabel.setName("confirmLabel");
        add(confirmLabel);

        confirmPasswordField = new RoundedPasswordField();
        confirmPasswordField.setFont(new Font("Arial", Font.PLAIN, 16));
        confirmPasswordField.setBackground(INPUT_BG);
        confirmPasswordField.setForeground(ACCENT_BLUE);
        confirmPasswordField.setCaretColor(ACCENT_BLUE);
        confirmPasswordField.setBorder(BorderFactory.createEmptyBorder(5, 10, 5, 10));
        confirmPasswordField.setBounds(150, 365, 500, 40);
        confirmPasswordField.setVisible(false);
        confirmPasswordField.setName("confirmField");
        add(confirmPasswordField);

        // Status Label
        statusLabel = new JLabel("");
        statusLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        statusLabel.setForeground(new Color(200, 100, 100));
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBounds(150, 410, 500, 20);
        add(statusLabel);

        // Login/Signup Button
        loginSignupButton = new RoundedButton("LOGIN");
        loginSignupButton.setFont(new Font("Arial", Font.BOLD, 16));
        loginSignupButton.setBackground(ACCENT_BLUE);
        loginSignupButton.setForeground(Color.WHITE);
        loginSignupButton.setBounds(200, 450, 400, 50);
        loginSignupButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        loginSignupButton.addActionListener(e -> handleLoginSignup());
        add(loginSignupButton);

        // Toggle Button (Switch between Login and Signup)
        toggleButton = new RoundedButton("Don't have an account? SIGN UP");
        toggleButton.setFont(new Font("Arial", Font.PLAIN, 12));
        toggleButton.setBackground(LIGHT_BG);
        toggleButton.setForeground(ACCENT_BLUE);
        toggleButton.setBounds(200, 520, 400, 30);
        toggleButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        toggleButton.addActionListener(e -> toggleMode());
        add(toggleButton);

        // Guest Login Button
        JButton guestButton = new RoundedButton("PLAY AS GUEST");
        guestButton.setFont(new Font("Arial", Font.BOLD, 12));
        guestButton.setBackground(ACCENT_LIGHT);
        guestButton.setForeground(Color.WHITE);
        guestButton.setBounds(200, 560, 400, 40);
        guestButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        guestButton.addActionListener(e -> handleGuestLogin());
        add(guestButton);
    }

    private void toggleMode() {
        if (mode == Mode.LOGIN) {
            mode = Mode.SIGNUP;
            loginSignupButton.setText("SIGN UP");
            toggleButton.setText("Already have an account? LOGIN");
            Component[] components = getComponents();
            for (Component c : components) {
                if (c.getName() != null && c.getName().equals("confirmLabel")) {
                    c.setVisible(true);
                } else if (c.getName() != null && c.getName().equals("confirmField")) {
                    c.setVisible(true);
                }
            }
            statusLabel.setText("");
        } else {
            mode = Mode.LOGIN;
            loginSignupButton.setText("LOGIN");
            toggleButton.setText("Don't have an account? SIGN UP");
            Component[] components = getComponents();
            for (Component c : components) {
                if (c.getName() != null && c.getName().equals("confirmLabel")) {
                    c.setVisible(false);
                } else if (c.getName() != null && c.getName().equals("confirmField")) {
                    c.setVisible(false);
                }
            }
            statusLabel.setText("");
        }
        confirmPasswordField.setText("");
    }

    private void handleLoginSignup() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Please fill in all fields");
            return;
        }

        if (mode == Mode.LOGIN) {
            handleLogin(username, password);
        } else {
            handleSignup(username, password);
        }
    }

    private void handleGuestLogin() {
        statusLabel.setForeground(new Color(100, 255, 100));
        statusLabel.setText("Starting guest session (scores not saved)...");
        SwingUtilities.invokeLater(() -> {
            try {
                Thread.sleep(800);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            listener.onLoginSuccess(null);  // null username indicates guest mode
        });
    }

    private void handleLogin(String username, String password) {
        if (userManager.login(username, password)) {
            statusLabel.setForeground(new Color(100, 255, 100));
            statusLabel.setText("Login successful! Welcome, " + username);
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                listener.onLoginSuccess(username);
            });
        } else {
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
    }

    private void handleSignup(String username, String password) {
        String confirmPassword = new String(confirmPasswordField.getPassword());

        if (!password.equals(confirmPassword)) {
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setText("Passwords do not match");
            passwordField.setText("");
            confirmPasswordField.setText("");
            return;
        }

        if (password.length() < 4) {
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setText("Password must be at least 4 characters");
            return;
        }

        if (username.length() < 3) {
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setText("Username must be at least 3 characters");
            return;
        }

        if (userManager.register(username, password)) {
            statusLabel.setForeground(new Color(100, 255, 100));
            statusLabel.setText("Account created! You can now login.");
            usernameField.setText("");
            passwordField.setText("");
            confirmPasswordField.setText("");
            SwingUtilities.invokeLater(() -> {
                try {
                    Thread.sleep(1500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                toggleMode(); // Switch back to login
            });
        } else {
            statusLabel.setForeground(new Color(255, 100, 100));
            statusLabel.setText("Username already exists");
            usernameField.setText("");
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw subtle soft border
        g2d.setColor(new Color(0, 150, 255, 100));
        g2d.setStroke(new BasicStroke(1));
        g2d.drawRect(0, 0, getWidth() - 1, getHeight() - 1);
    }
}
