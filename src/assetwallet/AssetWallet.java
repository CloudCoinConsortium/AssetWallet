package assetwallet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterException;
import java.io.File;
import java.io.IOException;
import java.util.*;
import javax.imageio.ImageIO;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.MenuItemUI;
import javax.swing.table.DefaultTableCellRenderer;

import assetwallet.FileDrop;
import assetwallet.core.AppCore;
import assetwallet.core.Asset;
import assetwallet.core.AssetUIItem;
import assetwallet.core.Authenticator.AuthenticatorResult;
import assetwallet.core.CallbackInterface;
import assetwallet.core.Config;
import assetwallet.core.Exporter.ExporterResult;
import assetwallet.core.FrackFixer.FrackFixerResult;
import assetwallet.core.Grader.GraderResult;
import assetwallet.core.LossFixer.LossFixerResult;
import assetwallet.core.RAIDA;
import assetwallet.core.ServantManager;
import assetwallet.core.ShowCoins.ShowCoinsResult;
import assetwallet.core.Unpacker.UnpackerResult;
import assetwallet.core.Wallet;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import javax.swing.plaf.basic.BasicScrollBarUI;


/**
 * 
 */
public class AssetWallet  {
    String version = "1.1.16";

    JPanel headerPanel;
    JPanel mainPanel;
    JPanel corePanel;
    JPanel wpanel;
    
    JPanel lwrapperPanel;
    
    String ltag = "Advanced Client";
    JLabel totalText;
    
    //Sets the default screen width and height
    int tw = 1208;
    int th = 726;    
    
 //   int tw = 870;
 //   int th = 524;
    
    int headerHeight;
        
    ProgramState ps;
    WLogger wl;
    
    ServantManager sm;
    
    MyButton continueButton;
    
    JProgressBar pbar;
    JLabel pbarText;
    
    JFrame mainFrame;
       
    final static int TYPE_ADD_BUTTON = 1;
    final static int TYPE_ADD_SKY = 2;
    
    JLabel trTitle, trInventory;
    
    public AssetWallet() {
        initSystem();
                
        AppUI.init(tw, th); 
        AppCore.logSystemInfo(version);
        
        headerHeight = th / 10;
        
        initMainScreen();
        
        if (!ps.errText.equals("")) {
            JLabel x = new JLabel(ps.errText);
            AppUI.setFont(x, 16);
            AppUI.setMargin(x, 8);
            AppUI.alignCenter(x);
            mainPanel.add(x);
            return;
        }
        
        initHeaderPanel();
        initCorePanel();
      
        mainPanel.add(headerPanel);
        mainPanel.add(corePanel);
    
        showScreen();
    }
    

    public void initSystem() {
        wl = new WLogger();
        
        String home = System.getProperty("user.home");
        //home += File.separator + "CloudCoinWallet";

        resetState();
        
        sm = new ServantManager(wl, home);
        if (!sm.init()) {
            resetState();
            ps.errText = "Failed to init program. Make sure you have correct folder permissions (" + home + ")";
            return;
        }

        if (!sm.initUser(Config.DEFAULT_NAME, "", "")) {
            ps.errText = "Failed to init Wallet";
            return;
        }

        AppCore.readConfig();
        resetState();
        
        if (Config.CONFIG_INITED == 1)
            ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
    }
   
    public void echoDone() {
        ps.isEchoFinished = true;
    }

    public void setCounters(int[][] counters) {
        ps.counters = counters;
    }
    
    public void cbDone() {
        ps.cbState = ProgramState.CB_STATE_DONE;
    }
  
    
    public void clear() {
      
        headerPanel.removeAll();
        fillHeaderPanel();
        
        headerPanel.repaint();
        headerPanel.revalidate();
        
        
        corePanel.removeAll();
        corePanel.repaint();
        
        corePanel.revalidate();
    }
    
    public void initMainScreen() {
        //creates the background panel 
        mainPanel = new JPanel();
       
        AppUI.setBoxLayout(mainPanel, true);
        AppUI.setSize(mainPanel, tw, th);
        AppUI.setBackground(mainPanel, AppUI.getColor1());
    
        mainFrame = AppUI.getMainFrame(version);
        mainFrame.setContentPane(mainPanel);
    }
    
    public void initHeaderPanel() {
        
        // Init header
        headerPanel = new JPanel();
        AppUI.setBoxLayout(headerPanel, false);
        AppUI.setSize(headerPanel, tw, headerHeight);
        AppUI.setBackground(headerPanel, AppUI.getColor0());
        AppUI.alignLeft(headerPanel);
        AppUI.alignTop(headerPanel);
         
        fillHeaderPanel();
    }
    
   
    
