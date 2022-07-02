package BusinessLayer;

import BusinessLayer.Entities.BudgetRequest;
import BusinessLayer.Entities.Edition;
import BusinessLayer.Entities.Trials;
import PersistenceLayer.*;
import com.opencsv.exceptions.CsvException;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.util.List;

public class ConductorManager {
    private Edition currentEdition;
    private Trials[] trials;
    private final TrialManager trialManager;
    private EditionsFileManager editionFileManager;
    private TrialsFileManager trialsFileManager;
    private ExecutionFileManager executionFileManager;
    private String errorMessage;
    private int startIndex; //the index of the first trial in the current edition

    /**
     * Constructor for ConductorManager
     * @param trialManager the trial manager
     */
    public ConductorManager(TrialManager trialManager) {
        this.trialManager = trialManager;
    }

    /**
     * Increment the investigation points of a player
     * @param indexTrial the index of the trial
     * @return the amount of investigation points the player has
     */
    public int incrementInvestigationPoints(int indexTrial){
        if(trials[indexTrial].getPassed())
            return trials[indexTrial].getRewardIP();
        else
            return trials[indexTrial].getPenalizationIP();
    }

    /**
     * Get the current edition
     * @return the current edition
     */
    public int getNumberOfPlayers(){
        return currentEdition.getNumberOfPlayers();
    }

    /**
     * Get the number of trials in the current edition
     * @return the number of trials in the current edition
     */
    public int getNumTrials(){
        return currentEdition.getNumberOfTrials();
    }

    /**
     * Get the number of players in the current edition
     * @return the number of players in the current edition
     */
    public int getTotalPlayer(){
        return currentEdition.getNumberOfPlayers();
    }

    public String getTypeOfTrial(int index){
        return trials[index].getTypeOfTrial();
    }

    public void setTrialExtraData(int index, int dataNeeded){
        this.trials[index].setDataNeeded(dataNeeded);
    }

    public String getTrialPrintOutput(int index, String playerName){
        return this.trials[index].printTrialOutput(playerName);
    }

    public String isBudgetAcquired(int index, int sumIPs){
        trials[index].setDataNeeded(sumIPs);
        return ((BudgetRequest) trials[index]).budgetAcquired();
    }

    public boolean isBudgetRequested(int index){
        return trials[index] instanceof BudgetRequest;
    }

    public boolean isPassed(int index){
        return trials[index].getPassed();
    }
    /**
     * Load data for the trials
     * @throws IOException if the file is not found
     * @throws CsvException if the file is not valid
     */
    public void loadDataForTrials() throws IOException, CsvException {
        List<String[]> allTrials = trialsFileManager.readTrials();
        for(String[] trial : allTrials){
            trialManager.addTrial(trial);
        }
    }

    /**
     * Load data for the edition
     * @return true if the edition exists, false otherwise
     */
    public boolean loadDataForCurrentEdition(){
        boolean currentEditionExists = false;
        List<String[]> editionsString;
        try {
            editionsString = editionFileManager.readEditions();
            for(String[] executionInfo: editionsString) {
                if(executionInfo[0].equals("2022")) {
                    currentEdition = new Edition(Integer.parseInt(executionInfo[0]), Integer.parseInt(executionInfo[1]), executionInfo.length-2);
                    trials = new Trials[currentEdition.getNumberOfTrials()];
                    for(int i = 2; i < executionInfo.length; i++) {
                        currentEdition.addTrial(executionInfo[i], i-2);
                        trials[i-2] = trialManager.getTrialByName(executionInfo[i]);
                    }
                    currentEditionExists = true;
                }
            }
        } catch (IOException | CsvException e) {
            errorMessage = "Error loading data for current edition";
        }
        return currentEditionExists;
    }

    /**
     * Initialize the current edition data
     * @param numberOfPlayers the number of players
     */
    public void initializeEditionData(int numberOfPlayers) {
        currentEdition = new Edition(2022, numberOfPlayers, trials.length);
        for(int i = 0; i < trials.length; i++) {
            currentEdition.addTrial(trials[i].getTrialName(), i);
        }
    }

    /**
     * Load data for the execution
     * @return Trials length
     */
    public boolean loadDataForExecution(){
        String[] allTrials;
        try {
            allTrials = executionFileManager.readTrials();
            trials = new Trials[allTrials.length - 1];
            for(int i = 0; i < allTrials.length - 1; i++) {
                trials[i] = trialManager.getTrialByName(allTrials[i]);
            }
            startIndex = Integer.parseInt(allTrials[allTrials.length - 1]);
            return true;
        } catch (IOException | CsvException e) {
            errorMessage = "Error loading data from the execution file";
            return false;
        }
    }

    /**
     * Check if the file is empty
     * @return true if the file is empty, false otherwise
     */
    public int fileIsEmpty(){
        try {
            return executionFileManager.fileIsEmpty()? 1: 0;
        } catch (IOException | CsvValidationException e) {
            errorMessage = "Error checking if the file is empty";
            return 2;
        }
    }


    /**
     * Save the current edition data
     * @param trialIndex the index of the trial
     * @param startIndex the index of the start player
     */
    public boolean saveData(int trialIndex, int startIndex){
        String[] allTrialNames = new String[trials.length - trialIndex + 1];
        for(int i = trialIndex; i < trials.length; i++) {
            allTrialNames[i-trialIndex] = trials[i].getTrialName();
        }
        allTrialNames[allTrialNames.length-1] = String.valueOf(startIndex);
        try {
            executionFileManager.writeTrials(allTrialNames);
            return true;
        } catch (IOException e) {
            errorMessage = "Error saving data for trials";
            return false;
        }
    }

    /**
     * Delete information for the execution files
     */
    public boolean eraseInformationExecutionFile() {
        try {
            executionFileManager.deleteFile();
            return true;
        } catch (IOException e) {
            errorMessage = "Error erasing information from the execution file";
            return false;
        }
    }

    /**
     * Get the error message
     * @return the error message
     */
    public String getErrorMessage() {
        return errorMessage;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public void setFileManagers(EditionsFileManager editionFileManager, TrialsFileManager trialsFileManager, ExecutionFileManager executionFileManager) {
        this.editionFileManager = editionFileManager;
        this.trialsFileManager = trialsFileManager;
        this.executionFileManager = executionFileManager;
    }
}
