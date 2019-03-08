/*
 * @(#)PJAToolkit.java   05/16/2000
 *
 * Copyright (c) 2000-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
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

import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.*;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

// Java 2 classes

/**
 * Pure Java AWT Toolkit implementation. This toolkit enables to draw in offscreen images
 * with all the <code>Graphics</code> methods, even if no X11 or other device display is available.
 * Its main purpose is for servlets returning graphics and runing on servers with no display.
 * Java programs using AWT and compliant with any Java version,
 * will work directly on this toolkit with the following modification.<BR>
 * To enable automatic switch to this toolkit,
 * the system property <code>awt.toolkit</code> must be changed to this class name
 * <code>com.PJAToolkit</code>, using either ways :
 * <UL><LI>Passing the argument <code>-Dawt.toolkit=com.PJAToolkit</code> in your java command line.
 * <LI>Or changing in your program the <code>awt.toolkit</code> system property :
 * <BLOCKQUOTE><PRE>
 *  Properties prop = System.getProperties ();
 *  prop.put ("awt.toolkit", "com.PJAToolkit");
 *  System.setProperties (prop);
 * </PRE></BLOCKQUOTE>
 * This can be done more easily in Java 1.2 :
 * <BLOCKQUOTE><PRE>
 *  System.setProperty ("awt.toolkit", "com.PJAToolkit");
 * </PRE></BLOCKQUOTE>
 * </LI></UL>
 * <P>If JDK version if greater or equal to 1.2 and no Display is available the two next properties
 * must be also set, using one of the previous ways (see also {@link com.eteks.java2d.PJAGraphicsEnvironment}) :
 * <ul><li><code>java.awt.graphicsenv</code> system property have to be set 
 * to <code>com.PJAGraphicsEnvironment</code> to allow the change
 * of <code>java.awt.GraphicsEnvironment</code> default implementation.</li>
 *     <li><code>java.awt.fonts</code> system property must be set to the 
 * path where True Type fonts files will be loaded from.</li></ul>
 * <P>Images can't be created using one of the three ways :
 * <UL><LI>Using a dummy <code>java.awt.Frame</code> instance to be able to call <code>createImage ()</code> :
 * <BLOCKQUOTE><PRE>
 *   Frame frame = new Frame ();
 *   frame.addNotify ();
 *   Image image = frame.createImage (width, height);
 * </PRE></BLOCKQUOTE></LI>
 * <LI>Using directly a <code>come.PJAImage</code> instance, created with :
 * <BLOCKQUOTE><PRE>
 *   Image image = new com.PJAImage (width, height);
 * </PRE></BLOCKQUOTE>
 * This second way is usefull when <code>Toolkit.getDefaultToolkit ()</code> fails because
 * the Java security manager forbids access to Java AWT Toolkit or because
 * it can't load the class set in <code>awt.toolkit</code> system property
 * (pja.jar must be in bootclasspath).</LI>
 * <LI>Using a <code>java.awt.image.BufferedImage</code> instance (supported only 
 * with JDK version >= 1.2), created for example with :
 * <BLOCKQUOTE><PRE>
 *   Image image = new java.awt.image.BufferedImage (width, height, java.awt.image.PJABufferedImage.TYPE_INT_ARGB);
 * </PRE></BLOCKQUOTE></LI><BR>
 * Here's a way to program a createImage () method which helps to always get an image instance :
 * <BLOCKQUOTE><PRE>
 *  public Image createImage (int width, int height) throws Exception
 *  {
 *    try
 *    {
 *      // Dummy frame to create an image
 *      Frame frame = new Frame ();
 *      // addNotify () is required to create an image (this will call Toolkit.getDefaultToolkit ())
 *      frame.addNotify ();
 *      return frame.createImage (width, height);
 *    }
 *    catch (AWTError e)
 *    {
 *      // Exception thrown because Toolkit class set in "awt.toolkit" couldn't be loaded or instantiate
 *      return createPJAImage (width, height, "Image can't be created : no AWT Toolkit available");
 *    }
 *    catch (IllegalArgumentException e)
 *    {
 *      // Exception thrown because Class.forName () method in getDefaultToolkit () was called with ""
 *      return createPJAImage (width, height, "Image can't be created : no AWT Toolkit available");
 *    }
 *    catch (SecurityException e)
 *    {
 *      // Exception thrown because getDefaultToolkit () didn't grant you access to AWT Toolkit
 *      return createPJAImage (width, height, "Image can't be created : access to AWT Toolkit refused");
 *    }
 *  }
 *
 *  public Image createPJAImage (int width, int height, String message) throws Exception
 *  {
 *    try
 *    {
 *      // Create an image with PJAToolkit directly (this one will be transparent)
 *      return new com.PJAImage (width, height);
 *    }
 *    catch (LinkageError er)
 *    {
 *      // If you don't want to use this servlet with PJA library you'll get this error
 *      throw new Exception (this, message);
 *    }
 *  }
 *
 * </PRE></BLOCKQUOTE>
 * <P>You can retrieve a <code>Graphics</code> class instance calling <code>getGraphics ()</code>
 * on the <code>image</code> instance, and use it to draw whatever you want.
 * When <code>Toolkit.getDefaultToolkit ()</code> fails, <code>java.awt.Font</code> can't be instantiated
 * because this class requires an instance of <code>java.awt.Toolkit</code> in Java 1.1...<br>
 * You can although use <code>Graphics</code> methods to draw in the image, except
 * the methods <code>setFont (Font font)</code>, <code>getFontMetrics (Font font)</code>
 * which needs a <code>java.awt.Font</code> instance (<code>getFont ()</code> will return <code>null</code>).
 * Drawing text will be done with the default font name returned by {@link PJAGraphicsManager#getDefaultFont()},
 * plain, size 10 if available in that style and size.
 * </LI>
 * </UL>
 * <P>With JDK 1.1 or if Java2D can't work, fonts are loaded from files with the ".pjaf" 
 * (Pure Java AWT Font) extension, either in the current directory, or in the directory kept in the System 
 * property "java.awt.fonts".
 * For example, if you want to set this directory to be your servlet directory, you may use :
 * <BLOCKQUOTE><PRE>
 *  prop.put ("java.awt.fonts", getServletContext ().getRealPath ("/servlet"));
 * </PRE></BLOCKQUOTE>
 * <P>These optional fonts must be captured on a computer on which JDK AWT can work
 * (a PC, Mac or UNIX/X11 machine with an available DISPLAY), with the PJA Font capture utility
 * (<code>main ()</code> method of the <code>com.PJAFontPeer</code> class).<BR>
 * If no fonts are available, all <code>Graphics</code> drawing methods
 * that don't use <code>Font</code> will work. Depending on the available fonts, default font
 * name is set to the following one, in that priority order :
 * <OL><LI>SansSerif
 * <LI>Helvetica
 * <LI>First font in the file system order.</OL>
 * <P>PJAToolkit allows to get a <code>Graphics</code> instance for an image initialized with a producer,
 * contrary to Java default Toolkit behavior which throws an <code>IllegalAccessError</code> exception in that case.
 * This allows to create transparent images from scratch and sending transparent GIF from servlets.
 * To create an intially transparent image execute
 * <code>Toolkit.getDefaultToolkit ().createImage (new java.awt.image.MemoryImageSource (width, height, new int [width * height], 0, width));</code>
 * <P>See the source of the <code>main ()</code> method of the class <code>ToolkitDemo</code> to have a test example
 * of all the Java 1.1 <code>Graphics</code> methods. You may also try
 * <code>com.TeksSurveyPie</code> servlet class.
 * <P>From PJA version 1.1, the .pjaf font files loading methods and some other methods
 * were moved to the class <code>PJAGraphicsManager</code>, to be able to use PJA even if no
 * Toolkit instance is available.
 * <P>PJAToolkit and depending files are Java 1.0 compliant but needs Java 1.2 library or higher to compile
 * (for Java 1.1 compilers, this can be done using any Java 2 rt.jar library instead of classes.zip
 * in classpath at compile time).
 *
 * @version   2.2
 * @author    Emmanuel Puybaret
 * @see       ToolkitDemo
 * @see       com.eteks.awt.PJAFontPeer
 * @see       com.eteks.awt.PJAGraphics	
 * @see       com.eteks.awt.PJAGraphicsManager
 * @see       com.eteks.awt.servlet.PJAServlet
 * @since     PJA1.0
 */
