---
name: new-module
description: Create a new Gradle KMP library module with the project's convention plugin and source-set layout. Use when code needs to live outside shared/ (a reusable library, a new feature module, an alternative provider implementation).
---

# Add a new Gradle module

Run from `MobileApp/`:

```bash
./scripts/create_module.sh ModuleName [targetDir] [namespace]
```

- `ModuleName` — module name (hyphens become dots in the namespace)
- `targetDir` — optional parent directory (e.g. `libs/payment`); defaults to the repo layout the script prints
- `namespace` — optional Android namespace; defaults to `com.kotlinfoundation.koko.<modulename>`

The script creates the module directories, a `build.gradle.kts` using the convention plugins from `build-logic/`, and registers the module in `settings.gradle.kts`.

## After running

1. Follow the existing module layout for API/impl splits — see `libs/subscription/` (`subscription-api` + provider implementations) for the pattern.
2. Add the module to consumers' dependencies (`implementation(projects.libs....)`).
3. Validate with the `run-quality-gates` skill.

---

*Phase 1 · First Run building block — part of the [getting-started](../getting-started/SKILL.md) guide.*
