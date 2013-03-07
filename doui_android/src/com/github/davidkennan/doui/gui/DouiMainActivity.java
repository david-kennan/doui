package com.github.davidkennan.doui.gui;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import com.github.davidkennan.doui.DouiContentProvider;
import com.github.davidkennan.doui.R;
import com.github.davidkennan.doui.database.adapters.TableTodoListAdapter;

public class DouiMainActivity extends ListActivity {
	private SimpleCursorAdapter adapter;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fillList();

	}

	private void fillList() {
		String[] from = new String[] { TableTodoListAdapter.TABLE_TODO_LISTS_NAME, TableTodoListAdapter.TABLE_TODO_LISTS_ID};
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
		Uri todoUri = Uri.parse(DouiContentProvider.TODO_LISTS_URI + "/" + id+"/"+DouiContentProvider.TODO_PATH);
		// TODO check whether it is acceptable to use DouiContentProvider.TODO_LISTS_PATH 
		i.putExtra(DouiContentProvider.TODO_LISTS_PATH, todoUri);

		startActivity(i);
	}

	/* (non-Javadoc)
	 * @see android.app.ListActivity#onDestroy()
	 */
	@Override
	protected void onDestroy() {
		if(!adapter.getCursor().isClosed())
		{
			adapter.getCursor().close();
		}
		super.onDestroy();
	}
}