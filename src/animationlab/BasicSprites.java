package animationlab;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BasicSprites extends JFrame implements Runnable, KeyListener {

    private Thread gameThread;
    private boolean running = true;
    private Image screen;

    private int scwidth, scheight;

    private BufferedImage spritesheet;
    private BufferedImage[] frames;

    private final int rows = 1;
    private final int cols = 8;
    private final int framewidth = 92;
    private final int frameheight = 187;

    private final int SPRITE_WIDTH = 55;
    private final int SPRITE_HEIGHT = 110;

    private Sprite player;
    private Sprite enemy;

    private final float ENEMY_DETECTION_RANGE = 300f;
    private final float ENEMY_STOP_DISTANCE = 80f;
    private final float ENEMY_SPEED = 0.14f;

    private boolean isAPressed;
    private boolean isDPressed;
    private boolean isWPressed;
    private boolean isSPressed;

    public BasicSprites(String title, int width, int height) {

        super(title);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(width, height);

        scwidth = width;
        scheight = height;

        addKeyListener(this);
        setFocusable(true);

        loadSprites();

        gameThread = new Thread(this);
    }

    private void loadSprites() {
        try {
            spritesheet = ImageIO.read(new File("spritesheet.png"));
            System.out.println("Spritesheet Loaded!");
        } catch (IOException ex) {
            System.out.println("Could not load spritesheet.");
            return;
        }

        frames = new BufferedImage[rows * cols];

        for (int r = 0; r < rows; r++) {
            for (int c = 0; c < cols; c++) {
                frames[(r * cols) + c] =
                    spritesheet.getSubimage(
                        c * framewidth,
                        r * frameheight,
                        framewidth,
                        frameheight);
            }
        }

        Animation playerAnim = new Animation();

        for (BufferedImage frame : frames) {
            playerAnim.addFrame(frame, 100);
        }

        player = new Sprite(playerAnim);
        player.setX(400);
        player.setY(250);

        Animation enemyAnim = new Animation();

        for (BufferedImage frame : frames) {
            enemyAnim.addFrame(frame, 100);
        }

        enemy = new Sprite(enemyAnim);
        enemy.setX(100);
        enemy.setY(100);
    }

    private void processInput() {
        float velocityX = 0;
        float velocityY = 0;

        if (isAPressed) velocityX = -0.18f;
        if (isDPressed) velocityX = 0.18f;
        if (isWPressed) velocityY = -0.18f;
        if (isSPressed) velocityY = 0.18f;

        player.setVelocityX(velocityX);
        player.setVelocityY(velocityY);

        if (velocityX != 0 || velocityY != 0)
            player.startAnimation();
        else
            player.stopAnimation();
    }

    private void updateEnemyAI() {
        Rectangle playerBounds = player.getBounds();
        Rectangle enemyBounds = enemy.getBounds();

        float playerCenterX = playerBounds.x + playerBounds.width / 2f;

        float playerCenterY = playerBounds.y + playerBounds.height / 2f;

        float enemyCenterX = enemyBounds.x + enemyBounds.width / 2f;

        float enemyCenterY = enemyBounds.y + enemyBounds.height / 2f;

        float differenceX = playerCenterX - enemyCenterX;
        float differenceY = playerCenterY - enemyCenterY;
        float distance = (float)Math.sqrt(differenceX * differenceX + differenceY * differenceY);

        if (distance > ENEMY_DETECTION_RANGE) {
            enemy.setVelocityX(0);
            enemy.setVelocityY(0);
            enemy.stopAnimation();
            return;
        }

        if (distance <= ENEMY_STOP_DISTANCE) {
            enemy.setVelocityX(0);
            enemy.setVelocityY(0);
            enemy.stopAnimation();
            return;
        }

        float directionX = differenceX / distance;
        float directionY = differenceY / distance;

        enemy.setVelocityX(directionX * ENEMY_SPEED);
        enemy.setVelocityY(directionY * ENEMY_SPEED);

        enemy.startAnimation();
    }

    private boolean checkCollision() {
        return player.getBounds().intersects(enemy.getBounds());
    }

    @Override
    public void run() {

        long previousTime = System.currentTimeMillis();

        while (running) {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - previousTime;

            previousTime = currentTime;

            processInput();
            updateEnemyAI();

            player.update(elapsedTime);
            enemy.update(elapsedTime);

            if (player.getX() < 0)
                player.setX(0);

            if (player.getY() < 0)
                player.setY(0);

            if (player.getX() > scwidth - SPRITE_WIDTH)
                player.setX(scwidth - SPRITE_WIDTH);

            if (player.getY() > scheight - SPRITE_HEIGHT)
                player.setY(scheight - SPRITE_HEIGHT);

            if (enemy.getX() < 0)
                enemy.setX(0);

            if (enemy.getY() < 0)
                enemy.setY(0);

            if (enemy.getX() > scwidth - SPRITE_WIDTH)
                enemy.setX(scwidth - SPRITE_WIDTH);

            if (enemy.getY() > scheight - SPRITE_HEIGHT)
                enemy.setY(scheight - SPRITE_HEIGHT);

            repaint();

            try {
                Thread.sleep(16);
            } catch (InterruptedException ex) {
                running = false;
            }
        }
    }

    @Override
    public void paint(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        screen = createImage(scwidth, scheight);

        Graphics2D offscreen =
                (Graphics2D) screen.getGraphics();

        offscreen.setColor(Color.WHITE);
        offscreen.fillRect(0, 0, scwidth, scheight);

        offscreen.drawImage(
                player.getImage(),
                Math.round(player.getX()),
                Math.round(player.getY()),
                SPRITE_WIDTH,
                SPRITE_HEIGHT,
                this);

        offscreen.drawImage(
                enemy.getImage(),
                Math.round(enemy.getX()),
                Math.round(enemy.getY()),
                SPRITE_WIDTH,
                SPRITE_HEIGHT,
                this);

        offscreen.setColor(Color.RED);
        offscreen.draw(player.getBounds());

        offscreen.setColor(Color.BLUE);
        offscreen.draw(enemy.getBounds());

        if (checkCollision()) {
            offscreen.setColor(Color.RED);
            offscreen.drawString("COLLISION!", 20, 50);
        } else {
            offscreen.setColor(Color.BLACK);
            offscreen.drawString("No Collision", 20, 50);
        }

        g2.drawImage(screen, 0, 0, this);

        offscreen.dispose();
    }

    public void startAnimation() {

        running = true;

        gameThread = new Thread(this);
        gameThread.start();
    }

    @Override
    public void keyPressed(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_A:
                isAPressed = true;
                break;

            case KeyEvent.VK_D:
                isDPressed = true;
                break;

            case KeyEvent.VK_W:
                isWPressed = true;
                break;

            case KeyEvent.VK_S:
                isSPressed = true;
                break;
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {

        switch (e.getKeyCode()) {

            case KeyEvent.VK_A:
                isAPressed = false;
                break;

            case KeyEvent.VK_D:
                isDPressed = false;
                break;

            case KeyEvent.VK_W:
                isWPressed = false;
                break;

            case KeyEvent.VK_S:
                isSPressed = false;
                break;
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    public static void main(String[] args) {

        BasicSprites game =
                new BasicSprites(
                        "REDACTED: Who Controls the Truth?",
                        800,
                        600);

        game.setVisible(true);
        game.startAnimation();
    }
}
