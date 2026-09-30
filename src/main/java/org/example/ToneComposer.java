package org.example;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ToneComposer {

    private static final Chord[] CHORDS_BY_LENGTH = Chord.values();

    static {
        java.util.Arrays.sort(CHORDS_BY_LENGTH, Comparator.comparingInt((Chord c) -> c.name().length()).reversed());
    }

    public List<Bar[]> compose(String[] lines) {
        List<Bar[]> result = new ArrayList<>();

        for (int i = 0; i < lines.length; i++) {
            if (!containsParentheses(lines[i])) {
                continue;
            }
            if (i + 1 >= lines.length || !containsParentheses(lines[i + 1])) {
                continue;
            }

            result.add(composePair(lines[i], i + 1, lines[i + 1], i + 2));
            i++;
        }

        return result;
    }

    public static void calculateDurations(Bar[] bars) {
        if (bars.length == 0) {
            return;
        }

        for (int i = 0; i < bars.length; i++) {
            if (bars[i].isEmpty()) {
                continue;
            }

            int duration = 1;
            int index = (i + 1) % bars.length;

            while (bars[index].isEmpty() && index != i) {
                duration++;
                index = (index + 1) % bars.length;
            }

            bars[i].setDuration(duration);
        }
    }

    private Bar[] composePair(String chordLine, int chordLineNumber, String beatLine, int beatLineNumber) {
        int contentStart = getContentStart(chordLine);
        int contentEnd = getContentEnd(chordLine);

        if (contentStart < 0 || contentEnd <= contentStart) {
            return new Bar[0];
        }

        List<Bar> bars = new ArrayList<>();

        for (int barStart = contentStart; barStart + Main.BAR_WIDTH <= contentEnd; barStart += Main.BAR_WIDTH) {
            int barEnd = barStart + Main.BAR_WIDTH;

            ChordToken chordToken = findChordInRange(chordLine, barStart, barEnd);
            BeatToken beatToken = findBeatInRange(beatLine, barStart, barEnd);

            List<Integer> lineNumbers = new ArrayList<>();
            List<Integer> charIndices = new ArrayList<>();

            if (chordToken != null) {
                lineNumbers.add(chordLineNumber);
                charIndices.add(chordToken.charIndex);
            }
            if (beatToken != null) {
                lineNumbers.add(beatLineNumber);
                charIndices.add(beatToken.charIndex);
            }

            bars.add(new Bar(
                    chordToken != null ? chordToken.chord : null,
                    beatToken != null ? beatToken.beatDirection : null,
                    toIntArray(lineNumbers),
                    toIntArray(charIndices)
            ));
        }

        return bars.toArray(new Bar[0]);
    }

    private ChordToken findChordInRange(String line, int barStart, int barEnd) {
        for (Chord chord : CHORDS_BY_LENGTH) {
            String name = chord.name();
            int index = barStart;

            while (index < barEnd && (index = line.indexOf(name, index)) >= 0 && index < barEnd) {
                if (isInsideParentheses(line, index) && index + name.length() <= barEnd) {
                    return new ChordToken(index, chord);
                }
                index += 1;
            }
        }

        return null;
    }

    private BeatToken findBeatInRange(String line, int barStart, int barEnd) {
        for (int index = barStart; index < barEnd; index++) {
            if (!isInsideParentheses(line, index)) {
                continue;
            }

            BeatDirection direction = switch (line.charAt(index)) {
                case '\u2193' -> BeatDirection.DOWN;
                case '\u2191' -> BeatDirection.UP;
                default -> null;
            };

            if (direction != null) {
                return new BeatToken(index, direction);
            }
        }

        return null;
    }

    private boolean containsParentheses(String line) {
        return line.indexOf('(') >= 0 && line.indexOf(')') >= 0;
    }

    private boolean isInsideParentheses(String line, int index) {
        int contentStart = getContentStart(line);
        int contentEnd = getContentEnd(line);

        if (contentStart < 0 || contentEnd <= contentStart) {
            return false;
        }

        return index >= contentStart && index < contentEnd;
    }

    static int getContentStart(String line) {
        int open = line.indexOf('(');
        return open < 0 ? -1 : open + 1;
    }

    static int getContentEnd(String line) {
        int close = line.lastIndexOf(')');
        return close < 0 ? -1 : close - 1;
    }

    private int[] toIntArray(List<Integer> values) {
        return values.stream().mapToInt(Integer::intValue).toArray();
    }

    private static class ChordToken {
        private final int charIndex;
        private final Chord chord;

        private ChordToken(int charIndex, Chord chord) {
            this.charIndex = charIndex;
            this.chord = chord;
        }
    }

    private static class BeatToken {
        private final int charIndex;
        private final BeatDirection beatDirection;

        private BeatToken(int charIndex, BeatDirection beatDirection) {
            this.charIndex = charIndex;
            this.beatDirection = beatDirection;
        }
    }
}
