package hram.sudoku.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.os.Handler;
import android.os.Message;

import hram.sudoku.R;

/**
 * Получает кадры с камеры и события автофокуса
 */
public final class PreviewCallback implements Camera.PreviewCallback, Camera.AutoFocusCallback {
    private static final long AUTOFOCUS_INTERVAL_MS = 2000L;
    private static final long AUTOFOCUS_TIME_LIMIT_MILISECONDS = 1500L;

    private final CameraConfigurationManager configManager;
    private Handler previewHandler;
    private int previewMessage;
    private Handler autoFocusHandler;
    private int autoFocusMessage;
    private long autoFocusTime = 0;
    private HandlerHolder mHandlerHolder;

    PreviewCallback(HandlerHolder holder, CameraConfigurationManager configManager) {
        mHandlerHolder = holder;
        this.configManager = configManager;
    }

    void setPreviewHandler(Handler previewHandler, int previewMessage) {
        this.previewHandler = previewHandler;
        this.previewMessage = previewMessage;
    }

    void setAutoFocusHandler(Handler autoFocusHandler, int autoFocusMessage) {
        this.autoFocusHandler = autoFocusHandler;
        this.autoFocusMessage = autoFocusMessage;
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
        // если работает автофокус то работаем только после автофокуса
        if (autoFocusHandler != null && System.currentTimeMillis() - autoFocusTime > AUTOFOCUS_TIME_LIMIT_MILISECONDS) {
            Handler handler = mHandlerHolder.getHandler();
            if (handler != null) {
                Message message = Message.obtain(handler, R.id.decode_failed);
                message.sendToTarget();
            }
            return;
        }

        // данные могут быть в разных форматах
        // int	JPEG	Encoded formats.
        // int	NV16	YCbCr format, used for video.
        // int	NV21	YCrCb format used for images, which uses the NV21 encoding format.
        // int	RGB_565	RGB format used for pictures encoded as RGB_565.
        // int	UNKNOWN
        // int	YUY2	YCbCr format used for images, which uses YUYV (YUY2) encoding format.
        // int	YV12	Android YUV format.

        // перекодирование
        // Camera.Parameters parameters = camera.getParameters();
        // Size size = parameters.getPreviewSize();
        // YuvImage image = new YuvImage(data, parameters.getPreviewFormat(), size.width, size.height, null);

        Point cameraResolution = configManager.getCameraResolution();
        Handler thePreviewHandler = previewHandler;
        if (thePreviewHandler != null) {
            Message message = thePreviewHandler.obtainMessage(previewMessage, cameraResolution.x, cameraResolution.y, data);
            message.sendToTarget();
            previewHandler = null;
        }
    }

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
        if (autoFocusHandler != null) {
            autoFocusTime = System.currentTimeMillis();
            Message message = autoFocusHandler.obtainMessage(autoFocusMessage, success);
            // Simulate continuous autofocus by sending a focus request every
            // AUTOFOCUS_INTERVAL_MS milliseconds.
            //Log.d(TAG, "Got auto-focus callback; requesting another");
            autoFocusHandler.sendMessageDelayed(message, AUTOFOCUS_INTERVAL_MS);
            autoFocusHandler = null;
        }
    }

    public interface HandlerHolder {
        Handler getHandler();
    }
}
