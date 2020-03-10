package assetwallet.core.ShowCoins;

import assetwallet.core.Asset;
import assetwallet.core.RAIDA;
import java.util.Properties;


public class ShowCoinsResult {
    public int[] coins;

    
    public static int STATUS_PROCESSING = 1;
    public static int STATUS_FINISHED = 2;
    public static int STATUS_ERROR = 3;
    public static int STATUS_CANCELLED = 4;

    public int status;
    String errText;
    
    public ShowCoinsAssetResult[] statuses;
    
    public ShowCoinsResult() {
        errText = "";
        status = STATUS_PROCESSING;
    }
    

    
    public int currIdx;
   
    public int getProgress(int idx) {
        return this.statuses[idx].progress;
    }
    
    public int getProgressTotal(int idx) {
        return this.statuses[idx].progressTotal;
    }
    
    public String getOperation(int idx) {
        return this.statuses[idx].operation;
    }
    
    public int getStatus(int idx) {
        return this.statuses[idx].status;
    }
    
    public Properties getMeta(int idx) {
        return this.statuses[idx].meta;
    }
    
    public byte[] getData(int idx) {
        return this.statuses[idx].data;
    }
}


 class ShowCoinsAssetResult {
    public int status;
    public int progress, progressTotal;
    public String operation;
    Properties meta;
    byte[] data;
    
    ShowCoinsAssetResult() {
        this.status = ShowCoinsResult.STATUS_PROCESSING;
        this.progress = 0;
        this.progressTotal = RAIDA.TOTAL_RAIDA_COUNT;
        this.operation = "Stripe";
    }
 }
