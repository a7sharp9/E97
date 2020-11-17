package cscie97.smartcity.authentication;

import static java.lang.System.lineSeparator;

/**
 * An instance of visitor that traverses the tree of dependencies of an item and
 * adds their text representations to the internal buffer
 */
import java.util.stream.IntStream;

public class InventoryVisitor implements ItemVisitor {
	
	private final StringBuilder mStringBuilder = new StringBuilder ();
	private final static String PREFIX = "   ";

	public final String getText () {
		return (mStringBuilder.toString ());
	}
	
	@Override
	public void visit (IterableItem<?> item, int level) {
		StringBuilder prefixBuilder = new StringBuilder ();
		
		// add as meny instances of the prefix as the level we're at
		IntStream.range (0, level).forEach (i -> prefixBuilder.append (PREFIX));
		
		mStringBuilder.append (lineSeparator ()).append (item.show (prefixBuilder.toString ())).append (lineSeparator ());
	}

}
