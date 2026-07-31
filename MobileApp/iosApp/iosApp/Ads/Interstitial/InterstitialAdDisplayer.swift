//
//  InterstitialAdDisplayer.swift
//  iosApp
//
//  Created by Mirzamehdi on 07/03/2025.
//

import Foundation
import Shared
import SwiftUI
import GoogleMobileAds


class InterstitialAdDisplayer: NSObject, FullScreenAdDisplayer {
    private let adLoader: FullScreenAdLoader
    
    init(adLoader: FullScreenAdLoader) {
        self.adLoader = adLoader
    }
    
    func show() {
        
        guard let interstitialAdLoader = adLoader as? InterstitialAdLoader else {
            print("Ad loader is not of type InterstitialAdLoader")
            return
        }
        
        guard let interstitialAd = interstitialAdLoader.interstitialAd else {
            print("Interstitial ad is not loaded yet")
            interstitialAdLoader.load()
            return
        }
        
        guard let rootViewController =
            UIApplication.shared.windows.first?.rootViewController else {
            print("No root view controller found")
            return
        }
        interstitialAd.fullScreenContentDelegate = self
        interstitialAd.present(from: rootViewController)
        
    }
    
}

extension InterstitialAdDisplayer: FullScreenContentDelegate {
    
    func adDidDismissFullScreenContent(_ ad: any FullScreenPresentingAd) {
        print("Interstitial ad is dismissed, loading a new one")
        guard let interstitialAdLoader = adLoader as? InterstitialAdLoader else { return }
        interstitialAdLoader.interstitialAd = nil
        interstitialAdLoader.load()
    }
    
    func ad(_ ad: any FullScreenPresentingAd, didFailToPresentFullScreenContentWithError error: any Error) {
        print("Interstitial ad failed to show")
        guard let interstitialAdLoader = adLoader as? InterstitialAdLoader else { return }
        interstitialAdLoader.interstitialAd = nil
    }
    
    func adWillPresentFullScreenContent(_ ad: any FullScreenPresentingAd) {
        print("Interstitial ad is shown")
    }
    
}

