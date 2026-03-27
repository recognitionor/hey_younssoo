import SwiftUI
import KakaoSDKCommon
import KakaoSDKAuth
import Firebase
import UserNotifications
import ComposeApp

@main
struct iOSApp: App {
    @UIApplicationDelegateAdaptor(AppDelegate.self) var delegate
    
    var body: some Scene {
        WindowGroup {
            ComposeView()     .onOpenURL { url in
                NSLog("[Kakao] SwiftUI onOpenURL: %@", url.absoluteString)
                if AuthApi.isKakaoTalkLoginUrl(url) {
                    AuthController.handleOpenUrl(url: url)
                }
            }
        }
    }
}

final class AppDelegate: NSObject, UIApplicationDelegate {

    // (보강) 일부 케이스에 대비해 AppDelegate 경로에서도 받아줌
    func application(_ app: UIApplication,
                     open url: URL,
                     options: [UIApplication.OpenURLOptionsKey : Any] = [:]) -> Bool {
        NSLog("[Kakao] AppDelegate openURL: %@", url.absoluteString)
        if AuthApi.isKakaoTalkLoginUrl(url) {
            return AuthController.handleOpenUrl(url: url)
        }
        return false
    }
    
    func application(_ application: UIApplication,
                         didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey : Any]? = nil) -> Bool {
            
            // 파이어베이스 설정
            FirebaseApp.configure()
            
            // 앱 실행 시 사용자에게 알림 허용 권한을 받음
            UNUserNotificationCenter.current().delegate = self
            
            let authOptions: UNAuthorizationOptions = [.alert, .badge, .sound] // 필요한 알림 권한을 설정
            UNUserNotificationCenter.current().requestAuthorization(
                options: authOptions,
                completionHandler: { _, _ in }
            )
            
            // UNUserNotificationCenterDelegate를 구현한 메서드를 실행시킴
            application.registerForRemoteNotifications()
            
            // 파이어베이스 Meesaging 설정
            Messaging.messaging().delegate = self
            
            return true
        }
    }

    extension AppDelegate: UNUserNotificationCenterDelegate {
        
        // 백그라운드에서 푸시 알림을 탭했을 때 실행
        func application(_ application: UIApplication,
                         didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data) {
            print("APNS token: \(deviceToken)")
            Messaging.messaging().apnsToken = deviceToken
        }
        
        // Foreground(앱 켜진 상태)에서도 알림 오는 설정
        func userNotificationCenter(_ center: UNUserNotificationCenter, willPresent notification: UNNotification, withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void) {
            completionHandler([.list, .banner])
        }
    }

    extension AppDelegate: MessagingDelegate {
        
        // 파이어베이스 MessagingDelegate 설정
        func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
          print("Firebase registration token: \(String(describing: fcmToken))")

          let dataDict: [String: String] = ["token": fcmToken ?? ""]
          NotificationCenter.default.post(
            name: Notification.Name("FCMToken"),
            object: nil,
            userInfo: dataDict
          )
            AppUserCache.shared.updateFCMToken = fcmToken
          // TODO: If necessary send token to application server.
          // Note: This callback is fired at each app startup and whenever a new token is generated.
        }
    }


