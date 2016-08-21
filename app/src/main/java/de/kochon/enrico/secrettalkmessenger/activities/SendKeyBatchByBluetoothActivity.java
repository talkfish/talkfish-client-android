package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.model.Messagekey;
import de.kochon.enrico.secrettalkmessenger.model.Conversation;
import de.kochon.enrico.secrettalkmessenger.TFApp;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView;

import java.util.Set;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.UUID;

import java.io.IOException;
import java.io.OutputStream;

import android.os.Handler;
import android.os.Message;

import de.kochon.enrico.secrettalkmessenger.R;

public class SendKeyBatchByBluetoothActivity extends Activity {

   final int MESSAGE_SENT = 31415;

   public final static int BATCH_KEY_GENERATION_COUNT_PER_DIRECTION=1000;
   public final static String KEY_SEPARATOR = "\n";

   public final static String SHOW_CONVERSATION_ID_KEY = "SHOW_CONVERSATION_ID_KEY";

	int REQUEST_ENABLE_BT = 1;

	EditText main;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private ArrayAdapter<String> mpairedDevicesArrayAdapter;
   private HashMap<String, BluetoothDevice> devicesMap;

   protected Conversation conversation;

   private boolean receiverWasRegistered = false;


    // The Handler that gets information back from the BluetoothChatService
    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
               case MESSAGE_SENT:
                  Log.d(TFApp.LOGKEY,
                        "got message_sent");
                  Intent reply = new Intent();
                  Bundle result = new Bundle();
                  setResult(RESULT_OK, reply);
                  finish();

