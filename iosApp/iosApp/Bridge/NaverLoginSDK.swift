import Foundation
import NidThirdPartyLogin

@objc(NaverLoginSDK)
public class NaverLoginSDK: NSObject {
    @objc public static let shared = NaverLoginSDK()

    @objc private override init() {
        super.init()

    }

    @objc public static func initSDK() {
        NidOAuth.shared.initialize()
        NidOAuth.shared.setLoginBehavior(.appPreferredWithInAppBrowserFallback)

    }

    @objc(requestLoginWithCompletion:)
    public class func requestLogin(
        _ completion: @escaping (_ accessToken: NSString?, _ refreshToken: NSString?, _ error: NSError?) -> Void
    ) {
        NidOAuth.shared.requestLogin { result in
            switch result {
            case .success(let loginResult):
                let access = loginResult.accessToken.tokenString as NSString
                let refresh = loginResult.refreshToken.tokenString as NSString
                completion(access, refresh, nil)
            case .failure(let err):
                completion(nil, nil, err as NSError)
            }
        }
    }
    
    @objc(logoutWithCompletion:)
    public class func logout(_ completion: @escaping (_ error: NSError?) -> Void) {
        NidOAuth.shared.disconnect { result in
            switch result {
            case .success:
                // 필요 시 메인 스레드 보장
                // DispatchQueue.main.async { completion(nil) }
                completion(nil)
            case .failure(let e):
                // NidError가 Error를 채택하면 대부분 as NSError 브릿지 가능
                let nsError = (e as NSError? )
                    ?? NSError(domain: "com.bium.youngssoo.nid", code: -1,
                               userInfo: [NSLocalizedDescriptionKey: String(describing: e)])
                completion(nsError)
            }
        }
    }
}
