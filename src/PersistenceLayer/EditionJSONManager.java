package PersistenceLayer;

import BusinessLayer.Entities.Edition;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditionJSONManager implements EditionsFileManager {
    private static final String filename = "files/Editions.json";
    private final Gson gson;
    private final List<Edition> editions;

    public EditionJSONManager() throws FileNotFoundException {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.editions = gson.fromJson(gson.newJsonReader(new FileReader(filename)), List.class);
    }


    @Override
    public void writeEditions(ArrayList<Edition> Editions) {
        try {
            List<String[]> convertedEditions = new ArrayList<>();
            FileWriter writer = new FileWriter(filename);
            for (Edition edition : Editions) {
                String[] line = new String[edition.getNumberOfTrials()];
                int i = 0;
                for(String trialName : edition.getTrials()) {
                    line[i] = trialName;
                    i++;
                }
                String[] data = {String.valueOf(edition.getYear()), String.valueOf(edition.getNumberOfPlayers())};
                String[] all = new String[data.length + line.length];
                System.arraycopy(data, 0, all, 0, data.length);
                System.arraycopy(line, 0, all, data.length, line.length);
                convertedEditions.add(all);
            }
            gson.toJson(convertedEditions, writer);
            writer.close();
        } catch (IOException e) {
            // handle exception
        }
    }

    @Override
    public List<String[]> readEditions() {
        List<String[]> convertedEditions = new ArrayList<>();
        int i = 0;
        for (Edition edition : editions) {
            //System.arraycopy(edition.getDataToWrite(),0,convertedEditions.get(i),0,edition.getDataToWrite().length);
            i++;
        }
        return convertedEditions;
    }
}