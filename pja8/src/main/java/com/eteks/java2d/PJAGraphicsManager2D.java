/*
 * @(#)PJAGraphicsManager2D.java   05/30/2000
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
package com.eteks.java2d;

import com.eteks.awt.PJAGraphicsManager;
import com.eteks.awt.PJAImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.image.PixelGrabber;
import java.awt.peer.FontPeer;

// Java 2 classes

/**
 * Pure Java AWT Manager for Java2D. This class manages fonts and images with Java 2D <code>GraphicsEnvironment</code>.
 * Fonts come from True Type font files and images are build with <code>PJABufferedImage</code>.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAToolkit	
 * @since     PJA2.0
 */
public class PJAGraphicsManager2D extends PJAGraphicsManager
{
  private static Graphics              java2DGraphicsInstance;
  private static GraphicsConfiguration java2DGraphicsConfiguration;

  /**
   * Returns a dummy <code>FontPeer</code> object.
   * @param name   The font name.
   * @param style  The font style (<code>Font.PLAIN</code>, <code>Font.ITALIC</code>,
   *               <code>Font.BOLD</code> or <code>Font.BOLD | Font.ITALIC</code>)
   */
  public FontPeer getFontPeer (String name, int style)
  {
    // Only True Type Fonts are used
    return new FontPeer () { };
  }
  
  /**
   * Returns the array <code>{"Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput"}</code>.
   * The list of True Type fonts is obtained from 
   * GraphicsEnvironment getAvailableFontFamilyNames() method.
   */
  public String [] getFontList ()
  {
    // From sun.awt.SunToolkit
    return new String [] {"Dialog", "SansSerif", "Serif", "Monospaced", "DialogInput"};
  }

  /**
   * Returns the font metrics of a font.
   */
  public FontMetrics getFontMetrics (Font font)
  {
    // With Java2D, only True Type Fonts are used
    // Get a dummy Graphics2D instance to be able to get True Type FontMetrics
    if (java2DGraphicsInstance == null)
      java2DGraphicsInstance = new PJABufferedImage (1, 1, PJABufferedImage.TYPE_INT_ARGB).getGraphics ();
    return java2DGraphicsInstance.getFontMetrics (font);
  }

  /**
   * Starts the loading of an image.
   * @see com.eteks.awt.PJAToolkit#prepareImage
   */
  public boolean prepareImage (Image image, int width, int height,
                              ImageObserver observer)
  {
    if (image instanceof BufferedImage)
    {
      int status =   ImageObserver.ALLBITS 
                   | ImageObserver.WIDTH           
                   | ImageObserver.HEIGHT 
                   | ImageObserver.PROPERTIES;
      // Tell the observer image is ready
      // See createImage(ImageProducer)
      if (observer != null)
        observer.imageUpdate (image, status, 
                              0, 0, ((BufferedImage)image).getWidth (), ((BufferedImage)image).getHeight ());
      return true;
    }
    else
      return super.prepareImage (image, width, height, observer);
  }

  /**
   * Checks the status of an image laoding.
   * @see com.eteks.awt.PJAToolkit#checkImage
   */
  public int checkImage (Image image, int width, int height,
                         ImageObserver observer)
  {
    if (image instanceof BufferedImage)
    {
      int status =   ImageObserver.ALLBITS 
                   | ImageObserver.WIDTH           
                   | ImageObserver.HEIGHT 
                   | ImageObserver.PROPERTIES;
      // Tell the observer image is ready
      // See createImage(ImageProducer)
      if (observer != null)
        observer.imageUpdate (image, status,
                              0, 0, ((BufferedImage)image).getWidth (), ((BufferedImage)image).getHeight ());
      return status;
    }
    else
      // v2.1 : added super. (this was an endless recursive call)
      return super.checkImage (image, width, height, observer);
  }

  /**
   * Creates an image from the <code>producer</code>. This is the method that
   * finally creates an instance of <code>PJABufferedImage</code>.
   * @return An instance of the class <code>java.awt.Image</code>.
   */
  public Image createImage (ImageProducer producer)
  {
    // Obliged to compute a BufferedImage instance with Java2D because
    // java.awt.image.BufferedImage and sun.awt.image.Image are the only classes 
    // the method drawImage () of the class BufferedImageGraphics2D can use !
    // This obliges to load the image first
    PJAImage image = new PJAImage (producer);
    image.sync ();
    int width  = image.getWidth (null);
    int height = image.getHeight (null);
    if (image.getWidth (null) >= 0)
      try
      {
        int [] pixels = new int [width * height];
        PixelGrabber grabber = new PixelGrabber (image, 0, 0, width, height, pixels, 0, width);
        grabber.grabPixels ();
        BufferedImage imageJava2D = new PJABufferedImage (width, height, PJABufferedImage.TYPE_INT_ARGB);
        // Copy the pixels in BufferedImage instance
        imageJava2D.setRGB (0, 0, width, height, pixels, 0, width);
        return imageJava2D;
      }
      catch (InterruptedException e)
      { }
    
    return null;
  }

  /**
   * Creates an image of <code>width x height</code> pixels. This method returns
   * an instance of <code>com.eteks.awt.PJABufferedImage</code>.
   * @return An instance of the class <code>java.awt.Image</code>.
   * @param width   Width in pixels of the new image.
   * @param height  Height in pixels of the new image.
   * @see com.eteks.java2d.PJAGraphicsEnvironment
   */
  public Image createImage (int width, int height)
  {
    return new PJABufferedImage (width, height, PJABufferedImage.TYPE_INT_ARGB);
  }
  
  /**
   * Returns a <code>GraphicsConfiguration</code> instance required by the the method 
   * <code>getGraphicsConfiguration ()</code> of <code>ComponentPeer</code> interface.
   * @since PJA2.0
   */
  public GraphicsConfiguration getGraphicsConfiguration ()
  {
    if (java2DGraphicsConfiguration == null)
      java2DGraphicsConfiguration = new PJAGraphicsEnvironment ().getDefaultScreenDevice ().getDefaultConfiguration ();
    return java2DGraphicsConfiguration;
  }
}
