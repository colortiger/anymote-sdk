# anymote-android
SDK for using AnyMote Home with an Android app

This project is a sample of how the AnyMote Home SDK should be used to access all features of AnyMote Home devices. The SDK was written for AnyMote Home, but with certain limitations it also works with Anymotuino Arduino prototypes.

In the case of AnyMote Home devices (as oposed to AnyMotuino Arduino prototypes), all commands can only be executed after an authentication step, which is automatically handled by the SDK in the background.

**How authentication works:**
The AnyMote Home has a *pair* button, which has to be pressed before a new client(phone) can connect. 60 seconds after pressing this button (pairing mode), the first connected client's UDID is added to a list of allowed clients, and is allowed any subsequent connection attempts.

**Methods supported on AnyMote Home**
- send raw IR commands
- record new raw IR commands from original(plastic) remotes
- rename this AnyMote device - this also changes the Bluetooth name that shows during discovery
- get the current firmware version
- get the current hardware version
- list the UDIDs allowed to control this AnyMote
- add a new phone UDID to the list of clients allowed to control this AnyMote
- remove a phone UDID from the list of clients allowed to control this AnyMote

**Methods supported on AnyMotuino Arduino Prototypes**
- send raw IR commands
- record new raw IR commands from original(plastic) remotes


**Discovery**

The first step for using an AnyMote Home device is discovering it. To do that, you will have to use [AnyMoteManager.startAnyMoteScan(OnScanListener scanListener)](https://colortiger.com/anymote-doc/com/colortiger/anymotesdk/AnyMoteManager.html#startAnyMoteScan%28com.colortiger.anymotesdk.OnScanListener%29):

    AnyMoteManager manager = AnyMoteManager.getInstance(this);
	manager.startAnyMoteScan(new OnScanListener() {
		@Override
		public void onBleDeviceFound(final AnyMoteDevice device) {
			// found AnyMote device
		}
		
		@Override
		public void onScanStopped() {
			// handle scan stopped
		}
	});
	

**Connection**

Connection is as simple as the discovery

    AnyMoteDevice device = currentAnyMote;
	device.addConnectionChangeListener(new OnConnectionChangeListener() {
		@Override
		public void onDisconnected() {
			// phone disconnected from AnyMote device
		}
		
		@Override
		public void onConnected() {
			// connection established and ready to be used
		}
	});
	
	
**Sending raw IR commands**

AnyMote Home works with InfraRed commands in a raw format, with the exact number of pulse-pairs (on-off) in PWM. This is a format similar to the popular Pronto HEX codes, excepting there's no header and the frequency is not included in the actual command. The command included in the *pattern* variable below is the Samsung TV Volume UP command.

    AnyMoteDevice device = currentAnyMote;
    int frequency = 38000; // frequency in Hz
    int[] pattern = {172,171,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,22,21,65,21,65,21,65,21,65,21,65,21,1673};
    device.sendIrPattern(frequency, pattern);
    
In real-life use cases you probably want to send IR commands at the press of a button. If you want to allow the user to send a command continuously as a button is being held pressed, there is a helper IrButtonTouchListener that you can use. 

    button.setOnTouchListener(new IrButtonTouchListener(device, frequency, pattern));
    
For a more (not yet fully) complete javadoc, [have a look here](https://colortiger.com/anymote-doc/com/colortiger/anymotesdk/AnyMoteManager.html).
