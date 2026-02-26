package com.github.maruryota.filecloserplugin.ui

import com.github.maruryota.filecloserplugin.MyBundle
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.ExtensionEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.FileEntry
import com.intellij.icons.AllIcons
import com.intellij.ui.components.JBLabel
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Point
import java.awt.Rectangle
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.tree.TreeCellRenderer

class FileCloserTreeCellRenderer : TreeCellRenderer {

    companion object {
        /** Extra pixels added to the left of the GC button for easier clicking. */
        private const val GC_HIT_PADDING = 20
    }

    private val panel = JPanel(BorderLayout()).apply { isOpaque = false }
    private val textLabel = JBLabel()
    private val gcButton = JBLabel(AllIcons.Actions.GC)

    init {
        panel.add(textLabel, BorderLayout.WEST)
        panel.add(gcButton, BorderLayout.EAST)
    }

    override fun getTreeCellRendererComponent(
        tree: JTree,
        value: Any?,
        selected: Boolean,
        expanded: Boolean,
        leaf: Boolean,
        row: Int,
        hasFocus: Boolean,
    ): Component {
        when (value) {
            is ExtensionEntry -> {
                textLabel.text = MyBundle.message("extensionEntry", value.extension, value.files.size)
            }
            is FileEntry -> {
                textLabel.text = value.displayName
            }
            else -> {
                textLabel.text = value.toString()
            }
        }
        return panel
    }

    /**
     * Performs a hit-test on the given row at the given point.
     *
     * IntelliJ's WideSelectionTreeUI renders cells at the full tree width,
     * but [JTree.getRowBounds] only returns the content's preferred width.
     * This method uses `tree.width - rowBounds.x` as the render width so
     * the GC button position matches its actual on-screen location.
     *
     * @return [FileCloserTreeNode.GCButton] if the click landed on the trash icon,
     *         [FileCloserTreeNode.NameArea] if the click landed on the text area,
     *         or `null` if the point is outside the row bounds.
     */
    fun hitTest(tree: JTree, row: Int, point: Point): FileCloserTreeNode? {
        val rowBounds = tree.getRowBounds(row) ?: return null

        if (point.y < rowBounds.y || point.y >= rowBounds.y + rowBounds.height) return null
        if (point.x < rowBounds.x) return null

        // Use the full available width to match WideSelectionTreeUI rendering
        val renderWidth = maxOf(rowBounds.width, tree.width - rowBounds.x)

        val node = tree.getPathForRow(row)?.lastPathComponent ?: return null
        getTreeCellRendererComponent(tree, node, false, false, false, row, false)
        panel.setBounds(0, 0, renderWidth, rowBounds.height)
        panel.doLayout()

        val localX = point.x - rowBounds.x
        val localY = point.y - rowBounds.y

        // Expand the GC button hit area with extra padding on the left
        val gc = gcButton.bounds
        val hitArea = Rectangle(gc.x - GC_HIT_PADDING, gc.y, gc.width + GC_HIT_PADDING, gc.height)
        if (hitArea.contains(localX, localY)) {
            return FileCloserTreeNode.GCButton
        }
        return FileCloserTreeNode.NameArea
    }
}
