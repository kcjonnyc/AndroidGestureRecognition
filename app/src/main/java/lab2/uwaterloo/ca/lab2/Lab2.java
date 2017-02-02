package lab2.uwaterloo.ca.lab2;

import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Arrays;

import ca.uwaterloo.sensortoy.LineGraphView;

public class Lab2 extends AppCompatActivity {

    // CLASS VARIABLES
    LinearLayout lay;                                       // layout
    TextView output;                                        // textview for direction of movement
    LineGraphView graph;                                    // line graph view object for graph
    float[][] accelReadings = new float[100][3];            // array to store acceleration readings in 3 components

    // CREATE LABEL METHOD
    // method to create a textview object, add it to layout and return a reference to it
    public TextView createLabel(String text) {
        // create a textview that has not been defined in the xml
        // getApplicationContext generates a new id for textview reference
        TextView tv = new TextView(getApplicationContext());
        tv.setText(text); // set text passed user passed into the method
        tv.setTextColor(Color.BLACK);
        tv.setTextSize(20);
        lay.addView(tv); // add to layout
        return tv;
    }

    // ON CREATE METHOD
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lab2);

        // LAYOUT INITIALIZATION
        lay = (LinearLayout) findViewById(R.id.activity_lab2); // get reference to layout
        lay.setBackgroundColor(Color.WHITE);
        lay.setOrientation(LinearLayout.VERTICAL);

        // GRAPH INITIALIZATION
        graph = new LineGraphView(getApplicationContext(), 100, Arrays.asList("x", "y", "z"));
        lay.addView(graph);
        graph.setVisibility(View.VISIBLE);

        // GENERATE CSV BUTTON INITIALIZATION
        Button generateButton = new Button(getApplicationContext());
        generateButton.setText("Generate CSV Record for Accel. Readings");
        generateButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                File file = new File(getExternalFilesDir("Readings"), "accelReadings.csv");
                Log.w("File Path", file.getAbsolutePath());
                PrintWriter printWriter; // buffer
                try {
                    printWriter = new PrintWriter(file);
                    for (int i = 0; i < 100; i++) {
                        printWriter.println(String.format("%f, %f, %f", accelReadings[i][0], accelReadings[i][1], accelReadings[i][2]));
                    }
                    printWriter.close();
                    Log.i("Completion", "Successfully wrote file");
                } catch (FileNotFoundException e) {
                    Log.i("Completion", "Failed to write file");
                }
            }
        });
        lay.addView(generateButton);

        // SENSOR MANAGER INITIALIZATION
        SensorManager sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);

        // TEXTVIEW CREATION
        output = createLabel("UNDETERMINED");

        // ACCELEROMETER INITIALIZATION
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        // The graph is passed to the accelerometerEventListener so points can be added
        // output textview is also passed by reference so it can be updated in the eventListener
        SensorEventListener e2 = new accelerometerEventListener(output, graph, accelReadings);
        sensorManager.registerListener(e2, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);

    }
}
