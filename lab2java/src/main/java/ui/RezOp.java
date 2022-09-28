package ui;

import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Rezolucija oporgavanjem
 * 
 * @author Antonio
 *
 */
public class RezOp {
	// maknut redundantne klauzule i nevazne starategijom brisanja
	// koristit upravljaˇcku strategiju skupa potpore

	private Map<Integer, List<String>> premise = new TreeMap<>();
	private Map<Integer, List<String>> pocPremise = new TreeMap<>();
	private String sourceFile;
	private String NEGATIVAN = "~";
	private boolean NIL = false;
	private boolean UNKNOWN = false;
	private int MAX_SIZE_KLAUZULE = 0;
	private List<KrizaniParovi> krizaniParovi = new LinkedList<>();
	private KrizaniParovi krizaniParZaNil;
	private Map<Integer, List<String>> sos = new TreeMap<>();
	private Map<Integer, List<String>> newSos = new TreeMap<>();
	private Map<Integer, List<String>> unija = new TreeMap<>();
	private List<String> cilj;
	Map<Integer, List<String>> tmpPremise = new TreeMap<>();

	Map<Integer, Integer> exNew = new TreeMap<>();
	Map<Integer, Integer> exNewKrizani = new TreeMap<>();
	Set<Integer> koristeneKlauzule = new TreeSet<>();

	List<Integer> brojeviCilja = new LinkedList<>();

	private Set<KrizaniParovi> relevantniKrizani = new TreeSet<>();
	int sizeNaPoc = 0;
	int ciljBrojNaPocetku = 0;

	public RezOp(String sourceFile) {
		this.sourceFile = sourceFile;
		loadingData(sourceFile);

		sizeNaPoc = premise.size();
		solve();
	}

	private void ispisiPremiseICilj() {
		for (Map.Entry<Integer, List<String>> ent : pocPremise.entrySet()) {
			System.out.print(ent.getKey() + ". ");
			for (int i = 0; i < ent.getValue().size(); i++) {
				System.out.print(ent.getValue().get(i));

				if (i < ent.getValue().size() - 1)
					System.out.print(" v ");

			}
			System.out.println();
		}

		int k = pocPremise.size() + 1;

		for (int i = 0; i < cilj.size(); i++) {
			System.out.print(k + ". ");
			System.out.print(negirajJedanClan(cilj.get(i)));
			System.out.println();
			k++;
		}
	}

