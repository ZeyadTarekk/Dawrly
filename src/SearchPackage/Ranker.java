package SearchPackage;

import java.io.File;
import java.util.*;
/*
HOW TO USE?
1- generateRelevance()
    Parameters: takes HashMap<String, HashMap<String, Pair<Integer, Integer, Double>>>
                returned from the database and converted
    returns HashMap of <Pages,Scores> sorted in a descending way with respect to
            scores of each page
2- getPhraseSearching()
    Parameters: takes HashMap<String, HashMap<String, Pair<Integer, Integer, Double>>>
                returned from the database and converted
    return list of Strings of the websites containing all the words in the query
 */

public class Ranker {
    private Integer pagesNumber;
    private HashMap<String, Double> wordsNormalizedIDFS;
    private HashMap<String, HashMap<String, Pair2<Double, Double>>> wordsNormalizedTFSScores;
    //              page              word         TF     Score

    private HashMap<String, Double> pagesFinalScore;

    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double,Integer>>> resultProcessed;

    //    public Ranker(){
//
//    }
//    HashMap<String, HashMap<String, Pair<Integer, Integer,Double>>>
//             Words           page         tf         size   score
    private void getPagesNumber() {
        // creates a file object
        File file = new File("downloads");
        // returns an array of all files
        String[] fileNamesList = file.list();
        if (fileNamesList != null)
            pagesNumber = fileNamesList.length;
        else
            pagesNumber = 0;

    }

    private void generateIDFS() {
        wordsNormalizedIDFS = new HashMap<>();
        double dummyIDF;
        String dummyWord;
        for (Map.Entry<String, HashMap<String, Pair<Integer, Integer, Double,Integer>>> Entry : resultProcessed.entrySet()) {
            dummyWord = Entry.getKey();
            dummyIDF = ((double) pagesNumber / Entry.getValue().size());
            wordsNormalizedIDFS.put(dummyWord, dummyIDF);
        }
    }

    private void generateTFSAndScores() {
        wordsNormalizedTFSScores = new HashMap<>();
        HashMap<String, Pair2<Double, Double>> dummyMap;
        Pair<Integer, Integer, Double,Integer> dummyPair;
        for (String word : resultProcessed.keySet()) {
            for (String page : resultProcessed.get(word).keySet()) {
                dummyPair = resultProcessed.get(word).get(page);
//                if(resultProcessed.get(word).get(page)!=null){
                dummyMap = new HashMap<>();
                if (wordsNormalizedTFSScores.get(page) == null) {
                    wordsNormalizedTFSScores.put(page, dummyMap);
                    wordsNormalizedTFSScores.get(page).put(word, new Pair2<Double, Double>(((double) dummyPair.TF / dummyPair.size), dummyPair.score));

                } else {
                    wordsNormalizedTFSScores.get(page).put(word, new Pair2<Double, Double>(((double) dummyPair.TF / dummyPair.size), dummyPair.score));
                }
//                }
            }
        }
    }

    private void generateFinalScores() {
        pagesFinalScore = new HashMap<>();
        double dummyScore;
        Pair2<Double, Double> dummyPair;
        for (String page : wordsNormalizedTFSScores.keySet()) {
            dummyScore = 0;
            for (String word : wordsNormalizedTFSScores.get(page).keySet()) {
                dummyPair = wordsNormalizedTFSScores.get(page).get(word);
                dummyScore = dummyScore + dummyPair.TF * dummyPair.score * wordsNormalizedIDFS.get(word);
            }
            pagesFinalScore.put(page, dummyScore);
        }

    }

    private HashMap<String, Double> sortHashMap() {
        // Creating a list from elements of HashMap
        List<Map.Entry<String, Double>> list
                = new LinkedList<Map.Entry<String, Double>>(
                pagesFinalScore.entrySet());

        // Sorting the list using Collections.sort() method
        // using Comparator
        Collections.sort(
                list,
                new Comparator<Map.Entry<String, Double>>() {
                    public int compare(
                            Map.Entry<String, Double> object1,
                            Map.Entry<String, Double> object2) {
                        return (object2.getValue())
                                .compareTo(object1.getValue());
                    }
                });

        // putting the  data from sorted list back to hashmap
        HashMap<String, Double> result
                = new LinkedHashMap<String, Double>();
        for (Map.Entry<String, Double> me : list) {
            result.put(me.getKey(), me.getValue());
        }

        // returning the sorted HashMap
        return result;
    }

    public HashMap<String, Double> generateRelevance(HashMap<String, HashMap<String, Pair<Integer, Integer, Double,Integer>>> result) {
        getPagesNumber();
        this.resultProcessed = result;
        generateIDFS();
        generateTFSAndScores();
        generateFinalScores();
        pagesFinalScore = sortHashMap();
        return pagesFinalScore;
    }

    public List<String> getPhraseSearching(HashMap<String, HashMap<String, Pair<Integer, Integer, Double,Integer>>> result) {
        List<String> pages = new ArrayList<>();
        getPagesNumber();
        this.resultProcessed = result;
        generateTFSAndScores();

        for (String page : wordsNormalizedTFSScores.keySet()) {
            if (wordsNormalizedTFSScores.get(page).size() == resultProcessed.size())
                pages.add(page);
        }

        return pages;
    }

    public static void main(String[] args) {
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double,Integer>>> resultProcessed = new HashMap<>();
        HashMap<String, Pair<Integer, Integer, Double,Integer>> inner = new HashMap<>();
        HashMap<String, Pair<Integer, Integer, Double,Integer>> inner2 = new HashMap<>();
        inner.put("Hello.com", new Pair<Integer, Integer, Double,Integer>(1, 20, 1.5));
        inner.put("welc.com", new Pair<Integer, Integer, Double,Integer>(2, 30, 1.5));
        inner.put("welc2.com", new Pair<Integer, Integer, Double,Integer>(2, 30, 1.5));
        inner.put("Hello2.com", new Pair<Integer, Integer, Double,Integer>(2, 30, 1.5));
        inner2.put("Hello.com", new Pair<Integer, Integer, Double,Integer>(1, 25, 1.5));
        inner2.put("Hello2.com", new Pair<Integer, Integer, Double,Integer>(1, 20, 1.5));
        inner2.put("Hello3.com", new Pair<Integer, Integer, Double,Integer>(1, 20, 1.5));
        inner2.put("Hello4.com", new Pair<Integer, Integer, Double,Integer>(1, 20, 1.5));
        resultProcessed.put("like", inner);
        resultProcessed.put("like2", inner2);
        resultProcessed.put("like3", inner2);

//        inner.put("first.com", new Pair<Integer, Integer, Double>(53,1837,1.5));
//        inner.put("second.com", new Pair<Integer, Integer, Double>(12,252,1.5));
//        inner2.put("first.com", new Pair<Integer, Integer, Double>(8,1837,1.5));
//        inner2.put("second.com", new Pair<Integer, Integer, Double>(3,252,1.5));

//        resultProcessed.put("backgammon",inner);
//        resultProcessed.put("computer",inner2);

        Ranker rank = new Ranker();
        System.out.println("===============================");
        System.out.println(rank.getPhraseSearching(resultProcessed));
        System.out.println("===============================");
        System.out.println(rank.generateRelevance(resultProcessed));
        System.out.println(rank.wordsNormalizedIDFS);
        System.out.println(rank.wordsNormalizedTFSScores);
        System.out.println(rank.pagesFinalScore);
    }


}
