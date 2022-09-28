package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.TreeMap;

/**
 * Provjerava optimisticnost heuristike
 * @author Antonio
 *
 */
public class H_OPT {
	private String beginningState;
	private String endState;
	
	private String sourceFileSS;
	private String sourceFileH;
	private Map<String, Double> mapH= new TreeMap<>();
	
	private boolean errHappend=false;
	
	/**
	 * Pokrece algoritam za provjeru optimisticnosti heuristike.
	 * @param sourceFileSS
	 * @param sourceFileH
	 */
	public H_OPT(String sourceFileSS, String sourceFileH) {
		this.sourceFileSS=sourceFileSS;
		this.sourceFileH=sourceFileH;
		loadingData(sourceFileH);
		startChecking();
	}
	/**
	 * Pokrece provjeravanje i ispis.
	 */
	private void startChecking() {
		System.out.println("# HEURISTIC-OPTIMISTIC "+sourceFileH);
		//prolaz kroz sve canove mape sa heuristikama
		for(Map.Entry<String, Double> ent: mapH.entrySet()) {
			//koristim UCS alg da mi pomogne u provjeri 
			UCS ucs= new UCS(sourceFileSS, ent.getKey());
			//provjera optimisticnosti
			if(ent.getValue()<=ucs.getCost()) {
				System.out.println("[CONDITION]: [OK] h("+ent.getKey()+") <= h*: "+ent.getValue()+" <= "+ ucs.getCost());
			}
			else {
				System.out.println("[CONDITION]: [ERR] h("+ent.getKey()+") <= h*: "+ent.getValue()+" <= "+ ucs.getCost());
				errHappend=true;
			}
		}
		//zakljucak
		if(errHappend)
			System.out.println("[CONCLUSION]: Heuristic is not optimistic.");
		else
			System.out.println("[CONCLUSION]: Heuristic is optimistic.");
	}
	
	//klasicno ocitavanje podataka
	private void loadingData(String sourceFileH) {
		
		//load heuristic
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFileH, Charset.forName("UTF-8")))) {
			String line=reader.readLine();
			
			while(line!=null){
				if(line.startsWith("#")) {
					line=reader.readLine();
					continue;
				}
				String[] splits=line.split(" ");
				mapH.put(splits[0].substring(0, splits[0].length()-1), Double.parseDouble(splits[1]));
				line=reader.readLine();
			}
		}catch(Exception e) {
			e.printStackTrace();
		}
		
	}
	
}
