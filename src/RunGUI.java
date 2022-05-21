import org.apache.commons.io.FileUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class RunGUI {
    public static void main(String[] args) throws InterruptedException {
        copyBuildClasses();
        Thread.sleep(100);
        runTomCat();
        Thread.sleep(100);
        openLocalHost();
    }

    public static void copyBuildClasses() {
        File source = new File("out\\production\\Search-Engine");
        File dest = new File("apache-tomcat-9.0.62\\webapps\\SearchEngine\\WEB-INF\\classes");
        try {
            FileUtils.copyDirectory(source, dest);
        } catch (IOException e) {
            e.printStackTrace();
        }
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
            Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start chrome http://localhost:8080/SearchEngine"});
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
