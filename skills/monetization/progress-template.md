# Monetization progress

## 1. Design the offer
- [ ] (Agent) Run `design-paywall` — hand over designer prompt + paywall template
- [ ] (User) Fill `TAILOR PER APP` blanks in `AiGuidelines/project/paywall.md`
- [ ] (User) No `TAILOR PER APP` markers remain

## 2. Set up subscriptions
- [ ] (Agent) Run `setup-subscriptions` — pick provider (`SUBSCRIPTION_PROVIDER`), confirm entitlement key
- [ ] (User) Provider Android + iOS keys in `MobileApp/local.properties`
- [ ] (User) Create subscription products in App Store Connect + Google Play Console (aligned IDs)
- [ ] (User) Link products + map to `Premium` entitlement + configure placement in provider dashboard
- [ ] (Agent) `:androidApp:assembleDebug` builds; subscription paywall shows real products

## 3. Enable credit packs
- [ ] (Agent) Run `enable-credits` — configure credit DSL, wire pack purchase → credit grant
- [ ] (User) Create consumable credit-pack IAPs in ASC + Play, register under credit-pack placement
- [ ] (Agent) Credit-pack paywall shows packs; purchase calls `addCredits(...)`

## 4. Enable ads
- [ ] (Agent) Run `enable-ads` — flip `IS_ADS_ENABLED`, place banner/interstitial/rewarded ads
- [ ] (User) Create AdMob app + ad units, paste IDs into `local.properties` (+ iOS `BaseConfig.xcconfig`)
- [ ] (User) Update store data-safety / privacy for advertising IDs

## Exit gate
- [ ] Sandbox/test purchase completes and unlocks premium (paywall dismisses / credits added)
