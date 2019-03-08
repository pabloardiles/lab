/*
 * @(#)PJAGraphicsManager.java   05/30/2000
 *
 * Copyright (c) 2000-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * Better GIF detection from content type by Fernando Echeverria
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 * Visit eTeks web site for up-to-date versions of this file and other
 * Java tools and tutorials : http://www.eteks.com/
 */
package com.eteks.awt;

import com.eteks.awt.image.GIFDecoder;
import sun.awt.image.ByteArrayImageSource;

import java.awt.*;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.IndexColorModel;
import java.awt.peer.FontPeer;
import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.StringTokenizer;

// Usefull Sun classes
// Java 2 classes

/**
 * Pure Java AWT Manager. This manager implements most of the methods required in an AWT Toolkit and
 * manages .pjaf fonts loading.<BR>
 * From version 1.1, <code>com.PJAFontCapture</code> is used to capture the fonts.
 * PJA 1.0 .pjaf font files are not compatible with PJA 1.1, please use font capture utility to produce
 * PJA 1.1 font files.
 * This class is abstract ; an instance can be obtained calling the method <code>getDefaultGraphicsManager ()</code>.
 * You may change the default behavior of <code>PJAGraphicsManager</code> by creating a new class extending this one,
 * and setting the <code>com.eteks.awt.graphicsmanager</code> system property with the new class name.<BR>
 * The main purpose of the existence of this class is to give access to other classes to Toolkit services,
 * without having an instance of <code>java.awt.Toolkit</code> class at disposal.
 * Actually, <code>com.PJAToolkit</code> extends <code>java.awt.Toolkit</code>.
 * When <code>com.PJAToolkit</code> is instantiated, both classes are loaded by class loader and
 * their <code>static</code> initializers are executed : at this moment <code>java.awt.Toolkit</code>
 * needs awt system library to call <code>initIDs</code> native method. If the privilege to laod awt library
 * is denied, the classes <code>java.awt.Toolkit</code> and also <code>com.PJAToolkit</code> won't be loaded
 * and it will be impossible to instantiate them.<BR>
 * As the class <code>com.PJAGraphics</code> needs font resources for <code>drawString ()</code> method,
 * it will able to use them thanks to an instance of <code>PJAGraphicsManager</code> and without any Toolkit instance,
 * thus enabling to draw in a <code>com.PJAImage</code> instance even in case security control is
 * very restrictive.<BR>
 * Anyway, a lot of AWT classes (in particular, <code>java.awt.Color</code>, <code>java.awt.Rectangle</code>,
 * <code>java.awt.Font</code>, <code>java.awt.FontMetrics</code>, <code>java.awt.image.ColorModel</code>)
 * need also the class <code>java.awt.Toolkit</code> or with JDK 1.2 have an <code>initIDs</code> native method 
 * that needs the library awt.
 * This could prevent from using some methods of the class <code>java.awt.Graphics</code>.
 * <code>PJAGraphics</code> implements <code>com.PJAGraphicsExtension</code> methods that you can
 * use to perform graphics operation if <code>java.awt.Toolkit</code> or awt library can't be accessed.
 *
 * @version   2.5
 * @author    Emmanuel Puybaret
 * @see       PJAToolkit
 * @see       PJAGraphics
 * @see       PJAFontPeer
 * @see       PJAGraphicsManager#isClassAccessible
 * @since     PJA1.1
 */
public abstract class PJAGraphicsManager
{
  private static final int  DEFAULT_SCREEN_WIDTH  = 1024;
  private static final int  DEFAULT_SCREEN_HEIGHT = 768;
  private static final int  DEFAULT_SCREEN_RES    = 75; // Resolution for a 15' screen

  private Hashtable  fontTable      = new Hashtable ();
  private String []  fontList       = null;
  private String     lastLoadedFont = null;
  private String     fontPath       = null;

  private ColorModel colorModel     = null;

  private static PJAGraphicsManager graphicsManager;  
  private static boolean            java2DAvailable;

