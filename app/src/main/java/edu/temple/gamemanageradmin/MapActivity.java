package edu.temple.gamemanageradmin;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import edu.temple.gamemanager.WifiLocationTracker;

public class MapActivity extends GMBaseActivity {
    /**
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        final EditText mapName = (EditText) findViewById(R.id.mapname_et);
        Button mapBtn = (Button) findViewById(R.id.map_btn);
        TextView compassTV = (TextView) findViewById(R.id.compassTv);

        final WifiLocationTracker wifi = new WifiLocationTracker();
        // wifi.initializePositioning(compassTV);
        mapBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //positionManager.map(mapName.getText().toString());
                wifi.showShortToast("Position mapping complete!");
            }
        });
    }
}