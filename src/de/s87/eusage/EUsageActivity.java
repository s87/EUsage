package de.s87.eusage;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class EUsageActivity extends Activity {

	TextView usageText;
	
	private static final String TAG = "EUsageActivity";
	static final int DATE_DIALOG_ID = 0;

	String usageFromDatabase = null;
	Calendar cal = Calendar.getInstance();

    private int mYear = cal.get(Calendar.YEAR);
    private int mMonth = cal.get(Calendar.MONTH);
    private int mDay = cal.get(Calendar.DAY_OF_MONTH);
    
    Button pickDate;
    
	SQLiteDatabase database = null; 
	SharedPreferences prefs = null;

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
        case DATE_DIALOG_ID:
            return new DatePickerDialog(this,
                        mDateSetListener,
                        mYear, mMonth, mDay);
        }
        return null;
    }
    
    // updates the date we display in the TextView
    private void updateDisplay() {
    	cal.set(Calendar.YEAR, mYear);
    	cal.set(Calendar.MONTH, mMonth);
    	cal.set(Calendar.DAY_OF_MONTH, mDay);    	
        SimpleDateFormat f = new SimpleDateFormat();
    	pickDate.setText(f.format(cal.getTime()));
    }
    
    // the callback received when the user "sets" the date in the dialog
    private DatePickerDialog.OnDateSetListener mDateSetListener =
            new DatePickerDialog.OnDateSetListener() {

                public void onDateSet(DatePicker view, int year, 
                                      int monthOfYear, int dayOfMonth) {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    updateDisplay();
                }
            };

    private OnClickListener saveButtonListener = new OnClickListener() {
    	@Override
        public void onClick(View v) {
        	
            RadioButton gas = (RadioButton)findViewById(R.id.gas);
            RadioButton power = (RadioButton)findViewById(R.id.power);
            RadioButton water = (RadioButton)findViewById(R.id.water);

            String type = "";
            if( gas.isChecked() )
            	type = "gas";
            else if( power.isChecked() )
            	type = "power";
            else if( water.isChecked() )
            	type = "water";
            else
            	return;

            if( usageText.getText().equals("") )
            	return;

            ItemModel model = new ItemModel(EUsageActivity.this);
            
        	Timestamp ts = new Timestamp(cal.getTime().getTime());
        	Long theTimestamp = ts.getTime();
            if( model.addItem(type, usageText.getText().toString(), Long.toString(theTimestamp)) )
            {
            	usageFromDatabase = usageText.getText().toString();
                Toast toast = Toast.makeText(
                		getApplicationContext(), 
                		getString(R.string.usage_saved_to_phone),
                		Toast.LENGTH_LONG);
                toast.show();
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
    	
        switch (item.getItemId()) {
            case R.id.usagelist:
                Intent intent = new Intent(getBaseContext(), UsageListActivity.class);
                startActivity(intent);
                return true;
            case R.id.sync:
                Intent syncIntent = new Intent(getBaseContext(), SyncActivity.class);
                startActivity(syncIntent);
                return true;
            case R.id.csvexport:
                Intent csvIntent = new Intent(getBaseContext(), CSVExportActivity.class);
                startActivity(csvIntent);
                return true;
            case R.id.properties:
                Intent settingsActivity = new Intent(getBaseContext(),
                        Preferences.class);
                startActivity(settingsActivity);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
     }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // To get the httpupdateurl etc.
        prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        
        DatabaseHelper dbHelper = new DatabaseHelper(this);
        database = dbHelper.getWritableDatabase();

        setContentView(R.layout.input);

        // Capture our button from layout
        Button saveButton = (Button)findViewById(R.id.savebutton);
        if( saveButton == null )
        	System.err.println("NIX SAVEBUTTON");
        pickDate = (Button)findViewById(R.id.pickDate);
        
        // add a click listener to the button
        pickDate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                showDialog(DATE_DIALOG_ID);
            }
        });

       	mYear = cal.get(Calendar.YEAR);
       	mMonth = cal.get(Calendar.MONTH);
       	mDay = cal.get(Calendar.DAY_OF_MONTH);
 
       	updateDisplay();
       
        usageText = (TextView)findViewById(R.id.evalue);

        // Register the onClick listener with the implementation above
        saveButton.setOnClickListener( saveButtonListener );
        
        RadioGroup radGrp = (RadioGroup)findViewById(R.id.usageType);
        radGrp.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
          public void onCheckedChanged(RadioGroup arg0, int id) {
        	  
        	  String type = "";
            switch (id) {
            case -1:
              break;
            case R.id.gas:
            	type = "gas";
              break;
            case R.id.power:
            	type = "power";
              break;
            case R.id.water:
            	type = "water";
              break;
            default:
              break;
            }
            
            if( type != "" && ( usageText.getText().toString().isEmpty() || 
            		usageText.getText().toString().equals(usageFromDatabase) ) )
            {
            	try
            	{
            		Cursor cursor = database.rawQuery("SELECT max(usage) FROM usage WHERE type='"+type+"'",null);
            		if( cursor.moveToFirst() )
            		{
            			usageText.setText(""); // to set the cursor at end of textview
            			usageText.append(Double.toString(cursor.getDouble(0)));
            			usageText.requestFocus();
            			usageFromDatabase = usageText.getText().toString();
            			cursor.close();
            		}
            	}
            	catch( SQLException e )
            	{
            		Log.e(TAG,e.getMessage());
            	}
            }
          }
        });
    }

	@Override
	public void onDestroy() {
		database.close();
		super.onDestroy();
	}

}