  static
  {
    try
    {
      // Test Java2D availability (JVM version >= 1.2)
      String JVMVersion = System.getProperty ("java.version", "1.0");
      StringTokenizer tokens = new StringTokenizer (JVMVersion, ".");
      if (Integer.parseInt (tokens.nextToken ()) >= 2)
        java2DAvailable = true;
      else if (   tokens.hasMoreTokens ()
               && Integer.parseInt (tokens.nextToken ()) >= 2)
        java2DAvailable = true;
    }
    catch (SecurityException e)
    { }
    // v2.3 : Some users experienced problems with weired JVM version
    //        In that case, consider Java2D isn't available
    catch (NumberFormatException e)
    { }
  }
      
  /**
   * Creates a default instance of <code>PJAGraphicsManager</code> and loads fonts.
   */
  protected PJAGraphicsManager ()
  {
    // Test if the class java.awt.image.ColorModel can be loaded
    boolean classColorModelAccessible = isClassAccessible ("java.awt.image.ColorModel");
    if (classColorModelAccessible)
    {
      String colorModelClassName = null;
      try
      {
        // v2.3 : Check user's required Color model
        colorModelClassName = System.getProperty ("com.eteks.awt.colormodel");
      }
      catch (SecurityException e)
      { }
      
      if (   colorModelClassName != null
          && isClassAccessible (colorModelClassName))
        try
        {
          colorModel = (ColorModel)Class.forName (colorModelClassName).newInstance ();
        }
        catch (Exception e)
        { }
        
      // If the color model class can't be loaded, use default RGB color model
      if (colorModel == null)
        colorModel = ColorModel.getRGBdefault ();
    }
    
    try
    {
      if (fontList == null)
        loadFonts ();
    }
    catch (Exception ex)
    { } // To bad we won't
  }

  // v2.5 : Moved InstantiablePJAGraphicsManager out of getDefaultGraphicsManager because of compilation problems with JDK 1.1.8
  // Declare a class extending PJAGraphicsManager that can be instantiate
  private static class InstantiablePJAGraphicsManager extends PJAGraphicsManager
  { }  
  
  /**
   * Returns a default <code>PJAGraphicsManager</code>. This method is programmed in the same style
   * as <code>java.awt.Toolkit</code> : it tries to instantiate either the class set in the <code>com.eteks.awt.graphicsmanager</code>
   * system property or the class <code>com.PJAGraphicsManager</code> if the property is not set.
   * This allows PJA users to change the way fonts and other stuff used by this class are managed.
   */
  public static synchronized PJAGraphicsManager getDefaultGraphicsManager ()
  {
    // If no graphics manager is available
    // v2.0 : Added PJAGraphicsManager2D class to manage Java2D
    // v2.1 : PJAGraphicsManager instance can't be changed in this method.
    //        After all, discovered that different ClassLoaders was a better way
    //        to have different PJAGraphicsManager instances in the same JVM.
    if (graphicsManager == null)
    {
      String managerClassName = null;
      try
      {
        managerClassName = System.getProperty ("com.eteks.awt.graphicsmanager");
      }
      catch (SecurityException e)
      { }
      
      try
      {
        if (managerClassName == null)
          graphicsManager = useJava2D () 
                              ? (PJAGraphicsManager)Class.forName ("com.PJAGraphicsManager2D").newInstance ()
                              : new InstantiablePJAGraphicsManager ();
        else
          graphicsManager = (PJAGraphicsManager)Class.forName (managerClassName).newInstance ();
      }
      catch (IllegalArgumentException e)
      { // thrown by forName () if managerClassName == ""
        throw new AWTError ("No GraphicsManager defined");
      }
      catch (ClassNotFoundException e)
      {
        throw new AWTError ("GraphicsManager not found : " + managerClassName);
      }
      catch (InstantiationException e)
      {
        throw new AWTError ("Could not instantiate GraphicsManager : " + managerClassName);
      }
      catch (IllegalAccessException e)
      {
        throw new AWTError ("Could not access GraphicsManager : " + managerClassName);
      }
      // v2.1 : Added this case thrown if super class is not PJAGraphicsManager
      catch (ClassCastException e)
      {
        throw new AWTError (String.valueOf (managerClassName) + " not a subclass of com.PJAGraphicsManager");
      }
    }
    return graphicsManager;
  }

