package hram.sudoku.core;

public class Color {
    public int R;
    public int G;
    public int B;
    public int Value;

    public static Color fromInt(int value) {
        return new Color(value);
    }

    public static Color fromArgb(int r, int g, int b) {
        return new Color(r, g, b);
    }

    private Color(int value) {
        Value = value;
        B = (value >> 0) & 0xFF;
        G = (value >> 8) & 0xFF;
        R = (value >> 16) & 0xFF;
    }

    private Color(int r, int g, int b) {
        Value = 0xff000000 | (r << 16) | (g << 8) | b;
        B = b;
        G = g;
        R = r;
    }
}
