package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Provjerava konzistentnost heuristike.
 * @author Antonio
 *
 */
public class H_CON {
	private String beginningState;
	private String endState;

	private String sourceFileSS;
	private String sourceFileH;
	private Map<String, Double> mapH = new TreeMap<>();
	private Map<String, Set<Pair>> mapSS = new TreeMap<>();
	
	private boolean errHappend = false;

	/**
	 * Pocetak provjere konzistentnosti.
	 * @param sourceFileSS
	 * @param sourceFileH
	 */
	public H_CON(String sourceFileSS, String sourceFileH) {
		this.sourceFileSS=sourceFileSS;
		this.sourceFileH=sourceFileH;
		loadingData(sourceFileSS, sourceFileH);
		this.sourceFileH=sourceFileH;
		startChecking();
	}
	
	/**
	 * Algoritam za provjeru.
	 */
	private void startChecking() {
		System.out.println("# HEURISTIC-CONSISTENT " + sourceFileH);
		//prolaz po mapi stanja i njenih sljedbenika
		for (Map.Entry<String, Set<Pair>> ent : mapSS.entrySet()) {
			
			Set<Pair> set= ent.getValue();
			
			for(Pair p: set) {
				double firstCost= mapH.get(ent.getKey());
				double secondCost= mapH.get(p.getState());
				//provjera konzistentnosti
				if(firstCost<=secondCost+p.getCost()) {
					System.out.println("[CONDITION]: [OK] h("+ent.getKey()+") <= h("+p.getState()+") + c: " +firstCost +" <= "+ secondCost +" + "+ p.getCost());
				}
				else {
					System.out.println("[CONDITION]: [ERR] h("+ent.getKey()+") <= h("+p.getState()+") + c: " +firstCost +" <= "+ secondCost +" + "+ p.getCost());
					errHappend=true;
				}
			}
		}
		//zakljucak
		if (errHappend)
			System.out.println("[CONCLUSION]: Heuristic is not consistent.");
		else
			System.out.println("[CONCLUSION]: Heuristic is consistent.");

	}
	//klasicno ocitavanje podataka
	private void loadingData(String sourceFileSS, String sourceFileH) {
		//load SS
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFileSS, Charset.forName("UTF-8")))) {
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
					
					mapSS.put(state, set);
				}
				else if(i==1) {
					endState=line;
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
