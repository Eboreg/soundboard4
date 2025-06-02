package us.huseli.soundboard4.data.database.model

interface IModel {
    data class Diff<T : IModel>(val new: List<T>, val deleted: List<T>, val changed: List<T>)

    val id: String
}

fun <T : IModel> Iterable<T>.diff(other: Iterable<T>): IModel.Diff<T> = IModel.Diff(
    new = filter { !other.map { it.id }.contains(it.id) },
    deleted = other.filter { !map { it.id }.contains(it.id) },
    changed = filter { item -> other.find { it.id == item.id }?.let { it != item } == true },
)
