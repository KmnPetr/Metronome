package org.example;

public enum Chord {
    Am(440.00, 523.25, 659.25),
    F(349.23, 440.00, 523.25),
    C(261.63, 329.63, 392.00),
    Em(329.63, 392.00, 493.88),
    E(329.63, 415.30, 493.88);

    private final double[] frequencies;

    Chord(double... frequencies) {
        this.frequencies = frequencies;
    }

    public double[] getFrequencies() {
        return frequencies.clone();
    }
}
