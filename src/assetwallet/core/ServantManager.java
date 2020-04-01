/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetwallet.core;

import assetwallet.ProgramState;

import assetwallet.core.Authenticator.Authenticator;
import assetwallet.core.Authenticator.AuthenticatorResult;
import assetwallet.core.Echoer.Echoer;

import assetwallet.core.FrackFixer.FrackFixer;
import assetwallet.core.FrackFixer.FrackFixerResult;
import assetwallet.core.Grader.Grader;
import assetwallet.core.Grader.GraderResult;
import assetwallet.core.LossFixer.LossFixer;
import assetwallet.core.LossFixer.LossFixerResult;

import assetwallet.core.ShowCoins.ShowCoins;

import assetwallet.core.Unpacker.Unpacker;

import assetwallet.core.AppCore;
import assetwallet.core.CallbackInterface;
import assetwallet.core.Asset;
import assetwallet.core.Config;
import assetwallet.core.Exporter.Exporter;

import assetwallet.core.GLogger;
import assetwallet.core.RAIDA;
import assetwallet.core.Servant;
import assetwallet.core.ServantRegistry;

import assetwallet.core.Wallet;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import org.json.JSONException;

/**
 *
 * @author Alexander
 */
public class ServantManager {
    String ltag = "ServantManager";
    ServantRegistry sr;
    GLogger logger;
    String home;
    String user;
    Hashtable<String, Wallet> wallets;
    Hashtable<String, Asset> assets;
 
    public ServantManager(GLogger logger, String home) {
        this.logger = logger;
        this.home = home;
        this.sr = new ServantRegistry();
        this.user = Config.DIR_DEFAULT_USER;
        
    }

    
    public ServantRegistry getSR() {
        return this.sr;
    }
    
    public void changeServantUser(String servant, String wallet) {
        sr.changeServantUser(servant, wallet);
    }
    
    public String getHomeDir() {
        return this.home;
    }
    
    public boolean init() {
        boolean rv; 
        
        AppCore.initPool();
        
        try {
            rv = AppCore.initFolders(new File(home), logger);
            if (!rv) {
                logger.error(ltag, "Failed to create folders");
                return false;
            }
        } catch (Exception e) {
            logger.error(ltag, "Failed to init root dir " + home);
            return false;
        }   
        
        initServants();
        
        return true;
    }
    
    public boolean initServants() {
        sr.registerServants(new String[]{
                "Echoer",
                "Authenticator",
                "ShowCoins",
                "Unpacker",
                "Grader",
                "FrackFixer",
                "Exporter",           
                "LossFixer",
        }, AppCore.getRootPath() + File.separator + user, logger);

        initWallets();

        return true;
    }
    
    public void initWallets() {
        this.wallets = new Hashtable<String, Wallet>();
        
        String[] wallets = AppCore.getDirs();
        for (int i = 0; i < wallets.length; i++) {
            initWallet(wallets[i], "");        
        }
    }
    
    public void setAssets(Asset[] assets) {
        this.assets = new Hashtable<String, Asset>();
        
        for (int i = 0; i < assets.length; i++) {
            this.assets.put("" + assets[i].sn, assets[i]);
        }
    }
    
    public void initCloudWallet(Asset cc, String name) {
        Wallet wobj = new Wallet(name, "", false, "", logger);
        
        wallets.put(name, wobj);   
    }
    
    public void initWallet(String wallet, String password) {
        if (wallets.containsKey(wallet)) 
            return;
        
        logger.debug(ltag, "Initializing wallet " + wallet);
        Authenticator au = (Authenticator) sr.getServant("Authenticator");
        String email = au.getConfigValue("email");
        if (email == null)
            email = "";
            
        
        Wallet wobj = new Wallet(wallet, email, false, "dummy", logger);

        
        wallets.put(wallet, wobj);    
    }
    
    public boolean initUser(String wallet, String email, String password) {
        logger.debug(ltag, "Init user " + wallet);
               
        try {
            AppCore.initUserFolders(wallet);
        } catch (Exception e) {
            logger.error(ltag, "Error: " + e.getMessage());
            return false;
        }
        
        this.user = wallet;
        sr.changeUser(wallet);

        
        if (!email.equals(""))
            sr.getServant("Authenticator").putConfigValue("email", email);

        if (!password.equals("")) {
            sr.getServant("Vaulter").putConfigValue("status", "on");
            sr.getServant("Vaulter").putConfigValue("password", AppCore.getMD5(password));
        }
              
        initWallet(wallet, password);
              
        return true;
    }
    
