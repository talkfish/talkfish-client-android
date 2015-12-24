package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;
import de.kochon.enrico.secrettalkmessenger.model.Messagekey;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ListView;
import android.widget.Toast;

import java.util.Set;
import java.util.UUID;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.IOException;
import java.io.InputStream;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import de.kochon.enrico.secrettalkmessenger.R;


public class ReceiveKeyByBluetoothActivity extends Activity {
	final int REQUEST_ENABLE_BT = 1;
	final int REQUEST_BE_DISCOVERABLE = 2;
   final int MESSAGE_READ = 314;
   final static String UUID_STRING = "6b1e1d2e-1d1e-45f2-b80e-84893025e2b3";
   final String SERVICENAME = "secrettalk";

   public final static String SHOW_CONVERSATION_ID_KEY = "SHOW_CONVERSATION_ID_KEY";

   EditText receiveText;
   AcceptThread listeningThread;

   TextView nameText;
   protected Conversation conversation;


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               case MESSAGE_READ:
                  byte[] readBuf = (byte[]) msg.obj;
                  // construct a string from the valid bytes in the buffer
                  String readBluetoothString = new String(readBuf, 0, msg.arg1);
                  String keys[] = readBluetoothString.split(SendKeyBatchByBluetoothActivity.KEY_SEPARATOR);
                  long startnow;
                  long endnow;
                  startnow = android.os.SystemClock.uptimeMillis();
                  int keycount = ((SecretTalkMessengerApplication)(ReceiveKeyByBluetoothActivity.this.getApplication())).
                           getDataAccessHelper().bulkAddEncodedKeysToConversationAndSetExchanged(ReceiveKeyByBluetoothActivity.this.conversation, keys);
                  endnow = android.os.SystemClock.uptimeMillis();
                  long totaltime = endnow-startnow;
                  double timeperkey = -1;
                  if (keycount>0) {
                     timeperkey= totaltime/keycount;
                  }
                  Log.d(SecretTalkMessengerApplication.LOGKEY, 
                        String.format("total processing of %d keys took %d ms, this is %f ms per key", 
                                       keycount, totaltime, timeperkey));
                  Intent reply = new Intent();
                  Bundle result = new Bundle();
                  setResult(RESULT_OK, reply);
                  finish();

