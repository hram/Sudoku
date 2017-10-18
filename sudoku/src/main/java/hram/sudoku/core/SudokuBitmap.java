package hram.sudoku.core;

import java.util.Arrays;

import hram.sudoku.core.common.BitMatrix;
import hram.sudoku.core.common.HybridBinarizer;

/**
 * Чернобелая картинка судоку
 *
 * @author hram
 */
public class SudokuBitmap extends GrayScaleBitmap {
    private int border = 2;
    private int rows = 9;
    private int columns = 9;

    private int cellWidth;
    private int cellHeight;

    private int[] rotateMatrix90;
    private int[] rotateMatrix180;
    private int[] rotateMatrix270;

    public SudokuBitmap(byte[] arr, int width, int height) {
        super(arr, width, height);

        cellWidth = width / columns;
        cellHeight = height / rows;

        CreateRotateMatrixes();
    }

    public byte[] GetCell(int y, int x) {
        int offsetX = x * cellWidth;
        int offsetY = y * cellHeight;

        byte[] arr = new byte[cellWidth * cellHeight];
        Arrays.fill(arr, (byte) 0xFF);

        for (int _y = border; _y < cellHeight - border; _y++) {
            for (int _x = border; _x < cellWidth - border; _x++) {
                arr[_y * cellWidth + _x] = (byte) GetPixel(offsetX + _x, offsetY + _y);
            }
        }

        return arr;
    }

    /**
     * Возвращает ячейку для распознавания (картинка в градациях серого)
     *
     * @param y
     * @param x
     * @param mask
     * @return
     */
    public byte[] GetCell(int y, int x, int[][] mask) {
        int offsetX = x * cellWidth;
        int offsetY = y * cellHeight;

        byte[] arr = new byte[cellWidth * cellHeight];

        // заливаем ячейку белым
        Arrays.fill(arr, (byte) 0xFF);

        for (int _y = border; _y < cellHeight - border; _y++) {
            for (int _x = border; _x < cellWidth - border; _x++) {
                // по маске регионов рисуем цифру
                if (mask[offsetY + _y][offsetX + _x] > 0) {
                    arr[_y * cellWidth + _x] = (byte) GetPixel(offsetX + _x, offsetY + _y);
                }
            }
        }

        return arr;
    }

    public byte[] GetCell(int y, int x, int rotate) {
        switch (rotate) {
            case 0:
                return GetCell(y, x);
            case 90:
                return getRotatedCell(y, x, rotateMatrix90);
            case 180:
                return getRotatedCell(y, x, rotateMatrix180);
            case 270:
                return getRotatedCell(y, x, rotateMatrix270);
        }
        return null;
    }

    private byte[] getRotatedCell(int y, int x, int[] matrix) {
        int offsetX = x * cellWidth;
        int offsetY = y * cellHeight;

        byte[] arr = new byte[cellWidth * cellHeight];
        Arrays.fill(arr, (byte) 0xFF);

        for (int _y = 2; _y < cellHeight - 2; _y++) {
            for (int _x = 2; _x < cellWidth - 2; _x++) {
                arr[matrix[_y * cellWidth + _x]] = (byte) GetPixel(offsetX + _x, offsetY + _y);
            }
        }

        return arr;
    }

    public byte[] GetCell(int y, int x, int rotate, int[][] mask) {
        switch (rotate) {
            case 0:
                return GetCell(y, x, mask);
            case 90:
                return getRotatedCell(y, x, rotateMatrix90, mask);
            case 180:
                return getRotatedCell(y, x, rotateMatrix180, mask);
            case 270:
                return getRotatedCell(y, x, rotateMatrix270, mask);
        }
        return null;
    }

    private byte[] getRotatedCell(int y, int x, int[] matrix, int[][] mask) {
        int offsetX = x * cellWidth;
        int offsetY = y * cellHeight;

        byte[] arr = new byte[cellWidth * cellHeight];
        Arrays.fill(arr, (byte) 0xFF);

        for (int _y = 2; _y < cellHeight - 2; _y++) {
            for (int _x = 2; _x < cellWidth - 2; _x++) {
                // по маске регионов рисуем цифру
                if (mask[offsetY + _y][offsetX + _x] > 0) {
                    arr[matrix[_y * cellWidth + _x]] = (byte) GetPixel(offsetX + _x, offsetY + _y);
                }
            }
        }

        return arr;
    }

    public BitMatrix GetBlackMatrix(int y, int x) {
        return GetBlackMatrix(GetCell(y, x));
    }

    public BitMatrix GetBlackMatrix(byte[] cell) {
        RGBLuminanceSource source = new RGBLuminanceSource(cellWidth, cellHeight, cell);

        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));

        try {
            return bb.getBlackMatrix();
        } catch (NotFoundException e) {
            return null;
        }
    }

    public BitMatrix GetBlackMatrix() {
        RGBLuminanceSource source = new RGBLuminanceSource(width, height, arr);

        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));

        try {
            return bb.getBlackMatrix();
        } catch (NotFoundException e) {
            return null;
        }
    }

    public boolean IsEmpty(BitMatrix matrix) {
        int count = 0;
        for (int _y = border; _y < cellHeight - border; _y++) {
            for (int _x = border; _x < cellWidth - border; _x++) {
                if (matrix.get(_x, _y)) {
                    count++;
                }
            }
        }

        return (count > (cellHeight * cellWidth * 0.05)) ? false : true;
    }

    public boolean IsEmpty(byte[] matrix) {
        int level = (int) (cellHeight * cellWidth * 0.03);
        int count = 0;
        for (byte b : matrix) {
            // если не белый пиксель
            if (b != -1) {
                count++;

                if (count >= level) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean IsEmpty(int y, int x) {
        RGBLuminanceSource source = new RGBLuminanceSource(cellWidth, cellHeight, GetCell(y, x));

        BinaryBitmap bb = new BinaryBitmap(new HybridBinarizer(source));

        BitMatrix matrix = null;

        try {
            matrix = bb.getBlackMatrix();
        } catch (NotFoundException e) {
            return true;
        }

        int count = 0;
        for (int _y = 0; _y < cellHeight; _y++) {
            for (int _x = 0; _x < cellWidth; _x++) {
                if (matrix.get(_x, _y)) {
                    count++;
                }
            }
        }

        return (count > (cellHeight * cellWidth * 0.10)) ? false : true;
    }

    public int GetCellWidth() {
        return cellWidth;
    }

    public int GetCellHeight() {
        return cellHeight;
    }

    private void CreateRotateMatrixes() {
        rotateMatrix90 = new int[cellWidth * cellHeight];
        rotateMatrix180 = new int[cellWidth * cellHeight];
        rotateMatrix270 = new int[cellWidth * cellHeight];

        int index = 0;
        for (int y = 0; y < cellHeight; y++) {
            for (int x = 0; x < cellWidth; x++, index++) {
                rotateMatrix90[index] = x * cellHeight + cellHeight - y - 1;
                rotateMatrix180[index] = (cellWidth - y - 1) * cellHeight + cellWidth - x - 1;
                rotateMatrix270[index] = (cellWidth - x - 1) * cellWidth + y;
            }
        }
    }
}
