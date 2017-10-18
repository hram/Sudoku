package hram.sudoku.core;

import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;

import hram.sudoku.utils.IProgressPublisher;

public class Utils {
    public static int[] GraySkaleByteToInt(byte[] arr, int width, int height) {
        int inputOffset = 0;
        int[] pixels = new int[width * height];
        for (int y = 0; y < height; y++) {
            int outputOffset = y * width;
            for (int x = 0; x < width; x++) {
                byte grey = arr[inputOffset + x];
                pixels[outputOffset + x] = 0xFF000000 | (grey * 0x00010101);
            }
            inputOffset += width;
        }

        return pixels;
    }

    public static GrayScaleBitmap RescaleImage(LuminanceSource bm, Point TL, Point TR, Point LL, Point LR, int sx, int sy) {
        return RescaleImage(bm, TL, TR, LL, LR, sx, sy, null);
    }

    public static GrayScaleBitmap RescaleImage(LuminanceSource bm, Point TL, Point TR, Point LL, Point LR, int sx, int sy, IProgressPublisher tasker) {
        byte[] array = bm.getMatrix();
        int width = bm.getWidth();
        int height = bm.getHeight();

        GrayScaleBitmap bmpOut = new GrayScaleBitmap(sx, sy);

        int fullProgress = sx * sy;

        int lastProgress = 0;
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                /*
                 * relative position
                 */
                double rx = (double) x / sx;
                double ry = (double) y / sy;

                /*
                 * get top and bottom position
                 */
                double topX = TL.x + rx * (TR.x - TL.x);
                double topY = TL.y + rx * (TR.y - TL.y);
                double bottomX = LL.x + rx * (LR.x - LL.x);
                double bottomY = LL.y + rx * (LR.y - LL.y);

                /*
                 * select center between top and bottom point
                 */
                double centerX = topX + ry * (bottomX - topX);
                double centerY = topY + ry * (bottomY - topY);

                /*
                 * store result
                 */
                int c = PolyColor(array, width, height, centerX, centerY);
                bmpOut.SetPixel(x, y, (byte) (c & 0xFF));
            }

