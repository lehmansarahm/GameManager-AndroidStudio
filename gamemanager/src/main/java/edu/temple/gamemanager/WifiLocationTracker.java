package edu.temple.gamemanager;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
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
import edu.temple.gamemanager.interfaces.LocationUpdateListener;

/**
 * General purpose Wifi localization library applicable to all Android applications.  Leverages
 * David Hadizadeh's "Indoor Position Solution" library to map areas of interest and scan for
 * matches of current signal strengths against logged fingerprints:
 *      http://hadizadeh.de/indoor-positioning/
 *
 * Compares results against known "restricted areas" to raise events for subscribing applications to
 * manage when a user has entered or exited certain areas of a building.
 */
public class WifiLocationTracker {
    protected static final String[] REQUEST_PERMISSIONS = new String[] {
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.ACCESS_COARSE_LOCATION,
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


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIMARY SETTERS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * Assigns the location listener for this class, so that a subscribing application may be
     * informed when the user has entered or exited a restricted area.
     * @param listener the listener instance to leverage in order to raise the necessary area
     *                 entry / exit events
     */
    public void setLocationUpdateListener(LocationUpdateListener listener) {
        this.locUpdateListener = listener;
    }

    /**
     * Simple setter method which assigns the context for this class, so that other methods such as
     * "showShortToast" and "showLongToast" can be used for debugging purposes.
     * @param activity the context in which the class shall operate
     */
    public void setActivity(Activity activity) {
        currentActivity = activity;
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIMARY INITIALIZERS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * Overloaded constructor for the primary "initializeActivity" method.  Allows an external
     * environment (such as Unity) to subscribe to the primary method without requiring Android to
     * perform the explicit permissions request.
     * @param activity the context in which the class shall operate
     */
    public void initializeActivity(Activity activity) {
        initializeActivity(activity, false);
    }

    /**
     * Sets and initializes the current activity in which the localization logic will operate.
     * Checks to see whether the appropriate permissions have been granted to the application, and
     * sets up the application configuration file if it can.
     * @param activity the context in which the class shall operate
     */
    public void initializeActivity(Activity activity, boolean forcePermissionRequest)
    {
    	currentActivity = activity;
        if (forcePermissionRequest) {
            ActivityCompat.requestPermissions(currentActivity, REQUEST_PERMISSIONS, REQUEST_INITIAL);
        }

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
     * Overloaded constructor for the primary "initializePositioning" method.  Allows an external
     * environment (such as Unity) to set up the positioning components without designating a
     * specific TextView object to use in displaying a compass heading.
     */
    public void initializePositioning() {
        initializePositioning(null);
    }

    /**
     * Sets up and initializes the Position Manager object for the class.  Utilizes the
     * "permissionsGranted" flag and config file created in "initializeActivity" to set up Wifi
     * Technology and Compass Technology objects to use in scanning for user positions.  Also
     * establishes the necessary event listeners for when new positions are received by the manager.
     * @param compassTV a TextView object to use in displaying the current compass heading
     */
    public void initializePositioning(TextView compassTV) {
        if (permissionsGranted) {
            try {
                positionManager = new PositionManager(configFile);
            } catch (PositioningPersistenceException ex) {
                showLongToast("Could not instantiate Position Manager: " + ex.getMessage());
            }

            try {
                WifiTechnology wifiTechnology = new WifiTechnology(currentActivity, "wifi");
                CompassTechnology compassTechnology = new CompassTechnology(currentActivity,
                        "compass", 80, compassTV);

                positionManager.addTechnology(wifiTechnology);
                positionManager.addTechnology(compassTechnology);
                positionManager.registerPositionListener(new PositionListener() {
                    @Override
                    public void positionReceived(final PositionInformation positionInformation) {
                        currentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                comparePosition(positionInformation);
                            }
                        });
                    }
                    @Override
                    public void positionReceived(final List<PositionInformation> list) {
                        currentActivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                comparePosition(list.get(0));
                            }
                        });
                    }
                });
            } catch (PositioningException ex) {
                showLongToast("Could not add wifi or compass to Position Manager: "
                        + ex.getMessage());
            }
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      POSITION MANAGER SCAN METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * General purpose wrapper for the Position Manager "map" command.  Allows an external
     * environment (such as Unity) to ask for an area to be mapped without having to interact with
     * the Position Manager directly.  This way, we can change the underlying logic without having
     * to change Unity.
     * @param areaName the name of the area to map
     */
    public void mapArea(String areaName) {
        positionManager.map(areaName);
        showShortToast("Position mapping complete!");
    }

    /**
     * General purpose wrapper for the Position Manager "startPositioning" command.  Allows an
     * external environment (such as Unity) to ask the class to start scanning for user positions
     * without having to interact with the Position Manager directly.  This way, we can change the
     * underlying logic without having to change Unity.
     */
    public void startPositionScanning() {
        positionManager.startPositioning(100);
        showShortToast("Now scanning for positions.");
    }

    /**
     * General purpose wrapper for the Position Manager "stopPositioning" command.  Allows an
     * external environment (such as Unity) to ask the class to stop scanning for user positions
     * without having to interact with the Position Manager directly.  This way, we can change the
     * underlying logic without having to change Unity.
     */
    public void stopPositionScanning() {
        positionManager.stopPositioning();
        showShortToast("No longer scanning for positions.");
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PUBLIC UTILITY METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * General purpose method to display a Toast message on top of the current activity for a short
     * period of time.  Used primarily for debugging.
     * @param message the message to display
     */
    public void showShortToast(final String message) {
        if (currentActivity != null) {
            currentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(currentActivity, message, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    /**
     * General purpose method to display a Toast message on top of the current activity for a longer
     * period of time.  Used primarily for debugging.
     * @param message the message to display
     */
    public void showLongToast(final String message) {
        if (currentActivity != null) {
            currentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(currentActivity, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }


    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------
    //      PRIVATE REFERENCE METHODS
    // ---------------------------------------------------------------------------------------------
    // ---------------------------------------------------------------------------------------------


    /**
     * Private reference method to set up the configuration file needed for the class.  Checks the
     * desired folder name within external storage and creates it if it doesn't exist.
     *
     * --- DOES NOT --- create a new config file if it does not exist.  Position Manager takes
     * care of that for us.  If you try to create the file *FOR* Position Manager, it ruins the
     * expected file format.  Better to let Position Manager take care of it on its own.
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
     * Private reference method to determine whether the localized position information returned
     * from Position Manager is a restricted area.  If so, the assigned listener's restricted area
     * entry event is raised.  Otherwise, the listener's restricted area exit event is raised.
     * @param positionInformation details about the user's current position to be categorized
     */
    private void comparePosition(PositionInformation positionInformation) {
        if (positionInformation.getName().equalsIgnoreCase("Desk")) {
            this.locUpdateListener.onRestrictedAreaEntered();
        } else {
            this.locUpdateListener.onRestrictedAreaLeft();
        }
    }
}