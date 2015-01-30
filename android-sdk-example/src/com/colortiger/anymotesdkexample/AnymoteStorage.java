package com.colortiger.anymotesdkexample;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;

import android.content.Context;
import android.content.SharedPreferences;

import com.colortiger.anymotesdk.AnyMoteDevice;

/**
 * Helper class for permanently storing AnyMoteDevice objects 
 * for easier re-connection at a later stage.
 */
public class AnymoteStorage {
	private final static String SHARED_PREF_ID = "anymote_storage";
	private Context mContext;
	
	public AnymoteStorage(Context context) {
		mContext = context;
	}
	
	private SharedPreferences getPrefs() {
		return mContext.getSharedPreferences(SHARED_PREF_ID, Context.MODE_PRIVATE);
	}
	
	/**
	 * Serializes an AnyMoteDevice object to permanent storage 
	 * 
	 * @param device the object to be saved
	 */
	public void save(AnyMoteDevice device) {
		SharedPreferences prefs = getPrefs();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(device.getAddress(), device.toString());
		editor.commit();
	}
	
	/**
	 * Loads the saved AnyMoteDevice objects from the disk.
	 * 
	 * @return the list of unique AnyMoteDevices
	 */
	public List<AnyMoteDevice> list() {
		List<AnyMoteDevice> result = new ArrayList<AnyMoteDevice>();
		SharedPreferences prefs = getPrefs();
		Map<String, ?> all = prefs.getAll();
		for (Object o : all.values()) {
			String raw = o.toString();
			if (raw!=null && raw.trim().length()>0) {
				try {
					result.add(AnyMoteDevice.fromString(raw));
				} catch (Exception e) {
					// TODO: data was corrupted. In real-life cases you should handle this
					e.printStackTrace();
				}
			}
		}
		
		return result;
	}
	
	/**
	 * Removes an AnyMoteDevice object from the permanent storage (cache)
	 * 
	 * @param device the AnyMoteDevice to delete
	 */
	public void delete(AnyMoteDevice device) {
		SharedPreferences prefs = getPrefs();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(device.getAddress(), "");
		editor.commit();
	}
	
	/**
	 * Load an AnyMoteDevice object from disk based on its unique MAC address.
	 * Returns null if this object did not exist.
	 * 
	 * @param macAddress the unique MAC of this AnyMote device
	 * @return the AnyMoteDevice. Returns NULL if the device didn't exist.
	 */
	public AnyMoteDevice get(String macAddress) {
		SharedPreferences prefs = getPrefs();
		String raw = prefs.getString(macAddress, "");
		if (raw.equals("")) {
			return null;
		}
		return AnyMoteDevice.fromString(raw);
	}
}
