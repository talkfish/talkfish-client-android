package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.backend.ConfigHelper;
import de.kochon.enrico.secrettalkmessenger.service.PeriodicMessageCheck;
import de.kochon.enrico.secrettalkmessenger.service.KeepAliveCheck;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import android.net.Uri;
import android.widget.Toolbar;


public class SettingsActivity extends Activity implements OnClickListener {
	
	private ConfigHelper configHelper;
	private EditText configName;
	private Button ok;
	private Button downloadLatest;
	private Button channels;
   private Button log;

   private RadioButton background_mobile;
   private RadioButton background_wifi;

   @Override
   public boolean onCreateOptionsMenu(Menu menu) {
      super.onCreateOptionsMenu(menu);

      MenuInflater inflater = getMenuInflater();
      inflater.inflate(R.menu.settingsmenu, menu);

      return true;
   }

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case (android.R.id.home):
            finish();
            return true;
         case (R.id.action_about):
            Intent intentWelcome = new Intent(SettingsActivity.this, WelcomeActivity.class);
            startActivityForResult(intentWelcome, 0);
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_settings);
        Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_for_settings);
       setActionBar(myToolbar);

        ActionBar actionBar = getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

      log = (Button) findViewById(R.id.buttonSettingsLog);
      if ( log != null) {
         log.setOnClickListener(this);
      }

      configHelper = ((TFApp)(this.getApplication())).configHelper;
         
		configName = (EditText) findViewById(R.id.editConfigName);
		ok = (Button) findViewById(R.id.buttonSettingsOk);
      if (configName != null && ok != null) {
         configName.setText(configHelper.getName());
         ok.setOnClickListener(this);
      }
      channels = (Button) findViewById(R.id.buttonSettingsChannels);
      if (channels != null) { channels.setOnClickListener(this); }
      downloadLatest = (Button) findViewById(R.id.buttonSettingsDownloadLatest);
      if (downloadLatest  != null) { downloadLatest.setOnClickListener(this); }

      String currentmode = configHelper.getBackground();
      background_mobile = (RadioButton) findViewById(R.id.background_mobile_data);
      if (background_mobile != null && currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_MOBILE)) {
         background_mobile.setChecked(true);
      }
      background_wifi = (RadioButton) findViewById(R.id.background_wifi);
      if (background_wifi != null && currentmode.equals(ConfigHelper.CONFIG_KEY_BACKGROUND_OPTION_WIFI)) {
         background_wifi.setChecked(true);
      }
       TextView versionView = (TextView) findViewById(R.id.textVersionsinfo);
       if (null != versionView) {
          String version = "unknown";
          try {
             version = getPackageManager().getPackageInfo("de.kochon.enrico.secrettalkmessenger",0).versionName;
          } catch (PackageManager.NameNotFoundException nnfe) {
             ((TFApp) (SettingsActivity.this.getApplication())).logException(nnfe);
          }
          versionView.setText(String.format("Aktuell installierte Version: %s", version));
       }
	}


   public void onRadiogroupBackgroundClicked(View rb) {
      if (((RadioButton) rb).isChecked()) {
         boolean serviceShouldRun = false;
         if (rb.getId() == R.id.background_mobile_data) {
            configHelper.setBackgroundMobile();
            serviceShouldRun = true;
			   Toast.makeText(this, "Der Hintergrunddienst wurde generell aktiviert!", Toast.LENGTH_LONG).show();
         }
         if (rb.getId() == R.id.background_wifi) {
            configHelper.setBackgroundWifi();
            serviceShouldRun = true;
			   Toast.makeText(this, "Der Hintergrunddienst wurde nur für WLAN aktiviert!", Toast.LENGTH_LONG).show();
         }
         if (serviceShouldRun) {
            PeriodicMessageCheck.setAlarm(this);
            KeepAliveCheck.setAlarm(this);
            ((TFApp) (SettingsActivity.this.getApplication())).addToApplicationLog("Backgroundservice activated by user.");
         } else {
            KeepAliveCheck.cancelAlarm(this);
            PeriodicMessageCheck.cancelAlarm(this);
            ((TFApp) (SettingsActivity.this.getApplication())).addToApplicationLog("Backgroundservice stopped by user.");
         }
      }
   }


	@Override
	public void onClick(View v) {
		if (v == channels) {
         Intent intentChannels = new Intent(SettingsActivity.this, ChannelListActivity.class);
         startActivityForResult(intentChannels, 0);
		}
		if (v == downloadLatest) {
         Intent intentGetLatest = new Intent(Intent.ACTION_VIEW, 
                                             Uri.parse(((TFApp)
                                             (this.getApplication())).DOWNLOADLOCATION));
         startActivityForResult(intentGetLatest, 0);
		}
      if (v == log) {
         Intent intentShowLog = new Intent(SettingsActivity.this, LogViewActivity.class);
         startActivityForResult(intentShowLog, 0);
      }
		if (v == ok) {
			String newName = configName.getText().toString();
			if (!configHelper.getName().equals(newName)) {
				configHelper.setName(newName);
				Toast.makeText(this, "Speichere Namen: " + newName, Toast.LENGTH_LONG).show();
			}
		}
	}
	
	
}
