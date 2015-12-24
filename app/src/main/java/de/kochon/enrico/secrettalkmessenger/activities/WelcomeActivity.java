package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;

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
      
      Button btnOk = (Button) findViewById(R.id.buttonWelcomeOk);
      TextView textWelcome = (TextView) findViewById(R.id.textViewWelcome);
      if (btnOk != null && textWelcome != null) {
         btnOk.setOnClickListener(new OnClickListener() { public void onClick(View v) { finish(); } });
         String name = ((SecretTalkMessengerApplication)(this.getApplication())).configHelper.getName();
         textWelcome.setText(textWelcome.getText().toString().replace("$", name));
         Linkify.addLinks(textWelcome, Linkify.ALL);
    }
   }
}
