package co.usersource.doui.gui;

import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoCategoriesAdapter;
import co.usersource.doui.database.adapters.TableTodoContextsAdapter;
import co.usersource.doui.database.adapters.TableTodoStatusAdapter;

public class DouiMainActivity extends ListActivity {
	private SimpleCursorAdapter adapter;
	private Cursor cursorToDoCategories;
	private Cursor cursorStatuses;
	private Cursor cursorContexts;
	private ImageButton imbtCategories;
	private MergeCursor mergeCursor;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		fillList();
		imbtCategories = (ImageButton)findViewById(R.id.imbtCategories);
		imbtCategories.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(DouiMainActivity.this, DouiTodoCategoriesManagerActivity.class);
				startActivity(i);
			}
		});
	}

	@Override
	protected void onRestart() {
		fillList();
		super.onRestart();
	}

	private void fillList() {
		String[] from = new String[] {
				"img_id",
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
				"TABLE_NAME",
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		int[] to = new int[] { R.id.icon, R.id.label };

		ContentResolver cr = getContentResolver();
		String categoryProjection[] = {
				R.drawable.ic_category + " as img_id",
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_NAME,
				"'"+TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES+"' as TABLE_NAME",
				TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES_ID };
		cursorToDoCategories = cr
				.query(DouiContentProvider.TODO_CATEGORIES_URI, categoryProjection, null,
						null, null);

		String statusProjection[] = {
				R.drawable.ic_status + " as img_id",
				TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME,
				"'"+TableTodoStatusAdapter.TABLE_TODO_STATUSES+"' as TABLE_NAME",
				TableTodoStatusAdapter.TABLE_TODO_STATUSES_ID };
		cursorStatuses = cr.query(DouiContentProvider.TODO_STATUSES_URI,
				statusProjection, null, null, TableTodoStatusAdapter.TABLE_TODO_STATUSES_NAME);

		String contextProjection[] = {
				R.drawable.ic_context + " as img_id",
				TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_NAME,
				"'"+TableTodoContextsAdapter.TABLE_TODO_CONTEXTS+"' as TABLE_NAME",
				TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_ID };
		cursorContexts = cr.query(DouiContentProvider.TODO_CONTEXTS_URI,
				contextProjection, null, null, TableTodoContextsAdapter.TABLE_TODO_CONTEXTS_NAME);

		Cursor cursors[] = { cursorStatuses, cursorToDoCategories, 
				cursorContexts };
		mergeCursor = new MergeCursor(cursors);

		adapter = new SimpleCursorAdapter(getApplicationContext(),
				R.layout.todo_list_row, mergeCursor, from, to, 0);
		setListAdapter(adapter);
	}

	
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		mergeCursor.moveToPosition(position);
		String tableName = mergeCursor.getString(mergeCursor.getColumnIndex("TABLE_NAME"));
		Intent i = new Intent(this, DouiTodoListActivity.class);
		Uri todoUri = null;
		if(tableName.equals(TableTodoCategoriesAdapter.TABLE_TODO_CATEGORIES))
		{
			todoUri = Uri.parse(DouiContentProvider.TODO_CATEGORIES_URI + "/"
					+ id + "/" + DouiContentProvider.TODO_PATH);
			
		}else if(tableName.equals(TableTodoStatusAdapter.TABLE_TODO_STATUSES))
		{
			todoUri = Uri.parse(DouiContentProvider.TODO_STATUSES_URI + "/"
					+ id + "/" + DouiContentProvider.TODO_PATH);
			
		}else if(tableName.equals(TableTodoContextsAdapter.TABLE_TODO_CONTEXTS))
		{
			Cursor mainCursor = (Cursor) l.getItemAtPosition(position);
			mainCursor.moveToPosition(position);
			String contextName = mainCursor.getString(1);
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