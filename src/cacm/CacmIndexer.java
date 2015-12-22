package cacm;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

//Indexing
public class CacmIndexer {

	// FIELD NAMES:
	// internal ID (.I)
	public static final String ID = "docid";
	// title of the entry (.T)
	public static final String TITLE = "title";
	// abstract/content (.W)
	public static final String CONTENT = "content";

	// analyzer
	public Analyzer analyzer = null;

	// index writer
	public IndexWriter writer;

	// determines which analyzer should be used
	public static final boolean USE_STANDARD_ANALYZER = true;

	// constructor
	public CacmIndexer(String indexDir, Analyzer analyzer) throws IOException {
		Directory dir = FSDirectory.open(new File(indexDir).toPath());

		this.analyzer = analyzer;

		IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

		writer = new IndexWriter(dir, iwc); // 3 modified
	}

	// main method for testing
	public static void main(String[] args) throws Exception {

		String indexDir = null;// 1
		String dataDir = "data/cacm.all"; // 2

		Analyzer analyzer = null;

		if (USE_STANDARD_ANALYZER) {
			indexDir = "idx_cacm_std";
			analyzer = new StandardAnalyzer();
		} else {// use MyStemAnalyzer
			indexDir = "idx_cacm_my";
			analyzer = new MyStemAnalyzer();
		}

		long start = System.currentTimeMillis();
		CacmIndexer indexer = new CacmIndexer(indexDir, analyzer);
		int numIndexed;
		try {
			numIndexed = indexer.index(dataDir);
		} finally {
			indexer.close();
		}
		long end = System.currentTimeMillis();

		System.out.println("Indexing " + numIndexed + " files took " + (end - start) + " milliseconds");
	}

	// as before, nothing new :-)
	public int index(String dataDir) throws Exception {

		File f = new File(dataDir);
		indexFile(f);

		return writer.numDocs(); // 5
	}

	// as before, nothing new :-)
	public void close() throws IOException {
		writer.close(); // 4
	}

	// Do the indexing! (see exercise 4.1)
	public void indexFile(File file) throws Exception {

		System.out.println("Indexing " + file.getCanonicalPath());

		// TODO: hier bitte implementieren! Datei einlesen und einzelne
		// Dokumente mit den entsprechenden Feldinformationen extrahieren.

		if (!file.canRead() || !file.isFile())
			System.exit(0);

		BufferedReader in = null;
		try {
			in = new BufferedReader(new FileReader(file));
			String zeile = null;
			Document doc = null;
			String currSection = null;
			StringBuilder sb = new StringBuilder();
			while ((zeile = in.readLine()) != null) {
				// Hier kommen jetzt die Zeilen aus dem Dokument an.
				// Wenn das erste Zeichen ein Punkt ist, dann ist das ein
				// Anzeichen für
				// einen neuen Abschnitt im Dokument.
				if (zeile.startsWith(".")) {
					if (doc != null && currSection != null && currSection != "notImportant")
						writeToIndex(doc, currSection, sb);
					sb = new StringBuilder();
					switch (zeile.substring(0, 2)) {
					case ".I":
						// .I leitet immer ein neues Dokument ein. Deswegen muss
						// das
						// alte Dokument im Index gespeichert werden um im
						// Anschluss
						// ein neues zu erstellen
						if (doc != null) {
							writer.addDocument(doc);
						}
						sb.append(zeile.substring(3));
						doc = new Document();
						currSection = ID;
						break;
					case ".T":
						currSection = TITLE;
						break;
					case ".W":
						currSection = CONTENT;
						break;
					default:
						currSection = "notImportant";
						break;
					}
				} else {
					if (currSection != "notImportant")
						sb.append(zeile);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (in != null)
				try {
					in.close();
				} catch (IOException e) {
				}
		}

	}

	private void writeToIndex(Document doc, String currSection, StringBuilder sb) throws FileNotFoundException {
		// bisher gelesene Informationen speichern
		if (currSection == CONTENT || currSection == TITLE) {
			doc.add(new TextField(currSection, sb.toString(), Field.Store.YES));
		} else {
			doc.add(new StringField(currSection, sb.toString(), Field.Store.YES));
		}
	}
}
