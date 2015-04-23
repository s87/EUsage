package de.s87.eusage;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.preference.PreferenceManager;
import android.widget.Toast;

public class ItemModel {

	DatabaseHelper dbHelper;
	SQLiteDatabase database;
	Activity activity;
	
	public ItemModel( Activity activity )
	{
		this.activity = activity;
		this.dbHelper = new DatabaseHelper( activity );
		this.database = dbHelper.getReadableDatabase();
	}
	
	protected boolean autoSync()
	{
		Context activityContext = activity.getApplicationContext();
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(activityContext);
        Boolean autoSync = prefs.getBoolean("syncOnSave", false);
        if( autoSync )
        {
        	String remoteSyncUrl=prefs.getString("httpupdateurl","");
        	if( !remoteSyncUrl.isEmpty() &&
        			!remoteSyncUrl.equals("http://") )
        	{
        		try
        		{
        			HTTPSyncronizer syncer = new HTTPSyncronizer( activityContext, database );
        			System.out.println("remoteSyncUrl "+remoteSyncUrl);
            		if( syncer.sync(remoteSyncUrl) )
            		{
            			Toast.makeText(activityContext, "Sync done", Toast.LENGTH_LONG).show();
            			return true;
            		}
        		}
        		catch( HTTPSyncronizerException e )
        		{
        			Toast.makeText(activityContext, e.getMessage(), Toast.LENGTH_LONG).show();
        		}
        	}
        }

		return false;
	}
	
	public boolean purgeAll()
	{
        try
        {
        	database.execSQL("DELETE FROM usage WHERE 1");
        	return true;
        }
        catch( SQLException e )
        {
        	System.err.println("SQL Error: "+e.getMessage());
        }
		return false;		
	}
	
	
	public boolean addItem( String type, String usage, String timestamp )
	{
        try
        {
        	database.execSQL("INSERT INTO usage "
                    +"(type,usage,cdate,synced)" 
                    +"VALUES ('"+type+"',"
                    +usage.toString()+","
                    +timestamp+",0)");

        	this.autoSync();
        	return true;
        }
        catch( SQLException e )
        {
        	System.err.println("SQL Error: "+e.getMessage());
        }
		return false;
	}

}
