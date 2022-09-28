package ui;

import java.util.Comparator;

/**
 * Nasljeđuje {@link CostNode} tako što je dodana heuristika.
 * @author Antonio
 *
 */
public class HeuristicNode extends CostNode {
	private double heuristic;

	public HeuristicNode(String state, HeuristicNode parent, double cost, double heuristic) {
		super(state, parent, cost);
		this.heuristic = heuristic;
	}

	public double getheuristic() {
		return heuristic;
	}

	@Override
	public HeuristicNode getParent() {
		return (HeuristicNode) super.getParent();
	}

	//komparator korišten  za usporedbu 2 heuristička cvora
	public static final Comparator<HeuristicNode> COMPARE_BY_TOTAL = (n1, n2) -> Double
			.compare(n1.getheuristic(), n2.getheuristic());
}