  private static boolean useJava2D ()
  {
    // This method is here and not in PJAGraphicsManager2D to avoid to load that class
    // Some class loaders under JDK 1.1 may try to load all PJAGraphicsManager2D Java2 dependant classes
    // and fail because they can't find Java2 classes...
    try
    {
      // if com.eteks.awt.nojava2d=true (false by default), Java2D is ignored
      return    !Boolean.getBoolean ("com.eteks.awt.nojava2d")
             && java2DAvailable    
             && Class.forName ("com.PJAGraphicsManager2D") != null
             && isFontInstantiableInternal (); 
    }
    catch (ClassNotFoundException e)
    { }
    catch (LinkageError error)
    { }  // Thrown by static initializer which requires awt library
    
    return false;
  }

  /**
   * Returns <code>true</code> if it successes to load the class <code>className</code>.
   * If security manager is too restictive, it is possible that the classes <code>java.awt.Color</code>, 
   * <code>java.awt.Rectangle</code>, <code>java.awt.Font</code>, <code>java.awt.FontMetrics</code> 
   * and <code>java.awt.image.ColorModel</code> (and also <code>java.awt.Dimension</code> and other classes 
   * not required by PJA classes) can't be loaded because they need either the class <code>java.awt.Toolkit</code> 
   * or the library awt to be accessible to call their <code>initIDs ()</code> native method.
   * @param  className  the fully qualified class name.
   * @return <code>true</code> if <code>java.awt.Toolkit</code> class could be loaded.
   */
  public boolean isClassAccessible (String className)
  {
    // Test if the class className can be loaded
    try
    {
      Class.forName (className);
      // Class can be loaded
      return true;
    }
    catch (ClassNotFoundException e)
    { }
    catch (LinkageError error)
    { }  // Thrown by some AWT classes which require awt library in static initializer.

    return false;
  }

  /**
   * Returns <code>true</code> if it successes to load instantiate a <code>java.awt.Font</code>.
   * @return <code>true</code> if <code>java.awt.Font</code> could be intantiated.
   */
  public boolean isFontInstantiable ()
  {
    return isFontInstantiableInternal ();
  }

  // v2.1 : renamed from _isFontInstantiable to isFontInstantiableInternal
  private static boolean isFontInstantiableInternal ()
  {
    // Test if an instance of Font can be created
    try
    {
      new Font ("Helvetica", Font.PLAIN, 10);
      return true;
      // JDK 1.1 : Font instantiation needs a Toolkit instance that could be instantiated
      // JDK 1.2 : Font instantiation requires a GraphicsEnvironment that could be instantiated
    }
    catch (AWTError error)
    { }  // With Java 1.1, Font constructor needs a default Toolkit that may be impossible to load
    catch (IllegalArgumentException e)
    { }
    catch (SecurityException error)
    { }  // With Java 1.1, Font constructor needs a default Toolkit that may be forbidden to load
    catch (LinkageError error)
    { }  // Thrown by static initializer which requires awt library
    // v2.0 : Added this case thrown sun.awt.X11GraphicsEnvironment initDisplay () native method
    catch (InternalError error)
    { }  // Thrown by static initializers of Font and GraphicsEnvironment which may require a Display
    // v2.1 : Added this case thrown if super classes are not as required
    catch (ClassCastException e)
    { }
    // v2.3.2 : Added this case thrown if getLocalGraphicsEnvironment () of GraphicsEnvironment fails
    catch (Error e)
    { }

    return false;
  }
  
