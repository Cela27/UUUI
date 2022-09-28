package ui;

/**
 * Klasa koja predstavlja čvor u stablu pretraživanja.
 * @author Antonio
 *
 */
public class Node {

	private Node parent;

	String state;
	/**
	 * Konstruktor
	 * @param state stanje čvora
	 * @param parent roditelj čvora
	 */
	public Node(String state, Node parent) {
		super();
		this.state = state;
		this.parent = parent;
	}

	public Node getParent() {
		return parent;
	}

	public String getState() {
		return state;
	}
	/**
	 * Računa dubinu čvora
	 * @return
	 */
	public int getDepth() {
		int depth = 0;
		Node current = this.getParent();
		while (current != null) {
			depth++;
			current = current.getParent();
		}
		return depth;
	}

	@Override
	public String toString() {
		return String.format("%s", state);
	}
	//vraća putanju do čvora
	public static String nodePath(Node node) {
		StringBuilder sb = new StringBuilder();
		nodePathRecursive(sb, node);
		return sb.toString();
	}
	//vraća putanju obrnuto
	private static void nodePathRecursive(StringBuilder sb, Node node) {
		if (node.getParent() != null) {
			nodePathRecursive(sb, node.getParent());
			sb.append(" => ");
		}
		sb.append(node);
	}
}