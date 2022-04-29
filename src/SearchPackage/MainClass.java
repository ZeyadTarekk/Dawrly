package SearchPackage;

import javax.swing.text.Document;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class MainClass {
    public static void main(String[] args) throws InterruptedException {
        // System.out.println("Zeyad");
        Indexer indexer = new Indexer();
        indexer.startIndexing();

        List<String> phraseSearch = new ArrayList<>();
        // HOW TO USE QueryProcessor
        QueryProcessor qp = new QueryProcessor();
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer,Integer>>> result = qp.processQuery("Hello", phraseSearch);
//
//        Ranker r1 = new Ranker();
        HashMap<String, Pair3<Double, String, String, String>> p = new HashMap<>();
        p.put("input", new Pair3<>(1.2, "Ahmed", "Ahmed", "Ahmed"));
        String[] intArray = new String[]{"input"};
        List<String> docsWithAllOccuerence = new ArrayList<>(Arrays.asList(intArray));
        System.out.println(docsWithAllOccuerence);
        PhraseSearcher ps = new PhraseSearcher(result, p, docsWithAllOccuerence, phraseSearch);
        System.out.println(ps);
    }
}
