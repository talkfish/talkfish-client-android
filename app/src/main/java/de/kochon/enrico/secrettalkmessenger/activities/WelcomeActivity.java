package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.text.util.Linkify;

public class WelcomeActivity extends AppCompatActivity {

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
      Toolbar myToolbar = (Toolbar) findViewById(R.id.my_toolbar_for_about);
      setSupportActionBar(myToolbar);

      ActionBar actionBar = getSupportActionBar();
      actionBar.setDisplayHomeAsUpEnabled(true);

      TextView textWelcome = (TextView) findViewById(R.id.textViewWelcome);
      if (textWelcome != null) {
         String name = ((TFApp) (this.getApplication())).configHelper.getName();
         textWelcome.setText(textWelcome.getText().toString().replace("$", name));
         Linkify.addLinks(textWelcome, Linkify.ALL);
      }
   }
}
