package de.s87.eusage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class SyncActivity extends Activity {
    /** Called when the activity is first created. */

	TextView syncUrl;
	
	private static final String TAG = "SyncActivity";
	
	SQLiteDatabase database = null; 
	SharedPreferences prefs = null;
	String remoteSyncUrl;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // To get the httpupdateurl etc.
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        DatabaseHelper dbHelper = new DatabaseHelper(this);
		database = dbHelper.getReadableDatabase();

        setContentView(R.layout.sync);
        
        remoteSyncUrl=prefs.getString("httpupdateurl","");
        
        syncUrl = (TextView)findViewById(R.id.remoteUrl);
        syncUrl.setText(remoteSyncUrl);
        // Capture our button from layout
        Button syncButton = (Button)findViewById(R.id.syncbutton);
        syncButton.setOnClickListener(syncButtonListener);

        if( remoteSyncUrl.isEmpty() || remoteSyncUrl.equals("http://") )
        {
            Toast toast = Toast.makeText(
            		getApplicationContext(), 
            		getString(R.string.Please_define_remoteurl_in_prefs),
            		Toast.LENGTH_LONG);
        	toast.show();
        	syncButton.setEnabled(false);
            Intent settingsActivity = new Intent(getBaseContext(),
                    Preferences.class);
            startActivity(settingsActivity);
            return;
        }
    }
    
    public void onRestart()
    {
    	super.onRestart();
    	prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        if( !remoteSyncUrl.isEmpty() && !remoteSyncUrl.equals("http://") )
        {
        	Button syncButton = (Button)findViewById(R.id.syncbutton);
        	syncButton.setEnabled(true);
        }
    	return;
    }

    private OnClickListener syncButtonListener = new OnClickListener() {
        public void onClick(View v) {
        	
    		try
    		{
    			HTTPSyncronizer syncer = new HTTPSyncronizer( getApplicationContext(), database );
        		if( syncer.sync(remoteSyncUrl) )
        		{
        			Toast.makeText(getApplicationContext(), 
        					getString(R.string.Sync_done), Toast.LENGTH_LONG).show();
        		}
    		}
    		catch( HTTPSyncronizerException e )
    		{
    			Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_LONG).show();
    		}
        }
    };
    
}