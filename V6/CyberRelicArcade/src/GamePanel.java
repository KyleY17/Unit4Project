import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.awt.image.BufferedImage;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * GamePanel — the heart of the game.
 *
 * This class does three big jobs:
 *  1. RENDERING  — paintComponent() draws every frame to the screen
 *  2. GAME LOOP  — a javax.swing.Timer fires actionPerformed() ~60 times/sec
 *  3. INPUT      — KeyListener methods respond to keyboard events
 *
 * Why extend JPanel?
 *   JPanel is a blank Swing canvas.  By extending it and overriding
 *   paintComponent() we can draw anything we want on it.
 *
 * Why implement ActionListener?
 *   javax.swing.Timer needs an ActionListener to call back every tick.
 *
 * Why implement KeyListener?
 *   KeyListener lets us intercept raw key presses/releases.
 */
public class GamePanel extends JPanel implements ActionListener, KeyListener {

    // ── Canvas dimensions (pixels) ─────────────────────────────────────────
    static final int W = 700;   // width
    static final int H = 900;   // height

    // ── Colour palette ────────────────────────────────────────────────────
    // new Color(r, g, b) or new Color(r, g, b, alpha)  — alpha 0=transparent, 255=opaque
    static final Color BG          = new Color(4, 0, 0);          // near-black red background
    static final Color NEON_RED    = new Color(255, 20, 50);
    static final Color NEON_YELLOW   = new Color(255, 255, 51);
    static final Color NEON_GREEN = new Color(57, 255, 20);
    static final Color NEON_CYAN   = new Color(0, 230, 255);
    static final Color NEON_WHITE  = new Color(255, 240, 240);
    static final Color NEON_PINK   = new Color(255, 0, 110);
    static final Color GRID_COLOR  = new Color(100, 0, 20, 40);   // very transparent
    static final Color HUD_BG      = new Color(15, 0, 5, 200);
    static final Color DANGER_RED  = new Color(200, 0, 0, 180);

    // ── Timing ───────────────────────────────────────────────────────────
    static final int FPS     = 60;              // target frames per second
    static final int TICK_MS = 1000 / FPS;      // milliseconds between ticks (~16 ms)

    // ── Game state machine ────────────────────────────────────────────────
    // An enum is a fixed list of named constants — perfect for game states
    enum State { TITLE, MODE_SELECT, PLAYING, PAUSED, BOSS, GAME_OVER, SHOP }
    State state = State.TITLE;  // start on the title screen

    // ── Mode selection ─────────────────────────────────────────────────────
    boolean isMultiplayer = true;      // true = multiplayer, false = singleplayer
    int modeSelection = 0;              // 0 = singleplayer, 1 = multiplayer

    // ── Game entities ─────────────────────────────────────────────────────
    Catcher p1; // Player 1 (Arrow Keys)
    Catcher p2; // Player 2 (WASD)                            // the player's paddle
    List<FallingObject> objects   = new ArrayList<>();  // things falling from above
    List<Particle>      particles = new ArrayList<>();  // visual sparkles
    List<TextPop>       textPops  = new ArrayList<>();  // floating "+50" style labels
    Boss boss;                                  // current boss (null when no boss active)
    List<BossProjectile> bossProjectiles = new ArrayList<>();  // boss bullets

    // ── Score / combo tracking ────────────────────────────────────────────
    long score        = 0;
    int  lives        = 3;
    int  bonusLives   = 0;      // extra lives earned from defeating bosses (max 3)
    int  combo        = 0;      // current consecutive-catch streak
    int  maxCombo     = 0;      // highest streak this run (shown on game-over screen)
    long comboEndTime = 0;      // timestamp when the current combo expires
    int  level        = 1;
    int  bossThreshold = 750;  // score needed to trigger the next boss fight
    
    // ── Milestone rewards (every 5000 points) ──────────────────────────────
    boolean hasShield = false;  // shield takes one hit
    boolean hasInvincibility = false;  // temporary invincibility power-up
    long invincibilityEndTime = 0;  // when invincibility expires
    boolean pulseUpgraded = false;  // upgraded pulse is more powerful
    long lastMilestoneScore = 0;  // tracks the last milestone reached

    // ── Spawn / timing variables ──────────────────────────────────────────
    javax.swing.Timer gameTimer;        // fires actionPerformed() every TICK_MS ms
    long gameStartTime;                 // System.currentTimeMillis() at game start
    long lastSpawn     = 0;            // when we last dropped a new object
    int  spawnInterval = 1200;         // ms between spawns (gets smaller = harder)
    long pulseReady    = 0;            // timestamp of last pulse activation
    long lastPulseTime = 0;            // timestamp of when pulse beam was last fired
    static final long PULSE_COOLDOWN = 5000; // 5 seconds between pulses
    static final long PULSE_BEAM_DURATION = 200; // how long the beam visual lasts (ms)

    // ── Visual effects ────────────────────────────────────────────────────
    float gridScrollY = 0;    // how far the background grid has scrolled vertically
    int   shakeFrames = 0;    // >0 means screen is currently shaking
    float titlePulse  = 0;    // angle (radians) used to animate the title screen

    // ── Pre-rendered overlay bitmaps ──────────────────────────────────────
    // We build these ONCE at startup and just blit them every frame (faster than
    // re-drawing them 60 times per second)
    BufferedImage scanlines;  // horizontal dark stripes — classic CRT effect
    BufferedImage vignette;   // dark edges fading to clear in the centre

    // ── Utility ───────────────────────────────────────────────────────────
    Random rng    = new Random();   // used for spawning, shake offsets, etc.
    
    // ── User & High Score Tracking ────────────────────────────────────────
    String loggedInUsername;        // currently logged-in user (null if guest)
    UserManager userManager;        // for saving high scores
    long coinsEarnedThisGame = 0;   // coins earned in the current game

    // ─────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Sets up the panel size, registers keyboard input, and builds the
     * static overlay images.
     */
    @SuppressWarnings({"LeakingThisInConstructor", "OverridableMethodCallInConstructor"})
    GamePanel(UserManager userManager) {
        this.userManager = userManager;
        this.loggedInUsername = null;
        
        // Tell Swing how big this panel should be
        setPreferredSize(new Dimension(W, H));
        setBackground(BG);

        // IMPORTANT: setFocusable(true) is required before addKeyListener works.
        // Without it, the panel never receives keyboard events.
        setFocusable(true);
        addKeyListener(this);

        buildOverlays();          // pre-render scanline + vignette bitmaps
    }
    
    /**
     * Set the currently logged-in user.
     * Called from the login panel after successful authentication.
     */
    public void setLoggedInUser(String username) {
        this.loggedInUsername = username;
    }

    // ─────────────────────────────────────────────────────────────────────
    // GAME LOOP
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Creates and starts the javax.swing.Timer.
     * The timer fires an ActionEvent every TICK_MS milliseconds,
     * which calls our actionPerformed() — one "tick" of the game loop.
     * Called once from the constructor of CyberRelicArcade.
     */
    void startGame() {
        gameTimer = new javax.swing.Timer(TICK_MS, this);
        gameTimer.start();
    }

