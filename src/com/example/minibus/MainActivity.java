package com.example.minibus;

import java.net.URLEncoder;

import util.webService;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.actionbarsherlock.view.Window;


public class MainActivity extends SherlockPreferenceActivity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// progress dialog
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setSupportProgressBarIndeterminate(true);
		super.onCreate(savedInstanceState);
		setTitle(R.string.app_name);
//		Crittercism.init(getApplicationContext(), "508ab27601ed857a20000003");
		this.addPreferencesFromResource(R.xml.main);
	}

	@Override
	public boolean onPreferenceTreeClick(PreferenceScreen screen, Preference pref) {
		Class<?> cls = null;
		String title = pref.getTitle().toString();
		if (title.equals(getString(R.string.bus_routes))) {
			String url="/redmini/api.php?action=Select_Bus";
			this.activateProgressBar(true);
			final TextView output=new TextView(this);
			webService connectToUrl=new webService();
			connectToUrl.contextStartBrowerTo_URL(this,output,url,1);
			Log.v("testing", "output succeeds");
			return true;
//			cls = SearchPanel.class;
		}
		else if (title.equals("Map")){
			cls = Amap.class;
//			return false;
		}
		else if(title.equals("Test")){
//			cls = DemoBusInfo3.class;
			cls = DemoAmap.class;
		}else if(title.equals("CUHK_MK")){
			cls = DemoBusInfo2.class;
		}else if(title.equals("MK_CUHK")){
			cls = DemoBusInfo3.class;
		}
		Intent intent = new Intent(this, cls);
		startActivity(intent);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.about:
			new AlertDialog.Builder(this)
			.setTitle(R.string.about)
			.setMessage(Html.fromHtml(getString(R.string.about_msg)))
			.show();
			break;
		case R.id.contact:
			final Intent email = new Intent(android.content.Intent.ACTION_SENDTO);
			String uriText = "mailto:winnic1116@gmail.com" +
					"?subject=" + URLEncoder.encode("CUHK Library Mobile App Feedback"); 
			email.setData(Uri.parse(uriText));
			try {
				startActivity(email);
			} catch (Exception e) {
				Toast.makeText(this, R.string.no_email, Toast.LENGTH_SHORT).show();
			}
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.example_list, menu);
		return true;
	}
	
	
	public void activateProgressBar(boolean activate){
	    setSupportProgressBarIndeterminateVisibility(activate);
	}
	
}