  /**
   * Returns a <code>FontPeer</code> instance matching to font <code>name</code>
   * with <code>style</code>. If font directory changed, any font available in the
   * new directory will be loaded first. This enables to share a JVM with different
   * users and different font directories.<br>
   * If Java2D is used, a dummy <code>FontPeer</code> is returned.
   * @return       Always return a <code>FontPeer</code> object. If the font name
   *               isn't available the <code>FontPeer</code> of the default font name
   *               is returned.
   * @param name   The font name.
   * @param style  The font style (<code>Font.PLAIN</code>, <code>Font.ITALIC</code>,
   *               <code>Font.BOLD</code> or <code>Font.BOLD | Font.ITALIC</code>)
   * @throws AWTError If no font is available.
   * @see   #loadFonts
   */
  public FontPeer getFontPeer (String name, int style)
  {
    return getFontPeer (name, style, true);
  }
  
  FontPeer getFontPeer (String name, int style, boolean usePJAFont)
  {
    if (name == null)
      throw new IllegalArgumentException ("Font name can't be null");

    // If directory changed reload any new available fonts
    if (   fontPath == null
        || !fontPath.equals (getFontsPath ()))
      loadFonts ();

    // Get PJAFontPeer objects array with the font name
    PJAFontPeer [] fontStylesPeer = (PJAFontPeer [])fontTable.get (name);
    if (   fontStylesPeer == null
        && getDefaultFont () != null) // v2.0 : must check if default font exists
      // If font not found use the default font instead
      fontStylesPeer = (PJAFontPeer [])fontTable.get (getDefaultFont ());

    if (fontStylesPeer == null)
      throw new AWTError ("No default font : at least one .pjaf font must be loaded.");

    // Find the first available style
    PJAFontPeer fontPeer = null;
    for (int i = 0; i < fontStylesPeer.length; i++)
      if ((fontPeer = fontStylesPeer [i]) != null)
        break;
    // Seek the closest style
    if (style == Font.PLAIN)
    {
      if (fontStylesPeer [0] != null)
        fontPeer = fontStylesPeer [0]; // PLAIN
      else if (fontStylesPeer [2] != null)
        fontPeer = fontStylesPeer [2]; // BOLD
    }
    else if (style == Font.BOLD)
    {
      if (fontStylesPeer [2] != null)
        fontPeer = fontStylesPeer [2]; // BOLD
    }
    else if (style == Font.ITALIC)
    {
      if (fontStylesPeer [1] != null)
        fontPeer = fontStylesPeer [1]; // ITALIC
      else if (fontStylesPeer [3] != null)
        fontPeer = fontStylesPeer [3]; // BOLD ITALIC
    }
    else if (style == (Font.ITALIC | Font.BOLD))
      if (fontStylesPeer [3] != null)
        fontPeer = fontStylesPeer [3]; // BOLD ITALIC
      else if (fontStylesPeer [1] != null)
        fontPeer = fontStylesPeer [1]; // ITALIC
      else if (fontStylesPeer [2] != null)
        fontPeer = fontStylesPeer [2];  // BOLD

    return fontPeer;
  }

  /**
   * Returns a default screen width for this manager (1024pixels).
   */
  public int getScreenWidth ()
  {
    return DEFAULT_SCREEN_WIDTH;
  }

  /**
   * Returns a default screen height for this manager (768 pixels).
   */
  public int getScreenHeight ()
  {
    return DEFAULT_SCREEN_HEIGHT;
  }

  /**
   * Returns a default screen resolution for this manager
   * (75 dpi which is the resolution of a 15' screen displaying 1024 x 768 pixels).
   */
  public int getScreenResolution ()
  {
    return DEFAULT_SCREEN_RES;
  }

  /**
   * Returns the default color model used by images for this manager
   * (RGB default color model).
   */
  public ColorModel getColorModel ()
  {
    // v2.0 : May return null if ColorModel class can't be loaded.
    return colorModel;
  }

