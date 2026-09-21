package com.bioscanner.`data`.local.db

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class BioScannerDatabase_Impl : BioScannerDatabase() {
  private val _speciesDao: Lazy<SpeciesDao> = lazy {
    SpeciesDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(1, "14099891103c02cbb2cb4fda861311d1", "25e529851dc1a95a793aa964b66e5c20") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `species` (`id` TEXT NOT NULL, `scientificName` TEXT NOT NULL, `popularName` TEXT NOT NULL, `family` TEXT NOT NULL, `category` TEXT NOT NULL, `venomous` INTEGER NOT NULL, `riskLevel` TEXT NOT NULL, `description` TEXT NOT NULL, `geographicDistribution` TEXT NOT NULL, `firstAidProcedure` TEXT NOT NULL, `referenceImagePath` TEXT, `classifierLabel` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, '14099891103c02cbb2cb4fda861311d1')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `species`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection): RoomOpenDelegate.ValidationResult {
        val _columnsSpecies: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsSpecies.put("id", TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("scientificName", TableInfo.Column("scientificName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("popularName", TableInfo.Column("popularName", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("family", TableInfo.Column("family", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("category", TableInfo.Column("category", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("venomous", TableInfo.Column("venomous", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("riskLevel", TableInfo.Column("riskLevel", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("description", TableInfo.Column("description", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("geographicDistribution", TableInfo.Column("geographicDistribution", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("firstAidProcedure", TableInfo.Column("firstAidProcedure", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("referenceImagePath", TableInfo.Column("referenceImagePath", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsSpecies.put("classifierLabel", TableInfo.Column("classifierLabel", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysSpecies: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesSpecies: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoSpecies: TableInfo = TableInfo("species", _columnsSpecies, _foreignKeysSpecies, _indicesSpecies)
        val _existingSpecies: TableInfo = read(connection, "species")
        if (!_infoSpecies.equals(_existingSpecies)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |species(com.bioscanner.data.local.db.SpeciesEntity).
              | Expected:
              |""".trimMargin() + _infoSpecies + """
              |
              | Found:
              |""".trimMargin() + _existingSpecies)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "species")
  }

  public override fun clearAllTables() {
    super.performClear(false, "species")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(SpeciesDao::class, SpeciesDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>): List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun speciesDao(): SpeciesDao = _speciesDao.value
}
