package com.github.maruryota.filecloserplugin.model

import com.intellij.openapi.vfs.VirtualFile

sealed class FileCloserTreeNode {

    data class RootNode(val extensionEntries: List<ExtensionEntry>) : FileCloserTreeNode()

    data class ExtensionEntry(
        val extension: String,
        val files: List<FileEntry>,
    ) : FileCloserTreeNode()

    data class FileEntry(
        val file: VirtualFile,
        val displayName: String,
    ) : FileCloserTreeNode()

    /** Hit-test result: click landed on the text / name area of a row. */
    data object NameArea : FileCloserTreeNode()

    /** Hit-test result: click landed on the GC (trash) button of a row. */
    data object GCButton : FileCloserTreeNode()
}