	private void solve() {

		while (!NIL && !UNKNOWN) {

			napraviUniju();

			popuniNewSos();

			provjeriJelImaNevaznihUNewSosMedusobno();

			if (newSos.size() == 0 && !NIL)
				UNKNOWN = true;

			posloziKrizaneParove();

			poravnajSosBrojeveNakonBrisanja();

			poravnajKrizane();

			pripremiZaSljdPretragu();

		}

		if (NIL) {

			koristeneKlauzule.add(krizaniParZaNil.getPrvi());
			koristeneKlauzule.add(krizaniParZaNil.getDrugi());

			while (true) {
				boolean dodano = false;
				for (KrizaniParovi kp : relevantniKrizani) {

					if (koristeneKlauzule.contains(kp.getKey())) {
						if (!koristeneKlauzule.contains(kp.getPrvi())) {
							koristeneKlauzule.add(kp.getPrvi());
							dodano = true;
						}
						if (!koristeneKlauzule.contains(kp.getDrugi())) {
							koristeneKlauzule.add(kp.getDrugi());
							dodano = true;
						}

					}
				}
				if (!dodano)
					break;
			}

			for (Map.Entry<Integer, List<String>> ent : pocPremise.entrySet()) {
				if (koristeneKlauzule.contains(ent.getKey())) {
					tmpPremise.put(ent.getKey(), ent.getValue());
				}
			}

			// poravnajBrojeve
			Map<Integer, List<String>> mapa = new TreeMap<>();
			int j = 1;
			for (Map.Entry<Integer, List<String>> ent : tmpPremise.entrySet()) {
				exNew.put(ent.getKey(), j);
				mapa.put(j, ent.getValue());
				j++;
			}
			tmpPremise = mapa;

			for (int i = 1; i <= cilj.size(); i++) {
				exNew.put(sizeNaPoc + i, tmpPremise.size() + i);
			}

			Set<KrizaniParovi> finalniKrizaniParovi = new TreeSet<>();

			// skupi krizane parove
			for (KrizaniParovi kp : relevantniKrizani) {
				if (koristeneKlauzule.contains(kp.getBrojNoveKlauzule())) {
					finalniKrizaniParovi.add(kp);
				}
			}

			// poravnaj KP
			int broj = tmpPremise.size() + cilj.size() + 1;

			for (KrizaniParovi kp : finalniKrizaniParovi) {
				if (kp.getBrojNoveKlauzule() != broj) {
					exNew.put(kp.getBrojNoveKlauzule(), broj);
					kp.setBrojNoveKlauzule(broj);
				}
				broj++;
			}

			for (KrizaniParovi kp : finalniKrizaniParovi) {
				if (exNew.containsKey(kp.getPrvi())) {
					kp.setPrvi(exNew.get(kp.getPrvi()));
				}
				if (exNew.containsKey(kp.getDrugi())) {
					kp.setDrugi(exNew.get(kp.getDrugi()));
				}
			}

			// ispis premisa
			for (Map.Entry<Integer, List<String>> ent : tmpPremise.entrySet()) {
				System.out.print(ent.getKey() + ". ");
				for (int i = 0; i < ent.getValue().size(); i++) {
					System.out.print(ent.getValue().get(i));

					if (i < ent.getValue().size() - 1)
						System.out.print(" v ");

				}
				System.out.println();
			}

			// ispis cilj
			int k = tmpPremise.size() + 1;

			for (int i = 0; i < cilj.size(); i++) {
				exNew.put(brojeviCilja.get(0), k);
				System.out.print(k + ". ");
				System.out.print(negirajJedanClan(cilj.get(i)));
				System.out.println();
				k++;
			}

			System.out.println("===============");

			// ispis krizanja

			for (KrizaniParovi kp : finalniKrizaniParovi) {
				System.out.print(kp.getBrojNoveKlauzule() + ". ");
				for (int i = 0; i < kp.getRezultatKrizanja().size(); i++) {
					System.out.print(kp.getRezultatKrizanja().get(i));

					if (i < kp.getRezultatKrizanja().size() - 1)
						System.out.print(" v ");

				}
				if (kp.getPrvi() < kp.getDrugi())
					System.out.print(" (" + kp.getPrvi() + ", " + kp.getDrugi() + ")");
				if (kp.getPrvi() > kp.getDrugi())
					System.out.print(" (" + kp.getDrugi() + ", " + kp.getPrvi() + ")");
				System.out.println();
			}

			// ispis kraja

			if (exNew.get(krizaniParZaNil.getPrvi()) != null)
				krizaniParZaNil.setPrvi(exNew.get(krizaniParZaNil.getPrvi()));

			if (exNew.get(krizaniParZaNil.getDrugi()) != null)
				krizaniParZaNil.setDrugi(exNew.get(krizaniParZaNil.getDrugi()));

			if (krizaniParZaNil.getPrvi() < krizaniParZaNil.getDrugi())
				System.out.println(tmpPremise.size() + cilj.size() + finalniKrizaniParovi.size() + 1 + ". NIL ("
						+ krizaniParZaNil.getPrvi() + ", " + krizaniParZaNil.getDrugi() + ")");

			if (krizaniParZaNil.getPrvi() > krizaniParZaNil.getDrugi())
				System.out.println(tmpPremise.size() + cilj.size() + finalniKrizaniParovi.size() + 1 + ". NIL ("
						+ krizaniParZaNil.getDrugi() + ", " + krizaniParZaNil.getPrvi() + ")");

			System.out.println("===============");

			System.out.print("[CONCLUSION]: ");
			for (int i = 0; i < cilj.size(); i++) {
				System.out.print(cilj.get(i));
				if (i < cilj.size() - 1)
					System.out.print(" v ");
			}

			System.out.println(" is true");
		}

		if (UNKNOWN) {

			ispisiPremiseICilj();

			// vrati pobrisane funkcije
			System.out.println("===============");
			System.out.print("[CONCLUSION]: ");
			for (int i = 0; i < cilj.size(); i++) {
				System.out.print(cilj.get(i));
				if (i < cilj.size() - 1)
					System.out.print(" v ");
			}

			System.out.println(" is unknown");
		}

	}

