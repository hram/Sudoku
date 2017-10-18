package hram.sudoku.tess;

import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class TessAPI {
    private static TessAPI _instance;
    //public static String DATA_PATH;
    public static final String lang = "eng";
    private static TessBaseAPI baseApi;

    @NonNull
    public static TessAPI getInstance(@NonNull Context context) {
        if (_instance == null) {
            _instance = new TessAPI(context);
        }
        return _instance;
    }

    private TessAPI(@NonNull Context context) {
        final File dir = new File(context.getExternalFilesDir(null) + "/tessdata/");
        if (!dir.exists()) {
            if (!dir.mkdirs()) {
                //Log.v(Constants.TAG, "ERROR: Creation of directory " + dir + " on sdcard failed");
                return;
            }
        }

        File file = new File(context.getExternalFilesDir(null) + "/tessdata/", "eng.traineddata");

        // lang.traineddata file with the app (in assets folder)
        // You can get them at:
        // http://code.google.com/p/tesseract-ocr/downloads/list
        // This area needs work and optimization
        if (!file.exists()) {
            try {

                AssetManager assetManager = context.getAssets();
                InputStream in = assetManager.open("tessdata/eng.traineddata");
                //GZIPInputStream gin = new GZIPInputStream(in);
                OutputStream out = new FileOutputStream(file, true);

                // Transfer bytes from in to out
                byte[] buf = new byte[1024];
                int len;
                //while ((lenf = gin.read(buff)) > 0) {
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
                in.close();
                //gin.close();
                out.close();
            } catch (IOException e) {
                // TODO: 18.10.2017  
            }
        }

        baseApi = new TessBaseAPI();
        //baseApi.setDebug(true);
        baseApi.init(context.getExternalFilesDir(null).toString(), "eng");
        //baseApi.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_CHAR);
        baseApi.setVariable(TessBaseAPI.VAR_CHAR_WHITELIST, "123456789");
        //baseApi.setVariable("tessedit_char_whitelist", "0123456789-.");
        //baseApi.setVariable("classify_bln_numeric_mode", "1");
        //baseApi.init(DATA_PATH, "eng");
        //baseApi.init(DATA_PATH, "eng", TessBaseAPI.OEM_TESSERACT_ONLY);
    }

    public TessBaseAPI GetApi() {
        return baseApi;
    }
}
