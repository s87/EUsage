package de.s87.eusage;

import android.R.integer;
import android.app.Activity;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ItemModel {

	DatabaseHelper dbHelper;
	SQLiteDatabase database;

	public ItemModel( Activity activity )
	{
		this.dbHelper = new DatabaseHelper( activity );
		this.database = dbHelper.getReadableDatabase();
	}
	
	protected boolean autoSync()
	{
		// @todo implement this
		return false;
	}
	
	public boolean purgeAll()
	{
        try
        {
        	System.err.println("REMOVING ALL ITEMS");
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
