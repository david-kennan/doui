/**
 * 
 */
package com.github.davidkennan.doui.gui;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.SimpleCursorAdapter;

import com.github.davidkennan.doui.DouiContentProvider;
import com.github.davidkennan.doui.R;
import com.github.davidkennan.doui.database.adapters.TableTodoItemsAdapter;
import com.github.davidkennan.doui.database.adapters.TableTodoListAdapter;

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
	        todoUri = extras
	            .getParcelable(DouiContentProvider.TODO_LISTS_PATH);
	      }
	    
		fillList();

	}

	private void fillList() {
		String[] from = new String[] { TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE };
		int[] to = new int[] { R.id.label };

		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(todoUri, null,
				null, null, null);

		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_list_row, cursor, from, to);
		setListAdapter(adapter);
	}

/*	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		Intent i = new Intent(this, TodoDetailActivity.class);
		Uri todoUri = Uri.parse(MyTodoContentProvider.CONTENT_URI + "/" + id);
		i.putExtra(MyTodoContentProvider.CONTENT_ITEM_TYPE, todoUri);

		startActivity(i);
	}*/
}
