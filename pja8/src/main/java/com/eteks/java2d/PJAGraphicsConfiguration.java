/*
 * @(#)PJAGraphicsConfiguration.java  06/14/2000
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

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.WritableRaster;

/**
 * Pure Java AWT GraphicsConfiguration implementation.
 * Java2D can work with PJA only with JVM version >= 1.2.
 * Implementation of these methods is inspired from 
 * <code>sun.awt.X11GraphicsConfig</code> class.
 *
 * @version   2.4
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAToolkit
 * @since     PJA2.0
 */
public class PJAGraphicsConfiguration extends GraphicsConfiguration
{
  private GraphicsDevice device;
  
  public PJAGraphicsConfiguration (GraphicsDevice device)
  {
    this.device = device;
  }

  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public GraphicsDevice getDevice ()
  {
    return device;
  }

  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public BufferedImage createCompatibleImage (int width, int height)
  {
    ColorModel     model = getColorModel();
    WritableRaster raster = model.createCompatibleWritableRaster(width, height);
    return new PJABufferedImage (model, raster, model.isAlphaPremultiplied(),
                                 null);
  }

  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public BufferedImage createCompatibleImage (int width, int height, int transparency)
  {
    switch (transparency) 
    {
      case Transparency.OPAQUE :
        return createCompatibleImage (width, height);            
      case Transparency.BITMASK :
      case Transparency.TRANSLUCENT :
        ColorModel cm = getColorModel (transparency);
        WritableRaster wr = cm.createCompatibleWritableRaster (width, height);
        return new PJABufferedImage (cm, wr, cm.isAlphaPremultiplied(), null);            
      default :
        throw new IllegalArgumentException ("Unknown transparency type "+
                                            transparency);
    }
  }
  
  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public ColorModel getColorModel ()
  {
    return PJAGraphicsManager.getDefaultGraphicsManager ().getColorModel ();
  }

  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public ColorModel getColorModel (int transparency)
  {
    if (transparency == Transparency.OPAQUE)
      return getColorModel ();
    else if (transparency == Transparency.BITMASK)
      return new DirectColorModel (25, 0xff0000, 0xff00, 0xff, 0x1000000);
    else if (transparency == Transparency.TRANSLUCENT)
      return ColorModel.getRGBdefault();
    else
      throw new IllegalArgumentException ("Unknown transparency type " + transparency);
  }

  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public AffineTransform getDefaultTransform ()
  {
    return new AffineTransform ();
  }

  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public AffineTransform getNormalizingTransform ()
  {
    int screenResolution = PJAGraphicsManager.getDefaultGraphicsManager ().getScreenResolution ();
    double xscale = screenResolution / 72.0;
    double yscale = screenResolution / 72.0;
    return new AffineTransform (xscale, 0.0, 0.0, yscale, 0.0, 0.0);
  }
  
  /**
   * <code>java.awt.GraphicsConfiguration</code> implementation.
   * @see GraphicsConfiguration
   */
  public Rectangle getBounds()
  {
    PJAGraphicsManager graphicsManager = PJAGraphicsManager.getDefaultGraphicsManager ();
    return new Rectangle (0, 0, 
                          graphicsManager.getScreenWidth (), 
                          graphicsManager.getScreenHeight ());
  }

  // v2.4 : Added createVolatileImage () for JDK 1.4 support
  public java.awt.image.VolatileImage createCompatibleVolatileImage (int width, int height)
  {
    return null;
  }
}
  
