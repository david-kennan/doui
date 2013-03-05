/**
 * 
 */
package com.github.davidkennan.doui.gui;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import com.github.davidkennan.doui.DouiContentProvider;
import com.github.davidkennan.doui.R;
import com.github.davidkennan.doui.database.adapters.TableTodoItemsAdapter;
import com.github.davidkennan.doui.database.adapters.TableTodoListAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoItemViewActivity extends Activity {

	private Uri itemUri;
	private String itemId;
	private String itemListId;
	private String itemBody;
	private String itemTitle;
	private TextView tvTodoItemTitle;
	private TextView tvTodoItemBody;
	private String itemListName;
	private TextView tvTodoListName;
	private Button btEdit;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_item_view);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			itemUri = extras.getParcelable(DouiContentProvider.TODO_LISTS_PATH);
		}
		String[] projection = { TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
				TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST };
		Cursor cursor = getContentResolver().query(itemUri, projection, null,
				null, null);
		cursor.moveToFirst();
		itemId = cursor.getString(0);
		itemTitle = cursor.getString(1);
		itemBody = cursor.getString(2);
		itemListId = cursor.getString(3);
		cursor.close();
		// TODO not best solution. Think about JOIN.
		Uri uriList = Uri.parse("content://" + DouiContentProvider.AUTHORITY
				+ "/" + DouiContentProvider.TODO_LISTS_PATH + "/" + itemListId);
		String listProperties[] = {TableTodoListAdapter.TABLE_TODO_LISTS_NAME};
		cursor = getContentResolver().query(uriList, listProperties, null,
				null, null);
		cursor.moveToFirst();
		itemListName = cursor.getString(0);
		tvTodoItemTitle = (TextView) findViewById(R.id.tvTodoTitle);
		tvTodoItemTitle.setText(itemTitle);
		tvTodoItemBody = (TextView) findViewById(R.id.tvTodoBody);
		tvTodoItemBody.setText(itemBody);
		tvTodoListName = (TextView) findViewById(R.id.tvListName);
		tvTodoListName.setText(itemListName);
		
		btEdit = (Button)findViewById(R.id.btEdit);
		final DouiTodoItemViewActivity self = this;
		btEdit.setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				Intent i = new Intent(self, DouiTodoItemEditActivity.class);
				i.putExtra(DouiContentProvider.TODO_LISTS_PATH, itemUri);
				startActivity(i);				
			}
		});
	}

}
