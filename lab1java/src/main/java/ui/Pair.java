package ui;

/**
 * Klasa koja omogucava u obliku para sprema stanje i njegovu cijenu.
 * @author Antonio
 *
 */
public class Pair implements Comparable<Pair> {
	private String state;
	private double cost;

	public Pair(String state, double cost) {
		super();
		this.state = state;
		this.cost = cost;
	}

	public String getState() {
		return state;
	}

	public double getCost() {
		return cost;
	}

	@Override
	public String toString() {
		return String.format("%s, %.1f", state, cost);
	}

	@Override
	public int compareTo(Pair o) {
		return this.getState().compareTo(o.getState());
	}
	
	
}
