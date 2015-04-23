package de.s87.eusage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class CSVExportActivity extends Activity {

	Button insetbt, exportButton;
	EditText csvResult;
	File exportDir;
	String exportFilename = null;
	private static final String TAG = "CSVExportActivity";
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.csvexport);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		exportFilename = prefs.getString("csvFilename",null);

		csvResult = (EditText) findViewById(R.id.editText1);
		insetbt = (Button) findViewById(R.id.bt1);
		exportButton = (Button) findViewById(R.id.bt2);

		exportDir = new File(
				Environment.getExternalStorageDirectory(), "");

		try
		{
			exportButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					try {
						new ExportDatabaseCSVTask().execute("");
					} catch (Exception ex) {
						Log.e("Error in MainActivity", ex.toString());
					}
				}
			});
			
			insetbt.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					fillTextField();
				}
			});

		} catch (SQLException ex) {
			ex.printStackTrace();
		}

	}
	
	public void fillTextField()
	{
		try
		{
			File file = new File(exportDir, exportFilename);
			if( !file.exists() )
			{
				csvResult.setText(getString(R.string.Export_file_not_found));
				return;
			}
			csvResult.setText("");
			FileReader fr = new FileReader(file);
			BufferedReader br = new BufferedReader(fr);
			String s;
			while((s = br.readLine()) != null) {
				csvResult.append(s+"\n");
			}
			br.close();
			fr.close();
		}
		catch( Exception e )
		{
			Log.e(TAG,e.getMessage());
		}
	}

	public class ExportDatabaseCSVTask extends AsyncTask<String, Void, Boolean>
	{
		private final ProgressDialog dialog = new ProgressDialog(
				CSVExportActivity.this);

		@Override
		protected void onPreExecute()
		{
			this.dialog.setMessage(getString(R.string.Exporting_database));
			this.dialog.show();
		}

		protected Boolean doInBackground(final String... args)
		{
			DatabaseHelper dbHelper = new DatabaseHelper( CSVExportActivity.this );
			SQLiteDatabase database = dbHelper.getReadableDatabase();

			if (!exportDir.exists())
				exportDir.mkdirs();

			File file = new File(exportDir, exportFilename);
			
			try
			{
				file.createNewFile();
				
				FileWriter fw = new FileWriter(file);
				PrintWriter pw = new PrintWriter(fw);
				
				Cursor curCSV = database.rawQuery("select * from usage", null);

				String[] columnNames = curCSV.getColumnNames();
				pw.append("\""+columnNames[0] +"\","+
						"\""+columnNames[1] +"\","+
						"\""+columnNames[2] +"\","+
						"\""+columnNames[3] +"\"\n");
				
				while (curCSV.moveToNext())
				{
					pw.append("\""+curCSV.getString(0) +"\","+
							"\""+curCSV.getString(1) +"\","+
							"\""+curCSV.getString(2) +"\","+
							"\""+curCSV.getString(3) +"\"\n");
				}
				
				pw.close();
				fw.close();

				curCSV.close();

				database.close();
				
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
				Toast.makeText(CSVExportActivity.this,
								getString(R.string.Export_done, exportFilename),
								Toast.LENGTH_SHORT).show();
				fillTextField();
			}
			else
			{
				Toast.makeText(CSVExportActivity.this,
								getString(R.string.Export_failed),
								Toast.LENGTH_SHORT).show();
			}
		}
	}
}
