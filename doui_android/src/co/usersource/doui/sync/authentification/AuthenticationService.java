package co.usersource.doui.sync.authentification;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Service to handle Account authentication. 
 * It instantiates the authenticator and returns its IBinder.
 * 
 * @author Sergey Gadzhilov
 */
public class AuthenticationService extends Service 
{
    private static final String TAG = "DouiAuthService";
    
    private Authenticator mAuthenticator;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() 
    {
        Log.v(TAG, "Authentication Service started.");
        mAuthenticator = new Authenticator(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() 
    {
        Log.v(TAG, "Authentication Service stopped.");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public IBinder onBind(Intent intent) 
    {
        Log.v(TAG, "getBinder() returning the AccountAuthenticator binder for intent " + intent);
        return mAuthenticator.getIBinder();
    }
}
