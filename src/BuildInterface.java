import javax.servlet.*;
import javax.servlet.http.*;
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
                " <link rel=\"manifest\" href=\"favicon/manifest.json\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"57x57\" href=\"favicon/apple-icon-57x57.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"60x60\" href=\"favicon/apple-icon-60x60.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"72x72\" href=\"favicon/apple-icon-72x72.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"76x76\" href=\"favicon/apple-icon-76x76.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"114x114\" href=\"favicon/apple-icon-114x114.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"120x120\" href=\"favicon/apple-icon-120x120.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"144x144\" href=\"favicon/apple-icon-144x144.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"152x152\" href=\"favicon/apple-icon-152x152.png\">\n" +
                "  <link rel=\"apple-touch-icon\" sizes=\"180x180\" href=\"favicon/apple-icon-180x180.png\">\n" +
                "  <link rel=\"icon\" type=\"image/png\" sizes=\"192x192\" href=\"favicon/android-icon-192x192.png\">\n" +
                "  <link rel=\"icon\" type=\"image/png\" sizes=\"32x32\" href=\"favicon/favicon-32x32.png\">\n" +
                "  <link rel=\"icon\" type=\"image/png\" sizes=\"96x96\" href=\"favicon/favicon-96x96.png\">\n" +
                "  <link rel=\"icon\" type=\"image/png\" sizes=\"16x16\" href=\"favicon/favicon-16x16.png\">\n" +
                "  <meta name=\"msapplication-TileColor\" content=\"#ffffff\">\n" +
                "  <meta name=\"msapplication-TileImage\" content=\"ms-icon-144x144.png\">\n" +
                "  <meta name=\"theme-color\" content=\"#ffffff\">" +
                "  <title>Dawrha Search</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"suggestions\" style=\"display: none;\">\n");

        //add the suggestions
        for (int i = 0; i < Suggestions.size(); i++) {
            page.append("<span>" + Suggestions.get(i) + "</span>");
        }

        page.append("  </div>\n" +
                "\n" +
                "  <form action=\"SearchInterface\" method=\"GET\" autocomplete=\"off\" class=\"container w-75\" style=\"margin: 150px auto 100px;\">\n" +
                "    <div>\n" +
                "        <img\n" +
                "          src=\"Logo2.png\"\n" +
                "          style=\"margin: auto; display: block\"\n" +
                "          width=\"75px\"\n" +
                "          alt=\"Logo\"\n" +
                "        />\n" +
                "        <h2 class=\"dawarha-logo\">Dawrha</h2>\n" +
                "      </div>" +
                "\n" +
                "    <div class=\"autocomplete\">\n" +
                "      <input name=\"query\" id=\"myInput\" autocomplete=\"off\" type=\"text\" placeholder=\"Search\" style=\"font-size: 18px;\">\n" +
                "      <span class=\"mic-btn\"><i class=\"fa-solid fa-microphone\"></i></span>\n" +
                "      <button type=\"submit\"><i class=\"fa-solid fa-magnifying-glass\"></i></button>\n" +
                "    </div>\n" +
                "  </form>\n" +
                "  \n" +
                "  <script src=\"js/popper.min.js\"></script>\n" +
                "  <script src=\"js/bootstrap.min.js\"></script>\n" +
                "  <script src=\"js/main.js\"></script>\n" +
                "</body>\n" +
                "</html>");

        resp.getWriter().println(page);
    }
}
