package assetwallet.core;


import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.zip.CRC32;
import java.util.zip.Checksum;
import javax.imageio.ImageIO;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AppCore {

    static private String ltag = "AppCore";

    static private File rootPath;
    static private GLogger logger;

    static private ExecutorService service;
    
    static public String raidaErrText = "Cannot Connect to the RAIDA. "
            + "Check that local routers are not blocking your connection.";

    static public boolean createDirectory(String dirName) {
        String idPath;

        idPath = rootPath + File.separator + dirName;
        
        File idPathFile = new File(idPath);
        if (idPathFile.exists())
            return true;
        
        logger.info(ltag, "Creating " + idPath);
        if (!idPathFile.mkdirs()) {
            logger.error(ltag, "Can not create directory " + dirName);
            return false;
        }

        return true;
    }

    static public void createDirectoryPath(String path) {
        File idPathFile = new File(path);
        if (idPathFile.exists())
            return;
        
        if (!idPathFile.mkdirs()) {
            logger.error(ltag, "Can not create directory " + path);
            return;
        }
    }
   
    static public boolean initFolders(File path, GLogger logger) throws Exception {
        rootPath = path;
        AppCore.logger = logger;

        if (!createDirectory(Config.DIR_ROOT))
            return false;
        
        rootPath = new File(path, Config.DIR_ROOT);

        if (!createDirectory(Config.DIR_TEMPLATES))
            return false;
        
        if (!createDirectory(Config.DIR_ACCOUNTS))
            return false;
        
        if (!createDirectory(Config.DIR_MAIN_LOGS))
            return false;
        
        if (!createDirectory(Config.DIR_MAIN_TRASH))
            return false;
        
        if (!createDirectory(Config.DIR_BACKUPS))
            return false;

        
        return true;
    }
   
    static public void initUserFolders(String user) throws Exception {
        String[] folders = new String[]{
            Config.DIR_BANK,
            Config.DIR_COUNTERFEIT,
            Config.DIR_DEPOSIT,
            Config.DIR_DETECTED,
            Config.DIR_EXPORT,
            Config.DIR_FRACKED,
            Config.DIR_GALLERY,
            Config.DIR_IMPORT,
            Config.DIR_IMPORTED,
            Config.DIR_LOGS,
            Config.DIR_LOST,
            Config.DIR_PREDETECT,
            Config.DIR_RECEIPTS,
            Config.DIR_SENT,
            Config.DIR_SUSPECT,
            Config.DIR_TRASH
        };

        createDirectory(Config.DIR_ACCOUNTS + File.separator + user);

        for (String dir : folders) {
            createDirectory(Config.DIR_ACCOUNTS + File.separator + user + File.separator + dir);
        }
    }

    static public String getRootPath() {
       return rootPath.toString();
    }

   static public String getBackupDir() {
       File f = new File(rootPath, Config.DIR_BACKUPS);
       
       return f.toString();
   }
   
   static public String getTemplateDir() {
       File f = new File(rootPath, Config.DIR_TEMPLATES);
       
       return f.toString();
   }
   
   static public String getMainTrashDir() {
       File f = new File(rootPath, Config.DIR_MAIN_TRASH);
       
       return f.toString();
   }
    
   static public void initPool() {
       service = Executors.newFixedThreadPool(Config.THREAD_POOL_SIZE);
   }

   static public void shutDownPool() {
       service.shutdown();
   }

   static public ExecutorService getServiceExecutor() {
       return service;
   }

   static public String getLogDir() {
       File f;

       f = new File(rootPath, Config.DIR_MAIN_LOGS);

       return f.toString();
   }

   static public String getPrivateLogDir(String user) {
       File f;

       f = new File(rootPath, Config.DIR_ACCOUNTS);
       f = new File(f, user);
       f = new File(f, Config.DIR_LOGS);

       return f.toString();
   }

   static public String getUserDir(String folder, String user) {
        File f;

        f = new File(rootPath, Config.DIR_ACCOUNTS);
        f = new File(f, user);
        f = new File(f, folder);

        return f.toString();
   }

   static public String getRootUserDir(String user) {
       File f;

       f = new File(rootPath, Config.DIR_ACCOUNTS);
       f = new File(f, user);
        
       return f.toString();
   }
   
   static public String formatNumber(int number) {
       NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
       DecimalFormat formatter = (DecimalFormat) nf;
       formatter.applyPattern("#,###,###");

       return formatter.format(number);
   }

   static public int getTotal(int[] counters) {
       return counters[Config.IDX_1] + counters[Config.IDX_5] * 5 +
                counters[Config.IDX_25] * 25 + counters[Config.IDX_100] * 100 +
                counters[Config.IDX_250] * 250;
    }

    static public int[] getDenominations() {
        int[] denominations = {1, 5, 25, 100, 250};

        return denominations;
    }
    
    static public boolean moveToFolderNewName(String fileName, String folder, String user, String newFileName) {
        logger.info(ltag, "Moving to " + folder + " -> " + fileName + " new " + newFileName);
        
        try {
            File fsource = new File(fileName);
            String target;
            if (user != null)
                target = AppCore.getUserDir(folder, user) + File.separator + newFileName;
            else 
                target = folder + File.separator + newFileName;
            
            File ftarget = new File(target);
            if (!fsource.renameTo(ftarget)) {
                logger.error(ltag, "Failed to rename file " + fileName + " to " + ftarget.getAbsolutePath());
                return false;
            }
        } catch (Exception e) {
            logger.error(ltag, "Failed to move file: " + e.getMessage());
            return false;
        }
        
        return true;
        
    }

    static public boolean moveToFolderNoTs(String fileName, String folder, String user) {
        return moveToFolderNoTs(fileName, folder, user, false);
    }
    
    static public boolean moveToFolderNoTs(String fileName, String folder, String user, boolean isOverwrite) {
        logger.info(ltag, "Moving no Ts to " + folder + " -> " + fileName);

        try {
            File fsource = new File(fileName);
            String target = AppCore.getUserDir(folder, user) + File.separator + fsource.getName();

            File ftarget = new File(target);
            if (ftarget.exists()) {
                if (isOverwrite) {
                    logger.info(ltag, "Overwriting file: " + target);
                    ftarget.delete();
                } else {
                    logger.error(ltag, "File exists. Leaving " + fileName);
                    return false;
                }
            }

            if (!fsource.renameTo(ftarget)) {
                logger.error(ltag, "Failed to rename file " + fileName);
                return false;
            }
        } catch (Exception e) {
            logger.error(ltag, "Failed to move file: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    static public boolean renameFile(String oldFile, String newFile) {
        logger.info(ltag, "Renaming " + oldFile + " -> " + newFile);
        try {
            File fsource = new File(oldFile);
            File ftarget = new File(newFile);
            if (ftarget.exists()) {
                logger.error(ltag, "Target File exists. Leaving");
                return false;
            }

            if (!fsource.renameTo(ftarget)) {
                logger.error(ltag, "Failed to rename file");
                return false;
            }
        } catch (Exception e) {
            logger.error(ltag, "Failed to move file: " + e.getMessage());
            return false;
        }
        
        return true;
    }

    static public void moveToTrash(String fileName, String user) {
        moveToFolderNoTs(fileName, Config.DIR_TRASH, user, true);
    }

    static public void moveToBank(String fileName, String user) { 
        moveToFolderNoTs(fileName, Config.DIR_BANK, user); 
    }

    static public void moveToImported(String fileName, String user) {
        moveToFolderNoTs(fileName, Config.DIR_IMPORTED, user, true);
    }

    static public boolean copyFile(InputStream is, String fdst) {
        File dest = new File(fdst);
        OutputStream os = null;
        
        try {
            os = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            logger.error(ltag, "Failed to copy file: " + e.getMessage());
            return false;
        } finally {
            try {
                if (is != null)
                    is.close();

                if (os != null)
                    os.close();

            } catch (IOException e) {
                logger.error(ltag, "Failed to copy file: " + e.getMessage());
            }
        }
        
        return true;
    }
    
    static public boolean copyFile(String fsrc, String fdst) {
        File source = new File(fsrc);
        File dest = new File(fdst);
        InputStream is = null;
        OutputStream os = null;
        
        logger.debug(ltag, "Copy " + fsrc + " to " + fdst);
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } catch (IOException e) {
            logger.error(ltag, "Failed to copy file: " + e.getMessage());
            return false;
        } finally {
            try {
                if (is != null)
                    is.close();

                if (os != null)
                    os.close();

            } catch (IOException e) {
                logger.error(ltag, "Failed to finally copy file: " + e.getMessage());
            }
        }

        return true;
    }

    static public String loadFile(String fileName) {
        String jsonData = "";
        BufferedReader br = null;
        try {
            byte[] encoded = Files.readAllBytes(Paths.get(fileName));
            jsonData = new String(encoded);
            
        } catch (IOException e) {
            logger.error(ltag, "Failed to load file: " + e.getMessage());
            return null;
        } finally {
            try {
                if (br != null)
                    br.close();
            } catch (IOException e) {
                logger.error(ltag, "Failed to finally load file: " + e.getMessage());
                return null;
            }
        }

        return jsonData.toString();
    }

    static public byte[] loadFileToBytes(String path) {
        byte[] getBytes = {};
        try {
            File file = new File(path);
            getBytes = new byte[(int) file.length()];
            InputStream is = new FileInputStream(file);
            is.read(getBytes);
            is.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return getBytes;
    }

    static public String loadFileFromInputStream(InputStream is) {
        StringBuilder out = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line;
            while ((line = reader.readLine()) != null) {
                out.append(line);
            }          
            reader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return out.toString();
    }
    
    static public boolean saveFile(String path, String data) {
        return saveFileAppend(path, data, false);
    }
    
    static public boolean saveFileAppend(String path, String data, boolean isAppend) {
        File f = new File(path);
        if (f.exists() && !isAppend) {
            logger.error(ltag, "File " + path + " already exists");
            return false;
        }

        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(path, isAppend));
            writer.write(data);
        } catch (IOException e){
            logger.error(ltag, "Failed to write file: " + e.getMessage());
            return false;
        } finally {
            try{
                if (writer != null)
                    writer.close();
            } catch (IOException e){
                logger.error(ltag, "Failed to close buffered writer");
                return false;
            }
        }

        return true;
    }

    static public boolean saveFileFromBytes(String path, byte[] bytes) {
       try {
           File file = new File(path);
           if (file.exists()) {
               logger.error(ltag, "File exists: " + path);
               return false;
           }

           FileOutputStream fos = new FileOutputStream(file);
           fos.write(bytes);
           fos.close();
       } catch (IOException e) {
           logger.error(ltag, "Failed to write file: " + e.getMessage());
           return false;
       }

       return true;
    }

    static public void deleteFile(String path) {
        File f = new File(path);

        logger.debug(ltag, "Deleting " + path);
        f.delete();
    }

    static public int getFilesCount(String dir, String user) {
        String path = getUserDir(dir, user);
        File rFile;
        int rv;

        try {
            rFile = new File(path);
            //rv = rFile.listFiles().length;
           
            rv = 0;
            for (File file: rFile.listFiles()) {
                if (file.isDirectory())
                    continue;
            
                if (!AppCore.hasCoinExtension(file))
                    continue;
                
                rv++;
            }               
        } catch (Exception e) {
           logger.error(ltag, "Failed to read directory: " + e.getMessage());
           return 0;
        }

        return rv;
    }

    static public String getMD5(String data) {
        byte[] bytesOfMessage = data.getBytes();
        byte[] digest;

        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            digest = md.digest(bytesOfMessage);
        } catch (NoSuchAlgorithmException e) {
            logger.error(ltag, "No such algorithm MD5: " + e.getMessage());
            return null;
        } catch (Exception e) {
            logger.error(ltag, "MD5 error: " + e.getMessage());
            return null;
        }

        return toHexString(digest);
    }

    static public String toHexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString();
    }

    static public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) +
                    Character.digit(s.charAt(i+1), 16));
        }

        return data;
    }


    static public String generateHex() {
        String AB = "0123456789ABCDEF";

        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder(32);
        for (int i = 0; i < 32; i++)
            sb.append(AB.charAt(rnd.nextInt(AB.length())));

        return sb.toString();
    }


    public static String padString(String string, int length, char padding) {
        return String.format("%" + length + "s", string).replace(' ', padding);
    }

    public static int charCount(String pown, char character) {
        return pown.length() - pown.replace(Character.toString(character), "").length();
    }
    
    public static String[] getFilesInDir(String dir, String user) {
        String path;
        if (user != null)
            path = AppCore.getUserDir(dir, user);
        else
            path = dir;
        
        String[] rv;
        int c = 0;

        File dirObj = new File(path);
        if (!dirObj.exists()) {
            return new String[0];
        }
        
        for (File file: dirObj.listFiles()) {
            if (file.isDirectory())
                continue;
            
            c++;
        }
        
        rv = new String[c];
        c = 0;
        for (File file: dirObj.listFiles()) {
            if (file.isDirectory())
                continue;
            
            rv[c++] = file.getName();
        }
        
        return rv;       
    }
    
    public static String[] getDirs() {
        String[] rv;
        int c = 0;

        File dirObj = new File(rootPath + File.separator + Config.DIR_ACCOUNTS);
        for (File file: dirObj.listFiles()) {
            if (!file.isDirectory())
                continue;
            
            c++;
        }
        
        rv = new String[c];
        c = 0;
        for (File file: dirObj.listFiles()) {
            if (!file.isDirectory())
                continue;
            
            rv[c++] = file.getName();
        }
        
        return rv;       
    }
    
    
    public static void copyTemplatesFromJar() {
        int d;
        String templateDir;

        String[] templates = new String[] {
            "jpeg1.jpg",
            "jpeg5.jpg",
            "jpeg25.jpg",
            "jpeg100.jpg",
            "jpeg250.jpg",
            Config.PNG_TEMPLATE_NAME
        };
        
	templateDir = AppCore.rootPath + File.separator + Config.DIR_TEMPLATES;

        for (int i = 0; i < templates.length; i++) {
            String fileName = templates[i];
            
            URL u = AppCore.class.getClassLoader().getResource("resources/" + fileName);
            if (u == null) {
                logger.debug(ltag, "Failed to find resource " + fileName);
                continue;
            }
            
            String url;
            try {
                url = URLDecoder.decode(u.toString(), "UTF-8");
            }  catch (UnsupportedEncodingException e) {
                logger.error(ltag, "Failed to decode url");
                return;
            }

            int bang = url.indexOf("!");
            String JAR_URI_PREFIX = "jar:file:";
            JarFile jf;
                
            logger.debug(ltag, "jurl " + url);
            try {
                if (url.startsWith(JAR_URI_PREFIX) && bang != -1) {
                    jf = new JarFile(url.substring(JAR_URI_PREFIX.length(), bang)) ;
                } else if (url.startsWith("file:/")) {
                    String file = url.substring(6, url.length());
                    logger.debug(ltag, "template file " + file);
                    String dst = templateDir + File.separator + fileName;
                    
                    File f = new File(dst);
                    if (f.exists())
                        continue;
                    
                    AppCore.copyFile(file, dst);                    
                    continue;
                } else {
                    logger.error(ltag, "Invalid jar");
                    return;
                }
                
                for (Enumeration<JarEntry> entries = jf.entries(); entries.hasMoreElements();) {
                    JarEntry entry = entries.nextElement();

                    if (entry.getName().equals("resources/" + fileName)) {
                        InputStream in = jf.getInputStream(entry);
                        String dst = templateDir + File.separator + fileName;
                        
                        File f = new File(dst);
                        if (f.exists())
                            continue;
                        
                        AppCore.copyFile(in, dst);
                    }
                }
            } catch (IOException e) {
                logger.error(ltag, "Failed to copy templates: " + e.getMessage());
                return ;
            }                      
        }
    }
    
    public static String getCurrentDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-dd hh:mma");
	Date date = new Date();
        
        String strDate = dateFormat.format(date).replaceAll("AM$", "am");
        strDate = strDate.replaceAll("PM$", "pm");
        
        return strDate;
    }
    
    
    public static String getCurrentBackupDir(String broot, String user) {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-d h-mma", Locale.US);
        Date date = new Date();
        
        String dsuffix = dateFormat.format(date);
        
        String bdir = broot + File.separator + user + File.separator + "CloudCoinBackup-" + dsuffix;
        
        return bdir;
    }
    
    
    public static String getDate(String ts) {
        int lts;
        
        try {
            lts = Integer.parseInt(ts);
        } catch (NumberFormatException e) {
            logger.error(ltag, "Failed to parse ts: " + ts);
            return "";
        }
        
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MMM-d h-mma");
        Date date = new Date((long) lts * 1000);
        
        String dateTime = dateFormat.format(date);
        
      
        return dateTime;
    }
    
    
    public static Asset getCoin(String path) {
        Asset cc;
        try {
            cc = new Asset(path);
        } catch (JSONException e) {
            logger.error(ltag, "Failed to parse coin: " + path + " error: " + e.getMessage());
            return null;
        }
        
        return cc;
    }
    
    public static Asset findCoinBySN(String dir, String user, int sn) {
        String dirPath = AppCore.getUserDir(dir, user);
        logger.debug(ltag, "Looking for sn " + sn + " into dir: " + dirPath);    
        Asset cc;

        File dirObj = new File(dirPath);
        if (dirObj.listFiles() == null) 
            return null;
        
        for (File file: dirObj.listFiles()) {
            if (file.isDirectory())
                continue;

            if (!AppCore.hasCoinExtension(file))
                continue;
            
            try {
                cc = new Asset(file.toString());
            } catch (JSONException e) {
                logger.error(ltag, "Failed to parse coin: " + file.toString() +
                        " error: " + e.getMessage());

                continue;
            }
            
            if (cc.sn == sn)
                return cc;
        }
        
        return null;
    }
    
    
    
    static public String getLogPath() {
        return getLogDir() + File.separator + Config.MAIN_LOG_FILENAME;
    }
    
    static public boolean moveFolderToTrash(String folder) {
        String path = AppCore.getRootUserDir(folder);
        
        File fsrc = new File(path);
        if (!fsrc.exists()) {
            logger.error(ltag, "Path " + path + " doesn't exist");
            return false;
        }
        
        String mainTrashDir = AppCore.getMainTrashDir() + File.separator + 
                folder + "-" + System.currentTimeMillis();
        
        File fdst = new File(mainTrashDir);
        
        logger.debug(ltag, "Deleting dir " + path + " to " + mainTrashDir);
        try {
            Files.move(fsrc.toPath(), fdst.toPath(),  StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error(ltag, "Failed to move dir " + e.getMessage());
            return false;
        }
        return true;
    }
    
    static public void moveFolderContentsToTrash(String folder, String user) {
        String path = AppCore.getUserDir(folder, user);
        
        File dirObj = new File(path);
        if (!dirObj.exists()) {
            logger.error(ltag, "Path " + path + " doesn't exist");
            return;
        }
        
        for (File file: dirObj.listFiles()) {
            AppCore.moveToTrash(file.getAbsolutePath(), user);
        }       
    }
 
    public static int maxCoinsWorkAround(int maxCoins) {
        String javaVersion = System.getProperty("java.version");
        
        logger.debug(ltag, "Java version: " + javaVersion);
        
        if (javaVersion.equals("1.8.0_211")) {
            logger.debug(ltag, "MaxCoins WorkAround applied");
            return 20;
        }

        return maxCoins;
    }
    
    
    public static void logSystemInfo(String version) {
        logger.info(ltag, "CloudCoin Wallet v." + version);
        
        String javaVersion = System.getProperty("java.runtime.version");
        String javaName = System.getProperty("java.runtime.name");
        
        String osName = System.getProperty("os.name");
        
        int cpus = Runtime.getRuntime().availableProcessors();
        long totalMemory =  Runtime.getRuntime().totalMemory();
        long freeMemory =  Runtime.getRuntime().freeMemory();
        
        totalMemory /= (1024 * 1024);
        freeMemory /= (1024 * 1024);
        
        logger.info(ltag, "JAVA " + javaName + " " + javaVersion);
        logger.info(ltag, osName);
        logger.info(ltag, "CPUS: " + cpus + " Memory for JVM, (free/avail): " + freeMemory + "/" + totalMemory + " MB");
        
       
    }
    
    public static String calcCoinsFromFilenames(ArrayList<String> files) {
        int total = 0;
        
        for (String filepath : files) {
            File f = new File(filepath);
            String filename = f.getName();
            
            String[] parts = filename.split("\\.");
            if (parts.length < 3)
                return "?";
            
            int ltotal;
            try {
                ltotal = Integer.parseInt(parts[0]);
            } catch (NumberFormatException e) {
                return "?";
            }
           
            String identifier = parts[1].toLowerCase();
            if (!identifier.equals("cloudcoin") && !identifier.equals("cloudcoins"))
                return "?";
                     
            total += ltotal;
        }
        
        String totalCoins = AppCore.formatNumber(total);
        
        return totalCoins;
    }
    
    public static String getMS(int ms) {
        double dms = (double) ms / 1000;
        String s = String.format(Locale.US, "%.2f", dms);

        if (s.charAt(0) == '0')
            s = s.substring(1, s.length());

        return s + " sec";
    }

    public static String getRAIDAString(int idx) {
        String sidx;

        if (idx < 10)
            sidx = "0" + idx;
        else
            sidx = "" + idx;

        String[] countries = {
            "Australia",
            "Macedonia",
            "Philippines",
            "Serbia",
            "Switzerland",
            "South Korea",
            "Japan",
            "UK",
            "India",
            "India",
            "Germany",
            "USA",
            "India",
            "Taiwan",
            "Russia",
            "Russia",
            "UK",
            "Singapore",
            "USA",
            "Argentina",
            "France",
            "India",
            "USA",
            "Germany",
            "Canada"
        };

        if (idx > countries.length) {
            return sidx + " RAIDA";
        }

        return sidx + " " + countries[idx];

    }

    public static int getErrorCount(Asset cc) {
        int error = 0;

        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (cc.getDetectStatus(i) == Asset.STATUS_ERROR || cc.getDetectStatus(i) == Asset.STATUS_UNTRIED)
                error++;
        }
        
        cc.setPownStringFromDetectStatus();
        logger.debug(ltag, "Error count " + error + " cc " + cc.sn + " pown=" + cc.getPownString());
        
        return error;
    }
    
    public static int getPassedCount(Asset cc) {
        int passed = 0;

        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (cc.getDetectStatus(i) == Asset.STATUS_PASS)
                passed++;
        }
        
        cc.setPownStringFromDetectStatus();
        logger.debug(ltag, "Passed count " + passed + " cc " + cc.sn + " pown=" + cc.getPownString());
        
        return passed;
    }
    
    public static String getMailConfigFilename() {
        return rootPath + File.separator + Config.MAIL_CONFIG_FILENAME;
    }
    
    public static void readConfig() {
        String globalConfigFilename = rootPath + File.separator + Config.GLOBAL_CONFIG_FILENAME;
        
        logger.debug(ltag, "Reading config file " + globalConfigFilename);
        
        String data = AppCore.loadFile(globalConfigFilename);
        if (data == null) {
            logger.debug(ltag, "Failed to read config file. Maybe it doesn't exist yet");
            return;
        }
        
        String os;
        int oi;
        try {
            JSONObject o = new JSONObject(data);
                    
            oi = o.optInt("echo_timeout", -1);
            if (oi != -1) {
                logger.debug(ltag, "Echo timeout: " + oi);
                Config.ECHO_TIMEOUT = oi;
            }
            
            oi = o.optInt("read_timeout", -1);
            if (oi != -1) {
                logger.debug(ltag, "Read timeout: " + oi);
                Config.READ_TIMEOUT = oi;
            }
            
            oi = o.optInt("detect_timeout", -1);
            if (oi != -1) {
                logger.debug(ltag, "Detect timeout: " + oi);
                Config.MULTI_DETECT_TIMEOUT = oi;
            }
            
            oi = o.optInt("fix_timeout", -1);
            if (oi != -1) {
                logger.debug(ltag, "Fix timeout: " + oi);
                Config.FIX_FRACKED_TIMEOUT = oi;
            }
            
            oi = o.optInt("max_coins", -1);
            if (oi != -1) {
                logger.debug(ltag, "Max coins: " + oi);
                Config.DEFAULT_MAX_COINS_MULTIDETECT = oi;
            }
            
            oi = o.optInt("inited", -1);
            if (oi != -1) {
                logger.debug(ltag, "Inited: " + oi);
                Config.CONFIG_INITED = oi;
            }
            
            os = o.optString("export_dir");
            if (os != null) {
                logger.debug(ltag, "Export dir: " + os);
                Config.DEFAULT_EXPORT_DIR = os;
            }

            os = o.optString("deposit_dir");
            if (os != null) {
                logger.debug(ltag, "Deposit dir: " + os);
                
                Config.DEFAULT_DEPOSIT_DIR = os;
            }
        } catch (JSONException e) {
            logger.error(ltag, "Failed to parse config file: " + e.getMessage());
            return;
        }
        
    }
    
    public static boolean writeConfig() {
        String globalConfigFilename = rootPath + File.separator + Config.GLOBAL_CONFIG_FILENAME;
        
        logger.debug(ltag, "Saving config " + globalConfigFilename);
        
        String edir = Config.DEFAULT_EXPORT_DIR.replace("\"", "\\\"").replace("\\", "\\\\");
        String ddir = Config.DEFAULT_DEPOSIT_DIR.replace("\"", "\\\"").replace("\\", "\\\\");
        String data = "{\"echo_timeout\":" + Config.ECHO_TIMEOUT + ", "
                + "\"detect_timeout\": " + Config.MULTI_DETECT_TIMEOUT + ", "
                + "\"read_timeout\": " + Config.READ_TIMEOUT + ", "
                + "\"fix_timeout\": " + Config.FIX_FRACKED_TIMEOUT + ", "
                + "\"max_coins\": " + Config.DEFAULT_MAX_COINS_MULTIDETECT + ", "
                + "\"export_dir\": \"" + edir + "\", "
                + "\"deposit_dir\": \"" + ddir + "\", "
                + "\"inited\" : \"1\""
                + "}";
        
        File f = new File(globalConfigFilename);
        f.delete();
        
        return AppCore.saveFile(globalConfigFilename, data);
    }
 
    public static String maskStr(String key, String data) {
        String result = data.replaceAll(key + "([A-Fa-f0-9]{28})", key + "***");
        return result;
    }
    
    public static int staleFiles(String user) {
        int cnt;
        
        cnt = AppCore.getFilesCount(Config.DIR_IMPORT, user);
        if (cnt != 0)
            return 1;
        
        cnt = AppCore.getFilesCount(Config.DIR_SUSPECT, user);
        if (cnt != 0) 
            return 2;

        cnt = AppCore.getFilesCount(Config.DIR_DETECTED, user);
        if (cnt != 0)
            return 3;
      
        return 0;
    }

    
    public static boolean hasCoinExtension(File file) {
        String f = file.toString().toLowerCase();
        if (f.endsWith(".stack") || f.endsWith(".jpg") || f.endsWith(".jpeg") || f.endsWith(".png"))
            return true;
        
        logger.debug(ltag, "Ignoring invalid extension " + file.toString());
        
        return false;        
    }
 
    public static Asset[] getCoinsInDirs(String dir0, String dir1, String user) {
        String[] dirs = new String[2];
        ArrayList<Asset> ccs = new ArrayList<Asset>();
        
        dirs[0] = AppCore.getUserDir(dir0, user);
        dirs[1] = AppCore.getUserDir(dir1, user);
        
        int c = 0;
        for (int i = 0; i < dirs.length; i++) {
            File dirObj = new File(dirs[i]);
            if (!dirObj.exists()) {
                logger.debug(ltag, "Directory " + dirs[i] + " doesn't exist");
                continue;
            }

            for (File file: dirObj.listFiles()) {
                if (file.isDirectory())
                    continue;
            
                if (!AppCore.hasCoinExtension(file))
                    continue;
               
                Asset cc;
                try {
                    cc = new Asset(file.getAbsolutePath());
                } catch (JSONException e) {
                    continue;
                }
                    
                ccs.add(cc);
            }
        }
            
        Asset[] assets = ccs.toArray(new Asset[ccs.size()]);

        return assets;     
    }
    
    
    public static Map<String, Properties> parseINI(Reader reader) throws IOException {
        Map<String, Properties> result = new HashMap();
        new Properties() {
            private Properties section;

            @Override
            public Object put(Object key, Object value) {
                String header = (((String) key) + " " + value).trim();
                if (header.startsWith("[") && header.endsWith("]")) {
                    return result.put(header.substring(1, header.length() - 1), 
                        section = new Properties());
                }
                else {
                    return section.put(key, value);
                }
            }
        }.load(reader);
        return result;
    }
    
    public static int basicPngChecks(byte[] bytes) {    
        if (bytes[0] != 0x89 && bytes[1] != 0x50 && bytes[2] != 0x4e && bytes[3] != 0x45 
                && bytes[4] != 0x0d && bytes[5] != 0x0a && bytes[6] != 0x1a && bytes[7] != 0x0a) {
            logger.error(ltag, "Invalid PNG signature");
            return -1;
        }

        long chunkLength = AppCore.getUint32(bytes, 8);
        long headerSig = AppCore.getUint32(bytes, 12);
        if (headerSig != 0x49484452) {
            logger.error(ltag, "Invalid PNG header");
            return -1;
        }
   
        int idx = (int)(16 + chunkLength);        
        long crcSig = AppCore.getUint32(bytes, idx);
        long calcCrc = AppCore.crc32(bytes, 12, (int)(chunkLength + 4));
        if (crcSig != calcCrc) {
            logger.error(ltag, "Invalid PNG Crc32 checksum");
            return -1;
        }
               
        
        return idx;
    }
    
    public static long getUint32(byte[] bytes, int offset) {
        byte[] nbytes = Arrays.copyOfRange(bytes, offset, offset + 4);
        ByteBuffer buffer = ByteBuffer.allocate(8).put(new byte[]{0, 0, 0, 0}).put(nbytes);
        buffer.position(0);

        return buffer.getLong();
    }
    
    public static void setUint32(byte[] data, int offset, long value) {
        byte[] bytes = new byte[8];
        ByteBuffer.wrap(bytes).putLong(value);

        data[offset] = bytes[4];
        data[offset + 1] = bytes[5];
        data[offset + 2] = bytes[6];
        data[offset + 3] = bytes[7];
        
        return;
    }

    public static long crc32(byte[] data, int offset, int length) {
        byte[] nbytes = Arrays.copyOfRange(data, offset, offset + length);
        Checksum checksum = new CRC32();
        checksum.update(nbytes, 0, nbytes.length);
        long checksumValue = checksum.getValue();
        
        return checksumValue;
    }

    public static int getRaidaMirror(int raidaIdx) {
        if (raidaIdx >= RAIDA.TOTAL_RAIDA_COUNT)
            return -1;
        
        if (raidaIdx == 0)
            return RAIDA.TOTAL_RAIDA_COUNT - 1;
               
        return raidaIdx - 1;
    }
    
    public static int getRaidaMirror2(int raidaIdx) {
        if (raidaIdx >= RAIDA.TOTAL_RAIDA_COUNT)
            return -1;

        if (raidaIdx == 0)
            return RAIDA.TOTAL_RAIDA_COUNT - 2;
        
        if (raidaIdx == 1)
            return RAIDA.TOTAL_RAIDA_COUNT - 1;
        
        return raidaIdx - 2;
    }
    
    public static String getMetaItem(Properties meta, String key) {
        String value = meta.getProperty(key);
        if (value == null)
            return null;
        
        return value.replaceAll("<br>$","");
    }
    
    public static Image getScaledImage(int maxWidth, int maxHeight, byte[] data) {
        BufferedImage bi;
        Image i;
        try {
            bi = ImageIO.read(new ByteArrayInputStream(data));
            i = (Image) bi;        
            int w = bi.getWidth();
            int h = bi.getHeight();
            
            
            double ratio = (double) w / (double) h;
            if (h > maxHeight) {
                h = maxHeight;
                w = (int) (h * ratio);
                if (w > maxWidth) {
                    w = maxWidth;
                    h = (int) (w / ratio);
                }
                
            } else if (w > maxWidth) {
                w = maxWidth;
                h = (int) (w / ratio);
            }

            i = i.getScaledInstance(w, h, Image.SCALE_SMOOTH);
        } catch (IOException e) {
            logger.error(ltag, "Failed to load image");
            return null;
        }
        
        return i;
    }
    
    public static String getExpstring(int number) {
        int zeroes = 0;
        
        if (number < 10)
            zeroes = 4;
        else if (number < 100)
            zeroes = 3;
        else if (number < 1000)
            zeroes = 2;
        else if (number < 10000)
            zeroes = 1;
        else 
            zeroes = 0;
        
        String s = "ECDT";
        for (int i = 0; i < zeroes; i++)
            s += "0";
        
        s += "" + number;
        
        return s;
    }
    
    
}
