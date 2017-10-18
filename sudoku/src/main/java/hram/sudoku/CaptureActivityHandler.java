package hram.sudoku;


import android.os.Handler;
import android.os.Message;

import hram.sudoku.activity.CaptureActivity;
import hram.sudoku.camera.CameraManager;

/**
 * This class handles all the messaging which comprises the state machine for capture.
 *
 * @author dswitkin@google.com (Daniel Switkin)
 */
public final class CaptureActivityHandler extends Handler {

    private final CaptureActivity activity;
    private final DecodeThread decodeThread;
    private State state;
    private final CameraManager cameraManager;

    private enum State {
        PREVIEW,
        SUCCESS,
        DONE
    }

    public CaptureActivityHandler(CaptureActivity activity, String characterSet, CameraManager cameraManager) {
        this.activity = activity;
        decodeThread = new DecodeThread(activity, characterSet, new ViewfinderResultLinesCallback(activity.getViewfinderView()), new ViewfinderFoundWeightCallback(activity.getViewfinderView()), new ViewfinderBitmapCallback(activity.getViewfinderView()));
        decodeThread.start();
        SetState(State.SUCCESS);

        // Start ourselves capturing previews and decoding.
        this.cameraManager = cameraManager;
        cameraManager.startPreview();
        restartPreviewAndDecode();
    }

    @Override
    public void handleMessage(Message message) {
        switch (message.what) {
            case R.id.auto_focus:
                //Log.d(TAG, "Got auto-focus message");
                // When one auto focus pass finishes, start another. This is the closest thing to
                // continuous AF. It does seem to hunt a bit, but I'm not sure what else to do.
                if (GetState() == State.PREVIEW) {
                    cameraManager.requestAutoFocus(this, R.id.auto_focus);
                }
                break;
            case R.id.restart_preview:
                restartPreviewAndDecode();
                break;
            case R.id.decode_succeeded:
                SetState(State.SUCCESS);
                activity.handleDecode(message.getData());
                break;
            case R.id.decode_failed:
                // We're decoding as fast as possible, so when one decode fails, start another.
                SetState(State.PREVIEW);
                cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
                break;
            case R.id.save_to_file:
                decodeThread.getHandler().sendEmptyMessage(R.id.save_to_file);
                break;
            //case R.id.return_scan_result:
            //	Log.d(TAG, "Got return scan result message");
            //	activity.setResult(Activity.RESULT_OK, (Intent) message.obj);
            //	activity.finish();
            //	break;
            //case R.id.launch_product_query:
            //	Log.d(TAG, "Got product query message");
            //	String url = (String) message.obj;
            //	Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            //	intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            //	activity.startActivity(intent);
            //	break;
        }
    }


    /**
     * Завершение потока
     */
    public void quitSynchronously() {
        SetState(State.DONE);
        cameraManager.stopPreview();
        Message quit = Message.obtain(decodeThread.getHandler(), R.id.quit);
        quit.sendToTarget();
        try {
            // Wait at most half a second; should be enough time, and onPause() will timeout quickly
            decodeThread.join(500L);
        } catch (InterruptedException e) {
            // continue
        }

        // Be absolutely sure we don't send any queued up messages
        removeMessages(R.id.decode_succeeded);
        removeMessages(R.id.decode_failed);
    }

    private void restartPreviewAndDecode() {
        if (GetState() == State.SUCCESS) {
            SetState(State.PREVIEW);
            cameraManager.requestPreviewFrame(decodeThread.getHandler(), R.id.decode);
            cameraManager.requestAutoFocus(this, R.id.auto_focus);
            activity.drawViewfinder();
        }
    }

    private void SetState(State value) {
        //Log.d(TAG, "SetState: " + value);
        state = value;
    }

    private State GetState() {
        return state;
    }
}
