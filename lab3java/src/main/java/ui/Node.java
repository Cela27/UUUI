package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Node for choice tree.
 * 
 * @author Antonio
 *
 */
public class Node {
	private Node parent;
	private String znacajka;
	private List<Node> children = new ArrayList<>();
	boolean leaf = false;
	Map<Node, String> dijeteInstanca = new HashMap<>();
	String instancaCilja;

	/**
	 * Getter for goal instance.
	 * 
	 * @return
	 */
	public String getInstancaCilja() {
		return instancaCilja;
	}

	/**
	 * Setter for goal instance.
	 * 
	 * @param instancaCilja
	 */
	public void setInstancaCilja(String instancaCilja) {
		this.instancaCilja = instancaCilja;
	}

	/**
	 * Konstruktor
	 * 
	 * @param state  stanje čvora
	 * @param parent roditelj čvora
	 */
	public Node(String znacajka) {
		super();
		this.znacajka = znacajka;
	}

	/**
	 * Konstruktor za leaf cvor.
	 * 
	 * @param znacajka
	 * @param instancaCilja
	 */
	public Node(String znacajka, String instancaCilja) {
		super();
		this.znacajka = znacajka;
		this.instancaCilja = instancaCilja;
		this.leaf = true;
	}

	/**
	 * Gets node parent.
	 * 
	 * @return
	 */
	public Node getParent() {
		return parent;
	}

	/**
	 * Add child.
	 * 
	 * @param node
	 * @param instanca
	 */
	public void addChild(Node node, String instanca) {
		children.add(node);
		dijeteInstanca.put(node, instanca);
	}

	/**
	 * Gets children.
	 * 
	 * @return
	 */
	public List<Node> getChildren() {
		return children;
	}

	/**
	 * Sets children
	 * 
	 * @param children
	 */
	public void setChildren(List<Node> children) {
		this.children = children;
	}

	/**
	 * Računa dubinu čvora
	 * 
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

	/**
	 * Vraca znacajku cvora.
	 * 
	 * @return
	 */
	public String getZnacajka() {
		return znacajka;
	}

	/**
	 * Postavlja znacajku.
	 * 
	 * @param znacajka
	 */
	public void setZnacajka(String znacajka) {
		this.znacajka = znacajka;
	}

	/**
	 * Sets parent.
	 * 
	 * @param parent
	 */
	public void setParent(Node parent) {
		this.parent = parent;
	}

	/**
	 * Ispisuje node.
	 */
	public void ispis() {
		Node node = this;
		if (node.getDepth() != 0) {
			System.out.println("krivi argument");
			return;
		}

		findLeafs(node);

	}

	private void findLeafs(Node node) {
		for (Node child : node.getChildren()) {
			if (child.isLeaf()) {
				child.ispisUnazad();
			} else {
				findLeafs(child);
			}
		}
	}

	private void ispisUnazad() {
		Node currNode = this;

		List<Node> list = new ArrayList<>();
		list.add(currNode);
		currNode = currNode.getParent();
		while (currNode != null) {
			list.add(currNode);

			currNode = currNode.getParent();
		}

		// -2 jer main cvor ne trebam za ispis
		for (int i = list.size() - 2; i >= 0; i--) {
			currNode = list.get(i);
			int depth = currNode.getDepth();
			System.out.print(depth + ":" + currNode + " ");
		}
		System.out.println();
	}

	@Override
	public String toString() {
		if (this.getParent() == null || this.getDepth() == 0) {
			return this.getZnacajka();
		}
		if (this.leaf) {
			return this.getParent().getZnacajka() + "=" + this.getParent().getDijeteInstanca().get(this) + " "
					+ this.getInstancaCilja();
		} else {
			return this.getParent().getZnacajka() + "=" + this.getParent().getDijeteInstanca().get(this);
		}

	}

	/**
	 * Checks if node is leaf.
	 * @return
	 */
	public boolean isLeaf() {
		return leaf;
	}
	
	/**
	 * Sets that node is leaf.
	 * @param leaf
	 */
	public void setLeaf(boolean leaf) {
		this.leaf = leaf;
	}
	/**
	 * Returns instances of child.
	 * @return
	 */
	public Map<Node, String> getDijeteInstanca() {
		return dijeteInstanca;
	}

	/**
	 * Returns instances of child.
	 * @return
	 */
	public void setDijeteInstanca(Map<Node, String> dijeteInstanca) {
		this.dijeteInstanca = dijeteInstanca;
	}

}
