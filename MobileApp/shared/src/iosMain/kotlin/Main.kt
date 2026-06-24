import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.kotlinfoundation.kmpstarterkit.root.App
import com.kotlinfoundation.kmpstarterkit.util.LocalNativeViewFactory
import com.kotlinfoundation.kmpstarterkit.util.NativeViewFactory
import com.kotlinfoundation.kmpstarterkit.util.SwiftLibDependencyFactory
import com.kotlinfoundation.kmpstarterkit.util.swiftLibDependenciesModule
import org.koin.core.KoinApplication
import platform.UIKit.UIViewController

fun MainViewController(nativeViewFactory: NativeViewFactory): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
        App()
    }
}

// This is called on application started on Swift side
fun KoinApplication.provideSwiftLibDependencyFactory(factory: SwiftLibDependencyFactory) = run { modules(swiftLibDependenciesModule(factory)) }
