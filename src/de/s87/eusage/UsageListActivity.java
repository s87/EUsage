package de.s87.eusage;

import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.ListActivity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
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

        Cursor data = database.query("usage", fields, 
                null, null, null, null, "cdate DESC");
    	startManagingCursor(data);
        dataSource = new UsageListAdapter(this, 
                R.layout.usagelist, data, fields,
                new int[] { R.id.usage_type, R.id.usage_val, R.id.usage_cdate, R.id.rowCheckBox });
        setListAdapter(dataSource);
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
   
    protected boolean fillDummies()
    {
    	database = dbHelper.getWritableDatabase();
        for( int x=0; x<25; x++)
        {
        	try
        	{
        		database.execSQL("INSERT INTO usage "
        				+"(type,usage,cdate)" 
        				+" VALUES ('dummy',"+x+","+new Date().getTime()+")");
        	}
        	catch( Exception e )
        	{
        		Log.d(TAG,e.getMessage());
        	}
        }
        
        /*Cursor data = database.query("usage", fields, 
                null, null, null, null, "cdate DESC");
        dataSource.changeCursor(data);*/
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
    	
    	Log.d(TAG,"SO VIELE "+cbStates.size());
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

                Date d = new Date(Long.parseLong(cdate));
                SimpleDateFormat f = new SimpleDateFormat();

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

}
