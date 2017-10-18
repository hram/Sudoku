package hram.sudoku.activity;

import android.content.Intent;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;

import hram.sudoku.Constants;
import io.fabric.sdk.android.Fabric;

public class ActivityLauncher extends BaseActivity {

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        if (!hram.sudoku.BuildConfig.DEBUG) {
            Fabric.with(this, new Crashlytics());
        }

        Intent intent = null;
        if (isNeedToContinueGame()) {
            intent = new Intent(this, PlayActivity.class);
            intent.putExtra(Constants.EXTRA_SUDOKU_ID, sudokuGameID);
        } else {
            intent = new Intent(this, CaptureActivity.class);
        }

        startActivity(intent);
        finish();
    }
}
