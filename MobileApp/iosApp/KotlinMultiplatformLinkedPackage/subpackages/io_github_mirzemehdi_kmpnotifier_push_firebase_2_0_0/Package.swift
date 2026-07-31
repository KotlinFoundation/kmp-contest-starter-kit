// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0",
      type: .none,
      targets: ["io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/firebase/firebase-ios-sdk.git",
      exact: "12.14.0"
    )
  ],
  targets: [
    .target(
      name: "io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0",
      dependencies: [
        .product(
          name: "FirebaseMessaging",
          package: "firebase-ios-sdk"
        )
      ]
    )
  ]
)
