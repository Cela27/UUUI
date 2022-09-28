package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Class for traing serach tree with ID3.
 * @author Antonio
 *
 */
public class ID3 {

	List<String> znacajke = new ArrayList<>();
	List<String> znacajkeSCiljem = new ArrayList<>();
	List<String> znacajke2 = new ArrayList<>();
	List<List<String>> redoviPodataka = new ArrayList<>();
	List<List<String>> redoviTest = new ArrayList<>();
	String znacajkaCilja;
	private Set<String> instanceCilja = new TreeSet<>();
	Node node;
	Integer maxLevel;

	/*
	 * Trainging model without maxLevel parameter.
	 */
	public ID3(String sourceFile, String testFile) {
		super();
		loadingData(sourceFile);
		loadingTests(testFile);
		znacajkaCilja = znacajke.remove(znacajke.size() - 1);
		nadiCiljeve(redoviPodataka);
		List<String> tmpZnacajke = new ArrayList<>();
		for (String znacajka : znacajke) {
			tmpZnacajke.add(znacajka);
			znacajkeSCiljem.add(znacajka);
		}
		znacajkeSCiljem.add(znacajkaCilja);

		node = id3(redoviPodataka, redoviPodataka, tmpZnacajke, znacajkaCilja);

		System.out.println("[BRANCHES]:");
		
		node.ispis();
		predict();
	}

	/**
	 * Training model with maxLevel parameter.
	 * @param sourceFile
	 * @param testFile
	 * @param maxLevel
	 */
	public ID3(String sourceFile, String testFile, int maxLevel) {
		super();
		this.maxLevel=maxLevel;
		loadingData(sourceFile);
		loadingTests(testFile);
		znacajkaCilja = znacajke.remove(znacajke.size() - 1);
		nadiCiljeve(redoviPodataka);
		List<String> tmpZnacajke = new ArrayList<>();
		for (String znacajka : znacajke) {
			tmpZnacajke.add(znacajka);
			znacajkeSCiljem.add(znacajka);
		}
		znacajkeSCiljem.add(znacajkaCilja);
		System.out.println(maxLevel);
		node = id3Levels(redoviPodataka, redoviPodataka, tmpZnacajke, znacajkaCilja, 1);

		System.out.println("[BRANCHES]:");
		
		node.ispis();
		predict();

	}
	/**
	 * Uses model to predict results.
	 */
	private void predict() {
		List<String> predictions = new ArrayList<>();
		List<String> testResults = new ArrayList<>();

		for (List<String> list : redoviTest) {
			testResults.add(list.get(list.size() - 1));
		}
		boolean unseen=false;
		for (List<String> list : redoviTest) {
			Node currNode = node;

			while (!currNode.isLeaf()) {

				String znacajka = currNode.getZnacajka();
				String instanca = list.get(znacajke.indexOf(znacajka));
				
				Set<String> instance=instanceZaZnacajku(znacajka, redoviPodataka);
				unseen=false;
				if(!instance.contains(instanca)) {
					unseen=true;
				}
				
				
				Map<Node, String> map = currNode.getDijeteInstanca();
				
				for (Node child : currNode.getChildren()) {
					String tmpInstanca = map.get(child);
				
					if (tmpInstanca.equals(instanca)) {
						currNode = child;
						break;
					}
				}
				if(unseen) {
					//dodaje abecedno najmanjeg
					for(String str:instanceCilja) {
						predictions.add(str);
						break;
					}
					break;
				}
			}
			if(!unseen)
				predictions.add(currNode.getInstancaCilja());

		}

		// predikcije

		System.out.print("[PREDICTIONS]: ");

		for (String prediction : predictions) {
			System.out.print(prediction + " ");
		}
		System.out.println();
		double br = predictions.size();
		int correct = 0;
		for (int i = 0; i < testResults.size(); i++) {
			if (predictions.get(i).equals(testResults.get(i))) {
				correct++;
			}
		}
		double accuracy = correct / br;
		double broj = 100000;
		accuracy = Math.round(accuracy * broj) / broj;

		String acc = String.valueOf(accuracy);
		while (acc.length() != 7) {
			acc = acc + "0";
		}

		System.out.println("[ACCURACY]: " + acc);

		// matrica
		Map<String, Map<String, Integer>> matrica = new TreeMap<>();

		for (String cilj : instanceCilja) {
			Map<String, Integer> map = new TreeMap<>();
			for (String cilj2 : instanceCilja) {
				map.put(cilj2, 0);
			}
			matrica.put(cilj, map);
		}

		for (int i = 0; i < testResults.size(); i++) {
			Map<String, Integer> map = matrica.get(testResults.get(i));
			map.put(predictions.get(i), map.get(predictions.get(i)) + 1);
			matrica.put(testResults.get(i), map);
		}
		System.out.println("[CONFUSION_MATRIX]:");
		for (String key : matrica.keySet()) {
			Map<String, Integer> map = matrica.get(key);

			for (String key2 : map.keySet()) {
				System.out.print(map.get(key2) + " ");
			}
			System.out.println();
		}
	}

