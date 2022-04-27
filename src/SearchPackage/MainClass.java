package SearchPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainClass {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println("Zeyad");
        Indexer indexer = new Indexer();
        indexer.startIndexing();
//
        List<String> phraseSearch = new ArrayList<>();
//        // HOW TO USE QueryProcessor
        QueryProcessor qp = new QueryProcessor();
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> result = qp.processQuery("Web Site", phraseSearch);
        System.out.println(result);
        List<String> docsWithAllOccuerence = new ArrayList<>();
        docsWithAllOccuerence.add("input.txt");
//        PhraseSearcher ps = new PhraseSearcher(result, docsWithAllOccuerence, phraseSearch);
//        List<String> goldenDocs = ps.getGoldenDocuments();
//        System.out.println("Query:");
//        System.out.println(phraseSearch);
//        System.out.println("Golden documents: ");
//        System.out.println(goldenDocs);
        //        System.out.println(result);
    }
}
