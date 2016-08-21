package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.model.Messagekey;
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
import java.util.UUID;

import java.io.IOException;
import java.io.OutputStream;

import de.kochon.enrico.secrettalkmessenger.R;

public class SendSingleKeyByBluetoothActivity extends Activity {

	int REQUEST_ENABLE_BT = 1;

	EditText main;
	private ArrayAdapter<String> mNewDevicesArrayAdapter;
	private ArrayAdapter<String> mpairedDevicesArrayAdapter;
   private HashMap<String, BluetoothDevice> devicesMap;

   private Messagekey key;

   private boolean receiverWasRegistered = false;

    
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
               SendSingleKeyByBluetoothActivity.this.devicesMap.put(device_id, btDevice);
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

      setContentView(R.layout.activity_sendsinglekeybybluetooth);

      Intent data = getIntent();
      if (data.hasExtra(ShowKeyActivity.MESSAGEKEY_ID_KEY)) {
         long keyID = data.getLongExtra(ShowKeyActivity.MESSAGEKEY_ID_KEY, -1);
         key = ((TFApp)(this.getApplication())).getDAH().loadMessagekey(keyID);
      }

      if (key == null) {
      	Toast.makeText(this, "Schlüssel kann nicht angezeigt werden.", Toast.LENGTH_LONG).show();
         finish();
      }

      devicesMap = new HashMap<String, BluetoothDevice>();

      main = (EditText) findViewById(R.id.mainTextArea);
      if (main != null) {
         main.setText("make sure your target is in receive mode - _before sending_");
      }
      mpairedDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.rowitem_bluetooth_endpoint);
      mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, R.layout.rowitem_bluetooth_endpoint);
      
      // Hook up the Discover button to its handler
      Button discover = (Button) findViewById(R.id.discoverButton);
      discover.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
               Log.d(TFApp.LOGKEY, "in onClick(" + v + ")");
               // IntentFilter for found devices
               IntentFilter foundFilter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
               // Broadcast receiver for any matching filter
               SendSingleKeyByBluetoothActivity.this.registerReceiver(mReceiver, foundFilter);
               SendSingleKeyByBluetoothActivity.this.receiverWasRegistered=true;
               IntentFilter doneFilter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
               SendSingleKeyByBluetoothActivity.this.registerReceiver(mReceiver, doneFilter);
               BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
               if (BT != null) {
                  BT.startDiscovery();
               }
            }
         });
      
      ListView lvPaired = (ListView) findViewById(R.id.pairedBtDevices);
      lvPaired.setAdapter(mpairedDevicesArrayAdapter);
      lvPaired.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice dev = SendSingleKeyByBluetoothActivity.this.devicesMap.get(mpairedDevicesArrayAdapter.getItem(position));
            Toast.makeText(SendSingleKeyByBluetoothActivity.this, dev.getName(), Toast.LENGTH_SHORT).show();
            ConnectThread ct = new ConnectThread(dev, SendSingleKeyByBluetoothActivity.this.key);
            ct.run();
         }
      });

      ListView lvDisco = (ListView) findViewById(R.id.discoBtDevices);
      lvDisco.setAdapter(mNewDevicesArrayAdapter);
      lvDisco.setOnItemClickListener(new AdapterView.OnItemClickListener() {
         @Override
         public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            BluetoothDevice dev = SendSingleKeyByBluetoothActivity.this.devicesMap.get(mNewDevicesArrayAdapter.getItem(position));
            Toast.makeText(SendSingleKeyByBluetoothActivity.this, dev.getName(), Toast.LENGTH_SHORT).show();
            ConnectThread ct = new ConnectThread(dev, SendSingleKeyByBluetoothActivity.this.key);
            ct.run();
         }
      });
      
      BluetoothAdapter BT = BluetoothAdapter.getDefaultAdapter();
      if (BT == null) {
      	String noDevMsg = "This device does not appear to have a Bluetooth adapter, sorry";
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
       private final Messagekey keyToBeExchanged;
    
       public ConnectThread(BluetoothDevice device, Messagekey key) {
           // Use a temporary object that is later assigned to mmSocket,
           // because mmSocket is final
           BluetoothSocket tmp = null;
           mmDevice = device;
           keyToBeExchanged = key;
    
           // Get a BluetoothSocket to connect with the given BluetoothDevice
           try {
               // UUID is the app's UUID string, also used by the server code
               tmp = device.createRfcommSocketToServiceRecord(UUID.fromString(ReceiveKeyByBluetoothActivity.UUID_STRING));
           } catch (IOException e) {
              ((TFApp)(SendSingleKeyByBluetoothActivity.this.getApplication())).logException(e);
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
                  ((TFApp)(SendSingleKeyByBluetoothActivity.this.getApplication())).logException(e);
               }
              ((TFApp)(SendSingleKeyByBluetoothActivity.this.getApplication())).logException(connectException);
               return;
           }
    
           try {
               OutputStream out = mmSocket.getOutputStream();
               if (out != null) {
                  Log.d(TFApp.LOGKEY, "sending key over the air");
                  String payload = keyToBeExchanged.getWebsafeSerialization();
                  out.write(payload.getBytes());
                  key.setIsExchanged(true);
                  Log.d(TFApp.LOGKEY, "saving keyupdate in db");
                  ((TFApp)(SendSingleKeyByBluetoothActivity.this.getApplication())).getDAH().updateKey(key);
               }
           } catch(IOException e) {
              ((TFApp)(SendSingleKeyByBluetoothActivity.this.getApplication())).logException(e);
           }
       }
    
       /** Will cancel an in-progress connection, and close the socket */
       public void cancel() {
           try {
               mmSocket.close();
           } catch (IOException e) {
              ((TFApp)(SendSingleKeyByBluetoothActivity.this.getApplication())).logException(e);
           }
       }
   }

}
