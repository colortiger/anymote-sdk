package com.colortiger.anymotesdkexample;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.colortiger.anymotesdk.AnyMoteDevice;
import com.colortiger.anymotesdk.AnyMoteManager;
import com.colortiger.anymotesdk.IrButtonTouchListener;
import com.colortiger.anymotesdk.OnAuthIdListedListener;
import com.colortiger.anymotesdk.OnConnectionChangeListener;
import com.colortiger.anymotesdk.OnRecordListener;
import com.colortiger.anymotesdk.ble.OnValueReadListener;
import com.colortiger.anymotesdk.util.Util;

public class AnymoteActivity extends Activity implements OnConnectionChangeListener {
	private AnyMoteDevice anymote;
	private Button rename, learn, save, delete;
	private Button samsungVolUp, samsungVolDown, lgVolUp, lgVolDown;
	private Button samsungChUp, samsungChDown, lgChUp, lgChDown;
	private TextView state, hw, fw;
	private Handler mHandler = new Handler();
	private AnymoteStorage storage = null;
	
	int frequency = 38000;
	
	// the raw IR codes pulse pattern for Samsung and LG volumes and channels
	int[] samsungVolumeUp =   {172,171,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,65,21,65,21,1673};
	int[] samsungVolumeDown = {172,171,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,22,21,65,21,22,21,22,21,22,21,22,21,22,21,22,21,65,21,22,21,65,21,65,21,65,21,65,21,3673};
	int[] lgVolumeUp = {341,173,20,22,22,21,22,64,22,21,21,21,22,22,22,21,22,21,21,65,21,65,21,21,22,64,22,63,22,63,22,64,22,64,22,21,22,64,22,21,21,21,22,22,22,21,21,21,22,22,22,63,22,22,21,64,21,64,21,65,21,65,21,64,21,65,21,1548};
	int[] lgVolumeDown = {340,173,21,22,22,21,22,64,22,21,21,21,22,22,22,21,21,21,22,64,22,64,22,21,22,64,22,63,22,63,22,64,22,64,22,63,22,63,22,21,21,21,22,21,22,21,21,22,21,21,22,21,22,21,21,64,21,64,21,65,21,65,21,64,21,65,21,1548};
	int[] samsungChannelUp =   {172,171,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,22,21,65,21,22,21,22,21,65,21,22,21,22,21,22,21,65,21,22,21,65,21,65,21,22,21,65,21,65,21,65,21,1673};
	int[] samsungChannelDown = {172,171,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,65,21,22,21,22,21,22,21,65,21,65,21,65,21,65,21,22,21,65,21,65,21,65,21,1673};
	int[] lgChannelUp = {340,173,21,22,22,21,21,65,21,21,22,21,22,22,21,22,21,21,22,64,22,64,22,21,22,64,22,64,21,63,22,64,22,64,22,21,21,21,22,22,22,21,21,21,22,22,22,21,21,21,22,64,22,64,22,63,22,63,22,64,22,64,22,63,22,64,22,1547};
	int[] lgChannelDown = {340,173,21,22,21,22,21,65,21,21,22,21,22,22,21,21,22,21,22,64,22,64,22,21,21,65,21,64,21,64,21,65,21,65,21,64,21,22,22,21,22,21,21,22,22,21,22,21,22,22,21,21,22,64,22,63,22,63,22,64,22,64,22,63,22,64,22,1547};
	
