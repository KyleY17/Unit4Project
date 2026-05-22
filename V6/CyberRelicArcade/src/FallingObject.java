import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * FallingObject — Relics, cubes, energy cells, hazards, and power-ups that fall.
 */
public class FallingObject {
    enum Type { RELIC, CUBE, ENERGY, HAZARD, INVINCIBILITY }

    double x, y;
    float speed;
    Type type;
    Color glowColor;
    int basePoints;
    static final int SIZE = 20;

    public FallingObject(int x, int y, float speed, Type type) {
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

    public void update() {
        y += speed;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, SIZE, SIZE);
    }

    public boolean isOffScreen() {
        return y > GamePanel.H;
    }

    public void draw(Graphics2D g) {
        g.setColor(glowColor);
        g.fillOval((int)x, (int)y, SIZE, SIZE);
    }
}