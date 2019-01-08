package es.ieci.tecdoc.fwktd.applets.scan.ui;

import java.applet.AppletContext;
import java.applet.AppletStub;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import javax.swing.JFrame;
import javax.swing.JRootPane;

import es.ieci.tecdoc.fwktd.applets.scan.applet.IdocApplet;

/**
 * 
 * @author anadal(u80067)
 *
 */
public class MainFrame {

  private static final Dimension PREF_SIZE = new Dimension(550, 350);

  
  public static class  ScanAppletStub implements AppletStub {

    final Properties prop ;
    
    
    public ScanAppletStub(Properties prop) {
      super();
      this.prop = prop;
    }

    @Override
    public boolean isActive() {
      return true;
    }

    @Override
    public URL getDocumentBase() {
      String db = prop.getProperty("DocumentBase");
      if (db == null) {
        return null;
      } else {
        try {
          return new URL(db);
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          return null;
        }
      }
    }

    @Override
    public URL getCodeBase() {
      String db = prop.getProperty("CodeBase");
      if (db == null) {
        return null;
      } else {
        try {
          return new URL(db);
        } catch (MalformedURLException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
          return null;
        }
      }
    }

    @Override
    public String getParameter(String name) {
      return  prop.getProperty(name);
    }

    @Override
    public AppletContext getAppletContext() {

      return null;
    }

    @Override
    public void appletResize(int width, int height) {
    }
    
  }
  
  
  
  private static void createAndShowUI(String[] args) {
    

    StringBuffer stb = new StringBuffer();
    
    for (int i = 0; i < args.length; i++) {
      stb.append(args[i]).append("\n");
    }
    
    Properties prop = new Properties();
    
    try {
      prop.load(new ByteArrayInputStream(stb.toString().getBytes("UTF-8")));
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
    
    if ("true".equals(prop.getProperty("isJNLP"))) {
      System.setProperty("isJNLP", "true");
    }
    
    if ("true".equals(prop.getProperty("closeWhenUpload"))) {
      System.setProperty("closeWhenUpload", "true");
    }
    
    
    ScanAppletStub stub = new ScanAppletStub(prop);
    
    
    IdocApplet applet = new IdocApplet();
    
    applet.setStub(stub);
    
    applet.init();
    
    
    JFrame frame = new JFrame("ScanApplet");
    
    JRootPane panell = frame.getRootPane();

    panell.setLayout(new BorderLayout());
    panell.add(applet, BorderLayout.CENTER);
    
    applet.start();
    
    

    applet.setSize(PREF_SIZE);
    applet.setPreferredSize(PREF_SIZE);
    applet.setMinimumSize(PREF_SIZE);
    

    // Get the size of the screen
    Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
    
    frame.pack();

    // Determine the new location of the window
    int w = frame.getSize().width;
    int h = frame.getSize().height;
    int x = (dim.width - w) / 2;
    int y = (dim.height - h) / 2;

    // Move the window
    frame.setLocation(x, y);

    frame.setVisible(true);
    
    
    
    /*
    
    JFrame frame = new JFrame("Scan");
    frame.setPreferredSize(PREF_SIZE);
    frame.getContentPane().add(myPanel);    
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setVisible(true);
    */
 }

 private static String[] args = null;
  
 public static void main(String[] argsCL) {
   
   args = argsCL;
   
    java.awt.EventQueue.invokeLater(new Runnable() {
       public void run() {
          createAndShowUI(args);
       }
    });
 }
 
 
 
 
  
}
