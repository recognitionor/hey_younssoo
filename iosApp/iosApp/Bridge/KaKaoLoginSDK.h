// KaKaoLoginSDK.h  (개념적으로 등가 인터페이스)
// 실제 빌드에선 이 파일을 직접 쓰기보다 <YourModuleName>-Swift.h 를 import 하세요.

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN
typedef void (^KaKaoLoginCompletion)(NSString * _Nullable accessToken,
                                     NSString * _Nullable refreshToken,
                                     NSError  * _Nullable error);

@interface KaKaoLoginSDK : NSObject

// Swift의 `public static let shared` → 클래스 프로퍼티(읽기 전용)
@property (class, nonatomic, readonly, strong) KaKaoLoginSDK *shared;

// Swift의 `@objc public static func initSDK()` → 클래스 메서드
+ (void)initSDK;
+ (void)requestLoginWithCompletion:(KaKaoLoginCompletion)completion;
+ (void)logoutWithCompletion:(void (^ _Nonnull)(NSError * _Nullable error))completion;
@end

NS_ASSUME_NONNULL_END
