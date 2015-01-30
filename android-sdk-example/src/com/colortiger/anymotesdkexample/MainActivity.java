package com.colortiger.anymotesdkexample;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.colortiger.anymotesdk.AnyMoteDevice;
import com.colortiger.anymotesdk.AnyMoteManager;
import com.colortiger.anymotesdk.OnScanListener;
import com.colortiger.anymotesdk.wifi.WifiManager;

public class MainActivity extends Activity {
	private Button scan;
	private ListView listView;
	private TextView listFooter;
	private LinearLayout savedContainer;
	ArrayList<AnyMoteDevice> devices = new ArrayList<AnyMoteDevice>();
	ArrayAdapterItem adapter;
	private boolean isScanning = false;
	private Handler mHandler = new Handler();
	private AnymoteStorage storage = null;
	private float dip = 1;
	
	{
		// set this to TRUE to enable logging for most BLE actions 
		// done by the AnyMoteManager
		AnyMoteManager.DEBUG_ON = false;   
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		storage = new AnymoteStorage(this);
		scan = (Button)findViewById(R.id.scan);
		listView = (ListView)findViewById(R.id.list);
		savedContainer = (LinearLayout)findViewById(R.id.savedContainer);
		adapter = new ArrayAdapterItem(this, devices);
		listView.setAdapter(adapter);
		
		dip = getResources().getDisplayMetrics().density;
		
		// the listFooter object is that row with the "scanning..." text that shows
		// while the AnyMoteManager is searching for AnyMote devices
		listFooter = (TextView)LayoutInflater.from(this).inflate(R.layout.text_row, null);
		listFooter.setGravity(Gravity.CENTER);
		listFooter.setText(R.string.scanning);
		listFooter.setTextSize(10);
		
		scan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				startScan();
			}
		});
		
		listView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
				stopScan();
				AnyMoteDevice current = devices.get(position);
				goToAnyMote(current);
			}
		});
		
		TextView txt = (TextView)findViewById(R.id.text);
		txt.setText("http://"+getIPAddress(true)+":9009/anymote/");
	}
	
	private void goToAnyMote(AnyMoteDevice dev) {
		Intent i = new Intent(MainActivity.this, AnymoteActivity.class);
		i.putExtra("anymote", dev);
		startActivity(i);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		// load and show the list of saved AnyMote devices
		savedContainer.removeAllViews();
		List<AnyMoteDevice> devices = storage.list();
		
		LayoutInflater inflater = LayoutInflater.from(this);
		for (final AnyMoteDevice dev : devices) {
			TextView row = (TextView)inflater.inflate(R.layout.text_row, null);
			row.setText(dev.getName());
			row.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					goToAnyMote(dev);
				} 
			});
			LinearLayout.LayoutParams rowLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
			savedContainer.addView(row, rowLp);
			
			LinearLayout.LayoutParams sepLp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, (int)(dip * 1));
			View separator = new View(this);
			separator.setBackgroundColor(Color.parseColor("#D8D8D8"));
			savedContainer.addView(separator, sepLp);
		}
	}
	
	// thanks to user @Whome
	// http://stackoverflow.com/questions/6064510/how-to-get-ip-address-of-the-device
	public static String getIPAddress(boolean useIPv4) {
        try {
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                List<InetAddress> addrs = Collections.list(intf.getInetAddresses());
                for (InetAddress addr : addrs) {
                    if (!addr.isLoopbackAddress()) {
                        String sAddr = addr.getHostAddress().toUpperCase();
                        boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr); 
                        if (useIPv4) {
                            if (isIPv4) 
                                return sAddr;
                        } else {
                            if (!isIPv4) {
                                int delim = sAddr.indexOf('%'); // drop ip6 port suffix
                                return delim<0 ? sAddr : sAddr.substring(0, delim);
                            }
                        }
                    }
                }
            }
        } catch (Exception ex) { } // for now eat exceptions
        return "";
    }
	
	/**
	 * Start the scanning process for AnyMote Home and Anymotuino devices
	 */
	public void startScan() {
		if (isScanning) {
			return;
		}
		isScanning = true;
		devices.clear();
		
		adapter.notifyDataSetChanged();
		try {
			listView.addFooterView(listFooter);
		} catch (Exception e) {
			// this will fail if the footer was already added. 
			// It's fine to silently fail.
		}
		
		AnyMoteManager manager = AnyMoteManager.getInstance(this);
		manager.startAnyMoteScan(new OnScanListener() {
			@Override
			public void onBleDeviceFound(final AnyMoteDevice device) {
				mHandler.post(new Runnable() {
					public void run() {
						devices.add(device); 
						adapter.notifyDataSetChanged();
					}
				});
			}
			
			@Override
			public void onScanStopped() {
				stopScan();
			}
		});
		
		scan.setText(R.string.stop_scan);
		scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				stopScan();
			}
		});
	}
	
	public void stopScan() {
		if (!isScanning) {
			return; 
		}
		isScanning = false;
		AnyMoteManager.getInstance(this).stopAnyMoteScan();
		try {
			listView.removeFooterView(listFooter);
		} catch (Exception e) {}
		scan.setText(R.string.start_scan);
		scan.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				startScan();
			}
		});
	}
	
	public class ArrayAdapterItem extends ArrayAdapter<AnyMoteDevice> {
	    Context mContext;
	    ArrayList<AnyMoteDevice> data = null;
	    LayoutInflater inflater;

	    public ArrayAdapterItem(Context mContext, ArrayList<AnyMoteDevice> data) {
	        super(mContext, 0, data);

	        this.mContext = mContext;
	        this.data = data;
	        inflater = LayoutInflater.from(mContext);
	    }

	    @Override
	    public View getView(int position, View convertView, ViewGroup parent) {
	    	if(convertView==null){
	            convertView = (TextView)inflater.inflate(R.layout.text_row, null);
	        }

	        String objectItem = data.get(position).getName();
	        TextView textViewItem = (TextView) convertView;
	        textViewItem.setText(objectItem);
	        return convertView;
	    }
	}
}














