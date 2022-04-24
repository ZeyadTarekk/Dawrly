package SearchPackage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainClass {
    public static void main(String[] args) throws InterruptedException {
//        System.out.println("Zeyad");
//        Indexer indexer = new Indexer();
//        indexer.startIndexing();

        List<String> phraseSearch = new ArrayList<>();
        // HOW TO USE QueryProcessor
        QueryProcessor qp = new QueryProcessor();
        HashMap<String, HashMap<String, Pair<Integer, Integer,Double>>> result = qp.processQuery("Mangaa Ingredients", phraseSearch);
        System.out.println(result);
    }
}
