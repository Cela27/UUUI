package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Class to solve problem using BFS alghorithm.
 * @author Antonio
 *
 */
public class BFS {
	
	private String beginningState;
	private List<String> endStates=new ArrayList<>();
	//mapa u koju spremam očitane podatke
	private Map<String, Set<Pair>> map= new HashMap<>();
	//lokacija izvorne datoteke
	private String sourceFile;
	private Set<String> sVisited=new HashSet<>();
	
	private boolean solutionFound=false;
	private int pathLength;
	private double cost;
	private String path;
	private int statesVisited;
	
	/**
	 * Funkcija koja vraća sljedbenika za dano stanje.
	 */
	private Function<String, Set<String>> succ= new Function<String, Set<String>>() {

		@Override
		public Set<String> apply(String t) {
			Set<Pair> set=map.get(t);
			Set<String> neighbours= new TreeSet<>();
			for(Pair p: set) {
				neighbours.add(p.getState());
			}
			return neighbours;
		}
	};
	
	/**
	 * Predikat koji provjerava jel dano stanje ujedno i ciljno stanje.
	 */
	private Predicate<String> goal= new Predicate<String>() {

		@Override
		public boolean test(String t) {
			return endStates.contains(t);
		}
	};
	
	/**
	 * pokretanje BFS algoritma sa danim sourcFileom
	 * @param sourceFile
	 */
	public BFS(String sourceFile) {
		loadingData(sourceFile);
	}
	
	/**
	 * Starts BFS search but you can give desired begginingState
	 * @param sourceFile
	 * @param beginningState
	 */
	public BFS(String sourceFile, String beginningState) {
		loadingData(sourceFile);	
		this.beginningState=beginningState;
	}
	
	/**
	 * Ispisuje rezultate u traženom formatu
	 * @param ispis
	 */
	public void ispis(boolean ispis) {
		Node node=startBFS(beginningState, succ, goal);
		if(ispis)
			System.out.println("# BFS");
		if(node==null) {
			System.out.println("[FOUND_SOLUTION]: no");		
		}
		else {
			
			solutionFound=true;
			if(ispis)
				System.out.println("[FOUND_SOLUTION]: yes");
			if(ispis)
				System.out.println("[STATES_VISITED]: "+ sVisited.size());
			statesVisited=sVisited.size();
			path= Node.nodePath(node);
			String[] states= path.split(" => ");
			if(ispis)
				System.out.println("[PATH_LENGTH]: "+states.length);
			pathLength=states.length;
			cost=0;
			for(int i=0; i<states.length-1;i++) {
				Set<Pair> set=map.get(states[i]);
				for(Pair p: set) {
					if(p.getState().equals(states[i+1])) {
						cost+=p.getCost();
					}
				}
			}
			if(ispis)
				System.out.println("[TOTAL_COST]: "+cost);
			if(ispis)
				System.out.println("[PATH]:" + path);
		}
	}
	
	public boolean isSolutionFound() {
		return solutionFound;
	}

	public int getPathLength() {
		return pathLength;
	}

	public double getCost() {
		return cost;
	}

	public String getPath() {
		return path;
	}
	/**
	 * Pokreće BFS algoritam i potragu
	 * @param beginningState početno stanje
	 * @param succ funkcija sljedbenika
	 * @param goal predikat za provjeru cilja
	 * @return
	 */
	public Node startBFS(String beginningState, Function<String, Set<String>> succ,
			Predicate<String> goal) {
		//koristim Deque jer je s njime lakše raditi nego s Listom u ovom slučaju
		Deque<Node> open = new LinkedList<>();
		Node startingNode = new Node(beginningState, null);
		//dodaje stanje kroz koje smo prošli
		sVisited.add(startingNode.getState());
		if (goal.test(beginningState)) {
			return startingNode;
		}
		//u open je prvo samo početno stanje
		open.add(startingNode);
		
		//vrti sve dok open nije prazan ili dok nismo rješenje našli
		while (!open.isEmpty()) {
			Node node = open.removeFirst();
			//Prođi po svakom sljedbeniku
			for (String child : succ.apply(node.getState())) {
				Node childNode = new Node(child, node);
				//zabilježi koja si sve stanja prošao-to mi treba samo zbog potrebe ispisa
				sVisited.add(child);
				//testiraj jel cilj zadovoljen
				if (goal.test(child)) {
					return childNode;
				}
				//dodaj na kraj
				open.addLast(childNode);
			}
		}
		return null;
	}
	
	//obična funkcija za očitavanje podataka iz sourceFilea
	private void loadingData(String sourceFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, Charset.forName("UTF-8")))) {
			String line= reader.readLine();
			int i=0;
			while(line!=null) {
				if(line.startsWith("#")) {
					line=reader.readLine();
					continue;
				}
				
				if(i>1) {
					String[] splits= line.split(" ");
					String state=splits[0].substring(0, splits[0].length()-1);
					Set<Pair> set=new TreeSet<>();
					
					for(int j=1; j<splits.length;j++) {
						set.add(new Pair(splits[j].substring(0, splits[j].indexOf(',')), Double.parseDouble(splits[j].substring(splits[j].indexOf(',')+1))));
					}
					
					map.put(state, set);
				}
				else if(i==1) {
					String[] splits=line.split(" ");
					for(String split: splits) {
						endStates.add(split);
					}
						
					i++;
				}
				
				else if(i==0) {
					i++;
					beginningState=line;
					
				}
				line=reader.readLine();
			}
		}catch(Exception ex) {
			ex.printStackTrace();
		}
	}
}
