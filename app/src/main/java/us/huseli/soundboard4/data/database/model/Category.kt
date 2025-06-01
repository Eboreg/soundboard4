package us.huseli.soundboard4.data.database.model

import androidx.compose.runtime.Immutable
import androidx.room.Entity
import androidx.room.PrimaryKey
import us.huseli.soundboard4.SoundSortingKey
import java.util.UUID

@Entity(tableName = "categories")
@Immutable
data class Category(
    @PrimaryKey override val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val backgroundColor: Int = 0,
    val isCollapsed: Boolean = false,
    val position: Int = 0,
    val sortingKey: SoundSortingKey = SoundSortingKey.NAME,
    val sortAscending: Boolean = true,
) : IModel<Category>
