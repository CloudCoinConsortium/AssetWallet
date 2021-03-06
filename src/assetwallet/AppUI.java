/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package assetwallet;

import assetwallet.core.Config;
import assetwallet.core.AppCore;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Map;
import java.util.Properties;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;


import javax.swing.JTextArea;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.basic.BasicScrollBarUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Александр
 */
public class AppUI {
    
    static int tw, th;
    static double ratio;
    
    //AGIEEE
    //static Font montLight, montMedium, montReg;
    static Font regFont, semiBoldFont, boldFont;
    static Font osRegFont, osSemiBoldFont;
    
    public static void init(int tw, int th) {
        AppUI.tw = tw;
        AppUI.th = th;
        
        AppUI.ratio = tw / th;
        
        try {
            ClassLoader cl;
            
            cl = AppUI.class.getClassLoader();
            semiBoldFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-SemiBold.otf"));
            boldFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Bold.otf"));
            regFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Regular.otf"));

            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(regFont);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(boldFont);
            GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(semiBoldFont);

            //AGIEEE
            //montLight = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Light.ttf"));
            //montMedium = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Medium.ttf"));
            //ontReg = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Regular.ttf"));
            
            //GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(montReg);
            //GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(montMedium);
            //GraphicsEnvironment.getLocalGraphicsEnvironment().registerFont(montLight);
            
            
            //osRegFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Medium.ttf"));
            //osSemiBoldFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/Montserrat-Semibold.ttf")); 
            
            osRegFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/OpenSans-Regular.ttf"));
            osSemiBoldFont = Font.createFont(Font.TRUETYPE_FONT, cl.getResourceAsStream("resources/OpenSans-Semibold.ttf"));

        } catch(Exception e){
            System.out.println("Failed to load font: " + e.getMessage());
        }
        
        
    }
    
    public static String getAgreementText() {
        ClassLoader cl;            
        cl = AppUI.class.getClassLoader();
            
        
        InputStream is = cl.getResourceAsStream("resources/TermsAndConditions.html");
        String aText = AppCore.loadFileFromInputStream(is);
        
        return aText;
    }
    
    
    public static void setSize(Component c, double w) {
        int h = (int) (w * ratio);
        
        setSize(c, (int) w, h);
    }
    
    public static void setSize(Component c, int w, int h) {
        c.setPreferredSize(new Dimension(w, h));
        c.setMinimumSize(new Dimension(w, h));
        c.setMaximumSize(new Dimension(w, h));
    }
    
    public static Color getColor0() {
        return new Color(0x0061E1);
    }
    
    public static Color getColor1() {
        return new Color(0xC9DBEE);
    }
    
    public static Color getColor2() {
        return new Color(0xDFEAF5);
    }
    
    public static Color getColor3() {
        //return new Color(0xBFFFFFFF, true);
        return new Color(0xE9F0F8);
    }
    
    public static Color getColor4() {
        return new Color(0x665FA8FF, true);
    }
    
    public static Color getColor5() {
        return Color.WHITE;
    }
    public static Color getColor6() {
        return new Color(0x5FA8FF);
    }
    public static Color getColor7() {
        return new Color(0x90C1FE);
    }
    
    public static Color getColor8() {
        return new Color(0xE5E5E5);
    }
    
    public static Color getColor9() {
         return new Color(0x5FA8FF);
    }
    
    public static Color getColor10() {
        return new Color(0x777777);
    }
    
    public static Color getColor11() {
        return new Color(0xF2F6FB);
    }
    
    public static Color getColor12() {
        return new Color(0xF7F9FC);
    }
       
    public static Color getColor13() {
        return new Color(0x919396);
    }
    
    public static Color getColor14() {
        return new Color(0x007f0e);
    }
    
    
    public static Color getDisabledColor() {
        return new Color(0xCCCCCC);
    }
    
    public static Color getDisabledColor2() {
        return new Color(0x999999);
    }
    
    public static Color getErrorColor() {
        return Color.RED;
    }
    
