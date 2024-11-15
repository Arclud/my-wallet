package kg.ivy.data.repository

import kg.ivy.base.threading.DispatchersProvider
import kg.ivy.data.DataWriteEvent
import kg.ivy.data.db.dao.read.TagAssociationDao
import kg.ivy.data.db.dao.read.TagDao
import kg.ivy.data.db.dao.write.WriteTagAssociationDao
import kg.ivy.data.db.dao.write.WriteTagDao
import kg.ivy.data.model.Tag
import kg.ivy.data.model.TagAssociation
import kg.ivy.data.model.TagId
import kg.ivy.data.model.primitive.AssociationId
import kg.ivy.data.repository.mapper.TagMapper
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TagRepository @Inject constructor(
    private val mapper: TagMapper,
    private val tagDao: TagDao,
    private val tagAssociationDao: TagAssociationDao,
    private val writeTagDao: WriteTagDao,
    private val writeTagAssociationDao: WriteTagAssociationDao,
    private val dispatchersProvider: DispatchersProvider,
    memoFactory: RepositoryMemoFactory,
) {
    private val memo = memoFactory.createMemo(
        getDataWriteSaveEvent = DataWriteEvent::SaveTags,
        getDateWriteDeleteEvent = DataWriteEvent::DeleteTags,
    )

    suspend fun findById(id: TagId): Tag? = memo.findById(
        id = id,
        findByIdOperation = ::findByIdOperation
    )

    suspend fun findByIds(ids: List<TagId>): List<Tag> = memo.findByIds(
        ids = ids,
        findByIdOperation = ::findByIdOperation,
    )

    private suspend fun findByIdOperation(id: TagId): Tag? = tagDao.findByIds(id.value)
        ?.let {
            with(mapper) { it.toDomain().getOrNull() }
        }

    suspend fun findByAssociatedId(id: AssociationId): List<Tag> {
        return withContext(dispatchersProvider.io) {
            tagDao.findTagsByAssociatedId(id.value).let { entities ->
                entities.mapNotNull {
                    with(mapper) {
                        it.toDomain().getOrNull()
                    }
                }
            }
        }
    }

    suspend fun findByAssociatedId(
        ids: List<AssociationId>
    ): Map<AssociationId, List<Tag>> {
        return ids.chunked(MAX_SQL_LITE_QUERY_SIZE).map {
            withContext(dispatchersProvider.io) {
                async {
                    tagDao.findTagsByAssociatedIds(it.map { it.value })
                        .entries.associate { (id, tags) ->
                            val domainTags = tags.mapNotNull {
                                with(mapper) {
                                    it.toDomain().getOrNull()
                                }
                            }
                            AssociationId(id) to domainTags
                        }
                }
            }
        }.awaitAll().asSequence()
            .flatMap { it.asSequence() }
            .associate { it.key to it.value }
    }

    suspend fun findAll(): List<Tag> = memo.findAll(
        findAllOperation = {
            tagDao.findAll().let { entities ->
                entities.mapNotNull {
                    with(mapper) { it.toDomain().getOrNull() }
                }
            }
        },
        sortMemo = {
            sortedByDescending { it.creationTimestamp.epochSecond }
        }
    )

    suspend fun findByText(text: String): List<Tag> {
        return withContext(dispatchersProvider.io) {
            tagDao.findByText(text).let { entities ->
                entities.mapNotNull {
                    with(mapper) { it.toDomain().getOrNull() }
                }
            }
        }
    }

    suspend fun findByAllAssociatedIdForTagId(
        tagIds: List<TagId>
    ): Map<TagId, List<TagAssociation>> {
        return withContext(dispatchersProvider.io) {
            tagAssociationDao.findByAllAssociatedIdForTagId(
                tagIds.toRawValues()
            ).entries.associate { (id, associations) ->
                with(mapper) {
                    TagId(id) to associations.map { it.toDomain() }
                }
            }
        }
    }

    suspend fun findByAllTagsForAssociations(): Map<AssociationId, List<TagAssociation>> {
        return withContext(dispatchersProvider.io) {
            tagAssociationDao.findAll().groupBy {
                AssociationId(it.associatedId)
            }.mapValues {
                with(mapper) {
                    it.value.map { it.toDomain() }
                }
            }
        }
    }

    suspend fun associateTagToEntity(associationId: AssociationId, tagId: TagId) {
        withContext(dispatchersProvider.io) {
            writeTagAssociationDao.save(
                with(mapper) {
                    createNewTagAssociation(tagId, associationId).toEntity()
                }
            )
        }
    }

    suspend fun removeTagAssociation(associationId: AssociationId, tagId: TagId) {
        withContext(dispatchersProvider.io) {
            writeTagAssociationDao.deleteId(tagId = tagId.value, associatedId = associationId.value)
        }
    }

    suspend fun save(value: Tag): Unit = memo.save(value) {
        writeTagDao.save(with(mapper) { it.toEntity() })
    }

    suspend fun deleteById(id: TagId): Unit = memo.deleteById(
        id = id,
        deleteByIdOperation = {
            writeTagAssociationDao.deleteAssociationsByTagId(it.value)
            writeTagDao.deleteById(it.value)
        }
    )

    suspend fun deleteAll(): Unit = memo.deleteAll {
        writeTagAssociationDao.deleteAll()
        writeTagDao.deleteAll()
    }

    private fun List<TagId>.toRawValues(): List<UUID> = this.map { it.value }

    companion object {
        private const val MAX_SQL_LITE_QUERY_SIZE = 999
    }
}