	private List<List<String>> skratiData(List<List<String>> data, String instanca, String znacajka) {
		List<List<String>> toRemove = new ArrayList<>();
		List<List<String>> newData = new ArrayList<>();

		for (List<String> red : data) {
			newData.add(red);
		}
		for (List<String> red : data) {

			if (!red.get(znacajkeSCiljem.indexOf(znacajka)).equals(instanca)) {
				toRemove.add(red);
			}
		}
		/**********************************/
		newData.removeAll(toRemove);

		return newData;
	}

	/**
	 * Recursive function for training model.
	 * @param data
	 * @param dataParent
	 * @param znacajke
	 * @param znacajkaCilja
	 * @return
	 */
	private Node id3(List<List<String>> data, List<List<String>> dataParent, List<String> znacajke,
			String znacajkaCilja) {

		String instancaCilja;
		// if D = ∅ then
		if (data.isEmpty()) {
			
			instancaCilja = argMax(dataParent, znacajkaCilja);
			
			return new Node(znacajkaCilja, instancaCilja);
		}
		// izbor instance, nju ne koristimo nigdje vise osim u sljd ifu,
		// to je ista stvar ko gore s roditeljom samo ovdje sa normalnom datom
		// ispred je zbog ifa jer treba smanjit datu za y=v i onda ako se nis zapravo ne
		// makne tj neki odg je 0(mozes i to koristit)
		// onda vracas Leaf Node

		instancaCilja = argMax(data, znacajkaCilja);

		List<List<String>> novaData = skratiData(data, instancaCilja, znacajkaCilja);
		
		if (znacajke.isEmpty() || data.equals(novaData)) {
			
			return new Node(znacajkaCilja, instancaCilja);
		}

		// ovdje krecemo zapravo

		String odabranaZnacajka = odaberiZnacajku(znacajke, data);
		Node node = new Node(odabranaZnacajka);
		znacajke.remove(odabranaZnacajka);

		// ovdje treba po instancama ic a ne znacajkama ja msm

		// nadi istance za nekuZnacajku

		Set<String> instanceZaZnacajku = instanceZaZnacajku(odabranaZnacajka, data);

		// tu sam dodao u skrati data znacajka cilja mozda zezene
		for (String instanca : instanceZaZnacajku) {
			List<List<String>> skraceniData = skratiData(data, instanca, odabranaZnacajka);
			List<String> tmpZnacajke = new ArrayList<>();
			for (String znacajka : znacajke) {
				tmpZnacajke.add(znacajka);
			}
			
			Node tmpNode = id3(skraceniData, data, tmpZnacajke, znacajkaCilja);
			
			tmpNode.setParent(node);
			node.addChild(tmpNode, instanca);

		}

		return node;
	}
	
