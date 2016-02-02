package creditcard;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AccountAnalysis {

	//Main-Methode zum Testen.
	public static void main(String[] args) {
		AccountAnalysis analysis = new AccountAnalysis();
		
		Map<Integer, Integer> result = null;
		
		try {
			result = analysis.mapFilterReduceBankAccount("konto.csv");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Ergebnis: " + result);
	}

	public Map<Integer, Integer> mapFilterReduceBankAccount(String filepath) throws IOException {
		
		// create stream from file
		Stream<String> lines = Files.lines(Paths.get(filepath));
		
		// capture time
		long start = System.currentTimeMillis();

		// result map
		Map<Integer, Integer> result = null;
		
		//TODO hier bitte implementieren
		Stream<Transaction> transactions;
		transactions = lines.map(l -> new Transaction(l.split(";")[0], l.split(";")[1]));
		transactions = transactions.filter(t -> t.getKey() >= 1505 && t.getKey() <= 1510 && t.getValue() >= 100);
		result = transactions.collect(Collectors.groupingBy(Transaction::getKey, Collectors.collectingAndThen(Collectors.averagingInt(Transaction::getValue),s -> s.intValue())));
		// close stream
		lines.close();
		
		// print time
		System.out.println("Zeit in ms.: " + (System.currentTimeMillis() - start));
		
		return result;
	}
}