import Foundation
import AuthenticationServices
import FirebaseAuth
import CryptoKit

@objc(AppleLoginSDK)
public class AppleLoginSDK: NSObject {
    @objc public static let shared = AppleLoginSDK()
    
    // Unhashed nonce.
    // To prevent replay attacks with the credential returned from Apple, we include
    // a random string(nonce) in the credential request.
    fileprivate var currentNonce: String?

    @objc private override init() {
        super.init()
    }

    @objc(requestLoginWithCompletion:)
    public class func requestLogin(
        _ completion: @escaping (
            _ identityToken: NSString?,
            _ authorizationCode: NSString?,
            _ email: NSString?,
            _ fullName: NSString?,
            _ error: NSError?
        ) -> Void
    ) {
        let provider = ASAuthorizationAppleIDProvider()
        let request = provider.createRequest()
        
        // 이메일과 이름을 요청하는 스코프 설정
        request.requestedScopes = [.fullName, .email]
        
        // Generate nonce for Firebase Auth
        let nonce = randomNonceString()
        AppleLoginSDK.shared.currentNonce = nonce
        request.nonce = sha256(nonce)

        let controller = ASAuthorizationController(authorizationRequests: [request])
        let delegate = AppleSignInDelegate.shared
        
        // 델리게이트에 결과 처리용 클로저 전달
        delegate.completion = completion
        
        controller.delegate = delegate
        controller.presentationContextProvider = delegate
        controller.performRequests()
    }

    @objc(logoutWithCompletion:)
    public class func logout(_ completion: @escaping (_ error: NSError?) -> Void) {
        do {
            try Auth.auth().signOut()
            completion(nil)
        } catch let signOutError as NSError {
            completion(signOutError)
        }
    }
    
    // MARK: - Helper Functions
    private static func randomNonceString(length: Int = 32) -> String {
        precondition(length > 0)
        var randomBytes = [UInt8](repeating: 0, count: length)
        let errorCode = SecRandomCopyBytes(kSecRandomDefault, randomBytes.count, &randomBytes)
        if errorCode != errSecSuccess {
            fatalError("Unable to generate nonce. SecRandomCopyBytes failed with OSStatus \(errorCode)")
        }
        
        let charset: [Character] =
            Array("0123456789ABCDEFGHIJKLMNOPQRSTUVXYZabcdefghijklmnopqrstuvwxyz-._")
        
        let nonce = randomBytes.map { byte in
            // Pick a random character from the set, wrapping around if needed.
            charset[Int(byte) % charset.count]
        }
        
        return String(nonce)
    }
    
    @available(iOS 13, *)
    private static func sha256(_ input: String) -> String {
        let inputData = Data(input.utf8)
        let hashedData = SHA256.hash(data: inputData)
        let hashString = hashedData.compactMap {
            return String(format: "%02x", $0)
        }.joined()
        
        return hashString
    }
}

// MARK: - Apple Sign In Delegate
class AppleSignInDelegate: NSObject, ASAuthorizationControllerDelegate, ASAuthorizationControllerPresentationContextProviding {
    static let shared = AppleSignInDelegate()

    // completion assumes: (FirebaseIDToken, FirebaseUID, Email, Name, Error)
    var completion: ((NSString?, NSString?, NSString?, NSString?, NSError?) -> Void)?

    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        guard let scene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
              let window = scene.windows.first else {
            return UIWindow()
        }
        return window
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        guard let appleIDCredential = authorization.credential as? ASAuthorizationAppleIDCredential else {
            let error = NSError(domain: "com.bium.youngssoo.apple", code: -1,
                                userInfo: [NSLocalizedDescriptionKey: "Invalid credential type"])
            completion?(nil, nil, nil, nil, error)
            return
        }
        
        guard let nonce = AppleLoginSDK.shared.currentNonce else {
            let error = NSError(domain: "com.bium.youngssoo.apple", code: -1,
                                userInfo: [NSLocalizedDescriptionKey: "Invalid state: A login callback was received, but no login request was sent."])
            completion?(nil, nil, nil, nil, error)
            return
        }
        
        guard let appleIDToken = appleIDCredential.identityToken else {
            let error = NSError(domain: "com.bium.youngssoo.apple", code: -1,
                                userInfo: [NSLocalizedDescriptionKey: "Unable to fetch identity token"])
            completion?(nil, nil, nil, nil, error)
            return
        }
        
        guard let idTokenString = String(data: appleIDToken, encoding: .utf8) else {
            let error = NSError(domain: "com.bium.youngssoo.apple", code: -1,
                                userInfo: [NSLocalizedDescriptionKey: "Unable to serialize token string from data"])
            completion?(nil, nil, nil, nil, error)
            return
        }
        
        // 3. Email & Name (Only available on first login)
        let email = appleIDCredential.email
        var fullName: String? = nil
        if let nameComponents = appleIDCredential.fullName {
            let givenName = nameComponents.givenName ?? ""
            let familyName = nameComponents.familyName ?? ""
            fullName = "\(familyName)\(givenName)".trimmingCharacters(in: .whitespaces)
        }
        
        // 4. Sign in to Firebase (Firebase SDK 11+ uses AuthProviderID enum)
        let credential = OAuthProvider.credential(providerID: .apple,
                                                  idToken: idTokenString,
                                                  rawNonce: nonce)
        
        Auth.auth().signIn(with: credential) { (authResult, error) in
            if let error = error {
                let nsError = error as NSError
                self.completion?(nil, nil, nil, nil, nsError)
                return
            }
            
            guard let user = authResult?.user else { return }
            
            user.getIDToken { (token, error) in
                if let error = error {
                     let nsError = error as NSError
                     self.completion?(nil, nil, nil, nil, nsError)
                     return
                }
                
                // Return Firebase ID Token and UID
                self.completion?(
                    token as NSString?,
                    user.uid as NSString?,
                    email as NSString?, // Might be null on subsequent logins
                    fullName as NSString?, // Might be null on subsequent logins
                    nil
                )
            }
        }
    }

    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        let nsError = error as NSError
        completion?(nil, nil, nil, nil, nsError)
    }
}