                  break;
               default: // do nothing
            }
        }
    };


   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_receivekeybybluetooth);

      Intent data = getIntent();
      if (data.hasExtra(SHOW_CONVERSATION_ID_KEY)) {
         Long conversationID = data.getLongExtra(SHOW_CONVERSATION_ID_KEY, -1);
         conversation = ((SecretTalkMessengerApplication)(this.getApplication())).getDataAccessHelper().loadConversation(conversationID);
         nameText = (TextView) findViewById(R.id.receiveViewConversationName);
         if ( null != conversation && null != nameText) 
         {
            nameText.setText(conversation.getNick());
         }
         else {
            Toast.makeText(this, String.format("Technischer Fehler. Unterhaltung mit ID %d kann nicht geladen werden.", conversationID),
            Toast.LENGTH_LONG).show();
         }
      }

      receiveText = (EditText) findViewById(R.id.receiveText);
        
      Button back = (Button) findViewById(R.id.receiveButtonBack);
      back.setOnClickListener(new OnClickListener() {
         @Override
         public void onClick(View v) {
            Log.d(SecretTalkMessengerApplication.LOGKEY, "onClick in ButtonBack");
            if (ReceiveKeyByBluetoothActivity.this.listeningThread != null) {
               Log.d(SecretTalkMessengerApplication.LOGKEY, "trying to close thread");
               ReceiveKeyByBluetoothActivity.this.listeningThread.cancel();
            }
            Intent reply = new Intent();
            Bundle result = new Bundle();
            setResult(RESULT_CANCELED, reply);
            finish();
         }
	   });

      
      BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
      if (BT == null) {
      	String noDevMsg = "This device does not appear to have a Bluetooth adapter, sorry";
      	Toast.makeText(this, noDevMsg, Toast.LENGTH_LONG).show();
      	finish();
      }
      if (!BT.isEnabled()) {
      	// Ask user's permission to switch the Bluetooth adapter On. 
      	Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      	startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
      } else {
         startListening();
      }
    }
        

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      switch (requestCode) {
         case REQUEST_ENABLE_BT:
            if (resultCode==Activity.RESULT_OK) {
               startListening();
            } else {
               Toast.makeText(this, "Bluetooth konnte nicht aktiviert werden.", Toast.LENGTH_LONG).show();
            }
            break;
         case REQUEST_BE_DISCOVERABLE :
            if (resultCode>0) {
               listeningThread = new AcceptThread();
               listeningThread.start();
            } else {
               Toast.makeText(this, "Sichtbarkeit für andere konnte nicht eingeschaltet werden.", Toast.LENGTH_LONG).show();
               finish();
            }
            break;
         default:
    		   Toast.makeText(this, "Unbekannter Anforderungscode " + requestCode, Toast.LENGTH_LONG).show();
      }
   }


   protected void startListening() {
    	BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
    	String address = BT.getAddress();
    	String name = BT.getName();
    	String connectedMsg = "Bluetooth aktiviert: "  + name + " : " + address;
      receiveText.setText(connectedMsg);

      Intent discoverableIntent = new
      Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
      discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
      startActivityForResult(discoverableIntent, REQUEST_BE_DISCOVERABLE);
   }


   private class AcceptThread extends Thread {
      private final BluetoothServerSocket mmServerSocket;
    
      public AcceptThread() {
          BluetoothServerSocket tmp = null;
          try {
             // MY_UUID is the app's UUID string, also used by the client code
             tmp = BluetoothAdapter.getDefaultAdapter().listenUsingRfcommWithServiceRecord(SERVICENAME, UUID.fromString(UUID_STRING));
          } catch (IOException e) {
              SecretTalkMessengerApplication.logException(e);
          }
          mmServerSocket = tmp;
       }
    
      public void run() {
         BluetoothSocket socket = null;
         try {
            Log.d(SecretTalkMessengerApplication.LOGKEY, "mmServerSocket.accept()");
            
            socket = mmServerSocket.accept();

         } catch (IOException e) {
            SecretTalkMessengerApplication.logException(e);
         }

         if (socket != null) {
            Log.d(SecretTalkMessengerApplication.LOGKEY, "got socket for receiving key(s)");
            try {
               mmServerSocket.close();
               InputStream in = socket.getInputStream();
               if (in != null) {
                  int total_buffersize = 2*SendKeyBatchByBluetoothActivity.BATCH_KEY_GENERATION_COUNT_PER_DIRECTION * 
                                          (Messagekey.VERSION_0_ENCODED_KEYLENGTH + SendKeyBatchByBluetoothActivity.KEY_SEPARATOR.length());
                  byte[] buffer = new byte[total_buffersize]; 
                  int current_offset = 0;
                  int bytesread = 0;

                  long startnow;
                  long endnow;
                  startnow = android.os.SystemClock.uptimeMillis();

                  do {
                     bytesread = in.read(buffer, current_offset, total_buffersize-current_offset);
                     current_offset += bytesread;
                     Log.d(SecretTalkMessengerApplication.LOGKEY, String.format("read %d bytes, now at offset %d", bytesread, current_offset));
                  } while ((bytesread > 0) && (current_offset < total_buffersize));
                  
                  endnow = android.os.SystemClock.uptimeMillis();
                  double transferrate_estimate = 0;
                  if (endnow-startnow>0) {
                     transferrate_estimate = 1000*total_buffersize/(1024*(endnow-startnow));
                  }
                  Log.d(SecretTalkMessengerApplication.LOGKEY, String.format("receiving %d bytes over air took %d ms, estimated speed %f KBytes/sec.", 
                                                                              total_buffersize, (endnow-startnow), transferrate_estimate));

                  mHandler.obtainMessage(MESSAGE_READ, total_buffersize, -1, buffer).sendToTarget();
               }

            } catch (IOException e) {
               SecretTalkMessengerApplication.logException(e);
            }
         }
       }

    
       /** Will cancel the listening socket, and cause the thread to finish */
       public void cancel() {
           try {
               Log.d(SecretTalkMessengerApplication.LOGKEY, "cancel called, closing mmServerSocket");
               mmServerSocket.close();
           } catch (IOException e) {
              SecretTalkMessengerApplication.logException(e);
           }
       }
   }
    
}
