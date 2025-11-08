package com.aserdipanda.synapse.data.triggers.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_settings")
class AppSettingEntity {
    @PrimaryKey()
    var key: String? = null
    var value: String? = null
}