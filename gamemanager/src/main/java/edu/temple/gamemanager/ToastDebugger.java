package edu.temple.gamemanager;

import android.app.Activity;
import android.widget.Toast;

public class ToastDebugger {
    protected Activity mCurrentActivity;

    /**
     * Sets the activity for the class so that the message
     * can be displayed
     * @param activity the context in which to display messages
     */
    public void setActivity(Activity activity)
    {
        mCurrentActivity = activity;
    }

    /**
     * Uses the activity to display a Toast message to the user
     * with the provided message
     * @param message the message to display
     */
    public void showMessage(final String message)
    {
        if (mCurrentActivity != null) {
            mCurrentActivity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(mCurrentActivity, message, Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}