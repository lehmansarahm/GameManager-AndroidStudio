package edu.temple.gamemanager;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import de.hadizadeh.positioning.controller.PositionListener;
import de.hadizadeh.positioning.controller.PositionManager;
import de.hadizadeh.positioning.controller.Technology;
import de.hadizadeh.positioning.exceptions.PositioningException;
import de.hadizadeh.positioning.exceptions.PositioningPersistenceException;
import de.hadizadeh.positioning.model.PositionInformation;
import edu.temple.gamemanager.indoorpositioning.CompassTechnology;
import edu.temple.gamemanager.indoorpositioning.WifiTechnology;

/**
 *
 */
public class WifiLocationTracker implements PositionListener {
    private String FOLDER_NAME = "GameManager";
    private String FILE_NAME = "gm_wifi_config.txt";

    protected PositionManager positionManager;
	protected LocationUpdateListener locUpdateListener;
    protected Activity mCurrentActivity;	
	private WifiConfigUtility wifiConfig;
	
	private Timer timer;
	private boolean firstScan = true;

    public void setActivityOnly(Activity activity) {
        mCurrentActivity = activity;
    }

	/**
	 * 
	 * @param activity
	 */
    public void setActivity(Activity activity)
    {
    	mCurrentActivity = activity;
    	int wifiPermissionStatus = 
    			mCurrentActivity.checkCallingOrSelfPermission(Manifest.permission.CHANGE_WIFI_STATE);
    	int extStoragePermissionStatus = 
    			mCurrentActivity.checkCallingOrSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
    	
    	try {
        	if (wifiPermissionStatus == PackageManager.PERMISSION_GRANTED 
        			&& extStoragePermissionStatus == PackageManager.PERMISSION_GRANTED) {
        		wifiConfig = new WifiConfigUtility();
        		startWifiScanner();
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
        try {
            File configFile = new File(Environment.getExternalStorageDirectory(), FILE_NAME);
            positionManager = new PositionManager(configFile);
            Log.d("positionManager", "initialized");
        } catch (PositioningPersistenceException ex) {
            showLongToast("Could not instantiate Position Manager: " + ex.getMessage());
        }

        try {
            Technology wifiTechnology = new WifiTechnology(mCurrentActivity, "WIFI");
            CompassTechnology compassTechnology = new CompassTechnology(mCurrentActivity, "compass", 80, compassTV);

            positionManager.addTechnology(wifiTechnology);
            positionManager.addTechnology(compassTechnology);
            positionManager.registerPositionListener(this);
        } catch (PositioningException ex) {
            showLongToast("Could not add wifi or compass to Position Manager: " + ex.getMessage());
        }
    }

    /**
     *
     * @param message
     */
    public void showShortToast(String message) {
        Toast.makeText(mCurrentActivity, message, Toast.LENGTH_SHORT).show();
    }

    /**
     *
     * @param message
     */
    public void showLongToast(String message) {
        Toast.makeText(mCurrentActivity, message, Toast.LENGTH_LONG).show();
    }
    
    /**
     * 
     */
    private void startWifiScanner() {
        showShortToast("Attempting to start wifi scanner!");
		final WifiManager wifi = (WifiManager) mCurrentActivity.getSystemService(Context.WIFI_SERVICE);
        mCurrentActivity.registerReceiver(new BroadcastReceiver() {
        	/**
        	 * 
        	 * @param c
        	 * @param intent
        	 */
        	public void onReceive(Context c, Intent intent) {
            try {
                //if (firstScan) {
                    wifiConfig.addScanResultsToRestrictedArea("lab333", wifi.getScanResults());
                    wifiConfig.finalizeConfig();
                    firstScan = false;
                    showShortToast("Scans logged.  Ready to play!");
                //} else {
                //    String area = wifiConfig.compareScanResultsToRestrictedAreas(wifi.getScanResults());
                //    handleAreaUpdate(area);
                //}
            } catch (Exception ex) {
                showLongToast(ex.getMessage());
            }
        	}
        }, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));        
        
        timer = new Timer();
		timer.schedule(new WifiScanner(wifi), 0, 5000);
        showShortToast("Wifi scanner started!");
    }
    
    /**
     * 
     */
    private void handleAreaUpdate(String areaComparisonResult) {
    	boolean listenerAvailable = !(this.locUpdateListener == null);
		if (areaComparisonResult.equals(WifiConfigUtility.NO_RESULTS_FOUND)) {
			if (listenerAvailable) {
				locUpdateListener.onRestrictedAreaLeft();
			}
            showShortToast("Congrats, you're in an approved area!");
		} else {
			if (listenerAvailable) {
				locUpdateListener.onRestrictedAreaEntered();
			}
            showShortToast("Oh no, you're in a restricted area!");
		}
    }

    @Override
    public void positionReceived(final PositionInformation positionInformation) {
        // Evaluate for restricted area entry / departure
        // Raise the corresponding events as necessary
    }

    @Override
    public void positionReceived(final List<PositionInformation> positionInformation) {
        // Evaluate for restricted area entry / departure
        // Raise the corresponding events as necessary

        /*currentPositionTv.post(new Runnable() {
            public void run() {
                String positioningText = "";
                for (int i = 0; i < positionInformation.size(); i++) {
                    positioningText += i + ".: " + positionInformation.get(i).getName() + System.getProperty("line.separator");
                }
                currentPositionTv.setText(positioningText);
            }
        });*/
    }

    /**
     * 
     */
	private class WifiScanner extends TimerTask {
		private WifiManager wifiManager;
		
		/**
		 * 
		 */
		public WifiScanner(WifiManager manager) {
			this.wifiManager = manager;
		}
		
		/**
		 * 
		 */
		public void run() {
			wifiManager.startScan();
		}
	}
}