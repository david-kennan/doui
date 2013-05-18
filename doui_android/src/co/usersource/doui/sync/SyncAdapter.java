package co.usersource.doui.sync;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;
import co.usersource.doui.sync.util.NetworkUtilities;


/**
 * This class implements synchronization with server.
 * 
 * @author Sergey Gadzhilov
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter 
{
	public static final String JSON_REQUEST_PARAM_NAME = "jsonData";
	public static final String JSON_UPDATED_OBJECT_VALUES = "updateObjectValues";
	public static final String JSON_UPDATED_OBJECT_KEY = "updateObjectKey";
    public static final String JSON_UPDATED_OBJECT_TIME = "updateObjectTime";
    public static final String JSON_UPDATED_OBJECT_TYPE = "updateObjectType";
    public static final String JSON_LAST_UPDATE_TIMESTAMP = "lastUpdateTimestamp";
    public static final String JSON_UPDATED_OBJECTS = "updatedObjects";
    
    public static final String JSON_UPDATED_TYPE_STATUS = "DouiTodoStatus";
    public static final String JSON_UPDATED_TYPE_CATEGORIES = "DouiTodoCategories";
    
	
	private static final String TAG = "DouiSyncAdapter";
    private String mLastUpdateDate;
    
    
    /**
     * {@inheritDoc}
     */
    public SyncAdapter(Context context, boolean autoInitialize) 
    {
        super(context, autoInitialize);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) 
    {
    	Log.v(TAG, "Start synchronization (onPerformSync)");
    	JSONObject request = new JSONObject();
    	try
    	{
    		request = getLocalData();
    		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    		params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME, request.toString()));
    		JSONObject response = NetworkUtilities.SendRequest("/sync", params);
    		updateLocalDatabase(response);
    		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS", Locale.ENGLISH);
    		formater.setTimeZone(TimeZone.getTimeZone("UTC"));
    		mLastUpdateDate = formater.format(new Date());
    	}
    	catch (IOException e) 
		{
			Log.v(TAG, "I/O excecption!!!");
			e.printStackTrace();
		}
    }
    
    /**
     * This function reads information from local database.
     */
    private JSONObject getLocalData()
    {
    	Cursor answer;
    	JSONObject request = new JSONObject();
    	JSONObject currentObject;
    	JSONArray updatedObjects = new JSONArray();
    	JSONObject updateObjectValues;
    	String selection;
    	
    	//Generate update data for statuses
    	if(mLastUpdateDate != null)
    	{
    		selection = TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE + " >= '" + mLastUpdateDate + "'";
    	}
    	else{
    		selection = null;
    	}
    	answer = getContext().getContentResolver().query(DouiContentProvider.TODO_STATUSES_URI, null, selection, null, null);
    	if(answer != null)
    	{
    		for(boolean flag = answer.moveToFirst(); flag; flag = answer.moveToNext())
    		{
   				try{
   					updateObjectValues = new JSONObject();
   					currentObject = new JSONObject();
   					currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, answer.getString(3) != null ? answer.getString(3) : JSONObject.NULL);
    				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_TYPE, SyncAdapter.JSON_UPDATED_TYPE_STATUS);
    				currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, answer.getString(2));
    				updateObjectValues.put(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME, answer.getString(1));
    				updateObjectValues.put("client"+TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID, answer.getString(0));
    				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_VALUES, updateObjectValues);
    				updatedObjects.put(updatedObjects.length(), currentObject);
  				
   				} catch (JSONException e) {
    					Log.v(TAG, "JSON for update statuses is not valid!!!");
    					e.printStackTrace();
    			}
    		}
    		
    	}
    	else
    	{
    		Log.v(TAG, "Data for update statuses is not present in the database!!!");
    	}
    	/////////////////////////////////////////////////////////////////////////////////////////////

    	//Generate update data for categories
    	if(mLastUpdateDate != null)
    	{
    		selection =  TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE + " >= '" + mLastUpdateDate + "'";
    	}
    	else{
    		selection = null;
    	}
    	answer = getContext().getContentResolver().query(DouiContentProvider.TODO_CATEGORIES_URI, null, selection, null, null);	
    	if(answer != null)
    	{
    		for(boolean flag = answer.moveToFirst(); flag; flag = answer.moveToNext())
    		{
   				try{
   					updateObjectValues = new JSONObject();
   					currentObject = new JSONObject();

   					currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, answer.getString(3) != null ? answer.getString(3) : JSONObject.NULL);
    				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_TYPE, SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES);
    				currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, answer.getString(2));

    				updateObjectValues.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME, answer.getString(1));
    				updateObjectValues.put("client"+TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID, answer.getString(0));
    				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_VALUES, updateObjectValues);
    				updatedObjects.put(updatedObjects.length(), currentObject);
  				
   				} catch (JSONException e) {
    					Log.v(TAG, "JSON for update statuses is not valid!!!");
    					e.printStackTrace();
    			}
    		}
    	}
    	else
    	{
    		Log.v(TAG, "Data for update categories is not present in the database!!!");
    	}
    	/////////////////////////////////////////////////////////////////////////////////////////////
    	
    	try{
    		if(mLastUpdateDate != null){
    			request.put("lastUpdateTimestamp", mLastUpdateDate);
    		}
    		else{
    			request.put("lastUpdateTimestamp", new Date().toString());
    		}
    		request.put("updatedObjects", updatedObjects);
    	}catch (JSONException e) {
    		Log.v(TAG, "JSON for request to the server is not valid!!!");
    		e.printStackTrace();
    	}
    	
    	return request;
    }
    
    
    private void updateLocalDatabase(JSONObject data)
    {
    	if(data != null)
    	{
    		//Update synch object with server key
    		Uri uriForUpdate;
    		String keyForUpdate = "";
    		String idForUpdate = "";
    		String nameForUpdate = "";
    		ContentValues valuesForUpdate = new ContentValues();

    		try {
    			JSONArray dataFromServer = data.getJSONArray(JSON_UPDATED_OBJECTS);

    			for(int i = 0; i < dataFromServer.length(); ++i)
    			{
    				if(dataFromServer.getJSONObject(i).get(JSON_UPDATED_OBJECT_TYPE).equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES))
    				{
    					keyForUpdate = dataFromServer.getJSONObject(i).getString(JSON_UPDATED_OBJECT_KEY);
    					idForUpdate  = dataFromServer.getJSONObject(i).getJSONObject(JSON_UPDATED_OBJECT_VALUES).getString("client_id");
    					nameForUpdate = dataFromServer.getJSONObject(i).getJSONObject(JSON_UPDATED_OBJECT_VALUES).getString("name");
    					uriForUpdate = Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI.toString() + "/" + idForUpdate);
    					valuesForUpdate.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME, nameForUpdate);
    					valuesForUpdate.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY, keyForUpdate);
    					getContext().getContentResolver().update(uriForUpdate, valuesForUpdate, keyForUpdate, null);
    				}
    			}
    		} catch (JSONException e) {
    			Log.v(TAG,"Data from server is not valid!!");
    			e.printStackTrace();
    		}
    	}
    }
}
