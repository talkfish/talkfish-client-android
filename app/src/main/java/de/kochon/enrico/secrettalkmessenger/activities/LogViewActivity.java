package de.kochon.enrico.secrettalkmessenger.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.TextView;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;

public class LogViewActivity extends Activity {

   @Override
   public boolean onOptionsItemSelected(MenuItem item) {
      switch (item.getItemId()) {
         case (android.R.id.home):
            finish();
            return true;
         default:
            return super.onOptionsItemSelected(item);
      }
   }

   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_logviewer);

      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      TextView textWelcome = (TextView) findViewById(R.id.textViewLogContent);
      if (textWelcome != null) {
         String logContent = ((TFApp) (this.getApplication())).getDAH().getFullLog();
         textWelcome.setText(logContent);
      }
   }
}
