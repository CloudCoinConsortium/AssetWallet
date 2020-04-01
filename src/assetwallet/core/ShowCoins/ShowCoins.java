package assetwallet.core.ShowCoins;

import assetwallet.AppUI;
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
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Map;
import java.util.Properties;
import javax.imageio.ImageIO;

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
        
        asset.setData(sdataBytes, meta);
        sdataBytes = appendText(asset, sdataBytes, meta);
        if (sdataBytes == null) {
            logger.error(ltag, "Failed to append text on the Image");
            globalResult.statuses[idx].status = ShowCoinsResult.STATUS_ERROR;
            return;
        }
 
        saveData(asset, metadata, sdataBytes);
        
        globalResult.statuses[idx].status = ShowCoinsResult.STATUS_FINISHED;
        globalResult.statuses[idx].meta = meta;
        globalResult.statuses[idx].data = sdataBytes;

    }

    public byte[] appendText(Asset asset, byte[] obytes, Properties meta) {
        String ls = System.getProperty("line.separator");
        String data;
        
        StringBuilder sb = new StringBuilder();
        sb.append("{" + ls + "\t\"celebrium\": [");
        sb.append(asset.getSimpleJson());
        sb.append("]" + ls + "}");
        data = sb.toString();
        
        byte[] bytes = writeText(asset, obytes);
        if (bytes == null) {
            logger.error(ltag, "Failed to write text on the image");
            return null;
        }
        
        int idx = AppCore.basicPngChecks(bytes);
        if (idx == -1) {
            logger.error(ltag, "PNG checks failed");
            return null;
        }
         
        logger.info(ltag, "Loaded, bytes: " + bytes.length);
        int dl = data.length();
        int chunkLength = dl + 12;
        logger.debug(ltag, "data length " + dl);
        byte[] nbytes = new byte[bytes.length + chunkLength];
        for (int i = 0; i < idx + 4; i++) {
            nbytes[i] = bytes[i];
        }

        // Setting up the chunk
        // Set length
        AppCore.setUint32(nbytes, idx + 4, dl);
        
        // Header cLDc
        nbytes[idx + 4 + 4] = 0x63;
        nbytes[idx + 4 + 5] = 0x4c;
        nbytes[idx + 4 + 6] = 0x44;
        nbytes[idx + 4 + 7] = 0x63;
                
        for (int i = 0; i < dl; i++) {
            nbytes[i + idx + 4 + 8] = (byte) data.charAt(i);
        }

        // crc
        long crc32 = AppCore.crc32(nbytes, idx + 8, dl + 4);
        AppCore.setUint32(nbytes, idx + 8 + dl + 4, crc32);
        logger.debug(ltag, "crc32 " + crc32);
        
        // Rest data
        for (int i = 0; i < bytes.length - idx - 4; i++) {
            nbytes[i + idx + 8 + dl + 4 + 4] = bytes[i + idx + 4];
        }

        return nbytes;
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
        
        asset.clean(user, false);
        
        String metaPath = asset.getMetaPath(user);
        String imgPath = asset.getImgPath(user);
        
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
       
        String imgPath = asset.getImgPath(user);
        String metaPath = asset.getMetaPath(user);
        
        File fimg = new File(imgPath);
        File fmeta = new File(metaPath);
                
        logger.debug(ltag, "Trying cache: " + imgPath + " m= " +metaPath);
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
    
    public byte[] writeText(Asset asset, byte[] bytes) {
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        BufferedImage bi;
        try {
            bi = ImageIO.read(bais);
        } catch (IOException e) {
            logger.debug(ltag, "Failed to convert bytes to Image: " + e.getMessage());
            return null;
        }
        
        Properties meta = asset.getMeta();
        if (meta == null) {
            logger.debug(ltag, "No meta found");
            return null;
        }

        String fontSize = AppCore.getMetaItem(meta, "font_size");
        if (fontSize == null)
            fontSize = "20";

        String fontFamily = AppCore.getMetaItem(meta, "font_family");
        if (fontFamily == null)
            fontFamily = "Arial Black";
        
        String fontColor = AppCore.getMetaItem(meta, "font_color");
        if (fontColor == null)
            fontColor = "FFFFFF";
        
        int x, y;
        try {
            
            x = Integer.parseInt(AppCore.getMetaItem(meta, "text_location_x"));
            y = Integer.parseInt(AppCore.getMetaItem(meta, "text_location_y"));
        } catch (NumberFormatException e) {
            logger.debug(ltag, "Failed to parse location in Meta. Falling back to default");
            x = y = 0;
        }
           
        int sn = asset.getTranslatedSN();      
        String r = AppCore.getExpstring(sn);
        
        logger.debug(ltag, "Font " + fontFamily + " " + fontSize + ", " + fontColor + " position: " + x + "," + y);
        
        
        Color color = AppUI.hex2Rgb("#" + fontColor);
        Font font = new Font(fontFamily, Font.BOLD, 20);
        String text = r;
       
        Graphics graphics = bi.getGraphics();
        //graphics.setColor(Color.LIGHT_GRAY);
        //graphics.fillRect(0, 0, 200, 50);
        graphics.setColor(color);
        graphics.setFont(font);
        graphics.drawString(text, x, y);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write(bi, "png", baos);
        } catch (IOException e) {
            logger.debug(ltag, "Failed to convert Image to bytes: " + e.getMessage());
            return null;
        }
        
        return baos.toByteArray();
    }
}
