/*
 * @(#)PJAGraphicsDevice.java  06/14/2000
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

/**
 * Pure Java AWT GraphicsDevice implementation.
 * Java2D can work with PJA only with JVM version >= 1.2.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAToolkit
 * @since     PJA2.0
 */
public class PJAGraphicsDevice extends GraphicsDevice
{
  public PJAGraphicsDevice (int screenNum)
  {
  }

  /**
   * <code>java.awt.GraphicsDevice</code> implementation.
   * @return <code>TYPE_RASTER_SCREEN</code>. <code>TYPE_IMAGE_BUFFER</code> would be more logical 
   *         but it prevents to create dummy Window (and Frame) objects.
   * @see GraphicsDevice
   */
  public int getType ()
  {
    return TYPE_RASTER_SCREEN;
  }

  /**
   * <code>java.awt.GraphicsDevice</code> implementation.
   * @return <code>&quot;:0.0&quot;</code>
   * @see GraphicsDevice
   */
  public String getIDstring()
  {
    return ":0.0";
  }

  /**
   * <code>java.awt.GraphicsDevice</code> implementation.
   * @see GraphicsDevice
   */
  public GraphicsConfiguration [] getConfigurations()
  {
    return new GraphicsConfiguration [] {new PJAGraphicsConfiguration (this)};
  }

  /**
   * <code>java.awt.GraphicsDevice</code> implementation.
   * @see GraphicsDevice
   */
  public GraphicsConfiguration getDefaultConfiguration ()
  {
    return getConfigurations () [0];
  }
}
