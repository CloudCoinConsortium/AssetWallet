package assetwallet.core.ShowCoins;

import org.json.JSONException;

import java.io.File;
import java.io.IOException;

import assetwallet.core.AppCore;
import assetwallet.core.CallbackInterface;
import assetwallet.core.Asset;
import assetwallet.core.CommonResponse;
import assetwallet.core.Config;
import assetwallet.core.GLogger;
import assetwallet.core.RAIDA;
import assetwallet.core.Servant;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;

public class ShowCoins extends Servant {
    String ltag = "ShowCoins";

    ShowCoinsResult globalResult;
    ArrayList<Asset> ccs;

    public ShowCoins(String rootDir, GLogger logger) {
        super("ShowCoins", rootDir, logger);
    }

    public void launch(Asset[] assets, CallbackInterface icb) {
        this.cb = icb;

        
        globalResult = new ShowCoinsResult();
        globalResult.status = ShowCoinsResult.STATUS_PROCESSING;
        globalResult.statuses = new ShowCoinsAssetResult[assets.length];
        for (int i = 0; i < assets.length; i++)
            globalResult.statuses[i] = new ShowCoinsAssetResult();
        
        ccs = new ArrayList<Asset>();
        
        final Asset[] fassets = assets;
        
        launchThread(new Runnable() {
            @Override
            public void run() {
                logger.info(ltag, "RUN ShowCoins");
                doShowCoins(fassets, cb);

                System.out.println("s="+globalResult.status);
                if (globalResult.status != ShowCoinsResult.STATUS_ERROR)
                    globalResult.status = ShowCoinsResult.STATUS_FINISHED;
                if (cb != null)
                    cb.callback(globalResult);
            }
        });
    }
    
    private void copyFromGlobalResult(ShowCoinsResult aResult) {
        aResult.statuses = new ShowCoinsAssetResult[globalResult.statuses.length];
        for (int i = 0; i < globalResult.statuses.length; i++)
            aResult.statuses[i] = new ShowCoinsAssetResult();
        aResult.status = globalResult.status;
        aResult.errText = globalResult.errText;
        aResult.currIdx = globalResult.currIdx;
        for (int i = 0; i < globalResult.statuses.length; i++) {
            aResult.statuses[i].status = globalResult.statuses[i].status;
            aResult.statuses[i].progress = globalResult.statuses[i].progress;
            aResult.statuses[i].progressTotal = globalResult.statuses[i].progressTotal;
            aResult.statuses[i].operation = globalResult.statuses[i].operation;
        }
    }

    public void doShowCoins(Asset[] assets, CallbackInterface cb) {
        
        
        for (int i = 0; i < assets.length; i++) {
            globalResult.currIdx = i;
            showAsset(assets[i], i, cb);
            
            ShowCoinsResult sr = new ShowCoinsResult();
            copyFromGlobalResult(sr);
            if (cb != null)
                cb.callback(sr);            
        }


    }

    public void showAsset(Asset asset, int idx, CallbackInterface cb) {
        String[] results;
        Object[] o;
        StringBuilder sb;
        String[] requests;
        
        logger.debug(ltag, "showing asset " + asset.sn);
        
        //sbs = new StringBuilder[RAIDA.TOTAL_RAIDA_COUNT];
        requests = new String[RAIDA.TOTAL_RAIDA_COUNT];
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            sb = new StringBuilder();
            sb.append("detect_denom?nn=");
            sb.append(asset.nn);
            sb.append("&sn=");
            sb.append(asset.sn);
            sb.append("&an=");
            sb.append(asset.ans[i]);
            sb.append("&pan=");
            sb.append(asset.ans[i]);
            sb.append("&denomination=1");
            sb.append("&data=stripe");
            
            requests[i] = sb.toString();
        }
        
