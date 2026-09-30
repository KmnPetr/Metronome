package org.example;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class TonePlayer {

    private static final int SAMPLE_RATE = 44100;
    private static final int SAMPLE_SIZE_BITS = 16;
    private static final int CHANNELS = 1;
    private static final double TRUNCATED_TAIL_RATIO = 0.1;

    /**
     * Асинхронно воспроизводит синусоидальный тон заданной частоты.
     * Длительность и огибающая берутся из {@link Main}.
     */
    public void playTone(double frequencyHz) {
        validateFrequency(frequencyHz);

        Thread.ofPlatform().start(() -> {
            try {
                playToneBlocking(frequencyHz, Main.DURATION_MS, Main.ENVELOPE);
            } catch (LineUnavailableException e) {
                System.err.println("Failed to play tone: " + e.getMessage());
            }
        });
    }

    /**
     * Синхронно воспроизводит аккорд. Огибающая берётся из {@link Main}.
     */
    public void playChord(Chord chord, double durationMs) throws LineUnavailableException {
        if (chord == null) {
            throw new IllegalArgumentException("Chord must not be null");
        }
        validatePlaybackParams(durationMs, Main.ENVELOPE);

        byte[] buffer = generateChordWave(chord.getFrequencies(), durationMs, Main.ENVELOPE);
        playBuffer(buffer);
    }

    /**
     * Синхронно воспроизводит удар вниз. Параметры берутся из {@link Main}.
     */
    public void playDownbeat() throws LineUnavailableException {
        playToneBlocking(Main.DOWNBEAT_HZ, Main.DURATION_MS, Main.ENVELOPE);
    }

    /**
     * Синхронно воспроизводит удар вверх. Параметры берутся из {@link Main}.
     */
    public void playUpbeat() throws LineUnavailableException {
        playToneBlocking(Main.UPBEAT_HZ, Main.DURATION_MS, Main.ENVELOPE);
    }

    /**
     * Воспроизводит один такт. Возвращает текущий активный аккорд.
     */
    public Chord playBar(Bar bar, Chord activeChord, double unitDurationMs)
            throws LineUnavailableException, InterruptedException {
        if (bar.getChord() != null) {
            activeChord = bar.getChord();
        }

        if (bar.getBeatDirection() != null) {
            if (activeChord != null) {
                playChord(activeChord, unitDurationMs * bar.getDuration());
            }
        }

        return activeChord;
    }

    /**
     * Синхронная пауза.
     */
    public void pause(double durationMs) throws InterruptedException {
        if (durationMs < 0) {
            throw new IllegalArgumentException("Duration must not be negative");
        }
        Thread.sleep((long) durationMs);
    }

    /**
     * Синхронная пауза. Длительность берётся из {@link Main}.
     */
    public void pause() throws InterruptedException {
        pause(Main.DURATION_MS);
    }

    private void playToneBlocking(double frequencyHz, double durationMs, ToneEnvelope envelope)
            throws LineUnavailableException {
        validateFrequency(frequencyHz);
        validatePlaybackParams(durationMs, envelope);

        byte[] buffer = generateSineWave(frequencyHz, durationMs, envelope);
        playBuffer(buffer);
    }

    private void playBuffer(byte[] buffer) throws LineUnavailableException {
        AudioFormat format = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE_BITS, CHANNELS, true, false);
        DataLine.Info info = new DataLine.Info(SourceDataLine.class, format);

        SourceDataLine line = (SourceDataLine) AudioSystem.getLine(info);
        try {
            line.open(format);
            line.start();
            line.write(buffer, 0, buffer.length);
            line.drain();
        } finally {
            line.close();
        }
    }

    private byte[] generateSineWave(double frequencyHz, double durationMs, ToneEnvelope envelope) {
        return generateChordWave(new double[]{frequencyHz}, durationMs, envelope);
    }

    private byte[] generateChordWave(double[] frequencies, double durationMs, ToneEnvelope envelope) {
        int sampleCount = (int) (SAMPLE_RATE * durationMs / 1000.0);
        byte[] buffer = new byte[sampleCount * 2];

        for (int i = 0; i < sampleCount; i++) {
            double amplitude = calculateAmplitude(i, sampleCount, envelope);
            double mixedSignal = 0;

            for (double frequencyHz : frequencies) {
                double angle = 2.0 * Math.PI * i * frequencyHz / SAMPLE_RATE;
                mixedSignal += Math.sin(angle);
            }

            mixedSignal /= frequencies.length;
            short sample = (short) (Short.MAX_VALUE * amplitude * mixedSignal);
            buffer[i * 2] = (byte) sample;
            buffer[i * 2 + 1] = (byte) (sample >> 8);
        }

        return buffer;
    }

    private double calculateAmplitude(int sampleIndex, int sampleCount, ToneEnvelope envelope) {
        int truncatedCutoff = (int) (sampleCount * (1.0 - TRUNCATED_TAIL_RATIO));

        return switch (envelope) {
            case FADE_OUT -> 1.0 - (double) sampleIndex / sampleCount;
            case TRUNCATED -> sampleIndex < truncatedCutoff ? 1.0 : 0.0;
            case SUSTAINED -> 1.0;
        };
    }

    private void validateFrequency(double frequencyHz) {
        if (frequencyHz <= 0) {
            throw new IllegalArgumentException("Frequency must be positive");
        }
    }

    private void validatePlaybackParams(double durationMs, ToneEnvelope envelope) {
        if (durationMs <= 0) {
            throw new IllegalArgumentException("Duration must be positive");
        }
        if (envelope == null) {
            throw new IllegalArgumentException("Envelope must not be null");
        }
    }
}
