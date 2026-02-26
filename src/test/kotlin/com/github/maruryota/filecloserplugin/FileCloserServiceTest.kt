package com.github.maruryota.filecloserplugin

import com.github.maruryota.filecloserplugin.services.FileCloserService
import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.testFramework.fixtures.BasePlatformTestCase

class FileCloserServiceTest : BasePlatformTestCase() {

    private lateinit var service: FileCloserService

    override fun setUp() {
        super.setUp()
        service = project.service<FileCloserService>()
    }

    fun testBuildTreeRootEmpty() {
        val root = service.buildTreeRoot()
        assertTrue("Root should have no entries when no files are open", root.extensionEntries.isEmpty())
    }

    fun testBuildTreeRootGroupsByExtension() {
        val file1 = myFixture.configureByText("test1.kt", "fun main() {}").virtualFile
        val file2 = myFixture.configureByText("test2.kt", "fun foo() {}").virtualFile
        val file3 = myFixture.configureByText("readme.md", "# Hello").virtualFile

        FileEditorManager.getInstance(project).openFile(file1, false)
        FileEditorManager.getInstance(project).openFile(file2, false)
        FileEditorManager.getInstance(project).openFile(file3, false)

        val root = service.buildTreeRoot()
        assertEquals("Should have 2 extension groups", 2, root.extensionEntries.size)

        val ktEntry = root.extensionEntries.find { it.extension == "kt" }
        assertNotNull("Should have kt extension group", ktEntry)
        assertEquals("kt group should have 2 files", 2, ktEntry!!.files.size)

        val mdEntry = root.extensionEntries.find { it.extension == "md" }
        assertNotNull("Should have md extension group", mdEntry)
        assertEquals("md group should have 1 file", 1, mdEntry!!.files.size)
    }

    fun testBuildTreeRootSortedByExtension() {
        val file1 = myFixture.configureByText("test.py", "pass").virtualFile
        val file2 = myFixture.configureByText("test.kt", "fun main() {}").virtualFile
        val file3 = myFixture.configureByText("test.java", "class Test {}").virtualFile

        FileEditorManager.getInstance(project).openFile(file1, false)
        FileEditorManager.getInstance(project).openFile(file2, false)
        FileEditorManager.getInstance(project).openFile(file3, false)

        val root = service.buildTreeRoot()
        val extensions = root.extensionEntries.map { it.extension }
        assertEquals("Extensions should be sorted", extensions.sorted(), extensions)
    }

    fun testBuildTreeRootFilesSortedByName() {
        val fileB = myFixture.configureByText("beta.kt", "").virtualFile
        val fileA = myFixture.configureByText("alpha.kt", "").virtualFile
        val fileC = myFixture.configureByText("charlie.kt", "").virtualFile

        FileEditorManager.getInstance(project).openFile(fileB, false)
        FileEditorManager.getInstance(project).openFile(fileA, false)
        FileEditorManager.getInstance(project).openFile(fileC, false)

        val root = service.buildTreeRoot()
        val ktEntry = root.extensionEntries.find { it.extension == "kt" }!!
        val names = ktEntry.files.map { it.displayName }
        assertEquals("Files should be sorted by name", names.sorted(), names)
    }

    fun testBuildTreeRootDuplicateNames() {
        val file1 = myFixture.configureByText("test.kt", "fun main() {}").virtualFile
        myFixture.addFileToProject("sub/test.kt", "fun foo() {}")
        val file2 = myFixture.findFileInTempDir("sub/test.kt")!!

        FileEditorManager.getInstance(project).openFile(file1, false)
        FileEditorManager.getInstance(project).openFile(file2, false)

        val root = service.buildTreeRoot()
        val ktEntry = root.extensionEntries.find { it.extension == "kt" }
        assertNotNull(ktEntry)

        ktEntry!!.files.forEach { fileEntry ->
            assertTrue(
                "Duplicate name files should show full path: ${fileEntry.displayName}",
                fileEntry.displayName.contains("/"),
            )
        }
    }

    fun testBuildTreeRootUniqueNamesShowShortName() {
        val file1 = myFixture.configureByText("Foo.kt", "class Foo").virtualFile
        val file2 = myFixture.configureByText("Bar.kt", "class Bar").virtualFile

        FileEditorManager.getInstance(project).openFile(file1, false)
        FileEditorManager.getInstance(project).openFile(file2, false)

        val root = service.buildTreeRoot()
        val ktEntry = root.extensionEntries.find { it.extension == "kt" }!!

        ktEntry.files.forEach { fileEntry ->
            assertFalse(
                "Unique name files should show short name: ${fileEntry.displayName}",
                fileEntry.displayName.contains("/"),
            )
        }
    }

    fun testCloseFile() {
        val file = myFixture.configureByText("test.kt", "fun main() {}").virtualFile
        FileEditorManager.getInstance(project).openFile(file, false)

        assertTrue("File should be open", FileEditorManager.getInstance(project).isFileOpen(file))

        val result = service.closeFile(file)

        assertTrue("closeFile should return true", result)
        assertFalse("File should be closed", FileEditorManager.getInstance(project).isFileOpen(file))
    }

    fun testCloseFiles() {
        val file1 = myFixture.configureByText("a.kt", "").virtualFile
        val file2 = myFixture.configureByText("b.kt", "").virtualFile
        val file3 = myFixture.configureByText("c.kt", "").virtualFile

        FileEditorManager.getInstance(project).openFile(file1, false)
        FileEditorManager.getInstance(project).openFile(file2, false)
        FileEditorManager.getInstance(project).openFile(file3, false)

        val closed = service.closeFiles(listOf(file1, file2, file3))

        assertEquals("All 3 files should be closed", 3, closed)
        assertFalse(FileEditorManager.getInstance(project).isFileOpen(file1))
        assertFalse(FileEditorManager.getInstance(project).isFileOpen(file2))
        assertFalse(FileEditorManager.getInstance(project).isFileOpen(file3))
    }

    fun testCloseFileUpdatesTree() {
        val file1 = myFixture.configureByText("a.kt", "").virtualFile
        val file2 = myFixture.configureByText("b.py", "").virtualFile

        FileEditorManager.getInstance(project).openFile(file1, false)
        FileEditorManager.getInstance(project).openFile(file2, false)

        var root = service.buildTreeRoot()
        assertEquals("Should have 2 groups before close", 2, root.extensionEntries.size)

        service.closeFile(file1)

        root = service.buildTreeRoot()
        assertEquals("Should have 1 group after closing kt file", 1, root.extensionEntries.size)
        assertEquals("Remaining group should be py", "py", root.extensionEntries[0].extension)
    }
}
