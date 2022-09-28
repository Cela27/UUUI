package ui;

public class Solution {
	public static void main(String[] args) {
		// --alg: kratica za algoritam za pretraˇzivanje (vrijednosti: bfs, ucs, ili
		// astar),
		// --ss: putanja do opisnika prostora stanja,
		// --h: putanja do opisnika heuristike,
		// --check-optimistic: zastavica koja signalizira da se za danu heuristiku ˇzeli
		// provjeriti optimistiˇcnost,
		// --check-consistent: zastavica koja signalizira da se za danu heuristiku ˇzeli
		// provjeriti konsistentnost.

		String alg = "";
		String putanjaSS = "";
		String putanjaH = "";

		boolean checkOpt = false;
		if (args[0].equals("--ss")) {
			putanjaSS = args[1];

			if (args[2].equals("--alg")) {
				alg = args[3];
				if (alg.equals("astar"))
					putanjaH = args[5];
			}
			else if(args[2].equals("--h")) {
				putanjaH=args[3];
				if(args[4].equals("--check-optimistic"))
					checkOpt=true;
			}

		} else {
			System.err.println("Pogreska");
		}
		// uvjeti za pozive rjesavanja
		if (alg.equals("bfs")) {
			BFS bfs = new BFS(putanjaSS);
			bfs.ispis(true);
		} else if (alg.equals("ucs")) {
			UCS ucs = new UCS(putanjaSS);
		} else if (alg.equals("astar")) {
			ASTAR astar = new ASTAR(putanjaSS, putanjaH);
		}
		// alg nije zadan
		else {
			if (checkOpt) {
				H_OPT hopt = new H_OPT(putanjaSS, putanjaH);
			} else {
				H_CON hcon = new H_CON(putanjaSS, putanjaH);
			}
		}
	}
}
