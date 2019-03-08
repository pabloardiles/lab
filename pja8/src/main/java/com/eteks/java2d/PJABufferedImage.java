/*
 * @(#)PJABufferedImage.java  06/14/2000
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

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

/**
 * Pure Java AWT BufferedImage implementation.
 * <p>When drawing in images of class <code>java.awt.image.BufferedImage</code>, 
 * <i>.pjaf</i> font files are not used (other fonts already exist).<br>
 * <tt>java.awt.image.BufferedImage</tt> and thus <tt>PJABufferedImage</tt> classes 
 * require awt library loading.
 * Java2D can work with PJA only with JVM version >= 1.2.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       com.eteks.java2d.PJAGraphicsEnvironment
 * @since     PJA2.0
 */
public class PJABufferedImage extends BufferedImage
{
  private Hashtable properties;
  
  /**
   * Same constructor as <code>BufferedImage</code>.
   */
  public PJABufferedImage (int width, 
                           int height, 
                           int imageType) 
  {
    super (width, height, imageType);
  }

  /**
   * Same constructor as <code>BufferedImage</code>.
   */
  public PJABufferedImage (int width, 
                           int height,
                           int imageType,
                           IndexColorModel cm) 
  {
    super (width, height, imageType, cm);
  }

  /**
   * Same constructor as <code>BufferedImage</code>.
   */
  public PJABufferedImage (ColorModel cm,
                           WritableRaster raster,
                           boolean isRasterPremultiplied,
                           Hashtable properties) 
  {
    super (cm, raster, isRasterPremultiplied, properties);
    this.properties = properties;
  }
   
  /**
   * Returns an instance of <code>Graphics2D</code> to draw in the image.
   * <code>createGraphics ();</code> returns the <code>Graphics</code> instance returned 
   * by the method <code>createGraphics ()</code> of <code>PJAGraphicsEnvironment</code> class, 
   * if <code>super.createGraphics ()</code> failed because for exemple 
   * it was impossible to change <code>java.awt.graphicsenv</code> system property.
   */
  public Graphics2D createGraphics ()
  {
    try
    {
      return super.createGraphics ();
      // The previous instruction may throw exceptions because
      // BufferedImage createGraphics () method uses SunGraphicsEnvironment class
      // which needs to load a default GraphicsEnvironment class.
      // This class is obtained from java.awt.graphicsenv system property and
      // instantiated by GraphicsEnvironment getLocalGraphicsEnvironment () call.
      // If the user is unable to change it to com.PJAGraphicsEnvironment
      // it will still use the X11 default graphics environment class which requires
      // a display, that may be impossible to get.
    }
    catch (IllegalArgumentException e)
    { }
    catch (SecurityException error)
    { }  // GraphicsEnvironment requires awt library may be forbidden to load
    catch (LinkageError error)
    { }  // Thrown by static initializer which requires awt library
    catch (InternalError error)
    { }  // Thrown by static initializers of GraphicsEnvironment which requires a Display
    
    return new PJAGraphicsEnvironment ().createGraphics (this);
  }
  
  /**
   * Overriden to return an instance of <code>PJABufferedImage</code>.
   */
  public BufferedImage getSubimage (int x, int y, int w, int h) 
  {
    // Same implementation as BufferedImage getSubimage () method
    // but with PJABufferedImage class.
    return new PJABufferedImage (getColorModel (),
                                 getRaster ().createWritableChild (x, y, w, h,
                                                                   0, 0, null),
                                 getColorModel ().isAlphaPremultiplied(),
                                 properties);
  }
}