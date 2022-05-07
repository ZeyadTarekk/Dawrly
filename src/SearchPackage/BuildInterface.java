package SearchPackage;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class BuildInterface extends HttpServlet {
    private MongoDB database;

    public void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        //get the suggestions from the database
        database = new MongoDB();
        database.ConnectWithQuery();
        List<String> Suggestions = database.getSuggestions();

        resp.setContentType("text/html");
        StringBuilder page = new StringBuilder();

        page.append("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta http-equiv=\"X-UA-Compatible\" content=\"IE=edge\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <link rel=\"stylesheet\" href=\"css/bootstrap.min.css\">\n" +
                "  <link rel=\"stylesheet\" href=\"css/all.min.css\">\n" +
                "  <link rel=\"stylesheet\" href=\"css/main.css\">\n" +
                "  <title>Dawrha Search</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"suggestions\" style=\"display: none;\">\n");

        //add the suggestions
        for (int i = 0; i < Suggestions.size(); i++) {
            page.append("<span>" + Suggestions.get(i) + "</span>");
        }

        page.append("</div>\n" +
                "  <form action=\"SearchInterface\" method=\"GET\" autocomplete=\"off\" class=\"container w-75\" style=\"margin: 100px auto 100px;\">\n" +
                "    <h2 class=\"dawarha-logo\">Dawrha</h2>\n" +
                "\n" +
                "    <div class=\"autocomplete\">\n" +
                "      <input name=\"query\" id=\"myInput\" autocomplete=\"off\" type=\"text\" placeholder=\"Search\" style=\"font-size: 18px;\">\n" +
                "      <button type=\"submit\"><i class=\"fa-solid fa-magnifying-glass\"></i></button>\n" +
                "    </div>\n" +
                "  </form>\n" +
                "\n" +
                "\n" +
                "  <script src=\"js/popper.min.js\"></script>\n" +
                "  <script src=\"js/bootstrap.min.js\"></script>\n" +
                "  <script src=\"js/main.js\"></script>\n" +
                "</body>\n" +
                "</html>");

        resp.getWriter().println(page);
    }
}
