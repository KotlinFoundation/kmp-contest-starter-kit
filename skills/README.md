# Agent Skills

Vendor-neutral skills for AI coding agents (Claude Code, Codex, Gemini CLI, Cursor, ...) working in this repo.
Each skill is a `<name>/SKILL.md` file in the open [Agent Skills](https://agentskills.io) format:
YAML frontmatter (`name`, `description`) followed by step-by-step instructions.

- Agents with native skill support discover these automatically (`.claude/skills` symlinks here for Claude Code).
- Agents without native support: read the skill file referenced from the **Skills** section of [AGENTS.md](../AGENTS.md) before doing the matching task.

| Skill | Use when |
|---|---|
| [refactor-package](refactor-package/SKILL.md) | Renaming the app package / applicationId / bundle ID / display name (rebrand) |
| [new-screen](new-screen/SKILL.md) | Adding a new screen (scaffolds UI + route + DI wiring) |
| [new-local-model](new-local-model/SKILL.md) | Adding a locally-persisted model (Room entity + DAO + DI) |
| [new-module](new-module/SKILL.md) | Adding a new Gradle KMP library module |
| [store-screenshots](store-screenshots/SKILL.md) | Generating App Store / Play Store screenshots |
| [bump-version](bump-version/SKILL.md) | Bumping app versionCode/versionName for a release |
| [run-quality-gates](run-quality-gates/SKILL.md) | Validating changes before commit/PR |
