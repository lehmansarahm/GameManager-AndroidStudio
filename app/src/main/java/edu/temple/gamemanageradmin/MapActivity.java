package edu.temple.gamemanageradmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import edu.temple.gamemanager.WifiLocationTracker;

public class MapActivity extends GMBaseActivity {
    final WifiLocationTracker wifi = new WifiLocationTracker();

    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        TextView compassTV = (TextView) findViewById(R.id.compassTv);
        wifi.initializeActivity(this);
        wifi.initializePositioning(compassTV);

        final EditText mapName = (EditText) findViewById(R.id.mapname_et);
        Button mapBtn = (Button) findViewById(R.id.map_btn);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                wifi.mapArea(mapName.getText().toString());
            }
        });
    }
}