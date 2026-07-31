//
//  FirebaseAnalyticsImpl.swift
//  iosApp
//
//  Created by Mirzamehdi on 06/03/2025.
//

import Foundation
import Shared
import FirebaseAnalytics
import FirebaseCore

class FirebaseAnalyticsImpl: Shared.Analytics {
    
    func logEvent(event: String, params: [String : Any]?) {
        var eventParams: [String: Any] = [:]
        params?.forEach { key, value in eventParams[key] = "\(value)" }
        Analytics.logEvent(event, parameters: eventParams)
    }
    
    func setEnabled(enabled: Bool) {
        Analytics.setAnalyticsCollectionEnabled(enabled)
    }
    
}
