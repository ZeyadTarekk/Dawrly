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

    //Function to connect to the local database
    public void ConnectToDataBase() {
        try {
            MongoClient mongoClient = new MongoClient();
            MongoDatabase db = mongoClient.getDatabase("SearchEngine");
            CrawlerCollection = db.getCollection("crawler");

            //TODO : check if there is _id == 1 if not add it
            System.out.println("Connected to database");
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
}
