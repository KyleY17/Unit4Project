import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;

/**
 * TextPop — Floating score/combo text that fades out.
 */
public class TextPop {
    double x, y;
    String text;
    Color color;
    int size;
    int life; // frames left
    static final int MAX_LIFE = 60;

    public TextPop(int x, int y, String text, Color color, int size) {
        this.x = x;
        this.y = y;
        this.text = text;
        this.color = color;
        this.size = size;
        this.life = MAX_LIFE;
    }

    public void update() {
        y -= 1; // float up
        life--;
    }

    public boolean dead() {
        return life <= 0;
    }

    public void draw(Graphics2D g) {
        float alpha = (float) life / MAX_LIFE;
        Color c = new Color(color.getRed(), color.getGreen(), color.getBlue(), (int)(alpha * 255));
        g.setColor(c);
        g.setFont(new Font("Arial", Font.BOLD, size));
        g.drawString(text, (int)x, (int)y);
    }
}