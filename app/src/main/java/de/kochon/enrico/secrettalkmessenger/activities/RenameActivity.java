package de.kochon.enrico.secrettalkmessenger.activities;

import de.kochon.enrico.secrettalkmessenger.R;
import de.kochon.enrico.secrettalkmessenger.SecretTalkMessengerApplication;
import de.kochon.enrico.secrettalkmessenger.backend.ConfigHelper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import android.net.Uri;

import java.util.ArrayList;


public class RenameActivity extends Activity implements OnClickListener {
	
	private EditText editNewName;
	private Button ok;

   public static final String STRINGPARAM_KEY = "RENAME_ACTIVITY_PARAMLIST_CAPTION_OLD";
   public static final String STRINGRESULT_KEY = "RENAME_ACTIVITY_RESULT";
	
	
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.activity_rename);

      String title = "Internal error!";
      String oldname = "Internal error!";
      Intent data = getIntent();
      if (data.hasExtra(STRINGPARAM_KEY)) {
         ArrayList<String> params = data.getStringArrayListExtra(STRINGPARAM_KEY);
         if ((null != params) && (2 == params.size())) {
            title = params.get(0);
            oldname = params.get(1);
         }
      }
		
      TextView viewTitle = (TextView) findViewById(R.id.activityRenameTitle);
      if (null != viewTitle) {
         viewTitle.setText(title);
      }

      TextView viewOldName = (TextView) findViewById(R.id.activityRenameTextOldName);
      if (null != viewOldName) {
         viewOldName.setText(oldname);
      }

		editNewName = (EditText) findViewById(R.id.activityRenameEditNewName);

		ok = (Button) findViewById(R.id.activityRenameButtonOk);
      if (ok != null) {
         ok.setOnClickListener(this);
      }
	}


	@Override
	public void onClick(View v) {
		if (v == ok) {
		   Intent reply = new Intent();
			String newName = editNewName.getText().toString();
         Bundle result = new Bundle();
         result.putString(STRINGRESULT_KEY, newName); 
         reply.putExtras(result); 
			setResult(RESULT_OK, reply);
		   finish();
		}
		
	}
	
	
}
