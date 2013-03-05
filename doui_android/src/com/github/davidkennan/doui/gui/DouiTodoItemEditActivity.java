/**
 * 
 */
package com.github.davidkennan.doui.gui;

import java.util.List;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.github.davidkennan.doui.DouiContentProvider;
import com.github.davidkennan.doui.R;
import com.github.davidkennan.doui.database.adapters.TableTodoItemsAdapter;
import com.github.davidkennan.doui.database.adapters.TableTodoListAdapter;

/**
 * @author rsh
 * 
 */
public class DouiTodoItemEditActivity extends Activity {

	private Uri itemUri;
	private Button btCancel;
	private Button btSave;
	private String itemId;
	private String itemTitle;
	private String itemBody;
	private String itemListId;
	private String itemListName;
	private TextView tvTodoListName;
	private EditText etTodoItemTitle;
	private EditText etTodoItemBody;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.todo_item_edit);
		Bundle extras = getIntent().getExtras();
		if (extras != null) {
			itemUri = extras.getParcelable(DouiContentProvider.TODO_LISTS_PATH);
		}
		
		if (itemUri.getLastPathSegment().equals(DouiContentProvider.TODO_PATH)==false)
		{
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
			etTodoItemTitle = (EditText) findViewById(R.id.etTodoItemTitle);
			etTodoItemTitle.setText(itemTitle);
			etTodoItemBody = (EditText) findViewById(R.id.etTodoItemBody);
			etTodoItemBody.setText(itemBody);
			tvTodoListName = (TextView) findViewById(R.id.tvListName);
			tvTodoListName.setText(itemListName);

		}else{
			List<String> pathSegments = itemUri.getPathSegments();
			
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
		
		btCancel = (Button) findViewById(R.id.btCancel);
		btCancel.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				finish();
			}
		});
		btSave = (Button) findViewById(R.id.btSave);
		btSave.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				ContentValues values = new ContentValues();
				values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_TITLE,
						((EditText) findViewById(R.id.etTodoItemTitle))
								.getText().toString());
				values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_BODY,
						((EditText) findViewById(R.id.etTodoItemBody))
								.getText().toString());
				
				List<String> pathSegments = itemUri.getPathSegments();
				
				if(DouiContentProvider.TODO_PATH.equals(pathSegments.get(pathSegments.size()-1)))
				{
					values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST,
							pathSegments.get(pathSegments.size()-2));
					
				}else
				{
					values.put(TableTodoItemsAdapter.TABLE_TODO_ITEMS_FK_LIST,
							pathSegments.get(pathSegments.size()-3));
				}
				
				
				if (itemUri.getLastPathSegment().equals(DouiContentProvider.TODO_PATH)) {
					itemUri = getContentResolver().insert(itemUri, values);
				} else {
					String selection = TableTodoItemsAdapter.TABLE_TODO_ITEMS_ID+"=?";
					String selectionArgs[] = {itemUri.getLastPathSegment()};
					getContentResolver().update(itemUri, values, selection,
							selectionArgs);
				}
				Toast toast = Toast.makeText(getApplicationContext(), "Item saved", Toast.LENGTH_SHORT);
				toast.show();
				finish();
			}
		});
	}
}
