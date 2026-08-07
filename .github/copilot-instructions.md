# Copilot instructions

This project ships vendor-neutral **Agent Skills**. Before implementing a task,
check whether a skill already covers it.

- Full index — a 5-phase developer journey (guides) plus one-job task skills:
  [`skills/README.md`](../skills/README.md).
- Read the matching `skills/<name>/SKILL.md` and follow it. Steps are exact
  (real commands, file paths, console URLs) and are written to be followed
  manually or by an agent.
- Broader project conventions and the same skills index live in
  [`AGENTS.md`](../AGENTS.md).

**Firebase data in shared code** (Firestore, sync, cloud storage, server-side
balances): there is no Firebase client SDK for the `wasmJs` target, so this
decision trades away the web build. **Stop and ask the developer which they
want** before writing code or recommending an approach — see
[`skills/setup-firebase/SKILL.md`](../skills/setup-firebase/SKILL.md).