  /**
   * Returns the index of the closest color of <code>ARGB</code> 
   * in the indexed color model <code>colorModel</code>.
   *
   * @param  colorModel an indexed color model.
   * @param  ARGB       a color coded in the default color model.
   * @return if alpha chanel == 0, returns the index returned by <code>getTransparentPixel ()</code>
   *         on <code>colorModel</code>. If this index is -1, 0 is returned.
   *         The returned color index is the index of the color with the smallest distance between the 
   *         given ARGB color and the colors of the color model.
   * @since  PJA2.3
   */
  public int getClosestColorIndex (IndexColorModel colorModel, int ARGB)
  {
    int a = (ARGB >> 24) & 0xFF; 
    if (a == 0)
      return colorModel.getTransparentPixel () != -1
               ? colorModel.getTransparentPixel ()
               : 0;

    int r = (ARGB >> 16) & 0xFF;
    int g = (ARGB >> 8) & 0xFF;
    int b = ARGB & 0xFF;
    int colorsCount = colorModel.getMapSize ();
    int colorIndex  = 0;
    int minDistance = Integer.MAX_VALUE;
    for (int i = 0; i < colorsCount; i++)
    {
      int aDif = a - colorModel.getAlpha (i);
      int rDif = r - colorModel.getRed (i);
      int gDif = g - colorModel.getGreen (i);
      int bDif = b - colorModel.getBlue (i);
      int distance =   aDif * aDif
                     + rDif * rDif
                     + gDif * gDif
                     + bDif * bDif;
      if (distance < minDistance)
      {
        minDistance = distance;
        colorIndex = i;
      }
    } 
    
    return colorIndex;  
  }

  /**
   * Returns the current font path. It is the path recorded in the <code>java.awt.fonts</code> system
   * property if it exists followed by the <code>user.dir</code> directory.
   * The returned path may be a set of directories separated by <code>File.pathSeparator</code> characters.
   */
  public String getFontsPath ()
  {
    String fontsPath = "";
    // Fonts are either in the java2d fonts dir or in the current directory
    // v2.1 : Added a try catch block
    try
    {
      fontsPath = System.getProperty ("java.awt.fonts", "");
    }
    catch (SecurityException e)
    { }

    try
    {
      if ("".equals (fontsPath))
        fontsPath = System.getProperty ("user.dir");
      else
        fontsPath += File.pathSeparator + System.getProperty ("user.dir");
    }
    catch (SecurityException e)
    { }

    return fontsPath;
  }

  /**
   * Returns a font directory. It is either the first directory of the path recorded in
   * the <code>java.awt.fonts</code> system property, or the <code>user.dir</code> directory
   * if that property is not set.
   */
  public String getFontsDirectory ()
  {
    // Fonts are either in the java2d fonts path or in the current directory
    String fontsPath = getFontsPath ();
    if ("".equals (fontsPath))
      return "";
    else
      return new StringTokenizer (fontsPath, File.pathSeparator).nextToken();
  }

  /**
   * Loads all font files (with extension .pjaf) contained in the path returned by
   * {@link #getFontsPath}. It's the main method called to load fonts in the graphics manager.<BR>
   * Override it if you want to get files from somewhere else than the file system.
   * @see PJAGraphicsManager#getFontsPath
   * @see PJAGraphicsManager#loadFont(InputStream)
   */
  public void loadFonts ()
  {
    fontPath = getFontsPath ();
    if (!"".equals (fontPath))
    {
      StringTokenizer parser = new StringTokenizer (fontPath, File.pathSeparator);
      while (parser.hasMoreTokens())
        loadFonts (parser.nextToken());
    }
    else
      loadFonts ("");
  }

