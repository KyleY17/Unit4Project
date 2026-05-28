import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * StandardFallingObject — Concrete implementation of FallingObject for regular falling items.
 * Represents relics, cubes, energy cells, hazards, and power-ups that fall straight down.
 */
public class StandardFallingObject extends FallingObject {

    public StandardFallingObject(int x, int y, float speed, Type type) {
        super(x, y, speed, type);
    }

    @Override
    public void update() {
        y += speed;
    }

    @Override
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, SIZE, SIZE);
    }

    @Override
    public boolean isOffScreen() {
        return y > GamePanel.H;
    }

    @Override
    public void draw(Graphics2D g) {
        g.setColor(glowColor);
        g.fillOval((int)x, (int)y, SIZE, SIZE);
    }
}
