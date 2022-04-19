package SearchPackage;

import java.util.List;

/*
TODO: create an abstract class called ProcessString
      Make QueryProcessor & Indexer extends it
      ProcessString should contains all methods of string processing
*/
public class QueryProcessor extends Indexer {
    // NOTE: Take care of quotes -> search as it is
    List<String> stopWords;


    public static void main(String[] args) {

    }
    // TODO: Determine the output data structure of processQuery method

    // TODO: Provide an interface to receive the query string
    // TODO: Provide an interface to pass the words to the RANKER
    void processQuery(String query) {
        // TODO: Split string into words
        // TODO: Convert to lowercase
        // TODO: Read stop words
        // TODO: Remove stop words
        // TODO: Stem words
        // TODO: Get documents containing words from database
        // TODO: [OPTIONAL] convert JSON into HASHMAP
    }


}
