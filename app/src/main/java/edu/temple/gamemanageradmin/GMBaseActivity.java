package edu.temple.gamemanageradmin;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 *
 */
public class GMBaseActivity extends AppCompatActivity {
    protected static final String[] REQUEST_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    protected static final int REQUEST_INITIAL = 1337;

    /**
     *
     * @param savedInstanceState
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     *
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent myIntent = null;
        int id = item.getItemId();
        switch (id) {
            case R.id.map:
                myIntent = new Intent(this, MapActivity.class);
                break;
            case R.id.position:
                myIntent = new Intent(this, PositionActivity.class);
                break;
            case R.id.help:
                myIntent = new Intent(this, MainActivity.class);
                break;
        }

        if (myIntent != null) {
            startActivityForResult(myIntent, 0);
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }
}