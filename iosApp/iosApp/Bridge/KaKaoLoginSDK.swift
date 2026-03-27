import Foundation
import KakaoSDKCommon
import KakaoSDKAuth
import KakaoSDKUser

@objc(KaKaoLoginSDK)
public class KaKaoLoginSDK: NSObject {

    @objc private override init() {
        super.init()
    }

    @objc public static func initSDK() {
        print("KaKaoLoginSDK initSDK")
        KakaoSDK.initSDK(appKey: "d160e887f28e5ecc93c0ee9d631f4267")
    
    }
    
    @objc(requestLoginWithCompletion:)
        public class func requestLogin(
            _ completion: @escaping (_ accessToken: NSString?, _ refreshToken: NSString?, _ error: NSError?) -> Void
        ) {
            print("loginWithKakaoTalk() start")
            // 카카오톡 실행 가능 여부 확인
            if (UserApi.isKakaoTalkLoginAvailable()) {
                print("isKakaoTalkLoginAvailable() true")
                UserApi.shared.loginWithKakaoTalk {(oauthToken, error) in
                    if let error = error as NSError? {
                        completion(nil, nil, error)
                    }
                    else if let token = oauthToken {
                        let accessToken = token.accessToken as NSString   // non-optional, 값 보장됨
                        let refreshToken = token.refreshToken as NSString // optional
                        completion(accessToken, refreshToken, nil)
                    }
                    else {
                        completion(nil, nil, NSError(domain: "com.bium.youngssoo.nid", code: -1,
                                                     userInfo: [NSLocalizedDescriptionKey: String(describing: "loginWithKakaoTalk error")]))
                    }
                }
            } else {
                UserApi.shared.loginWithKakaoAccount { oauthToken, error in
                    if let error = error as NSError? {
                        completion(nil, nil, error)
                    }
                    else if let token = oauthToken {
                        let accessToken = token.accessToken as NSString   // non-optional, 값 보장됨
                        let refreshToken = token.refreshToken as NSString // optional
                        completion(accessToken, refreshToken, nil)
                    }
                    else {
                        completion(nil, nil, NSError(domain: "com.bium.youngssoo.nid", code: -1,
                                                     userInfo: [NSLocalizedDescriptionKey: String(describing: "loginWithKakaoAccount error")]))
                    }
                }
            }
            
        }
    
    @objc(logoutWithCompletion:)
    public class func logout(_ completion: @escaping (_ error: NSError?) -> Void) {
        UserApi.shared.logout { error in
            if let err = error as NSError? {
                // 실패한 경우
                completion(err)
            } else {
                // 성공한 경우
                completion(nil)
            }
        }
    }

}
