
import org.tartarus.snowball.ext.PorterStemmer;

import java.io.*;
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

    private HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore;
    //                    page        Score    Paragraph  title
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer, Integer>>> resultProcessed;


    //    HashMap<String, HashMap<String, Pair<Integer, Integer,Double>>>
//             Words           page         tf         size   score
    private void getPagesNumber() {
        // creates a file object
        File file = new File("C:\\CMP\\CMP22\\Advanced Programming\\Project\\Search-Engine\\downloads");
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
                dummyMap = new HashMap<>();
                if (wordsNormalizedTFSScores.get(page) == null) {
                    wordsNormalizedTFSScores.put(page, dummyMap);
                    wordsNormalizedTFSScores.get(page).put(word, new Pair2<Double, Double>(((double) dummyPair.TF / dummyPair.size), dummyPair.score));

                } else {
                    wordsNormalizedTFSScores.get(page).put(word, new Pair2<Double, Double>(((double) dummyPair.TF / dummyPair.size), dummyPair.score));
                }
            }
        }
    }

    private void generateFinalScores() {
        pagesFinalScore = new HashMap<>();
        MongoDB dataBase = new MongoDB();
        dataBase.ConnectWithPagePopularity();
        HashMap<String, Integer> popularityScores = dataBase.getPagePopularity();

        Integer scorePopularity;
        double dummyScore;
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
            scorePopularity = popularityScores.get(page);
            if (scorePopularity == null) {
                scorePopularity = 1;
            }
            pagesFinalScore.put(page, new Pair3<Double, String, String, String>(dummyScore * scorePopularity, "", "", ""));
        }

    }

    private void fetchParagraphs(HashMap<String, Pair3<Double, String, String, String>> pagesFinalScore, String query) {
        //clear the query from ""
        if (query.contains("\"")) {
            query = query.replaceAll("\"", "");
        }
        query = query.toLowerCase();

        for (String page : pagesFinalScore.keySet()) {
            String titlePage, wholeDocument;
            try {
                String pageName = page;
                pageName = pageName.replace("*", "`{}");
                pageName = pageName.replace("://", "}");
                pageName = pageName.replace("/", "{");
                pageName = pageName.replace("?", "`");
                pageName = pageName + ".html";
                File file = new File("C:\\CMP\\CMP22\\Advanced Programming\\Project\\Search-Engine\\bodyFiles\\" + pageName);
                BufferedReader br = new BufferedReader(new FileReader(file));
                titlePage = br.readLine();
                wholeDocument = br.readLine().toLowerCase();
            } catch (Exception e) {
                System.out.println("Error while opening the file");
                pagesFinalScore.get(page).setTitle("-1");
                continue;
            }

            String queryList[] = query.split(" ");
            int bestCount = -1, currentCount = 0, index = 0, finalIndex = 0;
            int docLength = wholeDocument.length();
            String subPara, bestPara = null;

            while (true) {
                finalIndex = index + 500;
                currentCount = 0;

                if (index < docLength) {

                    //check if the finalIndex bigger than the document length
                    if (finalIndex >= docLength) {
                        finalIndex = docLength - 1;

                        while (wholeDocument.charAt(index) != ' ' && index < docLength && finalIndex > index + 350)
                            index++;

                        //get the sub-paragraph and check the number of query words in it
                        subPara = wholeDocument.substring(index, finalIndex);
                        for (int i = 0; i < queryList.length; i++) {
                            if (subPara.contains(queryList[i]))
                                currentCount++;
                        }
                        //save the start index of the best paragraph
                        if (currentCount > bestCount) {
                            bestCount = currentCount;
                            bestPara = subPara;
                        }
                        break;

                    } else {

                        while (wholeDocument.charAt(index) != ' ' && index < docLength && finalIndex > index + 350)
                            index++;

                        //get the sub-paragraph and check the number of query words in it
                        subPara = wholeDocument.substring(index, finalIndex);
                        for (int i = 0; i < queryList.length; i++) {
                            if (subPara.contains(queryList[i]))
                                currentCount++;
                        }
                        //save the start index of the best paragraph
                        if (currentCount > bestCount) {
                            bestCount = currentCount;
                            bestPara = subPara;
                        }
                        if (currentCount == queryList.length)
                            break;
                        index = finalIndex + 1;
                    }
                } else break;

            }

            if (bestCount > 0) {
                bestPara = bestPara + "....";
                bestPara = bestPara.replaceAll("<", "").replaceAll(">", "");
                pagesFinalScore.get(page).setParagraph(bestPara);
                pagesFinalScore.get(page).setWord(query);
                pagesFinalScore.get(page).setTitle(titlePage);
            } else
                pagesFinalScore.get(page).setTitle("-1");
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
        fetchParagraphs(pagesFinalScore, query);
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

}