    public static Color blendColors(Color c0, Color c1) {  
        double r0 = (double) c0.getRed() / 255.0;
        double g0 = (double) c0.getGreen()  / 255.0;
        double b0 = (double) c0.getBlue() / 255.0;
        double a0 = (double) c0.getAlpha() / 255.0;
                
        double r1 = (double) c1.getRed() / 255.0;
        double g1 = (double) c1.getGreen()  / 255.0;
        double b1 = (double) c1.getBlue() / 255.0;
        
        
        double r = (r0 * a0) + (r1 * (1 - a0));
        double g = (g0 * a0) + (g1 * (1 - a0));
        double b = (b0 * a0) + (b1 * (1 - a0));
     
        int ir = (int) (r * 255.0);
        int ig = (int) (g * 255.0);
        int ib = (int) (b * 255.0);
         
        Color nc = new Color(ir,ig,ib);
       
        return nc;
    }
    
    
    public static double getBoxWidth() {
        return AppUI.tw / 3;
    }
    
    public static double getBoxHeight() {
        return AppUI.th / 16;
    }
    
    public static void setBoxLayout(Component c, boolean isVertical) {
        int direction = isVertical ? BoxLayout.PAGE_AXIS : BoxLayout.LINE_AXIS;
        
        ((Container) c).setLayout(new BoxLayout((Container) c, direction));
    }
    
    public static void setBackground(JComponent c, Color color) {
        c.setBackground(color);
    }
    
    public static void alignLeft(JComponent c) {
        c.setAlignmentX(Component.LEFT_ALIGNMENT);
    }
    
    public static void alignCenter(JComponent c) {
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
    }
    
    
    public static void alignTop(JComponent c) {
        c.setAlignmentY(Component.TOP_ALIGNMENT);
    }
    
    public static void alignBottom(JComponent c) {
        c.setAlignmentY(Component.BOTTOM_ALIGNMENT);
    }
    
    public static void noOpaque(JComponent c) {
        c.setOpaque(false);
    }
    
    public static void vr(JComponent c, double size) {
        Component bc = Box.createRigidArea(new Dimension((int) size, 0));
        c.add(bc);
    }
    
    public static void hr(Component c, double size) {
        Component bc = Box.createRigidArea(new Dimension(0, (int) size));
        ((Container) c).add(bc);
    }
    
    public static void setMargin(JComponent c, int margin) {
        c.setBorder(BorderFactory.createEmptyBorder(margin, margin, margin, margin));
    }
    
    public static void setMargin(JComponent c, int top, int left, int bottom, int right) {
        c.setBorder(BorderFactory.createEmptyBorder(top, left, bottom, right));
    }
    
    
    public static void roundCorners(JComponent c, Color color, int radius) {
        c.setBorder(new RoundedBorder(radius, color));
    }
    
    public static void roundCorners(JComponent c, Color color, int radius, UICallBack cb) {
        c.setBorder(new RoundedBorder(radius, color, cb));
    }
    
    //AGIEEE
    public static void underLine(Component label) {
        Font font = label.getFont();
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
        label.setFont(font.deriveFont(attributes));
    }
    
    public static void noUnderLine(Component label) {
        Font font = label.getFont();
        Map attributes = font.getAttributes();
        attributes.put(TextAttribute.UNDERLINE, null);
        label.setFont(font.deriveFont(attributes));
    }
    
    
    public static void setTitleFont(Component c, int size) {
        c.setFont(regFont.deriveFont(Font.PLAIN, size));
        c.setForeground(Color.WHITE);
    }
    
    public static void setTitleBoldFont(Component c, int size) {
        c.setFont(boldFont.deriveFont(Font.BOLD, size));
        c.setForeground(Color.WHITE);
    }
    
    public static void setTitleSemiBoldFont(Component c, int size) {
        c.setFont(semiBoldFont.deriveFont(Font.PLAIN, size));
        c.setForeground(Color.WHITE);
    }
    
    public static void setFont(Component c, int size) {
        c.setFont(regFont.deriveFont(Font.PLAIN, size));
    }
    
    public static void setSemiBoldFont(Component c, int size) {
        c.setFont(semiBoldFont.deriveFont(Font.PLAIN, size));
    }
    