public class PJAToolkit extends Toolkit
{
  // com.PJAToolkit doesn't need to extend sun.awt.SunToolkit because :
  //  - java.awt.Component and java.awt.MenuComponent classes 
  //    uses SunToolkit insertTargetMapping () class method which is static.
  //  - java.awt.TextComponent uses SunToolkit enableInputMethodsForTextComponent ()
  //    instance method and java.awt.font.TextLine uses SunToolkit 
  //    getInputMethodHighlightMapping () instance method but they 
  //    catch Exception to ensure that cast to SunToolkit is possible.
 
  private static PJAGraphicsManager manager = PJAGraphicsManager.getDefaultGraphicsManager ();

  // v2.2 Added dummy event queue support
  private EventQueue eventQueue = null;

  public PJAToolkit ()
  {
  }

  @Override
  protected DesktopPeer createDesktopPeer(Desktop target) throws HeadlessException {
    return null;
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public ButtonPeer createButton (Button target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public TextFieldPeer createTextField(TextField target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public LabelPeer createLabel(Label target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public ListPeer createList(List target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public CheckboxPeer createCheckbox(Checkbox target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public ScrollbarPeer createScrollbar(Scrollbar target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public ScrollPanePeer createScrollPane(ScrollPane target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public TextAreaPeer  createTextArea(TextArea target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public ChoicePeer createChoice(Choice target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public FramePeer  createFrame(Frame target)
  {
    return new PJAFramePeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public CanvasPeer createCanvas(Canvas target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public PanelPeer createPanel(Panel target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public WindowPeer  createWindow(Window target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public DialogPeer  createDialog(Dialog target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public MenuBarPeer  createMenuBar(MenuBar target)
  {
    return new PJAMenuComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public MenuPeer  createMenu(Menu target)
  {
    return new PJAMenuComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public PopupMenuPeer createPopupMenu(PopupMenu target)
  {
    return new PJAMenuComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public MenuItemPeer createMenuItem(MenuItem target)
  {
    return new PJAMenuComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public FileDialogPeer createFileDialog(FileDialog target)
  {
    return new PJAComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target)
  {
    return new PJAMenuComponentPeer (target);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public LightweightPeer createComponent(Component target)
  {
    return new PJALightweightPeer (target);
  }

  /**
   * Returns a <code>FontPeer</code> instance matching to font <code>name</code>
   * with <code>style</code>. If font directory changed, any font available in the
   * new directory will be loaded first. This enables to share a JVM with different
   * users and different font directories.
   * @return <code>null</code> if the font doesn't exist.
   * @param name   The font name.
   * @param style  The font style (<code>Font.PLAIN</code>, <code>Font.ITALIC</code>,
   *               <code>Font.BOLD</code> or <code>Font.BOLD | Font.ITALIC</code>)
   * @see   #loadFonts
   */
  public FontPeer getFontPeer (String name, int style)
  {
    return manager.getFontPeer (name, style);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Does nothing.
   * @see Toolkit
   */
  public void loadSystemColors (int [] systemColors)
  {
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Dimension getScreenSize ()
  {
    return new Dimension (manager.getScreenWidth (), manager.getScreenHeight ());
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public int getScreenResolution ()
  {
    return manager.getScreenResolution ();
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public ColorModel getColorModel ()
  {
    return manager.getColorModel ();
  }

  /**
   * Returns a font directory.
   * @deprecated As of PJA version 1.1, replaced by PJAGraphicsManager.getFontsDirectory ().
   */
  public static String getFontsDirectory ()
  {
    return manager.getFontsDirectory ();
  }

  /**
   * Loads all font files (with extension .pjaf) from the directory returned by
   * <code>getFontsDirectory ()</code>.
   * @deprecated As of PJA version 1.1, replaced by PJAGraphicsManager.loadFonts ().
   */
  public static void loadFonts ()
  {
    loadFonts (getFontsDirectory ());
  }

  /**
   * Loads all font files (with extension .pjaf) in the <code>dir</code> directory. May be
   * called more than once.
   * @param dir Directory where the font files are seeked.
   * @deprecated As of PJA version 1.1, replaced by PJAGraphicsManager.loadFonts (String).
   */
  public static void loadFonts (String dir)
  {
    manager.loadFonts (dir);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public String [] getFontList ()
  {
    return manager.getFontList ();
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public FontMetrics getFontMetrics (Font font)
  {
    return manager.getFontMetrics (font);
  }

  /**
   * Returns the default font name. You may override this method
   * to have a different font.
   * @deprecated As of PJA version 1.1, replaced by PJAGraphicsManager.getDefaultFont ().
   */
  public static String getDefaultFont ()
  {
    return manager.getDefaultFont ();
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Does nothing.
   * @see Toolkit
   */
  public void sync ()
  {
    // Sould maybe wait all images are completely ready ???
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Image getImage (String filename)
  {
    return manager.getImage (filename);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Image getImage (URL url)
  {
    return manager.getImage (url);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Image createImage (String filename)
  {
    return manager.createImage (filename);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Image createImage (URL url)
  {
    return manager.createImage (url);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public boolean prepareImage (Image image, int width, int height,
                              ImageObserver observer)
  {
    return manager.prepareImage (image, width, height, observer);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public int checkImage (Image image, int width, int height,
                         ImageObserver observer)
  {
    return manager.checkImage (image, width, height, observer);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Image createImage (ImageProducer producer)
  {
    return manager.createImage (producer);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * @see Toolkit
   */
  public Image createImage (byte[] imagedata,
                            int imageoffset,
                            int imagelength)
  {
    return manager.createImage (imagedata, imageoffset, imagelength);
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Does nothing.
   * @see Toolkit
   */
  public void beep()
  {
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Returns <code>null</code>.
   * @see Toolkit
   */
  public PrintJob getPrintJob (Frame frame, String jobtitle, Properties props)
  {
    return null;
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Returns <code>null</code>.
   * @see Toolkit
   */
  public Clipboard getSystemClipboard()
  {
    return null;
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * As of PJA version 2.2, returns a dummy EventQueue.
   * @see Toolkit
   */
  public EventQueue getSystemEventQueueImpl()
  {
    // v2.2 Added dummy event queue support
    if (eventQueue == null) 
      eventQueue = new PJAEventQueue();

    return eventQueue;
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Returns <code>null</code>.
   * @see Toolkit
   */
  public DragSourceContextPeer createDragSourceContextPeer (DragGestureEvent dge)
  {
    return null;
  }

  @Override
  public boolean isModalityTypeSupported(Dialog.ModalityType modalityType) {
    return false;
  }

  @Override
  public boolean isModalExclusionTypeSupported(Dialog.ModalExclusionType modalExclusionType) {
    return false;
  }

  /**
   * <code>java.awt.Toolkit</code> implementation.
   * Returns <code>null</code>.
   * @see Toolkit
   */
  // Method with Java 2 classes
  public Map mapInputMethodHighlight (InputMethodHighlight highlight)
  {
    return null;
  }

  /**
   * @deprecated As of PJA version 1.1, replaced by PJANativeToolkitComparison.main (args).
   */
  public static void main (String args [])
  {
    System.out.println ("This method is deprecated. Run PJANativeToolkitComparison class");
  }

  // v2.2 Added dummy event queue support
  private static class PJAEventQueue extends EventQueue 
  {
    public void postEvent (AWTEvent theEvent) 
    {
      // This method must be overriden to avoid the cast 
      // to (SunToolkit) in super class
    }
  }
}

