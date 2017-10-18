package hram.sudoku;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import android.widget.ImageView;

import hram.sudoku.utils.Cache;

@SuppressLint("Registered")
public class SudokuActivity extends Activity {
    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        setContentView(R.layout.activity_sudoku);

        Bundle data = getIntent().getExtras();
        String fileName = data.getString(Constants.FILE_NAME);
        //Result region = data.getParcelable(Constants.SUDOKU_REGION);
        //Bitmap bitmap = data.getParcelable(DecodeThread.BARCODE_BITMAP);

        ImageView imageView = (ImageView) findViewById(R.id.imageView1);
        imageView.setImageBitmap(Cache.DecodeFile(this, fileName));
    }
}
