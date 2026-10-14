package animationlab;

import java.awt.Image;
import java.awt.Rectangle;

public class Sprite {

    private Animation anim;
    private float x, y;
    private float dx, dy;

    public Sprite(Animation anim) {
        this.anim = anim;
        x = 0;
        y = 0;
        dx = 0;
        dy = 0;
    }

    public float getX() {
        return x;
    }

    public float getY() {
        return y;
    }

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public void setVelocityX(float dx) {
        this.dx = dx;
    }

    public void setVelocityY(float dy) {
        this.dy = dy;
    }

    public Image getImage() {
        return anim.getImage();
    }

    public void startAnimation() {
        anim.start();
    }

    public void stopAnimation() {
        anim.stop();
    }

    public void update(long elapsedTime) {

        x += dx * elapsedTime;
        y += dy * elapsedTime;

        anim.update(elapsedTime);
    }

    public Rectangle getBounds() {

        return new Rectangle(
                Math.round(x),
                Math.round(y),
                70,     
                110
        );
    }
}
