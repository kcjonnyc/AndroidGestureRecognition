package lab2.uwaterloo.ca.lab2;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.TextView;

import ca.uwaterloo.sensortoy.LineGraphView;

/**
 * Created by KCJon on 2017-01-19.
 */

public class accelerometerEventListener implements SensorEventListener {

    // GESTURE RECOGNITION VARIABLES
    private final float FILTER_CONST = 6.0f;                 // constant factor for our low pass filter

    private enum myState{WAIT, RISE_A, RISE_B, RISE_C, RISE_D, FALL_A, FALL_B, FALL_C, FALL_D, DETERMINED} // FSM states
    // A = MOVE LEFT, B = MOVE RIGHT, C = TILT BACKWARDS, D = TILT FORWARDS, WAIT = WAITING TO DETECT A GESTURE, DETERMINED = ANALYSIS FINISHED
    private myState state = myState.WAIT;                   // initial state

    private enum mySig{SIG_A, SIG_B, SIG_X, SIG_C, SIG_D}   // signature states
    private mySig signature = mySig.SIG_X;                  // initial signature state

    // threshold constants
    private final float[] THRES_A = {2.0f, 5.0f, -0.4f};    // threshold values for moving left
    private final float[] THRES_B = {-2.0f, -5.0f, 0.4f};   // threshold values for moving right
    private final float[] THRES_C = {2.0f, 5.0f, -0.4f};    // threshold values for tilt backwards
    private final float[] THRES_D = {-2.0f, -5.0f, 0.4f};   // threshold values for tilt forwards

    private int sampleCounter = 30;                         // variable to count number of samples
    private final int SAMPLEDEFAULT = 30;                   // default number of samples

    // VARIABLES
    private TextView outputDir;                             // textview for output direction
    private LineGraphView graph;                            // graph object
    private float[][] aReadings;                            // 100 x 3 array to store filtered x, y and z values
    private float[] filteredReadings = {0, 0, 0};           // array of 3 elements to store the current filtered values

    // CONSTRUCTOR
    public accelerometerEventListener(TextView output, LineGraphView graphRef, float[][] readings) {
        outputDir = output;
        graph = graphRef;
        aReadings = readings;
    }

    // FSM FOR LEFT AND RIGHT MOTION
    private void callFSM(){

        float deltaA = aReadings[99][0] - aReadings[98][0];                 // calculate the difference in acceleration in the x plane
        float deltaZAxis = aReadings[99][2] - aReadings[98][2];             // calculate the difference in acceleration in the z plane
        Log.d("LEFT/RIGHT Readings: ", Float.toString(aReadings[99][0]));   // allows us to see current readings in the x plane
        Log.d("UP/DOWN Readings: ", Float.toString(aReadings[99][2]));      // allows us to see current readings in the z plane

        // SWITCH STATEMENTS TO DETERMINE WHICH GESTURE WAS STARTED
        switch (state) {

            case WAIT:

                Log.d("FSM: ", "State WAIT");       // for debug purposes

                sampleCounter = SAMPLEDEFAULT;      // in wait, we do not want to decrement the counter
                // the counter should only decrement after a gesture reading begins
                signature = mySig.SIG_X;            // signature will be the "undetermined" signature

                if (deltaA > THRES_A[0]) {          // slope is positive, we have started the right gesture (A)
                    state = myState.RISE_A;
                }
                else if (deltaA < THRES_B[0]) {     // slope is negative, we have started the left gesture (B)
                    state = myState.FALL_B;
                }
                if (deltaZAxis > THRES_C[0]) { // slope is positive, we have started the backwards gesture (C)
                    state = myState.RISE_C;
                }
                else if (deltaZAxis < THRES_D[0]) { // slope is negative, we have started the forwards gesture (D)
                    state = myState.FALL_D;
                }
                break;

            case RISE_A:    // RIGHT MOTION PART 1

                Log.d("FSM: ", "State RISE_A");     // for debug purposes

                if (deltaA <= 0) {  // we want to stay in this state until we are done rising and start to fall
                    if (aReadings[99][0] >= THRES_A[1]) {  // check if the peak was a large enough value
                        state = myState.FALL_A;            // if yes, proceed to look at the falling motion of the wave
                    }
                    else {
                        state = myState.DETERMINED;        // if no, it is not a correct right gesture
                    }
                }
                break;

            case RISE_B:    // LEFT MOTION PART 2

                Log.d("FSM: ", "State RISE_B");

                if (deltaA <= 0) {      // crossed over from rising to falling (rising to falling to rebound)
                    if (aReadings[99][0] >= THRES_B[2]) {   // check if the rebound value was a low enough value
                        signature = mySig.SIG_B;            // if yes, we have determined it is a left gesture
                    }
                    state = myState.DETERMINED;             // if no, not a correct left gesture
                }
                break;

            case RISE_C:    // TILT BACKWARDS MOTION PART 1

                Log.d("FSM: ", "State RISE_C");

                if (deltaZAxis <= 0) {  // we want to stay in this state until we are done rising and start to fall
                    if (aReadings[99][2] >= THRES_C[1]) {  // check if the peak was a large enough value
                        state = myState.FALL_C;            // if yes, proceed to look at the falling motion of the wave
                    }
                    else {
                        state = myState.DETERMINED;        // if no, it is not a correct forwards gesture
                    }
                }
                break;

            case RISE_D:    // TILT FORWARDS MOTION PART 2

                Log.d("FSM: ", "State RISE_D");

                if (deltaZAxis <= 0) {      // crossed over from rising to falling (rising to falling to rebound)
                    if (aReadings[99][2] >= THRES_D[2]) {   // check if the rebound value was a low enough value
                        signature = mySig.SIG_D;            // if yes, we have determined it is a forwards gesture
                    }
                    state = myState.DETERMINED;             // if no, not a correct backwards gesture
                }
                break;

            case FALL_A:    // RIGHT MOTION PART 2

                Log.d("FSM: ", "State FALL_A");

                if (deltaA >= 0) {      // crossed over from falling to rising (rising to falling to rebound)
                    if (aReadings[99][0] <= THRES_A[2]) {   // we check if the rebound value is low enough
                        signature = mySig.SIG_A;            // if yes, we have determined it is a right gesture
                    }
                    state = myState.DETERMINED;             // if no, not a correct right gesture
                }
                break;

            case FALL_B:    // LEFT MOTION PART 1

                Log.d("FSM: ", "State FALL_B");

                if (deltaA >= 0) { // we want to stay in this state until we are done falling and start to rise
                    if (aReadings[99][0] <= THRES_B[1]) {   // check if the peak was a low enough value
                        state = myState.RISE_B;             // if yes, we now look at the rising motion of the wave
                    }
                    else {
                        state = myState.DETERMINED;         // if no, this is not a correct left gesture
                    }
                }
                break;

            case FALL_C:    // TILT BACKWARDS MOTION PART 2

                Log.d("FSM: ", "State FALL_C");

                if (deltaZAxis >= 0) {      // crossed over from falling to rising (rising to falling to rebound)
                    if (aReadings[99][2] <= THRES_C[2]) {   // we check if the rebound value is low enough
                        signature = mySig.SIG_C;            // if yes, we have determined it is a backwards gesture
                    }
                    state = myState.DETERMINED;             // if no, not a correct backwards gesture
                }
                break;

            case FALL_D:    // TILT FORWARDS MOTION PART 1

                Log.d("FSM: ", "State FALL_D");

                if (deltaZAxis >= 0) { // we want to stay in this state until we are done falling and start to rise
                    if (aReadings[99][2] <= THRES_D[1]) {   // check if the peak was a low enough value
                        state = myState.RISE_D;             // if yes, we now look at the rising motion of the wave
                    }
                    else {
                        state = myState.DETERMINED;         // if no, this is not a correct forwards gesture
                    }
                }
                break;

            case DETERMINED:

                Log.d("FSM: ", "State DETERMINED " + signature.toString()); // signature has been determined
                break;

            default:

                state = myState.WAIT; // defaults to wait state
                break;

        }

        sampleCounter--;

    }

