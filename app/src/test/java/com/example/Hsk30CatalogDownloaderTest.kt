package com.example

import com.example.data.Hsk30CatalogDownloader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class Hsk30CatalogDownloaderTest {

    @Test
    fun parsesMinifiedAndFullHsk30LevelTags() {
        // Minified complete.min.json tags
        assertEquals(1, Hsk30CatalogDownloader.parseNewHskLevel("n1"))
        assertEquals(2, Hsk30CatalogDownloader.parseNewHskLevel("n2"))
        assertEquals(6, Hsk30CatalogDownloader.parseNewHskLevel("n6"))
        assertEquals(7, Hsk30CatalogDownloader.parseNewHskLevel("n7"))
        assertEquals(7, Hsk30CatalogDownloader.parseNewHskLevel("n7+"))
        assertEquals(7, Hsk30CatalogDownloader.parseNewHskLevel("n7-9"))
        assertEquals(8, Hsk30CatalogDownloader.parseNewHskLevel("n8"))
        assertEquals(9, Hsk30CatalogDownloader.parseNewHskLevel("n9"))

        // Full complete.json tags
        assertEquals(1, Hsk30CatalogDownloader.parseNewHskLevel("new-1"))
        assertEquals(4, Hsk30CatalogDownloader.parseNewHskLevel("new-4"))
        assertEquals(7, Hsk30CatalogDownloader.parseNewHskLevel("new-7+"))
        assertEquals(7, Hsk30CatalogDownloader.parseNewHskLevel("new-7-9"))

        // Legacy HSK 2.0 tags must not be treated as HSK 3.0 levels
        assertNull(Hsk30CatalogDownloader.parseNewHskLevel("o1"))
        assertNull(Hsk30CatalogDownloader.parseNewHskLevel("o5"))
        assertNull(Hsk30CatalogDownloader.parseNewHskLevel("old-3"))

        assertTrue(Hsk30CatalogDownloader.isOldHskLevel("o1"))
        assertTrue(Hsk30CatalogDownloader.isOldHskLevel("o6"))
        assertTrue(Hsk30CatalogDownloader.isOldHskLevel("old-3"))
        assertFalse(Hsk30CatalogDownloader.isOldHskLevel("n1"))
        assertFalse(Hsk30CatalogDownloader.isOldHskLevel("new-1"))
    }
}