    public static void setBoldFont(Component c, int size) {
        c.setFont(boldFont.deriveFont(Font.PLAIN, size));
    }
    
    
    
    public static void setCommonFont(Component c) {
        AppUI.setFont(c, 25);
    }
    
    public static void setCommonBoldFont(Component c) {
        AppUI.setBoldFont(c, 25);
    }
    
    public static void setCommonTableFont(Component c) {
        c.setFont(osRegFont.deriveFont(Font.PLAIN, 14));
    }
    
    public static void setCommonBoldTableFont(Component c) {
        c.setFont(osSemiBoldFont.deriveFont(Font.PLAIN, 14));
    }
    
    public static void setCommonTableFontSize(Component c, int size) {
        c.setFont(osRegFont.deriveFont(Font.PLAIN, size));
    }
    
    
    public static Component hr(int size) {
        return Box.createRigidArea(new Dimension(0, size));
    }
    
    public static Component vr(double size) {
        return Box.createRigidArea(new Dimension((int) size, 0));
    }
    
    public static void setHandCursor(Component c) {
        c.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }
    
    public static void setDefaultCursor(Component c) {
        c.setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
    
    public static void setColor(Component c, Color color) {
        c.setForeground(color);
    }
    
    public static JFrame getMainFrame(String version) {
        JFrame frame = new JFrame();
        
        frame.setTitle("ECD Vault " + version);
        frame.setLayout(new BorderLayout());
        frame.setSize(new Dimension(tw, th));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
        frame.setResizable(false);
        frame.setVisible(true);
        
        ClassLoader cl;
        cl = AppUI.class.getClassLoader();

        frame.setIconImage(
            //new ImageIcon(cl.getResource("resources/CloudCoinLogo2.png")).getImage()
             new ImageIcon(cl.getResource("resources/icon.png")).getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH)
        );
        
        return frame;
    }
   
   public static JPanel createRoundedPanel(JPanel parent) {
         return createRoundedPanel(parent, AppUI.getColor2(), 60, 20);
    }
    
    public static JPanel createRoundedPanel(JPanel parent, Color color, int sideMargin) {
        return createRoundedPanel(parent, color, sideMargin, 20);
    }
   
    public static JPanel createRoundedPanel(JPanel parent, Color color, int sideMargin, int radius) {
        //JPanel p = new JPanel();
        Bgpanel p = new Bgpanel("resources/ECDTokenVaultBody.png", color);
        
        AppUI.setBoxLayout(p, true);
        AppUI.alignCenter(p);
        //AppUI.roundCorners(p, color, radius);
        AppUI.setMargin(p, 20);
        AppUI.noOpaque(p);
        
        JPanel subInnerCore = new JPanel();
        
        AppUI.setBoxLayout(subInnerCore, true);
        //AppUI.roundCorners(subInnerCore, color, radius);
        AppUI.alignCenter(subInnerCore);
        AppUI.noOpaque(subInnerCore);
        AppUI.setMargin(subInnerCore, 0, sideMargin, 20, sideMargin);
        p.add(subInnerCore);
           
        parent.add(p);
        
        p.setAlignmentY(Component.CENTER_ALIGNMENT);
        subInnerCore.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        return subInnerCore;
    }
    
    public static JPanel createRoundedPanelNoBg(JPanel parent, Color color, int sideMargin, int radius) {
        JPanel p = new JPanel();
        
        AppUI.setBoxLayout(p, true);
        AppUI.alignCenter(p);
        AppUI.roundCorners(p, color, radius);
        //AppUI.setMargin(p, 20);
        AppUI.noOpaque(p);
        
        JPanel subInnerCore = new JPanel();
        
        AppUI.setBoxLayout(subInnerCore, true);
        //AppUI.roundCorners(subInnerCore, color, radius);
        AppUI.alignCenter(subInnerCore);
        AppUI.noOpaque(subInnerCore);
        AppUI.setMargin(subInnerCore, 0, sideMargin, 20, sideMargin);
        p.add(subInnerCore);
           
        parent.add(p);
        
        p.setAlignmentY(Component.CENTER_ALIGNMENT);
        subInnerCore.setAlignmentY(Component.CENTER_ALIGNMENT);
        
        return subInnerCore;
    }
    
    
    //AGIEEE
    public static JLabel getTitle(String s) {
        JLabel str = new JLabel(s);
        AppUI.alignCenter(str);
        AppUI.setBoldFont(str, 30);
        
        return str;
    }
    
