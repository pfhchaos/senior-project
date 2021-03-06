package com.senior.arexplorer.Utils;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.SystemClock;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.util.Log;
import android.view.Surface;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * This manager class handles compassVector events such as starting the tracking of device bearing, or
 * when a new compassVector update occurs.
 */
public class CompassAssistant implements SensorEventListener {

    // The rate sensor events will be delivered at. As the Android documentation states, this is only
    // a hint to the system and the events might actually be received faster or slower then this
    // specified rate. Since the minimum Android API levels about 9, we are able to set this value
    // ourselves rather than using one of the provided constants which deliver updates too quickly for
    // our use case. The default is set to 100ms. Overriding to 17 for 60 FPS refresh.
    private static final int SENSOR_DELAY_MICROS = 17 * 1000;
    // Filtering coefficient 0 < ALPHA < 1
    private static final float ALPHA = 0.45f;

    // Controls the compassVector update rate in milliseconds
    private static final int COMPASS_UPDATE_RATE_MS = 17;

    private final WindowManager windowManager;
    private final SensorManager sensorManager;
    private final List<CompassAssistantListener> compassAssistantListeners = new ArrayList<>();
    private final List<CompassAssistantListener> compassPitchListeners = new ArrayList<>();

    // Not all devices have a compassSensor
    @Nullable
    private Sensor compassSensor;
    @Nullable
    private Sensor gravitySensor;
    @Nullable
    private Sensor magneticFieldSensor;

    private float[] truncatedRotationVectorValue = new float[4];
    private float[] rotationMatrix = new float[9];
    private float[] rotationVectorValue;
    private float lastHeading;
    private int lastAccuracySensorStatus;
    private float lastPitch;
    private float disabledCompassAtAngle = 80; //this is 10 degrees from screen up flat

    private long compassUpdateNextTimestamp;
    private float[] gravityValues = new float[3];
    private float[] magneticValues = new float[3];

    private static CompassAssistant instance;

    /**
     * Construct a new instance of the this class. A internal compassVector listeners needed to separate it
     * from the cleared list of public listeners.
     */
    private CompassAssistant(WindowManager windowManager, SensorManager sensorManager) {
        this.windowManager = windowManager;
        this.sensorManager = sensorManager;
        compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        if (compassSensor == null) {
            if (isGyroscopeAvailable()) {
                Log.d("fallbackGyro", "Rotation vector sensor not supported on device, falling back to orientation.");
                compassSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
            } else {
                Log.d("fallbackAccelMagn", "Rotation vector sensor not supported on device, falling back to accelerometer and magnetic field.");
                gravitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
                magneticFieldSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            }
        }
    }

    public void addCompassListener(@NonNull CompassAssistantListener compassAssistantListener) {
        if (compassAssistantListeners.isEmpty() && compassPitchListeners.isEmpty()) {
            onStart();
        }
        compassAssistantListeners.add(compassAssistantListener);
    }

    public void addPitchListener(@NonNull CompassAssistantListener compassPitchListener) {
        if (compassPitchListeners.isEmpty() && compassAssistantListeners.isEmpty()) {
            onStart();
        }
        //Log.d("PitchTest", "Adding Pitch Listener " + compassPitchListener.getClass());
        compassPitchListeners.add(compassPitchListener);
    }

    public void removeCompassListener(@NonNull CompassAssistantListener compassAssistantListener) {
        compassAssistantListeners.remove(compassAssistantListener);
        if (compassAssistantListeners.isEmpty() && compassPitchListeners.isEmpty()) {
            onStop();
        }
    }

    public void removePitchListener(@NonNull CompassAssistantListener compassPitchListener) {
        compassPitchListeners.remove(compassPitchListener);
        if (compassPitchListeners.isEmpty() && compassAssistantListeners.isEmpty()) {
            onStop();
        }
    }

    public int getLastAccuracySensorStatus() {
        return lastAccuracySensorStatus;
    }

    public float getLastHeading() {
        return lastHeading;
    }

    public void onStart() {
        registerSensorListeners();
    }

