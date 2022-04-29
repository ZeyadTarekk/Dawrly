package SearchPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Search {

    private List<String> query;

    private PhraseSearcher phraseSearcher;
    private QueryProcessor qp;
    private HashMap<String, Pair3<Double, String, String, String>> finalResults;

    private List<String> goldenPages;

    private Ranker rank;
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> result;

    public HashMap<String, Pair3<Double, String, String, String>> searchQuery(String queryToSearch) {
        goldenPages = new ArrayList<>();
        query = new ArrayList<>();
        qp = new QueryProcessor();
        rank = new Ranker();
        result = qp.processQuery(queryToSearch, query);
        finalResults = rank.generateRelevance(result, goldenPages);
        phraseSearcher = new PhraseSearcher(result, finalResults, goldenPages, query);
        finalResults = phraseSearcher.getOrderedDocs();

        return finalResults;
    }

    public static void main(String[] args) {
        HashMap<String, Pair3<Double, String, String, String>> finalResults;
        Search ser = new Search();
        long start1 = System.currentTimeMillis();
        finalResults = ser.searchQuery("JavaScript");
        long end1 = System.currentTimeMillis();
        System.out.println("Elapsed Time in milli seconds: " + (end1 - start1));

        System.out.println("================================");
        System.out.println(finalResults);
        System.out.println("================================");
    }
}

