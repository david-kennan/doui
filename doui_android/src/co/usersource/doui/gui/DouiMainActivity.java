package co.usersource.doui.gui;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoContextsAdapter;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

public class DouiMainActivity extends ListActivity {
	private SimpleCursorAdapter adapter;
	private Cursor cursorToDoCategories;
	private Cursor cursorStatuses;
	private Cursor cursorContexts;

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
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		int[] to = new int[] { R.id.label };

		ContentResolver cr = getContentResolver();
		cursorToDoCategories = cr
				.query(DouiContentProvider.TODO_CATEGORIES_URI, from, null,
						null, null);

		String statusProjection[] = {
				TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME,
				TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID };
		cursorStatuses = cr.query(DouiContentProvider.TODO_STATUSES_URI,
				statusProjection, null, null, null);

		String contextProjection[] = {
				TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_NAME,
				TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID };
		cursorContexts = cr.query(DouiContentProvider.TODO_CONTEXTS_URI,
				contextProjection, null, null, null);

		Cursor cursors[] = { cursorToDoCategories, cursorStatuses,
				cursorContexts };
		Cursor cursor = new MergeCursor(cursors);

		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_list_row, cursor, from, to);
		setListAdapter(adapter);
	}

	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		int categoriesCount = cursorToDoCategories.getCount();
		int statusesCount = cursorStatuses.getCount();
		int contextsCount = cursorContexts.getCount();

		Intent i = new Intent(this, DouiTodoListActivity.class);
		Uri todoUri = null;
		// Position from categories cursor.
		if (position < categoriesCount) {
			todoUri = Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI + "/"
					+ id + "/" + DouiContentProvider.TODO_PATH);
		}
		// position from statuses cursor.
		if (position >= categoriesCount
				&& position < categoriesCount + statusesCount) {
			todoUri = Uri.parse(DouiContentProvider.TODO_STATUSES_URI + "/"
					+ id + "/" + DouiContentProvider.TODO_PATH);
		}
		// position from contexts cursor.
		if (position >= categoriesCount + statusesCount
				&& position < categoriesCount + statusesCount + contextsCount) {
			Cursor mainCursor = (Cursor) l.getItemAtPosition(position);
			mainCursor.moveToPosition(position);
			String contextName = mainCursor.getString(0);
			todoUri = Uri.parse(DouiContentProvider.TODO_CONTEXTS_URI
					.toString() + "/" + contextName);
		}

		i.putExtra(DouiTodoListActivity.STR_TODO_LIST_URI_EXT, todoUri);
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