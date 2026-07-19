# Contributing to Koko (KMP Contest Starter Kit)

Thank you for your interest in contributing to Koko! We welcome contributions from the community to help make this Kotlin Multiplatform + Compose Multiplatform template better.

## How to Contribute

Currently, since this is a private repository, you will contribute by creating a branch directly within this repo. Once this repository becomes public, we will shift to a fork-and-pull-request model.

### Contribution Workflow (Internal/Branching)

1. **Create a Branch**: Create a new branch off of `initial_version` (or the default branch) for your feature or fix. Use a descriptive name, categorized by the type of work (e.g., `feature/`, `bugfix/`, `docs/`).
   ```bash
   git checkout initial_version
   git pull origin initial_version
   git checkout -b feature/my-new-feature
   ```

2. **Make Changes**: Implement your changes. Ensure your code follows the project's style and architectural conventions.
   - For new screens, use the provided script: `./scripts/generate_screen.sh`
   - For new local database models, use: `./scripts/make_local.sh`
   - For architecture reference, consult the `AiGuidelines/tech/` directory.

3. **Test Your Changes**: Verify your work locally.
   - **Quality Gates**: Run the formatting and unit tests to ensure you haven't broken anything:
     ```bash
     ./gradlew spotlessApply
     ./gradlew :shared:jvmTest :shared:testAndroidHostTest
     ```
   - **Build**: Ensure the Android app compiles successfully:
     ```bash
     ./gradlew :androidApp:assembleDebug
     ```
   - **Run**: Test the app on your local emulator or physical device.

4. **Commit**: Commit your changes with clear, descriptive messages.
   ```bash
   git add .
   git commit -m "feat(ui): add new setting toggle for animations"
   ```

5. **Push and Open a Pull Request**: Push your branch to the remote repository and open a Pull Request (PR) against the default branch (`initial_version`).
   ```bash
   git push origin feature/my-new-feature
   ```
   Provide a clear description of your changes in the PR body, including what problem it solves or what feature it adds.

---

## Pull Request Guidelines

### What We Accept
- ✅ Improvements to the core template architecture or shared components.
- ✅ Enhancements to the `designsystem` module.
- ✅ New agent skills (in the `skills/` directory) or updates to existing skills.
- ✅ Documentation improvements (README, `AiGuidelines/`, or Docusaurus site).
- ✅ Bug fixes across any target (Android, iOS, Web, Desktop).

### What We Reject
- ❌ PRs that introduce major architectural shifts without prior discussion.
- ❌ PRs that add bloated, non-essential dependencies to the starter kit.
- ❌ Code that fails the `spotlessCheck` or unit tests.
- ❌ PRs that break the AI-agent compatibility (e.g., drastically changing the structure without updating `AGENTS.md` and related scripts).

---

## Code Style & Conventions

- **Idiomatic Kotlin**: Follow Kotlin and Compose Multiplatform best practices.
- **Formatting**: We use Spotless with ktlint. You **must** run `./gradlew spotlessApply` before committing.
- **UI Components**: Prefer using existing components from the `designsystem` module. If you need a new primitive, add it to `designsystem` rather than hardcoding it in a feature module.
- **Architecture**:
  - Keep domain models pure (no `@Serializable` or persistence annotations).
  - Use `backgroundExecutor.execute {}` in repositories instead of bare `try/catch` blocks.
  - Follow the dual-overload pattern for Compose screens (one with a ViewModel, one pure composable for previews/testing).

---

## Reporting Issues

If you find a bug or have a feature request, please open an issue.
* **Bugs**: Describe the issue in detail, including steps to reproduce, expected behavior, and screenshots (if it's a UI issue).
* **Features**: Explain the proposed feature and why it belongs in a generalized starter kit.

**Questions?** We're here to help! Feel free to reach out to the core maintainers.