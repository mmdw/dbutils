package dbhelper.graph;

import java.util.Iterator;
import java.util.LinkedList;

public interface NodeVisitor<E> {
	public void visit(LinkedList<Pair<E ,Iterator<E>>> path);
}
