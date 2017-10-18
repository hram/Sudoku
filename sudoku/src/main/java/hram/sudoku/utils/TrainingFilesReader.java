package hram.sudoku.utils;

import java.io.IOException;
import java.io.InputStream;

public class TrainingFilesReader {
    public static int g_cImageSize = 28;
    static int cCount = g_cImageSize * g_cImageSize;
    byte[] trainingArr;
    byte[] labelsArr;
    int size;

    public TrainingFilesReader(InputStream trainImages, InputStream trainLabels) throws IOException {
        trainingArr = IOUtil.toByteArray(trainImages);
        labelsArr = IOUtil.toByteArray(trainLabels);

        size = (trainingArr.length - 16) / cCount;
    }

    public byte[] GetTrainingPatternArrayValues(int iNumImage, boolean bFlipGrayscale) {
        byte[] grayArray = new byte[g_cImageSize * g_cImageSize];

        int fPos = 16 + iNumImage * cCount;  // 16 compensates for file header info

        System.arraycopy(trainingArr, fPos, grayArray, 0, cCount);

        if (bFlipGrayscale) {
            for (int ii = 0; ii < cCount; ++ii) {
                grayArray[ii] = (byte) (255 - (grayArray[ii] & 0xFF));
            }
        }

        return grayArray;
    }

    public byte GetTrainingPatternArrayLabels(int iNumImage) {
        int fPos = 8 + iNumImage;
        return labelsArr[fPos];
    }

    public int GetSize() {
        return size;
    }
}
