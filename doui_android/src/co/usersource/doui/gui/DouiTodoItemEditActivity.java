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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
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
 *         Todo item editor activity.
 * 
 */
public class DouiTodoItemEditActivity extends Activity {

	/** Id for extras used to store URI for todo item. */
	public static final String STR_TODO_ITEM_URI_EXT = "STR_TODO_ITEM_URI_EXT";

	/** Name for the category to be set by default. */
	private static final String STR_DEFAULT_CATEGORY_NAME = "-None-";

	private Uri itemUri;
	private Button btCancel;
	private Button btSave;
	private String itemId;

	private String itemTitle = "";
	private String itemBody = "";
	private String itemCategoryId;

	/**
	 * @return the itemTitle
	 */
	public String getItemTitle() {
		return itemTitle;
	}

	/**
	 * @param itemTitle
	 *            the itemTitle to set
	 */
	public void setItemTitle(String itemTitle) {
		this.itemTitle = itemTitle;
	}

	/**
	 * @return the itemBody
	 */
	public String getItemBody() {
		return itemBody;
	}

	/**
	 * @param itemBody
	 *            the itemBody to set
	 */
	public void setItemBody(String itemBody) {
		this.itemBody = itemBody;
	}

	/**
	 * @return the itemCategoryId
	 */
	public String getItemCategoryId() {
		return itemCategoryId;
	}

	/**
	 * @param itemCategoryId
	 *            the itemCategoryId to set
	 */
	public void setItemCategoryId(String itemCategoryId) {
		this.itemCategoryId = itemCategoryId;
	}

	private String itemCategoryName = "";
	private String itemStatusId;
	private String itemStatusName = "";

	private TextView tvTodoListName;
	private EditText etTodoItemTitle;
	private EditText etTodoItemBody;

	private int uriMatch;

	private TextView tvTodoContexts;

	private TextView tvSecondListName;

	private ImageButton imbtSetDone;

	private ImageButton imbtSetList;

