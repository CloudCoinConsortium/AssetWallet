package assetwallet.core.Unpacker;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
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
import assetwallet.core.Unpacker.UnpackerResult;
import java.util.ArrayList;
import java.util.Arrays;

public class Unpacker extends Servant {
    String ltag = "Unpacker";
    
    UnpackerResult globalResult;
    ArrayList<Asset> rccs;

    public Unpacker(String rootDir, GLogger logger) {
        super("Unpacker", rootDir, logger);
    }

    public void launch(CallbackInterface icb) {
        this.cb = icb;

        globalResult = new UnpackerResult();
        globalResult.errText = "";
        
        rccs = new ArrayList<Asset>();
        
        launchThread(new Runnable() {
            @Override
            public void run() {
                logger.info(ltag, "RUN Unpacker");
                doUnpack();

                if (cb != null)
                    cb.callback(globalResult);
            }
        });
    }
 
    
    public int checkCoinsInFolder(String folder) {
        String fullPath = AppCore.getUserDir(folder, user);
        Asset cc;
    
        File dirObj = new File(fullPath);
        if (dirObj.listFiles() == null) {
            logger.error(ltag, "No such dir " + fullPath);
            return -1;
        }

        for (File file: dirObj.listFiles()) {
            if (file.isDirectory())
                continue;

            if (!AppCore.hasCoinExtension(file))
                continue;
            
            logger.debug(ltag, "Parsing " + file);

            try {
                cc = new Asset(file.toString());
            } catch (JSONException e) {
                logger.error(ltag, "Failed to parse JSON: " + e.getMessage());
                continue;
            }
            
            for (Asset tcc : rccs) {
                if (tcc.sn == cc.sn) {
                    logger.debug(ltag, "Duplicate sn " + tcc.sn + " in the " + folder);
                    globalResult.duplicates.add(tcc);
                }
            }       
        }
        
        return 0;
    }
    
    public void addCoinToRccs(Asset cc, String fileName) {
        logger.debug(ltag, "Adding cc sn " + cc.sn + " file " + fileName);
        
        cc.originalFile = fileName;
        rccs.add(cc);
    }
    
    public void doUnpack() {
        String importFolder = AppCore.getUserDir(Config.DIR_IMPORT, user);
        String fileName, extension;
        int index;
        
        File dirObj = new File(importFolder);
        if (dirObj.listFiles() == null) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            logger.error(ltag, "Import Dir doesn't exist");
            return;
        }
        
        for (File file: dirObj.listFiles()) {
            if (file.isDirectory())
                continue;

            if (!AppCore.hasCoinExtension(file))
                continue;
            
            fileName = file.getName();
            index = fileName.lastIndexOf('.');
            if (index <= 0) {
                logger.error(ltag, "Skipping filename " + fileName + ". No extension found");
                //AppCore.moveToTrash(file.toString(), user);
                //globalResult.status = UnpackerResult.STATUS_ERROR;
                //return;
                continue;
            }

            extension = fileName.substring(index + 1).toLowerCase();

            logger.debug(ltag, "file " + fileName + " ext " + extension);

            boolean rv = false;
            if (extension.equals("jpg") || extension.equals("jpeg")) {
                rv = doUnpackJpeg(file.toString());
            } else if (extension.equals("stack")) {
                rv = doUnpackStack(file.toString());
            } else if (extension.equals("png")) {
                rv = doUnpackPng(file.toString());
            } else {
                rv = doUnpackStack(file.toString());
            }

            if (!rv) {
                logger.error(ltag, "Error processing file: " + fileName);
                AppCore.moveToTrash(file.toString(), user);
                globalResult.failedFiles++;
                continue;
            }
        }
        