  /**
   * Loads all font files (with extension .pjaf) in the <code>dir</code> directory. May be
   * called more than once.
   * @param dir Directory where the font files are seeked.
   */
  public void loadFonts (String dir)
  {
    String filesList [] = new File (dir).list (new FilenameFilter ()
                           {
                             public boolean accept (File dir, String name)
                             {
                               return name.toLowerCase ().endsWith (".pjaf");
                             }
                           });
    lastLoadedFont = null;
    if (filesList != null)
      for (int i = 0; i < filesList.length; i++)
      {
        File        fontFile = new File (dir, filesList [i]);
        InputStream input = null;
        try
        {
          // Open the file to read the fonts
          input = new BufferedInputStream (new FileInputStream (fontFile));
          loadFont (input);
        }
        catch (IOException e)
        {
          System.err.println ("Can't load font file " + fontFile + "\n" + e);
        }
        finally
        {
          try
          {
            if (input != null)
              input.close ();
          }
          catch (IOException e2)
          { }
        }
      }
  }

  /**
   * Loads the PJA 1.1 fonts from the stream <code>fontStream</code> and add them
   * to graphics manager for <code>PJAGraphics</code> disposal. If the stream
   * doesn't start with <code>PJAFontData.PJAF1_1_FILE_HEADER</code> header, this
   * method simply returns without throwing an exception.
   * @param fontStream A stream from which PJA fonts can be extracted.
   */
  public void loadFont (InputStream fontStream) throws IOException
  {
    try
    {
      DataInputStream input = new DataInputStream (fontStream);

      try
      {
        // Read the version of this file      
        String version = input.readUTF ();
        if (!PJAFontData.PJAF1_1_FILE_HEADER.equals (version))
          // v1.2 : throw exception if unknown stream instead od simply return
          throw new IOException ("Not a PJA font stream"); // Skip unknown streams
      }
      catch (UTFDataFormatException e)
      {
        // v1.2 : Added a comment about font files compatibility
        throw new IOException ("Not a PJA font stream (Warning : PJA 1.0 fonts aren't compatible with PJA 1.1 or higher)");
      }

      // Read PJAFontData objects from the file stopped by EOFException
      while (true)
      {
        // Create a new PJAFontData object and read it
        PJAFontData fontData = new PJAFontData (input);

        // PJAFontPeer instances are stored in a 4 elements table (one for each style).
        // This table is accessible in fontTable Hashtable with the font name used as key
        String fontName  = fontData.getName ();
        int    fontStyle = fontData.getStyle ();
        PJAFontPeer [] fontStylesPeer = (PJAFontPeer [])fontTable.get (fontName);
        if (fontStylesPeer == null)
        {
          fontStylesPeer = new PJAFontPeer [4];
          fontTable.put (fontName, fontStylesPeer);
        }

        // Get the good font peer into which fontData must be added
        PJAFontPeer fontPeer = null;
        if (fontStyle == Font.PLAIN)
        {
          if (fontStylesPeer [0] == null)
            fontStylesPeer [0] = new PJAFontPeer (fontName, fontStyle);
          fontPeer = fontStylesPeer [0];
        }
        else if (fontStyle == Font.ITALIC)
        {
          if (fontStylesPeer [1] == null)
            fontStylesPeer [1] = new PJAFontPeer (fontName, fontStyle);
          fontPeer = fontStylesPeer [1];
        }
        else if (fontStyle == Font.BOLD)
        {
          if (fontStylesPeer [2] == null)
            fontStylesPeer [2] = new PJAFontPeer (fontName, fontStyle);
          fontPeer = fontStylesPeer [2];
        }
        else if (fontStyle == (Font.ITALIC | Font.BOLD))
        {
          if (fontStylesPeer [3] == null)
            fontStylesPeer [3] = new PJAFontPeer (fontName, fontStyle);
          fontPeer = fontStylesPeer [3];
        }

        fontPeer.addFontData (fontData);

        if (lastLoadedFont == null)
          lastLoadedFont = fontName;
      }
    }
    catch (EOFException e)
    {
    }
    finally
    {
      // Font list changed, update it next time
      fontList = null;
    }
  }

  private void updateFontList ()
  {
    // Retrieve font list from the keys of fontTable Hashtable
    fontList = new String [fontTable.size ()];
    int i = 0;
    for (Enumeration enumeration = fontTable.keys ();
         enumeration.hasMoreElements (); i++)
      fontList [i] = (String)enumeration.nextElement ();
  }

