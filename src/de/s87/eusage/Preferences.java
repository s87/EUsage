package de.s87.eusage;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.preferences);
            // Get the custom preference

           /* Preference usageListButton = (Preference) findPreference("usageListButton");
            usageListButton.setOnPreferenceClickListener(new OnPreferenceClickListener() {

                                    public boolean onPreferenceClick(Preference preference) {
                                    	
                                        Intent intent = new Intent(preference.getContext(), UsageListActivity.class);
                                        startActivity(intent);
                                        return true;
                                            Toast.makeText(getBaseContext(),
                                                            "The custom preference has been clicked",
                                                            Toast.LENGTH_LONG).show();
                                            SharedPreferences customSharedPreference = getSharedPreferences(
                                                            "myCustomSharedPrefs", Activity.MODE_PRIVATE);
                                            SharedPreferences.Editor editor = customSharedPreference
                                                            .edit();
                                            editor.putString("myCustomPref",
                                                            "The preference has been clicked");
                                            editor.commit();
                                            return true;
                                    }

                            });

           */ 
    }
}
