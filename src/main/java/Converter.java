import java.io.*;

import com.github.javafaker.Faker;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class Converter {
    private static final String USER_AGENT = "Mozilla/5.0";

    public static void Convert() {
        JSONArray arrayBook = new JSONArray();
        try {
            List<String> allLines = Files.readAllLines(Paths.get("../Materiale/amazon-meta.txt"));
            for (String line : allLines) {
                if (line.indexOf("Id:") != -1) {
                    JSONObject book = new JSONObject();
                    System.out.println(line);
                    int index = allLines.indexOf(line);
                    if (line.equals("Id:   20000")) {
                        break;
                    }

                    // BAD OBJECT
                    String exitString = allLines.get(index + 2);
                    if (exitString.equals("  discontinued product")) {
                        System.out.println("stringa di errore" + " " + exitString);
                        continue;
                    }

                    // GROUP
                    String group = allLines.get(index + 3);
                    String[] groupSplit = group.split("group: ");
                    if (groupSplit[1].equals("Book")) {
                        // book.put("group", groupSplit[1]);
                    } else {
                        // NON SONO UN LIBRO QUINDI SKIPPO L'OGGETTO
                        System.out.println("trovato un non libro");
                        continue;
                    }

                    // ASIN
                    String asin = allLines.get(index + 1);
                    String[] asinSplit = asin.split("ASIN: ");
                    book.put("asin", asinSplit[1]);

                    // ISBN
                    book.put("isbn", "");

                    // LANGUAGE CODE
                    book.put("language_code", "");

                    // AVAERAGE RATING
                    book.put("average_rating", "");

                    // NUM PAGES
                    book.put("num_pages", "");

                    // PUB YEAR
                    book.put("publication_year", "");

                    // PUB MONTH
                    book.put("publication_month", "");

                    // PUB DAY
                    book.put("publication_day", "");

                    // RATING COUNT
                    book.put("rating_count", "");

                    // DESCRIPTION
                    book.put("description", "");

                    String author_imgURL = Scrape(asinSplit[1]);

                    // AUTHOR
                    String a[] = author_imgURL.split("___");
                    JSONArray authors = new JSONArray();
                    JSONObject newAutore = new JSONObject();
                    newAutore.put("author_name", (a[0]));
                    newAutore.put("author_role", "");
                    newAutore.put("author_id", "");
                    authors.add(newAutore);
                    book.put("authors", authors);

                    // IMAGE URL
                    book.put("image_url", a[1]);

                    // TITLE
                    String title = allLines.get(index + 2);
                    String[] titleSplit = title.split("title: ");
                    book.put("title", titleSplit[1]);

                    // CATEGORIES
                    String categories = allLines.get(index + 6);
                    String[] categoriesSplit = categories.split("categories: ");
                    int cat = Integer.parseInt(categoriesSplit[1]);
//                    book.put("count_categories", cat);
                    JSONArray categoriesList = new JSONArray();
                    if (cat > 0) {
                        //AGGIUNGO TUTTE LE CATEGORIE IN UN ARRAY
                        for (int i = 1; i <= cat; i++) {
                            String catelem = allLines.get(index + 6 + i);
                            if (catelem.contains("Children")) {
                                categoriesList.add("children");
                            }
                            if (catelem.contains("Comics")) {
                                categoriesList.add("comics");
                            }
                            if (catelem.contains("Fantasy")) {
                                categoriesList.add("fantasy");
                            }
                            if (catelem.contains("History")) {
                                categoriesList.add("history");
                            }
                            if (catelem.contains("Mystery")) {
                                categoriesList.add("mystery");
                            }
                            if (catelem.contains("Poetry")) {
                                categoriesList.add("poetry");
                            }
                            if (catelem.contains("Romance")) {
                                categoriesList.add("romance");
                            }
                            if (catelem.contains("Young")) {
                                categoriesList.add("young");
                            }
                            if (catelem.contains("Adult")) {
                                categoriesList.add("adult");
                            }
                            if (catelem.contains("Graphic")) {
                                categoriesList.add("graphic");
                            }
                            if (catelem.contains("Paranormal")) {
                                categoriesList.add("paranormal");
                            }
                            if (catelem.contains("Biography")) {
                                categoriesList.add("biography");
                            }
                            if (catelem.contains("Thriller")) {
                                categoriesList.add("thriller");
                            }
                            if (catelem.contains("Crime")) {
                                categoriesList.add("crime");
                            }
                            if (categoriesList.isEmpty()) {
                                String catExtracted = StringUtils.substringBetween(catelem, "|Books[283155]|Subjects[1000]|", "[");
                                categoriesList.add(catExtracted);
                            }
                        }
                        book.put("genres", categoriesList);
                    }

                    // BOOK REVIEWS
                    String reviews = allLines.get(index + 6 + cat + 1);
                    String[] reviewsSplit = reviews.split("reviews: ");
                    String[] app = reviewsSplit[1].split(": ");
                    String[] countReview = app[1].split("  ");
                    int rev = Integer.parseInt(countReview[0]);

//                    book.put("count_reviews", rev);
                    JSONArray reviewsList = new JSONArray();
                    if (rev > 0) {
                        for (int i = 1; i <= rev; i++) {
                            String reviewExtracted = allLines.get(index + 6 + cat + i + 1);
                            if (reviewExtracted.contains("cutomer:")) {
                                JSONObject review = new JSONObject();
                                String data = StringUtils.substringBetween(reviewExtracted, "", "cutomer").replaceAll(" ", "");
                                String cutomer = StringUtils.substringBetween(reviewExtracted, "cutomer:", "rating").replaceAll(" ", "");
                                String rating = StringUtils.substringBetween(reviewExtracted, "rating:", "votes").replaceAll(" ", "");
                                String votes = StringUtils.substringBetween(reviewExtracted, "votes:", "helpful").replaceAll(" ", "");
                                String helpfulSplit[] = reviewExtracted.split("helpful: ");
                                review.put("date_added", data);
                                review.put("date_updated", data);
                                review.put("review_id", cutomer + "_" + asinSplit[1]);
                                review.put("rating", rating);
                                review.put("n_votes", votes);
                                review.put("review_text", "");
                                review.put("user_id", cutomer);
                                review.put("helpful", helpfulSplit[1].replaceAll(" ", ""));
                                reviewsList.add(review);
                            }
                        }
                        book.put("reviews", reviewsList);
                    } else {
                        book.put("reviews", reviewsList);
                    }

                    // ID
                    String[] parts = line.split(":   ");
                    book.put("book_id", parts[1]);

//                    // SIMILAR BOOK ARRAY AND COUNTER
//                    String similar = allLines.get(index + 5);
//                    String[] similarSplit = similar.split("similar: ");
//                    String[] similarSplitElems = similarSplit[1].split("  ");
//                    int similarcount = Integer.parseInt(similarSplitElems[0]);
//                    book.put("count_similar", similarcount);
//                    JSONArray similarList = new JSONArray();
//                    if (similarcount > 0) {
//                        for (int i = 1; i <= similarcount; i++) {
//                            similarList.add(similarSplitElems[i]);
//                        }
//                    }
//                    book.put("similar", similarList);

                    arrayBook.add(book);
                }
            }
            FileWriter file = new FileWriter("../Materiale/BookAmazon.json");
            file.write(arrayBook.toJSONString());
            file.close();
            System.out.println("elementi inseriti nel JSON: " + arrayBook.size());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String Scrape(String asin) throws IOException {
//        asin = "0486287785";
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

//        System.out.println(response); // per debug

        String str = response.toString();
        String result = StringUtils.substringBetween(str, "<span class=\"a-size-base\">by </span>", "</div>");
//        System.out.println("risultato primo parsing \n" + result);

        if (StringUtils.substringBetween(result, ">", "</a>") != null) {
            if (StringUtils.substringBetween(result, ">", "</a>").contains("<")) {
                String app[] = StringUtils.substringBetween(result, ">", "</a>").split("<");
                System.out.println(app[0]);
                String image_url = StringUtils.substringBetween(str, "<img class=\"s-image\" src=\"", "\"");
//                System.out.println("stamp url..."+image_url);
                return app[0] + "___" + image_url;
            } else {
                String image_url = StringUtils.substringBetween(str, "<img class=\"s-image\" src=\"", "\"");
                System.out.println(StringUtils.substringBetween(result, ">", "</a>"));
//                System.out.println("stamp url..."+image_url);
                return StringUtils.substringBetween(result, ">", "</a>") + "___" + image_url;
            }
        } else if (StringUtils.substringBetween(result, ">", "</span>") != null) {
            if (StringUtils.substringBetween(result, ">", "</span>").contains("<")) {
                String app[] = StringUtils.substringBetween(result, ">", "</span>").split("<");
                System.out.println(app[0]);
                String image_url = StringUtils.substringBetween(str, "<img class=\"s-image\" src=\"", "\"");
//                System.out.println("stamp url..."+image_url);
                return app[0] + "___" + image_url;
            } else {
                String image_url = StringUtils.substringBetween(str, "<img class=\"s-image\" src=\"", "\"");
//                System.out.println("stamp url..."+image_url);
                System.out.println(StringUtils.substringBetween(result, ">", "</span>"));
                return StringUtils.substringBetween(result, ">", "</span>") + "___" + image_url;
            }
        } else {
//            // ci va un nome generato randomicamente
            Faker faker = new Faker();
            String name = faker.name().fullName();
            String firstName = faker.name().firstName();
            String lastName = faker.name().lastName();
            String image_url = StringUtils.substringBetween(str, "<img class=\"s-image\" src=\"", "\"");
//            System.out.println("stamp url..."+image_url);
//            return name + lastName + "___" + image_url;
            return "null___null";
        }

    }

    public static void main(String[] args) throws IOException, ParseException {
//        Convert();
//        Scrape("");

        String file = "../Materiale/BookAmazon.json";
        String json = new String(Files.readAllBytes(Paths.get(file)));
        String isbn = json.replaceAll("ISBN", "isbn");
        String asin = isbn.replaceAll("ASIN", "asin");
        String last = asin.replaceAll("rating_count","ratings_count");
       // String last = last2.replaceAll("&","and");

        BufferedWriter writer = new BufferedWriter(new FileWriter("../Materiale/test.txt"));
        writer.write(asin);
        writer.close();

        JSONParser parser1 = new JSONParser();
        JSONArray jsonObj = (JSONArray) parser1.parse(last);
        FileWriter fileJSON = new FileWriter("../Materiale/BookAmazon2.json");
        fileJSON.write(jsonObj.toJSONString());
        fileJSON.close();

        JSONParser parser = new JSONParser();
        try {
            Object obj = parser.parse(new FileReader("../Materiale/BookAmazon2.json"));
            JSONArray jsonArray = (JSONArray) obj;
            for (int i = 0; i < jsonArray.size(); i++) {
                JSONObject o = (JSONObject) jsonArray.get(i);
                ArrayList<String> autori = (ArrayList<String>) o.get("author");
                JSONArray newAutori = new JSONArray();
                for (int j = 0; j < autori.size(); j++) {
                    JSONObject newAutore = new JSONObject();
                    newAutore.put("author_name", autori.get(j));
                    newAutore.put("author_role", "");
                    String concat = autori.get(j);
                    String id = UUID.nameUUIDFromBytes(concat.getBytes()).toString();
                    newAutore.put("author_id",id);
                    newAutori.add(newAutore);
                }
                o.remove("author");
                o.put("authors", newAutori);

                // insert user_id
                JSONArray reviewList = (JSONArray) o.get("reviews");
                for (int k = 0; k < reviewList.size(); k++) {
                    JSONObject review = (JSONObject) reviewList.get(k);
                    String review_id = (String) review.get("review_id");
                    String[] cutomer = review_id.split("_");
                    review.put("user_id",cutomer[0]);
                }
            }
            FileWriter fileJSON2 = new FileWriter("../Materiale/BookAmazon2.json");
            fileJSON2.write(jsonArray.toJSONString());
            fileJSON2.close();
//            for (int i = 0; i < jsonArray.size(); i++) {
//                System.out.println(jsonArray.get(i));
//            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}