	private void poravnajKrizane() {
		Map<Integer, Integer> exNewOvdje = new TreeMap<>();
		int i = sizeNaPoc + cilj.size() + 1;
		for (KrizaniParovi kp : relevantniKrizani) {
			if (kp.getBrojNoveKlauzule() != i) {
				exNewOvdje.put(kp.getBrojNoveKlauzule(), i);
				kp.setBrojNoveKlauzule(i);
			}
			i++;
		}
		for (KrizaniParovi kp : relevantniKrizani) {
			if (exNewOvdje.containsKey(kp.getPrvi())) {
				kp.setPrvi(exNewOvdje.get(kp.getPrvi()));
			}
			if (exNewOvdje.containsKey(kp.getDrugi())) {
				kp.setDrugi(exNewOvdje.get(kp.getDrugi()));
			}
		}

	}

	private void posloziKrizaneParove() {

		for (Map.Entry<Integer, List<String>> ent : newSos.entrySet()) {
			for (KrizaniParovi kp : krizaniParovi) {
				if (ent.getKey() == kp.getBrojNoveKlauzule()) {
					relevantniKrizani.add(kp);
				}

			}
		}

	}

	private void poravnajSosBrojeveNakonBrisanja() {
		Map<Integer, List<String>> mapa = new TreeMap<>();
		int i = unija.size() + 1;
		exNewKrizani = new TreeMap<>();
		for (Map.Entry<Integer, List<String>> ent : newSos.entrySet()) {
			mapa.put(i, ent.getValue());
			if (ent.getKey() != i)
				exNewKrizani.put(ent.getKey(), i);
			i++;
		}
		newSos = mapa;

		for (KrizaniParovi kp : relevantniKrizani) {
			if (exNewKrizani.get(kp.getBrojNoveKlauzule()) != null) {
				kp.setBrojNoveKlauzule(exNewKrizani.get(kp.getBrojNoveKlauzule()));
			}
		}
	}

	private void pripremiZaSljdPretragu() {
		premise = unija;
		sos = newSos;
		newSos = new TreeMap<>();
	}

	private void provjeriJelImaNevaznihUNewSosMedusobno() {
		Map<Integer, List<String>> mapa = Map.copyOf(newSos);

		for (Map.Entry<Integer, List<String>> ent : mapa.entrySet()) {

			for (Map.Entry<Integer, List<String>> ent1 : mapa.entrySet()) {
				if (ent.getValue().equals(ent1.getValue())) {
					continue;
				}
				if (ent.getValue().containsAll(ent1.getValue())) {
					newSos.remove(ent.getKey());
				}
			}

		}

	}

