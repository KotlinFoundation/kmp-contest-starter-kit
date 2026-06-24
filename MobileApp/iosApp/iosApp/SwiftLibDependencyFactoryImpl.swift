//
//  SwiftLibDependencyFactoryImpl.swift
//  iosApp
//
//  Created by Mirzamehdi on 01/03/2025.
//

import Foundation
import Shared
import SwiftUI
import UIKit

class SwiftLibDependencyFactoryImpl: SwiftLibDependencyFactory {

    static var shared = SwiftLibDependencyFactoryImpl()

    func provideFeatureFlagManagerImpl() -> FeatureFlagManager {
        return FeatureFlagManagerImpl()
    }

    func provideFirebaseAnalyticsImpl() -> any Analytics {
        return FirebaseAnalyticsImpl()
    }
    
    func provideAdsManagerImpl() -> AdsManager {
        return AdsManagerImpl()
    }
        
    func provideIosAdsDisplayer() -> IosAdsDisplayer {
        return IosAdsDisplayerImpl()
    }

}
