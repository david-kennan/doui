/**
 * 
 */
package co.usersource.doui.gui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoItemViewActivity extends Activity {

	/** Id for extras used to store URI for todo item. */
	public static final String STR_TODO_ITEM_URI_EXT = "STR_TODO_ITEM_URI_EXT";
	
	/** Name for status done */
	private static final String STR_DONE_STATUS_NAME = "Done";

	private Uri itemUri;
	private String itemId;
	private String itemBody;
	private String itemTitle;

	private String itemCategoryId;
	private String itemCategoryName;
	private String itemStatusId;
	private String itemStatusName;

	private TextView tvTodoItemTitle;
	private TextView tvTodoItemBody;
	private TextView tvTodoListName;
	private ImageButton imbtEdit;
	private ImageButton imbtSetList;
	private LinearLayout llMain;
	private ImageButton imbtSetDone;

	private TextView tvTodoContexts;

	private TextView tvTodoSecondList ;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_item_view);
		
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			itemUri = extras.getParcelable(STR_TODO_ITEM_URI_EXT);
		}
		this.refreshTodoItemData();

		imbtEdit = (ImageButton) findViewById(R.id.imbtEdit);
		final DouiTodoItemViewActivity self = this;
		imbtEdit.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(self, DouiTodoItemEditActivity.class);
				i.putExtra(DouiTodoItemEditActivity.STR_TODO_ITEM_URI_EXT, itemUri);
				startActivity(i);
			}
		});
	}

	private void refreshTodoItemData() {
		// Load item data
		String[] projection = { TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS };
		Cursor cursor = getContentResolver().query(itemUri, projection, null,
				null, null);
		cursor.moveToFirst();
		itemId = cursor.getString(0);
		itemTitle = cursor.getString(1);
		itemBody = cursor.getString(2);
		itemCategoryId = cursor.getString(3);
		itemStatusId = cursor.getString(4);

		cursor.close();

		// Get item category
		if (null != itemCategoryId) {
			Uri uriCategory = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_CATEGORIES_PATH + "/"
					+ itemCategoryId);
			String listProperties[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME };
			cursor = getContentResolver().query(uriCategory, listProperties, null,
					null, null);
			cursor.moveToFirst();
			itemCategoryName = cursor.getString(0);
			cursor.close();
		}
		
		// Get item status
		if (null != itemStatusId) {
			Uri uriStatus = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_STATUSES_PATH + "/"
					+ itemStatusId);
			String statusProperties[] = { TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME };
			cursor = getContentResolver().query(uriStatus, statusProperties,
					null, null, null);
			cursor.moveToFirst();
			itemStatusName = cursor.getString(0);
			cursor.close();
		}
		
		this.populateControls();
	}

	/** Utility function to update controls on the activity. */
	private void populateControls() {
		llMain = (LinearLayout) findViewById(R.id.llMain);
		tvTodoItemTitle = (TextView) findViewById(R.id.tvTodoTitle);
		tvTodoItemTitle.setText(itemTitle);
		tvTodoItemBody = (TextView) findViewById(R.id.tvTodoBody);
		tvTodoItemBody.setText(itemBody);

		tvTodoListName = (TextView) findViewById(R.id.tvListName);
		tvTodoSecondList = (TextView) findViewById(R.id.tvTodoSecondList);

		int uriMatchId = DouiContentProvider.sURIMatcher.match(itemUri);
		switch (uriMatchId) {
		case DouiContentProvider.TODO_STATUS_ITEM_URI_ID:
			tvTodoListName.setText(itemStatusName);
			tvTodoSecondList.setVisibility(View.VISIBLE);
			tvTodoSecondList.setText(itemCategoryName);
			break;
		case DouiContentProvider.TODO_CATEGORYS_ITEM_URI_ID:
			tvTodoListName.setText(itemCategoryName);
			tvTodoSecondList.setVisibility(View.GONE);
			break;
		default:
			break;
		}
		
		tvTodoContexts = (TextView) findViewById(R.id.tvTodoContexts);
		Pattern contextPattern = Pattern.compile("@(\\w*)");
		Matcher contextMatcher = contextPattern.matcher(itemBody);
		String strContexts = "";
		while (contextMatcher.find()) {
			strContexts+=contextMatcher.group(0)+" ";
		}
		if(!strContexts.equals(""))
		{
			tvTodoContexts.setText(strContexts);
			tvTodoContexts.setVisibility(View.VISIBLE);
		}else
		{
			tvTodoContexts.setVisibility(View.GONE);
		}
		
		imbtSetDone = (ImageButton) findViewById(R.id.imbtDone);
		imbtSetDone.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				String doneStatusId = getStatusIdByName(STR_DONE_STATUS_NAME);
				if(null!=doneStatusId)
				{
					setItemStatus(doneStatusId);
				}
			}
		});
		
		imbtSetList = (ImageButton) findViewById(R.id.imbtSetList);
		imbtSetList.setOnClickListener(new OnClickListener() {
			TodoListPopupWindow popup;

			public void onClick(View v) {
				Uri uriList = Uri.parse("content://"
						+ DouiContentProvider.AUTHORITY + "/"
						+ DouiContentProvider.TODO_STATUSES_PATH);
				String[] projection = {
						TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME,
						TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID };

				List<String> args = new ArrayList<String>();
				String skipItem = TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME + " <> ? ";
				args.add(STR_DONE_STATUS_NAME);
				if(itemStatusId != null)
				{
					skipItem += "and "+TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID+" <> ?";
					args.add(itemStatusId);
				}
				String[] skipItemArg = args.toArray(new String[args.size()]);
				if (popup == null || !popup.isShowing()) {
					if (popup != null) {
						popup.dismiss();
					}
					popup = new TodoListPopupWindow(getApplicationContext(),
							uriList, "Set status", projection, skipItem, skipItemArg);
					popup.getLvTodoLists().setOnItemClickListener(
							new OnItemClickListener() {

								public void onItemClick(AdapterView<?> arg0,
										View arg1, int position, long id) {
									setItemStatus(new Long(id).toString());
									popup.dismiss();
								}
							});

					popup.showAtLocation(llMain, Gravity.RIGHT | Gravity.TOP,
							0, 0);
					int location[] = { 0, 0 };
					imbtSetList.getLocationOnScreen(location);
					popup.update(0, location[1] - 200, 300, 200);
				} else {
					popup.dismiss();
				}
			}
		});
	}
	/**
	 * Utility function to load Category properties by existing name. Updates
	 * local category fields.
	 * */
	private String getStatusIdByName(String itemStatusName) {
		String result = null;
		if (itemStatusName != null && !itemStatusName.equals("")) {
			// Load category name
			Uri uriList = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_STATUSES_PATH);
			String listProperties[] = {
					TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID,
					TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME };
			String selection = TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME
					+ "=?";
			String selectionArgs[] = { itemStatusName };
			Cursor cursor = getContentResolver().query(uriList, listProperties,
					selection, selectionArgs, null);
			cursor.moveToFirst();
			result = cursor.getString(0);
			cursor.close();
		}
		return result;
	}
	
	private void setItemStatus(String statusId)
	{
		ContentValues values = new ContentValues();
		values.put(
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS,
				statusId);

		String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
				+ "=?";
		String selectionArgs[] = { itemId };
		getContentResolver().update(itemUri, values, selection,
				selectionArgs);
		itemStatusId = statusId;
		Toast toast = Toast.makeText(getApplicationContext(), "Status set", Toast.LENGTH_SHORT);
		toast.show();
		refreshTodoItemData();

	}
	
	@Override
	protected void onRestart() {
		this.refreshTodoItemData();
		super.onRestart();
	}

}
