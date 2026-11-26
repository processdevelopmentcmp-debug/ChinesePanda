package com.example

import com.example.data.local.IdsDecompositionParser
import org.junit.Assert.assertEquals
import org.junit.Test

class HanziDecompositionParserTest {

    @Test
    fun decodesIdsStructureAndComponentsCorrectly() {
        assertEquals("Left + Right (⿰)", IdsDecompositionParser.extractStructureLabel("⿰女马"))
        assertEquals(listOf("女", "马"), IdsDecompositionParser.extractComponentGlyphs("妈", "⿰女马"))

        assertEquals("Top + Bottom (⿱)", IdsDecompositionParser.extractStructureLabel("⿱艹化"))
        assertEquals(listOf("艹", "化"), IdsDecompositionParser.extractComponentGlyphs("花", "⿱艹化"))

        assertEquals("Full Enclosure (⿴)", IdsDecompositionParser.extractStructureLabel("⿴囗玉"))
        assertEquals(listOf("囗", "玉"), IdsDecompositionParser.extractComponentGlyphs("国", "⿴囗玉"))

        assertEquals("Phono-Semantic" to "形声字", IdsDecompositionParser.formatFormationType("pictophonetic"))
        assertEquals("Compound Ideograph" to "会意字", IdsDecompositionParser.formatFormationType("ideographic"))
        assertEquals("Pictograph" to "象形字", IdsDecompositionParser.formatFormationType("pictographic"))

        assertEquals(listOf("电", "脑"), IdsDecompositionParser.extractHanziCharacters("电脑 (HSK 1)"))
    }
}
