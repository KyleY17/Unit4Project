import java.awt.Color;
import java.awt.Graphics2D;

/**
 * Particle — Sparkle/explosion visual effects.
 */
public class Particle {
    double x, y;
    double vx, vy;
    Color color;
    int life; // frames left

    public Particle(int x, int y, float vx, float vy, Color color, int life) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.color = color;
        this.life = life;
    }

    public void update() {
        x += vx;
        y += vy;
        life--;
    }

    public boolean dead() {
        return life <= 0;
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int)x, (int)y, 2, 2);
    }
}