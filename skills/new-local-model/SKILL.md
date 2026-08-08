---
name: new-local-model
description: Scaffold a locally-persisted model end-to-end - domain model, Room 3 entity with mappers, DAO, database registration, and Koin singleton. Use whenever the user asks to store a new model/table locally - never hand-create these files.
---

# Add a locally-stored model (Room 3)

Run from `MobileApp/`:

```bash
./scripts/make_local.sh ModelName
```

The script is idempotent (safe to re-run). It scaffolds in `shared/src/commonMain`:

- Domain model in `domain/model/` (skipped if it already exists)
- `@Entity` in `data/source/local/entity/` with `toModel()` / `toEntity()` extension-function mappers
- `@Dao` in `data/source/local/dao/` with the standard CRUD surface (suspend + Flow)
- Registers the entity in `@Database(entities = [...])` and adds the abstract DAO accessor on `AppDatabase`
- Registers a Koin singleton in `DatabaseModule.kt`

Insertion points are marked `// Add new ... — make_local.sh inserts here.` — never remove or move those markers.

## After running

1. Edit the generated entity to add real columns, then update both mappers.
2. If the schema has already shipped, bump `@Database(version = ...)` and add a `Migration`.
3. Rules: use `androidx.room3.*` imports (never `androidx.room.*`); DAO functions must be `suspend` or return `Flow<T>`; repositories inject DAOs directly (no LocalDataSource abstraction).
4. DAO tests go in `shared/src/jvmTest/` using `Room.inMemoryDatabaseBuilder<AppDatabase>().setDriver(BundledSQLiteDriver())` — see `ExampleDaoTest.kt` for the pattern.
5. Validate with the `run-quality-gates` skill.

**Want this model in the cloud too?** Use the `sync-data-firebase` skill rather than reaching for a
Firestore SDK — no Firebase client SDK supports the `wasmJs` target, so that choice trades away the web
build and the developer has to make it.

---

*Phase 1 · First Run building block — part of the [getting-started](../getting-started/SKILL.md) guide.*
