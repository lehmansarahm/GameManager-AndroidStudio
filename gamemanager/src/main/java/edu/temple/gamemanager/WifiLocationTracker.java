package edu.temple.gamemanager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;
import edu.temple.gamemanager.indoorpositioning.CompassTechnology;
import edu.temple.gamemanager.indoorpositioning.WifiTechnology;

/**
 *
 */
public class WifiLocationTracker implements PositionListener {
    protected static final String[] REQUEST_PERMISSIONS = new String[] {
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.CHANGE_WIFI_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.INTERNET
    };
    protected static final int REQUEST_INITIAL = 1337;

    private String FOLDER_NAME = "GameManager";
    private String FILE_NAME = "gm_wifi_config.txt";

    protected PositionManager positionManager;
	protected LocationUpdateListener locUpdateListener;

    private boolean permissionsGranted;
    protected Activity currentActivity;
    private File configFile;

	/**
	 * 
	 * @param activity
	 */
    public void initializeActivity(Activity activity)
    {
    	currentActivity = activity;
        ActivityCompat.requestPermissions(currentActivity, REQUEST_PERMISSIONS, REQUEST_INITIAL);

    	int wifiPermissionStatus = 
    			currentActivity.checkCallingOrSelfPermission(Manifest.permission.CHANGE_WIFI_STATE);
    	int extStoragePermissionStatus = 
    			currentActivity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    	
    	try {
        	if (wifiPermissionStatus == PackageManager.PERMISSION_GRANTED 
        			&& extStoragePermissionStatus == PackageManager.PERMISSION_GRANTED) {
                permissionsGranted = true;
                configFile = initializeConfig();
            }
        	else {
                showLongToast("Insufficient wifi or external storage access.  "
            		+ "Is the AR manifest properly formatted?");
        	}
    	} catch (Exception ex) {
			showLongToast(ex.getMessage());
		}
    }
    
	/**
	 *
	 */
	public void setLocationUpdateListener(LocationUpdateListener listener) {
		this.locUpdateListener = listener;
	}

    /**
     *
     */
    public void initializePositioning() {
        initializePositioning(null);
    }

    /**
     *
     * @param compassTV
     */
    public void initializePositioning(TextView compassTV) {
        if (permissionsGranted) {
            try {
                positionManager = new PositionManager(configFile);
                Log.d("positionManager", "initialized");
            } catch (PositioningPersistenceException ex) {
                showLongToast("Could not instantiate Position Manager: " + ex.getMessage());
            }

            try {
                WifiTechnology wifiTechnology = new WifiTechnology(currentActivity, "wifi");
                CompassTechnology compassTechnology = new CompassTechnology(currentActivity, "compass", 80, compassTV);

                positionManager.addTechnology(wifiTechnology);
                positionManager.addTechnology(compassTechnology);
                positionManager.registerPositionListener(this);
            } catch (PositioningException ex) {
                showLongToast("Could not add wifi or compass to Position Manager: " + ex.getMessage());
            }
        }
    }

    public void mapArea(String areaName) {
        positionManager.map(areaName);
        showShortToast("Position mapping complete!");
    }

    /**
     *
     */
    public void startPositionScanning() {
        positionManager.startPositioning(100);
        showShortToast("Now scanning for positions.");
    }

    /**
     *
     */
    public void stopPositionScanning() {
        positionManager.stopPositioning();
        showShortToast("No longer scanning for positions.");
    }

    /**
     *
     * @param message
     */
    public void showShortToast(String message) {
        Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param message
     */
    public void showLongToast(String message) {
        Toast.makeText(currentActivity, message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void positionReceived(final PositionInformation positionInformation) {
        comparePosition(positionInformation);
    }

    @Override
    public void positionReceived(final List<PositionInformation> positionInformation) {
        comparePosition(positionInformation.get(0));
    }

    /**
     *
     */
    private File initializeConfig() throws IOException {
        File folder = new File(Environment.getExternalStorageDirectory(), FOLDER_NAME);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File configFile = new File(folder, FILE_NAME);
        return configFile;
    }

    /**
     *
     * @param positionInformation
     */
    private void comparePosition(PositionInformation positionInformation) {
        if (positionInformation.getName().equals("Desk")) {
            this.locUpdateListener.onRestrictedAreaEntered();
        } else {
            this.locUpdateListener.onRestrictedAreaLeft();
        }
    }
}