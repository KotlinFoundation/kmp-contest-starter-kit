# MobileApp — agent context pointer

You are in `MobileApp/`, one level **below** the repository root. If your session was opened on this
folder (the "MobileApp window" of the two-window workflow), the project's agent context does not
auto-load — read it now:

- **Primary context:** [`../AGENTS.md`](../AGENTS.md) — architecture, build/test commands,
  conventions, validation guardrails. Read it before making changes.
- **Skills index:** [`../skills/README.md`](../skills/README.md) — every task and guide skill
  (`../skills/<name>/SKILL.md`). Read the matching skill before doing a task it covers.
- **Progress files:** `../PROGRESS_*.md` (at the git root, **not** in `MobileApp/`) — read before
  resuming work; continue from the first unchecked item.
- **Work in parallel by default:** independent pieces (screens, models, docs) get one subagent
  each, fanned out concurrently — see "Agent Working Style" in [`../AGENTS.md`](../AGENTS.md) for
  the file-ownership and single-Gradle rules that make it safe.

All Gradle commands run from this directory (`MobileApp/`).
