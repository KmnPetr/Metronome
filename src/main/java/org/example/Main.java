package org.example;

import javax.sound.sampled.LineUnavailableException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Main {
    static final int BAR_WIDTH = 4;
    static final double DURATION_MS = 200;
    static final double DOWNBEAT_HZ = 440.0;  // A4
    static final double UPBEAT_HZ = 466.16;   // A#4, half tone higher
//    static final ToneEnvelope ENVELOPE = ToneEnvelope.SUSTAINED;
    static final ToneEnvelope ENVELOPE = ToneEnvelope.FADE_OUT;
//    static final ToneEnvelope ENVELOPE = ToneEnvelope.TRUNCATED;
    static final String FILE_PATH = Path.of("src/main/resources/flyaway.txt").toAbsolutePath().toString();
    static final String[] LINES;
    static final Bar[] BARS;

    static {
        try {
            LINES = FileParser.parse();
            List<Bar[]> barGroups = new ToneComposer().compose(LINES);
            List<Bar> bars = new ArrayList<>();
            for (Bar[] group : barGroups) {
                for (Bar bar : group) {
                    bars.add(bar);
                }
            }
            BARS = bars.toArray(new Bar[0]);
            ToneComposer.calculateDurations(BARS);
        } catch (IOException e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    public static void main(String[] args) {
        try {
            test();
        } catch (LineUnavailableException | InterruptedException e) {
            System.err.println("Playback failed: " + e.getMessage());
        }
    }

    private static void test() throws LineUnavailableException, InterruptedException {
        TonePlayer tonePlayer = new TonePlayer();
        Display display = new Display();

        while (true) {
            Chord activeChord = null;
            for (Bar bar : BARS) {
                if (bar.getBeatDirection() != null) {
                    display.show(LINES, bar);
                    activeChord = tonePlayer.playBar(bar, activeChord, DURATION_MS);
                } else if (bar.getChord() != null) {
                    activeChord = bar.getChord();
                }
            }
        }
    }
}
