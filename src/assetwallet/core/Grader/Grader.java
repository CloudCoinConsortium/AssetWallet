package assetwallet.core.Grader;


import org.json.JSONException;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import assetwallet.core.AppCore;
import assetwallet.core.CallbackInterface;
import assetwallet.core.Asset;
import assetwallet.core.Config;
import assetwallet.core.GLogger;
import assetwallet.core.RAIDA;
import assetwallet.core.Servant;
import java.util.ArrayList;

public class Grader extends Servant {
    String ltag = "Grader";
    GraderResult gr;

    public Grader(String rootDir, GLogger logger) {
        super("Grader", rootDir, logger);
    }

    public void launch(CallbackInterface icb, ArrayList<Asset> duplicates, String source) {
        this.cb = icb;

        gr = new GraderResult();
        csb = new StringBuilder();

        receiptId = AppCore.generateHex();
        gr.receiptId = receiptId;
        
        final String fsource = source;
        
        final ArrayList<Asset> fduplicates = duplicates;

        launchThread(new Runnable() {
            @Override
            public void run() {
                logger.info(ltag, "RUN Grader");
                doGrade(fduplicates, fsource);

                if (cb != null)
                    cb.callback(gr);
            }
        });
    }

    public void doGrade(ArrayList<Asset> duplicates, String source) {
        String fullPath = AppCore.getUserDir(Config.DIR_DETECTED, user);
        Asset cc;
        boolean graded = false;

        File dirObj = new File(fullPath);
        for (File file: dirObj.listFiles()) {
            if (file.isDirectory())
                continue;

            if (!AppCore.hasCoinExtension(file))
                continue;
            
            graded = true;
            try {
                cc = new Asset(file.toString());
            } catch (JSONException e) {
                logger.error(ltag, "Failed to parse coin: " + file.toString() +
                        " error: " + e.getMessage());

                gr.totalUnchecked++;
                continue;
            }

            gradeCC(cc, source);
        }
        
        int dups = 0;
        if (duplicates != null && duplicates.size() != 0) {
            graded = true;
            dups = duplicates.size();
            for (Asset dcc : duplicates) {
                for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++)
                    dcc.setDetectStatus(i, Asset.STATUS_UNTRIED);
                
                dcc.setPownStringFromDetectStatus();
                
                logger.info(ltag, "Removing dup coin: " + dcc.sn);
                
                String ccFile = AppCore.getUserDir(Config.DIR_TRASH, user) + File.separator + dcc.getFileName();
                if (!AppCore.saveFile(ccFile, dcc.getJson(false))) {
                    logger.error(ltag, "Failed to save file: " + ccFile);
                    continue;
                }
                
                AppCore.deleteFile(dcc.originalFile);
            }
        }
    }

    public void gradeCC(Asset cc, String fsource) {
        String dstFolder;
        int untried, counterfeit, passed, error;

        untried = counterfeit = passed = error = 0;
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            switch (cc.getDetectStatus(i)) {
                case Asset.STATUS_ERROR:
                    error++;
                    break;
                case Asset.STATUS_FAIL:
                    counterfeit++;
                    break;
                case Asset.STATUS_UNTRIED:
                    untried++;
                    break;
                case Asset.STATUS_PASS:
                    passed++;
                    break;
            }
        }

        boolean includePans = false;
        String ccFile;

        String dst = "";
        if (passed >= Config.PASS_THRESHOLD) {
            if (counterfeit != 0) {
                logger.debug(ltag, "Coin " + cc.sn + " is fracked");

                gr.totalFracked++;
                gr.totalFrackedValue += cc.getDenomination();
                dst = dstFolder = Config.DIR_FRACKED;
                if (fsource != null) 
                    dst += " from " + fsource;

            } else {
                logger.debug(ltag, "Coin " + cc.sn + " is authentic");

                gr.totalAuthentic++;
                gr.totalAuthenticValue += cc.getDenomination();
                dst = dstFolder = Config.DIR_BANK;
                if (fsource != null) 
                    dst += " from " + fsource;

            }

            cc.calcExpirationDate();

            ccFile = AppCore.getUserDir(dstFolder, user) + File.separator + cc.getFileName();
        } else {
            if (passed + counterfeit > Config.PASS_THRESHOLD) {
                logger.debug(ltag, "Coin " + cc.sn + " is counterfeit");

                gr.totalCounterfeit++;
                gr.totalCounterfeitValue += cc.getDenomination();
                dst = dstFolder = Config.DIR_COUNTERFEIT;
                if (fsource != null) 
                    dst += " from " + fsource;

            } else {
                logger.debug(ltag, "Coin " + cc.sn + " is lost");

                gr.totalLost++;
                dst = dstFolder = Config.DIR_LOST;
                if (fsource != null) 
                    dst += " from " + fsource;

                gr.totalLostValue += cc.getDenomination();
                includePans = true;
            }

            ccFile = AppCore.getUserDir(dstFolder, user) + File.separator + cc.getFileName();
            File f = new File(ccFile);
            if (f.exists()) {
                logger.debug(ltag, "This coin already exists. Overwriting it");
                AppCore.deleteFile(ccFile);
            }
        }

        cc.setAnsToPansIfPassed();

        logger.info(ltag, "Saving grader coin: " + ccFile + " include=" + includePans);
        if (!AppCore.saveFile(ccFile, cc.getJson(includePans))) {
            logger.error(ltag, "Failed to save file: " + ccFile);
            return;
        }

        AppCore.deleteFile(cc.originalFile);
    }
    
    
    
    
}
