package de.kochon.enrico.secrettalkmessenger.activities;

import android.app.ActionBar;
import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.util.Linkify;
import android.view.MenuItem;
import android.widget.ScrollView;
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
      if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

      TextView textLogContent = (TextView) findViewById(R.id.textViewLogContent);
      if (textLogContent != null) {
         String logContent = ((TFApp) (this.getApplication())).getDAH().getFullLog();
         textLogContent.setText(logContent);
      }
      scrollDown();
   }

   // http://stackoverflow.com/questions/2667471/android-scroll-down
   private void scrollDown() {
      new Handler().postDelayed(new Runnable() {
         @Override
         public void run() {
            ScrollView scrollview = (ScrollView) findViewById(R.id.scrollViewForLogviewer);
            if (scrollview != null) {
               scrollview.fullScroll(ScrollView.FOCUS_DOWN);
            }
         }
      }, 1000);

   }
}
