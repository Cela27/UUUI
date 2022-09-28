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
import java.util.function.ToDoubleFunction;

/**
 * Class to solve problem using A* alghorithm.
 * @author Antonio
 *
 */
public class ASTAR {
	private String beginningState;
	private List<String> endStates=new ArrayList<>();
	private Map<String, Set<Pair>> mapSS = new HashMap<>();
	private Map<String, Double> mapH = new HashMap<>();
	private String sourceFileSS;
	private String sourceFileH;
	private Set<String> statesVisited = new HashSet<>();
	private double cost;
	/**
	 * Funkcija za dobivanje sljedbenika stanja.
	 */
	private Function<String, Set<Pair>> succ = new Function<String, Set<Pair>>() {

		@Override
		public Set<Pair> apply(String t) {
			return mapSS.get(t);
		}
	};
	/**
	 * Predikat za provjeru jel dano stanje i cilnno stanje.
	 */
	private Predicate<String> goal = new Predicate<String>() {

		@Override
		public boolean test(String t) {
			return endStates.contains(t);
		}
	};
	/**
	 * Funkcija za dobivanje heuristike za dano stanje
	 */
	private ToDoubleFunction<String> heuristic = new ToDoubleFunction<String>() {

		@Override
		public double applyAsDouble(String value) {
			return mapH.get(value);
		}
	};
	
	/**
	 * Pokretanje A* algoritma i traženje rješenja.
	 * @param sourceFileSS
	 * @param sourceFileH
	 */
	public ASTAR(String sourceFileSS, String sourceFileH) {
		this.sourceFileSS = sourceFileSS;
		this.sourceFileH = sourceFileH;

		loadingData(sourceFileSS, sourceFileH);

		HeuristicNode node = startASTAR(beginningState, succ, goal, heuristic);

		System.out.println("# ASTAR");
		if (node == null) {
			System.out.println("[FOUND_SOLUTION]: no");
		} else {
			System.out.println("[FOUND_SOLUTION]: yes");
			System.out.println("[STATES_VISITED]: " + (statesVisited.size()));
			String path = HeuristicNode.nodePath(node);
			String[] states = path.split(" => ");
			System.out.println("[PATH_LENGTH]: " + states.length);
			System.out.println("[TOTAL_COST]: " + node.getCost());
			System.out.println("[PATH]:" + path);
		}
	}
	/**
	 * Pokrece provjeru sa zadanim pocetnim stanjem
	 * @param sourceFileSS
	 * @param sourceFileH
	 * @param beginningState
	 */
	public ASTAR(String sourceFileSS, String sourceFileH, String beginningState) {
		this.sourceFileSS = sourceFileSS;
		this.sourceFileH = sourceFileH;

		loadingData(sourceFileSS, sourceFileH);
		this.beginningState=beginningState;
		
		HeuristicNode node = startASTAR(beginningState, succ, goal, heuristic);
		
		if (node == null) {
			
		} else {
			cost=node.getCost();
		}
	}
	
	public double getCost() {
		return cost;
	}

	/**
	 * Pokreće A* algoritam i potragu.
	 * @param beginningState
	 * @param succ
	 * @param goal
	 * @param heuristic
	 * @return
	 */
	public HeuristicNode startASTAR(String beginningState, Function<String, Set<Pair>> succ,
			Predicate<String> goal, ToDoubleFunction<String> heuristic) {
		
		//Koristim queue zbog mogucnosti koristenja PriorityQueue-a koji ih reda po kompratoru po heuristici 
		Queue<HeuristicNode> open = new PriorityQueue<>(HeuristicNode.COMPARE_BY_TOTAL);
		open.add(new HeuristicNode(beginningState, null, 0.0, heuristic.applyAsDouble(beginningState)));
		//za optimizaciju pamtim stanja kroz koja sam prošao za svako stanje
		Set<String> visited = new HashSet<>();
		
		while (!open.isEmpty()) {
			HeuristicNode node = open.remove();
			//to pamtim ukupno sva stanja kroz koja sam prošao
			statesVisited.add(node.getState());
			if (goal.test(node.getState()))
				return node;
			visited.add(node.getState());
			//prolazim kroz sve sljedbenike
			for (Pair child : succ.apply(node.getState())) {
				//ako smo već bili negdje onda to mjesto ignoriramo
				if (visited.contains(child.getState()))
					continue;
				
				double cost = node.getCost() + child.getCost();
				//zbroji heuristiku i trenutnu cijenu da imaš pretpostavku kolko treba do kraja
				double total = cost + heuristic.applyAsDouble(child.getState());
				boolean openHasCheaper = false;
				HeuristicNode forRemoval=null;
				for(HeuristicNode nextNode: open) {
					//ako nisu ista stanja preskacem
					if (!nextNode.getState().equals(child.getState()))
						continue;
					//ako je jeftinije pamtim to
					if (nextNode.getheuristic() <= total) {
						openHasCheaper = true;
					} else {
						forRemoval=nextNode;
					}
					break;
				}
				if (!openHasCheaper) {
					if(forRemoval!=null)
						open.remove(forRemoval);
					HeuristicNode childNode = new HeuristicNode(child.getState(), node, cost, total);
					open.add(childNode);
				}
			}
		}
		return null;
	}
	
	//klasicna funkcija ucitavanja podataka
	private void loadingData(String sourceFileSS, String sourceFileH) {
		// load SS
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFileSS, Charset.forName("UTF-8")))) {
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

					mapSS.put(state, set);
				} else if (i == 1) {
					String[] splits=line.split(" ");
					for(String split: splits) {
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
		// load heuristic
		try (BufferedReader reader = new BufferedReader(
				new FileReader(sourceFileH, Charset.forName("UTF-8")))) {
			String line = reader.readLine();

			while (line != null) {
				if (line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}
				String[] splits = line.split(" ");
				mapH.put(splits[0].substring(0, splits[0].length() - 1), Double.parseDouble(splits[1]));
				line = reader.readLine();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	
}
