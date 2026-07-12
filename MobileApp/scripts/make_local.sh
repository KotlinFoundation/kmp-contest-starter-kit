#!/usr/bin/env bash
#
# make_local.sh — scaffold a Room 3 entity + DAO end-to-end.
#
# Generates:
#   - domain model (skipped if it already exists)
#   - Room @Entity + extension-function mappers (toEntity/toModel)
#   - @Dao with the standard CRUD surface
# Wires:
#   - registers the entity in @Database(entities = [...])
#   - adds an abstract DAO accessor on AppDatabase
#   - registers the DAO singleton in databaseModule
#
# Idempotent — safe to re-run; insertion points are marked with
# "make_local.sh inserts here" comments in AppDatabase.kt and DatabaseModule.kt.

set -e

MODEL_NAME=$1

if [ -z "$MODEL_NAME" ]; then
  echo "Usage: ./scripts/make_local.sh ModelName"
  echo "Example: ./scripts/make_local.sh Note"
  exit 1
fi

BASE_PACKAGE="com.kotlinfoundation.koko"
BASE_PATH=$(echo "$BASE_PACKAGE" | tr '.' '/')

# snake_case for table name (e.g. CreditTransaction -> credit_transaction)
TABLE_NAME=$(echo "$MODEL_NAME" | sed 's/\(.\)\([A-Z]\)/\1_\2/g' | tr '[:upper:]' '[:lower:]')
ENTITY_NAME="${MODEL_NAME}Entity"
DAO_NAME="${MODEL_NAME}Dao"
# lowerCamelCase accessor (e.g. CreditTransaction -> creditTransaction)
LOWER_CAMEL=$(echo "$MODEL_NAME" | awk '{print tolower(substr($0,1,1)) substr($0,2)}')
DAO_ACCESSOR="${LOWER_CAMEL}Dao"

COMMON_MAIN="shared/src/commonMain/kotlin/$BASE_PATH"
LOCAL_DIR="$COMMON_MAIN/data/source/local"
DAO_DIR="$LOCAL_DIR/dao"
ENTITY_DIR="$LOCAL_DIR/entity"
DOMAIN_DIR="$COMMON_MAIN/domain/model"

mkdir -p "$DOMAIN_DIR" "$DAO_DIR" "$ENTITY_DIR"

DOMAIN_FILE="$DOMAIN_DIR/$MODEL_NAME.kt"
ENTITY_FILE="$ENTITY_DIR/$ENTITY_NAME.kt"
DAO_FILE="$DAO_DIR/$DAO_NAME.kt"
APP_DB_FILE="$LOCAL_DIR/AppDatabase.kt"
DB_MODULE_FILE="$LOCAL_DIR/DatabaseModule.kt"

################################
# Domain model (skip if exists)
################################
if [ -f "$DOMAIN_FILE" ]; then
  echo "Domain model '$MODEL_NAME' already exists — skipping."
else
  cat > "$DOMAIN_FILE" <<EOF
package $BASE_PACKAGE.domain.model

data class $MODEL_NAME(
    val id: String,
)
EOF
  echo "Created: $DOMAIN_FILE"
fi

################################
# Entity + extension mappers
################################
cat > "$ENTITY_FILE" <<EOF
@file:OptIn(ExperimentalUuidApi::class)

package $BASE_PACKAGE.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import $BASE_PACKAGE.domain.model.$MODEL_NAME
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Entity(tableName = "$TABLE_NAME")
data class $ENTITY_NAME(
    @PrimaryKey @ColumnInfo("id") val id: String = Uuid.random().toString(),
)

fun $ENTITY_NAME.toModel(): $MODEL_NAME = $MODEL_NAME(id = id)

fun $MODEL_NAME.toEntity(): $ENTITY_NAME = $ENTITY_NAME(id = id)
EOF
echo "Created: $ENTITY_FILE"

