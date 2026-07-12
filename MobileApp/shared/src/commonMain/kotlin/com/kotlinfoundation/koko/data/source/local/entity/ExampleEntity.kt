@file:OptIn(ExperimentalUuidApi::class)

package com.kotlinfoundation.koko.data.source.local.entity

import androidx.room3.ColumnInfo
import androidx.room3.Entity
import androidx.room3.PrimaryKey
import com.kotlinfoundation.koko.domain.model.ExampleModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

// Scaffolding entity from make_local.sh showing the entity + mappers pattern — delete when unused.
@Entity(tableName = "example")
data class ExampleEntity(
    @PrimaryKey @ColumnInfo("id") val id: String = Uuid.random().toString(),
    @ColumnInfo("title") val title: String? = null,
)

fun ExampleEntity.toModel(): ExampleModel = ExampleModel(id = id, title = title)

fun ExampleModel.toEntity(): ExampleEntity = ExampleEntity(id = id, title = title)
