package edu.temple.gamemanageradmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.temple.gamemanager.WifiLocationTracker;
import edu.temple.gamemanager.interfaces.LocationUpdateListener;

/**
 * Basic activity to demonstrate the scanning and determining of
 * positions.  Includes buttons to explicitly start and stop
 * scanning.  Also demonstrates the use of the Location Update
 * Listener interface, which allows us to designate when the user
 * has entered or exited a restricted area.
 */
public class PositionActivity extends GMBaseActivity implements LocationUpdateListener {
    final WifiLocationTracker wifi = new WifiLocationTracker();

    /**
     * Creates the activity to be displayed
     * @param savedInstanceState the instance state to create
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        wifi.setLocationUpdateListener(this);
        wifi.initializeActivity(this, true);
        wifi.initializePositioning();

        Button startBtn = (Button) findViewById(R.id.start_btn);
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi.startPositionScanning();
            }
        });

        Button stopBtn = (Button) findViewById(R.id.stop_btn);
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                wifi.stopPositionScanning();
            }
        });
    }

    /**
     * Event handler for Location Update Listener class.  Allows us to
     * designate the activity's response to the Wifi Location Tracker
     * determining that we have entered a restricted area.
     *
     * In this case, it simply updates the "current position" TextView
     * object on the phone screen to inform the user of the area transition.
     */
    @Override
    public void onRestrictedAreaEntered() {
        TextView positionTV = (TextView) findViewById(R.id.current_position_tv);
        positionTV.setText("You have entered a restricted area!");
    }

    /**
     * Event handler for Location Update Listener class.  Allows us to
     * designate the activity's response to the Wifi Location Tracker
     * determining that we have exited a restricted area.
     *
     * In this case, it simply updates the "current position" TextView
     * object on the phone screen to inform the user of the area transition.
     */
    @Override
    public void onRestrictedAreaLeft() {
        TextView positionTV = (TextView) findViewById(R.id.current_position_tv);
        positionTV.setText("You are no longer in a restricted area.");
    }
}