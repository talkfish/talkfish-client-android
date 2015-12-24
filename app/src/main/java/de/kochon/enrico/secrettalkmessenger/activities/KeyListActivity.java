package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.model.Messagekey;

import java.util.ArrayList;
import java.util.List;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.model.Channel;

import android.app.ListActivity;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Button;
import android.widget.Toast;
import android.content.Intent;


public class KeyListActivity extends ListActivity {

   public final static String CONVERSATION_ID_KEY = "CONVERSATION_ID_KEY";

   private ArrayAdapter<Messagekey> aa;

   private Long conversationID = -1L;
   private Conversation conversation = null;

   private void initValues() {
      if (conversationID != -1L) {
         List<Messagekey> keys = ((SecretTalkMessengerApplication)(this.getApplication())).getDataAccessHelper().loadAllKeysForReceiving(conversationID);
         keys.addAll(((SecretTalkMessengerApplication)(this.getApplication())).getDataAccessHelper().loadAllKeysForSending(conversationID));
         aa = new ArrayAdapter<Messagekey>(this, R.layout.rowlayout_small, R.id.label, keys);
         setListAdapter(aa);
      }
   }
   
   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_keys);

      Button btnBack = (Button) findViewById(R.id.buttonKeysBack);
      if (btnBack != null) {
         btnBack.setOnClickListener(new OnClickListener() { 
               public void onClick(View v) { 
                  finish();
               } 
         });
      }

      Intent data = getIntent();
      if (data.hasExtra(CONVERSATION_ID_KEY)) {
         conversationID = data.getLongExtra(CONVERSATION_ID_KEY, -1);
         conversation = ((SecretTalkMessengerApplication)(this.getApplication())).getDataAccessHelper().loadConversation(conversationID);
         initValues();
      }
   }
   
   
   @Override
   protected void onListItemClick(ListView l, View v, int position, long id) {
      super.onListItemClick(l,v,position,id);
      Messagekey key = aa.getItem(position);
      if (null != key) {
         Intent intentShowKey = new Intent(KeyListActivity.this, ShowKeyActivity.class);
         Bundle state = new Bundle();
         state.putLong(ShowKeyActivity.MESSAGEKEY_ID_KEY, key.getID());
         intentShowKey.putExtras(state); 
         startActivityForResult(intentShowKey, 0);
      }
   }


   @Override
   public void onActivityResult(int requestCode, int resultCode, Intent data) {
      super.onActivityResult(requestCode, resultCode, data);
      switch (resultCode) {
         case Activity.RESULT_OK:
               initValues();
            break;
         case Activity.RESULT_CANCELED:
            break;
         default:
      }
   }
   
}
