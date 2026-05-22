import java.awt.Graphics2D;
import java.util.List;

/**
 * Boss — Boss enemy with projectiles.
 */
public class Boss {
    double x, y;
    int hp;
    int maxHp;
    String name;
    long lastShot = 0;
    static final long SHOT_INTERVAL = 1000; // ms

    public Boss(int W, int level) {
        this.x = W / 2;
        this.y = 100;
        this.maxHp = 100 + level * 50;
        this.hp = maxHp;
        this.name = "BOSS LVL " + level;
    }

    // Now takes both p1 and p2
    public void update(long now, Catcher p1, Catcher p2, List<Particle> particles, GamePanel panel) {
        // simple AI: find nearest catcher to move towards and shoot at
        double dist1 = Math.abs((p1.x + Catcher.WIDTH / 2.0) - this.x);
        double dist2 = (p2 != null) ? Math.abs((p2.x + Catcher.WIDTH / 2.0) - this.x) : Double.MAX_VALUE;
        Catcher target = (dist1 <= dist2) ? p1 : (p2 != null ? p2 : p1);

        double dx = target.x + Catcher.WIDTH / 2.0 - x;
        x += Math.signum(dx) * 2;

        // clamp
        if (x < 50) x = 50;
        if (x > GamePanel.W - 50) x = GamePanel.W - 50;

        // shoot
        if (now - lastShot > SHOT_INTERVAL) {
            // shoot projectile towards the closest catcher
            double vx = (target.x + Catcher.WIDTH / 2.0 - x) / 50; // normalize
            double vy = 4;
            panel.bossProjectiles.add(new BossProjectile(x, y + 50, vx, vy));
            lastShot = now;
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(GamePanel.NEON_RED);
        g.fillRect((int)x - 40, (int)y - 20, 80, 40);
    }
}