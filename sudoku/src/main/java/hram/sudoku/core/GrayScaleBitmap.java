package hram.sudoku.core;

public class GrayScaleBitmap {
    protected byte[] arr;
    protected int width;
    protected int height;

    public GrayScaleBitmap(int width, int height) {
        this.arr = new byte[width * height];
        this.width = width;
        this.height = height;
    }

    public GrayScaleBitmap(byte[] arr, int width, int height) {
        this.arr = arr;
        this.width = width;
        this.height = height;
    }

    public int GetPixel(int x, int y) {
        return arr[y * width + x] & 0xFF;
    }

    public void SetPixel(int x, int y, byte pixel) {
        arr[y * width + x] = pixel;
    }

    public byte[] GetBytes() {
        return arr;
    }

    public int GetWidth() {
        return width;
    }

    public int GetHeight() {
        return height;
    }

    public byte[] GetRotate(int angle) {
        int index = 0;
        byte[] rotate = new byte[width * height];
        switch (angle) {
            case 90:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++, index++) {
                        rotate[index] = arr[(width - x - 1) * width + y];
                    }
                }
                return rotate;
            case 180:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++, index++) {
                        rotate[index] = arr[(width - y - 1) * height + width - x - 1];
                    }
                }
                return rotate;
            case 270:
                for (int y = 0; y < height; y++) {
                    for (int x = 0; x < width; x++, index++) {
                        rotate[index] = arr[x * height + height - y - 1];
                    }
                }
                return rotate;
        }
        return null;
    }
}
