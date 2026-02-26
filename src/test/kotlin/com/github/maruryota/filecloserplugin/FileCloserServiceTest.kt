package com.github.maruryota.filecloserplugin

import com.github.maruryota.filecloserplugin.model.FileCloserTreeNode
import com.github.maruryota.filecloserplugin.services.FileCloserService
import com.github.maruryota.filecloserplugin.ui.FileCloserTreeCellRenderer
import com.github.maruryota.filecloserplugin.ui.FileCloserTreeModel
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.fixtures.BasePlatformTestCase
import com.intellij.ui.treeStructure.Tree
import java.awt.Point

class FileCloserServiceTest : BasePlatformTestCase() {

    private lateinit var service: FileCloserService

    override fun setUp() {
        super.setUp()
        service = project.service<FileCloserService>()
    }

    // ── buildTreeRoot ──

    fun testBuildTreeRootEmpty() {
        val root = service.buildTreeRoot()
        assertTrue(root.extensionEntries.isEmpty())
    }

    fun testBuildTreeRootGroupsByExtension() {
        openFile("a.kt")
        openFile("b.kt")
        openFile("c.md")

        val root = service.buildTreeRoot()
        assertEquals(2, root.extensionEntries.size)
        assertEquals(2, root.extensionEntries.first { it.extension == "kt" }.files.size)
        assertEquals(1, root.extensionEntries.first { it.extension == "md" }.files.size)
    }

    fun testBuildTreeRootSortedByExtension() {
        openFile("test.py")
        openFile("test.kt")
        openFile("test.java")

        val extensions = service.buildTreeRoot().extensionEntries.map { it.extension }
        assertEquals(extensions.sorted(), extensions)
    }

    fun testBuildTreeRootFilesSortedByName() {
        openFile("beta.kt")
        openFile("alpha.kt")
        openFile("charlie.kt")

        val names = service.buildTreeRoot().extensionEntries.first { it.extension == "kt" }.files.map { it.displayName }
        assertEquals(names.sorted(), names)
    }

    fun testBuildTreeRootDuplicateNamesShowFullPath() {
        openFile("test.kt")
        myFixture.addFileToProject("sub/test.kt", "")
        val file2 = myFixture.findFileInTempDir("sub/test.kt")!!
        FileEditorManager.getInstance(project).openFile(file2, false)

        val files = service.buildTreeRoot().extensionEntries.first { it.extension == "kt" }.files
        files.forEach { assertTrue("expected path separator: ${it.displayName}", it.displayName.contains("/")) }
    }

    fun testBuildTreeRootUniqueNamesShowShortName() {
        openFile("Foo.kt")
        openFile("Bar.kt")

        val files = service.buildTreeRoot().extensionEntries.first { it.extension == "kt" }.files
        files.forEach { assertFalse("expected short name: ${it.displayName}", it.displayName.contains("/")) }
    }

    // ── closeFile / closeFiles ──

    fun testCloseFile() {
        val file = openFile("test.kt")
        val fem = FileEditorManager.getInstance(project)

        assertTrue(fem.isFileOpen(file))
        assertTrue(service.closeFile(file))
        assertFalse(fem.isFileOpen(file))
    }

    fun testCloseFiles() {
        val files = listOf(openFile("a.kt"), openFile("b.kt"), openFile("c.kt"))
        val fem = FileEditorManager.getInstance(project)

        assertEquals(3, service.closeFiles(files))
        files.forEach { assertFalse(fem.isFileOpen(it)) }
    }

    fun testCloseFileUpdatesTree() {
        val ktFile = openFile("a.kt")
        openFile("b.py")

        assertEquals(2, service.buildTreeRoot().extensionEntries.size)

        service.closeFile(ktFile)

        val root = service.buildTreeRoot()
        assertEquals(1, root.extensionEntries.size)
        assertEquals("py", root.extensionEntries[0].extension)
    }

    // ── hitTest ──

    fun testHitTestNameArea() {
        openFile("test.kt")
        val (renderer, tree) = buildTree()
        tree.expandRow(0)

        val bounds = tree.getRowBounds(0)!!
        val midY = bounds.y + bounds.height / 2

        // Click on the left side of the row → NameArea
        val result = renderer.hitTest(tree, 0, Point(bounds.x + 5, midY))
        assertEquals(FileCloserTreeNode.NameArea, result)
    }

    fun testHitTestGCButton() {
        openFile("test.kt")
        val (renderer, tree) = buildTree()
        tree.expandRow(0)

        val bounds = tree.getRowBounds(0)!!
        val midY = bounds.y + bounds.height / 2

        // GC button is at the right edge of the TREE (not rowBounds)
        val result = renderer.hitTest(tree, 0, Point(tree.width - 2, midY))
        assertEquals(FileCloserTreeNode.GCButton, result)
    }

    fun testHitTestOutsideRowReturnsNull() {
        openFile("test.kt")
        val (renderer, tree) = buildTree()
        tree.expandRow(0)

        val bounds = tree.getRowBounds(0)!!

        // Click below the row → null
        val result = renderer.hitTest(tree, 0, Point(bounds.x + 5, bounds.y + bounds.height + 50))
        assertNull(result)
    }

    // ── helpers ──

    private fun openFile(name: String): VirtualFile {
        val file = myFixture.configureByText(name, "").virtualFile
        FileEditorManager.getInstance(project).openFile(file, false)
        return file
    }

    private fun buildTree(): Pair<FileCloserTreeCellRenderer, Tree> {
        val renderer = FileCloserTreeCellRenderer()
        val tree = Tree(FileCloserTreeModel(service.buildTreeRoot())).apply {
            isRootVisible = false
            showsRootHandles = true
            cellRenderer = renderer
        }
        tree.setSize(400, 200)
        tree.doLayout()
        return renderer to tree
    }
}