        int sn;
        sn = checkCoinsInFolder(Config.DIR_BANK);
        if (sn == -1) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            return;
        }
        /*
        if (sn != 0) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            globalResult.errText = "Error. Coin " + sn + " exists in the Bank";
            return;
        }*/

        sn = checkCoinsInFolder(Config.DIR_FRACKED);
        if (sn == -1) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            return;
        }
        /*
        if (sn != 0) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            globalResult.errText = "Error. Coin " + sn + " exists in the Fracked";
            return;
        }
        */
        sn = checkCoinsInFolder(Config.DIR_LOST);
        if (sn == -1) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            return;
        }
        
        /*
        if (sn != 0) {
            globalResult.status = UnpackerResult.STATUS_ERROR;
            globalResult.errText = "Error. Coin " + sn + " exists in the Lost";
            return;
        }
        */

        rccs.removeAll(globalResult.duplicates);
        for (Asset cc : rccs) {
            if (!saveCoin(cc)) {
                globalResult.status = UnpackerResult.STATUS_ERROR;
                globalResult.errText = "Failed to save coin #" + cc.sn;
                return;
            }
            
            File f = new File(cc.originalFile);
            if (!f.exists())
                continue;
            
            AppCore.moveToImported(cc.originalFile, user);             
        }

        globalResult.status = UnpackerResult.STATUS_FINISHED;
    }


    public boolean saveCoin(Asset cc) {
        String fileName = cc.getFileName();
        String json = cc.getJson();
        String path;

        path = AppCore.getUserDir(Config.DIR_SUSPECT, user) + File.separator + fileName;
        

        logger.info(ltag, "Saving " + path + ": " + json);
        File f = new File(path);
        if (f.exists()) {
            logger.info(ltag, "File " + path + " already exists. Deleting the old version");
            AppCore.deleteFile(path);
        }

        if (!AppCore.saveFile(path, json)) {
            logger.error(ltag, "Failed to save file: " + fileName);
            return false;
        }

        return true;
    }

    public boolean doUnpackJpeg(String fileName) {
        logger.info(ltag, "Unpacking jpeg");

        FileInputStream fis;
        byte[] jpegHeader = new byte[455];
        String data;
        Asset cc;

        try {
            fis = new FileInputStream(fileName);
            fis.read(jpegHeader);
            data = toHexadecimal(jpegHeader);
            fis.close();
            cc = parseJpeg(data);
            if (cc == null)
                return false;

        } catch (FileNotFoundException e) {
            logger.error(ltag, "File not found: " + e.getMessage());
            return false;
        } catch (IOException e) {
            logger.error(ltag, "Failed to read file: " + e.getMessage());
            return false;
        }

        addCoinToRccs(cc, fileName);

        return true;
    }
    
    
    public boolean doUnpackPng(String fileName) {
        logger.info(ltag, "Unpacking png");

        Asset[] ccs;
        
        byte[] bytes = AppCore.loadFileToBytes(fileName);
        if (bytes == null) {
            logger.error(ltag, "Failed to load file " + fileName);
            return false;
        }

        int idx = AppCore.basicPngChecks(bytes);
        if (idx == -1) {
            logger.error(ltag, "PNG is corrupted");
            return false;
        }

        int i = 0;
        long length;
                        
        while (true) {
            length = AppCore.getUint32(bytes, idx + 4 + i);
            if (length == 0) {
                if (idx + 4 + i + 12 > bytes.length) {
                    logger.error(ltag, "CloudCoin was not found");
                    return false;
                }
            }

            StringBuilder sb = new StringBuilder();
            sb.append(Character.toChars(bytes[idx + 4 + i + 4]));
            sb.append(Character.toChars(bytes[idx + 4 + i + 5]));
            sb.append(Character.toChars(bytes[idx + 4 + i + 6]));
            sb.append(Character.toChars(bytes[idx + 4 + i + 7]));
            String signature = sb.toString();

            logger.debug(ltag, "sig " + signature);
            if (signature.equals("cLDc")) {
                long crcSig = AppCore.getUint32(bytes, idx + 4 + i + 8 + (int) length);
                long calcCrc = AppCore.crc32(bytes, idx + 4 + i + 4, (int)(length + 4));

                if (crcSig != calcCrc) {
                    logger.error(ltag, "Invalid CRC32");
                    return false;
                }

                break;
            }

            i += length + 12;
            if (i > bytes.length) {
                logger.error(ltag, "CloudCoin was not found");
                return false;
            }
        }
        
        byte[] nbytes =  Arrays.copyOfRange(bytes, idx + 4 + i + 8, idx + 4 + i + 8 + (int)length);
        String sdata = new String(nbytes);

        logger.debug(ltag, "Extracted coin. Length: " + sdata.length());     
        ccs = parseStack(sdata);
        if (ccs == null)
            return false;

        for (i = 0; i < ccs.length; i++) {
            addCoinToRccs(ccs[i], fileName);
        }

        return true;
    }
    

    public boolean doUnpackStack(String fileName) {
        logger.info(ltag, "Unpacking stack");

        Asset[] ccs;

        String data = AppCore.loadFile(fileName);
        if (data == null) {
            logger.error(ltag, "Failed to load stack: " + fileName);
            return false;
        }

        ccs = parseStack(data);
        if (ccs == null)
            return false;

        for (int i = 0; i < ccs.length; i++) {
            addCoinToRccs(ccs[i], fileName);
        }

        return true;
    }

    private String toHexadecimal(byte[] digest) {
        String hash = "";

        for (byte aux : digest) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1) hash += "0";
            hash += Integer.toHexString(b);
        }

        return hash;
    }

    private Asset parseJpeg(String data) {

        int startAn = 40;
        int endAn = 72;
        String[] ans = new String[RAIDA.TOTAL_RAIDA_COUNT];
        String aoid, ed;
        int nn, sn;

        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            ans[i] = data.substring(startAn + (i * 32), endAn + (i * 32));
        }

        aoid = pownHexToString(data.substring(840, 895));
        ed = expirationDateHexToString(data.substring(900, 902));
        try {
            nn = Integer.parseInt(data.substring(902, 904), 16);
            sn = Integer.parseInt(data.substring(904, 910), 16);
        } catch (NumberFormatException e) {
            logger.error(ltag, "Failed to parse numbers: " + e.getMessage());
            return null;
        }

        Asset cc = new Asset(nn, sn, ans, ed, new String[] { aoid }, Config.DEFAULT_TAG);

        return cc;
    }

    private Asset[] parseStack(String data) {
        JSONArray incomeJsonArray;

        Asset[] ccs;
        Asset cc;

        int sn, nn;

        try {
            JSONObject o = new JSONObject(data);
            incomeJsonArray = o.getJSONArray("celebrium");

            ccs = new Asset[incomeJsonArray.length()];
            for (int i = 0; i < incomeJsonArray.length(); i++) {
                JSONObject childJSONObject = incomeJsonArray.getJSONObject(i);

                sn = childJSONObject.getInt("sn");
                nn = childJSONObject.getInt("nn");

                JSONArray ans = childJSONObject.getJSONArray("an");
                String[] strAns = toStringArray(ans);
                String ed = childJSONObject.optString("ed");
                JSONArray aoidJson = childJSONObject.optJSONArray("aoid");
                String[] strAoid = toStringArray(aoidJson);

                cc = new Asset(nn, sn, strAns, ed, strAoid, Config.DEFAULT_TAG);
                ccs[i] = cc;
            }
        } catch (JSONException e) {
            logger.error(ltag, "Failed to parse stack: " + e.getMessage());
            return null;
        }

        return ccs;
    }

    


    private String pownHexToString(String hexString) {
        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0, j = hexString.length(); i < j; i++) {
            if ('0' == hexString.charAt(i))
                stringBuilder.append('p');
            else if ('1' == hexString.charAt(i))
                stringBuilder.append('9');
            else if ('2' == hexString.charAt(i))
                stringBuilder.append('n');
            else if ('E' == hexString.charAt(i))
                stringBuilder.append('e');
            else if ('F' == hexString.charAt(i))
                stringBuilder.append('f');
        }

        return stringBuilder.toString();
    }

    private String expirationDateHexToString(String edHex) {
        int monthsAfterZero = Integer.valueOf(edHex, 16);

        Date date;
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
            date = sdf.parse("13-08-2016");
        } catch (ParseException e) {
            return "08-2016";
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        cal.add(Calendar.MONTH, monthsAfterZero);
        //LocalDate zeroDate = LocalDate.of(2016, 8, 13);
        //LocalDate ed = zeroDate.plusMonths(monthsAfterZero);

        //return ed.getMonthValue() + "-" + ed.getYear();

        int m = cal.get(Calendar.MONTH) + 1;

        return m + "-" + cal.get(Calendar.YEAR);
    }

    private String[] toStringArray(JSONArray jsonArray) {
        if (jsonArray == null)
            return null;

        String[] arr = new String[jsonArray.length()];
        for (int i = 0; i < jsonArray.length(); i++) {
            arr[i] = jsonArray.optString(i);
        }

        return arr;
    }
}
