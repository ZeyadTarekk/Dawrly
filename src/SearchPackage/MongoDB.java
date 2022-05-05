package SearchPackage;

import com.mongodb.*;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.*;
import static com.mongodb.client.model.Filters.*;
import static com.mongodb.client.model.Updates.*;

/*
MongoDB commands

https://www.mongodb.com/developer/quickstart/java-setup-crud-operations/?utm_campaign=javainsertingdocuments&utm_source=facebook&utm_medium=organic_social
*/

public class MongoDB {
    private MongoCollection<org.bson.Document> CrawlerCollection;
    private MongoCollection<org.bson.Document> QueryCollection;
    private MongoCollection<org.bson.Document> pagePopularityCollection;

    //Function to connect to the local database
    public void ConnectToDataBase() {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            CrawlerCollection = db.getCollection("crawler");
            QueryCollection = db.getCollection("query");
            pagePopularityCollection = db.getCollection("pagePopularity");
            System.out.println("Connected to database");

            //check for the state document
            Document stateDocument = CrawlerCollection.find(eq("_id", 1)).first();
            if (stateDocument == null) {
                Document State = new Document("_id", 1);
                State.append("state", "Finished").append("NofVisited", 0);
                CrawlerCollection.insertOne(State);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //function to change the state of the crawler (Finished, Interrupted)
    public void ChangeState(String state) {
        Bson filter = eq("_id", 1);
        Bson updateOperation = set("state", state);
        CrawlerCollection.updateOne(filter, updateOperation);
        if (state == "Finished") {
            // Clear all the unvisited links and remove them from the database
            filter = eq("_id", 1);
            updateOperation = set("NofVisited", 0.0);
            CrawlerCollection.updateOne(filter, updateOperation);
            filter = ne("_id", 1.0);
            CrawlerCollection.deleteMany(filter);
        }
    }

    //Function to get the state of the crawler
    public String CheckState() {
        Document stateDocument = CrawlerCollection.find(eq("_id", 1)).first();
        Object state = stateDocument.get("state");
        return state.toString();
    }

    //Function to get all the links to visit next and the visited pages previously
    public float GetSavedLinks(List<String> PagesToVisit, Set<String> VisitedPages) {
        List<Document> LinksList = CrawlerCollection.find().into(new ArrayList<>());
        for (Document link : LinksList) {
            if (!link.get("_id").toString().equals("1.0"))
            {
                if (link.get("link") != null) {
                    PagesToVisit.add(link.get("link").toString());
                } else if (link.get("saved") != null) {
                    VisitedPages.add(link.get("saved").toString());
                }
            }
        }
        return GetNofVisitedPages();
    }

    public float GetNofVisitedPages() {
        Document stateDocument = CrawlerCollection.find(eq("_id", 1)).first();
        Object state = stateDocument.get("NofVisited");
        return Float.parseFloat(state.toString());
    }

    public void UpdatePagesToVisit(String URL) {
        Bson filter = eq("link", URL);
        CrawlerCollection.deleteOne(filter);
    }

    public void UpdatePagesToVisit(List<String> Links) {
        for (int i = 0; i < Links.size(); i++) {
            Document link = new Document("link", Links.get(i));
            CrawlerCollection.insertOne(link);
        }
    }

    public void UpdatePagesVisited(String SpecialWord) {
        Document saved = new Document("saved", SpecialWord);
        CrawlerCollection.insertOne(saved);
    }

    public void UpdateNofVisitedPages(int N) {
        Bson filter = eq("_id", 1);
        Bson updateOperation = set("NofVisited", N);
        CrawlerCollection.updateOne(filter, updateOperation);
    }

    //pagePopularity section
    public void ConnectWithPagePopularity() {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            pagePopularityCollection = db.getCollection("pagePopularity");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void insertPagePopularity(HashMap<String, Integer> PagesPopularity) {
        for (String page : PagesPopularity.keySet()) {
            //check if that link saved before
            Document scoreDoc = pagePopularityCollection.find(eq("link", page)).first();
            if (scoreDoc == null) {
                Document pageDoc = new Document("link", page);
                pageDoc.append("score", PagesPopularity.get(page));
                pagePopularityCollection.insertOne(pageDoc);
            } else {
                int score = Integer.parseInt(scoreDoc.get("score").toString());
                score = score + PagesPopularity.get(page);
                Bson filter = eq("link", page);
                Bson updateOperation = set("score", score);
                pagePopularityCollection.updateOne(filter, updateOperation);
            }
        }
    }

    public int getPagePopularity(String page) {
        Document scoreDoc = pagePopularityCollection.find(eq("link", page)).first();
        if (scoreDoc == null)
            return 1;
        else {
            Object score = scoreDoc.get("score");
            return Integer.parseInt(score.toString());
        }
    }

    //query collection section
    public void ConnectWithQuery() {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            QueryCollection = db.getCollection("query");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addQuery(String query) {
        //check if that query was used before
        Document queryDocument = QueryCollection.find(eq("key", query)).first();
        if (queryDocument == null) {
            Document Query = new Document("key", query);
            QueryCollection.insertOne(Query);
        }
    }

    public List<String> getSuggestions() {
        List<Document> LinksList = QueryCollection.find().into(new ArrayList<>());
        List<String> list = new ArrayList<>();
        for (Document link : LinksList) {
            if (link.get("key") != null) {
                list.add(link.get("key").toString());
            }
        }
        return list;
    }
}
