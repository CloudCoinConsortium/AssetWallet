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

import assetwallet.core.AppCore;
import assetwallet.core.Config;
import assetwallet.core.RAIDA;


/**
 * 
 */
public class AssetWallet  {
    String version = "1.1.15";

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

        AppCore.readConfig();
        resetState();
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
        p.add(icon3);
        
        if (ps.currentScreen == ProgramState.SCREEN_AGREEMENT) {
             // Init Label
            JLabel titleText = new JLabel("CloudCoin Wallet " + version);
            AppUI.setTitleSemiBoldFont(titleText, 32);
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 10, 0, tw ); 
            c.gridx = GridBagConstraints.RELATIVE;
            c.gridy = 0;
 
            
            gridbag.setConstraints(titleText, c);
            p.add(titleText);
            
            return;
        } else {
            JLabel titleText = new JLabel("Total Coins: ");
            AppUI.setTitleSemiBoldFont(titleText, 32);
            c.insets = new Insets(0, 10, 0, 0); 
            gridbag.setConstraints(titleText, c);
            p.add(titleText);
            
            
            c.insets = new Insets(15, 10, 0, 0); 
            JPanel wrp = new JPanel();
            AppUI.setBoxLayout(wrp, false);
            AppUI.setSize(wrp, 216, 32);
            AppUI.noOpaque(wrp);
            
            
            totalText = new JLabel("0");
            AppUI.setTitleFont(totalText, 32);
            //AppUI.setSize(totalText, 100, 32);
            //gridbag.setConstraints(totalText, c);
            //p.add(totalText);
           
            
            wrp.add(totalText);
            
            c.anchor = GridBagConstraints.NORTH;
            titleText = new JLabel("cc");
            AppUI.setTitleFont(titleText, 16);
            AppUI.setSize(titleText, 40, 40);
            AppUI.alignBottom(titleText);
            AppUI.setMargin(titleText, 0, 6, 0, 0);
            
            wrp.add(titleText);
            
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
            
            // Deposit Button 
            final JButton b0 = new JButton("Deposit");
            b0.setContentAreaFilled(false);
            b0.setFocusPainted(false);
            b0.setBorderPainted(false);
            AppUI.noOpaque(b0);
            AppUI.setTitleBoldFont(b0, 26);
            AppUI.setHandCursor(b0);

            
            gridbag.setConstraints(b0, c);
            p.add(b0);
            c.insets = new Insets(0, 5, 0, 0); 
            
            
            // Withdraw button
            final JButton b2 = new JButton("Withdraw");
            b2.setContentAreaFilled(false);
            b2.setFocusPainted(false);
            b2.setBorderPainted(false);
            AppUI.noOpaque(b2);
            AppUI.setTitleBoldFont(b2, 26);
            AppUI.setHandCursor(b2);

            
            gridbag.setConstraints(b2, c);
            p.add(b2);
            c.insets = new Insets(0, 5, 0, 0); 
            
            
            

            // Transfer Button
            final JButton b1 = new JButton("Transfer");
            
            b1.setContentAreaFilled(false);
            b1.setBorderPainted(false);
            b1.setFocusPainted(false);
            AppUI.setTitleBoldFont(b1, 26);
            AppUI.setHandCursor(b1);

            gridbag.setConstraints(b1, c);
            p.add(b1);

            ActionListener al0 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton b = (JButton) e.getSource();
                    
                    resetState();
                    //ps.currentScreen = ProgramState.SCREEN_PREDEPOSIT;
                    ps.isSkyDeposit = false;
                    ps.currentScreen = ProgramState.SCREEN_DEPOSIT;
                    showScreen();
                }
            };
            
            ActionListener al1 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton b = (JButton) e.getSource();
                    
                    resetState();
                    ps.currentScreen = ProgramState.SCREEN_TRANSFER;
                    showScreen();
                }
            };
            
            ActionListener al2 = new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JButton b = (JButton) e.getSource();
                    
                    resetState();
                    ps.currentScreen = ProgramState.SCREEN_WITHDRAW;
                    showScreen();
                }
            };
            
            MouseAdapter ma0 = new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {
                    JButton b = (JButton) e.getSource();
                    
                    AppUI.underLine(b);
                }
                public void mouseExited(MouseEvent e) {                
                    JButton b = (JButton) e.getSource();

                }
            };
            
            MouseAdapter ma1 = new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {                   
                    JButton b = (JButton) e.getSource();
                    
                    AppUI.underLine(b);
                }
                public void mouseExited(MouseEvent e) {                   
                    JButton b = (JButton) e.getSource();

                }
            };
            
            MouseAdapter ma2 = new MouseAdapter() {
                public void mouseEntered(MouseEvent e) {                   
                    JButton b = (JButton) e.getSource();
                    
                    AppUI.underLine(b);
                }
                public void mouseExited(MouseEvent e) {                   
                    JButton b = (JButton) e.getSource();
                    

                }
            };
                   
            b0.addActionListener(al0);
            b1.addActionListener(al1);
            b2.addActionListener(al2);
            b0.addMouseListener(ma0);
            b1.addMouseListener(ma1);
            b2.addMouseListener(ma2);
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
 
        String[] items = {"Backup", "List serials", "Clear History", "Fix Fracked", 
            "Delete Wallet", "Show Folders", "Echo RAIDA", "Settings", "Sent Coins", "Export Keys", "Bill Pay"};
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
                    
                    resetState();
                    
                    String action = jMenuItem.getActionCommand();
                    if (action.equals("0")) {
                        ps.currentScreen = ProgramState.SCREEN_BACKUP;
                    } else if (action.equals("1")) {
                        ps.currentScreen = ProgramState.SCREEN_LIST_SERIALS;
                    } else if (action.equals("2")) {
                        ps.currentScreen = ProgramState.SCREEN_CLEAR;
                    } else if (action.equals("3")) {
                        ps.currentScreen = ProgramState.SCREEN_FIX_FRACKED;
                    } else if (action.equals("4")) {
                        ps.currentScreen = ProgramState.SCREEN_DELETE_WALLET;
                    } else if (action.equals("5")) {
                        ps.currentScreen = ProgramState.SCREEN_SHOW_FOLDERS;
                    } else if (action.equals("6")) {
                        ps.currentScreen = ProgramState.SCREEN_ECHO_RAIDA;
                    } else if (action.equals("7")) {
                        ps.currentScreen = ProgramState.SCREEN_SETTINGS;
                    } else if (action.equals("8")) {
                        ps.currentScreen = ProgramState.SCREEN_SHOW_SENT_COINS;                        
                    } else if (action.equals("9")) {
                        ps.currentScreen = ProgramState.SCREEN_SHOW_BACKUP_KEYS; 
                    } else if (action.equals("10")) {
                        ps.currentScreen = ProgramState.SCREEN_SHOW_BILL_PAY; 
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
            case ProgramState.SCREEN_CREATE_WALLET:
               // showCreateWalletScreen();
                break;
            case ProgramState.SCREEN_DEFAULT:
                resetState();
             //   showDefaultScreen();
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
    
    private void setRAIDAProgressCoins(int raidaProcessed, int totalCoinsProcessed, int totalCoins) {
        pbar.setVisible(true);
        pbar.setValue(raidaProcessed);
        
        if (totalCoins == 0)
            return;
        
        String stc = AppCore.formatNumber(totalCoinsProcessed);
        String tc = AppCore.formatNumber(totalCoins);
        
        pbarText.setText("Deposited " + stc + " / " + tc + " CloudCoins");
        pbarText.repaint();
        
    }
    
   
    
    public void showFixingfrackedScreen() {
        JPanel subInnerCore = getModalJPanel("Fixing in Progress");
        maybeShowError(subInnerCore);

        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);
        
        int y = 0;
        // Text Label
        pbarText = new JLabel("");
        AppUI.setCommonFont(pbarText);
        c.insets = new Insets(40, 20, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(pbarText, c);
        ct.add(pbarText);
        
        y++;
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
        c.gridy = y;
        gridbag.setConstraints(pbar, c);
        ct.add(pbar);
        
        JPanel bp = getOneButtonPanelCustom("Cancel", new ActionListener() {
            public void actionPerformed(ActionEvent e) {


                showScreen();
            }
        });
       
        subInnerCore.add(bp);  
        
        Thread t = new Thread(new Runnable() {
            public void run() {
                try {
                    while (!ps.isEchoFinished) {
                        Thread.sleep(100);
                    }
                } catch (InterruptedException e) {}
                

                
            }
        });
        
        t.start();
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

      //  sm.startEchoService(new EchoCb());
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
        /*
        x = new JLabel("");
        AppUI.setFont(x, 15);
        c.anchor = GridBagConstraints.CENTER;
        c.insets = new Insets(0, 0, 4, 0); 
        c.gridx = 0;
        c.gridy = 0;
        c.gridwidth = 2;
        gridbag.setConstraints(x, c);
        ct.add(x);*/
        
       // int[] statuses = sm.getRAIDAStatuses();
        int[] statuses = new int[25];
        
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
    
  
    
    
    public void showImportingScreen() {
        JPanel subInnerCore = getModalJPanel("Deposit in Progress");
        maybeShowError(subInnerCore);

        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);

        JLabel x = new JLabel("<html><div style='width:480px;text-align:center'>Do not close the application until all CloudCoins are deposited!</div></html>");
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
  /*              
                if (!sm.isRAIDAOK()) {
                    ps.errText = "<html><div style='width:520px;text-align:center'>RAIDA cannot be contacted. "
                            + "This is usually caused by company routers blocking outgoing traffic. "
                            + "Please Echo RAIDA and try again.</div></html>";
                    ps.isEchoFinished = false;
                    ps.currentScreen = ProgramState.SCREEN_IMPORT_DONE;
                    showScreen();
                    return;
                }
*/
              //  ps.dstWallet.setPassword(ps.typedPassword);
//                sm.setActiveWalletObj(ps.dstWallet);
                
                pbarText.setText("Moving coins ...");
                for (String filename : ps.files) {
                   // String name = sm.getActiveWallet().getName();
                   // AppCore.moveToFolderNoTs(filename, Config.DIR_IMPORT, name);
                }

                pbarText.setText("Unpacking coins ...");
                pbarText.repaint();
                
                wl.debug(ltag, "issky " + ps.isSkyDeposit);
                if (ps.isSkyDeposit) {
               //     sm.startUnpackerService(new UnpackerSenderCb());
                } else {
                 //   sm.startUnpackerService(new UnpackerCb());
                }
            }
        });
        
        t.start();
        
    }
    
    public void showFixDoneScreen() {
        boolean isError = !ps.errText.equals("");
        JPanel subInnerCore;
        
        if (isError) {
            subInnerCore = getModalJPanel("Error");
            AppUI.hr(subInnerCore, 32);
            maybeShowError(subInnerCore);
            resetState();
            return;
        }
        
        //String total = AppCore.formatNumber(ps.statTotalFracked);
        String totalFixed = AppCore.formatNumber(ps.statTotalFixed);
        //String totalFailedToFix = AppCore.formatNumber(ps.statFailedToFix);
        
        
        String total = AppCore.formatNumber(ps.statTotalFrackedValue);
        String totalFixedValue = AppCore.formatNumber(ps.statTotalFixedValue);
        //String totalFailedToFixValue = AppCore.formatNumber(ps.statTotalFrackedValue - ps.statTotalFixedValue);

        subInnerCore = getModalJPanel("Fix Complete");
        maybeShowError(subInnerCore);
        
        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);

        
        String txt = "";
        if (!totalFixed.equals("" + total)) {
        //    txt = "Not all CloudCoins from <b>" + ps.srcWallet.getName() + "</b> had been fixed. Try it again later";
        } else {
          //  txt = "Your CloudCoins from <b>" + ps.srcWallet.getName() + "</b> have been fixed";
        }
        
        JLabel x = new JLabel("<html><div style='width:400px; text-align:center'>" + txt + "</div></html>");
          
        AppUI.setCommonFont(x);
 
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 0;
        c.gridwidth = 2;
        gridbag.setConstraints(x, c);
        ct.add(x);
 
        x = new JLabel("Total Fracked Coins:");
        AppUI.setCommonFont(x);
        
        c.weightx = 1;
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(50, 0, 4, 10);
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 1;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        x = new JLabel(total);
        AppUI.setCommonBoldFont(x);
        
        c.anchor = GridBagConstraints.WEST;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 1;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        x = new JLabel("Total Fixed Coins:");
        AppUI.setCommonFont(x);
        c.anchor = GridBagConstraints.EAST;
        c.insets = new Insets(10, 0, 4, 10);
        c.gridx = 0;
        c.gridy = 2;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        x = new JLabel(totalFixedValue);
        AppUI.setCommonBoldFont(x);
        c.anchor = GridBagConstraints.WEST;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        gridbag.setConstraints(x, c);
        ct.add(x);
                 
        JPanel bp = getOneButtonPanel();
  
        resetState();
        
        subInnerCore.add(bp);       
    }
    
    public void showBackupKeysDone() {
        boolean isError = !ps.errText.equals("");
        JPanel subInnerCore;
        
        if (isError) {
            subInnerCore = getModalJPanel("Error");
            AppUI.hr(subInnerCore, 32);
            maybeShowError(subInnerCore);
            resetState();
            return;
        }
        
        subInnerCore = getModalJPanel("Export Complete");
        maybeShowError(subInnerCore);
        
        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);
        
        int y = 0;
        
        JLabel x = new JLabel("<html><div style='width:400px; text-align:center'>" +
            "Your ID keys have been backed up into" +
            "</div></html>");
          
        AppUI.setCommonFont(x);
 
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;
        
        
        final String fdir = ps.chosenFile;
        JLabel sl = AppUI.getHyperLink(fdir, "javascript:void(0); return false", 20);
        sl.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (!Desktop.isDesktopSupported())
                    return;
                try {
                    Desktop.getDesktop().open(new File(fdir));
                } catch (IOException ie) {
                    wl.error(ltag, "Failed to open browser: " + ie.getMessage());
                }
            }
        });
        
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(sl, c);
        ct.add(sl);
        
        y++;

        JPanel bp = this.getOneButtonPanel();
        resetState();
        
        subInnerCore.add(bp);     
    
    }
    
    public void showBackupDoneScreen() {
        boolean isError = !ps.errText.equals("");
        JPanel subInnerCore;
        
        if (isError) {
            subInnerCore = getModalJPanel("Error");
            AppUI.hr(subInnerCore, 32);
            maybeShowError(subInnerCore);
            resetState();
            return;
        }
        
        subInnerCore = getModalJPanel("Backup Complete");
        maybeShowError(subInnerCore);
        
        JPanel ct = new JPanel();
        AppUI.noOpaque(ct);
        subInnerCore.add(ct);
        
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();      
        ct.setLayout(gridbag);
        
        int y = 0;
        
        JLabel x = new JLabel("<html><div style='width:400px; text-align:center'>" +
            "Your CloudCoins from <b></b> have been backed up into" +
            "</div></html>");
          
        AppUI.setCommonFont(x);
 
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;
        
        
        final String fdir = ps.chosenFile;
        JLabel sl = AppUI.getHyperLink(fdir, "javascript:void(0); return false", 20);
        sl.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (!Desktop.isDesktopSupported())
                    return;
                try {
                    Desktop.getDesktop().open(new File(fdir));
                } catch (IOException ie) {
                    wl.error(ltag, "Failed to open browser: " + ie.getMessage());
                }
            }
        });
        
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(sl, c);
        ct.add(sl);
        
        y++;
    
        x = new JLabel("<html><div style='width:400px; text-align:center'>" +
           "All backups are unencrypted. You should save them in a secure location. The CloudCoin Consortium "
                + "recommends opening a free account at </div></html>"
                + "to store backups and passwords.</div></html>");
          
        AppUI.setFont(x, 18);
        c.insets = new Insets(20, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        
        y++;
        
        x = AppUI.getHyperLink("https://SecureSafe.com", "https://securesafe.com", 18);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);
        
        y++;
        
        x = new JLabel("<html><div style='width:400px; text-align:center'>to store backups and passwords.</div></html>");
        AppUI.setFont(x, 18);
        c.insets = new Insets(0, 0, 4, 0);
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        gridbag.setConstraints(x, c);
        ct.add(x);

        JPanel bp = this.getOneButtonPanel();
        
        resetState();
        
        subInnerCore.add(bp);     
    }
    
    
    
    public void showImportDoneScreen() {
        
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
        
        String total = AppCore.formatNumber(ps.statToBankValue + ps.statFailedValue + ps.statLostValue);
        String totalBankValue = AppCore.formatNumber(ps.statToBankValue);
        String totalFailedValue = AppCore.formatNumber(ps.statFailedValue);
        String totalLostValue = AppCore.formatNumber(ps.statLostValue);
        String totalFailedFiles = AppCore.formatNumber(ps.failedFiles);
        
        JLabel x;
        x = new JLabel("<html><div style='width:400px; text-align:center'>Deposited <b>" +  total 
                +  " CloudCoins</b> to <b> </b></div></html>");
        
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
        x = new JLabel("Total Authentic Coins:");
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
        x = new JLabel("Total Counterfeit Coins:");
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
        x = new JLabel("Total Lost Coins:");
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
        
        
        JPanel bp = getTwoButtonPanelCustom("Next Deposit", "Continue", new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                resetState();
                //ps.currentScreen = ProgramState.SCREEN_PREDEPOSIT;
                ps.isSkyDeposit = false;
                ps.currentScreen = ProgramState.SCREEN_DEPOSIT;
                showScreen();
            }
        },  new ActionListener() {
            public void actionPerformed(ActionEvent e) {
               // setActiveWallet(ps.dstWallet);
                ps.sendType = 0;
                ps.currentScreen = ProgramState.SCREEN_SHOW_TRANSACTIONS;
                showScreen();
            }
        });
        
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
                ps.currentScreen = ProgramState.SCREEN_DEFAULT;
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
                ps.currentScreen = ProgramState.SCREEN_DEFAULT;
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
        AppUI.setSize(mwrapperPanel, tw - 260, th);

        JPanel subInnerCore = AppUI.createRoundedPanel(mwrapperPanel, color, 20);
        AppUI.setSize(subInnerCore, tw - 260, th - headerHeight - 120);
        
        corePanel.add(mwrapperPanel);
        
       // if (!ps.isEchoFinished)
         //   sm.startEchoService(new EchoCb());
        
        return subInnerCore;
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
                ps.currentScreen = ProgramState.SCREEN_CREATE_WALLET;
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
    
    
   
}


