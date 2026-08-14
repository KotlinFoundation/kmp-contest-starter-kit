// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "io_github_mirzemehdi_kmpauth_google_3_0_5",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "io_github_mirzemehdi_kmpauth_google_3_0_5",
      type: .none,
      targets: ["io_github_mirzemehdi_kmpauth_google_3_0_5"]
    )
  ],
  dependencies: [
    .package(
      url: "https://github.com/google/GoogleSignIn-iOS.git",
      from: "9.1.0"
    )
  ],
  targets: [
    .target(
      name: "io_github_mirzemehdi_kmpauth_google_3_0_5",
      dependencies: [
        .product(
          name: "GoogleSignIn",
          package: "GoogleSignIn-iOS"
        )
      ]
    )
  ]
)