	private void popuniNewSos() {

		// za svaku klauzulu iz sos-a
		for (Map.Entry<Integer, List<String>> klauzula : sos.entrySet()) {

			// trazimo po uniji
			for (Map.Entry<Integer, List<String>> literal : unija.entrySet()) {

				// ako sa samim sobom usporedujemo
				if (literal.getKey() == klauzula.getKey())
					continue;

				// provjera za krizane parove
				boolean krizano = provjeriJelParVecKrizan(klauzula.getKey(), literal.getKey());

				if (krizano)
					continue;
				boolean nadeno = false;
				// provjeri jel se ima kj krizat
				List<String> klauzulaNeg = negirajKlauzulu(klauzula.getValue());

				if (literal.getValue().containsAll(klauzulaNeg)) {
					nadeno = true;
					// u novu klauzulu stavljamo stvari za kratit
					List<String> novaKlauzula = new LinkedList<>();
					for (String str : literal.getValue()) {
						if (!(klauzula.getValue().contains(str))) {
							novaKlauzula.add(str);
						}
					}
					// sad kratimo
					for (String str : klauzula.getValue()) {
						novaKlauzula.remove(negirajJedanClan(str));
					}

					if (novaKlauzula.size() == 0) {
						krizaniParZaNil = new KrizaniParovi(literal.getKey(), klauzula.getKey());
						krizaniParZaNil.setPrvaLista(literal.getValue());
						krizaniParZaNil.setDrugaLista(klauzula.getValue());
						krizaniParZaNil.setBrojNoveKlauzule(unija.size() + newSos.size());
						NIL = true;
						break;
					}
					if (newSos.containsValue(novaKlauzula))
						continue;
					newSos.put(unija.size() + 1 + newSos.size(), novaKlauzula);

					// puni krizano
					KrizaniParovi kp = new KrizaniParovi(literal.getKey(), klauzula.getKey());
					kp.setRezultatKrizanja(novaKlauzula);
					kp.setBrojNoveKlauzule(unija.size() + newSos.size());
					kp.setPrvaLista(literal.getValue());
					kp.setDrugaLista(klauzula.getValue());
					krizaniParovi.add(kp);
				}

				// mozda se moze krizat u opbnutom smjeru
				List<String> literalNeg = negirajKlauzulu(literal.getValue());

				if (klauzula.getValue().containsAll(literalNeg)) {
					nadeno = true;
					// u novu klauzulu stavljamo stvari za kratit
					List<String> noviLiteral = new LinkedList<>();
					for (String str : klauzula.getValue()) {
						if (!(literal.getValue().contains(str))) {
							noviLiteral.add(str);
						}
					}
					// sad kratimo
					for (String str : literal.getValue()) {
						noviLiteral.remove(negirajJedanClan(str));
					}

					if (noviLiteral.size() == 0) {
						krizaniParZaNil = new KrizaniParovi(klauzula.getKey(), literal.getKey());
						krizaniParZaNil.setPrvaLista(klauzula.getValue());
						krizaniParZaNil.setDrugaLista(literal.getValue());
						krizaniParZaNil.setBrojNoveKlauzule(unija.size() + newSos.size());
						NIL = true;
						break;
					}
					if (newSos.containsValue(noviLiteral))
						continue;
					newSos.put(unija.size() + 1 + newSos.size(), noviLiteral);

					// puni krizano
					KrizaniParovi kp = new KrizaniParovi(klauzula.getKey(), literal.getKey());
					kp.setRezultatKrizanja(noviLiteral);
					kp.setBrojNoveKlauzule(unija.size() + newSos.size());
					kp.setPrvaLista(klauzula.getValue());
					kp.setDrugaLista(literal.getValue());
					krizaniParovi.add(kp);
				}

				if (!nadeno) {

					boolean imaNestoSlicno = false;

					List<String> slicno = new LinkedList<>();
					for (String str : literal.getValue()) {
						for (String str2 : klauzulaNeg) {

							if (str.equals(str2)) {
								imaNestoSlicno = true;
								slicno.add(str);
							}

						}
					}

					Set<String> set = new TreeSet<>();
					if (imaNestoSlicno) {
						for (String str : literal.getValue()) {
							set.add(str);
						}
						for (String str : klauzula.getValue()) {

							set.add(str);

						}
					} else {
						continue;
					}

					List<String> noviLiteral = new LinkedList<>();
					for (String str : set) {
						noviLiteral.add(str);
					}

					// sad kratimo-to di fali
					for (String str : slicno) {
						if (noviLiteral.contains(str)) {
							noviLiteral.remove(str);
							noviLiteral.remove(negirajJedanClan(str));
						}
					}

					if (newSos.containsValue(noviLiteral))
						continue;
					newSos.put(unija.size() + 1 + newSos.size(), noviLiteral);
					KrizaniParovi kp = new KrizaniParovi(klauzula.getKey(), literal.getKey());
					kp.setRezultatKrizanja(noviLiteral);
					kp.setBrojNoveKlauzule(unija.size() + newSos.size());
					kp.setPrvaLista(klauzula.getValue());
					kp.setDrugaLista(literal.getValue());
					krizaniParovi.add(kp);
				}
			}

		}

	}