  /**
   * Returns an array of all the font names.
   * If Java2D is used, the array <code>{"Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput"}</code>
   * is returned and the list of True Type fonts is obtained from 
   * GraphicsEnvironment getAvailableFontFamilyNames() method.
   */
  public String [] getFontList ()
  {
    if (fontList == null)
      updateFontList ();
    return fontList;
  }

  /**
   * Returns the font metrics of a font.
   */
  public FontMetrics getFontMetrics (Font font)
  {
    return getFontMetrics (font, true);
  }

  FontMetrics getFontMetrics (Font font, boolean usePJAFont)
  {
    return ((PJAFontPeer)getFontPeer (font.getName (), font.getStyle (), usePJAFont)).getFontMetrics (font.getSize ());    
  }

  /**
   * Returns the default font name. You may override this method
   * to have a different font.
   */
  public String getDefaultFont ()
  {
    if (fontList == null)
      updateFontList ();

    if (fontList == null || fontList.length == 0)
      return null;

    for (int i = 0; i < fontList.length; i++)
      if (fontList [i].equalsIgnoreCase ("SansSerif"))
        return fontList [i];
      else if (fontList [i].equalsIgnoreCase ("Helvetica"))
        return fontList [i];

    return lastLoadedFont != null ? lastLoadedFont : fontList [0];
  }

  /**
   * Does nothing. Should sync image loading.
   */
  public void sync ()
  {
  }

  /**
   * Returns an image from the file <code>filename</code>.
   * @see PJAToolkit#getImage(String)
   */
  public Image getImage (String filename)
  {
    try
    {
      Image image = getImage (new URL ("file:" + new File (System.getProperty ("user.dir"), filename)));
      // v2.3 If image is null try find it with getAbsolutePath ()
      return image != null
               ? image 
               : getImage (new URL ("file:" + new File (filename).getAbsolutePath ()));
    }
    catch (MalformedURLException e)
    { }
    // No catch for SecurityException
    
    return null;
  }

  /**
   * Returns an image from the URL <code>url</code>.
   * @see PJAToolkit#getImage(URL)
   */
  public Image getImage (URL url)
  {
    try
    {
      // v2.3
      // Fernando Echeverria : Detect GIF images from content type instead of .gif extension,
      // since some URLs do not have the correct extension. Use extension
      // only as a fallback check in case content type is null.
      java.net.URLConnection conn = url.openConnection();

      // v1.2 : Can't trust default GIF image loader in JDK 1.1 
      // (TODO ? as the java.lang.UnsatisfiedLinkError exception was thrown only under JDK 1.1 
      //         should we trust GIF default system loader from JDK 1.2 ?)
      String type = conn.getContentType();
      if (   "image/gif".equals (conn.getContentType ())
          || url.getFile ().toLowerCase ().endsWith (".gif"))
      {
        try
        {
          // Load class 
          Class c = GIFDecoder.class;
          // v2.4 image InputStream were never closed manually
          // As image data is read later when production is started, we must let the input open
          // and can't close the input in a finally statement
          return createImage (new GIFDecoder (conn.getInputStream (), true));
        }
        catch (LinkageError e)
        {
          // If GIFDecoder not available try default loader
          return createImage ((ImageProducer)(conn.getContent ()));
        }
      }
      else
        return createImage ((ImageProducer)(conn.getContent ()));
    }
    catch (IOException e)
    { }
    return null;
  }

  /**
   * Returns an image from the file <code>filename</code>.
   * @see PJAToolkit#createImage(String)
   */
  public Image createImage (String filename)
  {
    return getImage (filename);
  }

  /**
   * Returns an image from the URL <code>url</code>.
   * @see PJAToolkit#createImage(URL)
   */
  public Image createImage (URL url)
  {
    return getImage (url);
  }

