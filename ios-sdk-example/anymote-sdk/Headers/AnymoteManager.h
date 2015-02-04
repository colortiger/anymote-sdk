//
//  AnymoteManager.h
//  AnymoteSDK
//
//  Created by Cristi Habliuc on 01/02/15.
//  Copyright (c) 2015 ColorTiger. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "AnymoteDevice.h"


typedef void (^AnymoteManagerDiscoveryCompletionBlock)(AnymoteDevice *device, NSError *error);


@interface AnymoteManager : NSObject

/// The AnymoteManager is intended to be used as a singleton. Use this method to get a reference to the singleton object.
+ (instancetype)manager;

/// Starts the discovery of Anymote devices
- (void)startDiscoveryWithCompletion:(AnymoteManagerDiscoveryCompletionBlock)completion;

/// Stops the current discovery, if one has started
- (void)stopDiscovery;

@end
