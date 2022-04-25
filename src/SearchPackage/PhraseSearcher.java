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
            int[] wordIndices = new int[query.size()];
            for (int i = 0; i < query.size(); i++) {
                int index = invertedIndex.get(query.get(i)).get(document).index;
                wordIndices[i] = index;
            }
            boolean isGolden = true;
            for (int j = 1; j < wordIndices.length; j++) {
                if (wordIndices[j] - wordIndices[j - 1] != 1) {
                    isGolden = false;
                    break;
                }
            }
            if (isGolden)
                goldenDocuments.add(document);
        }
    }

    // TODO: Implement a function that make golden docs at the top of data structure (Built by the ranker)

}
