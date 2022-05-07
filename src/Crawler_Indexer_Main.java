

public class Crawler_Indexer_Main {
    public static void main(String[] args) throws InterruptedException {
        (new Crawler()).Crawl();
        (new Indexer()).startIndexing();
    }
}
