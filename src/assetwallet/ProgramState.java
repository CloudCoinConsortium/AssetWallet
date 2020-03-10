/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetwallet;

import assetwallet.core.Asset;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

/**
 *
 * @author Alexander
 */
public class ProgramState {
    final public static int SCREEN_AGREEMENT = 1;
    final public static int SCREEN_SHOW_ASSETS = 2;
    final public static int SCREEN_ECHO_RAIDA = 3;
    final public static int SCREEN_SUPPORT = 4;
    final public static int SCREEN_ECHO_RAIDA_FINISHED = 5;
    final public static int SCREEN_DEPOSIT = 6;
    final public static int SCREEN_DEPOSITING = 7;
    final public static int SCREEN_DEPOSIT_DONE = 8;
    final public static int SCREEN_SHOW_ASSET = 9;

    final static int CB_STATE_INIT = 1;
    final static int CB_STATE_RUNNING = 2;
    final static int CB_STATE_DONE = 3;
    
    final static int SEND_TYPE_WALLET = 1;
    final static int SEND_TYPE_REMOTE = 2;
    final static int SEND_TYPE_FOLDER = 3;
    
    public int currentScreen;
    

    Asset curAsset;
    Asset[] assets;
    
    String errText;

    
    ArrayList<Asset> duplicates;
    
    int[][] counters;
    int cbState;
    
    boolean isAddingWallet;

    ArrayList<String> files;
    
    String chosenFile;
    String typedMemo;

    
    boolean isEchoFinished;

    
    int statToBankValue, statToBank, statFailed, statLost;
    int statFailedValue, statLostValue;
    
    int statTotalFracked, statTotalFixed, statFailedToFix;
    int statTotalFrackedValue, statTotalFixedValue, statFailedToFixValue;
    
    String receiptId;
    

    boolean needBackup;
    
    public Hashtable<String, String[]> cenvelopes;
    
    String domain;
    
    String trustedServer;
    

    boolean popupVisible;
    
    boolean isCheckingSkyID;
    
    boolean needInitWallets;
    

    int failedFiles;
    
   
    
    boolean finishedMc;

    int exportType;
    
    public ProgramState() {
        currentScreen = SCREEN_AGREEMENT;
        errText = "";


        counters = null;
        cbState = CB_STATE_INIT;

        chosenFile = "";
        typedMemo = "";
        files = new ArrayList<String>();

        isEchoFinished = false;

        statToBankValue = statToBank = statFailed = 0;
        statFailedValue = statLostValue = 0;

        
        receiptId = "";
        

        duplicates = null;
        
        needBackup = false;
        
        cenvelopes = null;
        
        statTotalFracked = statTotalFixed = statFailedToFix = 0;
        statTotalFrackedValue = statTotalFixedValue = statFailedToFixValue = 0;
        
        domain = "";
        trustedServer = "";
        
        popupVisible = false;
        
    
        
        needInitWallets = false;
        
        
        finishedMc = false;

        exportType = 0;
    }
 
    public String toString() {
        StringBuilder result = new StringBuilder();
        String newLine = System.getProperty("line.separator");

        Field[] fields = this.getClass().getDeclaredFields();

        for (Field field : fields) {
            result.append("  ");
            try {
                Character c = field.getName().charAt(0);
                if (Character.isUpperCase(c)) {
                    continue;
                }
                
                result.append(field.getName());
                result.append(": ");
                
                result.append(field.get(this));
            } catch (IllegalAccessException ex) {
            }
            result.append("; ");
        }

        return result.toString();
    }
}
