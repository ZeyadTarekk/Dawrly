package SearchPackage;

import java.util.*;

public class PhraseSearcher {
    private HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> invertedIndex;
    private List<String> docsWithAllOccurrence;
    private List<String> query;

    private List<String> goldenDocuments;

    public List<String> getGoldenDocuments() {
        return goldenDocuments;
    }

    public static void main(String[] args) {
        Integer[] a = new Integer[5];
        for (int i = 0; i < 5; i++) a[i] = i;
        addToArray(a, 10);
        System.out.println(Arrays.toString(a));
    }

    public PhraseSearcher(HashMap<String, HashMap<String, Pair<Integer, Integer, Double, Integer>>> invertedIndex, List<String> docsWithAllOccurrence, List<String> query) {
        this.invertedIndex = invertedIndex;
        this.docsWithAllOccurrence = docsWithAllOccurrence;
        this.query = query;
        goldenDocuments = new ArrayList<>();
        buildAllOccurrenceDocs();
    }

    // TODO: implement a function that returns a list of documents in which the words appeared in same order
    private void buildAllOccurrenceDocs() {
        for (String document : docsWithAllOccurrence) {
            Integer[][] wordsIndices = new Integer[query.size()][];
            for (int i = 0; i < query.size(); i++) {
                List<Integer> index = new ArrayList<>(invertedIndex.get(query.get(i)).get(document).index);
                wordsIndices[i] = new Integer[index.size()];
                index.toArray(wordsIndices[i]);
            }
            for (int i = 0; i < query.size(); i++) {
                addToArray(wordsIndices[i], query.size() - i - 1);
            }
            System.out.println(Arrays.deepToString(wordsIndices));
            List<Integer> resultList = new ArrayList<>(List.of(wordsIndices[0]));
            for (int i = 0; i < query.size(); i++) {
                resultList.retainAll(List.of(wordsIndices[i]));
            }
            System.out.println(resultList);
            if (!resultList.isEmpty())
                goldenDocuments.add(document);
        }
    }

    private static void addToArray(Integer[] list, int value) {
        for (int i = 0; i < list.length; i++) {
            list[i] += value;
        }
    }
    // TODO: Implement a function that make golden docs at the top of data structure (Built by the ranker)

}
