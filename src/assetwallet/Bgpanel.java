
package assetwallet;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import javax.swing.JPanel;




class Bgpanel extends JPanel {
    
    Image img;
    
    public Bgpanel(String imgPath, Color color) {
        try {
            img = ImageIO.read(getClass().getClassLoader().getResource(imgPath));
            img = img.getScaledInstance(1160, 575, Image.SCALE_SMOOTH);
            
            BufferedImage bimage = new BufferedImage(img.getWidth(null), img.getHeight(null), 
                    BufferedImage.TYPE_INT_ARGB);
            
            
            Graphics2D g2 = bimage.createGraphics();
  //          g2.setComposite(AlphaComposite.Src);
            //g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.1f));
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(color);
            g2.fill(new RoundRectangle2D.Float(0, 0, bimage.getWidth(), bimage.getHeight(), 20, 20));
            //g2.setComposite(AlphaComposite.SrcAtop);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_ATOP, 0.3f));
            
            g2.drawImage(img, 0, 0, null);

            g2.dispose();

            img = bimage;
        } catch (Exception e) {
            
        }
    }
    
    public Bgpanel(String imgPath) {
        try {
            img = ImageIO.read(getClass().getClassLoader().getResource(imgPath));
            
        } catch (Exception e) {
            
        }
    }
    
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        g.drawImage(img, 0, 0, null);
    }
    
}