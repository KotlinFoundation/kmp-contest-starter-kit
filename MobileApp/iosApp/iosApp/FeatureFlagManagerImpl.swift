//
//  FeatureFlagManagerImpl.swift
//  iosApp
//
//  Created by Mirzamehdi on 01/03/2025.
//

import Foundation
import Shared
import FirebaseRemoteConfig

class FeatureFlagManagerImpl: FeatureFlagManager {

    private let remoteConfig = RemoteConfig.remoteConfig()

    init() {
        let settings = RemoteConfigSettings()

        #if targetEnvironment(simulator)
        settings.minimumFetchInterval = 3600
        #endif
        remoteConfig.configSettings = settings

        let defaultValues = FeatureFlagManagerCompanion.shared.DEFAULT_VALUES
        let convertedDefaults = convertToNSObjectDictionary(defaultValues)

        remoteConfig.setDefaults(convertedDefaults)


    }

    func syncsFlagsAsync() {
        remoteConfig.fetchAndActivate { status, error in
            if error?.localizedDescription.isEmpty == false {
                print("Feature Flag Sync Failed: \(error!.localizedDescription)")
            } else {
                print("Feature Flag Sync is completed, result: \(status)")
            }
        }
    }

    func getBoolean(key: String) -> Bool {
        return remoteConfig[key].boolValue
    }

    func getString(key: String) -> String {
        return remoteConfig[key].stringValue
    }

    func getLong(key: String) -> Int64 {
        return remoteConfig[key].numberValue.int64Value
    }

    func getDouble(key: String) -> Double {
        return remoteConfig[key].numberValue.doubleValue
    }

    private func convertToNSObjectDictionary(_ input: [String: Any]) -> [String: NSObject] {
        var result = [String: NSObject]()

        for (key, value) in input {
           if let nsValue = value as? NSObject {
               result[key] = nsValue
           } else if let boolValue = value as? Bool {
               result[key] = NSNumber(value: boolValue)
           } else if let stringValue = value as? String {
               result[key] = NSString(string: stringValue)
           } else if let intValue = value as? Int {
               result[key] = NSNumber(value: intValue)
           }
        }

         return result
    }

}
