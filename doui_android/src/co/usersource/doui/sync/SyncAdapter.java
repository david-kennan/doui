package co.usersource.doui.sync;

import android.accounts.Account;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;


/**
 * This class implements synchronization with server.
 * 
 * @author Sergey Gadzhilov
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter 
{
    private static final String TAG = "DouiSyncAdapter";

    /**
     * {@inheritDoc}
     */
    public SyncAdapter(Context context, boolean autoInitialize) 
    {
        super(context, autoInitialize);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) 
    {
    	//TODO: Create code for synchronization
    	Log.v(TAG, "Start synchronization");
    }
}
