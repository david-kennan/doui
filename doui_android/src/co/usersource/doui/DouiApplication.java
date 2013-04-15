/**
 * 
 */
package co.usersource.doui;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

/**
 * @author rsh
 *
 */
@ReportsCrashes(formKey = "", // will not be used
mailTo = "taranov.pavel@gmail.com")
public class DouiApplication extends Application {
	@Override
	  public void onCreate() {
	      super.onCreate();

	      // The following line triggers the initialization of ACRA
	      ACRA.init(this);
	  }
}