                  break;
               default: // do nothing
            }
        }
    };


    
	/** Receiver for the BlueTooth Discovery Intent; put the paired devices into the viewable list.
	 */
   private final BroadcastReceiver mReceiver = new BroadcastReceiver() {    		
      @Override
      public void onReceive(Context context, Intent intent) {
         String action = intent.getAction();
         
         if (BluetoothDevice.ACTION_FOUND.equals(action)){
            BluetoothDevice btDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
            
            if(btDevice.getBondState() != BluetoothDevice.BOND_BONDED){
               String device_id = btDevice.getName() + " - " + btDevice.getAddress(); 
               mNewDevicesArrayAdapter.add(device_id);
               SendKeyBatchByBluetoothActivity.this.devicesMap.put(device_id, btDevice);
            }
         } else {
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
               setProgressBarIndeterminateVisibility(false);
               setTitle(R.string.select_device);
               if (mNewDevicesArrayAdapter.getCount() == 0){
                  String noDevice = getResources().getText(R.string.none_paired).toString();
                  mNewDevicesArrayAdapter.add(noDevice);
               }
            }   			
         }
      }
   };

	
   /** Called when the activity is first created. */
   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      setContentView(R.layout.activity_sendkeybatchbybluetooth);

      Intent data = getIntent();
      if (data.hasExtra(SHOW_CONVERSATION_ID_KEY)) {
         Long conversationID = data.getLongExtra(SHOW_CONVERSATION_ID_KEY, -1);
         conversation = ((TFApp)(this.getApplication())).getDAH().loadConversation(conversationID);
         if (  null == conversation ) 
         {
            Toast.makeText(this, String.format("Technischer Fehler. Unterhaltung mit ID %d kann nicht angezeigt werden.", conversationID),
            Toast.LENGTH_LONG).show();
            finish();
         }
      }

      devicesMap = new HashMap<String, BluetoothDevice>();

      main = (EditText) findViewById(R.id.batch_mainTextArea);
      if (main != null) {
         main.setText("Bitte erst den Empfänger einschalten, danach senden.");
      }
      mpairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.rowitem_bluetooth_endpoint);
      mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.rowitem_bluetooth_endpoint);
      
      Button discover = (Button) findViewById(R.id.batch_discoverButton);
      discover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               Log.d(TFApp.LOGKEY, "in onClick(" + v + ")");
               // IntentFilter for found devices
               IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
               // Broadcast receiver for any matching filter
               SendKeyBatchByBluetoothActivity.this.registerReceiver(mReceiver, foundFilter);
               SendKeyBatchByBluetoothActivity.this.receiverWasRegistered=true;
               IntentFilter doneFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
               SendKeyBatchByBluetoothActivity.this.registerReceiver(mReceiver, doneFilter);
               BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
               if (BT != null) {
                  BT.startDiscovery();
               }
            }
         });
      
      ListView lvPaired = (ListView) findViewById(R.id.batch_pairedBtDevices);
      lvPaired.setAdapter(mpairedDevicesArrayAdapter);
      lvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice dev = SendKeyBatchByBluetoothActivity.this.devicesMap.get(mpairedDevicesArrayAdapter.getItem(position));
            Toast.makeText(SendKeyBatchByBluetoothActivity.this, dev.getName(), Toast.LENGTH_SHORT).show();
            ConnectThread ct = new ConnectThread(dev, SendKeyBatchByBluetoothActivity.this.conversation);
            ct.start();
         }
      });

      ListView lvDisco = (ListView) findViewById(R.id.batch_discoBtDevices);
      lvDisco.setAdapter(mNewDevicesArrayAdapter);
      lvDisco.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice dev = SendKeyBatchByBluetoothActivity.this.devicesMap.get(mNewDevicesArrayAdapter.getItem(position));
            Toast.makeText(SendKeyBatchByBluetoothActivity.this, dev.getName(), Toast.LENGTH_SHORT).show();
            ConnectThread ct = new ConnectThread(dev, SendKeyBatchByBluetoothActivity.this.conversation);
            ct.start();
         }
      });
      
      BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
      if (BT == null) {
      	String noDevMsg = "Es ist kein Bluetooth verfügbar.";
      	main.setText(noDevMsg);
      	Toast.makeText(this, noDevMsg, Toast.LENGTH_LONG).show();
      	return;
      }
      if (!BT.isEnabled()) {
      	Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
      	startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
      } else {
         fillBondedDevices();
      } 
   }


   private void fillBondedDevices() {
      Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
      if (pairedDevices.size() > 0) {
         for (BluetoothDevice device : pairedDevices) {
            String device_id = device.getName() + " - " + device.getAddress(); 
            mpairedDevicesArrayAdapter.add(device_id);
            devicesMap.put(device_id, device);
         }
      }
   }
        

   protected void onActivityResult(int requestCode, int resultCode, Intent data) {
      if (requestCode==REQUEST_ENABLE_BT) {
         if (resultCode==Activity.RESULT_OK) {
            fillBondedDevices();
         } else {
         	Toast.makeText(this, "Failed to enable Bluetooth adapter!", Toast.LENGTH_LONG).show();
         }
      } else {
         Toast.makeText(this, "Unknown RequestCode " + requestCode, Toast.LENGTH_LONG).show();
      }
   }
    
   protected void onDestroy() {
      if (receiverWasRegistered) unregisterReceiver(mReceiver);
      super.onDestroy();
   }


   private class ConnectThread extends Thread {
       private final BluetoothSocket mmSocket;
       private final BluetoothDevice mmDevice;
       private final Conversation conversation;
    
       public ConnectThread(BluetoothDevice device, Conversation conversation) {
           // Use a temporary object that is later assigned to mmSocket,
           // because mmSocket is final
           BluetoothSocket tmp = null;
           mmDevice = device;
           this.conversation = conversation;
    
           // Get a BluetoothSocket to connect with the given BluetoothDevice
           try {
               // UUID is the app's UUID string, also used by the server code
               tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(ReceiveKeyByBluetoothActivity.UUID_STRING));
           } catch (IOException e) {
              ((TFApp)(SendKeyBatchByBluetoothActivity.this.getApplication())).logException(e);
           }
           mmSocket = tmp;
       }
    
       public void run() {
           // Cancel discovery because it will slow down the connection
           BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
    
           try {
               // Connect the device through the socket. This will block
               // until it succeeds or throws an exception
               mmSocket.connect();
           } catch (IOException connectException) {
               try {
                   mmSocket.close();
               } catch (IOException e) {
                  ((TFApp)(SendKeyBatchByBluetoothActivity.this.getApplication())).logException(e);
               }
              ((TFApp)(SendKeyBatchByBluetoothActivity.this.getApplication())).logException(connectException);
               return;
           }
    
           try {

               OutputStream out = mmSocket.getOutputStream();
               if (out != null) {
                  ArrayList<Messagekey> keys = new ArrayList<Messagekey>();
                  StringBuffer transferstringbuilder = new StringBuffer();
                  Log.d(TFApp.LOGKEY, "creating keys");
                  for(int i=0; i<BATCH_KEY_GENERATION_COUNT_PER_DIRECTION; i++) {
                     Messagekey rKey = new Messagekey(true);
                     Messagekey sKey = new Messagekey(false);
                     transferstringbuilder.append(rKey.getWebsafeSerialization());
                     transferstringbuilder.append(KEY_SEPARATOR);
                     transferstringbuilder.append(sKey.getWebsafeSerialization());
                     transferstringbuilder.append(KEY_SEPARATOR);
                     keys.add(rKey);
                     keys.add(sKey);
                  }

                  Log.d(TFApp.LOGKEY, "sending keys over the air");
                  String payload = transferstringbuilder.toString();

                  long startnow;
                  long endnow;
                  startnow = android.os.SystemClock.uptimeMillis();
                  out.write(payload.getBytes());
                  endnow = android.os.SystemClock.uptimeMillis();
                  double transferrate_estimate = 0;
                  if (endnow-startnow>0) {
                     transferrate_estimate = 1000*payload.getBytes().length/(1024*(endnow-startnow));
                  }
                  Log.d(TFApp.LOGKEY, String.format("sending %d bytes over air took %d ms, estimated speed %f KBytes/sec.",
                                                                              payload.getBytes().length, (endnow-startnow), transferrate_estimate));

                  startnow = android.os.SystemClock.uptimeMillis();
                  int count = ((TFApp)(SendKeyBatchByBluetoothActivity.this.getApplication())).getDAH().
                                                      bulkAddKeysToConversationAndSetExchanged(SendKeyBatchByBluetoothActivity.this.conversation, keys);

                  endnow = android.os.SystemClock.uptimeMillis();
                  Log.d(TFApp.LOGKEY, String.format("storing %d keys took %d ms.",
                                                                              count, (endnow-startnow)));
                  mHandler.obtainMessage(MESSAGE_SENT, -1, -1, null).sendToTarget();
               }
           } catch(IOException e) {
              ((TFApp)(SendKeyBatchByBluetoothActivity.this.getApplication())).logException(e);
           }
       }
    
       /** Will cancel an in-progress connection, and close the socket */
       public void cancel() {
           try {
               mmSocket.close();
           } catch (IOException e) {
              ((TFApp)(SendKeyBatchByBluetoothActivity.this.getApplication())).logException(e);
           }
       }
   }

}