	private List<String> negirajKlauzulu(List<String> klauzula) {
		List<String> novaLista = new LinkedList<>();
		for (String str : klauzula) {
			novaLista.add(negirajJedanClan(str));
		}
		return novaLista;
	}

	private String negirajJedanClan(String str) {
		if (str.contains("~"))
			return str.substring(1);
		return NEGATIVAN + str;
	}

	private boolean provjeriJelParVecKrizan(Integer key, Integer key2) {
		return krizaniParovi.contains(new KrizaniParovi(key, key2))
				|| krizaniParovi.contains(new KrizaniParovi(key2, key));

	}

	private boolean provjeriTautologiju(List<String> lista) {
		boolean tautologija = false;
		for (String str1 : lista) {
			int i = 0;
			for (String str2 : lista) {
				if (str1.equals(str2))
					i++;
			}
			if (i > 1)
				tautologija = true;
		}
		return tautologija;
	}

	private void napraviUniju() {
		unija = new TreeMap<>();
		for (Map.Entry<Integer, List<String>> ent : premise.entrySet()) {
			unija.put(ent.getKey(), ent.getValue());
		}
		for (Map.Entry<Integer, List<String>> ent : sos.entrySet()) {
			unija.put(ent.getKey(), ent.getValue());
		}
	}

