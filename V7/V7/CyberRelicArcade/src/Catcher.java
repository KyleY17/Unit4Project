import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;

/**
 * Catcher — the player-controlled paddle at the bottom.
 */
public class Catcher {
    double x, y; // position
    double vx = 0; // velocity
    boolean movingLeft = false;
    boolean movingRight = false;
    Color color; // distinct player colours
    
    static final double SPEED = 10.0;
    static final int WIDTH = 80;
    static final int HEIGHT = 20;

    public Catcher(double x, double y, Color color) {
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public void update() {
        vx = 0;
        if (movingLeft) vx = -SPEED;
        if (movingRight) vx = SPEED;
        x += vx;
        
        // clamp to screen
        if (x < 0) x = 0;
        if (x + WIDTH > GamePanel.W) x = GamePanel.W - WIDTH;
    }

    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, WIDTH, HEIGHT);
    }

    /**
     * Handles collision with another catcher by pushing this catcher away.
     * Prevents the two players from overlapping.
     * @param other the other catcher to collide with
     */
    public void collideWith(Catcher other) {
        Rectangle myBounds = getBounds();
        Rectangle otherBounds = other.getBounds();
        
        if (myBounds.intersects(otherBounds)) {
            // Calculate the distance between the centers
            double myCenterX = x + WIDTH / 2.0;
            double otherCenterX = other.x + WIDTH / 2.0;
            
            // Push this catcher away from the other based on relative position
            if (myCenterX < otherCenterX) {
                // I'm to the left, push me left
                x = other.x - WIDTH;
            } else {
                // I'm to the right, push me right
                x = other.x + WIDTH;
            }
            
            // Clamp to screen boundaries
            if (x < 0) x = 0;
            if (x + WIDTH > GamePanel.W) x = GamePanel.W - WIDTH;
        }
    }

    public void draw(Graphics2D g) {
        g.setColor(color);
        g.fillRect((int)x, (int)y, WIDTH, HEIGHT);
    }
}