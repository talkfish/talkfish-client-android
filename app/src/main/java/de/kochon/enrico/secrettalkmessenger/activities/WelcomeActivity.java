package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.text.util.Linkify;

public class WelcomeActivity extends Activity {


   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_welcome);

      TextView textWelcome = (TextView) findViewById(R.id.textViewWelcome);
      if (textWelcome != null) {
         String name = ((TFApp) (this.getApplication())).configHelper.getName();
         textWelcome.setText(textWelcome.getText().toString().replace("$", name));
         Linkify.addLinks(textWelcome, Linkify.ALL);
      }
   }
}
