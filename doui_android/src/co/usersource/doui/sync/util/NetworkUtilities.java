
package co.usersource.doui.sync.util;

import android.content.Context;
import android.os.Handler;
import android.util.Log;

import co.usersource.doui.sync.authentification.AuthenticatorActivity;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;


/**
 * Provides utility methods for communicating with the server.
 * 
 * @author Sergey Gadzhilov
 */
public class NetworkUtilities 
{
    private static final String TAG = "NetworkUtilities";
    
    public static final String PARAM_USERNAME = "username";
    public static final String PARAM_PASSWORD = "password";
    public static final String PARAM_UPDATED = "timestamp";
    public static final String USER_AGENT = "AuthenticationService/1.0";
    public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms
    public static final String BASE_URL = "http://192.168.1.102:8080";
    public static final String AUTH_URI = BASE_URL + "/auth";
    
    private static HttpClient mHttpClient;

    /**
     * Configures the httpClient to connect to the URL provided.
     */
    public static void maybeCreateHttpClient() 
    {
        if (mHttpClient == null) 
        {
            mHttpClient = new DefaultHttpClient();
            final HttpParams params = mHttpClient.getParams();
            HttpConnectionParams.setConnectionTimeout(params, REGISTRATION_TIMEOUT);
            HttpConnectionParams.setSoTimeout(params, REGISTRATION_TIMEOUT);
            ConnManagerParams.setTimeout(params, REGISTRATION_TIMEOUT);
        }
    }

    /**
     * Executes the network requests on a separate thread.
     * 
     * @param runnable  - The runnable instance containing 
     *                    network mOperations to be executed.
     */
    public static Thread performOnBackgroundThread(final Runnable runnable) 
    {
        final Thread t = new Thread() 
        {
            @Override
            public void run() 
            {
                try 
                {
                    runnable.run();
                } 
                finally {}
            }
        };
        t.start();
        return t;
    }

    /**
     * Connects to the server, authenticates the provided username and password.
     * 
     * @param username The user's username
     * @param password The user's password
     * @param handler The hander instance from the calling UI thread.
     * @param context The context of the calling Activity.
     * @return boolean The boolean result indicating whether the user was successfully authenticated.
     */
    public static boolean authenticate(String username, String password, Handler handler, final Context context) 
    {
        boolean bRet = false;
    	final HttpResponse resp;

        final ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
        params.add(new BasicNameValuePair(PARAM_USERNAME, username));
        params.add(new BasicNameValuePair(PARAM_PASSWORD, password));
        HttpEntity entity = null;
        
        try 
        {
            entity = new UrlEncodedFormEntity(params);
        } 
        catch (final UnsupportedEncodingException e) 
        {
            // this should never happen.
            throw new AssertionError(e);
        }
        
        final HttpPost post = new HttpPost(AUTH_URI);
        post.addHeader(entity.getContentType());
        post.setEntity(entity);
        maybeCreateHttpClient();

        try 
        {
            resp = mHttpClient.execute(post);
            if (resp.getStatusLine().getStatusCode() == HttpStatus.SC_OK) 
            {
                Log.v(TAG, "Successful authentication");
                bRet = true;
            } 
            else 
            {
                Log.v(TAG, "Error authenticating" + resp.getStatusLine());
            }
        } 
        catch (final IOException e) 
        {
        	Log.v(TAG, "IOException when getting authtoken", e);
        } 
        finally 
        {
        	Log.v(TAG, "getAuthtoken completing");
        }
        
        sendResult(bRet, handler, context);
        return bRet;
    }

    /**
     * Sends the authentication response from server back to the caller main UI
     * thread through its handler.
     * 
     * @param result The boolean holding authentication result
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context.
     */
    private static void sendResult(final Boolean result, final Handler handler, final Context context) 
    {
        if (handler == null || context == null) 
        {
            return;
        }
        handler.post(new Runnable() 
        {
            public void run() 
            {
                ((AuthenticatorActivity) context).onAuthenticationResult(result);
            }
        });
    }

    /**
     * Attempts to authenticate the user credentials on the server.
     * 
     * @param username The user's username
     * @param password The user's password to be authenticated
     * @param handler The main UI thread's handler instance.
     * @param context The caller Activity's context
     * @return Thread The thread on which the network mOperations are executed.
     */
    public static Thread attemptAuth(final String username, final String password, final Handler handler, final Context context) 
    {
        final Runnable runnable = new Runnable() 
        {
            public void run() 
            {
                authenticate(username, password, handler, context);
            }
        };
        // run on background thread.
        return NetworkUtilities.performOnBackgroundThread(runnable);
    }
    
    /**
     * 
     * @param URI - URI for request.
     * @param params - parameters which must send to the server. 
     * @return - answer from server in JSON format.
     * @throws IOException 
     * @throws ParseException 
     */
    public static JSONArray SendRequest(String URI, ArrayList<NameValuePair> params) throws ParseException, IOException
    {
    	Log.v(TAG, "Send request to the " + BASE_URL + URI);
    	
    	final HttpPost postRequest = new HttpPost(BASE_URL + URI);
    	HttpEntity entity = null;
    	JSONArray result = null;
    	
  		entity = new UrlEncodedFormEntity(params);
   		postRequest.addHeader(entity.getContentType());
   		postRequest.setEntity(entity);
   		maybeCreateHttpClient();
    		
   		final HttpResponse response = mHttpClient.execute(postRequest);
        final String data = EntityUtils.toString(response.getEntity());
            
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK)
        {
            try 
            {
				result = new JSONArray(data);
				for (int i = 0; i < result.length(); i++) 
				{
					Log.v(TAG, result.getJSONObject(i).toString());
		        }
			} 
            catch (JSONException e) 
			{
				Log.v(TAG, "Cannot parse json from server!!!");
				e.printStackTrace();
            }
        }
    	return result;
    }

}
