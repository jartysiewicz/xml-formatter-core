package com.greenbird.prettyprinter;

import org.junit.Test;

import com.greenbird.xml.prettyprinter.plain.AbstractPrettyPrinter;
import com.greenbird.xml.prettyprinter.plain.filter.AbstractMultiFilterPrettyPrinter;

/**
 * For full code coverage of enum subclasses.
 * 
 * @author thomas
 *
 */

public class TestEnums {

	@Test
	public void testEnum1() {
		AbstractPrettyPrinter.Type.valueOf(AbstractPrettyPrinter.Type.DECREMENT.toString());
		AbstractPrettyPrinter.Type.valueOf(AbstractPrettyPrinter.Type.INCREMENT.toString());
		AbstractPrettyPrinter.Type.valueOf(AbstractPrettyPrinter.Type.NEITHER.toString());
	}
	
	@Test
	public void testEnum2() {
		AbstractPrettyPrinter.CharactersType.valueOf(AbstractPrettyPrinter.CharactersType.CDATA.toString());
		AbstractPrettyPrinter.CharactersType.valueOf(AbstractPrettyPrinter.CharactersType.COMMENT.toString());
		AbstractPrettyPrinter.CharactersType.valueOf(AbstractPrettyPrinter.CharactersType.NONE.toString());
	}

	@Test
	public void testEnum3() {
		AbstractMultiFilterPrettyPrinter.FilterType.valueOf(AbstractMultiFilterPrettyPrinter.FilterType.ANON.toString());
		AbstractMultiFilterPrettyPrinter.FilterType.valueOf(AbstractMultiFilterPrettyPrinter.FilterType.PRUNE.toString());
	}
}
