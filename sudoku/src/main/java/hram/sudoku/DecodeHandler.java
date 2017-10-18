package hram.sudoku;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.io.FileOutputStream;
import java.util.Map;

import hram.sudoku.activity.CaptureActivity;
import hram.sudoku.core.BinaryBitmap;
import hram.sudoku.core.DecodeHintType;
import hram.sudoku.core.LuminanceSource;
import hram.sudoku.core.MultiFormatReader;
import hram.sudoku.core.NotFoundException;
import hram.sudoku.core.PlanarYUVLuminanceSource;
import hram.sudoku.core.common.HybridBinarizer;
import hram.sudoku.result.Result;

final class DecodeHandler extends Handler {

    private final CaptureActivity activity;
    private final MultiFormatReader multiFormatReader;
    private boolean running = true;

    DecodeHandler(CaptureActivity activity, Map<DecodeHintType, Object> hints) {
        multiFormatReader = new MultiFormatReader();
        multiFormatReader.setHints(hints);
        this.activity = activity;
    }

    @Override
    public void handleMessage(Message message) {
        if (!running) {
            return;
        }

        switch (message.what) {
            // The data will arrive as byte[] in the message.obj field, with width and height encoded as message.arg1 and message.arg2, respectively
            case R.id.decode:
                decode((byte[]) message.obj, message.arg1, message.arg2);
                break;
            case R.id.quit:
                running = false;
                Looper.myLooper().quit();
                break;
        }
    }

    /**
     * Decode the data within the viewfinder rectangle, and time how long it took. For efficiency,
     * reuse the same reader objects from one decode to the next.
     *
     * @param data   The YUV preview frame.
     * @param width  The width of the preview frame.
     * @param height The height of the preview frame.
     */
    private void decode(byte[] data, int width, int height) {
        Handler handler = activity.getHandler();
        if (handler == null) {
            return;
        }

        try {

            // окно захвата
            Rect rect = activity.getCameraManager().getFramingRect();

            long start = System.currentTimeMillis();

            // вырезание прамоугольного окна из полного кадра для увеличение производительности
            LuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top, rect.width(), rect.height(), false);

            // поиск судоку
            Result rawResult = multiFormatReader.decodeWithState(new BinaryBitmap(new HybridBinarizer(source)));

            boolean founded = rawResult != null;

            long end = System.currentTimeMillis();
            if (founded) {
//                Log.v(TAG, "Found sudoku in " + (end - start) + " ms");
            } else {
//                Log.v(TAG, "Process sudoku in " + (end - start) + " ms");
            }

            // если судоку не найден то выходим
            if (founded == false) {
                throw NotFoundException.getNotFoundInstance();
            }

            // если надо прервать обработку
            //if(true)
            //{
            //    Message message = Message.obtain(handler, R.id.decode_failed);
            //    message.sendToTarget();
            //    return;
            //}


            // сохраняем в файл для послед. обработки
            FileOutputStream fos = activity.openFileOutput(Constants.LAST_SUDOKU_NAME, Context.MODE_PRIVATE);
            fos.write(data);
            fos.close();

            // !!! тут подумать что возвращать, возможно сырые даные лучше вернуть
            // потом для отображения конвертануть в RGB а для анализа использовать Y канал
            Message message = Message.obtain(handler, R.id.decode_succeeded, rawResult);
            Bundle bundle = new Bundle();
            bundle.putParcelable(Constants.SUDOKU_REGION, rawResult);
            bundle.putParcelable(Constants.SUDOKU_FRAMING_RECT, activity.getCameraManager().getFramingRect());
            bundle.putInt(Constants.SUDOKU_WIDTH, width);
            bundle.putInt(Constants.SUDOKU_HEIGHT, height);
            bundle.putInt(Constants.SUDOKU_FILE_TYPE, Constants.FILE_TYPE_YUV420SP);
            //bundle.putInt(Constants.SUDOKU_ORIENTATION, activity.getOrientation());
            message.setData(bundle);
            message.sendToTarget();
        } catch (NotFoundException e) {
            Message message = Message.obtain(handler, R.id.decode_failed);
            message.sendToTarget();
        } catch (Exception e) {
            Message message = Message.obtain(handler, R.id.decode_failed);
            message.sendToTarget();
        } finally {
            multiFormatReader.reset();
        }
    }
}
