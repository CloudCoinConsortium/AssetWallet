package assetwallet.core.Exporter;


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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
    }
/*
    private void deletePickedCoins() {
        for (CloudCoin cc : coinsPicked) {
            AppCore.deleteFile(cc.originalFile);
        }
    }
*/
  

    
    private boolean exportPng(String dir, Asset asset, String tag) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        int total = 0;
        String fileName;
        String data;

        sb.append("{" + ls + "\t\"celebrium\": [");
        sb.append(asset.getSimpleJson());
        sb.append("]" + ls + "}");
        
        data = sb.toString();
        
        fileName = asset.sn + ".Celebrium." + tag + ".png";
        
        File sdir = new File(dir);        
        if (!sdir.isDirectory()) {
            dir = AppCore.getUserDir(Config.DIR_EXPORT, user);
        }

        fileName = dir + File.separator + fileName;
        logger.debug(ltag, "File name " + fileName);
     
        byte[] bytes = asset.getData();
        if (bytes == null) {
            logger.error(ltag, "Failed to load bytes from asset");
            return false;
        }
        
        ByteArrayOutputStream ba = new ByteArrayOutputStream();
        
        try {
            BufferedImage bi = ImageIO.read(new ByteArrayInputStream(bytes));
            ImageIO.write(bi, "png", ba);
        } catch (IOException e) {
            er.status = ExporterResult.STATUS_ERROR;
            er.errText = "Failed to convert asset to PNG";
            logger.error(ltag, "Failed to convert to png: " + e.getMessage());
            return false;
        }
       
        bytes = ba.toByteArray();
        
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

        File f = new File(fileName);
        if (f.exists()) {
            logger.error(ltag, "File exists: " + fileName);
            er.status = ExporterResult.STATUS_ERROR;
            er.errText = "Exported file with the same tag already exists";
            return false;
        }
        
        System.out.println("f="+fileName);
        if (!AppCore.saveFileFromBytes(fileName, nbytes)) {
            logger.error(ltag, "Failed to write file");
            return false;
        }

        er.exportedFileNames.add(fileName);
        er.totalExported = total;
        
        return true;       
    }

    private void ByteArrayInputStream(byte[] bytes) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    

}