################################
# DAO
################################
cat > "$DAO_FILE" <<EOF
package $BASE_PACKAGE.data.source.local.dao

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Query
import androidx.room3.Upsert
import $BASE_PACKAGE.data.source.local.entity.$ENTITY_NAME
import kotlinx.coroutines.flow.Flow

@Dao
interface $DAO_NAME {
    @Query("SELECT * FROM $TABLE_NAME WHERE id = :id")
    suspend fun getById(id: String): $ENTITY_NAME?

    @Query("SELECT * FROM $TABLE_NAME WHERE id = :id")
    fun getByIdFlow(id: String): Flow<$ENTITY_NAME?>

    @Query("SELECT * FROM $TABLE_NAME")
    fun getAllFlow(): Flow<List<$ENTITY_NAME>>

    @Query("SELECT * FROM $TABLE_NAME")
    suspend fun getAll(): List<$ENTITY_NAME>

    @Upsert
    suspend fun upsert(entity: $ENTITY_NAME)

    @Query("DELETE FROM $TABLE_NAME WHERE id = :id")
    suspend fun deleteById(id: String)

    @Delete
    suspend fun delete(entity: $ENTITY_NAME)

    @Query("DELETE FROM $TABLE_NAME")
    suspend fun deleteAll()
}
EOF
echo "Created: $DAO_FILE"

################################
# Update AppDatabase.kt — register entity, import DAO, add accessor
################################
if [ ! -f "$APP_DB_FILE" ]; then
  echo "⚠️  $APP_DB_FILE not found — skipping AppDatabase wiring."
else
  if grep -q "$ENTITY_NAME::class" "$APP_DB_FILE"; then
    echo "AppDatabase already references $ENTITY_NAME — skipping."
  else
    # Insert entity into the @Database(entities = [...]) list.
    sed -i '' "s/entities = \[/entities = \[$ENTITY_NAME::class, /" "$APP_DB_FILE"

    # Insert entity + DAO imports inside the existing import block (before the
    # first `import` line) so they stay grouped and sortable by spotless.
    awk -v dao="import $BASE_PACKAGE.data.source.local.dao.$DAO_NAME" \
        -v ent="import $BASE_PACKAGE.data.source.local.entity.$ENTITY_NAME" '
        !inserted && /^import / { print dao; print ent; inserted=1 }
        { print }
    ' "$APP_DB_FILE" > "$APP_DB_FILE.tmp" && mv "$APP_DB_FILE.tmp" "$APP_DB_FILE"

    # Insert abstract DAO accessor right above the marker.
    sed -i '' "/\/\/ Add new DAOs above — make_local.sh inserts here./i\\
    abstract fun $DAO_ACCESSOR(): $DAO_NAME
" "$APP_DB_FILE"

    echo "Updated: $APP_DB_FILE"
  fi
fi

################################
# Update DatabaseModule.kt — register DAO singleton
################################
if [ ! -f "$DB_MODULE_FILE" ]; then
  echo "⚠️  $DB_MODULE_FILE not found — skipping DI wiring."
else
  if grep -q "\.$DAO_ACCESSOR()" "$DB_MODULE_FILE"; then
    echo "databaseModule already registers $DAO_ACCESSOR — skipping."
  else
    sed -i '' "/\/\/ Add new DAO accessors above — make_local.sh inserts here./i\\
    single { get<AppDatabase>().$DAO_ACCESSOR() }
" "$DB_MODULE_FILE"
    echo "Updated: $DB_MODULE_FILE"
  fi
fi

echo "✅ Local Room layer scaffolded for: $MODEL_NAME"
echo
echo "Next steps:"
echo "  1. Add real columns to $ENTITY_NAME and update mappers in:"
echo "     $ENTITY_FILE"
echo "  2. Bump the @Database version in AppDatabase.kt and add a Migration if you've already shipped."
echo "  3. Re-run ./gradlew :shared:kspKotlinJvm to regenerate Room sources."
