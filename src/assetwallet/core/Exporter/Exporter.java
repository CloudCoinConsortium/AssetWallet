package assetwallet.core.Exporter;


import assetwallet.AppUI;
import org.json.JSONException;

import java.io.File;
import java.util.ArrayList;

import assetwallet.core.AppCore;
import assetwallet.core.CallbackInterface;
import assetwallet.core.Asset;
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
import java.io.IOException;
import java.util.Properties;
import javax.imageio.ImageIO;

public class Exporter extends Servant {
    String ltag = "Exporter";
    ExporterResult er;
    String ls;


    public Exporter(String rootDir, GLogger logger) {
        super("Exporter", rootDir, logger);
    }

    public void launch(CallbackInterface icb) {
        this.cb = icb;
    }

    public void launch(int type, Asset asset, String tag, String dir, CallbackInterface icb) {
        this.cb = icb;

        final int ftype = type;
        final Asset fasset = asset;
        final String ftag = tag;
        final String fdir = dir;


        er = new ExporterResult();
        csb = new StringBuilder();
        
        ls = System.getProperty("line.separator");
    
        launchThread(new Runnable() {
            @Override
            public void run() {
                logger.info(ltag, "RUN Exporter");

                doExport(ftype, fasset, fdir, ftag);
            }
        });
    }
    
    
    public void doExport(int type, Asset asset, String dir, String tag) {
        if (tag.equals(""))
            tag = Config.DEFAULT_TAG;

        logger.debug(ltag, "Export type " + type + " dir " + dir + " tag " + tag + " user " + user);

        if (tag.indexOf('.') != -1 || tag.indexOf('/') != -1 || tag.indexOf('\\') != -1) {
            logger.error(ltag, "Invalid tag");
            er.status = ExporterResult.STATUS_ERROR;
            if (cb != null)
                cb.callback(er);
            
            return;
        }
        
        if (tag.toLowerCase().equals(Config.TAG_RANDOM)) {
            tag = AppCore.generateHex();
            logger.debug(ltag, "Generated random tag: " + tag);
        }
      
        String fullExportPath = AppCore.getUserDir(Config.DIR_EXPORT, user);        
        if (dir != null)
            fullExportPath = dir;

        if (type == Config.TYPE_PNG) {
            if (!exportPng(fullExportPath, asset, tag)) {
                er.status = ExporterResult.STATUS_ERROR;
                if (cb != null)
                    cb.callback(er);

                return;
            }
        } else {
            logger.error(ltag, "Unsupported format");
            er.status = ExporterResult.STATUS_ERROR;
            if (cb != null)
                cb.callback(er);

            return;
        }
        
        er.status = ExporterResult.STATUS_FINISHED;
        if (cb != null)
            cb.callback(er);

        logger.info(ltag, "EXPORT finished " + fullExportPath);
        
        // Delete asset
        logger.debug(ltag, "Deleting asset");
        asset.clean(user);
    }
 
    private boolean exportPng(String dir, Asset asset, String tag) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        int total = 0;
        String data;

        sb.append("{" + ls + "\t\"celebrium\": [");
        sb.append(asset.getSimpleJson());
        sb.append("]" + ls + "}");
        
        data = sb.toString();        
        File sdir = new File(dir);        
        if (!sdir.isDirectory()) {
            dir = AppCore.getUserDir(Config.DIR_EXPORT, user);
        }

        String fileName = asset.getMyFilename();
        fileName = dir + File.separator + fileName;
        logger.debug(ltag, "File name " + fileName);
         File f = new File(fileName);
        if (f.exists()) {
            logger.error(ltag, "File exists: " + fileName);
            er.status = ExporterResult.STATUS_ERROR;
            er.errText = "Exported file already exists: " + fileName;
            return false;
        }
   
        byte[] bytes = asset.getData();
        if (bytes == null) {
            logger.error(ltag, "Failed to load bytes from asset");
            return false;
        }
        
        logger.debug(ltag, "Writing text");
        bytes = writeText(asset, bytes);
        if (bytes == null) {
            logger.error(ltag, "Failed to write text on the image");
            return false;
        }
    
        int idx = AppCore.basicPngChecks(bytes);
        if (idx == -1) {
            er.status = ExporterResult.STATUS_ERROR;
            er.errText = "PNG checks failed";
            logger.error(ltag, "PNG checks failed");
            return false;
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

        

        if (!AppCore.saveFileFromBytes(fileName, nbytes)) {
            logger.error(ltag, "Failed to write file");
            return false;
        }

        er.exportedFileNames.add(fileName);
        er.totalExported = total;
        
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
