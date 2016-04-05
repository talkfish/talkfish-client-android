package de.kochon.enrico.secrettalkmessenger.activities;

import java.util.List;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Channel;

import android.app.ListActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;


public class ChannelListActivity extends ListActivity {

   private ArrayAdapter<Channel> aa;

   private void initValues() {
      List<Channel> channels = ((TFApp)(this.getApplication())).getDAH().loadAllChannels();
      aa = new ArrayAdapter<Channel>(this, R.layout.rowlayout_small, R.id.label, channels);
      setListAdapter(aa);
   }
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_channels);
      initValues();
      
      Button btnOK = (Button) findViewById(R.id.buttonChannelsOK);
      if (btnOK != null) {
         btnOK.setOnClickListener(new OnClickListener() { 
               public void onClick(View v) { 
                  finish();
               } 
         });
      }
       
   }


   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (resultCode) {
         case Activity.RESULT_OK:
               Toast.makeText(this, "Änderung gespeichert.", Toast.LENGTH_SHORT).show();
               initValues();
            break;
         case Activity.RESULT_CANCELED:
            break;
         default:
      }
   }
   
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l,v,position,id);
      Channel chan = aa.getItem(position);
      if (null != chan) {
         Intent intentEditChannel = new Intent(ChannelListActivity.this, EditChannelActivity.class);
         Bundle state = new Bundle();
         state.putInt(EditChannelActivity.EDIT_CHANNEL_ID_KEY, chan.id);
         intentEditChannel.putExtras(state); 
         startActivityForResult(intentEditChannel, 0);
      }
   }
   
}
