package hram.sudoku.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import hram.sudoku.BuildConfig;
import hram.sudoku.Constants;
import hram.sudoku.R;
import hram.sudoku.result.Result;
import hram.sudoku.utils.IOUtil;

public class RecognitionErrorActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_recognition_error);

        findViewById(R.id.bt_try_again).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RecognitionErrorActivity.this, CaptureActivity.class);
                startActivity(intent);
                finish();
            }
        });

        findViewById(R.id.bt_sent_email).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent emailIntent = new Intent(Intent.ACTION_SEND);
                    emailIntent.setType("message/rfc822");
                    emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{"hram.ov.e@gmail.com"});
                    emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Sudoca error");
                    emailIntent.putExtra(Intent.EXTRA_TEXT, getBodyText());
                    Uri uri = Uri.fromFile(getRawFile());
                    emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
                    try {
                        startActivity(Intent.createChooser(emailIntent, "Отправить письмо..."));
                    } catch (android.content.ActivityNotFoundException ex) {
                        Toast.makeText(RecognitionErrorActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        });
    }

    private File getRawFile() throws IOException {
        FileInputStream fis = openFileInput(Constants.LAST_SUDOKU_NAME);

        File dir = new File(getExternalFilesDir(null) + "/tmp/");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        File file = new File(dir, Constants.LAST_SUDOKU_NAME);

        OutputStream out = new FileOutputStream(file, true);

        IOUtil.copyFile(fis, out);

        fis.close();
        out.close();

        return file;
    }

    private String getBodyText() {
        StringBuilder sb = new StringBuilder();
        Bundle bundle = getIntent().getExtras();
        bundle.setClassLoader(Result.class.getClassLoader());
        Result res = (Result) bundle.getParcelable(Constants.SUDOKU_REGION);
        int width = bundle.getInt(Constants.SUDOKU_WIDTH);
        int height = bundle.getInt(Constants.SUDOKU_HEIGHT);
        Rect framingRect = bundle.getParcelable(Constants.SUDOKU_FRAMING_RECT);

        sb.append(String.format("Width: %d\n", width));
        sb.append(String.format("Height: %d\n", height));
        sb.append(String.format("FR left: %d\n", framingRect.left));
        sb.append(String.format("FR top: %d\n", framingRect.top));
        sb.append(String.format("FR right: %d\n", framingRect.right));
        sb.append(String.format("FR bottom: %d\n", framingRect.bottom));
        sb.append(String.format("FR width: %d\n", framingRect.width()));
        sb.append(String.format("FR height: %d\n", framingRect.height()));
        sb.append(String.format("Result TL: %d,%d\n", res.TL.x, res.TL.y));
        sb.append(String.format("Result TR: %d,%d\n", res.TR.x, res.TR.y));
        sb.append(String.format("Result BL: %d,%d\n", res.BL.x, res.BL.y));
        sb.append(String.format("Result BR: %d,%d\n", res.BR.x, res.BR.y));
        sb.append("\n");
        sb.append("App Version: " + BuildConfig.VERSION_NAME + "\n");
        sb.append("OS Version: " + System.getProperty("os.version") + "(" + android.os.Build.VERSION.INCREMENTAL + ")" + "\n");
        sb.append("OS API Level: " + android.os.Build.VERSION.SDK + "\n");
        sb.append("Device: " + android.os.Build.DEVICE + "\n");
        sb.append("Model (and Product): " + android.os.Build.MODEL + " (" + android.os.Build.PRODUCT + ")" + "\n");
        sb.append("Country: " + getResources().getConfiguration().locale.getCountry() + "\n");
        sb.append("Locale: " + Locale.getDefault().toString() + "\n");

        return sb.toString();
    }
}
