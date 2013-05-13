package co.usersource.doui.sync;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.Context;
import android.content.SyncResult;
import android.os.Bundle;
import android.util.Log;

import co.usersource.doui.sync.authentification.Authenticator;
import co.usersource.doui.sync.util.NetworkUtilities;


/**
 * This class implements synchronization with server.
 * 
 * @author Sergey Gadzhilov
 *
 */
public class SyncAdapter extends AbstractThreadedSyncAdapter 
{
    private static final String TAG = "DouiSyncAdapter";
    
    private final AccountManager mAccountManager;

    /**
     * {@inheritDoc}
     */
    public SyncAdapter(Context context, boolean autoInitialize) 
    {
        super(context, autoInitialize);
        mAccountManager = AccountManager.get(context);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) 
    {
    	Log.v(TAG, "Start synchronization (onPerformSync)");
    	String authtoken = ""; 
    	
    	try
    	{
    		authtoken = mAccountManager.blockingGetAuthToken(account, Authenticator.ACCOUNT_TYPE, true);
    		final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
    		params.add(new BasicNameValuePair("LOGIN", authtoken));
    		NetworkUtilities.SendRequest("/sync", params);
    		Log.v(TAG, authtoken);
    	}
    	catch (OperationCanceledException e) 
    	{
    		Log.v(TAG, "Operation canceled excecption!!!");
			e.printStackTrace();
		} 
    	catch (AuthenticatorException e) 
		{
			Log.v(TAG, "Authenticator excecption!!!");
			e.printStackTrace();
		} 
    	catch (IOException e) 
		{
			Log.v(TAG, "I/O excecption!!!");
			e.printStackTrace();
		}
    }
}
