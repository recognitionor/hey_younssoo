// AppleLoginSDK.h

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

// 1. Completion 정의 부분에 email과 fullName을 추가합니다.
typedef void (^AppleLoginCompletion)(NSString *_Nullable identityToken,
                                     NSString *_Nullable authorizationCode,
                                     NSString *_Nullable email,
                                     NSString *_Nullable fullName,
                                     NSError *_Nullable error);

@interface AppleLoginSDK : NSObject

@property(class, nonatomic, readonly, strong) AppleLoginSDK *shared;

// 2. 위에서 정의한 5개짜리 AppleLoginCompletion을 사용하도록 유지합니다.
+ (void)requestLoginWithCompletion:(AppleLoginCompletion)completion;

+ (void)logoutWithCompletion:
    (void (^_Nonnull)(NSError *_Nullable error))completion;

@end

NS_ASSUME_NONNULL_END
