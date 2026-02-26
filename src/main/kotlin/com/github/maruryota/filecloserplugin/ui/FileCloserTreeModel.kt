package com.github.maruryota.filecloserplugin.ui

import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.ExtensionEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.FileEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.RootNode
import javax.swing.event.TreeModelEvent
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

class FileCloserTreeModel(private var root: RootNode) : TreeModel {

    private val listeners = mutableListOf<TreeModelListener>()

    fun refresh(newRoot: RootNode) {
        root = newRoot
        val event = TreeModelEvent(this, arrayOf<Any>(root))
        listeners.forEach { it.treeStructureChanged(event) }
    }

    override fun getRoot(): Any = root

    override fun getChild(parent: Any?, index: Int): Any = when (parent) {
        is RootNode -> parent.extensionEntries[index]
        is ExtensionEntry -> parent.files[index]
        else -> throw IllegalArgumentException("Unexpected node: $parent")
    }

    override fun getChildCount(parent: Any?): Int = when (parent) {
        is RootNode -> parent.extensionEntries.size
        is ExtensionEntry -> parent.files.size
        is FileEntry -> 0
        else -> 0
    }

    override fun isLeaf(node: Any?): Boolean = node is FileEntry

    override fun valueForPathChanged(path: TreePath?, newValue: Any?) {
        // not editable
    }

    override fun getIndexOfChild(parent: Any?, child: Any?): Int = when (parent) {
        is RootNode -> parent.extensionEntries.indexOf(child)
        is ExtensionEntry -> parent.files.indexOf(child)
        else -> -1
    }

    override fun addTreeModelListener(l: TreeModelListener?) {
        l?.let { listeners.add(it) }
    }

    override fun removeTreeModelListener(l: TreeModelListener?) {
        l?.let { listeners.remove(it) }
    }
}
