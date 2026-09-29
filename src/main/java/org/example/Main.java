package org.example;

import javax.sound.sampled.LineUnavailableException;

public class Main {
    static final double BEAT_DURATION_MS = 100;
    static final double CHORD_DURATION_MS = 300;
    static final double PAUSE_DURATION_MS = 400;
    static final double DOWNBEAT_HZ = 440.0;  // A4
    static final double UPBEAT_HZ = 466.16;   // A#4, half tone higher
//    static final ToneEnvelope ENVELOPE = ToneEnvelope.SUSTAINED;
    static final ToneEnvelope ENVELOPE = ToneEnvelope.FADE_OUT;
//    static final ToneEnvelope ENVELOPE = ToneEnvelope.TRUNCATED;

    public static void main(String[] args) {
        try {
            test();
        } catch (LineUnavailableException | InterruptedException e) {
            System.err.println("Playback failed: " + e.getMessage());
        }
    }

    private static void test() throws LineUnavailableException, InterruptedException {
        TonePlayer tonePlayer = new TonePlayer();

        while (true) {
            tonePlayer.playDownbeat();
            tonePlayer.playUpbeat();
            tonePlayer.pause();
        }
    }
}