	{
		// set this to TRUE to enable logging for most BLE actions 
		// done by the AnyMoteManager
		AnyMoteManager.DEBUG_ON = false;
	} 
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_anymote_connected);
		
		storage = new AnymoteStorage(this.getApplicationContext());
		
		anymote = (AnyMoteDevice)getIntent().getSerializableExtra("anymote");
		anymote.setContext(getApplicationContext());
		if (getActionBar() != null) {
			// set the current AnyMote device name as the screen title,
			// when an ActionBar is available
			getActionBar().setTitle(anymote.getName());
		}
		
		loadViews();
		
		anymote.addConnectionChangeListener(this); 
		if (anymote.isConnected()) {
			onConnected();
		} else {
			anymote.connect();
		}
		
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				storage.save(anymote);
				delete.setEnabled(true);
			}
		});
		
		if (storage.get(anymote.getAddress()) != null) {
			delete.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					delete.setEnabled(false);
					storage.delete(anymote);
				}
			});
		}
	}
	
	private void loadViews() {
		// get references to the buttons on the screen - see activity_anymote_connected.xml
		rename			= (Button)findViewById(R.id.rename);
		samsungVolUp 	= (Button)findViewById(R.id.volUp);
		samsungVolDown 	= (Button)findViewById(R.id.volDown);
		lgVolUp 		= (Button)findViewById(R.id.lgVolUp);
		lgVolDown 		= (Button)findViewById(R.id.lgVolDown);
		
		samsungChUp 	= (Button)findViewById(R.id.chUp);
		samsungChDown 	= (Button)findViewById(R.id.chDown);
		lgChUp 			= (Button)findViewById(R.id.lgChUp);
		lgChDown 		= (Button)findViewById(R.id.lgChUp);
		
		learn 			= (Button)findViewById(R.id.learn);
		state 			= (TextView)findViewById(R.id.state);
		hw 				= (TextView)findViewById(R.id.hw);
		fw 				= (TextView)findViewById(R.id.fw);
		save 			= (Button)findViewById(R.id.save);
		delete 			= (Button)findViewById(R.id.delete);
	}
	
	@Override
	protected void onDestroy() {
		// disconnect from the AnyMote device as you leave the activity
		anymote.removeConnectionChangeListener(this);
		anymote.disconnect();
		
		super.onDestroy();
	}
	
	@Override
	public void onConnected() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				// set the click/touch listeners on each of the buttons
				setListeners();
				
				// enable all buttons and options on the screen
				setButtonsEnabled(true);
				
				// load and show the current hardware and firmware version
				if (hw.getText().length() < 3) { // hardware version wasn't loaded
					loadVersions();
				}
				
				state.setText("connected");
			}
		});
	} 
	
	private void loadVersions() {
		anymote.getHardwareVersion(new OnValueReadListener(){
			@Override
			public void onStringRead(final String value) {
				mHandler.post(new Runnable() {
					public void run() {
						hw.setText("Hardware Version: "+value);
					}
				});
			}
		});
		anymote.getFirmwareVersion(new OnValueReadListener(){
			@Override
			public void onStringRead(final String value) {
				mHandler.post(new Runnable() {
					public void run() {
						fw.setText("Firmware Version: "+value);
					}
				});
			}
		});
	}
	
	@Override
	public void onDisconnected() {
		mHandler.post(new Runnable() {
			@Override
			public void run() {
				state.setText("disconnected");
			}
		});
	}
	
	public void setButtonsEnabled(boolean enabled) {
		rename.setEnabled(enabled);
		samsungVolUp.setEnabled(enabled);
		samsungVolDown.setEnabled(enabled);
		lgVolUp.setEnabled(enabled);
		lgVolDown.setEnabled(enabled);
		
		samsungChUp.setEnabled(enabled);
		samsungChDown.setEnabled(enabled);
		lgChUp.setEnabled(enabled);
		lgChDown.setEnabled(enabled);
		
		findViewById(R.id.listAuth).setEnabled(enabled);
		findViewById(R.id.addAuth).setEnabled(enabled);
		
		learn.setEnabled(enabled);
	}
	
	public void setListeners() {
		/**
		 * The IrButtonTouchListener is a specially-crafted listener that sends an IR command
		 * continuously as long as you hold the button pressed
		 */
		samsungVolUp.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, samsungVolumeUp));
		samsungVolDown.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, samsungVolumeDown));
		lgVolUp.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, lgVolumeUp));
		lgVolDown.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, lgVolumeDown));
		
		samsungChUp.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, samsungChannelUp));
		samsungChDown.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, samsungChannelDown));
		lgChUp.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, lgChannelUp));
		lgChDown.setOnTouchListener(new IrButtonTouchListener(anymote, frequency, lgChannelDown));
		
		learn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startRecording(); 
			}
		});
		rename.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				rename();
			}
		});
		findViewById(R.id.listAuth).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startShowingAuthList();
			}
		});
		
		findViewById(R.id.addAuth).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showAddAuthPopup();
			}
		});
	}
	
	/**
	 * Loads and shows the list of phone unique IDs that are allowed to send 
	 * InfraRed commands through this AnyMote Home device. 
	 * Note that this method is not supported on Anymotuino (Arduino prototypes)
	 */
	View authOverlay = null;
	public void startShowingAuthList() {
		if (authOverlay != null) {
			authOverlay.setVisibility(View.GONE);
		}
		authOverlay = LayoutInflater.from(this).inflate(R.layout.overlay_list, null);
		addContentView(authOverlay, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
		
		final ListView listView = (ListView)authOverlay.findViewById(R.id.listView);
		Button close = (Button)authOverlay.findViewById(R.id.button);
		close.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				authOverlay.setVisibility(View.GONE);
			}
		});
		
		final ArrayList<BleAuthItemId> data = new ArrayList<AnymoteActivity.BleAuthItemId>();
		final ArrayAdapterItem adapter = new ArrayAdapterItem(this, data);
		listView.setAdapter(adapter);
		
		// start loading the list of phone IDs allowed to send commands through this AnyMote
		anymote.listAllowedIds(new OnAuthIdListedListener() {
			@Override
			public void onAuthIdFound(byte[] id) {
				final BleAuthItemId bId = new BleAuthItemId();
				bId.id = id;
				
				listView.post(new Runnable() {
					public void run() {
						data.add(bId);
						adapter.notifyDataSetChanged();
					}
				});
			}
		});
		
		// prompt to delete a phone ID from the list 
		listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
				final byte[] id = data.get(arg2).id;
				confirmDeleteAuthId(id);
			}
		});
	}
	
	public void confirmDeleteAuthId(final byte[] id) {
		new AlertDialog.Builder(this)
		.setTitle("Warning")
		.setMessage("Are you sure you want to remove this ID from the list of IDs allowed to use the AnyMote ["+anymote.getName()+"] ?")
		.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(AnymoteActivity.this, "done", Toast.LENGTH_SHORT).show();
				authOverlay.setVisibility(View.GONE);
				anymote.removeAuthId(id);
			}
		})
		.setNegativeButton("Cancel", null)
		.create().show();
	}
	
	// adds a phone UDID to the list of ids allowed to send commands
	// through this AnyMote device
	public void showAddAuthPopup() {
		final EditText edit = new EditText(this);
		edit.setHint("auth id here");
		edit.setMaxLines(1);
		
		new AlertDialog.Builder(this)
		.setTitle("Add auth id")
		.setMessage("Enter the new auth ID as a string, and it will automatically MD5 hashed and added to the list")
		.setView(edit)
		.setPositiveButton("Add", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(AnymoteActivity.this, "done", Toast.LENGTH_SHORT).show();
				authOverlay.setVisibility(View.GONE);
				anymote.addAuthId(Util.hash20(edit.getText().toString()));
			}
		})
		.setNegativeButton("Cancel", null)
		.create().show();
	}
	
	// record / learn a new IR command from a plastic remote
	public void startRecording() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Waiting for record");
		alert.setMessage("You can now point your plastic remote at the AnyMote, and press the button you want to record");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// do nothing. The code was automatically saved upon recording
		  	}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				anymote.cancelRecording();
			}
		});

		// start the recording process
		anymote.recordIrPattern(new OnRecordListener() {
			@Override
			public void onTimeout() {
				Toast.makeText(AnymoteActivity.this, "recording timed out", Toast.LENGTH_LONG).show();
				findViewById(R.id.sendLearned).setEnabled(false);
			}
			
			@Override
			public void onPatternRecorded(int frequency, int[] pattern) {
				findViewById(R.id.sendLearned).setEnabled(true);
				String cmd = frequency+",";
				for (int i : pattern) {
					cmd+= i+",";
				}
				input.setText(cmd);
				findViewById(R.id.sendLearned).setOnTouchListener(new IrButtonTouchListener(anymote, frequency, pattern));
			}
		});
		
		alert.show();
	}
	
	// rename the current Bluetooth name of this AnyMote device 
	// Note: not supported on Arduino prototypes
	public void rename() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);

		alert.setTitle("Rename");
		alert.setMessage("Enter a new name");

		// Set an EditText view to get user input 
		final EditText input = new EditText(this);
		alert.setView(input);

		alert.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				String txt = input.getText().toString();
				
				// rename the current AnyMote device
				anymote.rename(txt);
				if (txt.trim().length()>20) {
					txt = txt.trim().substring(0, 20);
				}
				if (getActionBar() != null) {
					getActionBar().setTitle(txt);
				}
		  	}
		});

		alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				// do nothing
			}
		});
		
		alert.show();
	}
	
	public class BleAuthItemId {
		byte[] id;
	}
	
	// adapter for the list 
	public class ArrayAdapterItem extends ArrayAdapter<BleAuthItemId> {
	    Context mContext;
	    ArrayList<BleAuthItemId> data = null;
	    LayoutInflater inflater;

	    public ArrayAdapterItem(Context mContext, ArrayList<BleAuthItemId> data) {
	        super(mContext, 0, data);

	        this.mContext = mContext;
	        this.data = data;
	        this.inflater = LayoutInflater.from(mContext);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	if(convertView==null){
	            convertView = (TextView)inflater.inflate(R.layout.text_row, null);
	        }

	        String objectItem = position+". "+Util.bytesToStr(data.get(position).id);
	        TextView textViewItem = (TextView) convertView;
	        textViewItem.setText(objectItem);
	        return convertView;
	    }
	}
}