        results = raida.query(requests, null, new CallbackInterface() {
            final GLogger gl = logger;
            final CallbackInterface myCb = cb;

            
            public void callback(Object result) {
                System.out.println("xxx="+ idx + " c=" + asset.sn + " p=" + globalResult.statuses[idx].progress+"");
                globalResult.statuses[idx].progress++;
                if (myCb != null) {
                    ShowCoinsResult sr = new ShowCoinsResult();
                    copyFromGlobalResult(sr);
                    myCb.callback(sr);
                }
            }
        });
        
        if (results == null) {
            logger.error(ltag, "Failed to query showcoins");
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
            return;
        }

        if (isCancelled()) {
            logger.error(ltag, "ShowCoins cancelled");
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_CANCELLED;
            return;
        }
        
        String[] collect = new String[RAIDA.TOTAL_RAIDA_COUNT];
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            System.out.println("r="+i+" res="+results[i]);
            if (results[i] != null) {
                if (results[i].equals("")) {
                    logger.error(ltag, "Skipped raida" + i);
                    continue;
                }
            }

            ShowCoinsResponse scr = (ShowCoinsResponse) parseResponse(results[i], ShowCoinsResponse.class);
            if (scr == null) {
                logger.error(ltag, "Failed to get response coin. RAIDA: " + i);
                continue;
            }

            if (!scr.status.equals(Config.REQUEST_STATUS_PASS)) {
                logger.error(ltag, "Failed to show env coins. RAIDA: " + i + " Result: " + scr.message);       
                continue;
            }
            