            if (tasker != null) {
                int currentProgress = sy * (x + 1) * 100 / fullProgress;
                if ((currentProgress - lastProgress) >= 10) {
                    tasker.PublishProgress(currentProgress);
                    lastProgress = currentProgress;
                }
            }
        }

        return bmpOut;
    }

    /**
     * Кадрирует чб изображение
     *
     * @param bm картинка
     * @param TL верхняя левая точка
     * @param TR верхняя правая точка
     * @param LL нижняя левая точка
     * @param LR нижняя правая точка
     * @param sx ширина результирующей картинки
     * @param sy высока  результирующей картинки
     * @return кадрированная картинка
     */
    public static GrayScaleBitmap RescaleImage(GrayScaleBitmap bm, Point TL, Point TR, Point LL, Point LR, int sx, int sy) {
        return RescaleImage(bm, TL, TR, LL, LR, sx, sy, null);
    }

    public static GrayScaleBitmap RescaleImage(GrayScaleBitmap bm, Point TL, Point TR, Point LL, Point LR, int sx, int sy, IProgressPublisher tasker) {
        GrayScaleBitmap bmpOut = new GrayScaleBitmap(sx, sy);

        //int fullProgress = sx * sy;

        int lastProgress = 0;
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                /*
                 * relative position
                 */
                double rx = (double) x / sx;
                double ry = (double) y / sy;

                /*
                 * get top and bottom position
                 */
                double topX = TL.x + rx * (TR.x - TL.x);
                double topY = TL.y + rx * (TR.y - TL.y);
                double bottomX = LL.x + rx * (LR.x - LL.x);
                double bottomY = LL.y + rx * (LR.y - LL.y);

                /*
                 * select center between top and bottom point
                 */
                double centerX = topX + ry * (bottomX - topX);
                double centerY = topY + ry * (bottomY - topY);

                /*
                 * store result
                 */
                int c = PolyColor(bm, centerX, centerY);
                bmpOut.SetPixel(x, y, (byte) (c & 0xFF));
            }

            if (tasker != null) {
                int currentProgress = x * 100 / sx;
                if ((currentProgress - lastProgress) >= 10) {
                    tasker.PublishProgress(currentProgress);
                    lastProgress = currentProgress;
                }
            }
        }

        return bmpOut;
    }

    private static int PolyColor(GrayScaleBitmap bm, double x, double y) {
        // get fractions
        double xf = x - (long) x;
        double yf = y - (long) y;

        // 4 colors - we're flipping sides so we can use the distance instead of inverting it later
        int cTL = bm.GetPixel((int) x + 1, (int) y + 1);
        int cTR = bm.GetPixel((int) x + 0, (int) y + 1);
        int cLL = bm.GetPixel((int) x + 1, (int) y + 0);
        int cLR = bm.GetPixel((int) x + 0, (int) y + 0);

        // 4 distances
        double dTL = Math.sqrt(xf * xf + yf * yf);
        double dTR = Math.sqrt((1 - xf) * (1 - xf) + yf * yf);
        double dLL = Math.sqrt(xf * xf + (1 - yf) * (1 - yf));
        double dLR = Math.sqrt((1 - xf) * (1 - xf) + (1 - yf) * (1 - yf));

        // 4 parts
        double factor = 1.0 / (dTL + dTR + dLL + dLR);
        dTL *= factor;
        dTR *= factor;
        dLL *= factor;
        dLR *= factor;

        // accumulate parts
        double r = dTL * cTL + dTR * cTR + dLL * cLL + dLR * cLR;

        return (int) (r + 0.5);
    }

    private static int PolyColor(byte[] bm, int width, int height, double x, double y) {
        // get fractions
        double xf = x - (long) x;
        double yf = y - (long) y;

        // 4 colors - we're flipping sides so we can use the distance instead of inverting it later
        int cTL = bm[((int) y + 1) * width + (int) x + 1] & 0xFF;
        int cTR = bm[((int) y + 1) * width + (int) x + 0] & 0xFF;
        int cLL = bm[((int) y + 0) * width + (int) x + 1] & 0xFF;
        int cLR = bm[((int) y + 0) * width + (int) x + 0] & 0xFF;

        // 4 distances
        double dTL = Math.sqrt(xf * xf + yf * yf);
        double dTR = Math.sqrt((1 - xf) * (1 - xf) + yf * yf);
        double dLL = Math.sqrt(xf * xf + (1 - yf) * (1 - yf));
        double dLR = Math.sqrt((1 - xf) * (1 - xf) + (1 - yf) * (1 - yf));

        // 4 parts
        double factor = 1.0 / (dTL + dTR + dLL + dLR);
        dTL *= factor;
        dTR *= factor;
        dLL *= factor;
        dLR *= factor;

        // accumulate parts
        double r = dTL * cTL + dTR * cTR + dLL * cLL + dLR * cLR;

        return (int) (r + 0.5);
    }

    /**
     * Кадрирует цветное изображение
     *
     * @param bm картинка
     * @param TL верхняя левая точка
     * @param TR верхняя правая точка
     * @param LL нижняя левая точка
     * @param LR нижняя правая точка
     * @param sx ширина результирующей картинки
     * @param sy высока  результирующей картинки
     * @return кадрированная картинка
     */
    public static Bitmap RescaleImage(Bitmap bm, Point TL, Point TR, Point LL, Point LR, int sx, int sy) {
        return RescaleImage(bm, TL, TR, LL, LR, sx, sy, null);
    }

    public static Bitmap RescaleImage(Bitmap bm, Point TL, Point TR, Point LL, Point LR, int sx, int sy, IProgressPublisher tasker) {
        Bitmap bmpOut = Bitmap.createBitmap(sx, sy, Bitmap.Config.ARGB_8888);

        int lastProgress = 0;
        for (int x = 0; x < sx; x++) {
            for (int y = 0; y < sy; y++) {
                /*
                 * relative position
                 */
                double rx = (double) x / sx;
                double ry = (double) y / sy;

                /*
                 * get top and bottom position
                 */
                double topX = TL.x + rx * (TR.x - TL.x);
                double topY = TL.y + rx * (TR.y - TL.y);
                double bottomX = LL.x + rx * (LR.x - LL.x);
                double bottomY = LL.y + rx * (LR.y - LL.y);

                /*
                 * select center between top and bottom point
                 */
                double centerX = topX + ry * (bottomX - topX);
                double centerY = topY + ry * (bottomY - topY);

                /*
                 * store result
                 */
                Color c = PolyColor(bm, centerX, centerY);
                bmpOut.setPixel(x, y, c.Value);
            }

            if (tasker != null) {
                int currentProgress = x * 100 / sx;
                if ((currentProgress - lastProgress) >= 10) {
                    tasker.PublishProgress(currentProgress);
                    lastProgress = currentProgress;
                }
            }
        }

        return bmpOut;
    }

    private static Color PolyColor(Bitmap bm, double x, double y) {
        // get fractions
        double xf = x - (int) x;
        double yf = y - (int) y;

        // 4 colors - we're flipping sides so we can use the distance instead of inverting it later
        Color cTL = Color.fromInt(bm.getPixel((int) x + 1, (int) y + 1));
        Color cTR = Color.fromInt(bm.getPixel((int) x + 0, (int) y + 1));
        Color cLL = Color.fromInt(bm.getPixel((int) x + 1, (int) y + 0));
        Color cLR = Color.fromInt(bm.getPixel((int) x + 0, (int) y + 0));

        // 4 distances
        double dTL = Math.sqrt(xf * xf + yf * yf);
        double dTR = Math.sqrt((1 - xf) * (1 - xf) + yf * yf);
        double dLL = Math.sqrt(xf * xf + (1 - yf) * (1 - yf));
        double dLR = Math.sqrt((1 - xf) * (1 - xf) + (1 - yf) * (1 - yf));

        // 4 parts
        double factor = 1.0 / (dTL + dTR + dLL + dLR);
        dTL *= factor;
        dTR *= factor;
        dLL *= factor;
        dLR *= factor;

        // accumulate parts
        double r = dTL * cTL.R + dTR * cTR.R + dLL * cLL.R + dLR * cLR.R;
        double g = dTL * cTL.G + dTR * cTR.G + dLL * cLL.G + dLR * cLR.G;
        double b = dTL * cTL.B + dTR * cTR.B + dLL * cLL.B + dLR * cLR.B;

        Color c = Color.fromArgb((int) (r + 0.5), (int) (g + 0.5), (int) (b + 0.5));

        return c;
    }

    // http://ketai.googlecode.com/svn/trunk/ketai/src/edu/uic/ketai/inputService/KetaiCamera.java
    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height) {
        // here we're using our own internal PImage attributes
        final int frameSize = width * height;

        int[] rgb = new int[frameSize];

        for (int j = 0, yp = 0; j < height; j++) {
            int uvp = frameSize + (j >> 1) * width, u = 0, v = 0;
            for (int i = 0; i < width; i++, yp++) {
                int y = (0xff & ((int) yuv420sp[yp])) - 16;
                if (y < 0)
                    y = 0;
                if ((i & 1) == 0) {
                    v = (0xff & yuv420sp[uvp++]) - 128;
                    u = (0xff & yuv420sp[uvp++]) - 128;
                }

                int y1192 = 1192 * y;
                int r = (y1192 + 1634 * v);
                int g = (y1192 - 833 * v - 400 * u);
                int b = (y1192 + 2066 * u);

                if (r < 0)
                    r = 0;
                else if (r > 262143)
                    r = 262143;
                if (g < 0)
                    g = 0;
                else if (g > 262143)
                    g = 262143;
                if (b < 0)
                    b = 0;
                else if (b > 262143)
                    b = 262143;

                // use interal buffer instead of pixels for UX reasons
                rgb[yp] = 0xff000000 | ((r << 6) & 0xff0000) | ((g >> 2) & 0xff00) | ((b >> 10) & 0xff);
            }
        }

        return rgb;
    }

    public static int[] decodeYUV420SP(byte[] yuv420sp, int width, int height, Rect rect) {
        // here we're using our own internal PImage attributes
        final int frameSize = width * height;

        int[] argb = new int[rect.width() * rect.height()];

        final int iy = rect.top;
        final int ix = rect.left;
        final int di = +1;
        final int dj = +1;

        int a = 0;
        for (int i = 0, cy = iy; i < rect.height(); ++i, cy += di) {
            for (int j = 0, cx = ix; j < rect.width(); ++j, cx += dj) {
                int y = (0xff & ((int) yuv420sp[cy * width + cx]));
                int v = (0xff & ((int) yuv420sp[frameSize + (cy >> 1) * width + (cx & ~1) + 0]));
                int u = (0xff & ((int) yuv420sp[frameSize + (cy >> 1) * width + (cx & ~1) + 1]));
                y = y < 16 ? 16 : y;

                int r = (int) (1.164f * (y - 16) + 1.596f * (v - 128));
                int g = (int) (1.164f * (y - 16) - 0.813f * (v - 128) - 0.391f * (u - 128));
                int b = (int) (1.164f * (y - 16) + 2.018f * (u - 128));

                r = r < 0 ? 0 : (r > 255 ? 255 : r);
                g = g < 0 ? 0 : (g > 255 ? 255 : g);
                b = b < 0 ? 0 : (b > 255 ? 255 : b);

                argb[a++] = 0xff000000 | (r << 16) | (g << 8) | b;
            }
        }

        return argb;
    }
}
