//
//  AnymoteDevice.h
//  AnymoteSDK
//
//  Created by Cristi Habliuc on 01/02/15.
//  Copyright (c) 2015 ColorTiger. All rights reserved.
//

#import <Foundation/Foundation.h>


/// The known device types. Used to differentiate between the Anymote Home and Anymotuinos or other DIY projects
typedef NS_ENUM(NSUInteger, AnymoteDeviceTypes) {
    AnymoteDeviceTypeAnymote = 0,
    AnymoteDeviceTypeOther
};


/**
 
 @discussion 
 
 This class represents Anymote devices. You can discover devices using the <code>AnymoteManager</code> class. Once you have done so, you can connect to and start interacting with those devices.
 
 You can send IR code patterns to them, record patterns from original plastic remotes and manage certain aspects of them.
 
 @see http://colortiger.com
 
 */
@interface AnymoteDevice : NSObject

/// The device CBPeripheral identifier
@property (copy, nonatomic, readonly) NSUUID *identifier;

/**
 
 @discussion
 The device type. This is necessary because the Anymote Home has extra functionality, such as IR recording, authentication (which is mandatory before use)
 
 */
@property (nonatomic, readonly) AnymoteDeviceTypes type;

/// The device MAC address
@property (copy, nonatomic, readonly) NSString *address;

/**
 
 @brief The device's firmware version.
 
 @note Not implemented yet
 
 */
@property (copy, nonatomic, readonly) NSString *firmwareVersion;

/**
 
 @brief The device's hardware version.
 
 @note Not implemented yet
 
 */
@property (copy, nonatomic, readonly) NSString *hardwareVersion;

/// The IP address (if it's a proxy)
@property (copy, nonatomic, readonly) NSString *IPAddress;

/// The device's name
@property (copy, nonatomic, readonly) NSString *name;

/// The device's current RSSI (fetched on advertisement)
@property (nonatomic, readonly) int RSSI;

/// Wether the device is currently connected or not
@property (nonatomic, readonly) BOOL connected;



/** 
 
 @discussion
 
 Adds the app auth ID to the Anymote. In order for this to work, the user has to had previously pressed the button on the Anymote device which allows the Anymote to accept new clients.
 
 This is a mandatory step for Anymote Home devices. If a client application doesn't authenticate, the Anymote will reject all other requests.
 
 @param authID the authentication ID, needs to be unique per appplication. You should store it globally after generating it, in the UserDefaults for example. You can set it to <code>nil</code> and the SDK will generate one and store it for you
 @param completion the completion block for the operation
 
 */
- (void)addAuthID:(NSUUID *)authID completion:(void (^)(NSError *error))completion;

/// Attempts to connect to the device
- (void)connectWithCompletion:(void (^)(NSError *error))completion;

/// Disconnects from the device
- (void)disconnectWithCompletion:(void (^)(NSError *error))completion;

/** 
 
 @discussion 
 Sends the IR pattern with the specified parameters once
 
 @note An IR code is specified by its frequency in Hz along with a pattern of ticks to keep the LED on or off. A pattern of @[ @5, @4, @3, @2 ] means that the LED will stay on 5 ticks, then turn off for 4 ticks, then come back on for 3 ticks, then stay off for another 2 ticks.
 
 @param frequency the frequency of the pattern in Hz
 @param pattern the pattern as an array of integer numbers (each number is the number of ticks to keep the IR LED on or off)
 
 */
- (void)sendIRPatternWithFrequency:(NSUInteger)frequency pattern:(NSArray *)pattern;

/**
 
 @brief Sends the IR pattern with the specified parameters, repeats <code>repeatCount</code> times
 
 @param frequency the frequency of the pattern in Hz
 @param pattern the pattern as an array of integer numbers (each number is the number of ticks to keep the IR LED on or off)
 @param repeatCount how many times to repeat the code sending
 
 */
- (void)sendIRPatternWithFrequency:(NSUInteger)frequency pattern:(NSArray *)pattern repeat:(NSUInteger)repeatCount;

/**
 
 @brief Sends the IR pattern with the specified parameters continuously. Stops when you call <code>-stopSending</code>

 @param frequency the frequency of the pattern in Hz
 @param pattern the pattern as an array of integer numbers (each number is the number of ticks to keep the IR LED on or off)

 */
- (void)sendIRPatternWithFrequencyContinuously:(NSUInteger)frequency pattern:(NSArray *)pattern;

/// Stops sending the IR pattern continuously.
- (void)stopSending;

/**
 
 @discussion 
 Instructs the device to go into recording mode. This will wait for a remote to be directed at it and will read the code on the button press and will run the completion block specified. 
 
 If a send code command is received in the meantime, it will return with the appropriate error filled in.
 
 It also has a 60 second timeout, so if a code hasn't been read in that specified timeframe, a timeout error will be returned with the block immediately.
 
 @param completion the completion block to be executed on code read success or if an error has occured.
 
 */
- (void)recordCodeWithCompletion:(void (^)(NSUInteger frequency, NSArray *pattern, NSError *error))completion;

/**
 
 @brief Renames the current Anymote device. Asynchronous.
 
 @param newName the new name for the device
 @param completion the completion block for the operation. Check the <code>success</code> flag for the operation's result
 
 @note Not implemented yet
 
 */
- (void)rename:(NSString *)newName completion:(void (^)(BOOL success))completion;

/**
 
 @brief Fetches the list of allowed IDs by the Anymote device. Asynchronous.
 
 @param completion the completion block.
 
 @note Not implemented yet
 
 */
- (void)fetchListOfAllowedIDsWithCompletion:(void (^)(NSArray *ids, NSError *error))completion;



@end
