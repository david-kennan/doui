/**
 * 
 */
package com.github.davidkennan.doui.gui;

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
			todoUri = extras.getParcelable(DouiContentProvider.TODO_LISTS_PATH);
		}

		fillList();

		ImageButton imbtAddTodoItem = (ImageButton) findViewById(R.id.imbtAddTodoItem);
		final DouiTodoListActivity self = this;
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
		
		String itemListId = pathSegments.get(pathSegments.size() - 2);
		Uri uriList = Uri.parse("content://" + DouiContentProvider.AUTHORITY
				+ "/" + DouiContentProvider.TODO_LISTS_PATH + "/" + itemListId);
		String listProperties[] = {TableTodoListAdapter.TABLE_TODO_LISTS_NAME};
		Cursor cursor = getContentResolver().query(uriList, listProperties, null,
				null, null);
		cursor.moveToFirst();
		String itemListName = cursor.getString(0);
		TextView tvCaption = (TextView)findViewById(R.id.tvListName);
		tvCaption.setText(itemListName);
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
		Intent i = new Intent(this, DouiTodoItemViewActivity.class);
		Uri todoItemUri = Uri.parse(todoUri.toString() + "/" + id);
		i.putExtra(DouiContentProvider.TODO_LISTS_PATH, todoItemUri);
		startActivity(i);
	}
}