	private LinearLayout llMain;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_item_edit);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			itemUri = extras.getParcelable(STR_TODO_ITEM_URI_EXT);
		}
		uriMatch = DouiContentProvider.sURIMatcher.match(itemUri);
		this.loadToDoItemProperties();
		this.initUiControls();

	}

	/**
	 * Loads item properties to internal variables using current itemUri.
	 * itemUri member must contain URI for concrete item.
	 * */
	private void loadToDoItemFromUri() {
		// Load primary properties.
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
		this.loadCategoryById(itemCategoryId);
		this.loadStatusById(itemStatusId);
	}

	/**
	 * Utility function to load Category properties by existing id. Updates
	 * local category fields.
	 * */
	private void loadCategoryById(String itemCategoryId) {
		if (itemCategoryId != null) {
			// Load category name
			Uri uriList = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_CATEGORIES_PATH + "/"
					+ itemCategoryId);
			String listProperties[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME };
			Cursor cursor = getContentResolver().query(uriList, listProperties,
					null, null, null);
			cursor.moveToFirst();
			this.itemCategoryId = itemCategoryId;
			itemCategoryName = cursor.getString(0);
			cursor.close();
		}
	}

	/**
	 * Utility function to load Category properties by existing name. Updates
	 * local category fields.
	 * */
	private void loadCategoryByName(String itemCategoryName) {
		if (itemCategoryName != null && !itemCategoryName.equals("")) {
			// Load category name
			Uri uriList = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_CATEGORIES_PATH);
			String listProperties[] = {
					TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID,
					TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME };
			String selection = TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME
					+ "=?";
			String selectionArgs[] = { itemCategoryName };
			Cursor cursor = getContentResolver().query(uriList, listProperties,
					selection, selectionArgs, null);
			cursor.moveToFirst();
			this.itemCategoryId = cursor.getString(0);
			this.itemCategoryName = cursor.getString(1);
			cursor.close();
		}
	}

	/**
	 * Utility function to load Status properties by existing id. Updates local
	 * status fields.
	 * */
	private void loadStatusById(String itemStatusId) {
		if (itemStatusId != null) {
			// Load status name
			Uri uriStatus = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_STATUSES_PATH + "/"
					+ itemStatusId);
			String statusProperties[] = { TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME };
			Cursor cursor = getContentResolver().query(uriStatus,
					statusProperties, null, null, null);
			cursor.moveToFirst();
			this.itemStatusId = itemStatusId;
			itemStatusName = cursor.getString(0);
			cursor.close();
		}

	}

	/**
	 * Utility function to load item properties from current URI.
	 * */
	private void loadToDoItemProperties() {
		switch (uriMatch) {
		case DouiContentProvider.TODO_CATEGORYS_ITEM_URI_ID:
		case DouiContentProvider.TODO_STATUS_ITEM_URI_ID:
			this.loadToDoItemFromUri();
			break;
		case DouiContentProvider.TODO_CATEGORY_LIST_URI_ID:
			List<String> pathSegments = itemUri.getPathSegments();
			String itemCategoryId = pathSegments.get(pathSegments.size() - 2);
			this.loadCategoryById(itemCategoryId);
			// Default status is null.
			break;
		case DouiContentProvider.TODO_STATUS_LIST_URI_ID:
			List<String> statusPathSegments = itemUri.getPathSegments();
			String itemStatusId = statusPathSegments.get(statusPathSegments
					.size() - 2);
			this.loadStatusById(itemStatusId);
			this.loadCategoryByName(STR_DEFAULT_CATEGORY_NAME);
			break;
		default:
			Log.e(this.getClass().getName(), "Unknown URI for edit Activity: "
					+ itemUri);
		}

	}

	/**
	 * Utility function to set control values with values retrieved from current
	 * URI.
	 * */
	private void initUiControls() {
		llMain = (LinearLayout) findViewById(R.id.llMain);
		etTodoItemTitle = (EditText) findViewById(R.id.etTodoItemTitle);
		etTodoItemBody = (EditText) findViewById(R.id.etTodoItemBody);
		tvTodoListName = (TextView) findViewById(R.id.tvListName);
		etTodoItemTitle.setText(itemTitle);
		etTodoItemTitle.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {
				itemTitle = etTodoItemTitle.getText().toString();
				DouiTodoItemEditActivity.this.showActionBarTop(true);
			}
		});
		etTodoItemBody.setText(itemBody);
		etTodoItemBody.addTextChangedListener(new TextWatcher() {

			public void onTextChanged(CharSequence s, int start, int before,
					int count) {

			}

			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {

			}

			public void afterTextChanged(Editable s) {
				itemBody = etTodoItemBody.getText().toString();
				DouiTodoItemEditActivity.this.showActionBarTop(true);
			}
		});

		this.createSecondListControl();

		switch (uriMatch) {
		case DouiContentProvider.TODO_CATEGORYS_ITEM_URI_ID:
			tvTodoListName.setText(itemCategoryName);
			tvSecondListName.setVisibility(View.GONE);
			break;
		case DouiContentProvider.TODO_STATUS_ITEM_URI_ID:
			tvTodoListName.setText(itemStatusName);
			tvSecondListName.setVisibility(View.VISIBLE);
			tvSecondListName.setText(itemCategoryName);
			break;
		case DouiContentProvider.TODO_CATEGORY_LIST_URI_ID:
			tvTodoListName.setText(itemCategoryName);
			tvSecondListName.setVisibility(View.GONE);
			break;
		case DouiContentProvider.TODO_STATUS_LIST_URI_ID:
			tvTodoListName.setText(itemStatusName);
			tvSecondListName.setVisibility(View.VISIBLE);
			tvSecondListName.setText(itemCategoryName);
			break;
		default:
			Log.e(this.getClass().getName(), "Unknown URI for edit Activity: "
					+ itemUri);
		}

		tvTodoContexts = (TextView) findViewById(R.id.tvTodoContexts);
		Pattern contextPattern = Pattern.compile("@(\\w*)");
		Matcher contextMatcher = contextPattern.matcher(itemBody);
		String strContexts = "";
		while (contextMatcher.find()) {
			strContexts += contextMatcher.group(0) + " ";
		}
		if (!strContexts.equals("")) {
			tvTodoContexts.setText(strContexts);
			tvTodoContexts.setVisibility(View.VISIBLE);
		} else {
			tvTodoContexts.setVisibility(View.GONE);
		}

		this.createActionBarTop();
		this.createStatusActionBar();
	}

	/**
	 * Utility function create second list name control. This control is active
	 * only for category name and have popup, which allow to select category.
	 * */
	private void createSecondListControl() {
		tvSecondListName = (TextView) findViewById(R.id.tvSecondListName);
		tvSecondListName.setOnClickListener(new OnClickListener() {
			TodoListPopupWindow popup;

			public void onClick(View v) {
				Uri uriList = Uri.parse("content://"
						+ DouiContentProvider.AUTHORITY + "/"
						+ DouiContentProvider.TODO_CATEGORIES_PATH);
				String[] projection = {
						TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
						TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };

				if (popup == null || !popup.isShowing()) {
					if (popup != null) {
						popup.dismiss();
					}
					popup = new TodoListPopupWindow(getApplicationContext(),
							uriList, "Change Category", projection, null, null);
					popup.getLvTodoLists().setOnItemClickListener(
							new OnItemClickListener() {

								public void onItemClick(AdapterView<?> arg0,
										View arg1, int position, long id) {
									loadCategoryById(new Long(id).toString());
									tvSecondListName.setText(itemCategoryName);
									DouiTodoItemEditActivity.this
											.showActionBarTop(true);
									popup.dismiss();
								}
							});
					popup.showAtLocation(tvSecondListName, Gravity.TOP
							| Gravity.LEFT, 0, 0);
					int location[] = { 0, 0 };
					tvSecondListName.getLocationOnScreen(location);
					popup.update(location[0],
							location[1] + tvSecondListName.getHeight(), 300,
							200);

				} else {
					popup.dismiss();
				}
			}
		});
	}

	private void showActionBarTop(boolean isVisible) {
		LinearLayout llActionBarTop = (LinearLayout) findViewById(R.id.llActionBarTop);
		if (isVisible) {
			llActionBarTop.setVisibility(View.VISIBLE);
		} else {
			llActionBarTop.setVisibility(View.GONE);
		}
	}

	/**
	 * Utility function to create action bar at the top of the screen.
	 * */
	private void createActionBarTop() {
		this.showActionBarTop(false);
		// Actionbar buttons.
		btCancel = (Button) findViewById(R.id.imbtCancel);
		btCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
		btSave = (Button) findViewById(R.id.imbtSave);
		btSave.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				if (!itemTitle.equals("")) {
					ContentValues values = new ContentValues();
					values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
							itemTitle);
					values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
							itemBody);

					values.put(
							TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
							itemCategoryId);
					values.put(
							TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS,
							itemStatusId);

					if (null == itemId) {
						itemUri = getContentResolver().insert(itemUri, values);
					} else {
						String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
								+ "=?";
						String selectionArgs[] = { itemId };
						getContentResolver().update(itemUri, values, selection,
								selectionArgs);
					}
					Toast toast = Toast.makeText(getApplicationContext(),
							"Item saved", Toast.LENGTH_SHORT);
					toast.show();
					finish();
				} else {
					Toast toast = Toast.makeText(getApplicationContext(),
							"Can't save item without title!",
							Toast.LENGTH_SHORT);
					toast.show();

				}
			}
		});
	}

	/** Utility function to create Action bar with "Done" and "Set status buttons". */
	private void createStatusActionBar() {
		imbtSetDone = (ImageButton) findViewById(R.id.imbtDone);
		imbtSetDone.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				String doneStatusId = getStatusIdByName(TableTodoStatusAdapter.STR_DONE_STATUS_NAME);
				if (null != doneStatusId) {
					setItemStatus(doneStatusId);
				}
			}
		});

		imbtSetList = (ImageButton) findViewById(R.id.imbtSetList);
		imbtSetList.setOnClickListener(new OnClickListener() {
			TodoListPopupWindow popup;

			private int getListHeight() {
				int itemCount = popup.getCursor().getCount();
				ListAdapter mAdapter = popup.getLvTodoLists().getAdapter();

				int listviewElementsHeight = 0;
				for (int i = 0; i < itemCount; i++) {

					View childView = mAdapter.getView(i, null,
							popup.getLvTodoLists());
					childView.measure(MeasureSpec.makeMeasureSpec(0,
							MeasureSpec.UNSPECIFIED), MeasureSpec
							.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
					listviewElementsHeight += childView.getMeasuredHeight();
				}

				return listviewElementsHeight;

			}

			public void onClick(View v) {
				Uri uriList = Uri.parse("content://"
						+ DouiContentProvider.AUTHORITY + "/"
						+ DouiContentProvider.TODO_STATUSES_PATH);
				String[] projection = {
						TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME,
						TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID };

				List<String> args = new ArrayList<String>();
				String skipItem = TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME
						+ " <> ? ";
				args.add(TableTodoStatusAdapter.STR_DONE_STATUS_NAME);
				if (itemStatusId != null) {
					skipItem += "and "
							+ TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID
							+ " <> ?";
					args.add(itemStatusId);
				}
				String[] skipItemArg = args.toArray(new String[args.size()]);
				if (popup == null || !popup.isShowing()) {
					if (popup != null) {
						popup.dismiss();
					}
					popup = new TodoListPopupWindow(getApplicationContext(),
							uriList, null, projection, skipItem, skipItemArg);
					popup.getLvTodoLists().setOnItemClickListener(
							new OnItemClickListener() {

								public void onItemClick(AdapterView<?> arg0,
										View arg1, int position, long id) {
									setItemStatus(new Long(id).toString());
									popup.dismiss();
								}
							});
					int popupHeight = this.getListHeight();
					popup.showAtLocation(llMain, Gravity.RIGHT | Gravity.TOP,
							0, 0);
					int location[] = { 0, 0 };
					imbtSetList.getLocationOnScreen(location);
					popup.update(0, location[1] - popupHeight, 300, popupHeight);
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

	/** Utility function to set item status by given status Id */
	private void setItemStatus(String statusId) {
		ContentValues values = new ContentValues();
		values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS, statusId);

		String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID + "=?";
		String selectionArgs[] = { itemId };
		getContentResolver().update(itemUri, values, selection, selectionArgs);
		itemStatusId = statusId;
		Toast toast = Toast.makeText(getApplicationContext(), "Status set",
				Toast.LENGTH_SHORT);
		toast.show();
		// refreshTodoItemData();
	}

}
