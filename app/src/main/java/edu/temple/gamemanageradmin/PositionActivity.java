package edu.temple.gamemanageradmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import edu.temple.gamemanager.WifiLocationTracker;
import edu.temple.gamemanager.interfaces.LocationUpdateListener;

public class PositionActivity extends GMBaseActivity implements LocationUpdateListener {
    final WifiLocationTracker wifi = new WifiLocationTracker();

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_position);

        wifi.setLocationUpdateListener(this);
        wifi.initializeActivity(this);
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
     *
     */
    @Override
    public void onRestrictedAreaEntered() {
        // wifi.showLongToast("You have entered a restricted area!");
        TextView positionTV = (TextView) findViewById(R.id.current_position_tv);
        positionTV.setText("You have entered a restricted area!");
    }

    /**
     *
     */
    @Override
    public void onRestrictedAreaLeft() {
        // wifi.showLongToast("You are no longer in a restricted area.");
        TextView positionTV = (TextView) findViewById(R.id.current_position_tv);
        positionTV.setText("You are no longer in a restricted area.");
    }
}