    public void fillHeaderPanel() {
        //fills header with objects
        JPanel p = new JPanel();
        AppUI.noOpaque(p);
        GridBagLayout gridbag = new GridBagLayout();
       
        p.setLayout(gridbag);
        
        GridBagConstraints c = new GridBagConstraints();      
        //c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;
        c.insets = new Insets(0, 10, 0, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;


        JLabel icon0, icon1, icon2, icon3;
        ImageIcon ii;
        try {
            Image img;
                   
            img = ImageIO.read(getClass().getClassLoader().getResource("resources/Gear icon.png"));
            icon0 = new JLabel(new ImageIcon(img));
            
            ii = new ImageIcon(img);
            
            img = ImageIO.read(getClass().getClassLoader().getResource("resources/Help_Support Icon.png"));
            icon1 = new JLabel(new ImageIcon(img));

            img = ImageIO.read(getClass().getClassLoader().getResource("resources/CloudCoinLogo2.png"));
            icon3 = new JLabel(new ImageIcon(img));
            
        } catch (Exception ex) {
            return;
        }
        
        headerPanel.add(p);
        gridbag.setConstraints(icon3, c);
        //p.add(icon3);
        
        if (ps.currentScreen == ProgramState.SCREEN_AGREEMENT) {
             // Init Label
            JLabel titleText = new JLabel("Asset Wallet " + version);
            AppUI.setTitleSemiBoldFont(titleText, 32);
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 40, 0, tw ); 
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = 0;
 
            
            gridbag.setConstraints(titleText, c);
            p.add(titleText);
            
            return;
        } else {
            JLabel titleText = new JLabel("Asset Wallet");
            AppUI.setHandCursor(titleText);
            MouseAdapter mal = new MouseAdapter() {
                public void mouseReleased(MouseEvent evt) {
                    ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                    showScreen();
                }
            };
            
            titleText.addMouseListener(mal);
            
            AppUI.setTitleSemiBoldFont(titleText, 32);
            c.insets = new Insets(0, 40, 0, 0); 
            gridbag.setConstraints(titleText, c);
            p.add(titleText);
            
            
            c.insets = new Insets(15, 10, 0, 0); 
            JPanel wrp = new JPanel();
            AppUI.setBoxLayout(wrp, false);
            AppUI.setSize(wrp, 216, 32);
            AppUI.noOpaque(wrp);
            
            

            
            c.anchor = GridBagConstraints.NORTH;            
            gridbag.setConstraints(wrp, c);
            p.add(wrp);
            
            c.anchor = GridBagConstraints.CENTER;
     
            // Pad
            c.weightx = 1;
            JLabel padd0 = new JLabel();
            gridbag.setConstraints(padd0, c);
            //p.add(padd0);
                        
            c.weightx = 0;
            c.insets = new Insets(0, 0, 0, 0); 
            
            
        }

        c.weightx = 1;
        JLabel padd = new JLabel();
        gridbag.setConstraints(padd, c);
        p.add(padd);
        

        c.fill = GridBagConstraints.NONE;
        c.gridwidth = 1;
        c.weightx = 0;
        c.fill = GridBagConstraints.NORTH;
        
        // Icon Gear
        AppUI.setHandCursor(icon0); 
        
        gridbag.setConstraints(icon0, c);
        AppUI.setSize(icon0, 52, 70);
        p.add(icon0);
 
        
        final Color savedColor = icon0.getBackground();
        final JLabel ficon = icon0;
   
        // Do stuff popup menu
        final int mWidth = 212;
        final int mHeight = 48;
        final JPopupMenu popupMenu = new JPopupMenu() {
            @Override
            public void paintComponent(final Graphics g) {
                g.setColor(AppUI.getColor6());
                g.fillRect(0,0,getWidth(), getHeight());
            } 
        };
 
        String[] items = {"Echo RAIDA" , "Export All" };
        for (int i = 0; i < items.length; i++) {
            JMenuItem menuItem = new JMenuItem(items[i]);
            menuItem.setActionCommand("" + i);
            AppUI.setHandCursor(menuItem);
    
            MouseAdapter ma = new MouseAdapter() {
                public void mouseEntered(MouseEvent evt) {
                    ps.popupVisible = true;
                    JMenuItem jMenuItem = (JMenuItem) evt.getSource();
                    jMenuItem.setBackground(AppUI.getColor0());
                }
                
                public void mouseExited(MouseEvent evt) {
                    JMenuItem jMenuItem = (JMenuItem) evt.getSource();
                    jMenuItem.setBackground(AppUI.getColor6());
                    
                    ps.popupVisible = false;
                    EventQueue.invokeLater(new Runnable() {
                        public void run(){
                            try {
                                 Thread.sleep(40);
                            } catch(InterruptedException ex) {
                            }

                            if (ps.popupVisible)
                                return;

                            ficon.setOpaque(false);
                            ficon.repaint();
                            popupMenu.setVisible(false);
                        }
                    });                  
                }
                
                public void mouseReleased(MouseEvent evt) {
                    JMenuItem jMenuItem = (JMenuItem) evt.getSource();
                    popupMenu.setVisible(false);

                    String action = jMenuItem.getActionCommand();
                    if (action.equals("0")) {
                        ps.currentScreen = ProgramState.SCREEN_ECHO_RAIDA;               
                    } else if (action.equals("1")) {
                        ps.currentScreen = ProgramState.SCREEN_EXPORT_ALL; 
                        
                    }

                    
                    showScreen();
                }
            };
            
            menuItem.addMouseListener(ma);
            
            AppUI.setSize(menuItem, mWidth, mHeight);
            AppUI.setFont(menuItem, 24);
            menuItem.setOpaque(true);

            menuItem.setBackground(AppUI.getColor6());
            menuItem.setBorder(BorderFactory.createEmptyBorder(0,0,0,0));
            menuItem.setUI(new MenuItemUI() {
                public void paint (final Graphics g, final JComponent c) {
                    final Graphics2D g2d = (Graphics2D) g;
                    final JMenuItem menuItem = (JMenuItem) c;
                    String ftext = menuItem.getText();
 
                    g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                    int width = g.getFontMetrics().stringWidth(ftext);
                    int cHeight = c.getHeight();

                    g.setColor(Color.WHITE);     
                    g.drawChars(ftext.toCharArray(), 0, ftext.length(), 12, cHeight/2 + 6);
                }
            });

            popupMenu.add(menuItem);
        }
        
        AppUI.setMargin(popupMenu, 0, 0, 0, 0);
        AppUI.noOpaque(popupMenu);
        AppUI.setHandCursor(popupMenu);

        popupMenu.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(final PopupMenuEvent e) {
                ps.popupVisible = true;
            }
            @Override
            public void popupMenuWillBecomeInvisible(final PopupMenuEvent e) {
                AppUI.setBackground(ficon, savedColor);
                ficon.setOpaque(false);
                ficon.repaint();
                ps.popupVisible = false;
            }
            
            @Override
            public void popupMenuCanceled(final PopupMenuEvent e) {
                AppUI.setBackground(ficon, savedColor);
                ficon.setOpaque(false);
                ficon.repaint();
                ps.popupVisible = false;
            }
        });

        icon0.addMouseListener(new MouseAdapter() {
            /*
            public void mouseReleased(MouseEvent e) {
                ficon.setOpaque(true);
                AppUI.setBackground(ficon, AppUI.getColor6());

                ficon.repaint();              
                popupMenu.show(ficon, 0 - mWidth  + ficon.getWidth(), ficon.getHeight());
            }*/
            
            public void mouseEntered(MouseEvent e) {
                ficon.setOpaque(true);
                AppUI.setBackground(ficon, AppUI.getColor6());

                ficon.repaint();              
                popupMenu.show(ficon, 0 - (int)(mWidth * 0.6) + ficon.getWidth(), ficon.getHeight());
            }
            
            public void mouseExited(MouseEvent e) {
                ps.popupVisible = false;
                EventQueue.invokeLater(new Runnable() {
                    public void run(){
                        try {
                            Thread.sleep(40);
                        } catch(InterruptedException ex) {
                        }

                        if (ps.popupVisible)
                            return;

                        ficon.setOpaque(false);
                        ficon.repaint();
                        popupMenu.setVisible(false);
                    }
                });
            }
        });
        
        
        // Icon Support
        c.insets = new Insets(0, 10, 0, 20); 
        AppUI.noOpaque(icon1);
        AppUI.setHandCursor(icon1);
        gridbag.setConstraints(icon1, c);
        p.add(icon1);
        icon1.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SUPPORT;
                showScreen();
            }
        });
 
        // Version
        c.insets = new Insets(0, 0, 20, 10);
        JLabel vl = new JLabel("v. " + version);
        AppUI.noOpaque(vl);
        AppUI.setTitleFont(vl, 14);
        gridbag.setConstraints(vl, c);
        p.add(vl);
        
        headerPanel.add(p);
    }
    
    
    public void initCorePanel() {
        corePanel = new JPanel();
        AppUI.setBoxLayout(corePanel, false);
        AppUI.noOpaque(corePanel);
        AppUI.alignLeft(corePanel);
        AppUI.setMargin(corePanel, 20);
    }
 
    
    public void resetState() {
        ps = new ProgramState();

    }
    
    public void showScreen() {
        wl.debug(ltag, "SCREEN " + ps.currentScreen + ": " + ps.toString());
        clear();

        
        switch (ps.currentScreen) {
            case ProgramState.SCREEN_AGREEMENT:
                resetState();
                showAgreementScreen();
                break;
            case ProgramState.SCREEN_SHOW_ASSETS:
                showAssetsScreen();
                break;
            case ProgramState.SCREEN_SUPPORT:
                showSupportScreen();
                break;
            case ProgramState.SCREEN_ECHO_RAIDA:
                showEchoRAIDAScreen();
                break;
            case ProgramState.SCREEN_ECHO_RAIDA_FINISHED:
                showEchoRAIDAFinishedScreen();
                break;
            case ProgramState.SCREEN_DEPOSIT:
                showDepositScreen();
                break;
            case ProgramState.SCREEN_DEPOSITING:
                showDepositingScreen();
                break;
            case ProgramState.SCREEN_DEPOSIT_DONE:
                showDepositDoneScreen();
                break;
            case ProgramState.SCREEN_SHOW_ASSET:
                showAssetScreen();
                break;
            case ProgramState.SCREEN_EXPORT_DONE:
                showExportDoneScreen();
                break;
            case ProgramState.SCREEN_EXPORT_ALL:
                showExportAllScreen();
                break;
            case ProgramState.SCREEN_EXPORTING:
                showExportingScreen();
                break;
            case ProgramState.SCREEN_EXPORT_ALL_DONE:
                showExportAllDoneScreen();
                break;
  
        }
        
        
     //   headerPanel.repaint();
     //   headerPanel.revalidate();

        corePanel.repaint();
        corePanel.revalidate();
    }
  
    public void maybeShowError(JPanel p) {
        if (!ps.errText.isEmpty()) {
            AppUI.hr(p, 10);

            JLabel err = new JLabel(ps.errText);
      
            AppUI.setFont(err, 16);
            AppUI.setColor(err, AppUI.getErrorColor());
            AppUI.alignCenter(err);
            
            AppUI.hr(p, 2);
            p.add(err);
            
            ps.errText = "";

            p.revalidate();
            p.repaint();
        }
        
    }
    
     private void setRAIDAFixingProgressCoins(int raidaProcessed, int totalCoinsProcessed, int totalCoins, int fixingRAIDA, int round) {
        pbar.setVisible(true);
        pbar.setValue(raidaProcessed);
        
        String stc = AppCore.formatNumber(totalCoinsProcessed);
        String tc = AppCore.formatNumber(totalCoins);
        
        pbarText.setText("<html><div style='text-align:center'>Round #" + round + " Fixing on RAIDA " + 
                fixingRAIDA + "<br>" + stc + " / " + tc + " Assets Fixed</div></html>");
        
        pbarText.repaint();
    }
    
    private void setRAIDAProgressCoins(int raidaProcessed, int totalCoinsProcessed, int totalCoins) {
        pbar.setVisible(true);
        pbar.setValue(raidaProcessed);
        
        if (totalCoins == 0)
            return;
        
        String stc = AppCore.formatNumber(totalCoinsProcessed);
        String tc = AppCore.formatNumber(totalCoins);
        
        pbarText.setText("Deposited " + stc + " / " + tc + " Assets");
        pbarText.repaint();
        
    }
    
   public void showEchoRAIDAScreen() {
        JPanel subInnerCore = getModalJPanel("Checking RAIDA");
        maybeShowError(subInnerCore);

        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        AppUI.hr(ct, 60);
        
        JLabel x = new JLabel("Please wait...");
        AppUI.setCommonFont(x);
        AppUI.alignCenter(x);
        ct.add(x);

        sm.startEchoService(new EchoCb());
    }
    
    public void showEchoRAIDAFinishedScreen() {
        JPanel subInnerCore = getModalJPanel("RAIDA Status");
        maybeShowError(subInnerCore);

        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        AppUI.hr(ct, 2);
        JLabel x;

        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);

        
        int[] statuses = sm.getRAIDAStatuses();
        
        int y = 1;
        int fontSize = 16;
        boolean isfailed = false;
        for (int i = 0; i < statuses.length / 2 + 1; i ++) {
            String status;
            
            x = new JLabel(AppCore.getRAIDAString(i));
            AppUI.setCommonTableFontSize(x, fontSize);
            AppUI.setColor(x, AppUI.getColor14());
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 2, 0); 
            c.gridx = 0;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
               
            x = new JLabel("");
            if (statuses[i] == -1) {
                status = "TIMED OUT";
                isfailed = true;
                AppUI.setColor(x, AppUI.getErrorColor());
            } else {
                status = AppCore.getMS(statuses[i]);
                AppUI.setColor(x, AppUI.getColor14());

            }
            
            x.setText(status);

            AppUI.setCommonTableFontSize(x, fontSize);
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 40, 2, 0); 
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
       
            int j = i + RAIDA.TOTAL_RAIDA_COUNT / 2 + 1;
            if (j == RAIDA.TOTAL_RAIDA_COUNT)
                break;
            
            x = new JLabel(AppCore.getRAIDAString(j));
            AppUI.setColor(x, AppUI.getColor14());
            AppUI.setCommonTableFontSize(x, fontSize);
            c.gridwidth = 1;
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 100, 2, 0); 
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
               
            x = new JLabel("");
            if (statuses[j] == -1) {
                status = "TIMED OUT";
                isfailed = true;
                AppUI.setColor(x, AppUI.getErrorColor());
            } else {
                status = AppCore.getMS(statuses[j]);
                AppUI.setColor(x, AppUI.getColor14());
            }
            
            
            x.setText(status);
            AppUI.setCommonTableFontSize(x, fontSize);
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 40, 2, 0); 
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);

            y++;
        }

        if (isfailed) {
            y++;
            String txt = "<html><div style='width:460px'>TIMED OUT means the response exceeded the " + Config.ECHO_TIMEOUT / 1000 + " seconds allowed. "
                + "This could be caused by a slow network or because the RAIDA was blocked (usually by office routers). "
                + "It could also be caused by your computer being old and unable to handle 25 threads at once. "
                + "Try changing your settings to increase the Timeout. Or try using a more powerful computer.</div></html>";  

            x = new JLabel(txt);
            AppUI.setCommonTableFontSize(x, fontSize);      
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(8, 0, 2, 0); 
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = y;
            c.gridwidth = 4;
            gridbag.setConstraints(x, c);
            ct.add(x);
        }

        
    }
    
    public void showExportDoneScreen() {
        JPanel rightPanel = getRightPanel();    
    
        JPanel ct = new JPanel();
        AppUI.setBoxLayout(ct, true);
        AppUI.noOpaque(ct);
        rightPanel.add(ct);
 
        JLabel ltitle = AppUI.getTitle("Export Done");   
        ct.add(ltitle);
        AppUI.alignTop(ct);
        AppUI.alignTop(ltitle);
        
        AppUI.hr(ct, 2);
        maybeShowError(ct);
        
        // Outer Container
        JPanel oct = new JPanel();
        AppUI.noOpaque(oct);
        
        JLabel txt = new JLabel("<html><div style='width: 720px'>" + ps.exportedFile + "</div></html>");
        AppUI.setFont(txt, 16);
        oct.add(txt);
        
        
        if (Desktop.isDesktopSupported()) {
            try {
                Desktop.getDesktop().open(new File(ps.exportedFile));
            } catch (IOException e) {
                wl.error(ltag, "Failed to open browser: " + e.getMessage());
            }
        }
        
        JPanel bp = getOneButtonPanelCustom("Continue", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                showScreen();
            }
        });
        
        resetState();
        

        AppUI.hr(rightPanel, 5);
        rightPanel.add(oct); 
        
        rightPanel.add(bp);
    }
    
    public void showExportAllDoneScreen() {
        boolean isError = !ps.errText.equals("");
        JPanel rightPanel = getRightPanel();    
    
        JPanel ct = new JPanel();
        AppUI.setBoxLayout(ct, true);
        AppUI.noOpaque(ct);
        rightPanel.add(ct);
 
        JLabel ltitle = AppUI.getTitle("Export Done");   
        ct.add(ltitle);
        AppUI.alignTop(ct);
        AppUI.alignTop(ltitle);
        
        AppUI.hr(ct, 2);
        maybeShowError(ct);
        
        // Outer Container
        JPanel oct = new JPanel();
        AppUI.noOpaque(oct);
        
        String dir = new File(ps.exportedFile).getParent();
        if (dir == null)
            return;
        
        if (!isError && dir != null) {
            JLabel txt = new JLabel("<html><div style='width: 720px; text-align:center'>"
                    + "Exported to folder " + dir + "</div></html>");
            AppUI.setFont(txt, 16);
            AppUI.alignCenter(txt);
            oct.add(txt);

            if (Desktop.isDesktopSupported()) {
                try {
                    Desktop.getDesktop().open(new File(dir));
                } catch (IOException e) {
                    wl.error(ltag, "Failed to open browser: " + e.getMessage());
                }
            }
        }
        
        JPanel bp = getOneButtonPanelCustom("Continue", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                showScreen();
            }
        });
        
        resetState();
        

        AppUI.hr(rightPanel, 5);
        rightPanel.add(oct); 
        
        rightPanel.add(bp);
    }
    
    public void showAssetScreen() {
        boolean isError = !ps.errText.equals("");

        JPanel rightPanel = getRightPanel();    
    
        JPanel ct = new JPanel();
        AppUI.setBoxLayout(ct, true);
        AppUI.noOpaque(ct);
        rightPanel.add(ct);
        
        Properties meta = ps.curAsset.getMeta();
        byte[] data = ps.curAsset.getData();
        
        String title = AppCore.getMetaItem(meta, "title");
        
        JLabel ltitle = AppUI.getTitle(title);   
        ct.add(ltitle);
        AppUI.alignTop(ct);
        AppUI.alignTop(ltitle);
        
        AppUI.hr(ct, 2);
        maybeShowError(ct);
        
        // Outer Container
        JPanel oct = new JPanel();
        AppUI.noOpaque(oct);
        
        int y = 0;
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints(); 
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHEAST;
        c.insets = new Insets(12, 0, 0, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.weightx = 1;
        oct.setLayout(gridbag);
        
        JPanel jp = new JPanel();
        AppUI.setCommonFont(jp);
        AppUI.noOpaque(jp);
        Image i = AppCore.getScaledImage(700, 560, data);
        ImageIcon ii = new ImageIcon(i);
        jp.add(new JLabel(ii));
        gridbag.setConstraints(jp, c);
        oct.add(jp);
        
        jp = new JPanel();
        AppUI.setBoxLayout(jp, true);
        AppUI.setCommonFont(jp);
        AppUI.alignTop(jp);
        AppUI.noOpaque(jp);
        AppUI.setSize(jp, 320, 470);
        
        
        /*
        String[] keys = {
            "id", "file_extension", "filename_tag", "series_name", "set", "title",
            "publisher", "description", "short_description", "genre", "category", "subject", "date_of_creation",
            "font_family", "font_size", "font_color", "text_location_x", "text_location_y", "translate_sn", "total"
        };*/

        
        AppUI.setMetaItem(meta, jp, "id", "ID #");
        AppUI.setMetaItem(meta, jp, "publisher", "Publisher");
        AppUI.setMetaItem(meta, jp, "series_name", "Series");
        AppUI.setMetaItem(meta, jp, "date_of_creation", "Date");
        AppUI.setMetaItem(meta, jp, "genre", "Genre");
        AppUI.setMetaItem(meta, jp, "subject", "Subject");
        AppUI.setMetaItem(meta, jp, "category", "Category");
        AppUI.setMetaItem(meta, jp, "description", "<br>");
        
        
        JPanel bp = getOneButtonPanelCustom("Export", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sm.startExporterService(ps.curAsset, new CallbackInterface() {
                    public void callback(Object o) {   
                        ExporterResult eresult = (ExporterResult) o;
                        if (eresult.status == ExporterResult.STATUS_ERROR) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run(){
                                    if (!eresult.errText.isEmpty())
                                        ps.errText = eresult.errText;
                                    else
                                        ps.errText = "Failed to export asset";
                                    showScreen();
                                    return;
                                }
                            });
                            return;
                        }
                            
                        if (eresult.status == ExporterResult.STATUS_FINISHED) {
                            EventQueue.invokeLater(new Runnable() {
                                public void run(){
                                    ps.exportedFile = eresult.exportedFileNames.get(0);
                                    ps.currentScreen = ProgramState.SCREEN_EXPORT_DONE;
                                    showScreen();
                                }
                            });
  
                            
                            return;
                        }
                    }
                });

            }
        });
        
        jp.add(bp);
        
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(12, 38, 0, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.weightx = 2;
        gridbag.setConstraints(jp, c);
        oct.add(jp);

        
        AppUI.hr(rightPanel, 5);
        rightPanel.add(oct); 
    }
    
    public void setAssetBlock(Asset asset, Properties meta, byte[] data) {
        JComponent[] jks = (JComponent[]) asset.getPrivate();
        JLabel jl = (JLabel) jks[1];
        JProgressBar pbar = (JProgressBar) jks[2];
        JLabel jdate = (JLabel) jks[3];
        JLabel jimg = (JLabel) jks[0];
    
        AppUI.setHandCursor(jl.getParent());
        jl.getParent().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                ps.curAsset = asset;
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSET;
                showScreen();
                return;
            }   
        });
        
        Image i = AppCore.getScaledImage(100, 80, data);
        ImageIcon ii = new ImageIcon(i);
       
        jimg.setIcon(ii);
        String title = AppCore.getMetaItem(meta, "title");
        String date = AppCore.getMetaItem(meta, "date_of_creation");
        jl.setText("<html><div style='text-align:center'>" + title + "</div></html>");
        jdate.setText(date);
        pbar.setVisible(false);
        pbar.repaint();
    }
    
    public void showExportingScreen() {
        JPanel subInnerCore = getModalJPanel("Export in Progress");
        maybeShowError(subInnerCore);

        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);

        JLabel x = new JLabel("<html><div style='width:480px;text-align:center'>"
                + "Do not close the application until all Assets are exported!</div></html>");
        AppUI.setCommonFont(x);
        //AppUI.setBoldFont(x, 16);
        AppUI.setColor(x, AppUI.getErrorColor());
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 0, 4, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        pbarText = new JLabel("");
        AppUI.setCommonFont(pbarText);
        c.insets = new Insets(40, 20, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 1;
        gridbag.setConstraints(pbarText, c);
        ct.add(pbarText);
        
        // ProgressBar
        pbar = new JProgressBar();
        pbar.setStringPainted(true);
        AppUI.setMargin(pbar, 0);
        AppUI.setSize(pbar, (int) (tw / 2.6f) , 50);
        pbar.setMinimum(0);
        pbar.setMaximum(24);
        pbar.setValue(0);
        pbar.setUI(new FancyProgressBar());
        AppUI.noOpaque(pbar);
        
        c.insets = new Insets(20, 20, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        gridbag.setConstraints(pbar, c);
        ct.add(pbar);
        
        subInnerCore.add(AppUI.hr(120));
        
        Thread t = new Thread(new Runnable() {
            public void run(){
                pbar.setVisible(true);

                pbarText.setText("Exporting assets ...");
                pbarText.repaint();
                pbar.setMaximum(ps.assets.length);
                pbar.setMinimum(0);
                
                for (int i = 0; i < ps.assets.length; i++) {
                    final int fi = i;
          
                    ps.asyncCanGo = false;
                    sm.startExporterService(ps.assets[i], new CallbackInterface() {
                        public void callback(Object o) {   
                            ExporterResult eresult = (ExporterResult) o;
                            if (eresult.status == ExporterResult.STATUS_ERROR) {
                                if (!eresult.errText.isEmpty()) {
                                    ps.errText = eresult.errText;
                                } else {    
                                    ps.errText = "Failed to export asset #" + ps.assets[fi].sn;
                                }
                            
                                return;
                            }
                            
                            ps.exportedFile = eresult.exportedFileNames.get(0);
                            ps.asyncCanGo = true;
                            return;
                        }
                    });
                    
                    while (!ps.asyncCanGo) {
                        try {
                            Thread.sleep(50);
                        } catch (InterruptedException e) {}
                    }
                    
                    pbarText.setText("Exported " + i + " / " + ps.assets.length + " assets");
                    pbarText.repaint();
                    pbar.setValue(i);
                    pbarText.repaint();
                    pbar.repaint();
                    
                    
                    
                    
                    
                };
 
                ps.currentScreen = ProgramState.SCREEN_EXPORT_ALL_DONE;
                showScreen();
                //sm.startUnpackerService(new UnpackerCb());
            }
        });
        
        t.start();
    }
    
    
    public void showExportAllScreen() {
        JPanel subInnerCore = getModalJPanel("Export Confirmation");
     
        // Container
        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);
        
        int y = 0;
       
        
        // Memo
        JLabel x = new JLabel("Total Assets:   ");
        AppUI.setCommonFont(x);
        c.insets = new Insets(0, 0, 4, 0);
        c.anchor = GridBagConstraints.EAST;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);

        x = new JLabel("" + ps.assets.length);
        AppUI.setCommonBoldFont(x);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = GridBagConstraints.RELATIVE;;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
               
        y++; 
        
        
        // Q
        x = new JLabel("Do you wish to continue?");
        AppUI.setCommonFont(x);
        c.insets = new Insets(32, 0, 4, 0);
        c.anchor = GridBagConstraints.CENTER;
        c.gridx = GridBagConstraints.RELATIVE;;
        c.gridy = y;
        c.gridwidth = 2;
        gridbag.setConstraints(x, c);
        ct.add(x);
               
        y++; 
             
        JPanel bp = getTwoButtonPanel(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.exportedFiles = 0;
                ps.currentScreen = ProgramState.SCREEN_EXPORTING;
                showScreen();
            }
        });
  
        
        subInnerCore.add(bp);    
    }
   
    
    
    public void showAssetsScreen() {      
        JPanel rightPanel = getRightPanel(AppUI.getColor4());    
        JPanel ct = new JPanel();
        AppUI.setBoxLayout(ct, true);
        
        AppUI.noOpaque(ct);
        rightPanel.add(ct);       
        JLabel trLabel;   
        
        boolean isError = !ps.errText.equals("");        
        if (isError) {
            maybeShowError(ct);
            resetState();
            return;
        }
    
        int y = 0;
        JPanel xpanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        xpanel.setLayout(gridbag);

        Asset[] assets;
        boolean isCached = false;
        if (ps.assets == null) {      
            assets = AppCore.getCoinsInDirs(Config.DIR_BANK, Config.DIR_FRACKED, Config.DEFAULT_NAME);
            ps.assets = assets;
        } else {
            assets = ps.assets;
            isCached = true;
        }
        
        if (assets == null || assets.length == 0) {
            trLabel = new JLabel("No Assets");
        } else {
            trLabel = new JLabel("Your Assets");
        }
        
        AppUI.setSemiBoldFont(trLabel, 20);
        AppUI.alignCenter(trLabel);
        ct.add(trLabel);
        
        for (int i = 0; i < assets.length; i++) {
            JLabel icon;
        
            AssetUIItem uitem = new AssetUIItem();
            uitem.idx = i; 
            try {
                Image img = ImageIO.read(getClass().getClassLoader().getResource("resources/no-image.png"));
                img = img.getScaledInstance(100, 80, Image.SCALE_SMOOTH);
                icon = new JLabel(new ImageIcon(img));
                AppUI.setSize(icon, 200, 80);
                uitem.image = icon;             
            } catch (Exception e) {                       
            }
    
            if (isCached) {
                JComponent[] jks = (JComponent[]) assets[i].getPrivate();
                uitem.title = (JLabel) jks[1];
                uitem.pbar = (JProgressBar) jks[2];
                uitem.date = (JLabel) jks[3];
                uitem.image = (JLabel) jks[0];
            } else {
                uitem.title = new JLabel("Loading...");
                uitem.date = new JLabel("");
                uitem.pbar = new JProgressBar();
                uitem.pbar.setStringPainted(true);
                AppUI.setMargin(uitem.pbar, 0);
                AppUI.setSize(uitem.pbar, (int) 100 , 20);
                uitem.pbar.setMinimum(0);
                uitem.pbar.setMaximum(24);
                uitem.pbar.setValue(0);
                uitem.pbar.setUI(new FancyProgressBar());
                AppUI.noOpaque(uitem.pbar);
                
                
            }
            
            JComponent[] jks = new JComponent[] {
                uitem.image,
                uitem.title,
                uitem.pbar,
                uitem.date,
                //new JLabel("button")
            };
            
            assets[i].setPrivate(jks);
            
            
            if (i % 4 == 0)
                y++;
            
            AppUI.getGBBlock(xpanel, jks, y, gridbag);
            if (isCached) {
                Properties meta = assets[i].getMeta();
                byte[] data = assets[i].getData();
                if (meta != null)
                    setAssetBlock(assets[i], meta, data);
            }
        }
        
        AppUI.GBPad(xpanel, y, gridbag);
        
        xpanel.setOpaque(false);
        
        
        JScrollPane scrollPane = new JScrollPane(xpanel);
        scrollPane.getVerticalScrollBar().setUnitIncrement(32);
        scrollPane.getVerticalScrollBar().setUI(new BasicScrollBarUI() {       
            @Override
            protected JButton createDecreaseButton(int orientation) {
                TriangleButton jbutton = new TriangleButton(false);
                AppUI.setHandCursor(jbutton);
                jbutton.setContentAreaFilled(false);
                jbutton.setFocusPainted(false);
            
                return jbutton;
            }

            @Override    
            protected JButton createIncreaseButton(int orientation) {
                TriangleButton jbutton = new TriangleButton(true);
                AppUI.setHandCursor(jbutton);
                jbutton.setContentAreaFilled(false);
                jbutton.setFocusPainted(false);
            
                return jbutton;
            }
            
         @Override 
            protected void configureScrollBarColors(){
                this.trackColor = AppUI.getColor6();
                this.thumbColor = AppUI.getColor7();
            }
        });
        AppUI.setSize(scrollPane, 1010, 385);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        ct.add(scrollPane);
 
        //print and export history from wallet 
        JPanel bp = getOneButtonPanelCustom("Deposit", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_DEPOSIT;
                showScreen();
            }
        });
  
        final Asset[] fassets = assets;
        // Launching Shower
        Thread t = new Thread(new Runnable() {
            public void run(){
                if (!ps.isEchoFinished) {
                    //pbarText.setText("Checking RAIDA ...");
                    //pbarText.repaint();
                }
                
                wl.debug(ltag, "Going here");
                while (!ps.isEchoFinished) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}

                }
          
                
                sm.startShowCoinsService(assets, new CallbackInterface() {
                    public void callback(Object o) {   
                        ShowCoinsResult scresult = (ShowCoinsResult) o;
                        if (scresult.status == ShowCoinsResult.STATUS_ERROR) {
                            ps.errText = "Global error";
                            wl.error(ltag, "Global error");
                            showScreen();
                            return;
                        } else if (scresult.status == ShowCoinsResult.STATUS_PROCESSING) {
                            int idx = scresult.currIdx;
                            if (idx >= fassets.length) {
                                wl.error(ltag, "Wrong idx " + idx + " length = " + fassets.length);
                                return;
                            }
                            
                            //System.out.println(scresult.getOperation(idx) + " " 
                              //      + scresult.getProgress(idx) + "/" + scresult.getProgressTotal(idx) + " st="+ scresult.getStatus(idx));

                            //if (ps.currentScreen == ProgramState.SCREEN_SHOW_ASSETS) {
                                JComponent[] jks = (JComponent[]) fassets[idx].getPrivate();
                                JLabel jl = (JLabel) jks[1];
                                JProgressBar pbar = (JProgressBar) jks[2];
                                JLabel jdate = (JLabel) jks[3];
                            
                                pbar.setValue(scresult.getProgress(idx));
                                pbar.setMaximum(scresult.getProgressTotal(idx));
                                if (scresult.getStatus(idx) == ShowCoinsResult.STATUS_ERROR) {
                                    jl.setText("Error");
                                    pbar.setVisible(false);
                                    return;
                                } else if (scresult.getStatus(idx) == ShowCoinsResult.STATUS_FINISHED) {
                                    Properties meta = scresult.getMeta(idx);
                                    byte[] img = scresult.getData(idx);
                                    
                                    setAssetBlock(fassets[idx], meta, img);
                                    fassets[idx].setData(img, meta);
                                            
                                } else if (scresult.getStatus(idx) == ShowCoinsResult.STATUS_PROCESSING) {
                                    int progress = (int) (((double) scresult.getProgress(idx) 
                                            / (double) scresult.getProgressTotal(idx)) * 100);
                                    jl.setText(scresult.getOperation(idx) + " " + progress + "%");
                                }
                          //  }
                            /*
                            if (scresult.getStatus(idx) == ShowCoinsResult.STATUS_FINISHED)
                                fassets[idx].setData(scresult.getData(idx), scresult.getMeta(idx));
                            else if (scresult.getStatus(idx) == ShowCoinsResult.STATUS_ERROR)
                                fassets[idx].setData(scresult.getData(idx), scresult.getMeta(idx));
*/
                            return;
                        } else if (scresult.status == ShowCoinsResult.STATUS_CANCELLED) {
                            wl.debug(ltag, "Cancelled");
                            return;
                        } 
                    }
                });
            }
        });
        
        if (!isCached)
            t.start();

        
        AppUI.hr(rightPanel, 5);
        rightPanel.add(bp);     
    }
    public void showDepositScreen() {
        boolean isError = !ps.errText.equals("");

        JPanel rightPanel = getRightPanel();    
    
        JPanel ct = new JPanel();
        AppUI.setBoxLayout(ct, true);
        AppUI.noOpaque(ct);
        rightPanel.add(ct);
        
        JLabel ltitle = AppUI.getTitle("Deposit");   
        ct.add(ltitle);
        AppUI.alignTop(ct);
        AppUI.alignTop(ltitle);
        
        AppUI.hr(ct, 2);
        maybeShowError(ct);
        
        // Outer Container
        JPanel oct = new JPanel();
        AppUI.noOpaque(oct);
        
        int y = 0;
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints(); 
        c.gridwidth = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(12, 18, 0, 0); 
        oct.setLayout(gridbag);


        // Total files selected
        String totalCloudCoins = AppCore.calcCoinsFromFilenames(ps.files);
        final JLabel tl = new JLabel("Selected " + ps.files.size() + " files ");
        AppUI.setCommonFont(tl);
        c.insets = new Insets(22, 18, 0, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.gridy = y + 3;  
        gridbag.setConstraints(tl, c);
        oct.add(tl);
      
        int ddWidth = 701;    
        JPanel ddPanel = new JPanel();
        ddPanel.setLayout(new GridBagLayout());
        
        JLabel l = new JLabel("<html><div style='text-align:center; width:" 
                + ddWidth  +"'><b>Drop files here or click<br>to select files</b></div></html>");
        AppUI.setColor(l, AppUI.getColor13());
        AppUI.setBoldFont(l, 40);
        AppUI.noOpaque(ddPanel);
        AppUI.setHandCursor(ddPanel);
        ddPanel.setBorder(new DashedBorder(40, AppUI.getColor13()));
        ddPanel.add(l);
        
        c.insets = new Insets(8, 18, 0, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridwidth = 2;
        c.anchor = GridBagConstraints.CENTER;
        c.gridy = y + 4;  
        
        AppUI.setSize(ddPanel, (int) ddWidth, 150);
        gridbag.setConstraints(ddPanel, c);
        new FileDrop(null, ddPanel, new FileDrop.Listener() {
            public void filesDropped( java.io.File[] files ) {   
                for( int i = 0; i < files.length; i++ ) {
                    if (!AppCore.hasCoinExtension(files[i])) {
                        ps.errText = "File must have .png, .jpeg or .stack extension";
                        maybeShowError(ct);
                        return;
                    }
                    ps.files.add(files[i].getAbsolutePath());
                }

                String text = "Selected " + ps.files.size() + " files";                              
                tl.setText(text);            
            } 
        }); 
        
        final JFileChooser chooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter(
                "Assets", "jpg", "jpeg", "stack", "png", "json", "txt");
        chooser.setFileFilter(filter);
        chooser.setMultiSelectionEnabled(true);
        
        ddPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (!Config.DEFAULT_DEPOSIT_DIR.isEmpty())
                    chooser.setCurrentDirectory(new File(Config.DEFAULT_DEPOSIT_DIR));

                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File[] files = chooser.getSelectedFiles();
                    for (int i = 0; i < files.length; i++) {
                        ps.files.add(files[i].getAbsolutePath());
                    }

                    String text = "Selected " + ps.files.size() + " files";
                              
                    tl.setText(text);                          
                    Config.DEFAULT_DEPOSIT_DIR = chooser.getCurrentDirectory().getAbsolutePath();
                    AppCore.writeConfig();
                }
            }   
        });

        oct.add(ddPanel);
        rightPanel.add(oct);
        
        // Space
        AppUI.hr(oct, 22);       
        JPanel bp = getTwoButtonPanel(new ActionListener() {
            public void actionPerformed(ActionEvent e) {            
                if (ps.files.size() == 0) {
                    ps.errText = "No files selected";
                    showScreen();
                    return;
                }
                
                ps.currentScreen = ProgramState.SCREEN_DEPOSITING;
                showScreen();
            }
        });
        
        AppUI.hr(rightPanel, 20);
        rightPanel.add(bp);       
    }
    
    
    public void showDepositingScreen() {
        JPanel subInnerCore = getModalJPanel("Deposit in Progress");
        maybeShowError(subInnerCore);

        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);

        JLabel x = new JLabel("<html><div style='width:480px;text-align:center'>"
                + "Do not close the application until all Assets are deposited!</div></html>");
        AppUI.setCommonFont(x);
        //AppUI.setBoldFont(x, 16);
        AppUI.setColor(x, AppUI.getErrorColor());
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(10, 0, 4, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        pbarText = new JLabel("");
        AppUI.setCommonFont(pbarText);
        c.insets = new Insets(40, 20, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 1;
        gridbag.setConstraints(pbarText, c);
        ct.add(pbarText);
        
        // ProgressBar
        pbar = new JProgressBar();
        pbar.setStringPainted(true);
        AppUI.setMargin(pbar, 0);
        AppUI.setSize(pbar, (int) (tw / 2.6f) , 50);
        pbar.setMinimum(0);
        pbar.setMaximum(24);
        pbar.setValue(0);
        pbar.setUI(new FancyProgressBar());
        AppUI.noOpaque(pbar);
        
        c.insets = new Insets(20, 20, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        gridbag.setConstraints(pbar, c);
        ct.add(pbar);
        
        subInnerCore.add(AppUI.hr(120));
        
        Thread t = new Thread(new Runnable() {
            public void run(){
                pbar.setVisible(false);
                if (!ps.isEchoFinished) {
                    pbarText.setText("Checking RAIDA ...");
                    pbarText.repaint();
                }
                
                wl.debug(ltag, "Going here");
                while (!ps.isEchoFinished) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {}

                }
       
                pbarText.setText("Moving assets ...");
                for (String filename : ps.files) {
                    AppCore.moveToFolderNoTs(filename, Config.DIR_IMPORT, Config.DEFAULT_NAME);
                }

                pbarText.setText("Unpacking assets ...");
                pbarText.repaint();
                
                sm.startUnpackerService(new UnpackerCb());
            }
        });
        
        t.start();
    }
    
    public void showDepositDoneScreen() {
        
        boolean isError = !ps.errText.equals("");
        JPanel subInnerCore;
        
        if (isError) {
            subInnerCore = getModalJPanel("Error");
            AppUI.hr(subInnerCore, 32);
            maybeShowError(subInnerCore);
            resetState();
            return;
        }
        
             
        subInnerCore = getModalJPanel("Deposit Complete");
        maybeShowError(subInnerCore);
        
        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);
        
        String total = AppCore.formatNumber(ps.statToBank + ps.statFailed + ps.statLost);
        String totalBankValue = AppCore.formatNumber(ps.statToBank);
        String totalFailedValue = AppCore.formatNumber(ps.statFailed);
        String totalLostValue = AppCore.formatNumber(ps.statLost);
        String totalFailedFiles = AppCore.formatNumber(ps.failedFiles);
        
        JLabel x;
        x = new JLabel("<html><div style='width:400px; text-align:center'>Deposited <b>" +  total 
                +  " Assets</div></html>");
        
        AppUI.setCommonFont(x);
 
        int y = 0;
        
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        c.gridwidth = 2;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;
        
        // Auth
        x = new JLabel("Total Authentic:");
        AppUI.setCommonFont(x);
        
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(50, 0, 4, 10);
        c.gridx = 0;
        c.gridy = y;
        c.gridwidth = 1;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        x = new JLabel(totalBankValue);
        AppUI.setCommonBoldFont(x);
        
        c.anchor = GridBagConstraints.WEST;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;
        
        // Counterfeit
        x = new JLabel("Total Counterfeit:");
        AppUI.setCommonFont(x);
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 0, 4, 10);
        c.gridx = 0;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        x = new JLabel(totalFailedValue);
        AppUI.setCommonBoldFont(x);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;
        
        // Lost
        x = new JLabel("Total Lost:");
        AppUI.setCommonFont(x);
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 0, 4, 10);
        c.gridx = 0;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        x = new JLabel(totalLostValue);
        AppUI.setCommonBoldFont(x);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;

        if (ps.failedFiles > 0) {
            x = new JLabel("Corrupted files:");
            AppUI.setCommonFont(x);
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(10, 0, 4, 10);
            c.gridx = 0;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
        
            x = new JLabel(totalFailedFiles);
            AppUI.setCommonBoldFont(x);
            c.anchor = GridBagConstraints.WEST;
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
        }
        
        if (ps.duplicates.size() != 0) {
            x = new JLabel("Duplicates:");
            AppUI.setCommonFont(x);
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(10, 0, 4, 10);
            c.gridx = 0;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
        
            x = new JLabel("" + ps.duplicates.size());
            AppUI.setCommonBoldFont(x);
            c.anchor = GridBagConstraints.WEST;
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = y;
            gridbag.setConstraints(x, c);
            ct.add(x);
            
        }
        
        
        JPanel bp = getTwoButtonPanelCustom("Next Deposit", "Continue", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetState();
                ps.currentScreen = ProgramState.SCREEN_DEPOSIT;
                showScreen();
            }
        },  new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                showScreen();
            }
        });
        
        resetState();
        
        subInnerCore.add(bp); 
    }
 
    public JPanel getTwoButtonPanel(ActionListener al) {
        return getTwoButtonPanel(al, null);
    }
    
    public JPanel getTwoButtonPanel(ActionListener al, String name) {
        JPanel bp = new JPanel();
     //   AppUI.setBoxLayout(bp, false);
        AppUI.noOpaque(bp);
       
        bp.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weightx = 1;
        gbc.weighty = 1;
        
        MyButton cb = new MyButton("Cancel");
        cb.addListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                showScreen();
            }
        });
        
        bp.add(cb.getButton(), gbc);           
        AppUI.vr(bp, 26);

        String text = "Continue";
     //   if (isConfirmingScreen())
       //     text = "Confirm";
        
        if (name != null)
            text = name;
        
        cb = new MyButton(text);
        cb.addListener(al);
        bp.add(cb.getButton(), gbc);
        
        continueButton = cb;
        
        return bp;
    }
    
    public JPanel getTwoButtonPanelCustom(String name0, String name1, ActionListener al0, ActionListener al1) {
        JPanel bp = new JPanel();
        AppUI.noOpaque(bp);
       
        bp.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weighty = 1;
        gbc.weightx = 1;
        
        MyButton cb = new MyButton(name0);
        cb.addListener(al0);
        
        bp.add(cb.getButton(), gbc);           
        AppUI.vr(bp, 26);

        cb = new MyButton(name1);
        cb.addListener(al1);
        bp.add(cb.getButton(), gbc);
        
        continueButton = cb;
        
        return bp;
    }

    public JPanel getOneButtonPanelCustom(String name0, ActionListener al0) {
        JPanel bp = new JPanel();
        AppUI.noOpaque(bp);
        
        bp.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weighty = 1;

        MyButton cb = new MyButton(name0);
        cb.addListener(al0);
        
        bp.add(cb.getButton(), gbc);           
        //AppUI.vr(bp, 26);
        
        return bp;
    }
    
    public JPanel getOneButtonPanel() {
        JPanel bp = new JPanel();
        AppUI.noOpaque(bp);
        
        bp.setLayout(new GridBagLayout());
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.anchor = GridBagConstraints.SOUTH;
        gbc.weighty = 1;

        MyButton cb = new MyButton("Continue");
        cb.addListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                showScreen();
            }
        });
        
        bp.add(cb.getButton(), gbc);           
        //AppUI.vr(bp, 26);
        
        return bp;
    }
    
    public JPanel getRightPanel() {
         return getRightPanel(AppUI.getColor2());
    }
    
    public JPanel getRightPanel(Color color) {
        JPanel mwrapperPanel = new JPanel();
        
        AppUI.setBoxLayout(mwrapperPanel, true);
        AppUI.noOpaque(mwrapperPanel);
        AppUI.alignLeft(mwrapperPanel);
        AppUI.alignTop(mwrapperPanel);
        AppUI.setSize(mwrapperPanel, tw - 48, th);

        JPanel subInnerCore = AppUI.createRoundedPanel(mwrapperPanel, color, 20);
        AppUI.setSize(subInnerCore, tw - 48, th - headerHeight - 120);
        
        corePanel.add(mwrapperPanel);
        
        if (!ps.isEchoFinished)
            sm.startEchoService(new EchoCb());
        
        return subInnerCore;
    }
    
    public void showSupportScreen() {

        JPanel rightPanel = getRightPanel();    
        JPanel ct = new JPanel();
        AppUI.setBoxLayout(ct, true);
        AppUI.noOpaque(ct);
        rightPanel.add(ct);
        
        JLabel ltitle = AppUI.getTitle("Help & Support");   
        ct.add(ltitle);
       // AppUI.hr(ct, 20);
            
        // GridHolder Container
        JPanel gct = new JPanel();
        AppUI.noOpaque(gct);
   
        GridBagLayout gridbag = new GridBagLayout();
        gct.setLayout(gridbag);   
        GridBagConstraints c = new GridBagConstraints();    
        
        int y = 0;
        
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(10, 0, 14, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        JLabel vl = new JLabel("Version: " + this.version);
        AppUI.setFont(vl, 16);
        gridbag.setConstraints(vl, c); 
        gct.add(vl);
        y++;
               
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(0, 0, 4, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        
        int topMargin = 26;
        
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(topMargin, 0, 4, 0); 
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;

        String urlName = "http://cloudcoinconsortium.com/use.html";
        JLabel l = AppUI.getHyperLink(urlName, urlName, 0);
        gridbag.setConstraints(l, c); 
        gct.add(l);
        
        y++;
        
        l = new JLabel("<html><div style='width:460px; text-align:center'><br>"
                + "Support: 9 AM to 3 AM California Time (PST)<br> "
                + "Tel: +1(530)762-1361 <br>"
                + "Email: Support@cloudcoinmail.com</div></html>");
        c.insets = new Insets(0, 0, 0, 0); 
        AppUI.alignCenter(l);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        AppUI.setCommonFont(l);
        gridbag.setConstraints(l, c);
        gct.add(l);
        
        y++;
        
        l = new JLabel("<html><div style='width:480px; text-align:center; font-size: 14px'>"
                + "(Secure if you get a free encrypted email account at ProtonMail.com)</div></html>");
      
        AppUI.setMargin(l, 0);
        AppUI.setFont(l, 12);
        AppUI.alignCenter(l);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        AppUI.setCommonFont(l);
        gridbag.setConstraints(l, c);
        gct.add(l);

        y++;
        
        // Get proton
        l = AppUI.getHyperLink("Get Protonmail", "https://www.protonmail.com", 14);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(l, c); 
        gct.add(l);
        
        y++;
        
        // Get proton
        l = AppUI.getHyperLink("Instructions, Terms and Conditions", "javascript:void(0); return false", 20);
        l.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                final JDialog f = new JDialog(mainFrame, "Instructions, Terms and Conditions", true);
                f.setModalityType(Dialog.ModalityType.APPLICATION_MODAL);
                AppUI.noOpaque((JComponent) f.getContentPane());
                AppUI.setSize(f, (int) (tw / 1.2), (int) (th / 1.2)); 

                JTextPane tp = new JTextPane();
                
                
                AppUI.setFont(tp, 12);
                String fontfamily = tp.getFont().getFamily();

                tp.setContentType("text/html"); 
                tp.setText("<html><div style=' font-family:"+fontfamily+"; font-size: 12px'>" + AppUI.getAgreementText() + "</div></html>"); 
                tp.setEditable(false); 
                tp.setBackground(null);
                tp.setCaretPosition(0);

                JScrollPane scrollPane = new JScrollPane(tp);

                f.add(scrollPane);
                f.pack();
                f.setLocationRelativeTo(mainFrame);
                f.setVisible(true);      
                
                scrollPane.getVerticalScrollBar().setValue(scrollPane.getVerticalScrollBar().getMinimum());
                scrollPane.getViewport().setViewPosition(new java.awt.Point(0, 100));
                scrollPane.repaint();
            }
        });
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        c.insets = new Insets(40, 0, 4, 0); 
        gridbag.setConstraints(l, c); 
        gct.add(l);
        
        y++;
        
        // Support Portal
        vl = new JLabel("Support Portal");
        AppUI.setCommonFont(vl);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        c.insets = new Insets(40, 0, 4, 0); 
        gridbag.setConstraints(vl, c); 
        gct.add(vl);
        y++;
        
        
        urlName = "https://cloudcoinsupport.atlassian.net/servicedesk/customer/portals";
        l = AppUI.getHyperLink(urlName, urlName, 14);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        c.insets = new Insets(4, 0, 4, 0); 
        gridbag.setConstraints(l, c); 
        gct.add(l);
        
        y++;

        ct.add(gct);        
    }

   
    
    public void showAgreementScreen() {
        
        JPanel subInnerCore = AppUI.createRoundedPanel(corePanel);
        
        // Title
        JLabel text = new JLabel("CloudCoin Wallet");
        AppUI.alignCenter(text);
        AppUI.setBoldFont(text, 24);
        subInnerCore.add(text);
 
        // Agreement Panel        
        JPanel agreementPanel = AppUI.createRoundedPanel(subInnerCore);
        AppUI.roundCorners(agreementPanel, AppUI.getColor3(), 20);
        AppUI.alignCenter(agreementPanel);
             
        // Title 
        text = new JLabel("Terms and Conditions");
        AppUI.alignCenter(text);
        AppUI.setBoldFont(text, 24);
        agreementPanel.add(text);
        
        // Space
        AppUI.hr(agreementPanel,  tw * 0.0082 * 2);
                
        
        String aText = AppUI.getAgreementText();
        aText = aText.replaceAll("<span class=\"instructions\">.+</span>", "");
        
        // Text
        text = new JLabel("<html><div style='padding-right: 20px; width: 720px'>" + aText + "</div></html>");
        AppUI.alignCenter(text);
        AppUI.setFont(text, 18);
              
        JPanel wrapperAgreement = new JPanel();
        AppUI.setBoxLayout(wrapperAgreement, true);
        AppUI.alignCenter(wrapperAgreement);
        AppUI.noOpaque(wrapperAgreement);
        wrapperAgreement.add(text);
        
        // Checkbox
        MyCheckBox cb = new MyCheckBox("I have read and agree with the Terms and Conditions");
        cb.setBoldFont();
        wrapperAgreement.add(cb.getCheckBox());
        
        // Space
        AppUI.hr(wrapperAgreement, 20);
        
        // JButton
        MyButton button = new MyButton("Continue");
        button.disable();
        wrapperAgreement.add(button.getButton());
        
        final MyButton fbutton = button;
        cb.addListener(new ItemListener() {
            public void itemStateChanged(ItemEvent e) {
                Object o = e.getSource();
                
                if (o instanceof JCheckBox) {
                    JCheckBox cb = (JCheckBox) o;
                    if (cb.isSelected()) {
                        fbutton.enable();
                    } else {
                        fbutton.disable();
                    }  
                }
            }
        });
        
        button.addListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                ps.currentScreen = ProgramState.SCREEN_SHOW_ASSETS;
                AppCore.writeConfig();
                showScreen();
            }
        });
                
        // ScrollBlock
        JScrollPane scrollPane = new JScrollPane(wrapperAgreement);
        JScrollBar scrollBar = new JScrollBar(JScrollBar.VERTICAL) {
            @Override
            public boolean isVisible() {
                return true;
            }
        };

        scrollPane.setVerticalScrollBar(scrollBar);
        scrollPane.getVerticalScrollBar().setUnitIncrement(42);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
      //  scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);     
        agreementPanel.add(scrollPane);
      
        subInnerCore.add(agreementPanel);
    }
    
    public JPanel getModalJPanel(String title) {
        
        JPanel rightPanel = getRightPanel();

        JPanel xpanel = new JPanel(new GridBagLayout());
        AppUI.noOpaque(xpanel);
        rightPanel.add(xpanel); 
        
        JPanel subInnerCore = AppUI.createRoundedPanel(xpanel, AppUI.getColor12(), 20);
        AppUI.setSize(subInnerCore, 718, 446);

        AppUI.hr(subInnerCore, 14);
        
        // Title
        JLabel ltitle = AppUI.getTitle(title);
        subInnerCore.add(ltitle);
        
        return subInnerCore;
    }
 
    
    public static void main(String[] args) {
        System.setProperty("awt.useSystemAAFontSettings","on");
        System.setProperty("swing.aatext", "true");
        /*
        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e) {
            e.printStackTrace();
        }*/
        
        Locale.setDefault(new Locale("en", "US"));
        System.setProperty("user.language","en-US");
        
        try {
           
           boolean isSet = false;
           for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Windows".equals(info.getName())) {
                   UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
                    

   
                   // javax.swing.UIManager.setLookAndFeel(info.getClassName());
                   isSet = true;
                   break;
                } 
           }   
           if (!isSet)
                UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (ClassNotFoundException ex) {
          
        } catch (InstantiationException ex) {
 
           
        } catch (IllegalAccessException ex) {
       
        } catch (javax.swing.UnsupportedLookAndFeelException ex) { 
      
        }

        UIManager.put("ScrollBar.background", new ColorUIResource(AppUI.getColor0()));
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new AssetWallet();
            }
        });
    }
    
    class EchoCb implements CallbackInterface {
	public void callback(Object result) {
            wl.debug(ltag, "Echo finished");
            
            echoDone();
            
            if (ps.currentScreen == ProgramState.SCREEN_ECHO_RAIDA) {
                EventQueue.invokeLater(new Runnable() {         
                    public void run() {
                        ps.currentScreen = ProgramState.SCREEN_ECHO_RAIDA_FINISHED;
                        showScreen();
                    }
                });
            }
	}  
    }
    
    class UnpackerCb implements CallbackInterface {
	public void callback(Object result) {
            wl.debug(ltag, "Unpacker finisheed");
            
            final Object fresult = result;
            final UnpackerResult ur = (UnpackerResult) fresult;

            if (ur.status == UnpackerResult.STATUS_ERROR) {
                EventQueue.invokeLater(new Runnable() {         
                    public void run() {
                        if (!ur.errText.isEmpty())
                            ps.errText = ur.errText;
                        else  
                            ps.errText = "Failed to Unpack file(s). Please check the logs";

                        ps.currentScreen = ProgramState.SCREEN_DEPOSIT_DONE;
                        showScreen();
                    }
                });
                
                return;
            }

            ps.duplicates = ur.duplicates;
            ps.failedFiles = ur.failedFiles;
            
            setRAIDAProgressCoins(0, 0, 0);
            sm.startAuthenticatorService(new AuthenticatorCb());
        }
    }
    
    class AuthenticatorCb implements CallbackInterface {
	public void callback(Object result) {
            wl.debug(ltag, "Authenticator finished");
            
            final Object fresult = result;
            final AuthenticatorResult ar = (AuthenticatorResult) fresult;
            if (ar.status == AuthenticatorResult.STATUS_ERROR) {
                EventQueue.invokeLater(new Runnable() {         
                    public void run() {
                        if (!ar.errText.isEmpty())
                            ps.errText = "<html><div style='text-align:center; width: 520px'>" + ar.errText + "</div></html>";
                        else
                            ps.errText = "Failed to Authencticate Coins";

                        ps.currentScreen = ProgramState.SCREEN_DEPOSIT_DONE;
                        showScreen();
                    }
                });
                return;
            } else if (ar.status == AuthenticatorResult.STATUS_FINISHED) {
                sm.startGraderService(new GraderCb(), ps.duplicates, null);
                return;
            } else if (ar.status == AuthenticatorResult.STATUS_CANCELLED) {
                sm.resumeAll();
                EventQueue.invokeLater(new Runnable() {         
                    public void run() {
                        ps.errText = "Operation Cancelled";
                        ps.currentScreen = ProgramState.SCREEN_DEPOSIT_DONE;
                        showScreen();
                    }
                });
                return;
            }

            setRAIDAProgressCoins(ar.totalRAIDAProcessed, ar.totalCoinsProcessed, ar.totalCoins);
	}
    }
    
    class GraderCb implements CallbackInterface {
	public void callback(Object result) {
            GraderResult gr = (GraderResult) result;

            ps.statToBankValue = gr.totalAuthenticValue + gr.totalFrackedValue;
            ps.statFailedValue = gr.totalCounterfeitValue;
            ps.statLostValue = gr.totalLostValue;
            ps.statToBank = gr.totalAuthentic + gr.totalFracked;
            ps.statFailed = gr.totalCounterfeit;
            ps.statLost = gr.totalLost + gr.totalUnchecked;
            ps.receiptId = gr.receiptId;
                                 
            EventQueue.invokeLater(new Runnable() {         
                public void run() {
                    pbarText.setText("Fixing fracked coins ...");
                    pbarText.repaint();
                }
            });

            sm.startFrackFixerService(new FrackFixerCb());           
	}
    }

    class FrackFixerCb implements CallbackInterface {
	public void callback(Object result) {
            FrackFixerResult fr = (FrackFixerResult) result;
            
            if (fr.status == FrackFixerResult.STATUS_PROCESSING) {
                wl.debug(ltag, "Processing coin");
                setRAIDAFixingProgressCoins(fr.totalRAIDAProcessed, fr.totalCoinsProcessed, fr.totalCoins, fr.fixingRAIDA, fr.round);
		return;
            }

            if (fr.status == FrackFixerResult.STATUS_ERROR) {
                ps.errText = "Failed to fix coins";
                wl.error(ltag, "Failed to fix");
            }
            
            if (fr.status == FrackFixerResult.STATUS_CANCELLED) {
                wl.error(ltag, "Frack cancelled");
                sm.resumeAll();
                EventQueue.invokeLater(new Runnable() {         
                    public void run() {
                        ps.errText = "Operation Cancelled";
                        ps.currentScreen = ProgramState.SCREEN_DEPOSIT_DONE;
                        showScreen();
                    }
                });
                return;
            }

            if (fr.status == FrackFixerResult.STATUS_FINISHED) {
		if (fr.fixed + fr.failed > 0) {
                    wl.debug(ltag, "Fracker fixed: " + fr.fixed + ", failed: " + fr.failed);
		}
            }

            EventQueue.invokeLater(new Runnable() {         
                public void run() {
                    pbarText.setText("Recovering lost coins ...");
                    pbarText.repaint();
                }
            });
    
            sm.startLossFixerService(new LossFixerCb());
        }
    }
    
    class LossFixerCb implements CallbackInterface {
	public void callback(final Object result) {
            LossFixerResult lr = (LossFixerResult) result;
            
            if (lr.status == LossFixerResult.STATUS_PROCESSING) {
                wl.debug(ltag, "Processing lossfixer");
                return;
            }
            
            if (lr.status == LossFixerResult.STATUS_CANCELLED) {
                ps.errText = "Operation Cancelled";
                sm.resumeAll();
            }
            
            wl.debug(ltag, "LossFixer finished");
            
           
            EventQueue.invokeLater(new Runnable() {         
                public void run() {
                    ps.currentScreen = ProgramState.SCREEN_DEPOSIT_DONE;
                    showScreen();
                };
            });
        }
    }
}


