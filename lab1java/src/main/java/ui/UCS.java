package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class to solve problem using UCS alghorithm.
 * 
 * @author Antonio
 *
 */
public class UCS {
	private String beginningState;
	private List<String> endStates = new ArrayList<>();
	private Map<String, Set<Pair>> map = new HashMap<>();
	private String sourceFile;
	private Set<String> statesVisited = new HashSet<>();
	private double cost;
	
	public double getCost() {
		return cost;
	}

	/**
	 * Funkcija koja vraća sljedbenika za dano stanje.
	 */
	private Function<String, Set<Pair>> succ = new Function<String, Set<Pair>>() {

		@Override
		public Set<Pair> apply(String t) {
			return map.get(t);
		}
	};

	/**
	 * Predikat koji provjerava jel dano stanje ujedno i ciljno stanje.
	 */
	private Predicate<String> goal = new Predicate<String>() {

		@Override
		public boolean test(String t) {
			return endStates.contains(t);
		}
	};

	/**
	 * Pokretanje UCS algoritma sa danim sourceFileom i ispis rezultata u traženom
	 * formatu
	 * 
	 * @param sourceFile
	 */
	public UCS(String sourceFile) {

		loadingData(sourceFile);

		CostNode node = startUCS(beginningState, succ, goal);

		System.out.println("# UCS");
		if (node == null) {
			System.out.println("[FOUND_SOLUTION]: no");
		} else {
			System.out.println("[FOUND_SOLUTION]: yes");
			// +1 za poc stanje
			System.out.println("[STATES_VISITED]: " + (statesVisited.size() + 1));
			String path = CostNode.nodePath(node);
			String[] states = path.split(" => ");
			System.out.println("[PATH_LENGTH]: " + states.length);
			System.out.println("[TOTAL_COST]: " + node.getCost());
			System.out.println("[PATH]:" + path);
		}
	}

	/**
	 * UCS search with desired beginning state.
	 * 
	 * @param sourceFile
	 * @param beginingState
	 * @param beginningState 
	 */
	public UCS(String sourceFile, String beginningState) {
		this.sourceFile = sourceFile;


		loadingData(sourceFile);
		this.beginningState=beginningState;
		
		CostNode node = startUCS(beginningState, succ, goal);
		
		if (node == null) {
			
		} else {
			cost=node.getCost();
		}
	}

	/**
	 * Pokreće UCS algoritam i potragu
	 * 
	 * @param beginningState početno stanje
	 * @param succ           funkcija sljedbenika
	 * @param goal           predikat za provjeru cilja
	 * @return
	 */
	public CostNode startUCS(String beginningState, Function<String, Set<Pair>> succ, Predicate<String> goal) {

		// Koristim queue zbog mogucnosti koristenja PriorityQueue-a
		Queue<CostNode> open = new PriorityQueue<>();
		open.add(new CostNode(beginningState, null, 0.0));
		// za optimizaciju pamtim stanja kroz koja sam prošao za svako stanje
		Set<String> visited = new HashSet<>();
		while (!open.isEmpty()) {

			CostNode node = open.remove();
			// to pamtim ukupno sva stanja kroz koja sam prošao
			statesVisited.add(node.getState());
			if (goal.test(node.getState())) {
				return node;
			}

			visited.add(node.getState());
			// prolazim kroz sve sljedbenike
			for (Pair child : succ.apply(node.getState())) {
				// ako smo već bili negdje onda to mjesto ignoriramo
				if (visited.contains(child.getState()))
					continue;

				double childPathCost = node.getCost() + child.getCost();
				boolean openHasCheaper = false;
				CostNode forRemoval = null;

				// prolazim po svim elementima liste open i tražim jeftiniju opciju za to stanje
				for (CostNode nextNode : open) {
					// ako nisu ista stanja preskacem
					if (!nextNode.getState().equals(child.getState()))
						continue;
					// ako je jeftinije pamtim to
					if (nextNode.getCost() <= childPathCost) {
						openHasCheaper = true;
						// ako nije onda ga brisem
					} else {
						forRemoval = nextNode;
					}
					// vidjeli smo ako je isto stanje u open i rješit ćemo to
					break;
				}
				// nema jeftinijega pa ovo trenutno dodajemo unutra i mičemo skuplje ako postoji
				if (!openHasCheaper) {
					if (forRemoval != null)
						open.remove(forRemoval);
					CostNode childNode = new CostNode(child.getState(), node, childPathCost);
					open.add(childNode);
				}
			}
		}
		return null;
	}

	// očitavanje podataka
	private void loadingData(String sourceFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				if (line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}

				if (i > 1) {
					String[] splits = line.split(" ");
					String state = splits[0].substring(0, splits[0].length() - 1);
					Set<Pair> set = new TreeSet<>();

					for (int j = 1; j < splits.length; j++) {
						set.add(new Pair(splits[j].substring(0, splits[j].indexOf(',')),
								Double.parseDouble(splits[j].substring(splits[j].indexOf(',') + 1))));
					}

					map.put(state, set);
				} else if (i == 1) {
					String[] splits = line.split(" ");
					for (String split : splits) {
						endStates.add(split);
					}
					i++;
				}

				else if (i == 0) {
					i++;
					beginningState = line;

				}
				line = reader.readLine();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
