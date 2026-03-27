// NaverLoginSDK.h

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef void (^NaverLoginCompletion)(NSString * _Nullable accessToken,
                                     NSString * _Nullable refreshToken,
                                     NSError  * _Nullable error);

@interface NaverLoginSDK : NSObject

@property (class, nonatomic, readonly, strong) NaverLoginSDK *shared;

+ (void)initSDK;
+ (void)requestLoginWithCompletion:(NaverLoginCompletion)completion;
+ (void)logoutWithCompletion:(void (^ _Nonnull)(NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END
