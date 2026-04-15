import UIKit

final class OrientationLock {
    static let notificationName = Notification.Name("YoungssooOrientationDidChange")
    static var currentMask: UIInterfaceOrientationMask = .portrait
    static weak var activeController: UIViewController?

    static var preferredOrientation: UIInterfaceOrientation {
        currentMask == .portrait ? .portrait : .landscapeRight
    }

    static func register(controller: UIViewController) {
        activeController = controller
    }

    static func refresh() {
        update(mask: currentMask, target: preferredOrientation)
    }

    static func handle(notification: Notification) {
        apply(rawValue: notification.object as? String)
    }

    private static func apply(rawValue: String?) {
        switch rawValue {
        case "LANDSCAPE":
            lockLandscape()
        default:
            lockPortrait()
        }
    }

    static func lockPortrait() {
        update(mask: .portrait, target: .portrait)
    }

    static func lockLandscape() {
        update(mask: .landscape, target: .landscapeRight)
    }

    private static func update(mask: UIInterfaceOrientationMask, target: UIInterfaceOrientation) {
        currentMask = mask

        DispatchQueue.main.async {
            guard let controller = activeController else { return }

            if #available(iOS 16.0, *) {
                controller.setNeedsUpdateOfSupportedInterfaceOrientations()
                controller.view.window?.windowScene?.requestGeometryUpdate(
                    UIWindowScene.GeometryPreferences.iOS(interfaceOrientations: mask)
                ) { error in
                    NSLog("[OrientationLock] Failed to update orientation: %@", error.localizedDescription)
                }
            } else {
                UIDevice.current.setValue(target.rawValue, forKey: "orientation")
                UIViewController.attemptRotationToDeviceOrientation()
            }
        }
    }
}
