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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoListAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoListActivity extends ListActivity {
	private SimpleCursorAdapter adapter;
	private Uri todoUri;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_list_activity);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			todoUri = extras.getParcelable(DouiContentProvider.TODO_LISTS_PATH);
		}

		fillList();

		ImageButton imbtAddTodoItem = (ImageButton) findViewById(R.id.imbtAddTodoItem);
		final DouiTodoListActivity self = this;
		// TODO imbtAddTodoItem must be disabled in contexts view
		imbtAddTodoItem.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				Intent i = new Intent(self, DouiTodoItemEditActivity.class);
				// TODO check whether it is acceptable to use
				// DouiContentProvider.TODO_LISTS_PATH
				i.putExtra(DouiContentProvider.TODO_LISTS_PATH, todoUri);
				startActivity(i);
			}
		});

		List<String> pathSegments = todoUri.getPathSegments();

		// pathSegments.get(pathSegments.size() - 2) could be context or id of
		// todo list
		String itemListId = pathSegments.get(pathSegments.size() - 2);
		if (!itemListId.equals(DouiContentProvider.TODO_CONTEXTS_PATH)) {
			Uri uriList = Uri.parse("content://"
					+ DouiContentProvider.AUTHORITY + "/"
					+ DouiContentProvider.TODO_LISTS_PATH + "/" + itemListId);
			String listProperties[] = { TableTodoListAdapter.TABLE_TODO_LISTS_NAME };
			Cursor cursor = getContentResolver().query(uriList, listProperties,
					null, null, null);
			cursor.moveToFirst();
			String itemListName = cursor.getString(0);
			TextView tvCaption = (TextView) findViewById(R.id.tvListName);
			tvCaption.setText(itemListName);
			imbtAddTodoItem.setVisibility(View.VISIBLE);
		} else {
			imbtAddTodoItem.setVisibility(View.GONE);
			TextView tvCaption = (TextView) findViewById(R.id.tvListName);
			tvCaption.setText(todoUri.getLastPathSegment());

		}
	}

	private void fillList() {
		String[] from = new String[] { TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE };
		int[] to = new int[] { R.id.label };

		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(todoUri, null, null, null, null);

		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_row, cursor, from, to);
		setListAdapter(adapter);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);

		Cursor todoItemsCursor = (Cursor) l.getItemAtPosition(position);
		todoItemsCursor.moveToPosition(position);
		String idFkList = todoItemsCursor.getString(todoItemsCursor.getColumnIndex(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST)); 
		Intent i = new Intent(this, DouiTodoItemViewActivity.class);
		Uri todoItemUri = Uri.parse(DouiContentProvider.TODO_LISTS_URI.toString()
				+ "/"+idFkList+"/" + DouiContentProvider.TODO_PATH+ "/" + id);
		i.putExtra(DouiContentProvider.TODO_LISTS_PATH, todoItemUri);
		startActivity(i);
	}
}
