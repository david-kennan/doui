/**
 * 
 */
package co.usersource.doui.gui;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoItemEditActivity extends Activity {

	/** Id for extras used to store URI for todo item. */
	public static final String STR_TODO_ITEM_URI_EXT = "STR_TODO_ITEM_URI_EXT";

	private Uri itemUri;
	private ImageButton imbtCancel;
	private ImageButton imbtSave;
	private String itemId;
	
	private String itemTitle = "";
	private String itemBody = "";
	private String itemCategoryId;
	private String itemCategoryName = "";
	private String itemStatusId;
	private String itemStatusName = "";

	
	private TextView tvTodoListName;
	private EditText etTodoItemTitle;
	private EditText etTodoItemBody;


	private int uriMatch;

	private TextView tvTodoContexts;

	private TextView tvSecondListName;

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

		// TODO Restore this
		/*imbtSave = (ImageButton) findViewById(R.id.imbtSave);
		imbtSave.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ContentValues values = new ContentValues();
				values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
						((EditText) findViewById(R.id.etTodoItemTitle))
								.getText().toString());
				values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
						((EditText) findViewById(R.id.etTodoItemBody))
								.getText().toString());

				List<String> pathSegments = itemUri.getPathSegments();

				if (DouiContentProvider.TODO_PATH.equals(pathSegments
						.get(pathSegments.size() - 1))) {
					values.put(
							TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
							pathSegments.get(pathSegments.size() - 2));

				} else {
					values.put(
							TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY,
							pathSegments.get(pathSegments.size() - 3));
				}

				if (itemUri.getLastPathSegment().equals(
						DouiContentProvider.TODO_PATH)) {
					itemUri = getContentResolver().insert(itemUri, values);
				} else {
					String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
							+ "=?";
					String selectionArgs[] = { itemUri.getLastPathSegment() };
					getContentResolver().update(itemUri, values, selection,
							selectionArgs);
				}
				Toast toast = Toast.makeText(getApplicationContext(),
						"Item saved", Toast.LENGTH_SHORT);
				toast.show();
				finish();
			}
		});*/
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
			// TODO set default status here
			break;
		case DouiContentProvider.TODO_STATUS_LIST_URI_ID:
			List<String> statusPathSegments = itemUri.getPathSegments();
			String itemStatusId = statusPathSegments.get(statusPathSegments
					.size() - 2);
			this.loadStatusById(itemStatusId);
			// TODO set default category here
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
		etTodoItemTitle = (EditText) findViewById(R.id.etTodoItemTitle);
		etTodoItemBody = (EditText) findViewById(R.id.etTodoItemBody);
		tvTodoListName = (TextView) findViewById(R.id.tvListName);
		tvSecondListName = (TextView) findViewById(R.id.tvSecondListName);

		etTodoItemTitle.setText(itemTitle);
		etTodoItemBody.setText(itemBody);

		switch (uriMatch) {
		case DouiContentProvider.TODO_CATEGORYS_ITEM_URI_ID:
			tvTodoListName.setText(itemCategoryName);
			tvSecondListName.setText(itemStatusName);
			break;
		case DouiContentProvider.TODO_STATUS_ITEM_URI_ID:
			tvTodoListName.setText(itemStatusName);
			tvSecondListName.setText(itemCategoryName);
			break;
		case DouiContentProvider.TODO_CATEGORY_LIST_URI_ID:
			tvTodoListName.setText(itemCategoryName);
			tvSecondListName.setText(itemStatusName);
			break;
		case DouiContentProvider.TODO_STATUS_LIST_URI_ID:
			tvTodoListName.setText(itemStatusName);
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
		tvTodoContexts.setText(strContexts);

		// Actionbar buttons.
		imbtCancel = (ImageButton) findViewById(R.id.imbtCancel);
		imbtCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});

	}
}
