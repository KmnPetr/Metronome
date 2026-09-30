package org.example;

public class Bar {
    private final Chord chord;
    private final BeatDirection beatDirection;
    private final int[] lineNumbers;
    private final int[] charIndices;
    private int duration = 1;

    public Bar(Chord chord, BeatDirection beatDirection, int[] lineNumbers, int[] charIndices) {
        this.chord = chord;
        this.beatDirection = beatDirection;
        this.lineNumbers = lineNumbers;
        this.charIndices = charIndices;
    }

    public Chord getChord() {
        return chord;
    }

    public BeatDirection getBeatDirection() {
        return beatDirection;
    }

    public int[] getLineNumbers() {
        return lineNumbers.clone();
    }

    public int[] getCharIndices() {
        return charIndices.clone();
    }

    public int getDuration() {
        return duration;
    }

    void setDuration(int duration) {
        this.duration = duration;
    }

    public boolean isEmpty() {
        return chord == null && beatDirection == null;
    }
}
