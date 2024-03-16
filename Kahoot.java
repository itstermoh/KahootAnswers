import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import org.json.JSONArray;
import org.json.JSONObject;

public class Kahoot {

    public static void main(String[] args) {
        String uuid = "a1311b79-30f2-4815-86f4-7f75c23de52c";
        try {
            JSONObject responseData = getQuizData(uuid);
            JSONArray entities = responseData.getJSONArray("entities");
            for (int i = 0; i < entities.length(); i++) {
                JSONObject question = entities.getJSONObject(i);
                System.out.println(question.toString(2));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static JSONObject getQuizData(String uuid) throws IOException {
        URL url = new URL("https://kahoot.it/rest/kahoots/" + uuid);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        StringBuilder response = new StringBuilder();
        try (BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                response.append(inputLine);
            }
        }

        JSONObject data = new JSONObject(response.toString());
        if (!data.has("questions")) {
            throw new IOException("Questions not iterable blyatt");
        }

        JSONArray questionList = new JSONArray();
        JSONArray questions = data.getJSONArray("questions");
        for (int i = 0; i < questions.length(); i++) {
            JSONObject slide = questions.getJSONObject(i);
            if (!slide.getString("type").equals("quiz")) {
                continue;
            }

            JSONArray choices = slide.getJSONArray("choices");
            JSONObject correctChoice = null;
            for (int j = 0; j < choices.length(); j++) {
                JSONObject choice = choices.getJSONObject(j);
                if (choice.getBoolean("correct")) {
                    correctChoice = choice;
                    break;
                }
            }

            if (correctChoice != null) {
                boolean isTrueFalse = true;
                for (int j = 0; j < choices.length(); j++) {
                    JSONObject choice = choices.getJSONObject(j);
                    if (!choice.getString("answer").equals("True") && !choice.getString("answer").equals("False")) {
                        isTrueFalse = false;
                        break;
                    }
                }

                String[] cols = (choices.length() == 2 && isTrueFalse) ? new String[]{"red", "blue"} : new String[]{"red", "blue", "yellow", "green"};

                JSONObject questionObj = new JSONObject();
                questionObj.put("number", i + 1);
                questionObj.put("type", "Quiz");
                questionObj.put("title", slide.getString("question").replaceAll("'", "\\\\'") + "',");
                questionObj.put("correctAnswer", correctChoice.getString("answer").replaceAll("'", "\\\\'") + "',");
                questionObj.put("buttonColor", cols[correctChoice.getBoolean("correct") ? 0 : 1]);
                questionObj.put("buttonIndex", correctChoice.getBoolean("correct") ? 1 : 0);

                questionList.put(questionObj);
            }
        }

        JSONObject result = new JSONObject();
        result.put("entities", questionList);
        return result;
    }
}
