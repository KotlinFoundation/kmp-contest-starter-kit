//
//  IosAdsDisplayerImpl.swift
//  iosApp
//
//  Created by Mirzamehdi on 07/03/2025.
//

import Foundation
import Shared
import SwiftUI
import GoogleMobileAds


class IosAdsDisplayerImpl: IosAdsDisplayer {
    
    func provideInterstitialAdDisplayer(adLoader: FullScreenAdLoader) -> FullScreenAdDisplayer {
        return InterstitialAdDisplayer(adLoader: adLoader)
    }

    func provideRewardedAdDisplayer(adLoader: FullScreenAdLoader, onRewarded: @escaping (AdsRewardItem) -> Void) -> any FullScreenAdDisplayer {
        return RewardedAdDisplayer(adLoader: adLoader, onRewarded: onRewarded)
    }
}
