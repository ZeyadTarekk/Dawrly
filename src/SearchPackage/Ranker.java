package SearchPackage;

//import okhttp3.OkHttpClient;
//import okhttp3.Request;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
/*
HOW TO USE?
1- generateRelevance()
    Parameters: takes HashMap<String, HashMap<String, Pair<Integer, Integer, Double>>>
                returned from the database and converted and  List<String> pages
    returns HashMap of <String, Pair3<Double, String, String, String> sorted in a descending way with respect to
            scores of each page and fills the pages list with the golden Websites
            <String, Pair3<Double, String, String, String>
            page            Score Paragraph Title  Word
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

    //    private HashMap<String, Double> pagesFinalScore;
    private HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore;
    //                    page        Score    Paragraph  title
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> resultProcessed;

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

    private String stemTheWord(String word) {
        PorterStemmer stemmer = new PorterStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent(); //

    }

    private List<String> splitQuery(String query, List<String> stemmed) {
        List<String> words = Arrays.asList(query.split(" "));
        for (String word : words)
            stemmed.add(stemTheWord(word));
        return words;
    }

    private void generateIDFS() {
        wordsNormalizedIDFS = new HashMap<>();
        double dummyIDF;
        String dummyWord;
        for (Map.Entry<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> Entry : resultProcessed.entrySet()) {
            dummyWord = Entry.getKey();
            dummyIDF = ((double) pagesNumber / Entry.getValue().size());
            wordsNormalizedIDFS.put(dummyWord, dummyIDF);
        }
    }

    private void generateTFSAndScores() {
        wordsNormalizedTFSScores = new HashMap<>();
        HashMap<String, Pair2<Double, Double>> dummyMap;
        Pair<Integer, Integer, Double, Integer, Integer> dummyPair;
        for (String word : resultProcessed.keySet()) {
            for (String page : resultProcessed.get(word).keySet()) {
                dummyPair = resultProcessed.get(word).get(page);
//                if (resultProcessed.get(word).get(page) != null) {
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
        MongoDB dataBase = new MongoDB();
        dataBase.ConnectWithPagePopularity();
        int scorePopularity;
        double dummyScore;
        Pair3<Double, String, String, String> dummyScorePair;
        Pair2<Double, Double> dummyPair;
        for (String page : wordsNormalizedTFSScores.keySet()) {
            dummyScore = 0;
            for (String word : wordsNormalizedTFSScores.get(page).keySet()) {
                dummyPair = wordsNormalizedTFSScores.get(page).get(word);

                if (dummyPair.score != null)
                    dummyScore = dummyScore + dummyPair.TF * dummyPair.score * wordsNormalizedIDFS.get(word);
                else
                    System.out.println("Score: " + dummyPair.score);
            }
            scorePopularity = dataBase.getPagePopularity(page);
//            System.out.println("Score before Popularity: " + dummyScore + " for page: " + page);
//            System.out.println("Score after Popularity: " + dummyScore * scorePopularity + " for page: " + page);
            pagesFinalScore.put(page, new Pair3<Double, String, String, String>(dummyScore * scorePopularity, "", "", ""));
        }

    }

    private void getParagraphs(HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore, HashMap<String, HashMap<String, Pair2<Double, Double>>> wordsNormalizedTFSScores, String query) {
        String wordToSearch = null;
        String wholeDocument;
        String pageName;
        String titlePage;
        boolean phraseSearchFlag;
        boolean notFoundPhrase = false;
        if (query.contains("\"")) {
            query = query.replaceAll("\"", "");
            phraseSearchFlag = true;
        } else
            phraseSearchFlag = false;
        List<String> wordsInQueryStemmed = new ArrayList<>();
        List<String> wordsInQuery = splitQuery(query.toLowerCase(), wordsInQueryStemmed);
        int indexOfWord;
        List<Integer> actualIndices;
        Pair<Integer, Integer, Double, Integer, Integer> dummyPair;
        for (String page : pagesFinalScore.keySet()) {
            int index = -1;
            try {
                pageName = page;
                pageName = pageName.replace("*", "`{}");
                pageName = pageName.replace("://", "}");
                pageName = pageName.replace("/", "{");
                pageName = pageName.replace("?", "`");
                pageName = pageName + ".html";
//                    System.out.println(pageName);
                File file = new File("bodyFiles//" + pageName);
                BufferedReader br = new BufferedReader(new FileReader(file));
                titlePage = br.readLine();
                wholeDocument = br.readLine().toLowerCase();
            } catch (IOException e) {
                pagesFinalScore.get(page).setTitle("-1");
                e.printStackTrace();
                continue;
            }
            if (phraseSearchFlag)
                if (wholeDocument.contains(query.toLowerCase())) {
                    index = wholeDocument.indexOf(query.toLowerCase());
                    wordToSearch = query;
                    notFoundPhrase = false;
                } else
                    notFoundPhrase = true;

            if (!phraseSearchFlag || notFoundPhrase)
                for (String word : wordsInQuery)
                    if (wholeDocument.contains(word.toLowerCase())) {
                        wordToSearch = word;
                        index = wholeDocument.indexOf(wordToSearch);
                    }
            if (index != -1) {


                int endIndex = index + 100;
                int startIndex = index - 100;
                int newEndIndex;
                if (startIndex > 0) {
                    while (wholeDocument.charAt(startIndex) != ' ' && startIndex != 0)
                        startIndex--;
                    if (wholeDocument.charAt(startIndex) == ' ')
                        startIndex++;
                    newEndIndex = wholeDocument.indexOf(" ", endIndex);

                } else if (startIndex == 0) {
                    newEndIndex = wholeDocument.indexOf(" ", endIndex);

                } else {
                    startIndex = 0;
                    newEndIndex = wholeDocument.indexOf(" ", endIndex);
                }

                if (newEndIndex >= wholeDocument.length() || newEndIndex == -1) {
                    newEndIndex = wholeDocument.length() - 1;
                }

                String paragraph = wholeDocument.substring(startIndex, newEndIndex) + "...";
                paragraph = paragraph.replaceAll("<", "");
                paragraph = paragraph.replaceAll(">", "");
                pagesFinalScore.get(page).setParagraph(paragraph);
                pagesFinalScore.get(page).setWord(wordToSearch);
                pagesFinalScore.get(page).setTitle(titlePage);
            } else {
                pagesFinalScore.get(page).setTitle("-1");
            }


        }
    }

    private HashMap<String, Pair3<Double, String, String, String>> sortHashMap() {
        // Creating a list from elements of HashMap
        List<Map.Entry<String, Pair3<Double, String, String, String>>> list
                = new LinkedList<Map.Entry<String, Pair3<Double, String, String, String>>>(
                pagesFinalScore.entrySet());

        // Sorting the list using Collections.sort() method
        // using Comparator
        Collections.sort(
                list,
                new Comparator<Map.Entry<String, Pair3<Double, String, String, String>>>() {
                    public int compare(
                            Map.Entry<String, Pair3<Double, String, String, String>> object1,
                            Map.Entry<String, Pair3<Double, String, String, String>> object2) {
                        return (object2.getValue().getScore())
                                .compareTo(object1.getValue().getScore());
                    }
                });

        // putting the  data from sorted list back to hashmap
        HashMap<String, Pair3<Double, String, String, String>> result
                = new LinkedHashMap<String, Pair3<Double, String, String, String>>();
        for (Map.Entry<String, Pair3<Double, String, String, String>> me : list) {
            result.put(me.getKey(), me.getValue());
        }

        // returning the sorted HashMap
        return result;
    }

    public HashMap<String, Pair3<Double, String, String, String>> generateRelevance(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> result, List<String> pages, String query) {

//        System.out.println("Printing data structure from database");
//        System.out.println(result);
        getPagesNumber();
        this.resultProcessed = result;
        generateIDFS();
        generateTFSAndScores();
        generateFinalScores();

        for (String page : wordsNormalizedTFSScores.keySet()) {
            if (wordsNormalizedTFSScores.get(page).size() == resultProcessed.size())
                pages.add(page);
        }

        pagesFinalScore = sortHashMap();
        getParagraphs(pagesFinalScore, wordsNormalizedTFSScores, query);
        return pagesFinalScore;
    }

    public List<String> getPhraseSearching(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> result) {
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
//        String testValue;
//        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> resultProcessed = new HashMap<>();
//        HashMap<String, Pair<Integer, Integer, Double, Integer>> inner = new HashMap<>();
//        HashMap<String, Pair<Integer, Integer, Double, Integer>> inner2 = new HashMap<>();
//        inner.put("Hello.com", new Pair<Integer, Integer, Double, Integer>(1, 20, 1.5));
//        inner.put("welc.com", new Pair<Integer, Integer, Double, Integer>(2, 30, 1.5));
//        inner.put("welc2.com", new Pair<Integer, Integer, Double, Integer>(2, 30, 1.5));
//        inner.put("Hello2.com", new Pair<Integer, Integer, Double, Integer>(2, 30, 1.5));
//        inner2.put("Hello.com", new Pair<Integer, Integer, Double, Integer>(1, 25, 1.5));
//        inner2.put("Hello2.com", new Pair<Integer, Integer, Double, Integer>(1, 20, 1.5));
//        inner2.put("Hello3.com", new Pair<Integer, Integer, Double, Integer>(1, 20, 1.5));
//        inner2.put("Hello4.com", new Pair<Integer, Integer, Double, Integer>(1, 20, 1.5));
//        resultProcessed.put("like", inner);
//        resultProcessed.put("like2", inner2);
//        resultProcessed.put("like3", inner2);
//
////        inner.put("first.com", new Pair<Integer, Integer, Double>(53,1837,1.5));
////        inner.put("second.com", new Pair<Integer, Integer, Double>(12,252,1.5));
////        inner2.put("first.com", new Pair<Integer, Integer, Double>(8,1837,1.5));
////        inner2.put("second.com", new Pair<Integer, Integer, Double>(3,252,1.5));
//
////        resultProcessed.put("backgammon",inner);
////        resultProcessed.put("computer",inner2);
//        Pair2<Integer, Integer> test = new Pair2<>(3, 7);
//        System.out.println("Printing pair2");
//        System.out.println(test.first());
//        System.out.println(test.second());
//
//        System.out.println(rank.generateRelevance(resultProcessed));
//        System.out.println("===============================");
//        System.out.println(rank.getPhraseSearching(resultProcessed));
//        System.out.println("===============================");
//        System.out.println(rank.wordsNormalizedIDFS);
//        System.out.println(rank.wordsNormalizedTFSScores);
//        System.out.println(rank.pagesFinalScore);
//        System.out.println("Printing map value for test");
//        testValue = (String) resultProcessed.get("like").keySet().toArray()[0];
//        System.out.println(testValue);
        Ranker rank = new Ranker();
        HashMap<String, Pair3<Double, String, String, String>> finalResult;
        Indexer indexer = new Indexer();
//        try {
//            indexer.startIndexing();
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        List<String> phraseSearch = new ArrayList<>();
        QueryProcessor qp = new QueryProcessor();
        HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> result = qp.processQuery("normalize", phraseSearch);
        System.out.println("============================================");
        System.out.println(result);
        System.out.println("============================================");
        finalResult = rank.generateRelevance(result, phraseSearch, "normalize");
        System.out.println(finalResult);
    }


}
