package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.R;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.content.pm.PackageManager.NameNotFoundException;


public class MainActivity extends Activity {

   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      Button btnWelcome = (Button) findViewById(R.id.buttonWelcome);
      Button btnChats = (Button) findViewById(R.id.buttonChats);
      Button btnSettings = (Button) findViewById(R.id.buttonSettings);
      if (btnWelcome != null && btnChats != null && btnSettings != null) {
         btnWelcome.setOnClickListener(new OnClickListener() { 
            public void onClick(View v) { 
               Intent intentWelcome = new Intent(MainActivity.this, WelcomeActivity.class);
               startActivityForResult(intentWelcome, 0);
            } 
         });
         btnChats.setOnClickListener(new OnClickListener() { 
            public void onClick(View v) { 
               Intent intentChats = new Intent(MainActivity.this, ConversationListActivity.class);
               startActivityForResult(intentChats, 0);
            } 
         });
         btnSettings.setOnClickListener(new OnClickListener() { 
            public void onClick(View v) { 
               Intent intentSettings = new Intent(MainActivity.this, SettingsActivity.class);
               startActivityForResult(intentSettings, 0);
            } 
         });
      }
      TextView versionView = (TextView) findViewById(R.id.textViewVersion);
      if (null != versionView) {
         String version = "unknown";
         try {
            version = getPackageManager().getPackageInfo("de.kochon.enrico.secrettalkmessenger",0).versionName;
         } catch (NameNotFoundException nnfe) {
            TFApp.logException(nnfe);
         }
         versionView.setText(String.format("Version: %s" , version));
      }
      ((TFApp)(this.getApplication())).checkBackgroundService(this);
   }
}
