package org.example;

import java.lang.management.ManagementFactory;
import java.util.HashSet;
import java.util.Set;

public class Display {

    private static final String CLEAR_SCREEN = "\033[2J\033[H";
    private static final String HIGHLIGHT_BACKGROUND = "\033[48;5;255m";
    private static final String RESET = "\033[0m";
    private static final int HEADER_ROWS = 2;

    private final boolean ideConsole = detectIdeConsole();

    private boolean initialized;
    private String[] renderedLines = new String[0];

    public void show(String[] lines, Bar currentBar) {
        if (ideConsole) {
            printAll(buildRenderedLines(lines, currentBar));
            initialized = true;
            return;
        }

        showInTerminal(lines, currentBar);
    }

    private void showInTerminal(String[] lines, Bar currentBar) {
        String[] nextRendered = buildRenderedLines(lines, currentBar);

        if (!initialized) {
            renderedLines = nextRendered;
            printAll(nextRendered);
            initialized = true;
            return;
        }

        Set<Integer> changedLineNumbers = new HashSet<>();
        for (int lineNumber = 1; lineNumber <= lines.length; lineNumber++) {
            if (!objectsEqual(renderedLines[lineNumber - 1], nextRendered[lineNumber - 1])) {
                changedLineNumbers.add(lineNumber);
            }
        }

        if (changedLineNumbers.isEmpty()) {
            return;
        }

        for (int lineNumber : changedLineNumbers) {
            rewriteLine(nextRendered[lineNumber - 1], lineNumber);
        }

        renderedLines = nextRendered;
        System.out.flush();
    }

    private void printAll(String[] rendered) {
        System.out.print(CLEAR_SCREEN);
        System.out.println(Main.FILE_PATH);
        System.out.println();

        for (String line : rendered) {
            System.out.println(line);
        }

        System.out.flush();
    }

    private void rewriteLine(String renderedLine, int lineNumber) {
        int row = lineNumber + HEADER_ROWS;
        System.out.printf("\033[%d;1H\033[2K%s\033[0m", row, renderedLine);
    }

    private String[] buildRenderedLines(String[] lines, Bar currentBar) {
        String[] rendered = new String[lines.length];

        for (int lineIndex = 0; lineIndex < lines.length; lineIndex++) {
            rendered[lineIndex] = formatLine(lines[lineIndex], lineIndex + 1, currentBar);
        }

        return rendered;
    }

    private boolean detectIdeConsole() {
        if (System.getenv("IDEA_INITIAL_DIRECTORY") != null) {
            return true;
        }

        for (String argument : ManagementFactory.getRuntimeMXBean().getInputArguments()) {
            if (argument.contains("idea_rt.jar") || argument.toLowerCase().contains("android-studio")) {
                return true;
            }
        }

        return false;
    }

    private boolean objectsEqual(String left, String right) {
        return left == null ? right == null : left.equals(right);
    }

    private String formatLine(String line, int lineNumber, Bar currentBar) {
        if (currentBar == null || line.isEmpty() || currentBar.getBeatDirection() == null) {
            return line;
        }

        if (lineNumber != getBeatLineNumber(currentBar)) {
            return line;
        }

        int beatCharIndex = getBeatCharIndex(currentBar);
        if (beatCharIndex < 0) {
            return line;
        }

        boolean[] highlighted = new boolean[line.length()];
        int slotStart = getSlotStart(line, beatCharIndex);
        int slotEnd = Math.min(slotStart + Main.BAR_WIDTH, line.length());

        for (int charIndex = slotStart; charIndex < slotEnd; charIndex++) {
            highlighted[charIndex] = true;
        }

        StringBuilder formatted = new StringBuilder();
        boolean highlightActive = false;

        for (int charIndex = 0; charIndex < line.length(); charIndex++) {
            if (highlighted[charIndex] != highlightActive) {
                formatted.append(highlighted[charIndex] ? HIGHLIGHT_BACKGROUND : RESET);
                highlightActive = highlighted[charIndex];
            }
            formatted.append(line.charAt(charIndex));
        }

        if (highlightActive) {
            formatted.append(RESET);
        }

        return formatted.toString();
    }

    private int getBeatLineNumber(Bar bar) {
        int[] lineNumbers = bar.getLineNumbers();
        if (lineNumbers.length == 0) {
            return -1;
        }
        if (bar.getChord() != null && lineNumbers.length > 1) {
            return lineNumbers[1];
        }
        return lineNumbers[0];
    }

    private int getBeatCharIndex(Bar bar) {
        int beatLineNumber = getBeatLineNumber(bar);
        int[] lineNumbers = bar.getLineNumbers();
        int[] charIndices = bar.getCharIndices();

        for (int i = 0; i < lineNumbers.length; i++) {
            if (lineNumbers[i] == beatLineNumber) {
                return charIndices[i];
            }
        }

        return -1;
    }

    private int getSlotStart(String line, int charIndex) {
        int contentStart = ToneComposer.getContentStart(line);
        int contentEnd = ToneComposer.getContentEnd(line);

        if (contentStart < 0 || contentEnd <= contentStart) {
            return charIndex;
        }

        if (charIndex <= contentStart) {
            return contentStart;
        }

        if (charIndex >= contentEnd) {
            return Math.max(contentStart, contentEnd - Main.BAR_WIDTH);
        }

        int offset = charIndex - contentStart;
        return contentStart + (offset / Main.BAR_WIDTH) * Main.BAR_WIDTH;
    }
}
