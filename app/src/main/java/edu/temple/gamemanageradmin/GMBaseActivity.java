package edu.temple.gamemanageradmin;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Base, view-less activity which provides common menu functionality
 * for all application views.
 */
public class GMBaseActivity extends AppCompatActivity {
    /**
     * Creates the activity to be displayed
     * @param savedInstanceState the instance state to create
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Creates an options menu to be displayed
     * @param menu the menu to display
     * @return whether the menu was created successfully
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    /**
     * Responds to the selection of a menu option item.
     * @param item the item selected
     * @return whether the item was properly handled
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