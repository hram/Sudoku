package hram.sudoku;

import android.os.Handler;
import android.os.Looper;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import hram.sudoku.activity.CaptureActivity;
import hram.sudoku.core.BitmapCallback;
import hram.sudoku.core.DecodeHintType;
import hram.sudoku.core.FoundWeightCallback;
import hram.sudoku.core.ResultLinesCallback;

/**
 * This thread does all the heavy lifting of decoding the images.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
final class DecodeThread extends Thread {
    private final CaptureActivity activity;
    private final Map<DecodeHintType, Object> hints;
    private Handler handler;
    private final CountDownLatch handlerInitLatch;

    DecodeThread(CaptureActivity activity, String characterSet, ResultLinesCallback resultLinesCallback, FoundWeightCallback foundWeightCallback, BitmapCallback bitmapCallback) {
        this.activity = activity;
        handlerInitLatch = new CountDownLatch(1);

        hints = new EnumMap<DecodeHintType, Object>(DecodeHintType.class);
        //hints.put(DecodeHintType.NEED_RESULT_POINT_CALLBACK, resultLinesCallback);
        //hints.put(DecodeHintType.NEED_FOUND_WEIGHT_CALLBACK, foundWeightCallback);
        //hints.put(DecodeHintType.NEED_RESULT_BITMAP_CALLBACK, bitmapCallback);
    }

    Handler getHandler() {
        try {
            handlerInitLatch.await();
        } catch (InterruptedException ie) {
            // continue?
        }
        return handler;
    }

    @Override
    public void run() {
        Looper.prepare();
        handler = new DecodeHandler(activity, hints);
        handlerInitLatch.countDown();
        Looper.loop();
    }
}
