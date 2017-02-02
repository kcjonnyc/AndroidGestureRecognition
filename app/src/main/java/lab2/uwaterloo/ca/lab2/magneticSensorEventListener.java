package lab2.uwaterloo.ca.lab2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

/**
 * Created by KCJon on 2017-01-19.
 */

public class magneticSensorEventListener implements SensorEventListener {
    TextView output;
    TextView outputHigh;
    float high1, high2, high3;
    static boolean first;
    public magneticSensorEventListener(TextView outputView, TextView outputHighView) {
        output = outputView;
        outputHigh = outputHighView;
        high1 = 0;
        high2 = 0;
        high3 = 0;
        first = true;
    }
    public void onAccuracyChanged(Sensor s, int i) {
    }
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            String s = String.format("(%.2f, %.2f, %.2f)", se.values[0], se.values[1], se.values[2]);
            if (first) {
                high1 = se.values[0];
                high2 = se.values[1];
                high3 = se.values[2];
                first = false;
            }
            if (se.values[0] > high1) {
                high1 = se.values[0];
            }
            if (se.values[1] > high2) {
                high2 = se.values[1];
            }
            if (se.values[2] > high3) {
                high3 = se.values[2];
            }
            String sHigh = String.format("(%.2f, %.2f, %.2f)", high1, high2, high3);
            output.setText(s);
            outputHigh.setText(sHigh);
        }
    }
}
