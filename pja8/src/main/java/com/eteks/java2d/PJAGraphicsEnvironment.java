/*
 * @(#)PJAGraphicsEnvironment.java  06/14/2000
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

import sun.java2d.SunGraphicsEnvironment;

import java.awt.*;
import java.awt.image.BufferedImage;

// The class it extends

/**
 * Pure Java AWT GraphicsEnvironment implementation. This implementation enables to get
 * an environment even when no Display is available.<br>
 * <code>java.awt.graphicsenv</code> system property have to be set 
 * to <code>com.PJAGraphicsEnvironment</code> to allow the change
 * of <code>java.awt.GraphicsEnvironment</code> default implementation.
 * <code>java.awt.fonts</code> system property must be set to the 
 * path where True Type fonts files will be loaded from 
 * (this property must be defined to avoid calling the <code>getFontPath ()</code>
 * native method of the class <code>NativeFontWrapper</code> which 
 * needs a X11 Display in Motif implementation).<br>
 * This path can be equal to :
 * <ul><li><code>/usr/openwin/lib/X11/fonts/Type1:/usr/openwin/lib/X11/fonts/TrueType</code> 
 *         directories on Solaris</li>
 *     <li><code>WinDir\Font</code> directory on Windows</li>
 *     <li><code>($JAVAHOME)/lib/fonts</code></li> 
 *     <li>Any directory list containing True Type fonts</li></ul>
 * <p>Java2D can work with PJA only with JVM version >= 1.2.
 * When <code>GraphicsEnvironment.getLocalGraphicsEnvironment ()</code> fails because
 * <code>PJAGraphicsEnvironment</code> class can't be loaded or <code>java.awt.graphicsenv</code> system 
 * property can't be changed, <code>java.awt.Font</code> can't be instantiated without a Display since 
 * this class requires an instance of <code>java.awt.GraphicsEnvironment</code> in Java 1.2. In that case,
 * it forbids to use <code>Graphics2D</code> methods in a program because <code>Graphics2D</code> needs a default
 * font (see PJADemo.java source for a way to change of .pjaf font).
 *
 * @version   2.5
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAToolkit
 * @see       com.eteks.awt.PJAGraphicsManager
 * @since     PJA2.0
 */
public class PJAGraphicsEnvironment extends SunGraphicsEnvironment
{
  public PJAGraphicsEnvironment ()
  {
  }

  /**
   * Returns the number of screens.
   * @return 1
   */
  protected int getNumScreens ()
  {
    return 1;
  }
  
  protected GraphicsDevice makeScreenDevice (int screenNum)
  {
    return new PJAGraphicsDevice (screenNum);
  }
  
  /**
   * Returns a <code>Graphics2D</code> instance or throws an <code>AWTError</code>
   * exception.
   */
  public Graphics2D createGraphics (BufferedImage image) 
  {
    try
    {
      return super.createGraphics (image);
      // This may fail because Java2D Graphics2D default implementation needs a Font 
      // default instance that requires GraphicsEnvironment getLocalGraphicsEnvironment () 
      // method to work.
      // The solution could be to create a class PJAGraphics2D extending Graphics2D
      // that wouldn't require a default Font (as PJAGraphics can do). But if it impossible
      // to change of font, PJAGraphics2D wouldn't be so interesting and PJAGraphicsExtension
      // implemented by PJAGraphics could be enough...
    }
    catch (IllegalArgumentException e)
    { }
    catch (SecurityException error)
    { }  // GraphicsEnvironment requires awt library may be forbidden to load
    catch (LinkageError error)
    { }  // Thrown by static initializer which requires awt library
    catch (InternalError error)
    { }  // Thrown by static initializers of GraphicsEnvironment which requires a Display
    
    throw new AWTError ("Graphics2D can't be instantiated");
  }

  @Override
  public boolean isDisplayLocal() {
    return false;
  }

  /**
   * Implementation of the abstract method of SunGraphicsEnvironment. This method appeared
   * in JVM version 1.4.1.
   * @since     PJA2.5
   */
//  protected sun.awt.FontProperties createFontProperties()
//  {
//    return new sun.awt.FontConfiguration.FontProperties ()
//      {
//        public String getFallbackFamilyName(String fontName, String defaultFallback)
//        {
//          // Uses same implementation as sun.awt.motif.MFontProperties and sun.awt.windows.WFontProperties
//          String compatibilityName = getCompatibilityFamilyName(fontName);
//          if (compatibilityName != null)
//          {
//            return compatibilityName;
//          }
//          return defaultFallback;
//        }
//      };
//  }
}
