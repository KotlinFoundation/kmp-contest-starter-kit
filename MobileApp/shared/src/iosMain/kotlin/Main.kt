import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.window.ComposeUIViewController
import com.kotlinfoundation.koko.root.App
import com.kotlinfoundation.koko.util.LocalNativeViewFactory
import com.kotlinfoundation.koko.util.NativeViewFactory
import com.kotlinfoundation.koko.util.SwiftLibDependencyFactory
import com.kotlinfoundation.koko.util.swiftLibDependenciesModule
import org.koin.core.KoinApplication
import platform.UIKit.UIViewController

fun MainViewController(nativeViewFactory: NativeViewFactory): UIViewController = ComposeUIViewController {
    CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
        App()
    }
}

// This is called on application started on Swift side
fun KoinApplication.provideSwiftLibDependencyFactory(factory: SwiftLibDependencyFactory) = run { modules(swiftLibDependenciesModule(factory)) }
