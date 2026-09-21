package com.bioscanner.`data`.local.db

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class SpeciesDao_Impl(
  __db: RoomDatabase,
) : SpeciesDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfSpeciesEntity: EntityInsertAdapter<SpeciesEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfSpeciesEntity = object : EntityInsertAdapter<SpeciesEntity>() {
      protected override fun createQuery(): String = "INSERT OR REPLACE INTO `species` (`id`,`scientificName`,`popularName`,`family`,`category`,`venomous`,`riskLevel`,`description`,`geographicDistribution`,`firstAidProcedure`,`referenceImagePath`,`classifierLabel`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: SpeciesEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.scientificName)
        statement.bindText(3, entity.popularName)
        statement.bindText(4, entity.family)
        statement.bindText(5, entity.category)
        val _tmp: Int = if (entity.venomous) 1 else 0
        statement.bindLong(6, _tmp.toLong())
        statement.bindText(7, entity.riskLevel)
        statement.bindText(8, entity.description)
        statement.bindText(9, entity.geographicDistribution)
        statement.bindText(10, entity.firstAidProcedure)
        val _tmpReferenceImagePath: String? = entity.referenceImagePath
        if (_tmpReferenceImagePath == null) {
          statement.bindNull(11)
        } else {
          statement.bindText(11, _tmpReferenceImagePath)
        }
        val _tmpClassifierLabel: String? = entity.classifierLabel
        if (_tmpClassifierLabel == null) {
          statement.bindNull(12)
        } else {
          statement.bindText(12, _tmpClassifierLabel)
        }
      }
    }
  }

  public override suspend fun insertAll(species: List<SpeciesEntity>): Unit = performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfSpeciesEntity.insert(_connection, species)
  }

  public override fun getAllSpecies(): Flow<List<SpeciesEntity>> {
    val _sql: String = "SELECT * FROM species ORDER BY popularName ASC"
    return createFlow(__db, false, arrayOf("species")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfScientificName: Int = getColumnIndexOrThrow(_stmt, "scientificName")
        val _columnIndexOfPopularName: Int = getColumnIndexOrThrow(_stmt, "popularName")
        val _columnIndexOfFamily: Int = getColumnIndexOrThrow(_stmt, "family")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVenomous: Int = getColumnIndexOrThrow(_stmt, "venomous")
        val _columnIndexOfRiskLevel: Int = getColumnIndexOrThrow(_stmt, "riskLevel")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfGeographicDistribution: Int = getColumnIndexOrThrow(_stmt, "geographicDistribution")
        val _columnIndexOfFirstAidProcedure: Int = getColumnIndexOrThrow(_stmt, "firstAidProcedure")
        val _columnIndexOfReferenceImagePath: Int = getColumnIndexOrThrow(_stmt, "referenceImagePath")
        val _columnIndexOfClassifierLabel: Int = getColumnIndexOrThrow(_stmt, "classifierLabel")
        val _result: MutableList<SpeciesEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SpeciesEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpScientificName: String
          _tmpScientificName = _stmt.getText(_columnIndexOfScientificName)
          val _tmpPopularName: String
          _tmpPopularName = _stmt.getText(_columnIndexOfPopularName)
          val _tmpFamily: String
          _tmpFamily = _stmt.getText(_columnIndexOfFamily)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpVenomous: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfVenomous).toInt()
          _tmpVenomous = _tmp != 0
          val _tmpRiskLevel: String
          _tmpRiskLevel = _stmt.getText(_columnIndexOfRiskLevel)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpGeographicDistribution: String
          _tmpGeographicDistribution = _stmt.getText(_columnIndexOfGeographicDistribution)
          val _tmpFirstAidProcedure: String
          _tmpFirstAidProcedure = _stmt.getText(_columnIndexOfFirstAidProcedure)
          val _tmpReferenceImagePath: String?
          if (_stmt.isNull(_columnIndexOfReferenceImagePath)) {
            _tmpReferenceImagePath = null
          } else {
            _tmpReferenceImagePath = _stmt.getText(_columnIndexOfReferenceImagePath)
          }
          val _tmpClassifierLabel: String?
          if (_stmt.isNull(_columnIndexOfClassifierLabel)) {
            _tmpClassifierLabel = null
          } else {
            _tmpClassifierLabel = _stmt.getText(_columnIndexOfClassifierLabel)
          }
          _item = SpeciesEntity(_tmpId,_tmpScientificName,_tmpPopularName,_tmpFamily,_tmpCategory,_tmpVenomous,_tmpRiskLevel,_tmpDescription,_tmpGeographicDistribution,_tmpFirstAidProcedure,_tmpReferenceImagePath,_tmpClassifierLabel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSpeciesById(id: String): Flow<SpeciesEntity?> {
    val _sql: String = "SELECT * FROM species WHERE id = ?"
    return createFlow(__db, false, arrayOf("species")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfScientificName: Int = getColumnIndexOrThrow(_stmt, "scientificName")
        val _columnIndexOfPopularName: Int = getColumnIndexOrThrow(_stmt, "popularName")
        val _columnIndexOfFamily: Int = getColumnIndexOrThrow(_stmt, "family")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVenomous: Int = getColumnIndexOrThrow(_stmt, "venomous")
        val _columnIndexOfRiskLevel: Int = getColumnIndexOrThrow(_stmt, "riskLevel")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfGeographicDistribution: Int = getColumnIndexOrThrow(_stmt, "geographicDistribution")
        val _columnIndexOfFirstAidProcedure: Int = getColumnIndexOrThrow(_stmt, "firstAidProcedure")
        val _columnIndexOfReferenceImagePath: Int = getColumnIndexOrThrow(_stmt, "referenceImagePath")
        val _columnIndexOfClassifierLabel: Int = getColumnIndexOrThrow(_stmt, "classifierLabel")
        val _result: SpeciesEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpScientificName: String
          _tmpScientificName = _stmt.getText(_columnIndexOfScientificName)
          val _tmpPopularName: String
          _tmpPopularName = _stmt.getText(_columnIndexOfPopularName)
          val _tmpFamily: String
          _tmpFamily = _stmt.getText(_columnIndexOfFamily)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpVenomous: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfVenomous).toInt()
          _tmpVenomous = _tmp != 0
          val _tmpRiskLevel: String
          _tmpRiskLevel = _stmt.getText(_columnIndexOfRiskLevel)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpGeographicDistribution: String
          _tmpGeographicDistribution = _stmt.getText(_columnIndexOfGeographicDistribution)
          val _tmpFirstAidProcedure: String
          _tmpFirstAidProcedure = _stmt.getText(_columnIndexOfFirstAidProcedure)
          val _tmpReferenceImagePath: String?
          if (_stmt.isNull(_columnIndexOfReferenceImagePath)) {
            _tmpReferenceImagePath = null
          } else {
            _tmpReferenceImagePath = _stmt.getText(_columnIndexOfReferenceImagePath)
          }
          val _tmpClassifierLabel: String?
          if (_stmt.isNull(_columnIndexOfClassifierLabel)) {
            _tmpClassifierLabel = null
          } else {
            _tmpClassifierLabel = _stmt.getText(_columnIndexOfClassifierLabel)
          }
          _result = SpeciesEntity(_tmpId,_tmpScientificName,_tmpPopularName,_tmpFamily,_tmpCategory,_tmpVenomous,_tmpRiskLevel,_tmpDescription,_tmpGeographicDistribution,_tmpFirstAidProcedure,_tmpReferenceImagePath,_tmpClassifierLabel)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun getSpeciesByCategory(category: String): Flow<List<SpeciesEntity>> {
    val _sql: String = "SELECT * FROM species WHERE category = ? ORDER BY popularName ASC"
    return createFlow(__db, false, arrayOf("species")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, category)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfScientificName: Int = getColumnIndexOrThrow(_stmt, "scientificName")
        val _columnIndexOfPopularName: Int = getColumnIndexOrThrow(_stmt, "popularName")
        val _columnIndexOfFamily: Int = getColumnIndexOrThrow(_stmt, "family")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVenomous: Int = getColumnIndexOrThrow(_stmt, "venomous")
        val _columnIndexOfRiskLevel: Int = getColumnIndexOrThrow(_stmt, "riskLevel")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfGeographicDistribution: Int = getColumnIndexOrThrow(_stmt, "geographicDistribution")
        val _columnIndexOfFirstAidProcedure: Int = getColumnIndexOrThrow(_stmt, "firstAidProcedure")
        val _columnIndexOfReferenceImagePath: Int = getColumnIndexOrThrow(_stmt, "referenceImagePath")
        val _columnIndexOfClassifierLabel: Int = getColumnIndexOrThrow(_stmt, "classifierLabel")
        val _result: MutableList<SpeciesEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: SpeciesEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpScientificName: String
          _tmpScientificName = _stmt.getText(_columnIndexOfScientificName)
          val _tmpPopularName: String
          _tmpPopularName = _stmt.getText(_columnIndexOfPopularName)
          val _tmpFamily: String
          _tmpFamily = _stmt.getText(_columnIndexOfFamily)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpVenomous: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfVenomous).toInt()
          _tmpVenomous = _tmp != 0
          val _tmpRiskLevel: String
          _tmpRiskLevel = _stmt.getText(_columnIndexOfRiskLevel)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpGeographicDistribution: String
          _tmpGeographicDistribution = _stmt.getText(_columnIndexOfGeographicDistribution)
          val _tmpFirstAidProcedure: String
          _tmpFirstAidProcedure = _stmt.getText(_columnIndexOfFirstAidProcedure)
          val _tmpReferenceImagePath: String?
          if (_stmt.isNull(_columnIndexOfReferenceImagePath)) {
            _tmpReferenceImagePath = null
          } else {
            _tmpReferenceImagePath = _stmt.getText(_columnIndexOfReferenceImagePath)
          }
          val _tmpClassifierLabel: String?
          if (_stmt.isNull(_columnIndexOfClassifierLabel)) {
            _tmpClassifierLabel = null
          } else {
            _tmpClassifierLabel = _stmt.getText(_columnIndexOfClassifierLabel)
          }
          _item = SpeciesEntity(_tmpId,_tmpScientificName,_tmpPopularName,_tmpFamily,_tmpCategory,_tmpVenomous,_tmpRiskLevel,_tmpDescription,_tmpGeographicDistribution,_tmpFirstAidProcedure,_tmpReferenceImagePath,_tmpClassifierLabel)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun findByClassifierLabel(label: String): SpeciesEntity? {
    val _sql: String = "SELECT * FROM species WHERE classifierLabel = ? LIMIT 1"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, label)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfScientificName: Int = getColumnIndexOrThrow(_stmt, "scientificName")
        val _columnIndexOfPopularName: Int = getColumnIndexOrThrow(_stmt, "popularName")
        val _columnIndexOfFamily: Int = getColumnIndexOrThrow(_stmt, "family")
        val _columnIndexOfCategory: Int = getColumnIndexOrThrow(_stmt, "category")
        val _columnIndexOfVenomous: Int = getColumnIndexOrThrow(_stmt, "venomous")
        val _columnIndexOfRiskLevel: Int = getColumnIndexOrThrow(_stmt, "riskLevel")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfGeographicDistribution: Int = getColumnIndexOrThrow(_stmt, "geographicDistribution")
        val _columnIndexOfFirstAidProcedure: Int = getColumnIndexOrThrow(_stmt, "firstAidProcedure")
        val _columnIndexOfReferenceImagePath: Int = getColumnIndexOrThrow(_stmt, "referenceImagePath")
        val _columnIndexOfClassifierLabel: Int = getColumnIndexOrThrow(_stmt, "classifierLabel")
        val _result: SpeciesEntity?
        if (_stmt.step()) {
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpScientificName: String
          _tmpScientificName = _stmt.getText(_columnIndexOfScientificName)
          val _tmpPopularName: String
          _tmpPopularName = _stmt.getText(_columnIndexOfPopularName)
          val _tmpFamily: String
          _tmpFamily = _stmt.getText(_columnIndexOfFamily)
          val _tmpCategory: String
          _tmpCategory = _stmt.getText(_columnIndexOfCategory)
          val _tmpVenomous: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfVenomous).toInt()
          _tmpVenomous = _tmp != 0
          val _tmpRiskLevel: String
          _tmpRiskLevel = _stmt.getText(_columnIndexOfRiskLevel)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpGeographicDistribution: String
          _tmpGeographicDistribution = _stmt.getText(_columnIndexOfGeographicDistribution)
          val _tmpFirstAidProcedure: String
          _tmpFirstAidProcedure = _stmt.getText(_columnIndexOfFirstAidProcedure)
          val _tmpReferenceImagePath: String?
          if (_stmt.isNull(_columnIndexOfReferenceImagePath)) {
            _tmpReferenceImagePath = null
          } else {
            _tmpReferenceImagePath = _stmt.getText(_columnIndexOfReferenceImagePath)
          }
          val _tmpClassifierLabel: String?
          if (_stmt.isNull(_columnIndexOfClassifierLabel)) {
            _tmpClassifierLabel = null
          } else {
            _tmpClassifierLabel = _stmt.getText(_columnIndexOfClassifierLabel)
          }
          _result = SpeciesEntity(_tmpId,_tmpScientificName,_tmpPopularName,_tmpFamily,_tmpCategory,_tmpVenomous,_tmpRiskLevel,_tmpDescription,_tmpGeographicDistribution,_tmpFirstAidProcedure,_tmpReferenceImagePath,_tmpClassifierLabel)
        } else {
          _result = null
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun count(): Int {
    val _sql: String = "SELECT COUNT(*) FROM species"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
