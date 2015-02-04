//
//  ViewController.m
//  AnymoteSDKExample
//
//  Created by Cristi Habliuc on 30/01/15.
//  Copyright (c) 2015 ColorTiger. All rights reserved.
//

#import "ViewController.h"
#import "AppDelegate.h"

@interface ViewController ()
@property (weak, nonatomic) IBOutlet UILabel *stateLabel;
@property (weak, nonatomic) IBOutlet UILabel *deviceNameLabel;
@property (weak, nonatomic) IBOutlet UIButton *testCodeButton;
@property (weak, nonatomic) IBOutlet UIButton *recordCodeButton;
@property (weak, nonatomic) IBOutlet UILabel *codeLabel;
@property (strong, nonatomic) AnymoteDevice *device;
@property (nonatomic) NSUInteger frequency;
@property (strong, nonatomic) NSArray *pattern;
@end

@implementation ViewController

#pragma mark - Actions

- (IBAction)codeButtonTouchedDown:(id)sender {
    self.stateLabel.text = @"Sending Code...";
    [self.device sendIRPatternWithFrequencyContinuously:self.frequency pattern:self.pattern];
}

- (IBAction)codeButtonTouchedUp:(id)sender {
    self.stateLabel.text = @"Stopped Sending Code";
    [self.device stopSending];
}

- (IBAction)codeButtonTouchedUpOutside:(id)sender {
    self.stateLabel.text = @"Stopped Sending Code";
    [self.device stopSending];
}

- (IBAction)recordCodeButtonTapped:(id)sender {
    [self.device recordCodeWithCompletion:^(NSUInteger frequency, NSArray *pattern, NSError *error) {
        if (error) {
            UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error" message:error.localizedDescription delegate:nil cancelButtonTitle:@"Okay" otherButtonTitles:nil];
            [alert show];
        } else {
            self.frequency = frequency;
            self.pattern = pattern;
            self.codeLabel.text = [self.pattern componentsJoinedByString:@","];
            [self.view layoutIfNeeded];
            
            self.stateLabel.text = @"Recorded code";
            self.stateLabel.textColor = [UIColor greenColor];
        }
    }];

    self.stateLabel.text = @"Waiting for code";
    self.stateLabel.textColor = [UIColor blackColor];
}

- (IBAction)reconnectButtonTapped:(id)sender {
    [self connectToCurrentDevice];
}

- (IBAction)disconnectButtonTapped:(id)sender {
    [self.device disconnectWithCompletion:^(NSError *error) {
        // update the UI
        self.stateLabel.textColor = [UIColor lightGrayColor];
        self.stateLabel.text = @"Disconnected";
        self.testCodeButton.enabled = NO;
        self.recordCodeButton.enabled = NO;
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Reconnect" style:UIBarButtonItemStyleDone target:self action:@selector(reconnectButtonTapped:)];
    }];
}

#pragma mark - Logic

- (void)connectToCurrentDevice {
    void (^runOnConnection)() = ^{
        if (self.device.type == AnymoteDeviceTypeAnymote) {
            // need to authenticate on connection
            [self.device addAuthID:nil completion:^(NSError *error) {
                if (error) {
                    NSLog(@"error: %@",error);
                }
            }];
        }
        
        self.stateLabel.text = @"Connected to Device";
        self.stateLabel.textColor = [UIColor greenColor];
        
        self.testCodeButton.enabled = YES;
        self.recordCodeButton.enabled = YES;
        
        self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Disconnect" style:UIBarButtonItemStyleDone target:self action:@selector(disconnectButtonTapped:)];

    };
    
    if (self.device.connected) {
        runOnConnection();
    } else {
        [self.device connectWithCompletion:^(NSError *error) {
            if (error) {
                UIAlertView *alert = [[UIAlertView alloc] initWithTitle:@"Error" message:error.localizedDescription delegate:nil cancelButtonTitle:@"Okay" otherButtonTitles:nil];
                [alert show];
                
            } else {
                runOnConnection();
            }
        }];
    }
}

#pragma mark - View Controller

- (void)viewDidLoad {
    [super viewDidLoad];
    
    self.title = @"Anymote SDK Example";

    self.frequency = 38028;
    self.pattern = @[@172,@171,@21,@65,@21,@65,@21,@65,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@65,@21,@65,@21,@65,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@65,@21,@65,@21,@65,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@22,@21,@65,@21,@65,@21,@65,@21,@65,@21,@65,@21,@1673];
    
    self.codeLabel.text = [self.pattern componentsJoinedByString:@","];
}

- (void)viewDidAppear:(BOOL)animated {
    [super viewDidAppear:animated];
    
    self.navigationController.navigationBar.translucent = NO;
    
    self.stateLabel.text = @"Scanning...";
    AppDelegate *appDelegate = (AppDelegate *)[UIApplication sharedApplication].delegate;
    [appDelegate.manager startDiscoveryWithCompletion:^(AnymoteDevice *device, NSError *error) {
        
        if (error) {
            self.stateLabel.text = [NSString stringWithFormat:@"ERROR: %@",error.localizedDescription];
            self.stateLabel.textColor = [UIColor redColor];
        } else {
            NSLog(@"discovered device = %@",device);
            
            self.device = device;
            
            self.deviceNameLabel.text = self.device.name;
            self.stateLabel.text = @"Discovered device";
            self.stateLabel.textColor = [UIColor greenColor];
            
            self.navigationItem.rightBarButtonItem = [[UIBarButtonItem alloc] initWithTitle:@"Connect" style:UIBarButtonItemStyleDone target:self action:@selector(reconnectButtonTapped:)];
        }
    }];
    
    [self.view layoutIfNeeded];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
    // Dispose of any resources that can be recreated.
}

@end
