package de.s87.eusage;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class HTTPSyncronizer {

	protected SQLiteDatabase database = null;
	protected Context context;

	private static final String TAG = "HTTPSyncronizer";
	
	public HTTPSyncronizer( Context context,SQLiteDatabase database )
	{
		this.context = context;
		this.database = database;
	}

	public boolean sync( String remoteUrl ) throws HTTPSyncronizerException
	{
		
		if( !Connectivity.isConnected(context) )
		{
			throw new HTTPSyncronizerException("Network not available");
		}

		JSONArray jsonList = new JSONArray();
		try
		{
			String fields[] = {"type", "usage", "cdate"};
			Cursor data = database.query("usage",fields,null,null,null,null,null);
			while( data.moveToNext() )
			{
				JSONObject obj = new JSONObject();
	            obj.put("type", data.getString(0));
	            obj.put("ts", data.getString(1));
	            obj.put("val", data.getString(2));
	            jsonList.put(obj);
			}
			data.close();
		}
		catch( Exception e )
		{
			throw new HTTPSyncronizerException(e.getMessage());
		}
		
		try {

            // Create a new HttpClient and Post Header
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(remoteUrl);

            
            if( !remoteUrl.isEmpty() )
            {
                // Add your data
                List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
                nameValuePairs.add(new BasicNameValuePair("data", jsonList.toString()));
                httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));
                // Execute HTTP Post Request
                HttpResponse response = httpclient.execute(httppost);

                JSONObject json = new JSONObject();
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    // A Simple JSON Response Read
                    InputStream instream = entity.getContent();
                    String result= convertStreamToString(instream);
                    instream.close();
                    try
                    {
                    	json=new JSONObject(result);
                    	if( json.getString("status").equals("ok") )
                    	{
                    		return true;
                    	}
                    	else
                    	{
                    		throw new HTTPSyncronizerException("Wrong/Missing response status: "+json.getString("status"));
                    	}
                    	
                    }
                    catch( JSONException e )
                    {
                    	throw new HTTPSyncronizerException(e.getMessage());
                    }
                    
                }
                
                Log.d(TAG,"Status code: "+response.getStatusLine().getStatusCode());
                
                if( response.getStatusLine().getStatusCode() == 200 )
                {
                	Log.d(TAG,"XX "+response.toString());
                	//msg = "Data send to server!";
                	return true;
                }
                else
                {
                	throw new HTTPSyncronizerException("HTTP-Error code: "+response.getStatusLine().getStatusCode());
                }
            }

            
        } catch (ClientProtocolException e) {
        	Log.e(TAG, e.getMessage());
            // TODO Auto-generated catch block
        } catch (IOException e) {
        	Log.e(TAG, e.getMessage());
            // TODO Auto-generated catch block
        }

        return true;
    }
	
    /**
    *
    * @param is
    * @return String
    */
   public static String convertStreamToString(InputStream is) {
       BufferedReader reader = new BufferedReader(new InputStreamReader(is));
       StringBuilder sb = new StringBuilder();

       String line = null;
       try {
           while ((line = reader.readLine()) != null) {
               sb.append(line + "\n");
           }
       } catch (IOException e) {
           e.printStackTrace();
       } finally {
           try {
               is.close();
           } catch (IOException e) {
               e.printStackTrace();
           }
       }
       return sb.toString();
   }

   
}