	/**
	 * recursive function for training model till given max level.
	 * @param data
	 * @param dataParent
	 * @param znacajke
	 * @param znacajkaCilja
	 * @param level
	 * @return
	 */
	private Node id3Levels(List<List<String>> data, List<List<String>> dataParent, List<String> znacajke,
			String znacajkaCilja, int level) {

		String instancaCilja;

		
		// if D = ∅ then
		if (data.isEmpty()) {
			
			instancaCilja = argMax(dataParent, znacajkaCilja);
			return new Node(znacajkaCilja, instancaCilja);
		}
		// izbor instance, nju ne koristimo nigdje vise osim u sljd ifu,
		// to je ista stvar ko gore s roditeljom samo ovdje sa normalnom datom
		// ispred je zbog ifa jer treba smanjit datu za y=v i onda ako se nis zapravo ne
		// makne tj neki odg je 0(mozes i to koristit)
		// onda vracas Leaf Node

		instancaCilja = argMax(data, znacajkaCilja);

		List<List<String>> novaData = skratiData(data, instancaCilja, znacajkaCilja);
		
		if (znacajke.isEmpty() || data.equals(novaData)) {
			return new Node(znacajkaCilja, instancaCilja);
		}
		
		if (level > maxLevel) {
			instancaCilja = argMax(data, znacajkaCilja);
			return new Node(znacajkaCilja, instancaCilja);
		}
		
		// ovdje krecemo zapravo

		String odabranaZnacajka = odaberiZnacajku(znacajke, data);
		Node node = new Node(odabranaZnacajka);
		znacajke.remove(odabranaZnacajka);

		// ovdje treba po instancama ic a ne znacajkama ja msm

		// nadi istance za nekuZnacajku

		Set<String> instanceZaZnacajku = instanceZaZnacajku(odabranaZnacajka, data);

		// tu sam dodao u skrati data znacajka cilja mozda zezene
		for (String instanca : instanceZaZnacajku) {
			List<List<String>> skraceniData = skratiData(data, instanca, odabranaZnacajka);
			List<String> tmpZnacajke = new ArrayList<>();
			for (String znacajka : znacajke) {
				tmpZnacajke.add(znacajka);
			}
			int nextLevel=level+1;
			Node tmpNode = id3Levels(skraceniData, data, tmpZnacajke, znacajkaCilja, nextLevel);
			
			tmpNode.setParent(node);
			node.addChild(tmpNode, instanca);

		}

		return node;
	}
	
	/**
	 * ArgMax function for finding needed instance.
	 * @param dataParent
	 * @param znacajkaCilja
	 * @return
	 */
	private String argMax(List<List<String>> dataParent, String znacajkaCilja) {
		Map<String, Integer> map = new TreeMap<>();
		for (String cilj : instanceCilja) {
			map.put(cilj, 0);
		}

		for (List<String> list : dataParent) {
			String instanca = list.get(list.size() - 1);
			map.put(instanca, map.get(instanca) + 1);
		}

		String maxInstanca = null;
		int max = 0;
		for (String instanca : map.keySet()) {
			// tu mozda problem ako jednako
			if (map.get(instanca) > max) {
				max = map.get(instanca);
				maxInstanca = instanca;
			}
		}

		return maxInstanca;
	}

	private Set<String> instanceZaZnacajku(String znacajka, List<List<String>> data) {
		Set<String> set = new TreeSet<>();
		int index = znacajke.indexOf(znacajka);
		if (index < 0) {
			System.out.println(znacajka);
		}
		for (List<String> red : data) {
			set.add(red.get(index));
		}

		return set;
	}

