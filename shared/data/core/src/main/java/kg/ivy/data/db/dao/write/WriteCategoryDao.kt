package kg.ivy.data.db.dao.write

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kg.ivy.data.db.entity.CategoryEntity
import java.util.UUID

@Dao
interface WriteCategoryDao {
    @Upsert
    suspend fun save(value: CategoryEntity)

    @Upsert
    suspend fun saveMany(values: List<CategoryEntity>)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteById(id: UUID)

    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
