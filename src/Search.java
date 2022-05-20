
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
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer,Integer,Double>>> result;

    public HashMap<String, Pair3<Double, String, String, String>> searchQuery(String queryToSearch) {
        goldenPages = new ArrayList<>();
        query = new ArrayList<>();
        qp = new QueryProcessor();
        rank = new Ranker();
        result = qp.processQuery(queryToSearch, query);
        finalResults = rank.generateRelevance(result, goldenPages,queryToSearch);
        phraseSearcher = new PhraseSearcher(result, finalResults, goldenPages, query);
        finalResults = phraseSearcher.getOrderedDocs();

        return finalResults;
    }

    public static void main(String[] args) {
        HashMap<String, Pair3<Double, String, String, String>> finalResults;
        Search ser = new Search();
        long start1 = System.currentTimeMillis();
        finalResults = ser.searchQuery("code");
        System.out.println(finalResults);
        long end1 = System.currentTimeMillis();
        System.out.println("Elapsed Time in milli seconds: " + (end1 - start1));

//        for (String page : finalResults.keySet()) {
//            System.out.println("------------------");
//            System.out.println(page);
//            System.out.println(finalResults.get(page).getTitle());
//            System.out.println(finalResults.get(page).getParagraph());
//            System.out.println("------------------");
//        }

        System.out.println("================================");
        System.out.println(finalResults.size());
        System.out.println("================================");
    }
}

