import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Search {

    private List<String> query;

    private PhraseSearcher phraseSearcher;
    private QueryProcessor qp;
    private HashMap<String, Pair3<Double, String, String, String>> finalResults;

    private List<String> goldenPages;

    private Ranker rank;
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer, Double>>> result;

    public HashMap<String, Pair3<Double, String, String, String>> searchQuery(String queryToSearch) {
        goldenPages = new ArrayList<>();
        query = new ArrayList<>();
        qp = new QueryProcessor();
        rank = new Ranker();
        result = qp.processQuery(queryToSearch, query);
        finalResults = rank.generateRelevance(result, goldenPages, queryToSearch);
        phraseSearcher = new PhraseSearcher(result, finalResults, goldenPages, query);
        finalResults = phraseSearcher.getOrderedDocs();

        return finalResults;
    }

    public static void main(String[] args) {
        HashMap<String, Pair3<Double, String, String, String>> finalResults;
        Search ser = new Search();
        long start1 = System.currentTimeMillis();

        System.out.print("Enter the query you want to search for: ");
        Scanner scanner = new Scanner(System.in);
        String searchQuery = scanner.nextLine();
        System.out.println();
        finalResults = ser.searchQuery(searchQuery);
        long end1 = System.currentTimeMillis();
        System.out.println("=======================================================");
        System.out.printf("About %d results (%d seconds)\n", finalResults.size(), (end1 - start1));
        System.out.println("=======================================================");
        System.out.printf("Search results for (%s): ", searchQuery);

        int index = 0;
        for (String page : finalResults.keySet()) {
            System.out.println(page + " " + finalResults.get(page));
            index++;
            if (index >= 10)
                break;
        }
        System.out.println("=======================================================");
    }
}
