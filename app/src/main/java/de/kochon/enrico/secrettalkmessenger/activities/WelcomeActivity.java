package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.TextView;
import android.text.util.Linkify;

public class WelcomeActivity extends Activity {

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
      setContentView(R.layout.activity_welcome);

      ActionBar actionBar = getActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      TextView textWelcome = (TextView) findViewById(R.id.textViewWelcome);
      if (textWelcome != null) {
         String name = ((TFApp) (this.getApplication())).configHelper.getName();
         textWelcome.setText(textWelcome.getText().toString().replace("$", name));
         Linkify.addLinks(textWelcome, Linkify.ALL);
      }
   }
}
