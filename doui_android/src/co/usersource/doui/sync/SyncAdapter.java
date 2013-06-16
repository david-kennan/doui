package co.usersource.doui.sync;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.Toast;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;
import co.usersource.doui.network.HttpConnector;
import co.usersource.doui.network.IHttpConnectorAuthHandler;
import co.usersource.doui.network.IHttpRequestHandler;

/**
 * This class implements synchronization with server.
 * 
 * @author Sergey Gadzhilov
 * 
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter implements OnSharedPreferenceChangeListener {
	public static final String JSON_REQUEST_PARAM_NAME = "jsonData";
	public static final String JSON_UPDATED_OBJECT_VALUES = "updateObjectValues";
	public static final String JSON_UPDATED_OBJECT_KEY = "updateObjectKey";
	public static final String JSON_UPDATED_OBJECT_TIME = "updateObjectTime";
	public static final String JSON_UPDATED_OBJECT_TYPE = "updateObjectType";
	public static final String JSON_LAST_UPDATE_TIMESTAMP = "lastUpdateTimestamp";
	public static final String JSON_UPDATED_OBJECTS = "updatedObjects";
	public static final String JSON_REQUEST_TYPE = "requestType";

	public static final String JSON_UPDATED_TYPE_STATUS = "DouiTodoStatus";
	public static final String JSON_UPDATED_TYPE_CATEGORIES = "DouiTodoCategories";
	public static final String JSON_UPDATED_TYPE_ITEMS = "DouiTodoItem";

	public static final String JSON_REQUEST_TYPE_GEN_KEYS = "generateKeys";
	public static final String JSON_REQUEST_TYPE_UPDATE_DATA = "updateData";

	public static final String SYNC_ACCOUNT_TYPE = "com.google";

	/**
	 * The frequency of sync in seconds
	 */
	public static final int SYNC_PERIOD = 300;

	private static final String TAG = "DouiSyncAdapter";
	private String mLastUpdateDate;
	private ContentValues m_valuesForUpdate;
	private HttpConnector httpConnector;
	private JSONObject m_localData;
	private JSONObject m_newRecords;

	private boolean prefIsSyncable; // Determinate whether this sync adapter
	private String prefSyncUrl; // Url where Sync service running
	private int prefSyncTimeframe; // Period how often perform sync
	private Account prefSyncAccount; // Account to be used for sync

	/**
	 * {@inheritDoc}
	 */
	public SyncAdapter(Context context, boolean autoInitialize) {
		super(context, autoInitialize);
		this.loadPreferences();
		// TODO set sync properties in depend of settings.
		m_valuesForUpdate = new ContentValues();
	}

	/**
	 * This procedure used to load preferences for sync adapter defined by user.
	 */
	private void loadPreferences() {
		SharedPreferences sharedPref = PreferenceManager
				.getDefaultSharedPreferences(this.getContext());
		prefIsSyncable = sharedPref
				.getBoolean(
						this.getContext()
								.getString(R.string.prefIsSyncable_Key), false);
		prefSyncUrl = sharedPref
				.getString(
						this.getContext().getString(
								R.string.prefSyncServerUrl_Key),
						this.getContext().getString(
								R.string.prefSyncServerUrl_Default));
		String strPrefSyncTimeframe = sharedPref.getString(
				this.getContext().getString(R.string.prefSyncRepeatTime_Key),
				""+SYNC_PERIOD);
		prefSyncTimeframe = Integer.parseInt(strPrefSyncTimeframe);
		String strPrefSyncAccount = sharedPref.getString(this.getContext()
				.getString(R.string.prefSyncAccount_Key), "");
		prefSyncAccount = this.getAccountByString(strPrefSyncAccount);
		if (null == prefSyncAccount) {
			prefIsSyncable = false;
			Log.e(this.getClass().getName(),
					"Wrong account provided in preferences: "
							+ strPrefSyncAccount);
		}
	}

	/**
	 * Get system account object by it's string representation.
	 * 
	 * @param accountName
	 *            string that identifies account.
	 * @return account object if exists null otherwise
	 */
	private Account getAccountByString(String accountName) {
		Account result = null;
		Account[] accounts = AccountManager.get(getContext()).getAccounts();
		for (Account account : accounts) {
			if (account.name.equals(accountName)) {
				result = account;
				break;
			}
		}
		return result;
	}

	/**
	 * @return the httpConnector
	 */
	public HttpConnector getHttpConnector() {
		if (httpConnector == null) {
			httpConnector = new HttpConnector();
		}
		return httpConnector;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onPerformSync(Account account, Bundle extras, String authority,
			ContentProviderClient provider, SyncResult syncResult) {
		Log.d(TAG, "onPerformSync");
		ContentResolver.addPeriodicSync(account, authority, extras,
				SyncAdapter.SYNC_PERIOD);
		if (getHttpConnector().isAuthenticated()) {
			Log.d(TAG, "httpConnector.isAuthenticated()==true. Perform sync.");
			performSyncRoutines();
		} else {
			Log.d(TAG, "httpConnector.isAuthenticated()==false. Perform auth.");
			getHttpConnector().setHttpConnectorAuthHandler(
					new IHttpConnectorAuthHandler() {

						public void onAuthSuccess() {
							performSyncRoutines();
						}

						public void onAuthFail() {
							Toast.makeText(getContext(),
									"Auth to sync service failed",
									Toast.LENGTH_LONG).show();
						}
					});
			getHttpConnector().authenticate(getContext(), account);
		}
	}

	private void performSyncRoutines() {
		Log.v(TAG, "Start synchronization (performSyncRoutines)");
		try {
			getLocalData();
			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(
					SyncAdapter.JSON_REQUEST_PARAM_NAME, this.m_newRecords
							.toString()));
			getHttpConnector().SendRequest("/sync", params,
					new IHttpRequestHandler() {

						public void onRequest(JSONObject response) {
							UpdateKeys(response);
						}
					});

		} catch (IOException e) {
			Log.v(TAG, "I/O excecption!!!");
			e.printStackTrace();
		}
	}

	private void UpdateKeys(JSONObject data) {
		try {
			// TODO make here check for empty array received.
			JSONArray updatedObjects = data.getJSONArray(JSON_UPDATED_OBJECTS);

			for (int i = 0; i < updatedObjects.length(); ++i) {
				if (updatedObjects.getJSONObject(i)
						.get(JSON_UPDATED_OBJECT_TYPE)
						.equals(SyncAdapter.JSON_UPDATED_TYPE_STATUS)) {
					updateKeysByType(updatedObjects.getJSONObject(i)
							.getJSONArray(JSON_UPDATED_OBJECT_VALUES),
							SyncAdapter.JSON_UPDATED_TYPE_STATUS);
					this.updateLocalStatuses(updatedObjects.getJSONObject(i)
							.getJSONArray(JSON_UPDATED_OBJECT_VALUES));
				}

				if (updatedObjects.getJSONObject(i)
						.get(JSON_UPDATED_OBJECT_TYPE)
						.equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES)) {
					updateKeysByType(updatedObjects.getJSONObject(i)
							.getJSONArray(JSON_UPDATED_OBJECT_VALUES),
							SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES);
					this.updateLocalCategories(updatedObjects.getJSONObject(i)
							.getJSONArray(JSON_UPDATED_OBJECT_VALUES));
				}

				if (updatedObjects.getJSONObject(i)
						.get(JSON_UPDATED_OBJECT_TYPE)
						.equals(SyncAdapter.JSON_UPDATED_TYPE_ITEMS)) {
					JSONArray keys = updatedObjects.getJSONObject(i)
							.getJSONArray("itemsKeys");
					JSONArray localUpdatedObjects = m_localData
							.getJSONArray(JSON_UPDATED_OBJECTS);

					int localItem = 0;
					for (; localItem < localUpdatedObjects.length(); ++localItem) {
						if (localUpdatedObjects.getJSONObject(localItem)
								.get(JSON_UPDATED_OBJECT_TYPE)
								.equals(SyncAdapter.JSON_UPDATED_TYPE_ITEMS)) {
							break;
						}
					}

					JSONArray localValues = localUpdatedObjects.getJSONObject(
							localItem).getJSONArray(JSON_UPDATED_OBJECT_VALUES);
					for (int item = 0, keyIndex = 0; item < localValues
							.length(); ++item) {
						if (localValues.getJSONObject(item)
								.getString(JSON_UPDATED_OBJECT_KEY)
								.equals("null")) {
							localValues.getJSONObject(item).put(
									JSON_UPDATED_OBJECT_KEY,
									keys.getString(keyIndex));
							++keyIndex;
						}
					}

					this.updateLocalItems(localValues);
				}
			}

			final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
			params.add(new BasicNameValuePair(
					SyncAdapter.JSON_REQUEST_PARAM_NAME, this.m_localData
							.toString()));
			getHttpConnector().SendRequest("/sync", params,
					new IHttpRequestHandler() {

						public void onRequest(JSONObject response) {
							updateLocalDatabase(response);
						}
					});

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			Log.v(TAG, "I/O excecption!!!");
			e.printStackTrace();
		}
	}

	private void updateKeysByType(JSONArray values, String type) {
		try {
			JSONArray localUpdatedObjects = m_localData
					.getJSONArray(JSON_UPDATED_OBJECTS);

			int localItem = 0;
			for (; localItem < localUpdatedObjects.length(); ++localItem) {
				if (localUpdatedObjects.getJSONObject(localItem)
						.get(JSON_UPDATED_OBJECT_TYPE).equals(type)) {
					break;
				}
			}

			for (int item = 0; item < values.length(); ++item) {
				localUpdatedObjects.getJSONObject(localItem)
						.getJSONArray(JSON_UPDATED_OBJECT_VALUES)
						.put(values.getJSONObject(item));
			}
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	/**
	 * This function reads information from local database.
	 */
	private void getLocalData() {
		Cursor answer;
		String selection;
		this.m_localData = new JSONObject();
		this.m_newRecords = new JSONObject();
		try {
			this.m_localData.put(SyncAdapter.JSON_REQUEST_TYPE,
					SyncAdapter.JSON_REQUEST_TYPE_UPDATE_DATA);
			this.m_localData.put(SyncAdapter.JSON_UPDATED_OBJECTS,
					new JSONArray());
			if (mLastUpdateDate == null) {
				this.m_localData.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP,
						"2000-01-01 00:00:00:00");
			} else {
				this.m_localData.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP,
						mLastUpdateDate);
			}

			this.m_newRecords.put(SyncAdapter.JSON_REQUEST_TYPE,
					SyncAdapter.JSON_REQUEST_TYPE_GEN_KEYS);
			this.m_newRecords.put(SyncAdapter.JSON_UPDATED_OBJECTS,
					new JSONArray());

		} catch (JSONException e1) {
			e1.printStackTrace();
		}

		// Generate update data for statuses
		if (mLastUpdateDate != null) {
			selection = TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE
					+ " > '" + mLastUpdateDate + "'";
		} else {
			selection = null;
		}
		answer = getContext().getContentResolver().query(
				DouiContentProvider.TODO_STATUSES_URI, null, selection, null,
				null);
		createJSONData(answer, SyncAdapter.JSON_UPDATED_TYPE_STATUS);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		// Generate update data for categories
		if (mLastUpdateDate != null) {
			selection = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE
					+ " > '" + mLastUpdateDate + "'";
		} else {
			selection = null;
		}
		answer = getContext().getContentResolver().query(
				DouiContentProvider.TODO_CATEGORIES_URI, null, selection, null,
				null);
		createJSONData(answer, SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES);
		// ///////////////////////////////////////////////////////////////////////////////////////////
		// Generate update data for items
		if (mLastUpdateDate != null) {
			selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_LAST_UPDATE
					+ " > '" + mLastUpdateDate + "'";
		} else {
			selection = null;
		}
		answer = getContext().getContentResolver()
				.query(DouiContentProvider.TODO_ITEMS_URI, null, selection,
						null, null);
		createJSONData(answer, SyncAdapter.JSON_UPDATED_TYPE_ITEMS);
		// ///////////////////////////////////////////////////////////////////////////////////////////
	}

	/**
	 * Generate json object from cursor
	 * 
	 * @param data
	 *            - cursor with data
	 * @param type
	 *            - type of object witch should be generated
	 */
	public void createJSONData(Cursor data, String type) {
		JSONObject keys = new JSONObject();
		JSONArray keysItems = new JSONArray();
		JSONObject updateObjectValues = new JSONObject();
		JSONArray updateObjectItems = new JSONArray();
		JSONObject currentObject = new JSONObject();
		int nItemsCount = 0;

		if (data != null) {
			try {
				keys.put(SyncAdapter.JSON_UPDATED_OBJECT_TYPE, type);
				updateObjectValues.put(SyncAdapter.JSON_UPDATED_OBJECT_TYPE,
						type);

				for (boolean flag = data.moveToFirst(); flag; flag = data
						.moveToNext()) {
					currentObject = new JSONObject();

					if (type.equals(SyncAdapter.JSON_UPDATED_TYPE_STATUS)) {
						currentObject
								.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY,
										data.getString(data
												.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY)) != null ? data.getString(data
												.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY))
												: JSONObject.NULL);
						currentObject
								.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP,
										data.getString(data
												.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_LAST_UPDATE)));
						currentObject
								.put(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME,
										data.getString(data
												.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME)));
						currentObject
								.put("client"
										+ TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID,
										data.getString(data
												.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID)));
					}

					if (type.equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES)) {
						currentObject
								.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY,
										data.getString(data
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY)) != null ? data.getString(data
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY))
												: JSONObject.NULL);
						currentObject
								.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP,
										data.getString(data
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_LAST_UPDATE)));
						currentObject
								.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
										data.getString(data
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME)));
						currentObject
								.put("client"
										+ TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID,
										data.getString(data
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID)));
						currentObject
								.put(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_IS_DELETED,
										data.getString(data
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_IS_DELETED)));
					}

					if (type.equals(SyncAdapter.JSON_UPDATED_TYPE_ITEMS)) {
						currentObject
								.put(SyncAdapter.JSON_UPDATED_OBJECT_KEY,
										data.getString(data
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY)) != null ? data.getString(data
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY))
												: JSONObject.NULL);
						currentObject
								.put(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP,
										data.getString(data
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_LAST_UPDATE)));
						currentObject
								.put("client"
										+ TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID,
										data.getString(data
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID)));
						currentObject
								.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
										data.getString(data
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY)));
						currentObject
								.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
										data.getString(data
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE)));

						if (data.getString(data
								.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY)) == null) {
							currentObject
									.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
											JSONObject.NULL);
						} else {
							String categotyObjectKey = getCategoryObjectKey(data
									.getInt(data
											.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY)));

							if (categotyObjectKey != null
									&& !categotyObjectKey.isEmpty()) {
								currentObject
										.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
												categotyObjectKey);
							} else {
								currentObject
										.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
												data.getString(data
														.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY)));
							}

						}

						if (data.getString(data
								.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS)) == null) {
							currentObject
									.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS,
											JSONObject.NULL);
						} else {
							String statusObjectKey = getStatusObjectKey(data
									.getInt(data
											.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS)));

							if (statusObjectKey != null
									&& !statusObjectKey.isEmpty()) {
								currentObject
										.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS,
												statusObjectKey);
							} else {
								currentObject
										.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS,
												data.getString(data
														.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS)));
							}

						}
					}

					if (currentObject.getString(
							SyncAdapter.JSON_UPDATED_OBJECT_KEY).equals("null")) {
						if (type == SyncAdapter.JSON_UPDATED_TYPE_ITEMS) {
							++nItemsCount;
							updateObjectItems.put(currentObject);
						} else {
							keysItems.put(currentObject);
						}
					} else {
						updateObjectItems.put(currentObject);
					}
				}

				updateObjectValues.put(SyncAdapter.JSON_UPDATED_OBJECT_VALUES,
						updateObjectItems);
				this.m_localData.getJSONArray(SyncAdapter.JSON_UPDATED_OBJECTS)
						.put(updateObjectValues);

				if (type == SyncAdapter.JSON_UPDATED_TYPE_ITEMS) {
					keys.put("itemsCount", nItemsCount);
				} else {
					keys.put(SyncAdapter.JSON_UPDATED_OBJECT_VALUES, keysItems);
				}
				this.m_newRecords
						.getJSONArray(SyncAdapter.JSON_UPDATED_OBJECTS).put(
								keys);

			} catch (JSONException e) {
				Log.v(TAG, "createJSONDataForServer failed");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Update local database with data from server
	 * 
	 * @param data
	 *            - json object with data from server
	 */
	private void updateLocalDatabase(JSONObject data) {

		if (data != null) {

			try {
				mLastUpdateDate = data
						.getString(SyncAdapter.JSON_LAST_UPDATE_TIMESTAMP);
				JSONArray dataFromServer = data
						.getJSONArray(JSON_UPDATED_OBJECTS);

				JSONArray statuses = new JSONArray();
				JSONArray categories = new JSONArray();
				JSONArray items = new JSONArray();
				for (int i = 0; i < dataFromServer.length(); ++i) {
					if (dataFromServer.getJSONObject(i)
							.get(JSON_UPDATED_OBJECT_TYPE)
							.equals(SyncAdapter.JSON_UPDATED_TYPE_STATUS)) {
						statuses = dataFromServer.getJSONObject(i)
								.getJSONArray(JSON_UPDATED_OBJECT_VALUES);
						continue;
					}

					if (dataFromServer.getJSONObject(i)
							.get(JSON_UPDATED_OBJECT_TYPE)
							.equals(SyncAdapter.JSON_UPDATED_TYPE_CATEGORIES)) {
						categories = dataFromServer.getJSONObject(i)
								.getJSONArray(JSON_UPDATED_OBJECT_VALUES);
						continue;
					}

					if (dataFromServer.getJSONObject(i)
							.get(JSON_UPDATED_OBJECT_TYPE)
							.equals(SyncAdapter.JSON_UPDATED_TYPE_ITEMS)) {
						items = dataFromServer.getJSONObject(i).getJSONArray(
								JSON_UPDATED_OBJECT_VALUES);
						continue;
					}
				}

				this.updateLocalStatuses(statuses);
				this.updateLocalCategories(categories);
				this.updateLocalItems(items);
				cleanDeletedCategories();

			} catch (JSONException e) {
				Log.v(TAG, "Data from server is not valid!!");
				e.printStackTrace();
			}
		}
	}

	/**
	 * Add field to set for update local database
	 * 
	 * @param fieldName
	 *            - name of field in db
	 * @param value
	 *            - JSON object with values
	 * @param valueName
	 *            - value name in json object. If null then fieldName will be
	 *            use as name in json object
	 */
	private void addFieldToUpdate(String fieldName, JSONObject value,
			String valueName) {
		String data;
		try {
			if (valueName != null) {
				data = value.getString(valueName);
			} else {
				data = value.getString(fieldName);
			}
			m_valuesForUpdate.put(fieldName, data);
		} catch (JSONException e) {
			Log.v(TAG, "Cannot add value for field " + fieldName);
			e.printStackTrace();
		}

	}

	/**
	 * This method gets status object key by his ID
	 * 
	 * @param nStatusID
	 *            status ID
	 * @return status object key or empty string if status not found
	 */
	private String getStatusObjectKey(int nStatusID) {
		String strResult = "";
		Cursor data;
		Uri uri = Uri.parse(DouiContentProvider.TODO_STATUSES_URI.toString()
				+ "/" + nStatusID);
		data = getContext().getContentResolver().query(uri, null, null, null,
				null);
		if (data != null && data.moveToFirst()) {
			strResult = data
					.getString(data
							.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY));
		}
		return strResult;

	}

	/**
	 * This method gets category object key by his ID
	 * 
	 * @param nCategoryID
	 *            category ID
	 * @return category object key or empty string if category not found.
	 */
	private String getCategoryObjectKey(int nCategoryID) {
		String strResult = "";
		Cursor data;
		Uri uri = Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI.toString()
				+ "/" + nCategoryID);
		data = getContext().getContentResolver().query(uri, null, null, null,
				null);
		if (data != null && data.moveToFirst()) {
			strResult = data
					.getString(data
							.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY));
		}
		return strResult;
	}

	/**
	 * This method gets category id by his object key
	 * 
	 * @param strObjectKey
	 *            category object key
	 * @return category ID or -1 if category not found
	 */
	private int getCategoryIDByObjectKey(String strObjectKey) {
		int nResult = -1;
		Cursor data;
		String strSelection = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY
				+ " = '" + strObjectKey + "'";
		data = getContext().getContentResolver().query(
				DouiContentProvider.TODO_CATEGORIES_URI, null, strSelection,
				null, null);
		if (data != null && data.moveToFirst()) {
			nResult = data
					.getInt(data
							.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID));
		}
		return nResult;
	}

	/**
	 * This method gets status id by his object_key
	 * 
	 * @param strObjectKey
	 *            status object key
	 * @return status id or -1 if status not found
	 */
	private int getStatusIDByObjectKey(String strObjectKey) {
		int nResult = -1;
		Cursor data;
		String strSelection = TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY
				+ " = '" + strObjectKey + "'";
		data = getContext().getContentResolver().query(
				DouiContentProvider.TODO_STATUSES_URI, null, strSelection,
				null, null);
		if (data != null && data.moveToFirst()) {
			nResult = data
					.getInt(data
							.getColumnIndex(TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID));
		}
		return nResult;
	}

	/**
	 * This method cleans categories marked as deleted
	 */
	private void cleanDeletedCategories() {
		String where = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_IS_DELETED
				+ " = 1";
		getContext().getContentResolver().delete(
				DouiContentProvider.TODO_CATEGORIES_URI, where, null);
	}

	private void updateLocalStatuses(JSONArray data) {
		Uri uriForUpdate;
		m_valuesForUpdate.clear();
		for (int i = 0; i < data.length(); ++i) {
			try {
				addFieldToUpdate(
						TableTodoStatusAdapter.TABLE_TODO_STATUSES_OBJECT_KEY,
						data.getJSONObject(i), JSON_UPDATED_OBJECT_KEY);
				addFieldToUpdate(
						TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME,
						data.getJSONObject(i), null);

				if (!data.getJSONObject(i).getString("client_id")
						.equals("null")) {

					uriForUpdate = Uri
							.parse(DouiContentProvider.TODO_STATUSES_URI
									.toString()
									+ "/"
									+ data.getJSONObject(i).getString(
											"client_id"));

					getContext().getContentResolver().update(uriForUpdate,
							m_valuesForUpdate, null, null);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void updateLocalCategories(JSONArray data) {
		String selection;
		Cursor localData;
		Uri uriForUpdate;

		m_valuesForUpdate.clear();
		for (int i = 0; i < data.length(); ++i) {
			try {
				addFieldToUpdate(
						TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_IS_DELETED,
						data.getJSONObject(i), null);
				addFieldToUpdate(
						TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY,
						data.getJSONObject(i), JSON_UPDATED_OBJECT_KEY);
				addFieldToUpdate(
						TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
						data.getJSONObject(i), null);

				if (data.getJSONObject(i).getString("client_id").equals("null")) {

					selection = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_OBJECT_KEY
							+ " = '"
							+ data.getJSONObject(i).getString(
									JSON_UPDATED_OBJECT_KEY) + "'";

					localData = getContext().getContentResolver().query(
							DouiContentProvider.TODO_CATEGORIES_URI, null,
							selection, null, null);

					if (localData != null && localData.moveToFirst()) {

						uriForUpdate = Uri
								.parse(DouiContentProvider.TODO_CATEGORIES_URI
										.toString()
										+ "/"
										+ localData.getInt(localData
												.getColumnIndex(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID)));

						getContext().getContentResolver().update(uriForUpdate,
								m_valuesForUpdate, null, null);

					} else {
						if (getContext().getContentResolver().insert(
								DouiContentProvider.TODO_CATEGORIES_URI,
								m_valuesForUpdate) == null) {

							Log.v(TAG,
									"Cannot insert new item for "
											+ DouiContentProvider.TODO_CATEGORIES_URI
													.toString());
						}
					}

				} else {
					uriForUpdate = Uri
							.parse(DouiContentProvider.TODO_CATEGORIES_URI
									.toString()
									+ "/"
									+ data.getJSONObject(i).getString(
											"client_id"));
					getContext().getContentResolver().update(uriForUpdate,
							m_valuesForUpdate, null, null);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void updateLocalItems(JSONArray data) {
		String selection;
		Cursor localData;
		Uri uriForUpdate;
		m_valuesForUpdate.clear();
		for (int i = 0; i < data.length(); ++i) {
			try {
				addFieldToUpdate(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
						data.getJSONObject(i), null);
				addFieldToUpdate(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
						data.getJSONObject(i), null);
				addFieldToUpdate(
						TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY,
						data.getJSONObject(i), JSON_UPDATED_OBJECT_KEY);

				if (!data
						.getJSONObject(i)
						.getString(
								TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY)
						.equals("null")) {

					int nCategoryId = getCategoryIDByObjectKey(data
							.getJSONObject(i)
							.getString(
									TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY));

					if (nCategoryId != -1) {
						m_valuesForUpdate
								.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
										nCategoryId);
					}
				}

				if (!data
						.getJSONObject(i)
						.getString(
								TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS)
						.equals("null")) {

					int nStatusId = getStatusIDByObjectKey(data
							.getJSONObject(i)
							.getString(
									TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS));
					if (nStatusId != -1) {
						m_valuesForUpdate
								.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS,
										nStatusId);
					}
				}

				if (data.getJSONObject(i).getString("client_id").equals("null")) {

					selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_OBJECT_KEY
							+ " = '"
							+ data.getJSONObject(i).getString(
									JSON_UPDATED_OBJECT_KEY) + "'";

					localData = getContext().getContentResolver().query(
							DouiContentProvider.TODO_ITEMS_URI, null,
							selection, null, null);

					if (localData != null && localData.moveToFirst()) {

						uriForUpdate = Uri
								.parse(DouiContentProvider.TODO_ITEMS_URI
										.toString()
										+ "/"
										+ localData.getInt(localData
												.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID)));

						getContext().getContentResolver().update(uriForUpdate,
								m_valuesForUpdate, null, null);

					} else {
						Uri itemUri = Uri
								.parse(DouiContentProvider.TODO_CATEGORIES_URI
										.toString()
										+ "/"
										+ getCategoryIDByObjectKey(data
												.getJSONObject(i)
												.getString(
														TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY))
										+ "/" + DouiContentProvider.TODO_PATH);

						if (getContext().getContentResolver().insert(itemUri,
								m_valuesForUpdate) == null) {

							Log.v(TAG,
									"Cannot insert new item for "
											+ DouiContentProvider.TODO_ITEMS_URI
													.toString());
						}
					}

				} else {
					uriForUpdate = Uri.parse(DouiContentProvider.TODO_ITEMS_URI
							.toString()
							+ "/"
							+ data.getJSONObject(i).getString("client_id"));

					getContext().getContentResolver().update(uriForUpdate,
							m_valuesForUpdate, null, null);
				}

			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}

}
