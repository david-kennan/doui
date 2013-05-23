/**
 * 
 */
package co.usersource.doui.network;

import java.io.IOException;
import java.util.ArrayList;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

/**
 * @author rsh
 * 
 */
public class HttpConnector {

	/** Timeouts for httpClient */
	public static final int REGISTRATION_TIMEOUT = 30 * 1000; // ms
	/** Base URL for DOUI services */
	public static final String BASE_URL = "https://douiserver.appspot.com";
	/** Auth URL part. */
	public static final String AUTH_URI = BASE_URL + "/_ah/login";

	private DefaultHttpClient httpClient;
	private Context applicationContext;
	private String auth_token;

	public DefaultHttpClient getHttpClient() {
		if (null == httpClient) {
			httpClient = new DefaultHttpClient();
		}
		return httpClient;
	}

	/**
	 * 
	 * @param URI
	 *            - URI for request.
	 * @param params
	 *            - parameters which must send to the server.
	 * @return - answer from server in JSON format.
	 * @throws IOException
	 * @throws ParseException
	 */
	public JSONObject SendRequest(String URI, ArrayList<NameValuePair> params)
			throws ParseException, IOException {
		String URL = BASE_URL + URI + "?auth="
				+ auth_token;
		final HttpPost postRequest = new HttpPost(BASE_URL + URI + "?auth="
				+ auth_token);
		HttpEntity entity = null;
		JSONObject result = null;

		entity = new UrlEncodedFormEntity(params);
		postRequest.addHeader(entity.getContentType());
		postRequest.setEntity(entity);

		final HttpParams HttpClientParams = getHttpClient().getParams();
		HttpConnectionParams.setConnectionTimeout(HttpClientParams,
				REGISTRATION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(HttpClientParams,
				REGISTRATION_TIMEOUT);
		ConnManagerParams.setTimeout(HttpClientParams, REGISTRATION_TIMEOUT);

		final HttpResponse response = getHttpClient().execute(postRequest);

		final String data = EntityUtils.toString(response.getEntity());
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			try {
				result = new JSONObject(data);
			} catch (JSONException e) {
				Log.v(this.getClass().getName(),
						"Cannot parse json from server!!!");
				e.printStackTrace();
			}
		}
		return result;
	}

	public boolean authenticate(Context applicationContext, Account account) {
		boolean result = false;
		this.applicationContext = applicationContext;
		AccountManager accountManager = AccountManager.get(applicationContext);
		accountManager.getAuthToken(account, "ah", null, false,
				new GetAuthTokenCallback(), null);
		return result;
	}

	private boolean onGetAuthToken(Bundle bundle) {
		this.auth_token = bundle.getString(AccountManager.KEY_AUTHTOKEN);

		new GetCookieTask().execute(auth_token);
		return false;
	}

	private class GetCookieTask extends AsyncTask<String, Integer, Boolean> {
		protected Boolean doInBackground(String... tokens) {
			try {
				// Don't follow redirects
				final HttpParams HttpClientParams = getHttpClient().getParams();
				HttpConnectionParams.setConnectionTimeout(HttpClientParams,
						REGISTRATION_TIMEOUT);
				HttpConnectionParams.setSoTimeout(HttpClientParams,
						REGISTRATION_TIMEOUT);
				ConnManagerParams.setTimeout(HttpClientParams,
						REGISTRATION_TIMEOUT);
				getHttpClient().getParams().setBooleanParameter(
						ClientPNames.HANDLE_REDIRECTS, false);

				HttpGet http_get = new HttpGet(AUTH_URI + "/?continue="
						+ BASE_URL + "&auth=" + tokens[0]);
				HttpResponse response;
				response = getHttpClient().execute(http_get);
				if (response.getStatusLine().getStatusCode() != 302) {
					// Response should be a redirect
					Log.e(this.getClass().getName(),
							"response.getStatusLine().getStatusCode() != 302");
					return false;
				}
				for (Cookie cookie : getHttpClient().getCookieStore()
						.getCookies()) {
					if (cookie.getName().equals("ACSID"))
						return true;
				}
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				getHttpClient().getParams().setBooleanParameter(
						ClientPNames.HANDLE_REDIRECTS, true);
			}
			return false;
		}

		protected void onPostExecute(Boolean result) {
			Toast.makeText(applicationContext, "Auth result: " + result,
					Toast.LENGTH_LONG).show();
		}
	}

	private class GetAuthTokenCallback implements
			AccountManagerCallback<Bundle> {
		public void run(AccountManagerFuture<Bundle> result) {
			Bundle bundle;
			try {
				bundle = result.getResult();
				Intent intent = (Intent) bundle.get(AccountManager.KEY_INTENT);
				if (null != intent) {
					applicationContext.startActivity(intent);
				} else {
					onGetAuthToken(bundle);
				}
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (AuthenticatorException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	};
}
