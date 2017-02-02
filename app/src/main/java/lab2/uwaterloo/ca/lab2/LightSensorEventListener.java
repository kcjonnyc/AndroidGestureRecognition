package lab2.uwaterloo.ca.lab2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * Created by KCJon on 2017-01-19.
 */

public class LightSensorEventListener implements SensorEventListener {
    TextView output;
    TextView outputHigh;
    float high;
    static boolean first;
    NumberFormat formatter = new DecimalFormat("#0.00"); // formats a float into a string
    public LightSensorEventListener(TextView outputView, TextView outputHighView) {
        output = outputView; // assigns references
        outputHigh = outputHighView;
        high = 0;
        first = true;
    }
    public void onAccuracyChanged(Sensor s, int i) {
    }
    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (first) {
                high = se.values[0];
                first = false;
            }
            if (se.values[0] > high) {
                high = se.values[0];
            }
            output.setText(formatter.format(se.values[0]));
            outputHigh.setText(formatter.format(high));
        }
    }
}
