# anymote-ios
SDK for using AnyMote Home with your iOS app

This project is a sample of how the AnyMote Home SDK should be used to access all features of AnyMote Home devices. The SDK was written for AnyMote Home, but with certain limitations it also works with Anymotuino Arduino prototypes.

In the case of AnyMote Home devices (as oposed to AnyMotuino Arduino prototypes), all commands can only be executed after an authentication step, which is automatically handled by the SDK in the background.

**How authentication works:**
The AnyMote Home has a *pair* button, which has to be pressed before a new client(phone) can connect. 60 seconds after pressing this button (pairing mode), the first connected client's UDID is added to a list of allowed clients, and is allowed any subsequent connection attempts.

**Methods supported on AnyMote Home**
- send raw IR commands
- record new raw IR commands from original(plastic) remotes
- add a new phone UDID to the list of clients allowed to control this AnyMote

**Methods supported on AnyMotuino Arduino Prototypes**
- send raw IR commands
- record new raw IR commands from original(plastic) remotes


**Discovery**

The first step for using an AnyMote Home device is discovering it. To do that, you will have to do this:

```
AnymoteManager *manager = [AnymoteManager manager];
[appDelegate.manager startDiscoveryWithCompletion:^(AnymoteDevice *device, NSError *error) {
    if (error) {
    	// handle the error
    } else {
        // a device has been discovered
        self.device = device;
    }
}];

```

**Connection**

Connection is as simple as the discovery

```
[self.device connectWithCompletion:^(NSError *error) {
    if (error) {
        // handle the error
    } else {
        // the device has connected
    }
}];
```

**Sending raw IR commands**

AnyMote Home works with InfraRed commands in a raw format, with the exact number of pulse-pairs (on-off) in PWM. This is a format similar to the popular Pronto HEX codes, excepting there's no header and the frequency is not included in the actual command. The command included in the *pattern* variable below is the Samsung TV Volume UP command.

```
NSUInteger frequency = 38028;
NSArray *pattern = @[@172,@171,@21,@65,@21,@65,@21,@65,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@65,@21,@65,@21,@65,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@65,@21,@65,@21,@65,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@65,@21,@65,@21,@65,@21,@65,@21,@65,@21,@1673];
[self.device sendIRPatternWithFrequencyContinuously:frequency pattern:pattern];
```

In real-life use cases you probably want to send IR commands at the press of a button. If you want to allow the user to send a command continuously as a button is being held pressed. You can set the touch down and touch up events to trigger the sending continuously and stopping.

```
// called on UIControlEventTouchDown
- (IBAction)codeButtonTouchedDown:(id)sender {
    [self.device sendIRPatternWithFrequencyContinuously:self.frequency pattern:self.pattern];
}

// called on UIControlEventTouchUpInside
- (IBAction)codeButtonTouchedUp:(id)sender {
    [self.device stopSending];
}

// called on UIControlEventTouchUpOutside
- (IBAction)codeButtonTouchedUpOutside:(id)sender {
    [self.device stopSending];
}
```

For a more (not yet fully) complete documentation, [have a look here](http://colortiger.com/anymote-doc/ios/).
