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
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
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
    public static final String JSON_UPDATED_TYPE_ITEMS = "DouiTodoItem";
    
	private static final String TAG = "DouiSyncAdapter";
    private String mLastUpdateDate;
    private ContentValues m_valuesForUpdate;
    
    
    /**
     * {@inheritDoc}
     */
    public SyncAdapter(Context context, boolean autoInitialize) 
    {
        super(context, autoInitialize);
        m_valuesForUpdate = new ContentValues();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) 
    {
    	Log.v(TAG, "Start synchronization (onPerformSync)");
    	JSONObject request = new JSONObject();
    	JSONObject response;
    	try
    	{
    		request = getLocalData(TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE, DouiContentProvider.TODO_STATUSES_URI, SyncAdapter.JSON_UPDATED_TYPE_STATUS);
    		ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    		params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME, request.toString()));
    		response = NetworkUtilities.SendRequest("/sync", params);
    		updateLocalDatabase(response);
    		
    		params.clear();
    		request = getLocalData(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE, DouiContentProvider.TODO_CATEGORIES_URI, SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES);
    		params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME, request.toString()));
    		response = NetworkUtilities.SendRequest("/sync", params);
    		updateLocalDatabase(response);
    		
    		params.clear();
    		request = getLocalData(TableTodoItemsAdapter.TABLE_TODO_ITEMS_LAST_UPDATE, DouiContentProvider.TODO_ITEMS_URI, SyncAdapter.JSON_UPDATED_TYPE_ITEMS);
    		params.add(new BasicNameValuePair(SyncAdapter.JSON_REQUEST_PARAM_NAME, request.toString()));
    		response = NetworkUtilities.SendRequest("/sync", params);
    		updateLocalDatabase(response);
    		
    		SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
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
    private JSONObject getLocalData(String strFieldName, Uri uri, String strType)
    {
    	Cursor answer;
    	String selection;
    	JSONObject request = new JSONObject();
    	JSONArray updatedObjects = new JSONArray();
    	
    	if(mLastUpdateDate != null)	{
    		selection = strFieldName + " > '" + mLastUpdateDate + "'";
    	}
    	else{
    		selection = null;
    	}
    	answer = getContext().getContentResolver().query(uri, null, selection, null, null);
    	updatedObjects = createJSONData(answer, strType,  updatedObjects);
    	/////////////////////////////////////////////////////////////////////////////////////////////
    	
    	try{
    		if(mLastUpdateDate == null){
    			SimpleDateFormat formater = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.ENGLISH);
        		formater.setTimeZone(TimeZone.getTimeZone("UTC"));
        		String date = formater.format(new Date());
        		request.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, date);
    		}
    		else{
    			request.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, mLastUpdateDate);
    		}
    		
    		request.put(SyncAdapter.JSON_UPDATED_OBJECTS, updatedObjects);
    		
    	}catch (JSONException e) {
    		Log.v(TAG, "JSON for request to the server is not valid!!!");
    		e.printStackTrace();
    	}
    	
    	return request;
    }
    
    
    /**
     * Generate json object from cursor
     * @param data - cursor with data
     * @param type - type of object witch should be generated
     * @param updatedObjects -
     * @return - json data for object
     */
    public JSONArray createJSONData(Cursor data, String type, JSONArray updatedObjects)
    {
    	JSONArray result = updatedObjects;
    	JSONObject updateObjectValues = new JSONObject();
    	JSONArray updateObjectItems = new JSONArray();
    	JSONObject currentObject = new JSONObject();
    	
    	
    	if(data != null)
    	{
    		try {
    			updateObjectValues.put(SyncAdapter.JSON_UPDATED_OBJECT_TYPE, type);
    			for(boolean flag = data.moveToFirst(); flag; flag = data.moveToNext())
    			{
    				currentObject = new JSONObject();
        			
        			if(type.equals(SyncAdapter.JSON_UPDATED_TYPE_STATUS))
        			{
        				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, 
        						          data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY)) != null ? 
        						          data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY)) : JSONObject.NULL);
        				currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE)));
        				currentObject.put(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME, data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME)));
        				currentObject.put("client"+TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID, data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID)));
        			}
        			
        			
        			if(type.equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES))
        			{
        				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, 
						          data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY)) != null ? 
						          data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY)) : JSONObject.NULL);
        				currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE)));
        				currentObject.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME, data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME)));
        				currentObject.put("client"+TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID, data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID)));
        			}
        			
        			if(type.equals(SyncAdapter.JSON_UPDATED_TYPE_ITEMS))
        			{
        				currentObject.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY, 
						          data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY)) != null ? 
						          data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY)) : JSONObject.NULL);
        				currentObject.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP, data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_LAST_UPDATE)));
        				currentObject.put("client"+TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID, data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID)));
        				currentObject.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY)));
        				currentObject.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE)));
        				
        				if(data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY)) == null){
        					currentObject.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, JSONObject.NULL);
        				}
        				else{
        					
        					currentObject.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, getCategoryObjectKey(data.getInt(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY))));
        				}
        				
        				
        				if(data.getString(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS)) == null){
        					currentObject.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS, JSONObject.NULL);
        				}
        				else{
        					currentObject.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS, getStatusObjectKey(data.getInt(data.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS))));
        				}
        				
        			}
        			updateObjectItems.put(currentObject);
    			}
    			updateObjectValues.put(SyncAdapter.JSON_UPDATED_OBJECT_VALUES, updateObjectItems) ;
				result.put(updateObjectValues);
    		} catch (JSONException e) {
				Log.v(TAG, "createJSONDataForServer failed");
				e.printStackTrace();
			}
    	}
    	return result;
    }
    
    /**
     * Update local database with data from server
     * @param data - json object with data from server
     */
    private void updateLocalDatabase(JSONObject data)
    {
    	Cursor localData;
    	String selection;
    	if(data != null)
    	{
    		//Update synch object with server key
    		Uri uriForUpdate;
    		JSONArray currentValues;

    		try {
    			JSONArray dataFromServer = data.getJSONArray(JSON_UPDATED_OBJECTS);
    			
    			for(int i = 0; i < dataFromServer.length(); ++i)
    			{
    				m_valuesForUpdate.clear();
    				currentValues = dataFromServer.getJSONObject(i).getJSONArray(JSON_UPDATED_OBJECT_VALUES);
    				for(int j = 0; j < currentValues.length(); ++j)
    				{
    					if(dataFromServer.getJSONObject(i).get(JSON_UPDATED_OBJECT_TYPE).equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES))
    					{
    						addFieldToUpdate(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY, currentValues.getJSONObject(j), JSON_UPDATED_OBJECT_KEY);
    						addFieldToUpdate(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME, currentValues.getJSONObject(j), null);
    						if(currentValues.getJSONObject(j).getString("client_id").equals("null"))
    						{
    							selection = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY + " = '" + currentValues.getJSONObject(j).getString(JSON_UPDATED_OBJECT_KEY) + "'";
    							localData = getContext().getContentResolver().query(DouiContentProvider.TODO_CATEGORIES_URI, null, selection, null, null);
    							if(localData != null && localData.moveToFirst())
    							{
    								uriForUpdate = Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI.toString() + "/" + localData.getInt(localData.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID)));
        							getContext().getContentResolver().update(uriForUpdate, m_valuesForUpdate, null, null);
    							}
    							else
    							{
    								if(getContext().getContentResolver().insert(DouiContentProvider.TODO_CATEGORIES_URI, m_valuesForUpdate) == null)
    								{
    									Log.v(TAG, "Cannot insert new item for " + DouiContentProvider.TODO_CATEGORIES_URI.toString());
    								}
    							}
    						}
    						else
    						{
    							uriForUpdate = Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI.toString() + "/" + currentValues.getJSONObject(j).getString("client_id"));
    							getContext().getContentResolver().update(uriForUpdate, m_valuesForUpdate, null, null);
    						}
    					}
    					
    					if(dataFromServer.getJSONObject(i).get(JSON_UPDATED_OBJECT_TYPE).equals(SyncAdapter.JSON_UPDATED_TYPE_ITEMS)){
    						 
    						addFieldToUpdate(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY, currentValues.getJSONObject(j), null);
        					addFieldToUpdate(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE, currentValues.getJSONObject(j), null);
        					
        					if(!currentValues.getJSONObject(j).getString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY).equals("null"))
        					{
        						int nCategoryId = getCategoryIDByObjectKey(currentValues.getJSONObject(j).getString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY));
        						if(nCategoryId != -1)
        						{
        							m_valuesForUpdate.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY, nCategoryId );
        						}
        					}
        					if(!currentValues.getJSONObject(j).getString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS).equals("null"))
        					{
        						int nStatusId = getStatusIDByObjectKey(currentValues.getJSONObject(j).getString(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS));
        						if(nStatusId != -1)
        						{
        							m_valuesForUpdate.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS, nStatusId );
        						}
        					}
        					addFieldToUpdate(TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY, currentValues.getJSONObject(j), JSON_UPDATED_OBJECT_KEY);
        					if(currentValues.getJSONObject(j).getString("client_id").equals("null"))
        					{
        						selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY + " = '" + currentValues.getJSONObject(j).getString(JSON_UPDATED_OBJECT_KEY) + "'";
        						localData = getContext().getContentResolver().query(DouiContentProvider.TODO_ITEMS_URI, null, selection, null, null);
        						if(localData != null && localData.moveToFirst())
        						{
        							uriForUpdate = Uri.parse(DouiContentProvider.TODO_ITEMS_URI.toString() + "/" + localData.getInt(localData.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID)));
                					getContext().getContentResolver().update(uriForUpdate, m_valuesForUpdate, null, null);
        						}
        						else
        						{
        							if(getContext().getContentResolver().insert(DouiContentProvider.TODO_ITEMS_URI, m_valuesForUpdate) == null)
        							{
        								Log.v(TAG, "Cannot insert new item for " + DouiContentProvider.TODO_ITEMS_URI.toString());
        							}
        						}
        					}
        					else
        					{
        						uriForUpdate = Uri.parse(DouiContentProvider.TODO_ITEMS_URI.toString() + "/" + currentValues.getJSONObject(j).getString("client_id"));
            					getContext().getContentResolver().update(uriForUpdate, m_valuesForUpdate, null, null);
        					}
        					
        				}
    					
    					if(dataFromServer.getJSONObject(i).get(JSON_UPDATED_OBJECT_TYPE).equals(SyncAdapter.JSON_UPDATED_TYPE_STATUS))
    					{
    						addFieldToUpdate(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY, currentValues.getJSONObject(j), JSON_UPDATED_OBJECT_KEY);
    						addFieldToUpdate(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME, currentValues.getJSONObject(j), null);
    						if(!currentValues.getJSONObject(j).getString("client_id").equals("null"))
    						{
    							uriForUpdate = Uri.parse(DouiContentProvider.TODO_STATUSES_URI.toString() + "/" + currentValues.getJSONObject(j).getString("client_id"));
    							getContext().getContentResolver().update(uriForUpdate, m_valuesForUpdate, null, null);
    						}
    						else{
    							Log.v(TAG, "id for status is empty");
    						}
    					}
    				}
    			}
    		} catch (JSONException e) {
    			Log.v(TAG,"Data from server is not valid!!");
    			e.printStackTrace();
    		}
    	}
    }
    
    /**
     * Add field to set for update local database
     * @param fieldName - name of field in db
     * @param value     - JSON object with values
     * @param valueName - value name in json object. 
     * If null then fieldName will be use as name in json object  
     */
    private void addFieldToUpdate(String fieldName, JSONObject value,  String valueName)
    {
    	String data;
		try {
			if(valueName != null){
				data = value.getString(valueName);
			}
			else{
				data = value.getString(fieldName);
			}
			m_valuesForUpdate.put(fieldName, data);
		} catch (JSONException e) {
			Log.v(TAG, "Cannot add value for field " + fieldName);
			e.printStackTrace();
		}
    	
    }
    
    
    private String getStatusObjectKey(int nStatusID)
    {
    	String strResult = "";
    	Cursor data;
   		Uri uri =  Uri.parse(DouiContentProvider.TODO_STATUSES_URI.toString() + "/" + nStatusID);
   		data =  getContext().getContentResolver().query(uri, null, null, null, null);
   		if(data != null && data.moveToFirst())
   		{
   			strResult = data.getString(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY));
   		}
    	return strResult;
    	
    }
    
    private String getCategoryObjectKey(int nCategoryID)
    {
    	String strResult = "";
    	Cursor data;
    	Uri uri =  Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI.toString() + "/" + nCategoryID);
    	data =  getContext().getContentResolver().query(uri, null, null, null, null);
    	if(data != null && data.moveToFirst())
    	{
    		strResult = data.getString(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY));
    	}
    	return strResult;
    }
    
    private int getCategoryIDByObjectKey(String strObjectKey)
    {
    	int nResult = -1;
    	Cursor data;
    	String strSelection = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY + " = '" + strObjectKey + "'";
    	data = getContext().getContentResolver().query(DouiContentProvider.TODO_CATEGORIES_URI, null, strSelection, null, null);
    	if(data != null && data.moveToFirst())
    	{
    		nResult = data.getInt(data.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID));
    	}
    	return nResult;
    }
    
    private int getStatusIDByObjectKey(String strObjectKey)
    {
    	int nResult = -1;
    	Cursor data;
    	String strSelection = TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY + " = '" + strObjectKey + "'";
    	data = getContext().getContentResolver().query(DouiContentProvider.TODO_STATUSES_URI, null, strSelection, null, null);
    	if(data != null && data.moveToFirst())
    	{
    		nResult = data.getInt(data.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID));
    	}
    	return nResult;
    }
}
