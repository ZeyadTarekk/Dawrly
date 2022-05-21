import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RunGUI {
    public static void main(String[] args) {
        runTomCat();
        openLocalHost();
    }

    public static void runTomCat() {
        try {
            Process process = Runtime.getRuntime().exec(
                    "cmd /c start startup.bat",
                    null,
                    new File("apache-tomcat-9.0.62\\bin\\"));
        } catch (IOException e) {
            System.out.println("Cannot run tomcat");
        }
    }

    public static void openLocalHost() {
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome http://localhost:8080/search_engine"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
