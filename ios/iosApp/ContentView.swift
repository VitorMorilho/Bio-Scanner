import SwiftUI
import UIKit
import Shared

/// Toda a interface é Compose Multiplatform (shared/); aqui só a hospedamos no SwiftUI.
struct ComposeView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}

struct ContentView: View {
    var body: some View {
        // O Compose desenha atrás das barras do sistema e aplica os insets ele mesmo.
        ComposeView().ignoresSafeArea()
    }
}
