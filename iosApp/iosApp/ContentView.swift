import UIKit
import SwiftUI
import ComposeApp

import UIKit
import SwiftUI


struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        let composeVC = MainViewControllerKt.MainViewController()

        let rootVC = UIViewController()
        rootVC.view.backgroundColor = .clear // 투명하게 설정 (Compose 배경 활용)

        // ComposeView가 SafeArea 침범할 수 있도록 설정
        composeVC.view.translatesAutoresizingMaskIntoConstraints = false
        composeVC.view.backgroundColor = .clear

        rootVC.addChild(composeVC)
        rootVC.view.addSubview(composeVC.view)

        NSLayoutConstraint.activate([
            composeVC.view.topAnchor.constraint(equalTo: rootVC.view.topAnchor),
            composeVC.view.bottomAnchor.constraint(equalTo: rootVC.view.bottomAnchor),
            composeVC.view.leadingAnchor.constraint(equalTo: rootVC.view.leadingAnchor),
            composeVC.view.trailingAnchor.constraint(equalTo: rootVC.view.trailingAnchor)
        ])

        composeVC.didMove(toParent: rootVC)

        return rootVC
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ComposeView: View {
    var body: some View {
        ContentView()
                .ignoresSafeArea(.all) // Compose has own keyboard handler
    }
}
