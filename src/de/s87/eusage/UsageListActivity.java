package de.s87.eusage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.BaseColumns;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

public class UsageListActivity extends ListActivity {

	private SQLiteDatabase database;
	private UsageListAdapter dataSource;
	protected DatabaseHelper dbHelper;
	private static final String TAG = "UsageListActivity";
	private static final String fields[] = { "type", "usage", "cdate", 
        BaseColumns._ID };
	
	// usage_val, usage_checkbox
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();
        try
        {
            Cursor data = database.query("usage", fields, 
                    null, null, null, null, "cdate DESC");
        	startManagingCursor(data);
        	if( data.getCount() > 0 )
        	{
        		dataSource = new UsageListAdapter(this, 
                    R.layout.usagelist, data, fields,
                    new int[] { R.id.usage_type, R.id.usage_val, R.id.usage_cdate, R.id.rowCheckBox });
        		setListAdapter(dataSource);
        	}
        }
        catch( Exception e )
        {
        	System.err.println(e.getMessage());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.usagelist, menu);
        return true;
    }

    protected boolean selectAll()
    {
    	Cursor c = dataSource.getCursor();
    	for( int i=0; i < dataSource.getCount(); i++ )
    	{
    		c.moveToPosition(i);
    		dataSource.setChecked(c.getInt(3), true);
    	}
    	return true;
    }
   
    protected void callCsvImport()
    {
    	AlertDialog.Builder dBuilder = new AlertDialog.Builder(this);
    	dBuilder.setMessage(getString(R.string.CsvImportWarningMessage)).
    			setCancelable(true).
    			setPositiveButton(getString(R.string.Yes), new DialogInterface.OnClickListener() {
    		           public void onClick(DialogInterface dialog, int id) {
    		                System.out.println("Import now");
    		                ImportCSVTask importer = new ImportCSVTask(UsageListActivity.this);
    		                importer.execute("");
    		           } } );
    	AlertDialog alert = dBuilder.create();
    	alert.show();
    }
    
    protected boolean fillDummies()
    {    	
        ItemModel model = new ItemModel(UsageListActivity.this);
        
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime();
    	Timestamp ts = new Timestamp(now.getTime());

    	String theTimestamp = Long.toString(ts.getTime());

        for( int x=0; x<25; x++)
        {
        	String usage = ""+(x*500);
            if( model.addItem("dummy", usage, theTimestamp) )
            {
            	System.out.println("Added");
            }
        }

    	dataSource.getCursor().requery();
    	dataSource.notifyDataSetChanged();
    	dataSource.notifyDataSetInvalidated();
    	getListView().invalidate();
    	return true;
    }
    
    protected boolean deleteSelected()
    {
    	SparseBooleanArray cbStates = dataSource.getCheckboxStates();
    	database = dbHelper.getWritableDatabase();

    	int deleteCount = 0;
    	for( int i=0; i<cbStates.size(); i++ )
    	{
    		if( cbStates.get(cbStates.keyAt(i)) )
    		{
    			String id = String.valueOf(cbStates.keyAt(i));
    			try
    			{
        	    	int affectedRows = database.delete("usage", "_id=?", new String[] { id });
        	    	//cbStates.delete(cbStates.keyAt(i));
        	    	deleteCount++;
        	    	Log.d(TAG,"Deleted "+id+" affectedRows "+affectedRows);
    			}
    			catch( Exception e )
    			{
    				Log.e(TAG,e.getMessage());
    			}
    		}
    	}
    	
    	Toast toast = Toast.makeText(getApplicationContext(), "Deleted "+deleteCount+" items", Toast.LENGTH_LONG);
    	toast.show();

    	dataSource.getCursor().requery();
    	dataSource.notifyDataSetChanged();
    	dataSource.notifyDataSetInvalidated();
    	getListView().invalidate();

    	return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.selectAll:
            	selectAll();
                return true;
            case R.id.deleteSelected:
            	deleteSelected();
                return true;
            case R.id.fillDummies:
            	fillDummies();
            	return true;
            case R.id.csvimport:
            	callCsvImport();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class UsageListAdapter extends SimpleCursorAdapter implements OnClickListener,
    	CompoundButton.OnCheckedChangeListener
    {
        private Context mContext;
        private int mLayout;
        private Cursor mCursor;
        private int mNameIndex;
        private int mIdIndex;
        private LayoutInflater mLayoutInflater;
        private SparseBooleanArray mCheckStates;

        private final class ViewHolder {
            public TextView type;
            public TextView val;
            public TextView date;
            public CheckBox checkBox;
            public boolean cbIsChecked = false;
            public int dbId;
        }

        public UsageListAdapter(Context context, int layout, Cursor c, String[] from, int[] to) {
            super(context, layout, c, from, to);

            this.mContext = context;
            this.mLayout = layout;
            this.mCursor = c;
            this.mLayoutInflater = LayoutInflater.from(context);
            this.mCheckStates = new SparseBooleanArray(c.getCount());
        }

        public SparseBooleanArray getCheckboxStates()
        {
        	return mCheckStates;
        }

        public boolean isChecked(int position) {
        	return mCheckStates.get(position, false);
        }

        public void setChecked(int position, boolean isChecked) {
        	mCheckStates.put(position, isChecked); notifyDataSetChanged();
        }

        public void toggle(int position) {
        	setChecked(position, !isChecked(position));
        } 

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        	Log.d(TAG,"onCheckedChanged "+(String)buttonView.getTag()+" "+isChecked);
        	mCheckStates.put(Integer.parseInt((String)buttonView.getTag()), isChecked);
        }
        
        public View getView(int position, View convertView, ViewGroup parent) {
            if (mCursor.moveToPosition(position)) {
                
            	ViewHolder viewHolder;

                if (convertView == null) {
                    convertView = mLayoutInflater.inflate(mLayout, null);

                    viewHolder = new ViewHolder();
                    viewHolder.type = (TextView) convertView.findViewById(R.id.usage_type);
                    viewHolder.val = (TextView) convertView.findViewById(R.id.usage_val);
                    viewHolder.date = (TextView) convertView.findViewById(R.id.usage_cdate);
                    viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.rowCheckBox);
                    convertView.setTag(viewHolder);
                }
                else {
                    viewHolder = (ViewHolder) convertView.getTag();
                }

                String name = mCursor.getString(0);
                String val = mCursor.getString(1);
                String cdate = mCursor.getString(2);
                String dbId = mCursor.getString(3);

                Date d;
                SimpleDateFormat f = new SimpleDateFormat();
                try
                {
                	d = new Date(Long.parseLong(cdate));
                }
                catch( Exception e )
                {
                	System.err.println("Error parsing timestamp "+e.getMessage());
                	d = new Date();
                }

                viewHolder.dbId = mCursor.getInt(3);
                viewHolder.type.setText(name);
                viewHolder.val.setText(val);
                viewHolder.date.setText(f.format(d));

                //viewHolder.checkBox.setOnClickListener(this);
                viewHolder.checkBox.setTag(dbId);
                viewHolder.checkBox.setChecked(mCheckStates.get(Integer.parseInt(dbId), false));
                viewHolder.checkBox.setOnCheckedChangeListener(this);
                //viewHolder.checkBox.setOnClickListener(this);
            }

            return convertView;
        }

        @Override
        public void onClick(View v) {
        	CheckBox cBox = (CheckBox) v;
            String dbId = (String) cBox.getTag();
        	this.toggle(Integer.parseInt(dbId));
        }
    }

	public class ImportCSVTask extends AsyncTask<String, Void, Boolean>
	{

		private final ProgressDialog dialog = new ProgressDialog( UsageListActivity.this );
		private Activity activity = null;
		public File importDir = new File(
				Environment.getExternalStorageDirectory(), "");

		public ImportCSVTask( Activity activity)
		{
			this.activity = activity;
			
		}

		@Override
		protected void onPreExecute()
		{
			this.dialog.setMessage(getString(R.string.Exporting_database));
			this.dialog.show();
		}

		protected Boolean doInBackground(final String... args)
		{

			if (!importDir.exists())
			{
				System.err.println("Import dir does not exist.");
				return false;
			}

	        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	        String importFilename = prefs.getString("csvFilename",null);
			
			File file = new File(importDir, importFilename);
			
			try
			{
				FileReader reader = new FileReader(file);
				BufferedReader br = new BufferedReader(reader);
				String cvsSplitBy = ",";
				String line;
				ItemModel model = new ItemModel( UsageListActivity.this );
				model.purgeAll();
				while((line = br.readLine()) != null) {
					String[] vals = line.split(cvsSplitBy); // 0 = id, 1=type, 2=val, 3=ts
					for( int x=0; x<vals.length; x++ )
					{
						vals[x] = vals[x].replace("\"", "");
					}
					model.addItem(vals[1], vals[2], vals[3]);
				}
				br.close();
				reader.close();

				return true;
			}
			catch (SQLException sqlEx)
			{
				Log.e("MainActivity", sqlEx.getMessage(), sqlEx);
				return false;
			}
			catch (IOException e)
			{
				Log.e("MainActivity", e.getMessage(), e);
				return false;
			}
		}

		protected void onPostExecute(final Boolean success)
		{
			if (this.dialog.isShowing())
				this.dialog.dismiss();

			if (success)
			{
				Toast.makeText(UsageListActivity.this,
								getString(R.string.CSV_Import_done),
								Toast.LENGTH_SHORT).show();
				this.activity.recreate();
			}
			else
			{
				Toast.makeText(UsageListActivity.this,
								getString(R.string.CSV_Import_failed),
								Toast.LENGTH_SHORT).show();
			}
		}
	}
}