    public static JLabel getCommonLabel(String s) {
        JLabel res = new JLabel(s);
        AppUI.setCommonFont(res);
        AppUI.alignCenter(res);
        
        return res;
    }
    
    public static JLabel getCommonBoldLabel(String s) {
        JLabel res = new JLabel(s);
        AppUI.setBoldFont(res, 25);
        AppUI.alignCenter(res);
        
        return res;
    }
    
    public static JPanel getTextBox(String name, String title) {
        JPanel hct = new JPanel();
        
        AppUI.setBoxLayout(hct, false);
        AppUI.setMargin(hct, 0, 28, 0, 0);
        AppUI.noOpaque(hct);
         
        AppUI.setSize(hct, tw / 2, 50);
        
        // Name Label        
        JLabel x = new JLabel(name);
        AppUI.setCommonFont(x);
        hct.add(x);
        
        AppUI.vr(hct, 50);
        
        MyTextField walletName = new MyTextField(title, false);
        hct.add(walletName.getTextField());
         
        return hct;
    }
    
    public static String getRemoteUserOption() {
        return "- Remote User";
    }
    
    public static String getLocalFolderOption() {
        return "- Local Folder";
    }

    public static JScrollPane setupTable(JTable table, String[] columnNames, String[][] data, DefaultTableCellRenderer r) {
        final String[] fcolumnNames = columnNames;
        JScrollPane scrollPane = new JScrollPane(table);
        if (data == null)
            return scrollPane;
        
        DefaultTableModel model = new DefaultTableModel(data.length, data[0].length - 1) {
            @Override
            public String getColumnName(int col) {        
                return fcolumnNames[col];
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                return String.class;
            }
            
            public boolean isCellEditable(int row, int column){  
               return false;  
            }
        };
        
        table.setModel(model);
        for (int row = 0; row < model.getRowCount(); row++) {
            for (int col = 0; col < model.getColumnCount(); col++) {
                model.setValueAt(data[row][col], row, col);
            }
        }
 
        table.setRowHeight(table.getRowHeight() + 15);
        table.setDefaultRenderer(String.class, r);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFocusable(false);
        table.setRowSelectionAllowed(false);
        table.setGridColor(AppUI.getColor7());
        
        
        JTableHeader header = table.getTableHeader();    
        AppUI.setColor(header, Color.WHITE);
        AppUI.noOpaque(header);
        AppUI.setCommonTableFont(table);
        AppUI.setCommonTableFont(header);
        
        final TableCellRenderer hr = table.getTableHeader().getDefaultRenderer();
        header.setDefaultRenderer(new TableCellRenderer() {
            private JLabel lbl;
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                lbl = (JLabel) hr.getTableCellRendererComponent(table, value, true, true, row, column);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                
                //internal transaction table margins
                AppUI.setMargin(lbl, 2, 10, 2, 2);
                AppUI.setBackground(lbl, AppUI.getColor0());
                return lbl;
            }
        });
        
        
        //This is the wallet table size 
        
