/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetwallet.core;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;


/**
 *
 * @author Alexander
 */
public class Wallet implements Comparable<Wallet> {
    String ltag = "Wallet";
    GLogger logger;
    String name;
    String lsep;
    String password;
    boolean isEncrypted;
    String email;
    Asset cc;
    Object uiRef;
    int total;
    String passwordHash;
    int[] sns;
    int[][] counters;
    public Hashtable<String, String[]> envelopes;
    boolean isUpdated;
    boolean correctionAdded;
    
    public Wallet(String name, String email, boolean isEncrypted, String password, GLogger logger) {
        this.name = name;
        this.email = email;
        this.isEncrypted = isEncrypted;
        this.password = password;
        this.ltag += " " + name;
        this.logger = logger;
        this.sns = new int[0];
        this.isUpdated = false;
        this.correctionAdded = false;
               
        logger.debug(ltag, "wallet " + name + " EmailRecovery: " + email + " isEncrypted: " + isEncrypted);
        lsep = System.getProperty("line.separator");
    }
    
    public void setEnvelopes(Hashtable<String, String[]> envelopes) {
        this.envelopes = envelopes;
    }
    
    public Hashtable<String, String[]> getEnvelopes() {
        return this.envelopes;
    }
    
    public int[] getSNs() {
        return this.sns;
    }
    
    public void setSNs(int[] sns) {
        this.sns = sns;
    }
    
    public void setTotal(int total) {
        this.total = total;
    }
    
    public int getTotal() {
        return this.total;
    }
    
    public void setuiRef(Object uiRef) {
        this.uiRef = uiRef;
    }
    
    public Object getuiRef() {
        return uiRef;
    }
    
    public void setCounters(int[][] counters) {
        this.counters = counters;
    }
    
    public int[][] getCounters() {
        return this.counters;
    }

    public String getName() {
        return this.name;
    }
    
    public String getEmail() {
        return this.email;
    }
    
    public boolean isUpdated() {
        return this.isUpdated;
    }
    
    public void setUpdated() {
        this.isUpdated = true;
    }
    
    public void setNotUpdated() {
        this.isUpdated = false;
    }
    
    public String getTransactionsFileName() {
        String tfFileName = Config.TRANSACTION_FILENAME;
        String rname;
 
        String fileName = AppCore.getUserDir(tfFileName, name);
        
        return fileName;
    }
    
    public void saveTransations(String dstPath) {
        String fileName = getTransactionsFileName();
        
        AppCore.copyFile(fileName, dstPath);
    }
    
    public void saveEnvelopes(String dstPath) {
        StringBuilder sb = new StringBuilder();

        Enumeration<String> enumeration = envelopes.keys();
        ArrayList<String> hlist = Collections.list(enumeration);
        Collections.sort(hlist);

        for (String key : hlist) {
            String[] data = envelopes.get(key);

            String value = data[0] + "," + data[2] + "," + data[1] + lsep;
            sb.append(value);
        }
       
        logger.debug(ltag, "Saving " + dstPath);
        AppCore.saveFile(dstPath, sb.toString());  
    }
    
    public String[][] getTransactions() {
        String fileName = getTransactionsFileName();

        String data = AppCore.loadFile(fileName);
        if (data == null)
            return null;
        
        String[] parts = data.split("\\r?\\n");
        String[][] rv = new String[parts.length][];
        
        for (int i = 0; i < parts.length; i++) {
            rv[i] = parts[i].split(",");
            if (rv[i].length != 6) {
                logger.error(ltag, "Transaction parse error: " + parts[i]);
                return null;
            }
            
            rv[i][3] = rv[i][3].replace("-", "");
        }
        
        return rv;            
    }
    
    public void appendTransaction(String memo, int amount, String receiptId) {
        appendTransaction(memo, amount, receiptId, AppCore.getCurrentDate());
    }
    
    public void appendTransaction(String memo, int amount, String receiptId, String date) { 
        logger.debug(ltag, "Append transaction " + receiptId + " amount " + amount + " wallet " + getName());
        
        if (!receiptId.equals("COUNTERFEIT")) {
            if (amount == 0)
                return;
        }
        
        String fileName = getTransactionsFileName();
        String rMemo = memo.replaceAll("\r\n", " ").replaceAll("\n", " ").replaceAll(",", " ");
        //String sAmount = Integer.toString(amount);
        
        int rest = 0;
        String[][] tr = getTransactions();
        if (tr != null) {        
            String[] last = tr[tr.length - 1];
            String rRest = last[4];
            
            try {
                rest = Integer.parseInt(rRest);
            } catch (NumberFormatException e) {
                rest = 0;
            }
        }
         
        int expectedRest = getTotal() + amount;
        String result = "";
        if (rest != getTotal() && !correctionAdded) {
            int adjusted = getTotal() - rest;
            
            logger.debug(ltag, "Appending correcting transaction. Rest was: " + 
                    rest + " total=" + getTotal() + " adjusted: " + adjusted);
            
            result = "Balance Auto Adjustment," + date + ",";
            if (adjusted > 0) {
                result += adjusted + ",,";
            } else {
                result += "," + adjusted + ",";
            }
            
            result += getTotal() + ",dummy" + lsep;
            rest = getTotal();    
        }
               
        correctionAdded = true;
        
        rest += amount;
        result += rMemo + "," + date + ",";
        if (amount >= 0) {
            result += amount + ",,";
        } else {
            result += "," + amount + ",";
        }
        
        result += rest;
        result += "," + receiptId + lsep;
 
        logger.debug(ltag, "Saving " + result);
        AppCore.saveFileAppend(fileName, result, true);              
    }

    @Override
    public int compareTo(Wallet w) {
        return getName().compareTo(w.getName());
    }
    

    
}
