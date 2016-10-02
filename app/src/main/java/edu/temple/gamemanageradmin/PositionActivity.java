package edu.temple.gamemanageradmin;

import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.Button;

import edu.temple.gamemanager.LocationUpdateListener;
import edu.temple.gamemanager.WifiLocationTracker;

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

        ActivityCompat.requestPermissions(this, REQUEST_PERMISSIONS, REQUEST_INITIAL);
        wifi.setLocationUpdateListener(this);
        wifi.setActivity(this);
        // wifi.initializePositioning();

        Button startBtn = (Button) findViewById(R.id.start_btn);
        Button stopBtn = (Button) findViewById(R.id.stop_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // positionManager.startPositioning(100);
                wifi.showShortToast("Now scanning for positions.");
            }
        });
        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // positionManager.stopPositioning();
                wifi.showShortToast("No longer scanning for positions.");
            }
        });
    }

    /**
     *
     */
    @Override
    public void onRestrictedAreaEntered() {
        // do something
    }

    /**
     *
     */
    @Override
    public void onRestrictedAreaLeft() {
        // do something
    }
}