    /**
     * Resets ALL game state and begins a fresh round.
     * Called when the player presses ENTER on the title screen or R after dying.
     */
    void newGame() {
        // 1. Define 'now' at the start of the method
        long now = System.currentTimeMillis(); 

        // Reset counters
        score = 0;  lives = 3;  bonusLives = 0;  combo = 0;  maxCombo = 0;
        level = 1;  bossThreshold = 750;
        hasShield = false;  hasInvincibility = false;  invincibilityEndTime = 0;  pulseUpgraded = false;  lastMilestoneScore = 0;
        spawnInterval = 1200;

        // Clear entity lists
        objects.clear();
        particles.clear();
        textPops.clear();
        boss = null;

        // Place the catchers based on game mode
        if (isMultiplayer) {
            // Multiplayer: place p1 on the left, p2 on the right
            p1 = new Catcher(W / 3.0 - 40, H - 80, NEON_RED);
            p2 = new Catcher((W / 3.0) * 2 - 40, H - 80, NEON_CYAN);
        } else {
            // Singleplayer: place p1 in the center, p2 is null
            p1 = new Catcher(W / 2.0 - 40, H - 80, NEON_RED);
            p2 = null;
        }

        pulseReady    = 0;
        gameStartTime = now; // 2. Use 'now' here
        lastSpawn     = now; // 3. And here
        shakeFrames   = 0;
        state         = State.PLAYING;
    }
    
