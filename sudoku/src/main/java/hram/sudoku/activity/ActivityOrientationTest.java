package hram.sudoku.activity;

import android.app.Fragment;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hram.sudoku.R;
import hram.sudoku.utils.Log;

public class ActivityOrientationTest extends Fragment implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private Sensor mMagnetometer;

    private float[] mLastAccelerometer = new float[3];
    private float[] mLastMagnetometer = new float[3];
    private boolean mLastAccelerometerSet = false;
    private boolean mLastMagnetometerSet = false;

    private float[] mR = new float[9];
    private float[] mOrientation = new float[3];

    private OrientationEventListener mOrientationEventListener;
    private int mLastOrientation = -1;
    private static final int ORIENTATION_PORTRAIT_NORMAL = 1;
    private static final int ORIENTATION_PORTRAIT_INVERTED = 2;
    private static final int ORIENTATION_LANDSCAPE_NORMAL = 3;
    private static final int ORIENTATION_LANDSCAPE_INVERTED = 4;

    private TextView tv;
    private TextView tv2;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rv = inflater.inflate(R.layout.activity_orientation_test, container, false);

        tv = (TextView) rv.findViewById(R.id.textView1);
        tv2 = (TextView) rv.findViewById(R.id.textView2);

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        return rv;
    }

    @Override
    public void onResume() {
        super.onResume();
        mLastAccelerometerSet = false;
        mLastMagnetometerSet = false;
        mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, SensorManager.SENSOR_DELAY_NORMAL);

        if (mOrientationEventListener == null) {
            mOrientationEventListener = new OrientationEventListener(getActivity(), SensorManager.SENSOR_DELAY_NORMAL) {

                @Override
                public void onOrientationChanged(int orientation) {

                    // determine our orientation based on sensor response
                    int lastOrientation = mLastOrientation;

                    if (orientation >= 315 || orientation < 45) {
                        if (mLastOrientation != ORIENTATION_PORTRAIT_NORMAL) {
                            mLastOrientation = ORIENTATION_PORTRAIT_NORMAL;
                        }
                    } else if (orientation < 315 && orientation >= 225) {
                        if (mLastOrientation != ORIENTATION_LANDSCAPE_NORMAL) {
                            mLastOrientation = ORIENTATION_LANDSCAPE_NORMAL;
                        }
                    } else if (orientation < 225 && orientation >= 135) {
                        if (mLastOrientation != ORIENTATION_PORTRAIT_INVERTED) {
                            mLastOrientation = ORIENTATION_PORTRAIT_INVERTED;
                        }
                    } else { // orientation <135 && orientation > 45
                        if (mLastOrientation != ORIENTATION_LANDSCAPE_INVERTED) {
                            mLastOrientation = ORIENTATION_LANDSCAPE_INVERTED;
                        }
                    }

                    if (lastOrientation != mLastOrientation) {
                        changeRotation(mLastOrientation, lastOrientation);
                    }
                }
            };
        }
        if (mOrientationEventListener.canDetectOrientation()) {
            mOrientationEventListener.enable();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        mSensorManager.unregisterListener(this);
        mOrientationEventListener.disable();
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor == mAccelerometer) {
            System.arraycopy(event.values, 0, mLastAccelerometer, 0, event.values.length);
            mLastAccelerometerSet = true;
        } else if (event.sensor == mMagnetometer) {
            System.arraycopy(event.values, 0, mLastMagnetometer, 0, event.values.length);
            mLastMagnetometerSet = true;
        }
        if (mLastAccelerometerSet && mLastMagnetometerSet) {
            SensorManager.getRotationMatrix(mR, null, mLastAccelerometer, mLastMagnetometer);
            SensorManager.getOrientation(mR, mOrientation);
            Log.i("OrientationTestActivity", String.format("Orientation: %f, %f, %f", mOrientation[0], mOrientation[1], mOrientation[2]));
            tv.setText(String.format("Orientation: %f, %f, %f", mOrientation[0], mOrientation[1], mOrientation[2]));
        }
    }

    /**
     * Performs required action to accommodate new orientation
     *
     * @param orientation
     * @param lastOrientation
     */
    private void changeRotation(int orientation, int lastOrientation) {
        switch (orientation) {
            case ORIENTATION_PORTRAIT_NORMAL:
                tv2.setText("Orientation = 90");
                break;
            case ORIENTATION_LANDSCAPE_NORMAL:
                tv2.setText("Orientation = 0");
                break;
            case ORIENTATION_PORTRAIT_INVERTED:
                tv2.setText("Orientation = 270");
                break;
            case ORIENTATION_LANDSCAPE_INVERTED:
                tv2.setText("Orientation = 180");
                break;
        }
    }
}
