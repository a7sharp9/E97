package cscie97.smartcity.authentication;

/**
 * The functional interface that encapsulates navigation through hierarchies of
 * instances of IterableItem
 */
public interface ItemVisitor {
	public void visit (IterableItem<?> item, int level); 
}
