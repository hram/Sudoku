package hram.sudoku.camera;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.hardware.Camera;
import android.preference.PreferenceManager;
import android.view.Display;
import android.view.WindowManager;

import java.util.Collection;

import hram.sudoku.PreferencesActivity;
import hram.sudoku.utils.Log;

/**
 * A class which deals with reading, parsing, and setting the camera parameters which are used to
 * configure the camera hardware.
 */
final class CameraConfigurationManager {

    private static final String TAG = "CameraConfiguration";
    private static final int MIN_PREVIEW_PIXELS = 320 * 240; // small screen
    private static final int MAX_PREVIEW_PIXELS = 800 * 480; // large/HD screen

    private final Context context;
    // разрешение экрана
    private Point screenResolution;
    // разрешение камеры
    private Point cameraResolution;

    Point getCameraResolution() {
        return cameraResolution;
    }

    Point getScreenResolution() {
        return screenResolution;
    }

    CameraConfigurationManager(Context context) {
        this.context = context;
    }

    /**
     * Для unit теста
     *
     * @param context
     * @param screenResolution
     * @param cameraResolution
     */
    CameraConfigurationManager(Context context, Point screenResolution, Point cameraResolution) {
        this.context = context;
        this.screenResolution = screenResolution;
        this.cameraResolution = cameraResolution;
    }

    /**
     * Чтение параметров камеры и вычисление оптимального разрешения захвата
     */
    void initFromCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = manager.getDefaultDisplay();
        int width = display.getWidth();
        int height = display.getHeight();
        // We're landscape-only, and have apparently seen issues with display thinking it's portrait
        // when waking from sleep. If it's not landscape, assume it's mistaken and reverse them:
        if (width < height) {
            Log.i(TAG, "Display reports portrait orientation; assuming this is incorrect");
            width = display.getHeight();
            height = display.getWidth();
        }
        screenResolution = new Point(width, height);
        Log.i(TAG, "Разрешение экрана: " + screenResolution);
        cameraResolution = findBestPreviewSizeValue(parameters, screenResolution, false);
        Log.d(TAG, "Будет производиться захват с разрешением " + cameraResolution);
    }

    /**
     * Выставление требуемых параметров для камеры
     *
     * @param camera
     */
    void setDesiredCameraParameters(Camera camera) {
        Camera.Parameters parameters = camera.getParameters();

        if (parameters == null) {
            Log.w(TAG, "Device error: no camera parameters are available. Proceeding without configuration.");
            return;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        // инициализация вспышки
        initializeTorch(parameters, prefs);

        // инициализация автофокуса
        String focusMode = findSettableValue(parameters.getSupportedFocusModes(), Camera.Parameters.FOCUS_MODE_AUTO, Camera.Parameters.FOCUS_MODE_MACRO);
        if (focusMode != null) {
            parameters.setFocusMode(focusMode);
        }

        // выставление разрешения
        parameters.setPreviewSize(cameraResolution.x, cameraResolution.y);

        // сохранение параметров в настройках камеры
        camera.setParameters(parameters);
    }

//  void setTorch(Camera camera, boolean newSetting) {
//    Camera.Parameters parameters = camera.getParameters();
//    doSetTorch(parameters, newSetting);
//    camera.setParameters(parameters);
//    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//    boolean currentSetting = prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false);
//    if (currentSetting != newSetting) {
//      SharedPreferences.Editor editor = prefs.edit();
//      editor.putBoolean(PreferencesActivity.KEY_FRONT_LIGHT, newSetting);
//      editor.commit();
//    }
//  }

    /**
     * Инициализация вспышки
     *
     * @param parameters
     * @param prefs
     */
    private static void initializeTorch(Camera.Parameters parameters, SharedPreferences prefs) {
        boolean currentSetting = prefs.getBoolean(PreferencesActivity.KEY_FRONT_LIGHT, false);
        doSetTorch(parameters, currentSetting);
    }

    /**
     * Включение/отключение вспышки через настройки камеры
     *
     * @param parameters
     * @param newSetting
     */
    private static void doSetTorch(Camera.Parameters parameters, boolean newSetting) {
        String flashMode;
        if (newSetting) {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_TORCH, Camera.Parameters.FLASH_MODE_ON);
        } else {
            flashMode = findSettableValue(parameters.getSupportedFlashModes(), Camera.Parameters.FLASH_MODE_OFF);
        }

        if (flashMode != null) {
            parameters.setFlashMode(flashMode);
        }
    }

    /**
     * Поиск оптимального разрешения камеры
     *
     * @param parameters
     * @param screenResolution
     * @param portrait
     * @return
     */
    private static Point findBestPreviewSizeValue(Camera.Parameters parameters, Point screenResolution, boolean portrait) {
        Point bestSize = null;
        int diff = Integer.MAX_VALUE;

        for (Camera.Size supportedPreviewSize : parameters.getSupportedPreviewSizes()) {
            Log.d(TAG, "Доступно разрешение: " + new Point(supportedPreviewSize.width, supportedPreviewSize.height));
            int pixels = supportedPreviewSize.height * supportedPreviewSize.width;
            if (pixels < MIN_PREVIEW_PIXELS || pixels > MAX_PREVIEW_PIXELS) {
                Log.d(TAG, "Вне разрешенного диапазона");
                continue;
            }

            Log.d(TAG, "В рамках разрешенного диапазона");
            int supportedWidth = portrait ? supportedPreviewSize.height : supportedPreviewSize.width;
            int supportedHeight = portrait ? supportedPreviewSize.width : supportedPreviewSize.height;
            int newDiff = Math.abs(screenResolution.x * supportedHeight - supportedWidth * screenResolution.y);
            if (newDiff == 0) {
                bestSize = new Point(supportedWidth, supportedHeight);
                break;
            }
            if (newDiff < diff) {
                bestSize = new Point(supportedWidth, supportedHeight);
                diff = newDiff;
            }
        }

        if (bestSize == null) {
            Log.d(TAG, "Не найдено подходящее разрешение");
            Camera.Size defaultSize = parameters.getPreviewSize();
            bestSize = new Point(defaultSize.width, defaultSize.height);
        }

        return bestSize;
    }

    private static String findSettableValue(Collection<String> supportedValues, String... desiredValues) {
        Log.i(TAG, "Supported values: " + supportedValues);
        String result = null;
        if (supportedValues != null) {
            for (String desiredValue : desiredValues) {
                if (supportedValues.contains(desiredValue)) {
                    result = desiredValue;
                    break;
                }
            }
        }
        Log.i(TAG, "Settable value: " + result);
        return result;
    }

}
