/**
 * ╔══════════════════════════════════════════╗
 * ║   C Y B E R   R E L I C   A R C A D E   ║
 * ║   Neon Red & Black Synthwave Catcher     ║
 * ╚══════════════════════════════════════════╝
 *
 * HOW TO COMPILE & RUN:
 *   javac *.java
 *   java CyberRelicArcade
 *
 * CONTROLS:
 *   ← →   Arrow Keys  — Move the magnetic catcher
 *   SPACE             — Activate Magnet Pulse (every 8s)
 *   P                 — Pause / Resume
 *   R                 — Restart (from Game Over screen)
 *
 * PROJECT STRUCTURE:
 *   CyberRelicArcade.java  — Entry point; creates the window (JFrame)
 *   GamePanel.java         — Core game loop, rendering, and input handling
 *   Catcher.java           — The player-controlled paddle at the bottom
 *   FallingObject.java     — Relics, cubes, energy cells, and hazards that fall
 *   Boss.java              — Boss enemy with projectiles
 *   BossProjectile.java    — Bullets fired by the boss
 *   Particle.java          — Sparkle/explosion visual effects
 *   TextPop.java           — Floating score/combo text that fades out
 *   SoundEngine.java       — Pure Java tone synthesizer (no audio files needed)
 */

// javax.swing gives us JFrame (the application window) and SwingUtilities
import javax.swing.*;
import java.awt.*;

/**
 * CyberRelicArcade — the outermost class.
 *
 * All it does is:
 *  1. Create a JFrame (the OS window)
 *  2. Show LoginPanel first for user authentication
 *  3. Upon successful login, switch to GamePanel
 *  4. Pack the window to fit and make it visible
 *
 * Keeping this class tiny is good practice — the "window" and the
 * "game" are separate concerns.
 */
public class CyberRelicArcade extends JFrame {
    private CardLayout cardLayout;
    private JPanel contentPanel;
    private GamePanel gamePanel;
    private UserManager userManager;

    /**
     * main() is always where Java starts.
     * SwingUtilities.invokeLater() makes sure the window is created
     * on the "Event Dispatch Thread" (EDT) — the thread Swing uses
     * to draw and handle mouse/keyboard events.  Forgetting this can
     * cause random graphical glitches.
     */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            CyberRelicArcade frame = new CyberRelicArcade();
            frame.setVisible(true); // actually show the window
        });
    }

    /**
     * Constructor — called once to build the window.
     */
    public CyberRelicArcade() {
        setTitle("CYBER RELIC ARCADE");

        // EXIT_ON_CLOSE means clicking the X button shuts down the JVM
        setDefaultCloseOperation(EXIT_ON_CLOSE);

        // Prevent the user from dragging the window to a different size
        setResizable(false);

        // Initialize UserManager for authentication
        userManager = new UserManager();

        // Create CardLayout for switching between Login and Game panels
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);

        // Create login panel
        LoginPanel loginPanel = new LoginPanel(userManager, username -> {
            // On successful login, set the logged-in user in game panel
            gamePanel.setLoggedInUser(username);
            // Switch to game panel
            cardLayout.show(contentPanel, "GAME");
            // Request focus for the GamePanel to receive keyboard events
            gamePanel.requestFocus();
            gamePanel.startGame();
        });

        // Create game panel with UserManager
        gamePanel = new GamePanel(userManager);

        // Add panels to card layout
        contentPanel.add(loginPanel, "LOGIN");
        contentPanel.add(gamePanel, "GAME");

        // Add the card layout panel to the frame
        add(contentPanel);

        // pack() resizes the frame to exactly fit its contents (the LoginPanel initially)
        pack();

        // Centre the window on the screen
        setLocationRelativeTo(null);

        // Show login panel first
        cardLayout.show(contentPanel, "LOGIN");
    }
}