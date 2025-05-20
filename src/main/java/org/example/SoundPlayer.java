package org.example;

import javax.sound.sampled.*;
import java.net.URL;

public class SoundPlayer {
    private static Clip loopingClip;

    public static void playLoop(String fileName) {
        try {
            URL soundURL = SoundPlayer.class.getClassLoader().getResource("sounds/" + fileName);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + fileName);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            loopingClip = AudioSystem.getClip();
            loopingClip.open(audioIn);

            // Loop forever
            loopingClip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void stopLoop() {
        if (loopingClip != null && loopingClip.isRunning()) {
            loopingClip.stop();
            loopingClip.close();
        }
    }

    public static void playOnce(String fileName) {
        try {
            URL soundURL = SoundPlayer.class.getClassLoader().getResource("sounds/" + fileName);
            if (soundURL == null) {
                System.err.println("Sound file not found: " + fileName);
                return;
            }

            AudioInputStream audioIn = AudioSystem.getAudioInputStream(soundURL);
            Clip clip = AudioSystem.getClip();
            clip.open(audioIn);
            clip.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