	private String odaberiZnacajku(List<String> znacajke, List<List<String>> data) {
		String odabranaZnacajka = "";
		double maxIG = 0;
		for (String znacajka : znacajke) {
			double IG = racunajIG(znacajka, data);
			System.out.print("IG(" + znacajka + ")=" + IG + " ");
			if (IG > maxIG) {
				odabranaZnacajka = znacajka;
				maxIG = IG;
			} else if (IG == maxIG) {
				if (odabranaZnacajka.compareTo(znacajka) > 0) {
					odabranaZnacajka = znacajka;
				}
			}
		}
		System.out.println();
		return odabranaZnacajka;
	}
	/**
	 * Calculates IG.
	 * @param znacajka
	 * @param data
	 * @return
	 */
	private double racunajIG(String znacajka, List<List<String>> data) {
		double IG = 0;

		Map<String, List<String>> znacajkaRezultatiMapa = kreirajZnacajkaRezultatiMapa(data);

		Set<String> set = new TreeSet<>();
		List<String> list = znacajkaRezultatiMapa.get(znacajka);
		for (String instanca : list) {
			set.add(instanca);
		}

		List<Integer> listaPonavljanja = new ArrayList<>();
		// tmpMap cuva cilj i broj puta kolko se pojavljuje
		Map<String, Integer> tmpMap = new TreeMap<>();

		for (String cilj : instanceCilja) {
			tmpMap.put(cilj, 0);
		}

		// kolko puta se za koju instancu pojavljuje koji cilj
		Map<String, Map<String, Map<String, Integer>>> znacajkaInstancaPojavljivanjeCiljevaKodNje = kreirajMapuZnacajkaInstancaPojavljivanjeCiljevaKodNje(
				data);

		Map<String, Map<String, Integer>> instancaPojavljivanjeCiljevaKodNje = znacajkaInstancaPojavljivanjeCiljevaKodNje
				.get(znacajka);

		for (String instanca : set) {

			Map<String, Integer> mapa = instancaPojavljivanjeCiljevaKodNje.get(instanca);

			for (Map.Entry<String, Integer> es : mapa.entrySet()) {
				tmpMap.put(es.getKey(), tmpMap.get(es.getKey()) + es.getValue());
			}
		}
		for (String key : tmpMap.keySet()) {
			listaPonavljanja.add(tmpMap.get(key));
		}
		IG = izracunajPocetnuEntropiju(listaPonavljanja);
		// do ovdje bi dobro trebalo biti

		// razlicite istance
		// za svaku istancu
		double ukPonavljanaj = 0;
		double sumaPrijeDjeljenjaSaUk = 0;

		for (String instanca : set) {
			Map<String, Integer> mapa = instancaPojavljivanjeCiljevaKodNje.get(instanca);
			List<Integer> listPonavljanja = new ArrayList<>();
			int brPonavljanaj = 0;
			for (Map.Entry<String, Integer> es : mapa.entrySet()) {
				listPonavljanja.add(es.getValue());
				brPonavljanaj += es.getValue();
			}
			ukPonavljanaj += brPonavljanaj;
			double tmpED = izracunajPocetnuEntropiju(listPonavljanja);
			sumaPrijeDjeljenjaSaUk += brPonavljanaj * tmpED;
		}
		IG = IG - sumaPrijeDjeljenjaSaUk / ukPonavljanaj;
		double broj = 10000;
		IG = Math.round(IG * broj) / broj;
		return IG;
	}

	private Map<String, List<String>> kreirajZnacajkaRezultatiMapa(List<List<String>> data) {
		Map<String, List<String>> znacajkaRezultatiMapa = new TreeMap<>();
		for (String znacajka : znacajkeSCiljem) {

			znacajkaRezultatiMapa.put(znacajka, new ArrayList<>());
		}

		for (List<String> red : data) {
			for (int i = 0; i < znacajkeSCiljem.size(); i++) {
				List<String> list = znacajkaRezultatiMapa.get(znacajkeSCiljem.get(i));
				list.add(red.get(i));
				znacajkaRezultatiMapa.put(znacajkeSCiljem.get(i), list);

			}
		}
		return znacajkaRezultatiMapa;
	}

