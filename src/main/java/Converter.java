
import java.io.*;

import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.apache.commons.lang3.StringUtils;

import java.net.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.zip.GZIPInputStream;

public class Converter {

    private static final String USER_AGENT = "Mozilla/5.0";

    public static void Convert() {
        JSONArray array = new JSONArray();
        try {
            List<String> allLines = Files.readAllLines(Paths.get("../Materiale/amazon-meta.txt"));
            for (String line : allLines) {
                if (line.indexOf("Id:") != -1) {
                    JSONObject elem = new JSONObject();
                    System.out.println(line);
                    int index = allLines.indexOf(line);
                    if (line.equals("Id:   100")) {
                        break;
                    }

                    String exitString = allLines.get(index + 2);
                    if (exitString.equals("  discontinued product")) {
                        System.out.println("stringa di errore" + " " + exitString);
                        continue;
                    }

                    String group = allLines.get(index + 3);
                    String[] groupSplit = group.split("group: ");
                    if (groupSplit[1].equals("Book")) {
                        elem.put("group", groupSplit[1]);

                        String asin = allLines.get(index + 1);
                        String[] asinSplit = asin.split("ASIN: ");
                        elem.put("ASIN", asinSplit[1]);

                        String author = Scrape(asinSplit[1]);
                        elem.put("author", author);
                    } else {
                        System.out.println("trovato un non libro");
                        continue;
                    }

                    String[] parts = line.split(":   ");
                    elem.put("id", parts[1]);

                    String title = allLines.get(index + 2);
                    String[] titleSplit = title.split("title: ");
                    elem.put("title", titleSplit[1]);

                    String similar = allLines.get(index + 5);
                    String[] similarSplit = similar.split("similar: ");
                    String[] similarSplitElems = similarSplit[1].split("  ");
                    int similarcount = Integer.parseInt(similarSplitElems[0]);
                    elem.put("count_similar", similarcount);
                    JSONArray similarList = new JSONArray();
                    if (similarcount > 0) {
                        for (int i = 1; i <= similarcount; i++) {
                            similarList.add(similarSplitElems[i]);
                        }
                    }
                    elem.put("similar", similarList);

                    String categories = allLines.get(index + 6);
                    String[] categoriesSplit = categories.split("categories: ");
                    int cat = Integer.parseInt(categoriesSplit[1]);
                    elem.put("count_categories", cat);
                    JSONArray categoriesList = new JSONArray();
                    if (cat > 0) {
                        for (int i = 1; i <= cat; i++) {
                            String catelem = allLines.get(index + 6 + i);
                            categoriesList.add(catelem);
                        }
                        elem.put("categories", categoriesList);
                    }

                    String reviews = allLines.get(index + 6 + cat + 1);
                    String[] reviewsSplit = reviews.split("reviews: ");
                    String[] app = reviewsSplit[1].split(": ");
                    String[] countReview = app[1].split("  ");
                    int rev = Integer.parseInt(countReview[0]);
                    elem.put("count_reviews", rev);
                    JSONArray reviewsList = new JSONArray();
                    if (rev > 0) {
                        for (int i = 1; i <= rev; i++) {
                            String revlem = allLines.get(index + 6 + cat + i + 1);
                            reviewsList.add(revlem);
                        }
                        elem.put("reviews", reviewsList);
                    }

                    array.add(elem);
                }
            }
            FileWriter file = new FileWriter("../Materiale/datasetAmazon.json");
            file.write(array.toJSONString());
            file.close();
            System.out.println("elementi inseriti nel JSON: " + array.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String Scrape(String asin) throws IOException {
        URL obj = new URL("https://www.amazon.in/s?k=" + asin + "&ref=hp_aps_search");
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Encoding", "gzip");
        int responseCode = con.getResponseCode();
        // System.out.println("GET Response Code :: " + con.getResponseCode() + "...." + con.getInputStream());
        Reader reader = null;
        if ("gzip".equals(con.getContentEncoding())) {
            reader = new InputStreamReader(new GZIPInputStream(con.getInputStream()));
        } else {
            reader = new InputStreamReader(con.getInputStream());
        }
        StringBuffer response = new StringBuffer();
        while (true) {
            int ch = reader.read();
            if (ch == -1) {
                break;
            }
            response.append((char) ch);
        }

        //System.out.println(response);

        String str = response.toString();
        String result = StringUtils.substringBetween(str, "<span class=\"a-size-base\">by </span>", "</div>");
        System.out.println("risultato primo parsing \n" + result);

        if (StringUtils.substringBetween(result, ">", "</a>") != null) {
            if (StringUtils.substringBetween(result, ">", "</a>").contains("<")) {
                String app[] = StringUtils.substringBetween(result, ">", "</a>").split("<");
                System.out.println(app[0]);
                return app[0];
            } else {
                System.out.println(StringUtils.substringBetween(result, ">", "</a>"));
                return StringUtils.substringBetween(result, ">", "</a>");
            }
        } else if (StringUtils.substringBetween(result, ">", "</span>") != null) {
            if (StringUtils.substringBetween(result, ">", "</span>").contains("<")) {
                String app[] = StringUtils.substringBetween(result, ">", "</span>").split("<");
                System.out.println(app[0]);
                return app[0];
            } else {
                System.out.println(StringUtils.substringBetween(result, ">", "</span>"));
                return StringUtils.substringBetween(result, ">", "</span>");
            }
        }

        return "";
    }

    public static void main(String[] args) throws IOException {
        Convert();
    }


}