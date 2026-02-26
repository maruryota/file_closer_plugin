package com.github.maruryota.filecloserplugin.services

import com.github.maruryota.filecloserplugin.MyBundle
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.ExtensionEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.FileEntry
import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode.RootNode
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.Service
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.VirtualFile

@Service(Service.Level.PROJECT)
class FileCloserService(private val project: Project) {

    fun buildTreeRoot(): RootNode {
        val openFiles = FileEditorManager.getInstance(project).openFiles.toList()
        val grouped = openFiles.groupBy { it.extension ?: MyBundle.message("noExtension") }

        val extensionEntries = grouped.entries
            .sortedBy { it.key }
            .map { (ext, files) ->
                val sortedFiles = files.sortedBy { it.name }
                val nameCount = sortedFiles.groupBy { it.name }
                val fileEntries = sortedFiles.map { file ->
                    val displayName = if ((nameCount[file.name]?.size ?: 0) > 1) {
                        file.path
                    } else {
                        file.name
                    }
                    FileEntry(file, displayName)
                }
                ExtensionEntry(ext, fileEntries)
            }

        return RootNode(extensionEntries)
    }

    /**
     * Close a single file. Returns true if closed, false if cancelled.
     */
    fun closeFile(file: VirtualFile): Boolean {
        val docManager = FileDocumentManager.getInstance()
        val document = docManager.getDocument(file)

        if (document != null && docManager.isDocumentUnsaved(document)) {
            val result = Messages.showYesNoCancelDialog(
                project,
                MyBundle.message("unsavedPrompt", file.name),
                MyBundle.message("unsavedTitle"),
                MyBundle.message("save"),
                MyBundle.message("dontSave"),
                MyBundle.message("cancel"),
                Messages.getQuestionIcon(),
            )
            when (result) {
                Messages.YES -> {
                    ApplicationManager.getApplication().runWriteAction {
                        docManager.saveDocument(document)
                    }
                }
                Messages.NO -> {
                    WriteCommandAction.runWriteCommandAction(project) {
                        document.setText(docManager.getDocument(file)?.text ?: "")
                    }
                    // Reload from disk to discard changes
                    ApplicationManager.getApplication().runWriteAction {
                        docManager.reloadFromDisk(document)
                    }
                }
                else -> return false // Cancel
            }
        }

        FileEditorManager.getInstance(project).closeFile(file)
        return true
    }

    /**
     * Close multiple files. Returns the number of files actually closed.
     * Stops on first cancel.
     */
    fun closeFiles(files: List<VirtualFile>): Int {
        var closed = 0
        for (file in files) {
            if (closeFile(file)) {
                closed++
            } else {
                break // Cancel stops the batch
            }
        }
        return closed
    }
}
