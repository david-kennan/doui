package co.usersource.doui.gui;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoListAdapter;

public class DouiMainActivity extends ListActivity {
	private SimpleCursorAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fillList();

	}


	@Override
	protected void onRestart() {
		fillList();
		super.onRestart();
	}


	private void fillList() {
		String[] from = new String[] {
				TableTodoListAdapter.TABLE_TODO_LISTS_NAME,
				TableTodoListAdapter.TABLE_TODO_LISTS_ID };
		int[] to = new int[] { R.id.label };

		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(DouiContentProvider.TODO_LISTS_URI, null,
				null, null, null);

		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_list_row, cursor, from, to);
		setListAdapter(adapter);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, DouiTodoListActivity.class);
		Uri todoUri = null;
		if (id > -1) {
			todoUri = Uri.parse(DouiContentProvider.TODO_LISTS_URI + "/" + id
					+ "/" + DouiContentProvider.TODO_PATH);
		} else {
			Cursor contextsCursor = (Cursor) l.getItemAtPosition(position);
			contextsCursor.moveToPosition(position);
			String contextName = contextsCursor.getString(1);
			todoUri = Uri.parse(DouiContentProvider.TODO_CONTEXTS_URI
					.toString() + "/" + contextName);
		}
		// TODO check whether it is acceptable to use
		// DouiContentProvider.TODO_LISTS_PATH
		i.putExtra(DouiContentProvider.TODO_LISTS_PATH, todoUri);

		startActivity(i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if (!adapter.getCursor().isClosed()) {
			adapter.getCursor().close();
		}
		super.onDestroy();
	}
}