    /**
     * Called when game ends. Saves high score if applicable and transitions to GAME_OVER.
     */
    private void endGame() {
        state = State.GAME_OVER;
        shakeFrames = 0;  // Stop screen shake on game over
        
        // Calculate coins earned (score / 15)
        coinsEarnedThisGame = score / 15;
        
        // Save high score and add coins for logged-in user
        if (loggedInUsername != null && userManager != null) {
            userManager.saveHighScore(loggedInUsername, score);
            userManager.addCoins(loggedInUsername, coinsEarnedThisGame);
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRE-RENDERED OVERLAYS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Builds two BufferedImages that are drawn on top of every frame:
     *  1. scanlines — every third row is slightly darker (CRT monitor look)
     *  2. vignette  — a radial gradient that darkens the screen edges
     *
     * BufferedImage is an off-screen bitmap we can draw into,
     * then blit (copy) onto the panel very quickly.
     */
    void buildOverlays() {
        // ── Scanlines ──────────────────────────────────────────────────
        scanlines = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = scanlines.createGraphics();
        for (int y = 0; y < H; y += 3) {
            g.setColor(new Color(0, 0, 0, 28)); // nearly transparent black
            g.fillRect(0, y, W, 1);             // 1-pixel-tall stripe
        }
        g.dispose(); // always dispose a Graphics2D you created manually

        // ── Vignette ───────────────────────────────────────────────────
        vignette = new BufferedImage(W, H, BufferedImage.TYPE_INT_ARGB);
        g = vignette.createGraphics();
        // RadialGradientPaint draws a circle that fades from one colour to another
        RadialGradientPaint vig = new RadialGradientPaint(
            new Point2D.Float(W / 2f, H / 2f),  // centre of the gradient
            Math.max(W, H) * 0.72f,              // radius
            new float[]{0f, 1f},                 // colour-stop positions (0=centre, 1=edge)
            new Color[]{
                new Color(0, 0, 0, 0),           // fully transparent in the centre
                new Color(0, 0, 0, 180)          // dark at the edges
            }
        );
        g.setPaint(vig);
        g.fillRect(0, 0, W, H);
        g.dispose();
    }

    // ─────────────────────────────────────────────────────────────────────
    // TIMER CALLBACK  (called ~60 times/second by the javax.swing.Timer)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * This method is called automatically every TICK_MS milliseconds.
     * ActionEvent e is the event object from the timer — we don't need it here.
     *
     * Steps:
     *  1. Advance title animation counter
     *  2. If the game is running, update all game logic
     *  3. Ask Swing to redraw the panel (schedules paintComponent)
     */
    @Override
    public void actionPerformed(ActionEvent e) {
        long now = System.currentTimeMillis();
        titlePulse += 0.05f; // slowly advances title pulsing animation

        if (state == State.PLAYING || state == State.BOSS) {
            updateGame(now);
        }

        // repaint() does NOT draw immediately; it schedules a call to
        // paintComponent() on the EDT as soon as possible
        repaint();
    }

    // ─────────────────────────────────────────────────────────────────────
    // UPDATE — all game logic lives here
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Advances the entire game world by one frame.
     * @param now  current timestamp from System.currentTimeMillis()
     */
    void updateGame(long now) {

        // ── Screen-shake decay ─────────────────────────────────────────
        // shakeFrames counts down to 0; while >0, rendering applies a random offset
        if (shakeFrames > 0) shakeFrames--;

        // ── Background grid scroll ─────────────────────────────────────
        // Mod 60 keeps the value in [0, 60), creating a seamless loop
        gridScrollY = (gridScrollY + 1.8f) % 60;

        // ── Move the catcher left/right based on held keys ──────────────
        p1.update();
        if (isMultiplayer && p2 != null) p2.update();

        // ── Check player-to-player collision (prevent phasing through) ────
        if (isMultiplayer && p2 != null) {
            p1.collideWith(p2);
            p2.collideWith(p1);
        }

        // ── Spawn new falling objects on a timer ───────────────────────
        if (now - lastSpawn > spawnInterval) {
            spawnObject();
            lastSpawn = now;
            // Difficulty ramp: reduce gap between spawns, minimum 350 ms
            spawnInterval = Math.max(350, spawnInterval - 3);
        }

        // ── Update falling objects; check catches and misses ──────────
        // We collect objects to remove in a separate list because you
        // cannot remove items from a List while iterating over it
        // (that throws ConcurrentModificationException).
        List<FallingObject> toRemove = new ArrayList<>();
        for (FallingObject fo : objects) {
            fo.update();

            // getBounds() returns a Rectangle2D representing the hitbox
            boolean caught = fo.getBounds().intersects(p1.getBounds());
            if (!caught && isMultiplayer && p2 != null) {
                caught = fo.getBounds().intersects(p2.getBounds());
            }
            if (caught) {
                onCatch(fo, now);
                spawnCatchParticles(fo);
                toRemove.add(fo);
            } else if (fo.y > H + 30) {
                // Object fell off the bottom of the screen
                if (fo.type != FallingObject.Type.HAZARD) {
                    onMiss(fo);  // only penalise for non-hazard objects
                }
                toRemove.add(fo);
            }
        }
        objects.removeAll(toRemove);

        // ── Boss logic ────────────────────────────────────────────────
        if (state == State.BOSS && boss != null) {
            boss.update(now, p1, p2, particles, this);
            if (boss.hp <= 0) endBoss(now);
        }

        // ── Update boss projectiles ─────────────────────────────────────
        List<BossProjectile> projToRemove = new ArrayList<>();
        for (BossProjectile proj : bossProjectiles) {
            proj.update();
            boolean hit = proj.getBounds().intersects(p1.getBounds());
            if (!hit && isMultiplayer && p2 != null) {
                hit = proj.getBounds().intersects(p2.getBounds());
            }
            if (hit) {
                // Check for invincibility first
                if (hasInvincibility && now < invincibilityEndTime) {
                    // Do nothing — invincibility absorbs the hit
                } else if (hasShield) {
                    hasShield = false;
                    textPops.add(new TextPop(W / 2, H / 2, "SHIELD BLOCKED!", new Color(0, 200, 255), 28));
                } else if (bonusLives > 0) {
                    bonusLives--;
                    textPops.add(new TextPop(W / 2, H / 2, "BONUS LIFE LOST", new Color(255, 215, 0), 28));
                } else {
                    lives--;
                }
                if (lives <= 0 && bonusLives <= 0 && !hasShield) endGame();
                projToRemove.add(proj);
            } else if (proj.isOffScreen()) {
                projToRemove.add(proj);
            }
        }
        bossProjectiles.removeAll(projToRemove);

        // ── Update particles; remove dead ones ────────────────────────
        // removeIf takes a lambda (condition): removes every particle where dead()==true
        particles.removeIf(p -> { p.update(); return p.dead(); });

        // ── Update text pops; remove expired ones ─────────────────────
        textPops.removeIf(t -> { t.update(); return t.dead(); });

        // ── Check for score milestones (every 5000 points) ────────────────
        long currentMilestone = score / 5000;
        if (currentMilestone > lastMilestoneScore) {
            lastMilestoneScore = currentMilestone;
            hasShield = true;
            pulseUpgraded = true;
            textPops.add(new TextPop(W / 2, H / 2 - 30, "★ MILESTONE ★", NEON_YELLOW, 40));
            textPops.add(new TextPop(W / 2, H / 2 + 30, "SHIELD + PULSE UP", new Color(0, 200, 255), 32));
        }

        // ── Trigger boss fight when score threshold is crossed ─────────
        if (state == State.PLAYING && score >= bossThreshold) {
            triggerBoss(now);
        }

        // ── Expire combo if player hasn't caught anything recently ─────
        if (combo > 0 && now > comboEndTime) {
            combo = 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────
    // GAME EVENTS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Called when the catcher overlaps a falling object.
     * Handles both rewards (normal objects) and damage (hazards).
     */
    void onCatch(FallingObject fo, long now) {
        int pts = fo.basePoints;

        // ── Hazard hit — damages the player ───────────────────────────
        if (fo.type == FallingObject.Type.HAZARD) {
            combo = 0;
            
            // First check for invincibility
            if (hasInvincibility && now < invincibilityEndTime) {
                textPops.add(new TextPop((int) fo.x, (int) fo.y, "INVINCIBLE!", new Color(200, 100, 255), 28));
                spawnCatchParticles(fo);
            } else if (hasShield) {
                hasShield = false;
                textPops.add(new TextPop((int) fo.x, (int) fo.y, "SHIELD BLOCKED!", new Color(0, 200, 255), 28));
            } else if (bonusLives > 0) {
                bonusLives--;
                textPops.add(new TextPop((int) fo.x, (int) fo.y, "BONUS LIFE LOST", new Color(255, 215, 0), 28));
            } else {
                lives--;
                textPops.add(new TextPop((int) fo.x, (int) fo.y, "DAMAGE!", NEON_RED, 28));
            }
            if (!(hasInvincibility && now < invincibilityEndTime)) {
                shakeFrames = 15;
                spawnMissParticles(fo);
                if (lives <= 0 && bonusLives <= 0) {
                    endGame();
                }
            }
            return; // early return — no points awarded
        }

        // ── Invincibility power-up — grants 5 seconds of protection ───
        if (fo.type == FallingObject.Type.INVINCIBILITY) {
            hasInvincibility = true;
            invincibilityEndTime = now + 5000;  // 5 seconds
            combo++;
            if (combo > maxCombo) maxCombo = combo;
            comboEndTime = now + 3000;
            
            int multiplier = Math.min(8, 1 + combo / 3);
            pts *= multiplier;
            score += pts;
            
            textPops.add(new TextPop((int) fo.x, (int) fo.y, "INVINCIBLE!", new Color(200, 100, 255), 28));
            spawnCatchParticles(fo);
            return;
        }

        // ── Normal catch — award points with combo multiplier ─────────
        combo++;
        if (combo > maxCombo) maxCombo = combo;
        comboEndTime = now + 3000; // combo persists for 3 seconds

        // Multiplier grows every 3 catches, capped at 8×
        int multiplier = Math.min(8, 1 + combo / 3);
        pts *= multiplier;
        score += pts;

        // Build the floating label text  (e.g. "x3 +150")
        String label = multiplier > 1 ? "x" + multiplier + " +" + pts : "+" + pts;
        textPops.add(new TextPop((int) fo.x, (int) fo.y, label, fo.glowColor, 24));

        // ── Damage the boss if one is active ──────────────────────────
        if (state == State.BOSS && boss != null) {
            boss.hp -= 10 + level * 2;
            // Spawn red particles at the boss position as a hit flash
            for (int i = 0; i < 10; i++) {
                double a = Math.random() * Math.PI * 2;
                float spd = 2f + (float) Math.random() * 3;
                particles.add(new Particle(
                    (int) boss.x, (int) boss.y,
                    (float) Math.cos(a) * spd,
                    (float) Math.sin(a) * spd,
                    NEON_RED, 25));
            }
            textPops.add(new TextPop((int) boss.x, (int) boss.y, "HIT!", NEON_PINK, 22));
        }

        // ── Milestone combo messages ───────────────────────────────────
        if (combo == 5) {
            textPops.add(new TextPop(W / 2, H / 2 - 60, "COMBO x5!", NEON_PINK, 36));
        }
        if (combo == 10) {
            textPops.add(new TextPop(W / 2, H / 2 - 60, "INSANE x10!", NEON_CYAN, 42));
            shakeFrames = 8;
        }

        // Award points (already calculated above)
    }

    /**
     * Called when a non-hazard object reaches the bottom without being caught.
     * Reduces lives and resets the combo (unless invincible).
     */
    void onMiss(FallingObject fo) {
        long now = System.currentTimeMillis();
        combo = 0;
        
        // If invincible, don't lose lives
        if (hasInvincibility && now < invincibilityEndTime) {
            textPops.add(new TextPop((int) fo.x, H - 100, "INVINCIBLE!", new Color(200, 100, 255), 28));
            return;
        }
        
        // First use bonus lives, then regular lives
        if (bonusLives > 0) {
            bonusLives--;
            textPops.add(new TextPop((int) fo.x, H - 100, "BONUS LIFE LOST", new Color(255, 215, 0), 28));
        } else {
            lives--;
            textPops.add(new TextPop((int) fo.x, H - 100, "MISSED!", NEON_RED, 28));
        }
        shakeFrames = 12;
        spawnMissParticles(fo);
        if (lives <= 0 && bonusLives <= 0) {
            endGame();
        }
    }

    /**
     * Switches to the BOSS state and creates a new Boss instance.
     * Clears all current falling objects so the screen is clean.
     */
    void triggerBoss(long now) {
        state = State.BOSS;
        boss = new Boss(W, level);
        objects.clear();
        shakeFrames = 30;
        textPops.add(new TextPop(W / 2, H / 2, "⚠  BOSS  ⚠", NEON_RED, 52));
    }

    /**
     * Called when the boss HP drops to 0.
     * Awards bonus score, advances level, and returns to normal play.
     */
    void endBoss(long now) {
        state = State.PLAYING;
        level++;
        // Set the next boss trigger point further ahead
        bossThreshold = (int) (score + 600 + level * 150);
        spawnInterval = Math.max(350, spawnInterval - 50); // increase difficulty
        boss = null;
        shakeFrames = 20;
        score += 300L * level;
        textPops.add(new TextPop(W / 2, H / 2, "BOSS SLAIN  +" + (300 * level), NEON_CYAN, 36));
        
        // Award 1 life or bonus life
        if (lives < 3) {
            lives++;
            textPops.add(new TextPop(W / 2, H / 2 + 40, "+ 1 LIFE", NEON_RED, 28));
        } else if (bonusLives < 3) {
            bonusLives++;
            textPops.add(new TextPop(W / 2, H / 2 + 40, "+ 1 BONUS LIFE", new Color(255, 215, 0), 28));
        }
    }

    /**
     * Drops one new FallingObject from a random x position at the top of the screen.
     * Object type is chosen by weighted random chance:
     *   40% RELIC  |  25% CUBE  |  13% ENERGY  |  21% HAZARD  |  1% INVINCIBILITY (rare power-up)
     */
    void spawnObject() {
        double r = Math.random();
        FallingObject.Type type;
        if      (r < 0.40) type = FallingObject.Type.RELIC;
        else if (r < 0.65) type = FallingObject.Type.CUBE;
        else if (r < 0.78) type = FallingObject.Type.ENERGY;
        else if (r < 0.99) type = FallingObject.Type.HAZARD;
        else               type = FallingObject.Type.INVINCIBILITY;

        int x = 30 + rng.nextInt(W - 60);
        // Speed increases with level; also slightly random to keep things unpredictable
        float speed = 2.5f + level * 0.4f + rng.nextFloat() * 2f;
        objects.add(new FallingObject(x, -20, speed, type));
    }

    // ── Particle helpers ──────────────────────────────────────────────────

    /** Burst of coloured sparks at the catch point. */
    void spawnCatchParticles(FallingObject fo) {
        for (int i = 0; i < 18; i++) {
            float ang = (float) (Math.random() * Math.PI * 2);
            float spd = 1.5f + (float) (Math.random() * 4);
            particles.add(new Particle(
                (int) fo.x, (int) fo.y,
                (float) Math.cos(ang) * spd,
                (float) Math.sin(ang) * spd,
                fo.glowColor, 40 + rng.nextInt(20)));
        }
    }

    /** Red sparks at the bottom when an object is missed. */
    void spawnMissParticles(FallingObject fo) {
        for (int i = 0; i < 10; i++) {
            float ang = (float) (Math.random() * Math.PI * 2);
            particles.add(new Particle(
                (int) fo.x, H - 60,
                (float) Math.cos(ang) * 2,
                (float) Math.sin(ang) * 2,
                NEON_RED, 30));
        }
    }

    /** External classes (e.g. Boss) can trigger a screen shake by calling this. */
    public void shake(int frames) { shakeFrames = frames; }

    // ─────────────────────────────────────────────────────────────────────
    // RENDERING
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Swing calls this automatically whenever the panel needs redrawn
     * (after every repaint() call from actionPerformed).
     *
     * We cast Graphics to Graphics2D so we can use anti-aliasing and
     * gradient paints.  Always call super.paintComponent(g) first —
     * it fills the background colour and prevents ghosting.
     */
    @Override
    protected void paintComponent(Graphics g0) {
        super.paintComponent(g0);
        Graphics2D g = (Graphics2D) g0;

        // Anti-aliasing makes curves and diagonal lines look smooth
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,      RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        // ── Screen shake: shift the entire drawing origin randomly ─────
        int sx = 0, sy = 0;
        if (shakeFrames > 0) {
            sx = rng.nextInt(shakeFrames) - shakeFrames / 2;
            sy = rng.nextInt(shakeFrames) - shakeFrames / 2;
        }
        g.translate(sx, sy); // moves the coordinate system

        // Clear to background colour
        g.setColor(BG);
        g.fillRect(-10, -10, W + 20, H + 20);

        // Dispatch to the correct screen renderer
        switch (state) {
            case TITLE:
                drawTitle(g);
                break;
            case MODE_SELECT:
                drawModeSelect(g);
                break;
            case GAME_OVER:
                drawGameOver(g);
                break;
            case SHOP:
                drawShop(g);
                break;
            default:
                drawWorld(g);
                break;
        }

        g.translate(-sx, -sy); // undo the shake translation
    }

    // ─────────────────────────────────────────────────────────────────────
    // DRAW METHODS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Draws the main gameplay view (used for PLAYING, PAUSED, and BOSS states).
     * Order matters: later draws appear on top of earlier ones.
     */
    void drawWorld(Graphics2D g) {
        drawGrid(g);           // scrolling background grid
        drawCatcherBeam(g);    // vertical light beam above catcher

        for (FallingObject fo : objects) fo.draw(g);  // falling items
        if (boss != null) boss.draw(g);                // boss (if present)
        for (BossProjectile proj : bossProjectiles) proj.draw(g);  // boss bullets
        p1.draw(g);
        if (isMultiplayer && p2 != null) p2.draw(g);             // player 2 paddle (multiplayer only)

        // Draw invincibility aura if active
        long now = System.currentTimeMillis();
        if (hasInvincibility && now < invincibilityEndTime) {
            g.setColor(new Color(200, 100, 255, 100));  // Semi-transparent purple
            g.setStroke(new BasicStroke(3));
            g.drawRect((int)p1.x - 5, (int)p1.y - 5, Catcher.WIDTH + 10, Catcher.HEIGHT + 10);
            if (isMultiplayer && p2 != null) {
                g.drawRect((int)p2.x - 5, (int)p2.y - 5, Catcher.WIDTH + 10, Catcher.HEIGHT + 10);
            }
        }

        for (Particle p : particles) p.draw(g);        // sparkles (on top of entities)

        // CRT-style post-processing overlays
        g.drawImage(scanlines, 0, 0, null);
        g.drawImage(vignette,  0, 0, null);

        for (TextPop t : textPops) t.draw(g);   // floating score text (topmost)

        drawHUD(g);    // score/lives bar

        if (state == State.PAUSED) drawPaused(g);
        if (state == State.BOSS)   drawBossHUD(g);
    }

    /**
     * Draws the scrolling perspective grid that gives a synthwave / retro feel.
     * The gridScrollY offset increases each frame, making horizontal lines
     * appear to move toward the viewer.
     */
    void drawGrid(Graphics2D g) {
        g.setColor(GRID_COLOR);
        g.setStroke(new BasicStroke(0.7f));
        int cellW = 60, cellH = 60;

        // Vertical lines (static)
        for (int x = 0; x < W; x += cellW) {
            g.drawLine(x, 0, x, H);
        }

        // Horizontal lines (scrolling — mod keeps them looping)
        for (int y = (int) (gridScrollY % cellH) - cellH; y < H + cellH; y += cellH) {
            g.drawLine(0, y, W, y);
        }

        // Warm glow near the bottom to simulate a horizon
        GradientPaint horizon = new GradientPaint(
            0, H * 0.6f, new Color(180, 0, 30, 0),
            0, H,        new Color(180, 0, 30, 80));
        g.setPaint(horizon);
        g.fillRect(0, (int) (H * 0.6f), W, (int) (H * 0.4f));
    }

    /**
     * Draws a faint red beam rising from the catcher toward the top.
     * This helps the player aim when objects are high up.
     */
    void drawCatcherBeam(Graphics2D g) {
        drawBeamForCatcher(g, p1, NEON_RED);
        if (isMultiplayer && p2 != null) drawBeamForCatcher(g, p2, NEON_CYAN);
    }

    void drawBeamForCatcher(Graphics2D g, Catcher c, Color color) {
        long now = System.currentTimeMillis();
        long timeSincePulse = now - lastPulseTime;
        
        // Draw faint colored beam as aiming guide
        GradientPaint beam = new GradientPaint(
            (float)c.x + 40, (float)c.y - 400, new Color(color.getRed(), color.getGreen(), color.getBlue(), 0),
            (float)c.x + 40, (float)c.y,       new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        g.setPaint(beam);
        g.fillRect((int) c.x + 38, (int) c.y - 400, 4, 400);
        
        // Draw bright white beam with black border when pulse is recently fired
        if (timeSincePulse < PULSE_BEAM_DURATION) {
            // Calculate fade-out effect
            float fadeAlpha = 1.0f - (timeSincePulse / (float) PULSE_BEAM_DURATION);
            int alpha = (int) (255 * fadeAlpha);
            
            // Draw black border
            g.setColor(new Color(0, 0, 0, alpha));
            g.fillRect((int) c.x + 35, (int) c.y - 400, 10, 400);
            
            // Draw white beam
            g.setColor(new Color(255, 255, 255, alpha));
            g.fillRect((int) c.x + 37, (int) c.y - 400, 6, 400);
        }
    }

    /**
     * Draws the heads-up display (HUD) at the top and bottom of the screen:
     *  • Score and level label
     *  • Heart icons for remaining lives
     *  • Combo bar that shrinks as the combo timer expires
     *  • Pulse-charge bar at the very bottom
     */
    void drawHUD(Graphics2D g) {
        // Translucent background strip
        g.setColor(HUD_BG);
        g.fillRect(0, 0, W, 56);
        g.setColor(NEON_RED);
        g.setStroke(new BasicStroke(1f));
        g.drawLine(0, 56, W, 56);

        // Score (left-aligned)
        drawNeonText(g, "SCORE",                          18, 16, NEON_RED,   false);
        drawNeonText(g, String.format("%08d", score),     26, 34, NEON_WHITE, true);

        // Level (centred)
        drawNeonText(g, "LVL " + level, W / 2, 34, NEON_PINK, true);

        // Hearts — draw 3 red hearts and up to 3 golden bonus hearts
        // Red hearts (normal lives)
        for (int i = 0; i < 3; i++) {
            Color c = i < lives ? NEON_RED : new Color(60, 0, 0); // dark = lost life
            drawHeart(g, W - 28 - i * 30, 28, c);
        }
        // Golden hearts (bonus lives)
        for (int i = 0; i < bonusLives; i++) {
            Color c = new Color(255, 215, 0); // golden
            drawHeart(g, W - 28 - (3 + i) * 30, 28, c);
        }
        
        // Shield indicator
        if (hasShield) {
            drawNeonText(g, "SHIELD", W - 180, 34, new Color(0, 200, 255), false);
        }
        
        // Invincibility indicator with timer
        long now3 = System.currentTimeMillis();
        if (hasInvincibility && now3 < invincibilityEndTime) {
            long timeRemaining = invincibilityEndTime - now3;
            float secs = timeRemaining / 1000f;
            drawNeonText(g, String.format("INVINCIBLE (%.1fs)", secs), W - 320, 34, new Color(200, 100, 255), false);
        }

        // ── Combo timer bar ───────────────────────────────────────────
        if (combo > 0) {
            long now = System.currentTimeMillis();
            // pct goes from 1.0 (full) down to 0.0 as the combo expires
            float pct = Math.max(0, (comboEndTime - now) / 3000f);
            int barW = (int) ((W - 40) * pct);

            g.setColor(new Color(255, 20, 50, 50)); // dim background track
            g.fillRect(20, 60, W - 40, 6);

            GradientPaint comboGrad = new GradientPaint(20, 0, NEON_RED, 20 + barW, 0, NEON_PINK);
            g.setPaint(comboGrad);
            g.fillRect(20, 60, barW, 6);

            drawNeonText(g, "COMBO x" + combo, W / 2, 84, NEON_PINK, true);
        }

        // ── Pulse charge bar (bottom of screen) ──────────────────────
        long now2        = System.currentTimeMillis();
        long sinceGame   = now2 - gameStartTime;
        // Calculate how charged the pulse is (0.0 → 1.0)
        float pulsePct   = pulseReady == 0
            ? Math.min(1f, sinceGame / (float) PULSE_COOLDOWN)
            : Math.min(1f, (now2 - pulseReady) / (float) PULSE_COOLDOWN);

        g.setColor(new Color(0, 230, 255, 40)); // dim background track
        g.fillRect(0, H - 6, W, 6);
        g.setColor(NEON_CYAN);
        g.fillRect(0, H - 6, (int) (W * pulsePct), 6);

        if (pulsePct >= 1f) {
            drawNeonText(g, "[ SPACE ] PULSE READY", W / 2, H - 14, NEON_CYAN, true);
        }
    }

    /**
     * Draws the boss's HP bar below the top HUD panel.
     */
    void drawBossHUD(Graphics2D g) {
        if (boss == null) return;
        int bw = W - 100, bx = 50, by = 70;

        // Background (dark red track)
        g.setColor(new Color(60, 0, 0));
        g.fillRoundRect(bx, by, bw, 14, 7, 7);

        // Foreground (fills according to current HP ratio)
        float hpPct = (float) boss.hp / boss.maxHp;
        GradientPaint bossGrad = new GradientPaint(bx, 0, DANGER_RED, bx + bw, 0, NEON_PINK);
        g.setPaint(bossGrad);
        g.fillRoundRect(bx, by, (int) (bw * hpPct), 14, 7, 7);

        g.setColor(NEON_RED);
        g.setStroke(new BasicStroke(1f));
        g.drawRoundRect(bx, by, bw, 14, 7, 7);

        drawNeonText(g, "BOSS  " + boss.name, W / 2, by - 5, NEON_RED, true);
    }

    /**
     * Draws a small heart icon at (cx, cy) using a Bezier curve path.
     * GeneralPath lets us build arbitrary vector shapes programmatically.
     */
    void drawHeart(Graphics2D g, int cx, int cy, Color c) {
        g.setColor(c);
        GeneralPath heart = new GeneralPath();
        double s = 9;
        heart.moveTo(cx, cy + s * 0.25);
        heart.curveTo(cx,     cy - s * 0.3, cx - s, cy - s * 0.3, cx - s, cy + s * 0.2);
        heart.curveTo(cx - s, cy + s * 0.7, cx,     cy + s,        cx,     cy + s);
        heart.curveTo(cx,     cy + s,        cx + s, cy + s * 0.7, cx + s, cy + s * 0.2);
        heart.curveTo(cx + s, cy - s * 0.3, cx,     cy - s * 0.3, cx,     cy + s * 0.25);
        g.fill(heart);

        // Soft outer glow — draw the path outline with a wide, transparent stroke
        g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
        g.setStroke(new BasicStroke(5f));
        g.draw(heart);
    }

    /**
     * Draws glowing text.  Produces the neon effect by drawing the same
     * string multiple times in a transparent, slightly offset colour
     * before drawing the fully-opaque version on top.
     *
     * @param centered  if true, the text is horizontally centred on x
     */
    void drawNeonText(Graphics2D g, String text, int x, int y, Color c, boolean centered) {
        Font f = new Font("Courier New", Font.BOLD, 13);
        g.setFont(f);
        FontMetrics fm = g.getFontMetrics();
        int tx = centered ? x - fm.stringWidth(text) / 2 : x;

        // Glow: draw concentric offset copies in a very transparent colour
        for (int glow = 8; glow >= 0; glow -= 2) {
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), 18));
            g.drawString(text, tx - glow / 2, y + glow / 2);
        }
        // Crisp foreground text
        g.setColor(c);
        g.drawString(text, tx, y);
    }

    // ─────────────────────────────────────────────────────────────────────
    // SCREEN-SPECIFIC DRAW METHODS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Draws the animated title / attract screen.
     */
    void drawTitle(Graphics2D g) {
        drawGrid(g);

        // pulse oscillates between 0 and 1 using a sine wave
        float pulse = (float) (Math.sin(titlePulse) * 0.5 + 0.5);
        int alpha   = (int) (150 + pulse * 105);

        // ── Big title text ─────────────────────────────────────────────
        Font titleFont = new Font("Courier New", Font.BOLD, 48);
        g.setFont(titleFont);
        String line1 = "CYBER RELIC";
        String line2 = "ARCADE";
        FontMetrics fm = g.getFontMetrics();

        // Glow layers (drawn first, behind the main text)
        for (int glow = 20; glow >= 0; glow -= 4) {
            int a = (int) (40 * (1f - glow / 20f));
            g.setColor(new Color(255, 20, 50, a));
            g.drawString(line1, (W - fm.stringWidth(line1)) / 2 - glow / 2, 260 + glow / 2);
            g.drawString(line2, (W - fm.stringWidth(line2)) / 2 - glow / 2, 320 + glow / 2);
        }
        g.setColor(new Color(255, 20, 50, alpha));
        g.drawString(line1, (W - fm.stringWidth(line1)) / 2, 260);
        g.setColor(new Color(255, 0, 110, alpha));
        g.drawString(line2, (W - fm.stringWidth(line2)) / 2, 320);

        // ── Subtitle ───────────────────────────────────────────────────
        Font subFont = new Font("Courier New", Font.PLAIN, 14);
        g.setFont(subFont);
        String sub = "[ MAGNETIC CATCHER ] [ NEON RELICS ] [ BOSS EVENTS ]";
        g.setColor(new Color(255, 80, 80, 180));
        g.drawString(sub, (W - g.getFontMetrics().stringWidth(sub)) / 2, 370);

        // ── Controls list ──────────────────────────────────────────────
        String[] controls = {
            "P1: ← → ARROWS (Move)  |  SPACE (Pulse)",
            "P2: A D KEYS (Move)  |  W (Pulse)",
            "R TO RESTART  |  P TO PAUSE  |  ESC TO QUIT",
            "PULSE: Clears all non-hazard objects on screen, but has a cooldown timer of 8 seconds.",
        };
        Font ctrlFont = new Font("Courier New", Font.PLAIN, 12);
        g.setFont(ctrlFont);
        for (int i = 0; i < controls.length; i++) {
            g.setColor(new Color(200, 100, 100, 200));
            String s = controls[i];
            g.drawString(s, (W - g.getFontMetrics().stringWidth(s)) / 2, 460 + i * 22);
        }

        // ── Blinking "press start" prompt ─────────────────────────────
        // The (int)(titlePulse * 4) % 2 expression alternates between 0 and 1
        // quickly, creating a blink effect
        if ((int) (titlePulse * 4) % 2 == 0) {
            Font startFont = new Font("Courier New", Font.BOLD, 20);
            g.setFont(startFont);
            String start = "[ PRESS  ENTER  TO  JACK  IN ]";
            g.setColor(NEON_WHITE);
            g.drawString(start, (W - g.getFontMetrics().stringWidth(start)) / 2, 560);
        }

        // Decorative horizontal rule lines
        g.setColor(new Color(255, 20, 50, 80));
        g.setStroke(new BasicStroke(1f));
        g.drawLine(60, 590, W - 60, 590);


        // CRT overlays (always on top)
        g.drawImage(scanlines, 0, 0, null);
        g.drawImage(vignette,  0, 0, null);
    }

    /**
     * Draws the mode selection screen (Singleplayer vs Multiplayer).
     */
    void drawModeSelect(Graphics2D g) {
        drawGrid(g);

        // Semi-transparent dark overlay
        g.setColor(new Color(0, 0, 0, 100));
        g.fillRect(0, 0, W, H);

        // Title text
        Font titleFont = new Font("Courier New", Font.BOLD, 48);
        g.setFont(titleFont);
        String title = "SELECT MODE";
        FontMetrics fm = g.getFontMetrics();
        g.setColor(NEON_RED);
        g.drawString(title, (W - fm.stringWidth(title)) / 2, 150);

        // Button dimensions
        int buttonWidth = 200;
        int buttonHeight = 80;
        int buttonSpacing = 80;
        int totalWidth = buttonWidth * 2 + buttonSpacing;
        int startX = (W - totalWidth) / 2;
        int buttonY = 300;

        // Draw SINGLEPLAYER button
        drawModeButton(g, "SINGLEPLAYER", startX, buttonY, buttonWidth, buttonHeight, modeSelection == 0);

        // Draw MULTIPLAYER button
        drawModeButton(g, "MULTIPLAYER", startX + buttonWidth + buttonSpacing, buttonY, buttonWidth, buttonHeight, modeSelection == 1);

        // Instructions
        Font instrFont = new Font("Courier New", Font.PLAIN, 14);
        g.setFont(instrFont);
        g.setColor(new Color(200, 100, 100, 200));
        String instr = "[ USE ARROW KEYS OR A/D TO SELECT ] [ PRESS ENTER TO START ]";
        g.drawString(instr, (W - g.getFontMetrics().stringWidth(instr)) / 2, 520);

        // CRT overlays (always on top)
        g.drawImage(scanlines, 0, 0, null);
        g.drawImage(vignette,  0, 0, null);
    }

    /**
     * Helper method to draw a mode selection button.
     */
    void drawModeButton(Graphics2D g, String label, int x, int y, int width, int height, boolean selected) {
        // Draw border
        if (selected) {
            g.setColor(NEON_WHITE);
            g.setStroke(new BasicStroke(3f));
        } else {
            g.setColor(new Color(255, 80, 80, 150));
            g.setStroke(new BasicStroke(2f));
        }
        g.drawRect(x, y, width, height);

        // Draw button background
        if (selected) {
            g.setColor(new Color(255, 20, 50, 80));
        } else {
            g.setColor(new Color(20, 0, 10, 50));
        }
        g.fillRect(x, y, width, height);

        // Draw text
        Font btnFont = new Font("Courier New", Font.BOLD, 18);
        g.setFont(btnFont);
        FontMetrics fm = g.getFontMetrics();
        g.setColor(selected ? NEON_WHITE : new Color(200, 100, 100, 200));
        int textX = x + (width - fm.stringWidth(label)) / 2;
        int textY = y + ((height - fm.getHeight()) / 2) + fm.getAscent();
        g.drawString(label, textX, textY);
    }

    /**
     * Draws the Game Over screen with the player's final score, level, max combo, and leaderboard.
     */
    void drawGameOver(Graphics2D g) {
        drawGrid(g);

        // Dim red overlay across the whole screen
        g.setColor(new Color(60, 0, 0, 160));
        g.fillRect(0, 0, W, H);

        // Show coins earned for logged-in users at the top
        if (loggedInUsername != null && coinsEarnedThisGame > 0) {
            Font coinsFont = new Font("Courier New", Font.BOLD, 18);
            g.setFont(coinsFont);
            FontMetrics cfm = g.getFontMetrics();
            String coins = "COINS EARNED  +" + coinsEarnedThisGame;
            g.setColor(new Color(255, 215, 0));  // Golden color
            g.drawString(coins, (W - cfm.stringWidth(coins)) / 2, 100);
        }

        // Big "GAME OVER" text with glow
        Font goFont = new Font("Courier New", Font.BOLD, 56);
        g.setFont(goFont);
        String go = "GAME OVER";
        FontMetrics fm = g.getFontMetrics();
        for (int gl = 16; gl >= 0; gl -= 4) {
            g.setColor(new Color(255, 0, 0, 25));
            g.drawString(go, (W - fm.stringWidth(go)) / 2 - gl / 2, 220 + gl / 2);
        }
        g.setColor(NEON_RED);
        g.drawString(go, (W - fm.stringWidth(go)) / 2, 220);

        // Stats
        Font infoFont = new Font("Courier New", Font.BOLD, 18);
        g.setFont(infoFont);
        FontMetrics fi = g.getFontMetrics();

        String sc = "FINAL SCORE  " + String.format("%08d", score);
        g.setColor(NEON_WHITE);
        g.drawString(sc, (W - fi.stringWidth(sc)) / 2, 290);

        String lv = "LEVEL REACHED  " + level;
        g.setColor(NEON_PINK);
        g.drawString(lv, (W - fi.stringWidth(lv)) / 2, 320);

        String mc = "MAX COMBO  x" + maxCombo;
        g.setColor(NEON_CYAN);
        g.drawString(mc, (W - fi.stringWidth(mc)) / 2, 350);
        
        // Draw leaderboard
        drawLeaderboard(g);

        // Blinking instruction text
        Font restFont = new Font("Courier New", Font.BOLD, 16);
        g.setFont(restFont);
        FontMetrics fr = g.getFontMetrics();
        String restart = loggedInUsername != null ? "[ R ]  SHOP   |   [ ESC ]  QUIT" : "[ R ]  RESTART   |   [ ESC ]  QUIT";
        g.setColor(new Color(255, 80, 80, (int) (150 + 105 * Math.sin(titlePulse * 3))));
        g.drawString(restart, (W - fr.stringWidth(restart)) / 2, 870);

        g.drawImage(scanlines, 0, 0, null);
        g.drawImage(vignette,  0, 0, null);
    }

    /**
     * Draws the top 5 high scores on the game over screen.
     */
    void drawLeaderboard(Graphics2D g) {
        if (userManager == null) return;
        
        int leaderboardY = 470;
        int lineHeight = 28;
        
        // Title - centered
        Font titleFont = new Font("Courier New", Font.BOLD, 16);
        g.setFont(titleFont);
        g.setColor(NEON_YELLOW);
        String titleText = "TOP SCORES";
        FontMetrics titleFm = g.getFontMetrics();
        int titleX = (W - titleFm.stringWidth(titleText)) / 2;
        g.drawString(titleText, titleX, leaderboardY);
        
        // Get top 5 scores
        List<String[]> topScores = userManager.getTopScores(5);
        
        Font scoreFont = new Font("Courier New", Font.PLAIN, 14);
        g.setFont(scoreFont);
        FontMetrics scoreFm = g.getFontMetrics();
        
        int rank = 1;
        for (String[] entry : topScores) {
            String username = entry[0];
            String scoreStr = entry[1];
            
            // Highlight current player's entry with green text
            boolean isCurrentPlayer = (loggedInUsername != null) && username.equals(loggedInUsername);
            g.setColor(isCurrentPlayer ? NEON_GREEN : NEON_WHITE);
            String rankStr = String.format("%d. %s: %s", rank, username, scoreStr);
            int entryX = (W - scoreFm.stringWidth(rankStr)) / 2;
            g.drawString(rankStr, entryX, leaderboardY + rank * lineHeight);
            rank++;
        }
    }

    /**
     * Draws the shop menu screen where users can view coins and purchase items.
     */
    void drawShop(Graphics2D g) {
        // Dark background
        g.setColor(new Color(15, 15, 25));
        g.fillRect(0, 0, W, H);

        // Title
        Font titleFont = new Font("Courier New", Font.BOLD, 48);
        g.setFont(titleFont);
        g.setColor(new Color(0, 150, 255));
        String title = "SHOP";
        FontMetrics fm = g.getFontMetrics();
        g.drawString(title, (W - fm.stringWidth(title)) / 2, 100);

        // Coins display
        Font coinsFont = new Font("Courier New", Font.BOLD, 24);
        g.setFont(coinsFont);
        long currentCoins = (loggedInUsername != null) ? userManager.getCoins(loggedInUsername) : 0;
        long totalCoins = currentCoins + coinsEarnedThisGame;
        
        g.setColor(new Color(220, 230, 255));
        String coinsText = "COINS: " + totalCoins + " (+" + coinsEarnedThisGame + " earned)";
        FontMetrics cfm = g.getFontMetrics();
        g.drawString(coinsText, (W - cfm.stringWidth(coinsText)) / 2, 180);

        // Coming soon message
        Font messageFont = new Font("Courier New", Font.PLAIN, 20);
        g.setFont(messageFont);
        g.setColor(new Color(200, 200, 200));
        String message = "SHOP ITEMS COMING SOON!";
        FontMetrics mfm = g.getFontMetrics();
        g.drawString(message, (W - mfm.stringWidth(message)) / 2, 320);

        // Back to menu instruction
        Font instructFont = new Font("Courier New", Font.BOLD, 16);
        g.setFont(instructFont);
        g.setColor(new Color(100, 180, 255));
        String instruct = "[ M ]  BACK TO MENU";
        FontMetrics ifm = g.getFontMetrics();
        g.drawString(instruct, (W - ifm.stringWidth(instruct)) / 2, 520);

        g.drawImage(scanlines, 0, 0, null);
        g.drawImage(vignette,  0, 0, null);
    }

    /**
     * Draws a semi-transparent "— PAUSED —" overlay over the gameplay.
     */
    void drawPaused(Graphics2D g) {
        g.setColor(new Color(0, 0, 0, 160));
        g.fillRect(0, 0, W, H);
        Font pf = new Font("Courier New", Font.BOLD, 40);
        g.setFont(pf);
        String p = "— PAUSED —";
        FontMetrics fm = g.getFontMetrics();
        g.setColor(NEON_RED);
        g.drawString(p, (W - fm.stringWidth(p)) / 2, H / 2);
    }

    // ─────────────────────────────────────────────────────────────────────
    // KEYBOARD INPUT  (KeyListener interface)
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Called the moment a key is pressed down.
     * We check the key code and update game state accordingly.
     */
    @Override
    public void keyPressed(KeyEvent e) {
        int k = e.getKeyCode(); // e.g. KeyEvent.VK_LEFT, VK_RIGHT, VK_SPACE ...

        // Title screen: only ENTER does anything
        if (state == State.TITLE) {
            if (k == KeyEvent.VK_ENTER) state = State.MODE_SELECT;
            return;
        }

        // Mode select screen: arrow keys to navigate, ENTER to select
        if (state == State.MODE_SELECT) {
            switch (k) {
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_A:
                    modeSelection = 0;  // Singleplayer
                    break;
                case KeyEvent.VK_RIGHT:
                case KeyEvent.VK_D:
                    modeSelection = 1;  // Multiplayer
                    break;
                case KeyEvent.VK_ENTER:
                    isMultiplayer = (modeSelection == 1);
                    newGame();
                    break;
                default:
                    break;
            }
            return;
        }

        // Game Over screen: R to restart, ESC to quit
        if (state == State.GAME_OVER) {
            if (k == KeyEvent.VK_R) {
                // Go to shop if logged in, otherwise new game or exit
                if (loggedInUsername != null) {
                    state = State.SHOP;
                } else {
                    newGame();
                }
            }
            if (k == KeyEvent.VK_ESCAPE) System.exit(0);
            return;
        }

        // Shop screen: M to back to menu
        if (state == State.SHOP) {
            if (k == KeyEvent.VK_M) {
                state = State.TITLE;
            }
            if (k == KeyEvent.VK_ESCAPE) System.exit(0);
            return;
        }

        // P toggles pause from any playing state
        if (k == KeyEvent.VK_P) {
            state = (state == State.PAUSED) ? State.PLAYING : State.PAUSED;
            return;
        }

        // While paused, ignore everything else
        if (state == State.PAUSED) return;

        // Normal gameplay controls
        if (p1 != null) {
            if (k == KeyEvent.VK_LEFT)  p1.movingLeft  = true;
            if (k == KeyEvent.VK_RIGHT) p1.movingRight = true;
            if (k == KeyEvent.VK_SPACE) activatePulse(p1);
        }
        
        if (isMultiplayer && p2 != null) {
            if (k == KeyEvent.VK_A) p2.movingLeft = true;
            if (k == KeyEvent.VK_D) p2.movingRight = true;
            if (k == KeyEvent.VK_W) activatePulse(p2);
        }

        if (k == KeyEvent.VK_R)     newGame();
    }

    /**
     * Called when a key is released.
     * We stop the catcher moving in that direction.
     */
    @Override
    public void keyReleased(KeyEvent e) {
        int k = e.getKeyCode();
        if (p1 != null) {
            if (k == KeyEvent.VK_LEFT)  p1.movingLeft  = false;
            if (k == KeyEvent.VK_RIGHT) p1.movingRight = false;
        }
        
        if (isMultiplayer && p2 != null) {
            if (k == KeyEvent.VK_A) p2.movingLeft = false;
            if (k == KeyEvent.VK_D) p2.movingRight = false;
        }
    }

    /** Required by KeyListener but we don't use typed-key events. */
    @Override
    public void keyTyped(KeyEvent e) {}

    // ─────────────────────────────────────────────────────────────────────
    // PULSE ABILITY
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Activates the Magnetic Pulse if it has fully recharged.
     *
     * Effect:
     *  • Yanks nearby falling objects horizontally toward the catcher
     *  • Speeds them up so they arrive faster
     *  • Spawns a ring of cyan particles for visual feedback
     */
void activatePulse(Catcher c) {
        long now = System.currentTimeMillis();
        
        // Check if pulse is still on cooldown
        if (now - pulseReady < PULSE_COOLDOWN) {
            return;  // Not ready yet
        }

        pulseReady  = now;
        lastPulseTime = now;  // Record the time when the beam fires
        shakeFrames = pulseUpgraded ? 12 : 8;  // Upgraded pulse shakes more

        // Pull objects towards the player who triggered it
        for (FallingObject fo : objects) {
            double dx   = c.x + 40 - fo.x; // +40 targets centre of paddle
            float dist = Math.abs((float)dx);
            float range = pulseUpgraded ? 300 : 220;  // Upgraded pulse has larger range
            float strength = pulseUpgraded ? 0.8f : 0.6f;  // Upgraded pulse is stronger
            
            if (dist < range) {
                fo.x    += dx * strength;  
                fo.speed += (pulseUpgraded ? 5f : 3f);  // Upgraded pulse accelerates more
            }
        }

        // Particle effect originating from the active player (upgraded version has more particles)
        int particleCount = pulseUpgraded ? 60 : 40;
        for (int i = 0; i < particleCount; i++) {
            double ang = Math.PI * 2 * i / particleCount;
            float speed = pulseUpgraded ? 7 : 5;
            particles.add(new Particle(
                (int) c.x + 40, (int) c.y,
                (float) Math.cos(ang) * speed,
                (float) Math.sin(ang) * speed,
                c.color, 30)); // match player's color
        }
        String pulseText = pulseUpgraded ? "PULSE ★" : "PULSE!";
        textPops.add(new TextPop(W / 2, H / 2, pulseText, c.color, 34));
        textPops.add(new TextPop(W / 2, H / 2, pulseText, NEON_CYAN, 34));
    }
}