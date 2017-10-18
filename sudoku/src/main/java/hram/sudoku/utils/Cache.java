package hram.sudoku.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class Cache {

    private static File CreateDir(Context context) {
        return CreateDir(context, null);
    }

    private static File CreateDir(Context context, String subDir) {
        File cacheDir;
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), (subDir == null) ? "Sudoku" : "Sudoku" + "/" + subDir);
        else
            cacheDir = context.getCacheDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();

        return cacheDir;
    }

    public static File CteateFile(Context context, String filename) {
        File dir = CreateDir(context);
        File file = new File(dir, filename);
        file.getParentFile().mkdirs();
        return file;
    }

    public static File GetFile(Context context, String filename) {
        return new File(CreateDir(context), filename);
    }

    public static File CteateFile(Context context, String subDir, String filename) {
        return new File(CreateDir(context, subDir), filename);
    }

    public static Bitmap DecodeFile(Context context, String filename) {
        try {
            //decode image size
            return BitmapFactory.decodeStream(new FileInputStream(GetFile(context, filename)), null, null);
        } catch (FileNotFoundException e) {
        }
        return null;
    }
}
