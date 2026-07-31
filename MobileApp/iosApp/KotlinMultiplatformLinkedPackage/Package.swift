// swift-tools-version: 5.9
import PackageDescription
let package = Package(
  name: "KotlinMultiplatformLinkedPackage",
  platforms: [
    .iOS("16.0")
  ],
  products: [
    .library(
      name: "KotlinMultiplatformLinkedPackage",
      type: .none,
      targets: ["KotlinMultiplatformLinkedPackage"]
    )
  ],
  dependencies: [
    .package(path: "subpackages/io_github_mirzemehdi_kmpauth_firebase_3_0_3"),
    .package(path: "subpackages/io_github_mirzemehdi_kmpauth_google_3_0_3"),
    .package(path: "subpackages/io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0")
  ],
  targets: [
    .target(
      name: "KotlinMultiplatformLinkedPackage",
      dependencies: [
        .product(name: "io_github_mirzemehdi_kmpauth_firebase_3_0_3", package: "io_github_mirzemehdi_kmpauth_firebase_3_0_3"),
        .product(name: "io_github_mirzemehdi_kmpauth_google_3_0_3", package: "io_github_mirzemehdi_kmpauth_google_3_0_3"),
        .product(name: "io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0", package: "io_github_mirzemehdi_kmpnotifier_push_firebase_2_0_0")
      ]
    )
  ]
)
