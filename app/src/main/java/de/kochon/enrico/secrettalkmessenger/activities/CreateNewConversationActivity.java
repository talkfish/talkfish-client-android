package de.kochon.enrico.secrettalkmessenger.activities;

import java.util.List;
import java.util.ArrayList;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import android.content.Intent;

import android.view.View.OnClickListener;
import android.view.View;

import de.kochon.enrico.secrettalkmessenger.R;

import de.kochon.enrico.secrettalkmessenger.TFApp;
import de.kochon.enrico.secrettalkmessenger.model.Channel;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;


public class CreateNewConversationActivity extends Activity {

   public final static String NEW_CONVERSATION_ID_KEY = "NEW_CONVERSATION_ID_KEY";

   protected Channel defaultSending;
   protected Channel defaultReceiving;
   
   protected Button btnOk;
   protected EditText convName;


   protected boolean initDefaultChannels() {
      boolean foundReceiving = false;
      boolean foundSending = false;
      final List<Channel> channels = ((TFApp)(this.getApplication())).getDAH().loadAllChannels();
      final ArrayList<String> channelEntries = new ArrayList<String>();
      for(Channel c: channels) { 
         if (c.name.equals("default") && c.protocol.equals("secrettalk") && c.isforreceiving) 
         { foundReceiving = true; defaultReceiving = c; }
         if (c.name.equals("default") && c.protocol.equals("secrettalk") && !c.isforreceiving) 
         { foundSending = true; defaultSending = c; }
      }
      return foundReceiving && foundSending;
   }


   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      if (initDefaultChannels()) {
         setContentView(R.layout.activity_createnewconversationactivity);
         btnOk = (Button) findViewById(R.id.buttonCreateNewConversationActivityOk);
         convName = (EditText) findViewById(R.id.editNewConversationName);
         if (btnOk != null && convName != null) {
            btnOk.setOnClickListener(new OnClickListener() {
               public void onClick(View v) {
                  // create Conversation
                  String name = convName.getText().toString();
                  Conversation newConversation = new Conversation(name, defaultReceiving, defaultSending);
                  long rowid = ((TFApp)(CreateNewConversationActivity.this.getApplication()))
                     .getDAH().addNewConversation(newConversation);
                  if (-1 != rowid)
                  {
                     // build reply
                     Intent reply = new Intent();
                     Bundle result = new Bundle();
                     result.putLong(CreateNewConversationActivity.NEW_CONVERSATION_ID_KEY, rowid);
                     reply.putExtras(result);
                     setResult(RESULT_OK, reply);
                     finish();
                  } else {
                     String message = String.format("Fehler: Die neue Unterhaltung mit <%s> konnte der internen Datenbank nicht hinzugefügt werden.", name);
                     Toast.makeText(CreateNewConversationActivity.this, message, Toast.LENGTH_LONG).show();
                     TFApp.addToApplicationLog(message);
                  }
               }
            });
         }
      }
   }
}
