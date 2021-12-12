
import java.io.FileWriter;
import java.io.IOException;
import org.json.simple.JSONObject;
import org.json.simple.JSONArray;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class Converter {
    public static void main(String args[]) {
        JSONArray array = new JSONArray();
        try {
            List<String> allLines = Files.readAllLines(Paths.get("../amazon-meta.txt"));
            for (String line : allLines) {
                if (line.indexOf("Id:") != -1) {
                    System.out.println(line);
                    int index = allLines.indexOf(line);
                    if (line.equals("Id:   200")) {
                        break;
                    }

                    String exitString = allLines.get(index + 2);
                    if (exitString.equals("  discontinued product") ) {
                        System.out.println("stringa di errore"+" "+exitString);
                        continue;
                    }

                    String[] parts = line.split(":   ");
                    JSONObject elem = new JSONObject();
                    elem.put("id", parts[1]);

                    String title = allLines.get(index + 2);
                    String[] titleSplit = title.split("title: ");
                    elem.put("title", titleSplit[1]);

                    String group = allLines.get(index + 3);
                    String[] groupSplit = group.split("group: ");
                    elem.put("group", groupSplit[1]);

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
            FileWriter file = new FileWriter("output.json");
            file.write(array.toJSONString());
            file.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
}