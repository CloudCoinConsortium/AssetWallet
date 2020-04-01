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
        int total = 0;

        File sdir = new File(dir);        
        if (!sdir.isDirectory()) {
            dir = AppCore.getUserDir(Config.DIR_EXPORT, user);
        }

        String fileName = asset.getMyFilename();
        String filePath = dir + File.separator + fileName;
        logger.debug(ltag, "File name " + filePath);
        File f = new File(filePath);
        if (f.exists()) {
            logger.error(ltag, "File exists: " + filePath);
            er.status = ExporterResult.STATUS_ERROR;
            er.errText = "Exported file already exists: " + filePath;
            return false;
        }
        /*
        byte[] bytes = asset.getData();
        if (bytes == null) {
            logger.error(ltag, "Failed to load bytes from asset");
            return false;
        }
        
        if (!AppCore.saveFileFromBytes(fileName, bytes)) {
            logger.error(ltag, "Failed to write file");
            return false;
        }
        */
        
        if (!AppCore.moveToFolderNewName(asset.getImgPath(user), dir, null, fileName)) {
            logger.error(ltag, "Failed to move file: " + asset.originalFile);
            er.status = ExporterResult.STATUS_ERROR;
            er.errText = "Failed to move file " + asset.originalFile;
            return false;
        }

        er.exportedFileNames.add(dir + File.separator + fileName);
        er.totalExported = total;
        
        return true;       
    }

    
}
