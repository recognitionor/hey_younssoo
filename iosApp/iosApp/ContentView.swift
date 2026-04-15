import UIKit
import SwiftUI
import ComposeApp

final class AppOrientationContainerViewController: UIViewController {
    private let composeVC = MainViewControllerKt.MainViewController()

    override func viewDidLoad() {
        super.viewDidLoad()

        view.backgroundColor = .clear
        composeVC.view.translatesAutoresizingMaskIntoConstraints = false
        composeVC.view.backgroundColor = .clear

        addChild(composeVC)
        view.addSubview(composeVC.view)

        NSLayoutConstraint.activate([
            composeVC.view.topAnchor.constraint(equalTo: view.topAnchor),
            composeVC.view.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            composeVC.view.leadingAnchor.constraint(equalTo: view.leadingAnchor),
            composeVC.view.trailingAnchor.constraint(equalTo: view.trailingAnchor)
        ])

        composeVC.didMove(toParent: self)

        NotificationCenter.default.addObserver(
            self,
            selector: #selector(handleOrientationNotification(_:)),
            name: OrientationLock.notificationName,
            object: nil
        )
    }

    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
        OrientationLock.register(controller: self)
        OrientationLock.refresh()
    }

    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    override var supportedInterfaceOrientations: UIInterfaceOrientationMask {
        OrientationLock.currentMask
    }

    override var preferredInterfaceOrientationForPresentation: UIInterfaceOrientation {
        OrientationLock.preferredOrientation
    }

    override var shouldAutorotate: Bool {
        true
    }

    @objc private func handleOrientationNotification(_ notification: Notification) {
        OrientationLock.handle(notification: notification)
    }
}

struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        AppOrientationContainerViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ComposeView: View {
    var body: some View {
        ContentView()
                .ignoresSafeArea(.all) // Compose has own keyboard handler
    }
}
