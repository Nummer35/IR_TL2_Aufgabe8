package eval;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

class TRECFileFilter implements FilenameFilter {
	public boolean accept(File dir, String name) {
		String lowercaseName = name.toLowerCase();
		if (lowercaseName.endsWith(".trec")) {
			return true;
		} else {
			return false;
		}
	}
}

public class Eval {

	/**
	 * Main-Methode zum Berechnen der Evaluationskennzahl.
	 * 
	 * @param args
	 *            nicht noetig.
	 */
	public static void main(String[] args) {

		// key: Anfrage; value: Menge der relevanten DokumentIDs
		Map<String, Set<String>> groundtruth = readGroundtruth("data/cacm-new-63.qrel");

		String[] filenames = new File("./logs").list(new TRECFileFilter());
		for (String filename : filenames) {
			// System.out.println(filename);
			double map = evaluateMAP("./logs/" + filename, groundtruth);
			System.out.println(filename + "\t MAP=" + Math.round(map * 1000.0) / 1000.0);
		}
	}

	/*
	 * Berechnet MAP.
	 */
	protected static double evaluateMAP(String filename, Map<String, Set<String>> groundtruth) {

		// TODO Hier bitte implementieren und korrekten Wert zurueckgeben.
		List<String> lines = null;
		try {
			lines = Files.readAllLines(new File(filename).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// Zuerst die 63 Rankings aus der einzelnen Datei lesen und speichern
		List<List<String>> rankings = new ArrayList<List<String>>();
		List<String> ranking = new ArrayList<String>();
		for (String line : lines) {
			if (Integer.valueOf(line.split(" ")[3]) == 1) {
				if (!ranking.isEmpty()) {
					rankings.add(ranking);
				}
				ranking = new ArrayList<String>();
				ranking.add(line);
			} else {
				ranking.add(line);
			}
		}
		rankings.add(ranking); // um query 63 noch hinzu zu f�gen.
		
		double map = 0;
		int relevantRankings = 0;
		for (List<String> r : rankings) {
			int queryId = getQueryId(r);
			// jetzt die Liste durchlaufen und relevante Dokumente suchen
			if (!groundtruth.containsKey(queryId + "")){
				continue;}
			// Ein Ranking ist dann relevant wenn es auch im groundtruth vorkommt.
			relevantRankings++;
			Set<String> gtDocs = groundtruth.get(queryId + "");
			// Diese Liste beinhaltet alle bisher gesehenen relevanten
			// DokumentIds. Das wird ben�tigt um dublikate zu eleminieren.
			List<String> seenDocs = new ArrayList<String>();
			double precision = 0;
			double numberOfRelevantDocsFound = 0;
			double ap = 0;
			// jetzt werden alle 1000 dokument in einem Ranking durchgegangen
			// und der Reihenfolge nach den relevanten durchsucht.
			for (String doc : r) {
				String docId = doc.split(" ")[2]; 
				double docRang = Integer.valueOf(doc.split(" ")[3]);
				if (gtDocs.contains(docId) && !seenDocs.contains(docId)) {
					// Die Precision nur berechnen wenn die docId das erste
					// mal gesehen wird und diese relevant ist.
					numberOfRelevantDocsFound++;
					precision = numberOfRelevantDocsFound / docRang;
					ap = ap + precision;
					seenDocs.add(docId);
				}
			}
			map = map + ap;
		}
		return map / relevantRankings;
	}

	private static int getQueryId(List<String> r) {
		if (r.isEmpty())
			return 0;
		return Integer.valueOf(r.get(0).split(" ")[0]);
	}

	/*
	 * Liefert die Relevanzurteile: key: Anfrage ID; value: Menge mit relevanten
	 * Dokument IDs.
	 */
	private static Map<String, Set<String>> readGroundtruth(String filename) {
		Map<String, Set<String>> groundtruth = new HashMap<String, Set<String>>();
		String oldQueryId = "1";
		Set<String> relIdsPerQuery = new HashSet<String>();

		List<String> lines = null;
		try {
			lines = Files.readAllLines(new File(filename).toPath());
		} catch (IOException e) {
			e.printStackTrace();
		}

		for (String line : lines) {
			String[] parts = line.split(" ");
			if (parts.length != 4)
				throw new RuntimeException("Fehler" + parts.length);

			String queryId = parts[0];
			String docId = parts[2];

			if (!queryId.equals(oldQueryId)) {
				groundtruth.put(oldQueryId, relIdsPerQuery);
				relIdsPerQuery = new HashSet<String>();
				oldQueryId = queryId;
			}
			relIdsPerQuery.add(docId);
		}
		groundtruth.put(oldQueryId, relIdsPerQuery);
		return groundtruth;
	}

}
