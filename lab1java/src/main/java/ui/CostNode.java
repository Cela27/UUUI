package ui;

/**
 * Nasljeđuje {@link Node} tako da je još dodana cijena uz stanje
 * @author Antonio
 *
 */
public class CostNode extends Node implements Comparable<CostNode> {
	protected double cost;

	public CostNode(String state, CostNode parent, double cost) {
		super(state, parent);
		this.cost = cost;
	}

	public double getCost() {
		return cost;
	}

	@Override
	public CostNode getParent() {
		return (CostNode) super.getParent();
	}

	@Override
	public int compareTo(CostNode other) {
		if(Double.compare(this.cost, other.cost)==0)
			return this.getState().compareTo(other.getState());
		return Double.compare(this.cost, other.cost);
	}
}
