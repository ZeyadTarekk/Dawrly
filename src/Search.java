
import SearchPackage.Pair;
import SearchPackage.Pair3;
import SearchPackage.PhraseSearcher;
import SearchPackage.QueryProcessor;
import SearchPackage.Ranker;

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
        SearchPackage.Search ser = new SearchPackage.Search();
        long start1 = System.currentTimeMillis();
        finalResults = ser.searchQuery("JavaScript");
        long end1 = System.currentTimeMillis();
        System.out.println("Elapsed Time in milli seconds: " + (end1 - start1));

        System.out.println("================================");
        System.out.println(finalResults.size());
        int index = 0;
        for (String page : finalResults.keySet()) {
            System.out.println(page + " " + finalResults.get(page));
            index++;
            if (index >= 10)
                break;
        }
        System.out.println("================================");
    }
}

