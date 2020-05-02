package ai.beu;

import ai.beu.models.Profile;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.var;

import java.io.*;

public class Main {

    private static int maxDocCount = 10000;
    private static boolean continueOnErrors = true;
    private static ObjectMapper objectMapper = new ObjectMapper();

    public static void main(String[] args) throws Exception {

        objectMapper.enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT);

        var fileInputStream = new FileInputStream("data/profiles.json");
        var inputStreamReader = new InputStreamReader(fileInputStream);
        var bufferedReader = new BufferedReader(inputStreamReader);
        int docCount = 0;

        var errorOutputFile = new BufferedWriter(new FileWriter("output/errors_" + System.currentTimeMillis() + "_.txt"));

        var docStringBuilder = new StringBuilder();
        String line;
        while ((line = bufferedReader.readLine()) != null && docCount < maxDocCount) {
            if (line.equals("\t\"_id\": {")) {
                var doc = docStringBuilder.substring(0, docStringBuilder.lastIndexOf("{"));
                if (!"".equals(doc.trim())) {
                    process(doc, errorOutputFile);
                    docCount++;
                    if (docCount % 1000 == 0) {
                        System.out.println("Processed " + docCount + " records.");
                    }
                }
                docStringBuilder = new StringBuilder();
                docStringBuilder.append("{\n");
            }
            docStringBuilder.append(line + "\n");
        }

        if (docCount < maxDocCount)
            process(docStringBuilder.toString(), errorOutputFile);

        errorOutputFile.close();
        bufferedReader.close();
    }

    public static void process(String doc, BufferedWriter errorOutputFile) throws Exception {
        try {
            var profile = objectMapper.readValue(doc, Profile.class);

            // do stuff with profile object here

        } catch (Exception e) {
            errorOutputFile.append(doc);
            errorOutputFile.append("\n\n");
            errorOutputFile.append(e.getMessage());
            errorOutputFile.append("\n\n");
            if (!continueOnErrors)
                throw e;
        }
    }
}
