package org.example;

public enum Chord {
    // Am open: x A E A C E
    Am(110.00, 164.81, 220.00, 261.63, 329.63),
    // F barre: F C F A C F
    F(87.31, 130.81, 174.61, 220.00, 261.63, 349.23),
    // C open: x C E G C E
    C(130.81, 164.81, 196.00, 261.63, 329.63),
    // Em open: E B E G B E
    Em(82.41, 123.47, 164.81, 196.00, 246.94, 329.63),
    // E open: E B E G# B E
    E(82.41, 123.47, 164.81, 207.65, 246.94, 329.63);

    private final double[] frequencies;

    Chord(double... frequencies) {
        this.frequencies = frequencies;
    }

    public double[] getFrequencies() {
        return frequencies.clone();
    }
}
