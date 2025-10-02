package com.coheser.app.activitesfragments.livestreaming.utils
class ObservableArrayList<T> : ArrayList<T>() {
    var onAdd: ((T, Int) -> Unit)? = null
    var onRemove: ((T, Int) -> Unit)? = null
    var onUpdate: ((T, T, Int) -> Unit)? = null
    var onClear: (() -> Unit)? = null
    var onChange: ((List<T>) -> Unit)? = null  // General list update callback

    override fun add(element: T): Boolean {
        val result = super.add(element)
        onAdd?.invoke(element, size - 1)
        onChange?.invoke(this)
        return result
    }

    override fun add(index: Int, element: T) {
        super.add(index, element)
        onAdd?.invoke(element, index)
        onChange?.invoke(this)
    }

    override fun removeAt(index: Int): T {
        val removedItem = super.removeAt(index)
        onRemove?.invoke(removedItem, index)
        onChange?.invoke(this)
        return removedItem
    }

    override fun set(index: Int, element: T): T {
        val oldItem = this[index]
        val result = super.set(index, element)
        onUpdate?.invoke(oldItem, element, index)
        onChange?.invoke(this)
        return result
    }

    override fun clear() {
        super.clear()
        onClear?.invoke()
        onChange?.invoke(this)
    }
}