    public void onAccuracyChanged(Sensor s, int i) {

    }

    public void onSensorChanged(SensorEvent se) {
        if (se.sensor.getType() == Sensor.TYPE_LINEAR_ACCELERATION) {

            // APPLY LOW PASS FILTER
            filteredReadings[0] += (se.values[0] - filteredReadings[0]) / FILTER_CONST;
            filteredReadings[1] += (se.values[1] - filteredReadings[1]) / FILTER_CONST;
            filteredReadings[2] += (se.values[2] - filteredReadings[2]) / FILTER_CONST;
            // ADD FILTERED DATA POINT TO THE GRAPH
            graph.addPoint (filteredReadings);

            // SHIFT ARRAY ELEMENTS TO ELIMINATE THE OLDEST READING, AND LEAVE SPACE FOR THE NEWEST READING TO BE ADDED TO THE END OF THE ARRAY
            for(int i = 1; i < 100; i++){
                aReadings[i - 1][0] = aReadings[i][0];
                aReadings[i - 1][1] = aReadings[i][1];
                aReadings[i - 1][2] = aReadings[i][2];
            }
            // COPY OVER NEW ELEMENTS REPLACING KICKING OUT THE OLDEST READING
            // we will always have the most recent 100 values stored
            for (int j = 0; j < 3; j++) {
                aReadings[99][j] = filteredReadings[j];
            }

            // START FSM ANALYSIS
            callFSM();

            if (sampleCounter <= 0){    // 30 points were analysed after start of gesture detected
                if (state == myState.DETERMINED){      // we have finished analysis of a gesture
                    if (signature == mySig.SIG_B)
                        outputDir.setText("LEFT");
                    else if (signature == mySig.SIG_A)
                        outputDir.setText("RIGHT");
                    else if (signature == mySig.SIG_D)
                        outputDir.setText("BACKWARDS");
                    else if (signature == mySig.SIG_C)
                        outputDir.setText("FORWARDS");
                    else
                        outputDir.setText("Undetermined");
                }
                else {    // if the gesture has not been determined after 30 points, we reset it back to wait
                    state = myState.WAIT;
                    outputDir.setText("Undetermined");
                }
                sampleCounter = SAMPLEDEFAULT;    // reset counter and go back to wait state for another gesture
                state = myState.WAIT;
            }
        }
    }
}