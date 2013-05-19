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
    	String selection;
    	JSONObject request = new JSONObject();
    	JSONArray updatedObjects = new JSONArray();
    	
    	//Generate update data for statuses
    	if(mLastUpdateDate != null)	{
    		selection = TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE + " >= '" + mLastUpdateDate + "'";
    	}
    	else{
    		selection = null;
    	}
    	answer = getContext().getContentResolver().query(DouiContentProvider.TODO_STATUSES_URI, null, selection, null, null);
    	updatedObjects = createJSONData(answer, SyncAdapter.JSON_UPDATED_TYPE_STATUS,  updatedObjects);
    	/////////////////////////////////////////////////////////////////////////////////////////////

    	//Generate update data for categories
    	if(mLastUpdateDate != null){
    		selection =  TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE + " >= '" + mLastUpdateDate + "'";
    	}
    	else{
    		selection = null;
    	}
    	answer = getContext().getContentResolver().query(DouiContentProvider.TODO_CATEGORIES_URI, null, selection, null, null);
    	updatedObjects = createJSONData(answer, SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES,  updatedObjects);
    	///////////////////////////////////////////////////////////////////////////////////////////
    	
    	try{
    		if(mLastUpdateDate != null){
    			request.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, mLastUpdateDate);
    		}
    		else{
    			request.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, new Date().toString());
    		}
    		request.put(SyncAdapter.JSON_UPDATED_OBJECTS, updatedObjects);
    		
    	}catch (JSONException e) {
    		Log.v(TAG, "JSON for request to the server is not valid!!!");
    		e.printStackTrace();
    	}
    	
    	return request;
    }
    
    
    public JSONArray createJSONData(Cursor data, String type, JSONArray updatedObjects)
    {
    	JSONArray result = updatedObjects;
    	JSONObject currentObject = new JSONObject();
    	JSONObject updateObjectValues = new JSONObject();
    	
    	if(data != null)
    	{
    		for(boolean flag = data.moveToFirst(); flag; flag = data.moveToNext())
    		{
    			try {
    				updateObjectValues = new JSONObject();
        			currentObject = new JSONObject();
        			
        			currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_TYPE, type);
        			if(type.equals(SyncAdapter.JSON_UPDATED_TYPE_STATUS))
        			{
        				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, 
        						          data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY)) != null ? 
        						          data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY)) : JSONObject.NULL);
    					currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE)));
    					updateObjectValues.put(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME, data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME)));
        				updateObjectValues.put("client"+TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID, data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID)));
        			}
        			
        			if(type.equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES))
        			{
        				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, 
						          data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY)) != null ? 
						          data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY)) : JSONObject.NULL);
        				currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE)));
						updateObjectValues.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME, data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME)));
						updateObjectValues.put("client"+TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID, data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID)));
        			}
					

    				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_VALUES, updateObjectValues);
    				result.put(updatedObjects.length(), currentObject);
					
				} catch (JSONException e) {
					Log.v(TAG, "createJSONDataForServer failed");
					e.printStackTrace();
				}
    		}
    	}
    	return result;
    }
    
    private void updateLocalDatabase(JSONObject data)
    {
    	if(data != null)
    	{
    		//Update synch object with server key
    		Uri uriForUpdate;
    		String keyForUpdate  = "";
    		String idForUpdate   = "";
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
