package animationlab;

import java.awt.Image;
import java.util.ArrayList;

public class Animation {

    private ArrayList<AnimFrame> frames;
    private int currFrameIndex;
    private long animTime;
    private long totalDuration;
    private boolean playing;

    public Animation() {
        frames = new ArrayList<>();
        totalDuration = 0;
        currFrameIndex = 0;
        animTime = 0;
        playing = false;
    }

    public synchronized void addFrame(Image image, long duration) {
        totalDuration += duration;
        frames.add(new AnimFrame(image, totalDuration));
    }

    public synchronized void start() {
        if (playing) return;

        currFrameIndex = 0;
        animTime = 0;
        playing = true;
    }

    public synchronized void stop() {
        playing = false;
        currFrameIndex = 0;
        animTime = 0;
    }

    public synchronized void update(long elapsedTime) {

        if (!playing || frames.size() <= 1)
            return;

        animTime += elapsedTime;

        if (animTime >= totalDuration) {
            animTime %= totalDuration;
            currFrameIndex = 0;
        }

        while (animTime > getFrame(currFrameIndex).endTime) {
            currFrameIndex++;
        }
    }

    public synchronized Image getImage() {

        if (frames.size() == 0)
            return null;

        return getFrame(currFrameIndex).image;
    }

    private AnimFrame getFrame(int i) {
        return frames.get(i);
    }

    private class AnimFrame {

        Image image;
        long endTime;

        public AnimFrame(Image image, long endTime) {
            this.image = image;
            this.endTime = endTime;
        }
    }
}
