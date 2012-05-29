package de.s87.eusage;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;

public class DatabaseHelper extends SQLiteOpenHelper {

	SQLiteDatabase myDB = null; 

	public static final String  DATABASE_FILE_PATH = Environment.getExternalStorageDirectory().toString();
	private static final String DATABASE_NAME="eusage.db";
	private static final int DATABASE_VERSION=1;

	public DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		
		 // Setup database table
		db.execSQL("CREATE TABLE IF NOT EXISTS usage"
                + " (_id integer primary key autoincrement," 
                +"type varchar(40)," 
                +"usage DOUBLE," 
                +"cdate TIMESTAMP,synced BOOLEAN) ");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		// Steps to upgrade the database for the new version ...
	}
	
	/*public SQLiteDatabase getReadableDatabase()
	{
	    database = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH
	            + File.separator + DATABASE_NAME, null,
	            SQLiteDatabase.OPEN_READONLY);
	    return database;
	}

	public SQLiteDatabase getWritableDatabase()
	{
	    database = SQLiteDatabase.openDatabase(DATABASE_FILE_PATH
	            + File.separator + DATABASE_NAME, null,
	            SQLiteDatabase.OPEN_READWRITE);
	    return database;
	}*/

}