    public void onStop() {
        unregisterSensorListeners();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        // check when the last time the compassVector was updated, return if too soon.
        long currentTime = SystemClock.elapsedRealtime();
        if (currentTime < compassUpdateNextTimestamp) {
            return;
        }
        if (lastAccuracySensorStatus == SensorManager.SENSOR_STATUS_UNRELIABLE) {
            Log.d("unreliable", "Compass sensor is unreliable, device calibration is needed.");
            return;
        }
        if (event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            rotationVectorValue = getRotationVectorFromSensorEvent(event);
            updateOrientation();

            // Update the compassUpdateNextTimestamp
            compassUpdateNextTimestamp = currentTime + COMPASS_UPDATE_RATE_MS;
        } else if (event.sensor.getType() == Sensor.TYPE_ORIENTATION) {
            notifyCompassChangeListeners((event.values[0] + 360) % 360);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            gravityValues = lowPassFilter(getRotationVectorFromSensorEvent(event), gravityValues);
            updateOrientation();
        } else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            magneticValues = lowPassFilter(getRotationVectorFromSensorEvent(event), magneticValues);
            updateOrientation();
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        if (lastAccuracySensorStatus != accuracy) {
            for (CompassAssistantListener compassAssistantListener : compassAssistantListeners) {
                compassAssistantListener.onCompassAccuracyChange(accuracy);
            }
            lastAccuracySensorStatus = accuracy;
        }
    }

