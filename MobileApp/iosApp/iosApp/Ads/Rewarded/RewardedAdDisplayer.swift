//
//  RewardedAdDisplayer.swift
//  iosApp
//
//  Created by Mirzamehdi on 07/03/2025.
//

import Foundation
import Shared
import SwiftUI
import GoogleMobileAds


class RewardedAdDisplayer: NSObject, FullScreenAdDisplayer {
    
    private let adLoader: FullScreenAdLoader
    private let onRewarded: (AdsRewardItem) -> Void
    
    init(adLoader: FullScreenAdLoader, onRewarded: @escaping (AdsRewardItem) -> Void) {
        self.adLoader = adLoader
        self.onRewarded = onRewarded
    }
    
    func show() {
        
        guard let rewardedAdLoader = adLoader as? RewardedAdLoader else {
            print("Ad loader is not of type RewardedAdLoader")
            return
        }
        
        guard let rewardedAd = rewardedAdLoader.rewardedAd else {
            print("Rewarded ad is not loaded yet")
            rewardedAdLoader.load()
            return
        }
        
        rewardedAd.fullScreenContentDelegate = self
        rewardedAd.present(from: nil) {
            let reward = rewardedAd.adReward
            let rewardItem = AdsRewardItem(
                amount: reward.amount.int32Value,
                type: reward.type
            )
            self.onRewarded(rewardItem)
            print("Reward received with type \(reward.type), amount \(reward.amount)")
          }
        
    }
    
}

extension RewardedAdDisplayer: FullScreenContentDelegate {
    
    func adDidDismissFullScreenContent(_ ad: any FullScreenPresentingAd) {
        print("Rewarded ad is dismissed, loading a new one")
        guard let rewardedAdLoader = adLoader as? RewardedAdLoader else { return }
        rewardedAdLoader.rewardedAd = nil
        rewardedAdLoader.load()
    }
    
    func ad(_ ad: any FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: any Error) {
        print("Rewarded ad failed to show")
        guard let rewardedAdLoader = adLoader as? RewardedAdLoader else { return }
        rewardedAdLoader.rewardedAd = nil
    }
    
    func adWillPresentFullScreenContent(_ ad: any FullScreenPresentingAd) {
        print("Rewarded ad is shown")
    }
    
}