	private double izracunajPocetnuEntropiju(List<Integer> listaPonavljanja) {
		double pocEnt = 0;
		if (listaPonavljanja.contains(0)) {
			return 0;
		}
		double ukPojavljivanja = 0;
		for (Integer i : listaPonavljanja) {
			ukPojavljivanja += i;
		}

		for (Integer i : listaPonavljanja) {
			pocEnt -= (i / ukPojavljivanja) * log2(i / ukPojavljivanja);
		}

		return pocEnt;
	}

	private Map<String, Map<String, Map<String, Integer>>> kreirajMapuZnacajkaInstancaPojavljivanjeCiljevaKodNje(
			List<List<String>> data) {

		Map<String, Map<String, Map<String, Integer>>> znacajkaInstancaPojavljivanjeCiljevaKodNje = new TreeMap<>();

		Map<String, List<String>> znacajkaRezultatiMapa = kreirajZnacajkaRezultatiMapa(data);

		List<String> rezultati = znacajkaRezultatiMapa.get(znacajkaCilja);

		for (String znacajka : znacajke) {
			List<String> pojaveInstanciZnacajke = znacajkaRezultatiMapa.get(znacajka);

			Map<String, Map<String, Integer>> instancaPojavljivanjeCiljevaKodNje = new TreeMap<>();

			for (int i = 0; i < pojaveInstanciZnacajke.size(); i++) {

				if (instancaPojavljivanjeCiljevaKodNje.containsKey(pojaveInstanciZnacajke.get(i))) {
					Map<String, Integer> instancaCiljaBrPojava = instancaPojavljivanjeCiljevaKodNje
							.get(pojaveInstanciZnacajke.get(i));

					if (instancaCiljaBrPojava.containsKey(rezultati.get(i))) {
						instancaCiljaBrPojava.put(rezultati.get(i), instancaCiljaBrPojava.get(rezultati.get(i)) + 1);
					} else {
						instancaCiljaBrPojava.put(rezultati.get(i), 1);
					}

					instancaPojavljivanjeCiljevaKodNje.put(pojaveInstanciZnacajke.get(i), instancaCiljaBrPojava);
				} else {
					Map<String, Integer> instancaCiljaBrPojava = new TreeMap<>();
					instancaCiljaBrPojava.put(rezultati.get(i), 1);
					instancaPojavljivanjeCiljevaKodNje.put(pojaveInstanciZnacajke.get(i), instancaCiljaBrPojava);
				}
			}
			znacajkaInstancaPojavljivanjeCiljevaKodNje.put(znacajka, instancaPojavljivanjeCiljevaKodNje);
		}
		return znacajkaInstancaPojavljivanjeCiljevaKodNje;
	}

	private void loadingData(String sourceFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				if (line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}

				String[] splits = line.split(",");

				if (i == 0) {
					for (String split : splits) {
						znacajke.add(split);
						i++;
					}
				} else {
					List<String> list = new ArrayList<>();
					for (String split : splits) {
						list.add(split);

					}
					redoviPodataka.add(list);
				}

				line = reader.readLine();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private void loadingTests(String testFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(testFile, Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			int i = 0;
			while (line != null) {
				if (line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}

				String[] splits = line.split(",");

				if (i == 0) {
					for (String split : splits) {
						znacajke2.add(split);
						i++;
					}

				} else {
					List<String> list = new ArrayList<>();
					for (String split : splits) {
						list.add(split);

					}
					redoviTest.add(list);
				}

				line = reader.readLine();

			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	public double log2(double N) {
		double result = (Math.log(N) / Math.log(2));
		double broj = 10000;
		result = Math.round(result * broj) / broj;
		return result;
	}

	private void nadiCiljeve(List<List<String>> data) {
		for (List<String> list : data) {
			instanceCilja.add(list.get(list.size() - 1));
		}
	}
}