        AppUI.setSize(scrollPane, 660, 325);
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
        
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setOpaque(false);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        
        return scrollPane;
        
    }
    
    
    public static JLabel getHyperLink(String name, String url, int fontSize) {
        final String fname = name;
        final String furl = url;
        
        JLabel l = new JLabel(name);
        if (fontSize == 0)
            AppUI.setCommonFont(l);
        else
            AppUI.setFont(l, fontSize);
        
        AppUI.setColor(l, AppUI.getColor0());
        AppUI.underLine(l);
        AppUI.setHandCursor(l);
        l.addMouseListener(new MouseAdapter() {
            public void mouseReleased(MouseEvent e) {
                if (Desktop.isDesktopSupported()) {
                    try {
                        URI uri = new URI(furl);
                        Desktop.getDesktop().browse(uri);
                    } catch (IOException ex) { 
  
                    } catch (URISyntaxException ex) {
                        
                    }
                } else { 
                }   
            }
        });
        
        return l;
    }
    
    public static void invertImage(BufferedImage img) {
        // No color change
        if (1==1)
            return;
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                int rgba = img.getRGB(x, y);
                Color col = new Color(rgba, true);
                col = new Color(255 - col.getRed(),
                                255 - col.getGreen(),
                                255 - col.getBlue(), col.getAlpha());
                img.setRGB(x, y, col.getRGB());
            }
        }
    }

    public static void getGBBlock(JComponent parent, JComponent[] items, int y, GridBagLayout gridbag) {
        GridBagConstraints c = new GridBagConstraints();
  
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(12, 10, 10, 10);    
        c.gridy = y;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = GridBagConstraints.RELATIVE;
        
        JPanel jp = new JPanel();
        AppUI.setBoxLayout(jp, true);

        for (int i = 0; i < items.length; i++) {
            if (i == 0)
                AppUI.setMargin(items[i], 2, 0, 0, 0);
            else
                AppUI.setMargin(items[i], 8, 0, 0, 0);
            AppUI.alignCenter(items[i]);
            AppUI.setFont(items[i], 14);
            jp.add(items[i]);
        }

        AppUI.setSize(jp, 200, 180);
        AppUI.setMargin(jp, 10);
        AppUI.alignCenter(jp);
        AppUI.noOpaque(jp);
        //AppUI.setBackground(jp, AppUI.getColor5());
        
        gridbag.setConstraints(jp, c);
        parent.add(jp);
     
    }
    
    public static void getGBRow(JComponent parent, JComponent[] items, int y, GridBagLayout gridbag) {
        GridBagConstraints c = new GridBagConstraints();
  
        c.anchor = GridBagConstraints.NORTH;
        c.insets = new Insets(20, 10, 10, 10);    
        c.gridy = y;
        c.weightx = 1;
        c.weighty = 0;
        c.gridx = GridBagConstraints.RELATIVE;
        
        c.gridwidth = 1;
        if (items.length == 1)
            c.gridwidth = 4;
                
        for (int i = 0; i < items.length; i++) {
            if (items[i] instanceof JLabel)
                AppUI.setCommonFont(items[i]);
         
            gridbag.setConstraints(items[i], c);
            parent.add(items[i]);
        }
    }
    
    public static void GBPad(JComponent parent, int y, GridBagLayout gridbag) {
        GridBagConstraints c = new GridBagConstraints();
        JPanel padder = new JPanel();
        AppUI.noOpaque(padder);
        c.anchor = GridBagConstraints.NORTHWEST;
        c.insets = new Insets(0, 0, 0, 0);    
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = y;
        c.weightx = 1;
        c.weighty = 1;
        gridbag.setConstraints(padder, c);
        parent.add(padder);
    }
    
    public static void setMetaItem(Properties meta, JPanel jp, String key, String value) {
        String propname = AppCore.getMetaItem(meta, key);
        if (propname != null) {
            JLabel jl = new JLabel();
            AppUI.setFont(jl, 16);
            jl.setText("<html><b>" + value + "</b> " + propname + "</html>");
            AppUI.setMargin(jl, 10, 0, 0, 0);
            AppUI.alignCenter(jl);
            jp.add(jl);
        }
    }
    
    public static void setItem(JPanel jp, String key, String value) {
        JLabel jl = new JLabel();
        AppUI.setFont(jl, 16);
        jl.setText("<html><b>" + key + "</b> " + value + "</html>");
        AppUI.setMargin(jl, 10, 0, 0, 0);
        AppUI.alignCenter(jl);
        jp.add(jl);
        
    }
    
    
    public static Color hex2Rgb(String colorStr) {
        return new Color(
            Integer.valueOf( colorStr.substring( 1, 3 ), 16 ),
            Integer.valueOf( colorStr.substring( 3, 5 ), 16 ),
            Integer.valueOf( colorStr.substring( 5, 7 ), 16 ) );
    }
}
