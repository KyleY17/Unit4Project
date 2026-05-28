import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * FallingObject — Abstract base class for relics, cubes, energy cells, hazards, and power-ups that fall.
 * Subclasses must implement update(), getBounds(), isOffScreen(), and draw().
 */
public abstract class FallingObject {
    enum Type { RELIC, CUBE, ENERGY, HAZARD, INVINCIBILITY }

    protected double x, y;
    protected float speed;
    protected Type type;
    protected Color glowColor;
    protected int basePoints;
    static final int SIZE = 20;

    protected FallingObject(int x, int y, float speed, Type type) {
        this.x = x;
        this.y = y;
        this.speed = speed;
        this.type = type;
        switch (type) {
            case RELIC: glowColor = GamePanel.NEON_CYAN; basePoints = 10; break;
            case CUBE: glowColor = GamePanel.NEON_GREEN; basePoints = 20; break;
            case ENERGY: glowColor = GamePanel.NEON_YELLOW; basePoints = 50; break;
            case HAZARD: glowColor = GamePanel.NEON_RED; basePoints = 0; break;
            case INVINCIBILITY: glowColor = new Color(200, 100, 255); basePoints = 500; break; // Purple
        }
    }

    /**
     * Updates the position and state of this falling object.
     * Subclasses must provide their own implementation.
     */
    public abstract void update();

    /**
     * Returns the bounding rectangle for collision detection.
     * Subclasses must provide their own implementation.
     */
    public abstract Rectangle getBounds();

    /**
     * Checks if this object has moved off-screen.
     * Subclasses must provide their own implementation.
     */
    public abstract boolean isOffScreen();

    /**
     * Renders this object to the graphics context.
     * Subclasses must provide their own implementation.
     */
    public abstract void draw(Graphics2D g);
}