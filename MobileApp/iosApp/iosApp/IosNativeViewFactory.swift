//
//  IosNativeViewFactory.swift
//  iosApp
//
//  Created by Mirzamehdi on 22/01/2025.
//

import Foundation
import Shared
import SwiftUI
import UIKit

class IosNativeViewFactory: NativeViewFactory{
    static var shared = IosNativeViewFactory()
    
    func createSwiftTextView(text: String) -> UIViewController {
        let swiftUIView = Text(text)
        return UIHostingController(rootView: swiftUIView)
    }
    
    func createAdmobBannerView(
            bannerId: String,
            onAdLoaded: @escaping () -> Void,
            onAdFailedToLoad: @escaping () -> Void
    ) -> UIViewController {
        let adMobBannerView = BannerAdView(
            bannerAdUnitId: bannerId,
            onAdLoaded: onAdLoaded,
            onAdFailedToLoad: onAdFailedToLoad
        )
        return UIHostingController(rootView: adMobBannerView)
    }
    
}