  /**
   * Starts the loading of an image.
   * @see PJAToolkit#prepareImage
   */
  public boolean prepareImage (Image image, int width, int height,
                              ImageObserver observer)
  {
    if (image instanceof PJAImage)
      return ((PJAImage)image).prepare (observer);
    else
      throw new AWTError ("PJA can't manage this image (" + image.getClass ().getName () + ")");
  }

  /**
   * Checks the status of an image laoding.
   * @see PJAToolkit#checkImage
   */
  public int checkImage (Image image, int width, int height,
                         ImageObserver observer)
  {
    if (image instanceof PJAImage)
      return ((PJAImage)image).check (observer);
    else
      throw new AWTError ("PJA can't manage this image (" + image.getClass ().getName () + ")");
  }

  /**
   * Creates an image from the <code>imagedata</code> array.
   * @see PJAToolkit#createImage(byte[],int,int)
   */
  public Image createImage (byte[] imagedata,
                            int imageoffset,
                            int imagelength)
  {
    // v2.3
    // Fernando Echeverria : Detect GIF format and use GIFDecoder directly, to avoid later link
    // error, if possible.
    // v2.4 : Added a test on imagedata length
    if (   imagedata.length >= 4
        && imagedata[0]=='G' 
        && imagedata[1]=='I' 
        && imagedata[2]=='F' 
        && imagedata[3]=='8')
      try
      {
        Class c = GIFDecoder.class;
        return createImage (new GIFDecoder (new ByteArrayInputStream (imagedata, imageoffset, imagelength)));
      }
      catch (LinkageError err)
      { } // let it fall through to default code

    // Same implementation as sun.awt.SunToolkit createImage () method
    // ByteArrayImageSource doesn't exist in JDK 1.0.2 but it's not really a problem
    // since createImage (byte[], int, int) doesn't exist too in the class java.awt.Toolkit of JDK 1.0.2
    return createImage (new ByteArrayImageSource (imagedata, imageoffset, imagelength));
  }

  /**
   * Creates an image from the <code>producer</code>. This is the method that
   * finally creates an instance of <code>PJAImage</code>.
   * @return An instance of the class <code>java.awt.Image</code> (an instance of 
   * either <code>com.PJAImage</code> or <code>com.eteks.awt.PJABufferedImage</code>).
   * @see PJAToolkit#createImage(ImageProducer)
   */
  public Image createImage (ImageProducer producer)
  {
    return new PJAImage (producer);
  }

  /**
   * Creates an image of <code>width x height</code> pixels. This method returns
   * an instance of <code>com.PJAImage</code> if Java2D can't work (JDK < 1.2
   * or because <code>GraphicsEnvironment.getLocalGraphicsEnvironment ()</code> failed).
   * This ensures to always get an <code>Image</code> but may require you to verify
   * if <code>Image getGraphics ()</code> method returns an instance of <code>Graphics</code>
   * or code>Graphics2D</code> (<code>com.PJAImage</code> don't have Java2D and
   * <code>Graphics2D</code> capabilities).<br>
   * This method is called by <code>createImage (int, int)</code> of the class 
   * <code>PJAComponentPeer</code> (and thus of the class <code>java.awt.Component</code>).
   * @return An instance of the class <code>java.awt.Image</code> (an instance of 
   * either <code>com.PJAImage</code> or <code>com.eteks.awt.PJABufferedImage</code>).
   * @param width   Width in pixels of the new image.
   * @param height  Height in pixels of the new image.
   * @see com.eteks.java2d.PJAGraphicsEnvironment
   */
  public Image createImage (int width, int height)
  {
    return new PJAImage (width, height);
  }
  
  /**
   * Returns a <code>GraphicsConfiguration</code> instance required by the the method 
   * <code>getGraphicsConfiguration ()</code> of <code>ComponentPeer</code> interface.
   * @since PJA2.0
   */
  public GraphicsConfiguration getGraphicsConfiguration ()
  {
    return null;
  }
}
