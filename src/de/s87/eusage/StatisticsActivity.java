package de.s87.eusage;

import java.text.DecimalFormat;
import java.util.Iterator;

import android.app.ActionBar.LayoutParams;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class StatisticsActivity extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.statistics);

		Statistics stats = new Statistics(this);

        TableLayout.LayoutParams rowParams = 
                new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1f);
        TableRow.LayoutParams itemParams = 
                new TableRow.LayoutParams(LayoutParams.FILL_PARENT, 
                                          LayoutParams.FILL_PARENT, 1f);

		TableLayout stk = (TableLayout) findViewById(R.id.table_main);
		TableRow tbrow0 = new TableRow(this);
		tbrow0.setLayoutParams(rowParams);
        TextView tv0 = new TextView(this);
        tv0.setText(getString(R.string.type_of_usage));

        tv0.setTextColor(Color.WHITE);
        tv0.setLayoutParams(itemParams);
        tbrow0.addView(tv0);
        
        TextView tv1 = new TextView(this);
        tv1.setText(getString(R.string.Usage_calculated_by_day));
        tv1.setTextColor(Color.WHITE);

        tv1.setLayoutParams(itemParams);
        tbrow0.addView(tv1);
        stk.addView(tbrow0);
        
        //String[] types = stats.getUsageTypes();
        Iterator<String> types = stats.getUsageTypes();
        while( types.hasNext() )
        {
        	String typeId = types.next();
        	TableRow row = new TableRow(this);
        	row.setLayoutParams(rowParams);
        	TextView type = new TextView(this);
        	type.setText( typeId );
        	type.setLayoutParams(itemParams);
        	row.addView(type);
        	
        	TextView usageText = new TextView(this);
        	
        	Double usageVal = stats.getUsageForType(typeId).getUsageForDay();
        	DecimalFormat f = new DecimalFormat("####.##");
        	usageText.setText( f.format(usageVal) );
        	usageText.setLayoutParams(itemParams);
        	row.addView(usageText);
        	
        	stk.addView( row );
        }
		
    }
}