	private void loadingData(String sourceFile) {
		try (BufferedReader reader = new BufferedReader(new FileReader(sourceFile, Charset.forName("UTF-8")))) {
			String line = reader.readLine();
			int i = 1;
			while (line != null) {
				if (line.startsWith("#")) {
					line = reader.readLine();
					continue;
				}
				line = line.toLowerCase();
				String[] splits = line.split(" v ");

				if (splits.length > MAX_SIZE_KLAUZULE)
					MAX_SIZE_KLAUZULE = splits.length;

				List<String> lista = new LinkedList<>();
				for (String split : splits) {
					lista.add(split.trim());
				}

				boolean tautologija = provjeriTautologiju(lista);

				// jer tautologiju ne želimo dodati
				if (tautologija) {
					System.out.println("tautologija" + lista);
					continue;
				}

				premise.put(i, lista);
				line = reader.readLine();
				i++;
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		cilj = premise.get(premise.size());

		negiraj(premise.remove(premise.size()));

		makniNevazneOsimCilja();

		poravnajBrojeve();
		prebaciCiljUSos();

		pocPremise = Map.copyOf(premise);
	}

	private void poravnajBrojeve() {
		Map<Integer, List<String>> mapa = new TreeMap<>();
		int i = 1;
		for (Map.Entry<Integer, List<String>> ent : premise.entrySet()) {
			mapa.put(i, ent.getValue());
			i++;
		}
		premise = mapa;
	}

	private void prebaciCiljUSos() {
		int size = premise.size();
		for (int i = premise.size() - cilj.size() + 1; i <= size; i++) {
			sos.put(i, premise.remove(i));
			brojeviCilja.add(i);
		}

	}

	private void negiraj(List<String> lista) {

		if (lista.size() == 1) {
			List<String> novaLista = new LinkedList<>();
			String literal = lista.get(0);
			if (literal.contains(NEGATIVAN)) {
				literal = literal.substring(1);
			} else {
				literal = NEGATIVAN + literal;
			}
			novaLista.add(literal);
			premise.put(premise.size() + 1, novaLista);
		} else {
			int i = premise.size() + 1;
			for (String literal : lista) {
				List<String> novaLista = new LinkedList<>();
				if (literal.contains(NEGATIVAN)) {
					literal = literal.substring(1);
				} else {
					literal = NEGATIVAN + literal;
				}
				novaLista.add(literal);
				premise.put(i, novaLista);
				i++;
			}
		}

	}

	private void makniNevazneOsimCilja() {

		// micemo nevazne premise ako ih ima;
		Map<Integer, List<String>> mapa = Map.copyOf(premise);

		for (Map.Entry<Integer, List<String>> ent : mapa.entrySet()) {

			for (Map.Entry<Integer, List<String>> ent1 : mapa.entrySet()) {
				if (ent.getValue().equals(ent1.getValue())) {
					continue;
				}
				if (ent.getValue().containsAll(ent1.getValue())) {
					premise.remove(ent.getKey());
				}
			}
		}
	}

	class KrizaniParovi implements Comparable<KrizaniParovi> {
		Integer prvi;
		Integer drugi;
		Integer brojNoveKlauzule;

		List<String> prvaLista;
		List<String> drugaLista;

		boolean koristen = true;

		public boolean isKoristen() {
			return koristen;
		}

		public void setKoristen(boolean koristen) {
			this.koristen = koristen;
		}

		public List<String> getPrvaLista() {
			return prvaLista;
		}

		public void setPrvaLista(List<String> prvaLista) {
			this.prvaLista = prvaLista;
		}

		public List<String> getDrugaLista() {
			return drugaLista;
		}

		public void setDrugaLista(List<String> drugaLista) {
			this.drugaLista = drugaLista;
		}

		public Integer getBrojNoveKlauzule() {
			return brojNoveKlauzule;
		}

		public void setBrojNoveKlauzule(Integer brojNoveKlauzule) {
			this.brojNoveKlauzule = brojNoveKlauzule;
		}

		List<String> rezultatKrizanja;

		public KrizaniParovi(Integer prvi, Integer drugi) {
			this.prvi = prvi;
			this.drugi = drugi;

		}

		public List<String> getRezultatKrizanja() {
			return rezultatKrizanja;
		}

		public void setRezultatKrizanja(List<String> rezultatKrizanja) {
			this.rezultatKrizanja = rezultatKrizanja;
		}

		public Integer getPrvi() {
			return prvi;
		}

		public void setPrvi(Integer prvi) {
			this.prvi = prvi;
		}

		public Integer getDrugi() {
			return drugi;
		}

		public void setDrugi(Integer drugi) {
			this.drugi = drugi;
		}

		@Override
		public String toString() {
			return "Krizani par je: " + rezultatKrizanja + " (" + brojNoveKlauzule + ") " + " nastao iz " + prvaLista
					+ " (" + prvi + ") " + " i " + drugaLista + " (" + drugi + ")";
		}

		@Override
		public int hashCode() {
			return brojNoveKlauzule.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof KrizaniParovi))
				return false;

			KrizaniParovi kp = (KrizaniParovi) obj;
			return (this.getPrvi() == kp.getPrvi() && this.getDrugi() == kp.getDrugi())
					|| (this.getPrvi() == kp.getDrugi() && this.getDrugi() == kp.getPrvi());
		}

		public int getKey() {
			for (Map.Entry<Integer, List<String>> ent : unija.entrySet()) {
				if (ent.getValue().equals(rezultatKrizanja)) {
					return ent.getKey();
				}
			}
			return 0;
		}

		@Override
		public int compareTo(KrizaniParovi o) {
			return this.brojNoveKlauzule.compareTo(o.brojNoveKlauzule);
		}
	}
}
