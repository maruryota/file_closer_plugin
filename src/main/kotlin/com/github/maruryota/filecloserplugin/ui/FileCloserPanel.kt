package com.github.maruryota.filecloserplugin.ui

import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.ExtensionEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.FileEntry
import com.github.maruryota.filecloserplugin.services.FileCloserService
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManagerEvent
import com.intellij.openapi.fileEditor.FileEditorManagerListener
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

class FileCloserPanel(private val project: Project) : JPanel(BorderLayout()), Disposable {

    private val service = project.service<FileCloserService>()
    private val treeModel = FileCloserTreeModel(service.buildTreeRoot())
    private val tree = Tree(treeModel).apply {
        isRootVisible = false
        showsRootHandles = true
        cellRenderer = FileCloserTreeCellRenderer()
    }

    init {
        add(JBScrollPane(tree), BorderLayout.CENTER)
        setupMouseListener()
        subscribeToFileEvents()
    }

    private fun setupMouseListener() {
        tree.addMouseListener(object : MouseAdapter() {
            override fun mousePressed(e: MouseEvent) {
                val row = tree.getClosestRowForLocation(e.x, e.y)
                if (row < 0) return

                val rowBounds = tree.getRowBounds(row) ?: return
                if (e.y < rowBounds.y || e.y >= rowBounds.y + rowBounds.height) return

                if (!FileCloserTreeCellRenderer.isDeleteIconHit(tree, row, e.x)) return

                val path = tree.getPathForRow(row)
                val node = path?.lastPathComponent ?: return

                when (node) {
                    is FileEntry -> {
                        service.closeFile(node.file)
                    }
                    is ExtensionEntry -> {
                        service.closeFiles(node.files.map { it.file })
                    }
                }
                // Tree will refresh via MessageBus listener
            }
        })
    }

    private fun subscribeToFileEvents() {
        val connection = project.messageBus.connect(this)
        connection.subscribe(
            FileEditorManagerListener.FILE_EDITOR_MANAGER,
            object : FileEditorManagerListener {
                override fun fileOpened(source: com.intellij.openapi.fileEditor.FileEditorManager, file: VirtualFile) {
                    refreshTree()
                }

                override fun fileClosed(source: com.intellij.openapi.fileEditor.FileEditorManager, file: VirtualFile) {
                    refreshTree()
                }

                override fun selectionChanged(event: FileEditorManagerEvent) {
                    // no-op
                }
            },
        )
    }

    private fun refreshTree() {
        treeModel.refresh(service.buildTreeRoot())
    }

    override fun dispose() {
        // connection is disposed via parent Disposable
    }
}
