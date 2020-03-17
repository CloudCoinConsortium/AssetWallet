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
            aResult.statuses[i].meta = globalResult.statuses[i].meta;
            aResult.statuses[i].data = globalResult.statuses[i].data;
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
        
        logger.debug(ltag, "Showing asset " + asset.sn);
        
        if (tryCache(asset)) {
            logger.debug(ltag, "Read img/meta from the filesystem: " + asset.originalFile);
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_FINISHED;
            globalResult.statuses[idx].meta = asset.getMeta();
            globalResult.statuses[idx].data = asset.getData();
            return;
        }
        
        logger.debug(ltag, "Showing asset from RAIDA " + asset.sn);
        
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
        String[] bdata = new String[RAIDA.TOTAL_RAIDA_COUNT];
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
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
            bdata[i] = scr.base64data;
        }
        
        // Doing Mirrors if necessary
        if (!queryMirror(asset, idx, collect, bdata, 1))
            return;
        
        if (!queryMirror(asset, idx, collect, bdata, 2))
            return;
        
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {         
            if (collect[i] == null) {
                logger.error(ltag, "Failed to get all chunks from RAIDA servers. Chunk " + i + " is missing");
                globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
                return;
            }
        }
        
        sb = new StringBuilder();
        StringBuilder sbdata = new StringBuilder();
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            sb.append(collect[i]);
            sbdata.append(bdata[i]);
        }
        
        String metadata = sb.toString().replace("-", "");
        String sbdataString = sbdata.toString().replace("-", "");

        byte[] bytes;
        byte[] sdataBytes;

        try {
            bytes = Base64.getDecoder().decode(metadata);
            sdataBytes = Base64.getDecoder().decode(sbdataString);
        } catch (Exception e) {
            logger.debug(ltag, "Failed to decode metadata or data");
            logger.debug(ltag, metadata);
            logger.debug(ltag, sbdataString);
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
            return;
        }

        metadata = new String(bytes);
        metadata = "[meta]\n" + metadata;

        saveData(asset, metadata, sdataBytes);
        
        Map<String, Properties> data;
        try {
            data = AppCore.parseINI(new StringReader(metadata));
        } catch (IOException e) {
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
            return;
        }

        Properties meta = data.get("meta");
        if (meta == null) {
            logger.error(ltag, "Failed to parse properties: " + metadata);
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
            return;
        }
        
        globalResult.statuses[idx].status = ShowCoinsResult.STATUS_FINISHED;
        globalResult.statuses[idx].meta = meta;
        globalResult.statuses[idx].data = sdataBytes;

    }

    public boolean queryMirror(Asset asset, int idx, String[] collect, String[] bdata, int mirrorNum) {
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
        int[] targetRaidas = new int[c];
        String[] mrequests = new String[c];
        c = 0;
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (collect[i] != null)
                continue;
            
            
            if (mirrorNum == 1)
                raidas[c] = AppCore.getRaidaMirror(i);
            else 
                raidas[c] =  AppCore.getRaidaMirror2(i);
            
            targetRaidas[c] = i;
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
            c++;
        }

        String[] results = raida.query(mrequests, null, new CallbackInterface() {
            final GLogger gl = logger;
            final CallbackInterface myCb = cb;
     
            public void callback(Object result) {
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
            if (results[i] != null) {
                if (results[i].equals("")) {
                    logger.error(ltag, "Skipped raida" + targetRaidas[i]);
                    continue;
                }
            }

            ShowCoinsResponse scr = (ShowCoinsResponse) parseResponse(results[i], ShowCoinsResponse.class);
            if (scr == null) {
                logger.error(ltag, "Mirror. Failed to get response coin. RAIDA: " + targetRaidas[i]);
                continue;
            }

            if (!scr.status.equals(Config.REQUEST_STATUS_PASS)) {
                logger.error(ltag, "Mirror. Failed to show coins. RAIDA: " + targetRaidas[i] + " Result: " + scr.message);       
                continue;
            }
            
            collect[targetRaidas[i]] = scr.metadata;
            bdata[targetRaidas[i]] = scr.base64data;
        }

        return true;        
    }
    
    public void saveData(Asset asset, String metadata, byte[] bytes) {
        String folder = AppCore.getUserDir(Config.DIR_GALLERY, user);
        
        String basename = new File(asset.originalFile).getName();
        String imageName = basename + ".jpg";
        String metaName = basename + ".meta";
        
        String imgPath = folder + File.separator + imageName;
        String metaPath = folder + File.separator + metaName;
        
        File fimg = new File(imgPath);
        File fmeta = new File(metaPath);
                
        if (fimg.exists())
            fimg.delete();
        
        if (fmeta.exists())
            fmeta.delete();
        
        
        logger.debug(ltag, "Saving meta " + metaPath);
        if (!AppCore.saveFile(metaPath, metadata)) {
            logger.debug(ltag, "Failed to save metadata");
        }
        
        logger.debug(ltag, "Saving img " + imgPath);
        if (!AppCore.saveFileFromBytes(imgPath, bytes)) {
            logger.debug(ltag, "Failed to save files");            
        }
    }
    
    public boolean tryCache(Asset asset) {
        String folder = AppCore.getUserDir(Config.DIR_GALLERY, user);
        
        String basename = new File(asset.originalFile).getName();
        String imageName = basename + ".jpg";
        String metaName = basename + ".meta";
        
        String imgPath = folder + File.separator + imageName;
        String metaPath = folder + File.separator + metaName;
        
        File fimg = new File(imgPath);
        File fmeta = new File(metaPath);
                
        if (!fimg.exists() || !fmeta.exists()) 
            return false;
       
        byte[] bytes = AppCore.loadFileToBytes(imgPath);
        if (bytes == null)
            return false;
        
        String metadata = AppCore.loadFile(metaPath);
        if (metadata == null) 
            return false;
        
        Map<String, Properties> data;
        try {
            data = AppCore.parseINI(new StringReader(metadata));
        } catch (IOException e) {
            logger.debug(ltag, "Failed to parse cached metadata");
            return false;
        }

        Properties meta = data.get("meta");
        if (meta == null) {
            logger.error(ltag, "Failed to parse properties: " + metadata);
            return false;
        }
        
        logger.debug(ltag, "Successfully extracted data from filesystem");
        asset.setData(bytes, meta);
        
        return true;
    }
    
}
