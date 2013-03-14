package co.usersource.doui.gui;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.SimpleCursorAdapter;
import android.widget.Toast;
import co.usersource.doui.DouiContentProvider;
import co.usersource.doui.R;
import co.usersource.doui.database.adapters.TableTodoItemsAdapter;
import co.usersource.doui.database.adapters.TableTodoListAdapter;

/**
 * Popup window used to display list selection menu.
 * */
public class TodoListsPopupWindow extends PopupWindow {
	private Context context;
	private SimpleCursorAdapter adapter;
	private Uri itemUri;

	public TodoListsPopupWindow(Context popupContext, Uri popupItemUri) {
		setFocusable(true);
	    setOutsideTouchable(true);
	    // Removes default black background
	    setBackgroundDrawable(new BitmapDrawable());
	    
		this.context = popupContext;
		this.itemUri = popupItemUri;
		ListView lvTodoLists = new ListView(context);

		String[] from = new String[] {
				TableTodoListAdapter.TABLE_TODO_LISTS_NAME,
				TableTodoListAdapter.TABLE_TODO_LISTS_ID };
		int[] to = new int[] { R.id.popupItemLabel };

		ContentResolver cr = context.getContentResolver();
		Cursor cursor = cr.query(DouiContentProvider.TODO_LISTS_URI, null,
				null, null, null);

		adapter = new SimpleCursorAdapter(context,
				R.layout.todo_list_popup_row, cursor, from, to);
		lvTodoLists.setAdapter(adapter);

		lvTodoLists.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {

				ContentValues values = new ContentValues();
				values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST, id);

				String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID
						+ "=?";
				String selectionArgs[] = { itemUri.getLastPathSegment() };
				context.getContentResolver().update(itemUri, values, selection,
						selectionArgs);
				Toast toast = Toast.makeText(context, "moved", Toast.LENGTH_SHORT);
				toast.show();
				dismiss();
			}
		});
		
		this.setContentView(lvTodoLists);
	}

	/* (non-Javadoc)
	 * @see android.widget.PopupWindow#dismiss()
	 */
	@Override
	public void dismiss() {
		if(!adapter.getCursor().isClosed())
		{
			adapter.getCursor().close();
		}
		super.dismiss();
	}
}
