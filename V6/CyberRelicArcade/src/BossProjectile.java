import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * BossProjectile — Bullets fired by the boss. Extends FallingObject.
 */
public class BossProjectile extends FallingObject {
    static final int PROJ_SIZE = 10;  // Boss projectiles are smaller than regular falling objects
    double vx, vy;

    public BossProjectile(double x, double y, double vx, double vy) {
        super((int)x, (int)y, 0, FallingObject.Type.HAZARD);  // Initialize as HAZARD type
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.glowColor = GamePanel.NEON_RED;
    }

    @Override
    public void update() {
        x += vx;
        y += vy;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, PROJ_SIZE, PROJ_SIZE);
    }

    @Override
    public boolean isOffScreen() {
        return y > GamePanel.H || x < 0 || x > GamePanel.W;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(glowColor);
        g.fillRect((int)x, (int)y, PROJ_SIZE, PROJ_SIZE);
    }
}