    private boolean isGyroscopeAvailable() {
        return sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE) != null;
    }

    @SuppressWarnings("SuspiciousNameCombination")
    private void updateOrientation() {
        if (rotationVectorValue != null) {
            SensorManager.getRotationMatrixFromVector(rotationMatrix, rotationVectorValue);
        } else {
            // Get rotation matrix given the gravity and geomagnetic matrices
            SensorManager.getRotationMatrix(rotationMatrix, null, gravityValues, magneticValues);
        }

        final int worldAxisForDeviceAxisX;
        final int worldAxisForDeviceAxisY;

        // Remap the axes as if the device screen was the instrument panel,
        // and adjust the rotation matrix for the device orientation.
        switch (windowManager.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                worldAxisForDeviceAxisX = SensorManager.AXIS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_X;
                break;
            case Surface.ROTATION_180:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_MINUS_Z;
                break;
            case Surface.ROTATION_270:
                worldAxisForDeviceAxisX = SensorManager.AXIS_MINUS_Z;
                worldAxisForDeviceAxisY = SensorManager.AXIS_X;
                break;
            case Surface.ROTATION_0:
            default:
                worldAxisForDeviceAxisX = SensorManager.AXIS_X;
                worldAxisForDeviceAxisY = SensorManager.AXIS_Z;
                break;
        }

        float[] adjustedRotationMatrix = new float[9];
        SensorManager.remapCoordinateSystem(rotationMatrix, worldAxisForDeviceAxisX,
                worldAxisForDeviceAxisY, adjustedRotationMatrix);

        // Transform rotation matrix into azimuth/pitch/roll
        float[] orientation = new float[3];
        SensorManager.getOrientation(adjustedRotationMatrix, orientation);

        // The x-axis is all we care about here.
        notifyCompassChangeListeners((float) Math.toDegrees(orientation[0]));

        // Now we care about pitch!
        notifyPitchChangeListeners((float) Math.toDegrees(orientation[1]));
    }

    private void notifyCompassChangeListeners(float heading) {
        if(lastPitch < disabledCompassAtAngle) {
            for (CompassAssistantListener compassAssistantListener : compassAssistantListeners) {
                compassAssistantListener.onCompassChanged(heading);
            }
            lastHeading = heading;
        }
    }

    private void notifyPitchChangeListeners(float pitch) {
        for (CompassAssistantListener compassPitchListener : compassPitchListeners) {
            compassPitchListener.onPitchChanged(pitch);
        }
        //Log.d("PitchTest", "Notifying pitch listeners, new pitch is " + pitch);
        lastPitch = pitch;
    }

    private void registerSensorListeners() {
        if (isCompassSensorAvailable()) {
            // Does nothing if the sensors already registered.
            sensorManager.registerListener(this, compassSensor, SENSOR_DELAY_MICROS);
        } else {
            sensorManager.registerListener(this, gravitySensor, SENSOR_DELAY_MICROS);
            sensorManager.registerListener(this, magneticFieldSensor, SENSOR_DELAY_MICROS);
        }
    }

    private void unregisterSensorListeners() {
        if (isCompassSensorAvailable()) {
            sensorManager.unregisterListener(this, compassSensor);
        } else {
            sensorManager.unregisterListener(this, gravitySensor);
            sensorManager.unregisterListener(this, magneticFieldSensor);
        }
    }

    private boolean isCompassSensorAvailable() {
        return compassSensor != null;
    }

    /**
     * Helper function, that filters newValues, considering previous values
     *
     * @param newValues      array of float, that contains new data
     * @param smoothedValues array of float, that contains previous state
     * @return float filtered array of float
     */
    private float[] lowPassFilter(float[] newValues, float[] smoothedValues) {
        if (smoothedValues == null) {
            return newValues;
        }
        for (int i = 0; i < newValues.length; i++) {
            smoothedValues[i] = smoothedValues[i] + ALPHA * (newValues[i] - smoothedValues[i]);
        }
        return smoothedValues;
    }

    /**
     * Pulls out the rotation vector from a SensorEvent, with a maximum length
     * vector of four elements to avoid potential compatibility issues.
     *
     * @param event the sensor event
     * @return the events rotation vector, potentially truncated
     */
    @NonNull
    private float[] getRotationVectorFromSensorEvent(@NonNull SensorEvent event) {
        if (event.values.length > 4) {
            // On some Samsung devices SensorManager.getRotationMatrixFromVector
            // appears to throw an exception if rotation vector has length > 4.
            // For the purposes of this class the first 4 values of the
            // rotation vector are sufficient (see crbug.com/335298 for details).
            // Only affects Android 4.3
            System.arraycopy(event.values, 0, truncatedRotationVectorValue, 0, 4);
            return truncatedRotationVectorValue;
        } else {
            return event.values;
        }
    }

    public static CompassAssistant getInstance(Context context){
        if(instance == null){
            WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
            SensorManager sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            instance = new CompassAssistant(windowManager, sensorManager);
        }
        return instance;
    }

    public static CompassAssistant getInstance(){
        if(instance == null) Log.e("CompassAssistant", "There is no instance yet, need to pass context");
        return instance;
    }

    public void cleanUp(){
        if(instance != null)
            instance.onStop();
        instance = null;
    }

    public static float shortestRotation(float heading, float previousHeading) {
        double diff = previousHeading - heading;
        if (diff > 180.0f) {
            heading += 360.0f;
        } else if (diff < -180.0f) {
            heading -= 360.f;
        }
        return heading;
    }

    /**
     * Callbacks related to the compassVector
     */
    public interface CompassAssistantListener {

        /**
         * Callback's invoked when a new compassVector update occurs. You can listen into the compassVector updates
         * using (CompassAssistantListener)} and implementing these
         * callbacks. Note that this interface is also used internally to to update the UI chevron/arrow.
         *
         * @param userHeading the new compassVector heading
         */
        void onCompassChanged(float userHeading);

        /**
         * Callback's invoked when a new compassVector update occurs. You can listen into the compassVector updates
         * using (CompassAssistantListener)} and implementing these callbacks.
         * callbacks. Note that this interface is also used internally to to update the UI chevron/arrow.
         *
         * @param userPitch the new compassVector pitch on a range of -90 to 90. NOTE: -90 indicates back of the phone is facing zenith
         */
        void onPitchChanged(float userPitch);

        /**
         * This gets invoked when the compassVector accuracy status changes from one value to another. It
         * provides an integer value which is identical to the {@code SensorManager} class constants:
         * <ul>
         * <li>{@link android.hardware.SensorManager#SENSOR_STATUS_NO_CONTACT}</li>
         * <li>{@link android.hardware.SensorManager#SENSOR_STATUS_UNRELIABLE}</li>
         * <li>{@link android.hardware.SensorManager#SENSOR_STATUS_ACCURACY_LOW}</li>
         * <li>{@link android.hardware.SensorManager#SENSOR_STATUS_ACCURACY_MEDIUM}</li>
         * <li>{@link android.hardware.SensorManager#SENSOR_STATUS_ACCURACY_HIGH}</li>
         * </ul>
         *
         * @param compassStatus the new accuracy of this sensor, one of
         *                      {@code SensorManager.SENSOR_STATUS_*}
         */
        void onCompassAccuracyChange(int compassStatus);

    }
}