/**
 * 
 */
package co.usersource.doui.gui;

import java.util.List;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

/**
 * @author rsh This activity used to show a list of todo items. This activity
 *         can show todo items from different projections: - context - category
 *         - status
 */
public class DouiTodoListActivity extends ListActivity {
	/**
	 * Identifier used to pass todoListUri to this activity.
	 * */
	public static final String STR_TODO_LIST_URI_EXT = "STR_TODO_LIST_URI_EXT";

	private SimpleCursorAdapter adapter;
	private Uri todoListUri;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_list_activity);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			todoListUri = extras.getParcelable(STR_TODO_LIST_URI_EXT);
		}

		fillList();

		ImageButton imbtAddTodoItem = (ImageButton) findViewById(R.id.imbtAddTodoItem);
		final DouiTodoListActivity self = this;
		// TODO imbtAddTodoItem must be disabled in contexts view
		imbtAddTodoItem.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(self, DouiTodoItemEditActivity.class);
				i.putExtra(DouiTodoItemEditActivity.STR_TODO_ITEM_URI_EXT,
						todoListUri);
				startActivity(i);
			}
		});

		String listId;
		int uriMatchId = DouiContentProvider.sURIMatcher.match(todoListUri);
		switch (uriMatchId) {
		case DouiContentProvider.TODO_CATEGORY_LIST_URI_ID:
			listId = getListIdFromPath();
			Uri uriCategory = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_CATEGORIES_PATH + "/" + listId);
			String listCategoryProperties[] = { TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME };
			Cursor cursorCategory = getContentResolver().query(uriCategory,
					listCategoryProperties, null, null, null);
			cursorCategory.moveToFirst();
			this.setCaption(cursorCategory.getString(0));
			imbtAddTodoItem.setVisibility(View.VISIBLE);
			break;
		case DouiContentProvider.TODO_STATUS_LIST_URI_ID:
			listId = getListIdFromPath();
			Uri uriStatus = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_STATUSES_PATH + "/" + listId);
			String listStatusProperties[] = { TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME };
			Cursor cursorStatus = getContentResolver().query(uriStatus,
					listStatusProperties, null, null, null);
			cursorStatus.moveToFirst();
			this.setCaption(cursorStatus.getString(0));
			imbtAddTodoItem.setVisibility(View.VISIBLE);
			break;
		case DouiContentProvider.TODO_CONTEXT_LIST_URI_ID:
			imbtAddTodoItem.setVisibility(View.GONE);
			this.setCaption(todoListUri.getLastPathSegment());
			break;
		default:
			Log.e(this.getClass().getName(),
					"Unknown URI given to build list: " + todoListUri);
		}

	}

	/**
	 * Utility function to get list Id from URI. Work for categories and
	 * statuses.
	 */
	private String getListIdFromPath() {
		List<String> pathSegments = todoListUri.getPathSegments();
		return pathSegments.get(pathSegments.size() - 2);
	}

	/** Utility function to set activity caption. */
	private void setCaption(String caption) {
		TextView tvCaption = (TextView) findViewById(R.id.tvListName);
		tvCaption.setText(caption);
	}

	private void fillList() {
		String[] from = new String[] { TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE };
		int[] to = new int[] { R.id.label };

		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(todoListUri, null, null, null, null);

		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_row, cursor, from, to);
		setListAdapter(adapter);
	}

	@Override
	protected void onRestart() {
		fillList();
		super.onRestart();
	}

	// TODO What must be displayed if we come from context?
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor todoItemsCursor = (Cursor) l.getItemAtPosition(position);
		todoItemsCursor.moveToPosition(position);
		String pathStart = "";
		String idFkList = "";

		int uriMatchId = DouiContentProvider.sURIMatcher.match(todoListUri);
		switch (uriMatchId) {
		case DouiContentProvider.TODO_CATEGORY_LIST_URI_ID:
		case DouiContentProvider.TODO_CONTEXT_LIST_URI_ID:
			pathStart = DouiContentProvider.TODO_CATEGORIES_URI.toString();
			idFkList = todoItemsCursor
					.getString(todoItemsCursor
							.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_CATEGORY));
			break;
		case DouiContentProvider.TODO_STATUS_LIST_URI_ID:
			pathStart = DouiContentProvider.TODO_STATUSES_URI.toString();
			idFkList = todoItemsCursor
					.getString(todoItemsCursor
							.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_STATUS));
			break;
		default:
			Log.e(this.getClass().getName(),
					"Unknown URI given to build list: " + todoListUri);
		}

		Intent i = new Intent(this, DouiTodoItemViewActivity.class);
		Uri todoItemUri = Uri.parse(pathStart + "/" + idFkList + "/"
				+ DouiContentProvider.TODO_PATH + "/" + id);
		i.putExtra(DouiTodoItemViewActivity.STR_TODO_ITEM_URI_EXT, todoItemUri);
		startActivity(i);
	}
}
