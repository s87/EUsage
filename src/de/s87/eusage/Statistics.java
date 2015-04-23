package de.s87.eusage;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class Statistics {

	protected Activity activity;
	protected DatabaseHelper dbHelper;
	protected Boolean statsLoaded = false;
	protected Map<String, UsageItem> typeList;
	
	public Statistics( Activity activity )
	{
		this.activity = activity;
		dbHelper = new DatabaseHelper(this.activity);
	}
	
	public UsageItem getUsageForType( String type )
	{
		this.refresh();
		if( typeList.containsKey(type) )
			return typeList.get(type);
		return null;
	}
	
	public String[] getUsageTypes()
	{
		String[] arr1 = new String[typeList.size()];
		Object[] keys = typeList.keySet().toArray();
        for(int i=0;i<keys.length;i++) {
        	arr1[i] = (String)keys[i];
        }
        return arr1;
	}

	public void refresh()
	{
		if( this.statsLoaded )
			return;

		SQLiteDatabase database = dbHelper.getReadableDatabase();
		String selectQuery = "SELECT type,usage,cdate FROM usage ORDER BY cdate desc LIMIT 50";
		Cursor cursor = database.rawQuery(selectQuery, null);
		
		typeList = new HashMap<String, UsageItem>();
		// looping through all rows and adding to list
	    if (cursor.moveToFirst()) {
	        do {
	        	String type = cursor.getString(0);
	        	if( typeList.containsKey(type) == false )
	        	{
	        		typeList.put(type, new UsageItem(type));
	        	}

	        	UsageItem item = typeList.get(type);
	        	if( item.isComplete() != true )
	        	{
		        	String usage = cursor.getString(1);
		        	String cdate = cursor.getString(2);
	        		item.addValue(cdate, usage);
	        	}
	        	
	        	if( item.isComplete() )
	        	{
	        		Double itemUsage = item.getUsageForDay();
	        		continue;
	        	}
	        } while (cursor.moveToNext());
	    }
	    this.statsLoaded = true;
	}
	
	class UsageItem
	{
		protected String type;
		protected Long lowDate;
		protected Double lowDateUsage;
		protected Long lastDate;
		protected Double lastDateUsage;

		protected Integer valueCount = 0;
		
		public UsageItem( String type )
		{
			this.type = type;
		}
		
		public String getType()
		{
			return type;
		}

		public void addValue( String cdate, String usage )
		{
			Long date = Long.parseLong(cdate);
			if( lowDate != null && date < lowDate )
			{				
				this.lastDate = lowDate;
				this.lastDateUsage = lowDateUsage;
				this.lowDate = Long.parseLong(cdate);
				this.lowDateUsage = Double.parseDouble(usage);
			}
			else
			{
				this.lowDate = Long.parseLong(cdate);
				this.lowDateUsage = Double.parseDouble(usage);
			}
			valueCount++;
		}
		
		public Boolean isComplete()
		{
			return( valueCount >= 2 );
		}
		
		@Override
		public String toString()
		{
			return type+": "+lowDate+" "+lowDateUsage+" / "+lastDate+" "+lastDateUsage;
		}
		
		public Double getUsageForDay()
		{
			Double valInPeriod;
			valInPeriod = (lastDateUsage-lowDateUsage);
			if( this.isComplete() == false || valInPeriod < 0 )
				return 0.0;

			Timestamp tsLastdate = new Timestamp(lastDate);
			Timestamp tsLowdate = new Timestamp(lowDate);
			Calendar cal = Calendar.getInstance();
			cal.setTimeInMillis(lowDate);

			Long days = daysDiff(lowDate,lastDate);
			Double dailyUsage = 0.0;
			if( valInPeriod > 0 )
				dailyUsage = valInPeriod/days;
			
			return dailyUsage;
		}
	
		public long daysDiff(long from, long to) {
		    return Math.round( (to - from) / 86400000D ); // 1000 * 60 * 60 * 24
		}
		
	}
	
}
