//
//  RewardedAdLoader.swift
//  iosApp
//
//  Created by Mirzamehdi on 07/03/2025.
//

import Foundation
import Shared
import SwiftUI
import GoogleMobileAds

class RewardedAdLoader: NSObject, FullScreenAdLoader  {
    
    var rewardedAd: RewardedAd?

    func load() {
        let request = Request()
        RewardedAd.load(
            with: AdsConfig.shared.getRewardedAdId(),
            request: request,
            completionHandler: { [weak self] ad, error in
                if let error = error {
                    print("Error loading rewarding ad: \(error.localizedDescription)")
                    self?.rewardedAd = nil
                } else {
                    print("Rewarded ad is loaded")
                    self?.rewardedAd = ad
                }
            }
        )
    }
}