    public void startExporterService(Asset asset, String dir, CallbackInterface cb) {
        if (sr.isRunning("Exporter")) {
            return;
        }
        
	Exporter e = (Exporter) sr.getServant("Exporter");
	e.launch(Config.TYPE_PNG, asset, Config.TAG_RANDOM, dir, cb);
    }

    public void startEchoService(CallbackInterface cb) {
        if (sr.isRunning("Echoer")) {
            return;
        }
        
	Echoer e = (Echoer) sr.getServant("Echoer");
	e.launch(cb);
    }
    
    public boolean isEchoerFinished() {
        return !sr.isRunning("Echoer");
    }
    
    public void startFrackFixerService(CallbackInterface cb) {
        if (sr.isRunning("FrackFixer"))
            return;
        
        FrackFixer ff = (FrackFixer) sr.getServant("FrackFixer");
	ff.launch(cb);
    }
    
    public void startUnpackerService(CallbackInterface cb) {
        if (sr.isRunning("Unpacker"))
            return;
        
	Unpacker up = (Unpacker) sr.getServant("Unpacker");
	up.launch(cb);
    }
     
    public void startAuthenticatorService(CallbackInterface cb) {
        if (sr.isRunning("Authenticator"))
            return;

	Authenticator at = (Authenticator) sr.getServant("Authenticator");
	at.launch(cb);
    }
    
    public void startAuthenticatorService(Asset cc, CallbackInterface cb) {
        if (sr.isRunning("Authenticator"))
            return;

	Authenticator at = (Authenticator) sr.getServant("Authenticator");
	at.launch(cc, cb);
    }

    public void startGraderService(CallbackInterface cb, ArrayList<Asset> duplicates, String source) {
        if (sr.isRunning("Grader"))
            return;
        
	Grader gd = (Grader) sr.getServant("Grader");
	gd.launch(cb, duplicates, source);
    }

    
    
    public void startShowCoinsService(Asset[] assets, CallbackInterface cb) {
        if (sr.isRunning("ShowCoins"))
            return;
                
	ShowCoins sc = (ShowCoins) sr.getServant("ShowCoins");
	sc.launch(assets, cb);
    }
    
    public void startLossFixerService(CallbackInterface cb) {
        LossFixer l = (LossFixer) sr.getServant("LossFixer");
	l.launch(cb);
    }
    
    public int getRemoteSn(String dstWallet) {
        int sn;
        
        try {
            sn = Integer.parseInt(dstWallet);
        } catch (NumberFormatException e) {
            return 0;
        }
        
        if (sn <= 0)
            return 0;
        
        return sn;
    }
    
    public void cancel(String servantName) {
        Servant s = sr.getServant(servantName);
        if (s == null)
            return;
        
        s.cancel();
    }

   
    
    public Wallet[] getWallets() {
        TreeMap<String, Wallet> sorted = new TreeMap<>(wallets);
        
        int size = sorted.size();
        Collection c = sorted.values();
        Wallet[] ws = new Wallet[size];
        
        int i = 0;
        Iterator itr = c.iterator();
        while (itr.hasNext()) {
            Wallet tw = (Wallet) itr.next();
            ws[i++] = tw;
        }

        
        return ws;
    }
    
    public Wallet getWalletByName(String walletName) {
        Collection c = wallets.values();

        Iterator itr = c.iterator();
        while (itr.hasNext()) {                     
            Wallet tw = (Wallet) itr.next();
            if (tw.getName().equals(walletName)) 
                return tw;            
        }
        
        return null;
    }
    
    public void openTransactions() {
        Collection c = wallets.values();

        Iterator itr = c.iterator();
        while (itr.hasNext()) {
            Wallet w = (Wallet) itr.next();

            if (w.getTotal() != 0)
                w.appendTransaction("Opening Balance", w.getTotal(), "openingbalance");      
        }
    }
    
    public int[] getRAIDAStatuses() {
        Servant e = sr.getServant("Echoer");
        
        e.updateRAIDAStatus();
        
        String[] urls = e.getRAIDA().getRAIDAURLs();
        int[] latencies = e.getRAIDA().getLatencies();
        int[] rv = new int[RAIDA.TOTAL_RAIDA_COUNT];
        
        for (int i = 0; i < RAIDA.TOTAL_RAIDA_COUNT; i++) {
            if (urls[i] == null)
                rv[i] = -1;
            else
                rv[i] = latencies[i];
        }
        
        return rv;
    }
 
    public void resumeAll() {
        sr.resumeAll();
    }
    
    
    public boolean isRAIDAOK() {
        return sr.getServant("Echoer").updateRAIDAStatus();
    }
    


    
}