            collect[i] = scr.metadata;
        }
        
        // Doing Mirrors if necessary
        if (!queryMirror(asset, idx, collect, 1))
            return;
        
        if (!queryMirror(asset, idx, collect, 2))
            return;
        
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (collect[i] == null) {
                logger.error(ltag, "Failed to get all chunks from RAIDA servers. Chunk " + i + " is missing");
                globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
                return;
            }
        }
        
        sb = new StringBuilder();
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++)
            sb.append(collect[i]);
        
        String metadata = sb.toString().replace("-", "");
        System.out.println(metadata);
        byte[] bytes = Base64.getDecoder().decode(metadata);
        metadata = new String(bytes);
        System.out.println(metadata);
        Map<String, Properties> data;
        try {
            data = AppCore.parseINI(new StringReader(metadata));
        } catch (IOException e) {
            System.out.println("failed");
            return;
        }

        System.out.println(data);
        Properties font = data.get("font_size");
        
        System.out.println("f=");
        
        
        System.out.println("c="+metadata);
        
        globalResult.statuses[idx].status = ShowCoinsResult.STATUS_FINISHED;
        
        
  
  //      System.out.println("s1=" + assembleMessage(collect));
    }

    public boolean queryMirror(Asset asset, int idx, String[] collect, int mirrorNum) {
        StringBuilder sb;      
        String mirror;
        
        if (mirrorNum == 1)
            mirror = "mirror";
        else if (mirrorNum == 2)
            mirror = "mirror2";
        else {
            logger.error(ltag, "Invalid mirror: " + mirrorNum);
            return false;
        }
        
        int c = 0;
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (collect[i] != null)
                continue;
                
            c++;
        }
        System.out.println("DOING MIRROR " + mirror + " c=" + c);
        if (c == 0) {
            logger.debug(ltag, "No need to query mirror " + mirror);
            return true;
        }
            
        
        globalResult.statuses[idx].progress = 0;
        globalResult.statuses[idx].progressTotal = c;
        globalResult.statuses[idx].operation = mirror;
        if (cb != null) {
            ShowCoinsResult sr = new ShowCoinsResult();
            copyFromGlobalResult(sr);
            cb.callback(sr);
        }
        
        int[] raidas = new int[c];
        String[] mrequests = new String[c];
        c = 0;
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (collect[i] != null)
                continue;
            
            if (mirrorNum == 1)
                raidas[c] = AppCore.getRaidaMirror(i);
            else 
                raidas[c] =  AppCore.getRaidaMirror2(i);
            
            if (raidas[c] < 0) {
                logger.error(ltag, "Invalid mirror raida: " + raidas[c]);
                globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
                return false;
            }
            
            sb = new StringBuilder();
            sb.append("detect_denom?nn=");
            sb.append(asset.nn);
            sb.append("&sn=");
            sb.append(asset.sn);
            sb.append("&an=");
            sb.append(asset.ans[raidas[c]]);
            sb.append("&pan=");
            sb.append(asset.ans[raidas[c]]);
            sb.append("&denomination=1");
            sb.append("&data=");
            sb.append(mirror);

            
            
            mrequests[c] = sb.toString();
            System.out.println("r="+mrequests[c]);
            c++;
        }
        

        
        String[] results = raida.query(mrequests, null, new CallbackInterface() {
            final GLogger gl = logger;
            final CallbackInterface myCb = cb;
     
            public void callback(Object result) {
                System.out.println("xxx2="+ idx + " c=" + asset.sn + " p=" + globalResult.statuses[idx].progress+"");
                globalResult.statuses[idx].progress++;
                if (myCb != null) {
                    ShowCoinsResult sr = new ShowCoinsResult();
                    copyFromGlobalResult(sr);
                    myCb.callback(sr);
                }
            }
        }, raidas);
        
        if (results == null) {
            logger.error(ltag, "Failed to query showcoins for mirror " + mirror);
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
            return false;
        }

        if (isCancelled()) {
            logger.error(ltag, "ShowCoins cancelled in mirror " + mirror);
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_CANCELLED;
            return false;
        }

        for (int i = 0; i < results.length; i++) {
            System.out.println("r22="+i+" res="+results[i]);
            if (results[i] != null) {
                if (results[i].equals("")) {
                    logger.error(ltag, "Skipped raida" + i);
                    continue;
                }
            }

            ShowCoinsResponse scr = (ShowCoinsResponse) parseResponse(results[i], ShowCoinsResponse.class);
            if (scr == null) {
                logger.error(ltag, "Mirror. Failed to get response coin. RAIDA: " + i);
                continue;
            }

            if (!scr.status.equals(Config.REQUEST_STATUS_PASS)) {
                logger.error(ltag, "Mirror. Failed to show coins. RAIDA: " + i + " Result: " + scr.message);       
                continue;
            }
            
            System.out.println("setting md="+i+" m="+scr.metadata);
            collect[i] = scr.metadata;
        }

        return true;        
    }
    
    

    public String assembleMessage(String[] mparts) {
        int cs, length;
    
        cs = 0;
        
        // Determine the chunk size
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (mparts[i] == null)
                continue;

            System.out.println("length="+mparts[i].length() + " val="+mparts[i]+ " i="+i);
            cs = mparts[i].length() / 3;
            break;
        }

        // Failed to determine the chunk size
        if (cs == 0)
            return null;

        // The length of the message
        length = cs * RAIDA.TOTAL_RAIDA_COUNT;
        
        char[] msg = new char[length];
        //msg = [length]

        System.out.println("l="+length + " cs="+cs);
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            System.out.println("m="+i+" v="+mparts[i]);
            if (mparts[i] == null)
                continue;

            // Split the data from one RAIDA server
            char[] chrs = mparts[i].toCharArray();

            System.out.println("cl="+chrs.length);
            // Go over this data
            for (int j = 0; j < chrs.length; j += 3) {
                int triplet = j / 3;

                int cidx0 = triplet * 25 + i;
                int cidx1 = triplet * 25 + i + 3;
                int cidx2 = triplet * 25 + i + 6;

                if (cidx0 >= length)
                    cidx0 -= length;

                if (cidx1 >= length)
                    cidx1 -= length;

                if (cidx2 >= length)
                    cidx2 -= length;
                                        
                msg[cidx0] = chrs[j];
                msg[cidx1] = chrs[j + 1];
                msg[cidx2] = chrs[j + 2];
            }
        }
        
        // Check if the message is full
        for (int i = 0; i < length; i++) {
            if (msg[i] == '\0') {
                logger.error(ltag, "Message is not full. idx " + i);
                return null;
            }
        }
        
        String result = msg.toString().replace("-", "");
        
        System.out.println("r="+result);


        return result;
    }


}
