package com.github.maruryota.filecloserplugin.ui

import com.github.maruryota.filecloserplugin.MyBundle
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.ExtensionEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.FileEntry
import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.TreeCellRenderer

class FileCloserTreeCellRenderer : TreeCellRenderer {

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component {
        val panel = JPanel(BorderLayout())
        panel.isOpaque = false

        val textLabel: JBLabel
        when (value) {
            is ExtensionEntry -> {
                textLabel = JBLabel(
                    MyBundle.message("extensionEntry", value.extension, value.files.size),
                )
            }
            is FileEntry -> {
                textLabel = JBLabel(value.displayName)
            }
            else -> {
                textLabel = JBLabel(value.toString())
            }
        }

        val deleteLabel = JBLabel(AllIcons.Actions.GC)

        panel.add(textLabel, BorderLayout.CENTER)
        panel.add(deleteLabel, BorderLayout.EAST)

        return panel
    }

    companion object {
        /**
         * Returns true if the given x coordinate is within the delete icon region
         * for a cell at the given row in the tree.
         */
        fun isDeleteIconHit(tree: JTree, row: Int, x: Int): Boolean {
            val bounds = tree.getRowBounds(row) ?: return false
            val iconWidth = AllIcons.Actions.GC.iconWidth
            val deleteRegionStart = bounds.x + bounds.width - iconWidth - 4
            return x >= deleteRegionStart
        }
    }
}
