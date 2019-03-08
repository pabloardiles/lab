/*
 * @(#)PJAGraphics.java  06/08/2000
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
import java.awt.image.*;
import java.awt.peer.FontPeer;
import java.text.AttributedCharacterIterator;
import java.util.Hashtable;

// Java 2 classes

/**
 * Pure Java AWT Graphics class implementation. This class enables to draw
 * in an off screen image, on a system that has no display available.
 * All drawings are done in the buffer of an image of class <code>PJAImage</code>.
 * All the methods are the implementation of the abstract ones of <code>java.awt.Graphics</code>
 * class. See <code>java.awt.Graphics</code> for more information.
 * This class uses only <code>int</code> and <code>long</code> number type (no <code>float</code>
 * or <code>double</code>).
 * This class can work even if the classes <code>Rectangle</code>, <code>Color</code>, <code>Font</code>
 * and <code>FontMetrics</code> aren't accessible for security reasons.
 *
 * @version   2.3.1
 * @author    Emmanuel Puybaret
 * @see       PJAImage
 * @see       Graphics
 * @see       com.eteks.awt.PJAGraphicsManager
 * @since     PJA1.0
 */
public class PJAGraphics extends Graphics implements PJAGraphicsExtension
{
  private PJAGraphicsManager manager;
  private PJAFont            defaultFont;
  private boolean            classColorAccessible;
  private boolean            classRectangleAccessible;
  private boolean            classFontMetricsAccessible;
  private boolean            fontInstantiationEnabled;

  PJAImage       image;
  Color          color;
  int            ARGB;
  int            ARGBIndex;
  Color          XORColor;
  boolean        XORMode;
  int            XorRGB;
  PJAFontPeer    fontPeer;
  PJAFont        font;
  String         fontName;  // User required font name (if not available, may be different of the fontPeer name)
  int            fontStyle; // User required font style (if not available, may be different of the fontPeer style)
  int            fontSize;  // User required font size (if not available, may be different of the fontPeer size)
  int            translateX;
  int            translateY;
  PJARectangle   userClip;
  PJARectangle   clip;

  // v2.2 : Added PJAGraphics default constructor to initialize an instance correctly
  private PJAGraphics ()
  {
    manager = PJAGraphicsManager.getDefaultGraphicsManager ();

    // Test if the class java.awt.Color can be loaded
    classColorAccessible = manager.isClassAccessible ("java.awt.Color");

    // Test if the class java.awt.Rectangle can be loaded
    classRectangleAccessible = manager.isClassAccessible ("java.awt.Rectangle");

    // Test if the class java.awt.FontMetrics can be loaded
    classFontMetricsAccessible = manager.isClassAccessible ("java.awt.FontMetrics");

    String defaultFontName  = manager.getDefaultFont ();
    int    defaultFontStyle = Font.PLAIN;
    int    defaultFontSize  = 10;
    if (defaultFontName != null)
    {
      fontPeer = (PJAFontPeer)manager.getFontPeer (defaultFontName, Font.PLAIN, true);

      // Test if font can be created
      fontInstantiationEnabled = manager.isFontInstantiable ();
      if (fontInstantiationEnabled)
        defaultFont = new PJAFont (defaultFontName, defaultFontStyle, defaultFontSize);
    }

    image      = null;
    color      = classColorAccessible ? Color.black : null; // Color class needs Toolkit class
    ARGB       = 0xFF000000;
    ARGBIndex  = -1;
    XORColor   = null;
    XORMode    = false;
    XorRGB     = 0;
    font       = defaultFont;
    fontName   = defaultFontName;
    fontStyle  = defaultFontStyle; 
    fontSize   = defaultFontSize;
    translateX = 0;
    translateY = 0;
    userClip   = null;
    clip       = null;
  }
  
  protected PJAGraphics (PJAImage image)
  {
    // Init with default values
    this ();
    this.image = image;
    this.userClip = new PJARectangle (0, 0, image.getWidth (), image.getHeight ());
    this.clip     = new PJARectangle (0, 0, image.getWidth (), image.getHeight ());
  }

  private PJAGraphics (PJAImage       image,
                       Color          color,
                       int            ARGB,
                       int            ARGBIndex,
                       Color          XORColor,
                       boolean        XORMode,
                       int            XorRGB,
                       PJAFontPeer    fontPeer,
                       PJAFont        font,
                       String         fontName,
                       int            fontStyle,
                       int            fontSize,
                       int            translateX,
                       int            translateY,
                       PJARectangle   userClip,
                       PJARectangle   clip)
  {
    // v2.2 : PJAGraphics not initialized correctly with this constructor
    this ();
    this.image = image;
    this.color = color;
    this.ARGB        = ARGB;
    this.ARGBIndex   = ARGBIndex;
    this.XORColor    = XORColor;
    this.XORMode     = XORMode;
    this.XorRGB      = XorRGB;
    this.fontPeer    = fontPeer;
    this.font        = font;
    this.fontName    = fontName;
    this.fontStyle   = fontStyle;
    this.fontSize    = fontSize;
    this.translateX  = translateX;
    this.translateY  = translateY;
    this.userClip    = userClip;
    this.clip        = clip;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public Graphics create ()
  {
    // v2.2 : added copy of objects
    return new PJAGraphics (image,
                            color, ARGB, ARGBIndex, XORColor, XORMode, XorRGB,
                            fontPeer, font, fontName, fontStyle, fontSize,
                            translateX, translateY,
                            new PJARectangle (userClip), 
                            new PJARectangle (clip));
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void translate (int x, int y)
  {
    // v2.2 : translation are supposed to be cumulative 
    translateX += x;
    translateY += y;

    userClip.x -= x;
    userClip.y -= y;
    // v2.2 : don't change real clip coordinates. Translation has no effect on them.
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public Color getColor ()
  {
    return color;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void setColor (Color color)
  {
    this.color = color;
    
    int newARGB = color.getRGB () | 0xFF000000;
    // v2.3 : added support for 8 bit color images
    // if colorModel != null, IndexColorModel class is accessible
    if (   image.getColorModel () != null
        && image.getColorModel () instanceof IndexColorModel
        && (   ARGB != newARGB
            || ARGBIndex == -1))
      ARGBIndex = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newARGB) & 0xFF;
      
    ARGB = newARGB;
  }

  /**
   * Returns the graphics current color RGB.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the color RGB.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getColorRGB ()
  {
    return ARGB & 0x00FFFFFF;
  }

  /**
   * Sets this graphics context's color.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @param  red   the red component.
   * @param  green the green component.
   * @param  blue  the blue component.
   * @see    com.eteks.awt.PJAGraphicsExtension
   * @since  PJA1.1
   */
  public void setColor (int red, int green, int blue)
  {
    if (classColorAccessible)
      setColor (new Color (red, green, blue));
    else
      ARGB = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF) | 0xFF000000;
  }

  private static final int COLOR_FACTOR_100 = 143;  // = 100 / java.awt.Color.FACTOR

  private int getBrighterColor (int ARGB)
  {
    int red   = (ARGB & 0xFF0000) >>> 16;
    int green = (ARGB & 0xFF00) >>> 8;
    int blue  = ARGB & 0xFF;
    // Compute a brighter color using the same algorithm as Color.brighter () method
    // but without the use of double type and Color class
    red   = Math.min (roundDiv (red * COLOR_FACTOR_100, 100), 255);
    green = Math.min (roundDiv (green * COLOR_FACTOR_100, 100), 255);
    blue  = Math.min (roundDiv (blue * COLOR_FACTOR_100, 100), 255);

    return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF) | 0xFF000000;
  }

  private int getDarkerColor (int ARGB)
  {
    int red   = (ARGB & 0xFF0000) >>> 16;
    int green = (ARGB & 0xFF00) >>> 8;
    int blue  = ARGB & 0xFF;
    // Compute a brighter color using the same algorithm as Color.brighter () method
    // but without the use of double type and Color class
    red   = Math.min (roundDiv (red * 100, COLOR_FACTOR_100), 255);
    green = Math.min (roundDiv (green * 100, COLOR_FACTOR_100), 255);
    blue  = Math.min (roundDiv (blue * 100, COLOR_FACTOR_100), 255);

    return ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF) | 0xFF000000;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void setPaintMode ()
  {
    XORColor = null;
    XORMode  = false;
    XorRGB   = 0;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void setXORMode (Color c1)
  {
    XORColor = c1;
    XORMode  = XORColor != null;
    XorRGB   = XORMode ? XORColor.getRGB () : 0;
  }

  /**
   * Sets this graphics context's XOR color.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @param  red   the red component.
   * @param  green the green component.
   * @param  blue  the blue component.
   * @see    com.eteks.awt.PJAGraphicsExtension
   * @since  PJA1.1
   */
  public void setXORMode (int red, int green, int blue)
  {
    if (classColorAccessible)
      this.XORColor = new Color (red, green, blue);
    XORMode = true;
    XorRGB  = ((red & 0xFF) << 16) | ((green & 0xFF) << 8) | (blue & 0xFF);
  }

  private void checkFontLoading ()
  {
    if (fontPeer == null)
      throw new AWTError ("No default font : at least one .pjaf font must be loaded.");
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public FontMetrics getFontMetrics ()
  {
    checkFontLoading ();
    if (font != null)
      return fontPeer.getFontMetrics (font);
    else if (classFontMetricsAccessible)
      return fontPeer.getFontMetrics (fontSize);
    else
      return null;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public FontMetrics getFontMetrics (Font font)
  {
    return manager.getFontMetrics (getPJAFont (font), true);
  }

  /**
   * Determines the standard leading of the current font.
   * The standard leading (interline spacing) is the
   * logical amount of space to be reserved between the descent of one
   * line of text and the ascent of the next line. The height metric is
   * calculated to include this extra space.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the standard leading of the current font.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getFontLeading()
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getLeading ();
  }

  /**
   * Determines the font ascent of the current font.
   * The font ascent is the distance from the font's
   * baseline to the top of most alphanumeric characters. Some
   * characters in the font may extend above the font ascent line.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return   the font ascent of the font.
   * @see      com.eteks.awt.PJAGraphicsExtension
   * @since    PJA1.1
   */
  public int getFontAscent()
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getAscent ();
  }

  /**
   * Determines the font descent of the current font.
   * The font descent is the distance from the font's
   * baseline to the bottom of most alphanumeric characters with
   * descenders. Some characters in the font may extend below the font
   * descent line.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return   the font descent of the font.
   * @see      com.eteks.awt.PJAGraphicsExtension
   * @since    PJA1.1
   */
  public int getFontDescent ()
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getDescent ();
  }

  /**
   * Determines the maximum ascent of the current font.
   * No character extends further above the font's baseline
   * than this height.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the maximum ascent of any character in the font.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getFontMaxAscent ()
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getMaxAscent ();
  }

  /**
   * Determines the maximum descent of the current font.
   * No character extends further below the font's baseline
   * than this height.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the maximum descent of any character in the font.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getFontMaxDescent ()
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getMaxDescent ();
  }

  /**
   * Gets the maximum advance width of any character of the current Font.
   * The advance width is the amount by which the current point is
   * moved from one character to the next in a line of text.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the maximum advance width of any character
   *          in the font.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getFontMaxAdvance ()
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getMaxAdvance ();
  }

  /**
   * Returns the advance width of the specified character of the current font.
   * The advance width is the amount by which the current point is
   * moved from one character to the next in a line of text.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @param ch the character to be measured
   * @return  the advance width of the specified <code>char</code>
   *         in the font described by this font metric.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getCharWidth (char ch)
  {
    checkFontLoading ();
    return fontPeer.getFontData (fontSize).getCharWidth (ch);
  }

  /**
   * Returns the total advance width for showing the specified String
   * of the current Font.
   * The advance width is the amount by which the current point is
   * moved from one character to the next in a line of text.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @param str the String to be measured
   * @return  the advance width of the specified string
   *          in the font described by this font metric.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getStringWidth (String str)
  {
    checkFontLoading ();
    int len = str.length ();
    char data [] = new char [len];
    str.getChars (0, len, data, 0);
    return fontPeer.getFontData (fontSize).getCharsWidth (data, 0, len);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public Font getFont ()
  {
    checkFontLoading ();
    return font;
  }

  /**
   * Returns the graphics current font name. Returns <code>null</code> if no font
   * could be loaded from .pjaf font files.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the font name.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public String getFontName ()
  {
    checkFontLoading ();
    return fontName;
  }

  /**
   * Returns the graphics current font style.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the font style.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getFontStyle ()
  {
    checkFontLoading ();
    return fontStyle;
  }

  /**
   * Returns the graphics current font size.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @return  the font size.
   * @see     com.eteks.awt.PJAGraphicsExtension
   * @since   PJA1.1
   */
  public int getFontSize ()
  {
    checkFontLoading ();
    return fontSize;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void setFont (Font font)
  {
    if (font != null)
    {
      fontPeer = (PJAFontPeer)manager.getFontPeer (font.getName (), font.getStyle (), true);
      fontSize  = font.getSize ();
      fontStyle = font.getStyle ();
      fontName  = font.getName ();
    }
    this.font = getPJAFont (font);
  }

  /**
   * Sets this graphics context's font to the specified font specified by its
   * name, style and size.
   * All subsequent text operations using this graphics context
   * use this font. If unable to have a default toolkit this method enables
   * to draw text although with an other font.
   * <code>PJAGraphicsExtension</code> implementation.
   *
   * @param  fontName   the font name.
   * @param  fontStyle  the font style.
   * @param  fontSize   the font size.
   * @see    com.eteks.awt.PJAGraphicsExtension
   * @see    Graphics#setFont(Font)
   * @since  PJA1.1
   */
  public void setFont (String fontName, int fontStyle, int fontSize)
  {
    if (fontName == null)
      throw new IllegalArgumentException ("Font name can't be null, use \"\" for default");

    if (fontInstantiationEnabled)
      setFont (new Font (fontName, fontStyle, fontSize));
    else
    {
      this.fontPeer = (PJAFontPeer)manager.getFontPeer (fontName, fontStyle, true);
      this.fontName  = fontName;
      this.fontStyle = fontStyle;
      this.fontSize  = fontSize;
      this.font = null;
    }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  private PJAFont getPJAFont (final Font font)
  {
    if (   font == null
        || font instanceof PJAFont)
      return (PJAFont)font;
    else
      if (fontInstantiationEnabled)
        // Create a copy of the font with a new class to ensure that
        // the font peer will be an instance of PJAFontPeer
        return new PJAFont (font.getName (), font.getStyle (), font.getSize ());
      else
        return null;
  }

  /**
   *  Inner font class extending <code>java.awtFont</code> class to override getPeer ().
   */
  private class PJAFont extends Font
  {
    FontPeer peer;

    public PJAFont (String fontName, int fontStyle, int fontSize)
    {
      super (fontName, fontStyle, fontSize);
      peer = manager.getFontPeer (fontName, fontStyle, true);
    }

    public FontPeer getPeer ()
    {
      return peer;
    }
  }

  /**
   * Inner rectangle class avoiding the use of <code>java.awt.Rectangle</code>
   * which requires <code>java.awt.Toolkit</code> class to be loaded.
   */
  private static class PJARectangle
  {
    int x;
    int y;
    int width;
    int height;

    public PJARectangle (int x, int y, int width, int height)
    {
      this.x = x;
      this.y = y;
      this.width  = width;
      this.height = height;
    }

    // Added this copy constructor
    public PJARectangle (PJARectangle rect)
    {
      this.x = rect.x;
      this.y = rect.y;
      this.width  = rect.width;
      this.height = rect.height;
    }

    public PJARectangle intersection (PJARectangle rect)
    {
      int x1 = Math.max (x, rect.x);
      int x2 = Math.min (x + width, rect.x + rect.width);
      int y1 = Math.max (y, rect.y);
      int y2 = Math.min (y + height, rect.y + rect.height);
      return new PJARectangle (x1, y1, x2 - x1, y2 - y1);
    }

    public boolean inside (int x, int y)
    {
      return    (x >= this.x)
             && ((x - this.x) < this.width)
             && (y >= this.y)
             && ((y - this.y) < this.height);
    }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public Rectangle getClipBounds ()
  {
    // v2.2 : changed bad test 
    if (!classRectangleAccessible)
      return null;
    else
      return new Rectangle (userClip.x, userClip.y, userClip.width, userClip.height);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void clipRect (int x, int y, int width, int height)
  {
    // v2.2 : userClip instance always available
    
    // Compute intersection between rectangles (x, y, width, height) and userClip
    userClip = userClip.intersection (new PJARectangle (x, y, width, height));
    // Compute intersection between rectangles (x + translateX, y + translateY, width, height) and clip
    clip = clip.intersection (new PJARectangle (x + translateX, y + translateY, width, height));
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void setClip (int x, int y, int width, int height)
  {
    // v2.2 : init clipping rectangles and compute with clipRect
    userClip = new PJARectangle (-translateX, -translateY, image.getWidth (), image.getHeight ());
    clip     = new PJARectangle (0, 0, image.getWidth (), image.getHeight ());
    clipRect (x, y, width, height);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public Shape getClip ()
  {
    // v2.2 : new Rectangle () is useless
    // return new Rectangle (getClipBounds ());
    return getClipBounds ();
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void setClip (Shape clip)
  {
    Rectangle rect = clip.getBounds ();
    setClip (rect.x, rect.y, rect.width, rect.height);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void copyArea (int x, int y, int width, int height,
                        int dx, int dy)
  {
    x += translateX;
    y += translateY;

    int xStart = x;
    int xEnd   = x + width - 1;
    int xStep  = 1;
    if (dx < 0)
    {
      xStart = x + width - 1;
      xEnd   = x;
      xStep  = -1;
    }

    int yStart = y;
    int yEnd   = y + height - 1;
    int yStep  = 1;
    if (dy < 0)
    {
      yStart = y + height - 1;
      yEnd   = y;
      yStep  = -1;
    }

    synchronized (image)
    {
      // v2.3 : added support for 8 bit color images
      Object pixelsArray   = image.getPixelsArray ();
      // v1.1 : pixels may be null because of image flush ()
      if (pixelsArray != null)
      {
        int imageWidth  = image.getWidth ();
        // v2.3.1 : wrong method call to get the height of the image (used to be getWidth ())
        int imageHeight = image.getHeight ();
          
        if (pixelsArray instanceof int [])
        {
          int  [] intPixels = (int [])pixelsArray;
          for (x = xStart; x <= xEnd; x+= xStep)
            for (y = yStart; y <= yEnd; y+= yStep)
              if (   !isClipped (x + dx, y + dy)
                  && x >= 0
                  && x < imageWidth
                  && y >= 0
                  && y < imageHeight)
                intPixels [x + dx + (y + dy) * imageWidth] = intPixels [x + y * imageWidth];
        }
        else
        {
          byte [] bytePixels = (byte [])pixelsArray;
          for (x = xStart; x <= xEnd; x+= xStep)
            for (y = yStart; y <= yEnd; y+= yStep)
              if (   !isClipped (x + dx, y + dy)
                  && x >= 0
                  && x < imageWidth
                  && y >= 0
                  && y < imageHeight)
                bytePixels [x + dx + (y + dy) * imageWidth] = bytePixels [x + y * imageWidth];
        }
      }
    }
  }

  public void drawLine (int x1, int y1, int x2, int y2)
  {
    x1 += translateX;
    y1 += translateY;
    x2 += translateX;
    y2 += translateY;

    int dx = x2 - x1;
    int dy = y2 - y1;
    synchronized (image)
    {
      if (dx == 0)
      {
        if (y1 < y2)
          drawVerticalLine (x1, y1, y2);
        else
          drawVerticalLine (x1, y2, y1);
      }
      else if (dy == 0)
      {
        if (x1 < x2)
          drawHorizontalLine (x1, x2, y1);
        else
          drawHorizontalLine (x2, x1, y1);
      }
      else
      {
        // From Bresenham's line generation algorithm
        // v1.1 : suppressed all float use
        boolean swapXY = false;
        int     dxNeg = 1;
        int     dyNeg = 1;
        boolean negativeSlope = false;
        if (Math.abs (dy) > Math.abs (dx))
        {
          int temp = x1;
          x1 = y1;
          y1 = temp;
          temp = x2;
          x2 = y2;
          y2 = temp;
          dx = x2 - x1;
          dy = y2 - y1;
          swapXY = true;
        }

        if (x1 > x2)
        {
          int temp = x1;
          x1 = x2;
          x2 = temp;
          temp = y1;
          y1 = y2;
          y2 = temp;
          dx = x2 - x1;
          dy = y2 - y1;
        }

        if (dy * dx < 0)
        {
          if (dy < 0)
          {
            dyNeg = -1;
            dxNeg = 1;
          }
          else
          {
            dyNeg = 1;
            dxNeg = -1;
          }
          negativeSlope = true;
        }

        int d      = 2 * (dy * dyNeg) - (dx * dxNeg);
        int incrH  = 2 * dy * dyNeg;
        int incrHV = 2 * ((dy * dyNeg) - (dx * dxNeg));
        int x = x1;
        int y = y1;
        int tempX = x;
        int tempY = y;

        if (swapXY)
        {
          int temp = x;
          x = y;
          y = temp;
        }

        drawPoint (x, y);
        x = tempX;
        y = tempY;

        while (x < x2)
        {
          if (d <= 0)
          {
            x++;
            d += incrH;
          }
          else
          {
            d += incrHV;
            x++;
            if (!negativeSlope)
              y ++;
            else
              y --;
          }

          tempX = x;
          tempY = y;
          if (swapXY)
          {
            int temp = x;
            x = y;
            y = temp;
          }
          drawPoint (x, y);
          x = tempX;
          y = tempY;
        }
      }
    }
  }

  private boolean isClipped (int x, int y)
  {
    return (   x < clip.x
            || x >= clip.x + clip.width
            || y < clip.y
            || y >= clip.y + clip.height);
  }

  private void drawPoint (int x, int y)
  {
    // This method is only called by methods who synchronized on image
    // TODO : optimize because some methods calling drawPoint () already checked clipping
    if (!isClipped (x, y))
    {
      // v2.3 : added support for 8 bit color images
      Object pixelsArray = image.getPixelsArray ();
      // v1.1 : pixelsArray may be null because of image flush ()
      if (pixelsArray != null)
      {
        int pixelIndex = x + y * image.getWidth ();
        
        if (pixelsArray instanceof int [])
        {
          int  [] intPixels  = (int [])pixelsArray;
          intPixels [pixelIndex] = XORMode
                                    ? 0xFF000000 | ((intPixels [pixelIndex] ^ ARGB) ^ XorRGB)
                                    : ARGB;
        }
        else
        {
          // v2.3 (thanks to Fernando Echeverria)
          // Added support for 8 bit color images
          byte [] bytePixels = (byte [])pixelsArray;
          if (XORMode)
          {
            int oldColor = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), bytePixels [pixelIndex] & 0xFF) ;
            int newColor = 0xFF000000 | ((oldColor ^ ARGB) ^ XorRGB);
            // This may not work if IndexColorModel doesn't provide all possible Xor colors
            bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
          }
          else
            bytePixels [pixelIndex] = ARGBIndex != -1 ? (byte)ARGBIndex : 0;
        }
      }
    }
  }

  // v1.1 : added the method private void drawPoint (Object pixelsArray, int pixelIndex) to simplify code
  // v2.3 : deleted drawPoint (Object pixelsArray, int pixelIndex) because its code is inlined for optimization

  // x1 < x2
  private void drawHorizontalLine (int x1, int x2, int y)
  {
    if (   y >= clip.y
        && y < clip.y + clip.height)
    {
      Object pixelsArray  = image.getPixelsArray ();
      // v1.1 : pixelsArray may be null because of image flush ()
      if (pixelsArray != null)
      {
        int offset     = y * image.getWidth ();
        int startIndex = Math.max (x1, clip.x) + offset;
        int endIndex   = Math.min (x2, clip.x + clip.width - 1) + offset;
      
        // v2.3 Inlined drawPoint ()
        if (pixelsArray instanceof int [])
        {
          int  [] intPixels  = (int [])pixelsArray;
          if (XORMode)
            for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
              intPixels [pixelIndex] = 0xFF000000 | ((intPixels [pixelIndex] ^ ARGB) ^ XorRGB);
          else
            for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
              intPixels [pixelIndex] = ARGB;
        }
        else
        {
          // v2.3 (thanks to Fernando Echeverria)
          // Added support for 8 bit color images
          byte [] bytePixels = (byte [])pixelsArray;
          if (XORMode)
            for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
            {
              int oldColor = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), bytePixels [pixelIndex] & 0xFF) ;
              int newColor = 0xFF000000 | ((oldColor ^ ARGB) ^ XorRGB);
              // This may not work if IndexColorModel doesn't provide all possible Xor colors
              bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
            }
          else
            if (ARGBIndex != -1)
              for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                bytePixels [pixelIndex] = (byte)ARGBIndex;
            else
              for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                bytePixels [pixelIndex] = 0;
        }
      }
    }
  }

  // y1 < y2
  private void drawVerticalLine (int x, int y1, int y2)
  {
    if (   x >= clip.x
        && x < clip.x + clip.width)
    {
      Object pixelsArray  = image.getPixelsArray ();
      // v1.1 : pixelsArray may be null because of image flush ()
      if (pixelsArray != null)
      {
        int imageWidth = image.getWidth ();
        int startIndex = x + Math.max (y1, clip.y) * imageWidth;
        int endIndex   = x + Math.min (y2, clip.y + clip.height - 1) * imageWidth;
      
        // v2.3 Inlined drawPoint ()
        if (pixelsArray instanceof int [])
        {
          int  [] intPixels  = (int [])pixelsArray;
          if (XORMode)
            for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex += imageWidth)            
              intPixels [pixelIndex] = 0xFF000000 | ((intPixels [pixelIndex] ^ ARGB) ^ XorRGB);
          else
            for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex += imageWidth)            
              intPixels [pixelIndex] = ARGB;
        }
        else
        {
          // v2.3 (thanks to Fernando Echeverria)
          // Added support for 8 bit color images
          byte [] bytePixels = (byte [])pixelsArray;
          if (XORMode)
            for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex += imageWidth)            
            {
              int oldColor = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), bytePixels [pixelIndex] & 0xFF) ;
              int newColor = 0xFF000000 | ((oldColor ^ ARGB) ^ XorRGB);
              // This may not work if IndexColorModel doesn't provide all possible Xor colors
              bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
            }
          else
            if (ARGBIndex != -1)
              for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex += imageWidth)            
                bytePixels [pixelIndex] = (byte)ARGBIndex;
            else
              for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex += imageWidth)            
                bytePixels [pixelIndex] = 0;
        }
      }
    }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void fillRect (int x, int y, int width, int height)
  {
    // Clip the requested rectangle with current clipping region, expressing
    // the result in absolute coordinates.
    int xMin = Math.max (x + translateX, clip.x);
    int xMax = Math.min (x + width + translateX, clip.x + clip.width) - 1;
    int yMin = Math.max (y + translateY, clip.y);
    int yMax = Math.min (y + height + translateY, clip.y + clip.height) - 1;

    // Check if completely clipped out
    if (   xMin <= xMax 
        && yMin <= yMax)
      synchronized (image)
      {
        Object pixelsArray  = image.getPixelsArray ();
        // v1.1 : pixelsArray may be null because of image flush ()
        if (pixelsArray != null)
        {
          int imageWidth = image.getWidth();
          int rowOffset  = yMin * imageWidth;
          int startIndex = xMin + rowOffset;
          int endIndex   = xMax + rowOffset;
      
          // v2.3 Inlined drawPoint ()
          if (pixelsArray instanceof int [])
          {
            int  [] intPixels  = (int [])pixelsArray;
            if (XORMode)
              for (int y1 = yMin; y1 <= yMax; y1++, startIndex += imageWidth, endIndex += imageWidth)
                for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                  intPixels [pixelIndex] = 0xFF000000 | ((intPixels [pixelIndex] ^ ARGB) ^ XorRGB);
            else
              for (int y1 = yMin; y1 <= yMax; y1++, startIndex += imageWidth, endIndex += imageWidth)
                for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                  intPixels [pixelIndex] = ARGB;
          }
          else
          {
            // v2.3 (thanks to Fernando Echeverria)
            // Added support for 8 bit color images
            byte [] bytePixels = (byte [])pixelsArray;
            if (XORMode)
              for (int y1 = yMin; y1 <= yMax; y1++, startIndex += imageWidth, endIndex += imageWidth)
                for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                {
                  int oldColor = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), bytePixels [pixelIndex] & 0xFF) ;
                  int newColor = 0xFF000000 | ((oldColor ^ ARGB) ^ XorRGB);
                  // This may not work if IndexColorModel doesn't provide all possible Xor colors
                  bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
                }
            else
              if (ARGBIndex != -1)
                for (int y1 = yMin; y1 <= yMax; y1++, startIndex += imageWidth, endIndex += imageWidth)
                  for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                    bytePixels [pixelIndex] = (byte)ARGBIndex;
              else
                for (int y1 = yMin; y1 <= yMax; y1++, startIndex += imageWidth, endIndex += imageWidth)
                  for (int pixelIndex = startIndex; pixelIndex <= endIndex; pixelIndex++)            
                    bytePixels [pixelIndex] = 0;
          }
        }
      }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void clearRect (int x, int y, int width, int height)
  {
    fillRect (x, y, width, height);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void drawRoundRect (int x, int y, int width, int height,
                             int arcWidth, int arcHeight)
  {
    drawLine (x + arcWidth / 2, y, x + width - arcWidth / 2, y);
    drawLine (x, y + arcHeight / 2, x, y + height - arcHeight / 2);
    drawLine (x + arcWidth / 2, y + height, x + width - arcWidth / 2, y + height);
    drawLine (x + width, y + arcHeight / 2, x + width, y + height - arcHeight / 2);

    drawArc (x, y, arcWidth, arcHeight, 90, 90);
    drawArc (x + width - arcWidth, y, arcWidth, arcHeight, 0, 90);
    drawArc (x, y + height + - arcHeight, arcWidth, arcHeight, 180, 90);
    drawArc (x + width - arcWidth, y + height + - arcHeight, arcWidth, arcHeight, 270, 90);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void fillRoundRect (int x, int y, int width, int height,
                             int arcWidth, int arcHeight)
  {
    fillRect (x + arcWidth / 2, y, width - arcWidth + 1, height);
    fillRect (x, y + arcHeight / 2 - 1, arcWidth / 2, height - arcHeight);
    fillRect (x + width - arcWidth / 2, y + arcHeight / 2 - 1, arcWidth / 2, height - arcHeight);

    fillArc (x, y, arcWidth - 1, arcHeight - 1, 90, 90);
    fillArc (x + width - arcWidth, y, arcWidth - 1, arcHeight - 1, 0, 90);
    fillArc (x, y + height + - arcHeight, arcWidth - 1, arcHeight - 1, 180, 90);
    fillArc (x + width - arcWidth, y + height + - arcHeight, arcWidth - 1, arcHeight - 1, 270, 90);
  }

  /**
   * Same implementation as in the class <code>java.awtGraphics</code> except
   * it doesn't use <code>Color</code> class.
   */
  public void draw3DRect (int x, int y, int width, int height, boolean raised)
  {
    int currentARGB = ARGB;
    int brighter = getBrighterColor (ARGB);
    int darker   = getDarkerColor (ARGB);
    
    // v2.3 : added support for 8 bit color images
    int currentIndex  = ARGBIndex;
    int brighterIndex = 0;
    int darkerIndex   = 0;

    if (   image.getColorModel () != null
        && image.getColorModel () instanceof IndexColorModel)
    {
      brighterIndex = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), brighter);
      darkerIndex   = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), darker);
    }

    ARGB      = raised ? brighter      : darker;
    ARGBIndex = raised ? brighterIndex : darkerIndex;
    drawLine (x, y, x, y + height);
    drawLine (x + 1, y, x + width - 1, y);
    ARGB      = raised ? darker      : brighter;
    ARGBIndex = raised ? darkerIndex : brighterIndex;
    drawLine (x + 1, y + height, x + width, y + height);
    drawLine (x + width, y, x + width, y + height - 1);
    // Restore old color
    ARGB      = currentARGB;
    ARGBIndex = currentIndex;
  }

  /**
   * Same implementation as in the class <code>java.awtGraphics</code> except
   * it doesn't use <code>Color</code> class.
   */
  public void fill3DRect (int x, int y, int width, int height, boolean raised)
  {
    int currentARGB = ARGB;
    int brighter = getBrighterColor (ARGB);
    int darker   = getDarkerColor (ARGB);

    // v2.3 : added support for 8 bit color images
    int currentIndex  = ARGBIndex;
    int brighterIndex = 0;
    int darkerIndex   = 0;

    if (   image.getColorModel () != null
        && image.getColorModel () instanceof IndexColorModel)
    {
      brighterIndex = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), brighter);
      darkerIndex   = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), darker);
    }

    if (!raised)
    {
      ARGB = darker;
      ARGBIndex = darkerIndex;
    }
    
    fillRect (x + 1, y + 1, width - 2, height - 2);
    ARGB      = raised ? brighter      : darker;
    ARGBIndex = raised ? brighterIndex : darkerIndex;
    drawLine (x, y, x, y + height - 1);
    drawLine (x + 1, y, x + width - 2, y);
    ARGB      = raised ? darker      : brighter;
    ARGBIndex = raised ? darkerIndex : brighterIndex;
    drawLine (x + 1, y + height - 1, x + width - 1, y + height - 1);
    drawLine (x + width - 1, y, x + width - 1, y + height - 2);
    // Restore old color
    ARGB      = currentARGB;
    ARGBIndex = currentIndex;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void drawOval (int x, int y, int width, int height)
  {
    synchronized (image)
    {
      parseCircleQuarter (x, y, width, height, false,
                          new CircleQuarterListener ()
                            {
                              public void newPoint (int xLeft, int yTop, int xRight, int yBottom)
                              {
                                // Draw each of the 4 points
                                drawPoint (xLeft, yTop);
                                drawPoint (xRight, yTop);
                                drawPoint (xLeft, yBottom);
                                drawPoint (xRight, yBottom);
                              }
                            });
    }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void fillOval (int x, int y, int width, int height)
  {
    synchronized (image)
    {
      parseCircleQuarter (x, y, width, height, true,
                          new CircleQuarterListener ()
                            {
                              public void newPoint (int xLeft, int yTop, int xRight, int yBottom)
                              {
                                drawHorizontalLine (xLeft, xRight, yTop);
                                if (yTop != yBottom)
                                  drawHorizontalLine (xLeft, xRight, yBottom);
                              }
                            });
    }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void drawArc (int x, int y, int width, int height,
                       int start, int arcAngle)
  {
    if (arcAngle == 0)
      return;

    start %= 360;
    if (start < 0)
      start += 360;

    if (arcAngle % 360 == 0)
      arcAngle = 360;
    else
      arcAngle %= 360;

    final int startAngle = arcAngle > 0
                             ? start
                             : (start + arcAngle < 0
                                  ? start + arcAngle + 360
                                  : start + arcAngle);
    final int endAngle   = arcAngle > 0
                             ? (start + arcAngle > 360
                                  ? start + arcAngle - 360
                                  : start + arcAngle)
                             : start;
    final int centerX = x + translateX + width / 2;
    final int centerY = y + translateY + height / 2;
    final int xPoints [] = new int [7];
    final int yPoints [] = new int [7];
    final int nPoints    = getBoundingShape (xPoints, yPoints, startAngle, Math.abs (arcAngle), centerX, centerY,
                                             x + translateX - 1, y + translateY - 1, width + 2, height + 2);
    final PJARectangle bounds = getBoundingBox (xPoints, yPoints, nPoints).intersection (clip);
    synchronized (image)
    {
      parseCircleQuarter (x, y, width, height, false,
                          new CircleQuarterListener ()
                            {
                              public void newPoint (int xLeft, int yTop, int xRight, int yBottom)
                              {
                                // Draw each of the 4 points
                                drawArcPoint (xPoints, yPoints, nPoints, bounds, xLeft, yTop);
                                drawArcPoint (xPoints, yPoints, nPoints, bounds, xRight, yTop);
                                drawArcPoint (xPoints, yPoints, nPoints, bounds, xLeft, yBottom);
                                drawArcPoint (xPoints, yPoints, nPoints, bounds, xRight, yBottom);
                              }
                            });
    }
  }

  /**
   * This method builds a bounding polygon of an arc of arcAngle degres starting at startAngle,
   * and returns the number of points set in xPoints and yPoints tables.
   */
  private int getBoundingShape (int xPoints [], int yPoints [],
                                int startAngle, int arcAngle, int centerX, int centerY,
                                int boundingX, int boundingY, int boundingWidth, int boundingHeight)
  {
    // First point is the center
    xPoints [0] = centerX;
    yPoints [0] = centerY;
    // Second point is the projection of the starting point of the arc on the bounding rectangle.
    Point startPoint = getBoundingPointAtAngle (boundingX, boundingY, boundingWidth, boundingHeight, startAngle);
    xPoints [1] = startPoint.x;
    yPoints [1] = startPoint.y;
    int i = 2;
    // Add all the corners of the bounding rectangle between the startAngle and endAngle.
    for (int angle = 0; angle < arcAngle; i++, angle += 90)
    {
      if (   angle + 90 > arcAngle
          && ((startAngle + angle - 45) % 360) / 90 == ((startAngle + arcAngle + 45) % 360) / 90)
        break;
      int modAngle = (startAngle + angle) % 360;
      if (modAngle > 315 || modAngle <= 45)
      {
        xPoints [i] = boundingX + boundingWidth;
        yPoints [i] = boundingY;
      }
      else if (modAngle > 135  && modAngle <= 225)
      {
        xPoints [i] = boundingX;
        yPoints [i] = boundingY + boundingHeight;
      }
      else if (modAngle > 45 && modAngle <= 135)
      {
        xPoints [i] = boundingX;
        yPoints [i] = boundingY;
      }
      else // (modAngle > 225  && modAngle <= 315)
      {
        xPoints [i] = boundingX + boundingWidth;
        yPoints [i] = boundingY + boundingHeight;
      }
    }
    // Add last point (projection of the end point of the arc on the bounding rectangle).
    Point endPoint = getBoundingPointAtAngle (boundingX, boundingY, boundingWidth, boundingHeight,
                                              (startAngle + arcAngle) % 360);
    if (   xPoints [i - 1] != endPoint.x
        || yPoints [i - 1] != endPoint.y)
    {
      xPoints [i]   = endPoint.x;
      yPoints [i++] = endPoint.y;
    }

    return i;
  }

  // All the values of tangent for integer angles 0 to 45 deg
  // multiplied by (1 << 16) (thus the division to
  // get back tangent is very easy : just have to shift >>> 16)
  private final int TAN_0_TO_45_SHIFT_16 [] = {0,1144,2289,3435,4583,
                                               5734,6888,8047,9210,10380,
                                               11556,12739,13930,15130,16340,
                                               17560,18792,20036,21294,22566,
                                               23853,25157,26478,27818,29179,
                                               30560,31964,33392,34846,36327,
                                               37837,39378,40951,42560,44205,
                                               45889,47615,49385,51202,53070,
                                               54991,56970,59009,61113,63287,
                                               65536};

  /**
   * Returns the tangent of <code>angle</code> if angle is between -45 deg and 45 deg or
   * between 135 deg and 225 deg, or returns the cotangent of <code>angle</code>
   * if angle is between 45 deg and 135 deg or between 225 deg and 315 deg
   */
  private int tanOrCotanShift16 (int angle)
  {
    // tan values for angles between -45 and 45 or 135 and 225 deg
    if (angle <= 45)
      return TAN_0_TO_45_SHIFT_16 [angle];
    else if (angle >= 315)
      return -TAN_0_TO_45_SHIFT_16 [360 - angle];
    else if (angle >= 135  && angle <= 180)
      return -TAN_0_TO_45_SHIFT_16 [180 - angle];
    else if (angle >= 180  && angle <= 225)
      return TAN_0_TO_45_SHIFT_16 [angle - 180];
    // cotan values for angles between 45 and 135 or 225 and 315 deg
    else if (angle >= 45  && angle <= 90)
      return TAN_0_TO_45_SHIFT_16 [90 - angle];
    else if (angle >= 90  && angle <= 135)
      return -TAN_0_TO_45_SHIFT_16 [angle - 90];
    else if (angle >= 225  && angle <= 270)
      return TAN_0_TO_45_SHIFT_16 [270 - angle];
    else // (angle >= 270  && angle <= 315)
      return -TAN_0_TO_45_SHIFT_16 [angle - 270];
  }

  private Point getBoundingPointAtAngle (int boundingX, int boundingY, int boundingWidth, int boundingHeight, int angle)
  {
    // v1.1 : suppressed float use and Math.tan () calls (65536 = 2 pow 16)
    if (angle >= 315 || angle <= 45)
      return new Point (boundingX + boundingWidth, boundingY + (boundingHeight * (65536 - tanOrCotanShift16 (angle)) >>> 17));
    else if (angle > 45 && angle < 135)
      return new Point (boundingX + (boundingWidth * (65536 + tanOrCotanShift16 (angle)) >>> 17), boundingY);
    else if (angle >= 135  && angle <= 225)
      return new Point (boundingX, boundingY + (boundingHeight * (65536 + tanOrCotanShift16 (angle)) >>> 17));
    else // (angle > 225  && angle < 315)
      return new Point (boundingX + (boundingWidth * (65536 - tanOrCotanShift16 (angle)) >>> 17), boundingY + boundingHeight);
  }

  private void drawArcPoint (int xPoints [], int yPoints [], int nPoints, PJARectangle bounds, int x, int y)
  {
    if (contains (xPoints, yPoints, nPoints, bounds, x, y))
      drawPoint (x, y);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void fillArc (int x, int y, int width, int height,
                       int start, int arcAngle)
  {
    if (arcAngle == 0)
      return;

    start %= 360;
    if (start < 0)
      start += 360;

    if (arcAngle % 360 == 0)
      fillOval (x, y, width, height);
    else
      arcAngle %= 360;

    final int startAngle = arcAngle > 0
                             ? start
                             : (start + arcAngle < 0
                                  ? start + arcAngle + 360
                                  : start + arcAngle);
    final int endAngle   = arcAngle > 0
                             ? (start + arcAngle > 360
                                  ? start + arcAngle - 360
                                  : start + arcAngle)
                             : start;
    final int centerX = x + translateX + width / 2;
    final int centerY = y + translateY + height / 2;
    final int xPoints [] = new int [7];
    final int yPoints [] = new int [7];
    final int nPoints = getBoundingShape (xPoints, yPoints, startAngle, Math.abs (arcAngle), centerX, centerY,
                                          x + translateX - 1, y + translateY - 1, width + 2, height + 2);
    final PJARectangle bounds = getBoundingBox (xPoints, yPoints, nPoints).intersection (clip);
    synchronized (image)
    {
      parseCircleQuarter (x, y, width, height, true,
                          new CircleQuarterListener ()
                            {
                              public void newPoint (int xLeft, int yTop, int xRight, int yBottom)
                              {
                                drawArcHorizontalLine (xPoints, yPoints, nPoints, bounds, xLeft, xRight, yTop);
                                if (yTop != yBottom)
                                  drawArcHorizontalLine (xPoints, yPoints, nPoints, bounds, xLeft, xRight, yBottom);
                              }
                            });
    }
  }

  private void drawArcHorizontalLine (int xPoints [], int yPoints [], int nPoints, PJARectangle bounds,
                                      int xLeft, int xRight, int y)
  {
    // v1.1 : avoid computing points out of clipping rectangle
    if (   y >= clip.y
        && y < clip.y + clip.height)
    {
      int  xMax = Math.min (xRight, clip.x + clip.width - 1);
      for (int x = Math.max (xLeft, clip.x); x <= xMax; x++)
        if (contains (xPoints, yPoints, nPoints, bounds, x, y))
          drawPoint (x, y);
    }
  }

  /**
   * Returns the rounded result of <code>dividend / divisor</code>, avoiding the use of floating
   * point operation (returns the same as <code>Math.round((float)dividend / divisor)</code>).
   * @param dividend A <code>int</code> number to divide.
   * @param divisor  A <code>int</code> divisor.
   * @return dividend / divisor rounded to the closest <code>int</code> integer.
   */
  public static int roundDiv (int dividend, int divisor)
  {
    final int remainder = dividend % divisor;
    if (Math.abs (remainder) * 2 <= Math.abs (divisor))
      return dividend / divisor;
    else
      if (dividend * divisor < 0)
        return dividend / divisor - 1;
      else
        return dividend / divisor + 1;
  }

  /**
   * Returns the rounded result of <code>dividend / divisor</code>, avoiding the use of floating
   * point operation (returns the same as <code>Math.round((double)dividend / divisor)</code>).
   * @param dividend A <code>long</code> number to divide.
   * @param divisor  A <code>long</code> divisor.
   * @return dividend / divisor rounded to the closest <code>long</code> integer.
   */
  public static long roundDiv (long dividend, long divisor)
  {
    final long remainder = dividend % divisor;
    if (Math.abs (remainder) * 2 <= Math.abs (divisor))
      return dividend / divisor;
    else
      if (dividend * divisor < 0)
        return dividend / divisor - 1;
      else
        return dividend / divisor + 1;
  }

  private interface CircleQuarterListener
  {
    public void newPoint(int xLeft, int yTop, int xRight, int yBottom);
  }

  /**
   * Enumerates all the points of the quarter of an ellipse. Each computed point is sent
   * to the method <code>newPoint ()</code> of the interface <code>CircleQuarterListener</code>.
   */
  private void parseCircleQuarter (int x, int y, int width, int height,
                                   boolean fill,
                                   CircleQuarterListener listener)
  {
    // v1.1 : changed to avoid the use of float
    int a = width / 2;    // half width of an ellipse
    int b = height / 2;   // half height of an ellipse
    long squareA  = width * width / 4;   // = a * a
    long squareB  = height * height / 4; // = b * b
    long squareAB = roundDiv ((long)width * width * height * height, 16L); // = a * a * b * b

    x += translateX;
    y += translateY;
    int centerX = x + a;
    int centerY = y + b;

    // Original algorithm made for even lengths
    int deltaX = (width % 2 == 0) ? 0 : 1;
    int deltaY = (height % 2 == 0) ? 0 : 1;

    // From Bresenham's circle generation algorithm
    int currentY = b;
    int currentX = 0;

    // These variables are used to optimize drawing :
    // horizontal lines that overlapp on a given y are drawn only once
    // when in fill mode
    int lastx1 = centerX - currentX;
    int lastx2 = centerX + currentX + deltaX;
    int lasty1 = centerY - currentY;
    int lasty2 = centerY + currentY + deltaY;
    while (currentX <= a && currentY >= 0)
    {
      long deltaA = (currentX + 1) * (currentX + 1) * squareB + currentY * currentY * squareA - squareAB;
      long deltaB = (currentX + 1) * (currentX + 1) * squareB + (currentY - 1) * (currentY - 1) * squareA - squareAB;
      long deltaC = currentX * currentX * squareB + (currentY - 1) * (currentY - 1) * squareA - squareAB;
      if (deltaA <= 0)
        currentX++;
      else if (deltaC >= 0)
        currentY--;
      else
      {
        long min = Math.min (Math.abs (deltaA), Math.min (Math.abs (deltaB), Math.abs (deltaC)));
        if (min == Math.abs (deltaA))
          currentX++;
        else if (min == Math.abs (deltaC))
          currentY--;
        else
        {
          currentX++;
          currentY--;
        }
      }

      int x1 = centerX - currentX;
      int x2 = centerX + currentX + deltaX;
      int y1 = centerY - currentY;
      int y2 = centerY + currentY + deltaY;
      if (   !fill
          || lasty1 != y1)
      {
        // Call listeners with the 4 new points
        listener.newPoint (lastx1, lasty1, lastx2, lasty2);
        lasty1 = y1;
        lasty2 = y2;
      }
      lastx1  = x1;
      lastx2 = x2;
    }
    if (lasty1 < lasty2)
      for ( ; lasty1 <= lasty2; lasty1++, lasty2--)
        listener.newPoint (centerX - a, lasty1,
                           centerX + a + deltaX, lasty2);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void drawPolyline (int xPoints[], int yPoints[], int nPoints)
  {
    for (int i = 1; i < nPoints; i++)
      drawLine (xPoints [i - 1], yPoints [i - 1], xPoints [i], yPoints [i]);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void drawPolygon (int xPoints[], int yPoints[], int nPoints)
  {
    drawPolyline (xPoints, yPoints, nPoints);
    drawLine (xPoints [nPoints - 1], yPoints [nPoints - 1], xPoints [0], yPoints [0]);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void fillPolygon (int xPoints[], int yPoints[], int nPoints)
  {
    // Translate all points when necessary
    int xPointsCopy [];
    if (translateX == 0)
      xPointsCopy = xPoints;
    else
    {
      xPointsCopy = (int [])xPoints.clone ();
      for (int i = 0; i < nPoints; i++)
        xPointsCopy [i] += translateX;
    }
    int yPointsCopy [];
    if (translateY == 0)
      yPointsCopy = yPoints;
    else
    {
      yPointsCopy = (int [])yPoints.clone ();
      for (int i = 0; i < nPoints; i++)
        yPointsCopy [i] += translateY;
    }

    // v1.1 : added clipping to avoid computing points out of clipping rectangle
    PJARectangle bounds = getBoundingBox (xPointsCopy, yPointsCopy, nPoints).intersection (clip);
    // v1.1 : added synchronized
    synchronized (image)
    {
      for (int x = bounds.x; x < bounds.x + bounds.width; x++)
        for (int y = bounds.y; y < bounds.y + bounds.height; y++)
          if (contains (xPointsCopy, yPointsCopy, nPoints, bounds, x, y))
            drawPoint (x, y);
    }
  }

  // v1.1 : Added getBoundingBox () method.
  private PJARectangle getBoundingBox (int xpoints[], int ypoints[], int npoints)
  {
    int boundsMinX = Integer.MAX_VALUE;
    int boundsMinY = Integer.MAX_VALUE;
    int boundsMaxX = Integer.MIN_VALUE;
    int boundsMaxY = Integer.MIN_VALUE;

    for (int i = 0; i < npoints; i++)
    {
      int x = xpoints[i];
      boundsMinX = Math.min (boundsMinX, x);
      boundsMaxX = Math.max (boundsMaxX, x);
      int y = ypoints[i];
      boundsMinY = Math.min (boundsMinY, y);
      boundsMaxY = Math.max (boundsMaxY, y);
    }

    return new PJARectangle (boundsMinX, boundsMinY, boundsMaxX - boundsMinX, boundsMaxY - boundsMinY);
  }

  // v1.1 : Added contains () method : it's the same code as
  // Polygon.inside () of JDK 1.1 method except it doesn't use float type.
  private boolean contains (int xPoints [], int yPoints [], int nPoints, PJARectangle bounds, int x, int y)
  {
    if (   (   bounds != null
            && bounds.inside (x, y))
        || (   bounds == null
            && getBoundingBox (xPoints, yPoints, nPoints).inside (x, y)))
    {
      int hits = 0;
      int ySave = 0;

      // Find a vertex that's not on the halfline
      int i = 0;
      while (i < nPoints && yPoints [i] == y)
        i++;

      // Walk the edges of the polygon
      for (int n = 0; n < nPoints; n++)
      {
        int j = (i + 1) % nPoints;

        int dx = xPoints [j] - xPoints [i];
        int dy = yPoints [j] - yPoints [i];

        // Ignore horizontal edges completely
        if (dy != 0)
        {
          // Check to see if the edge intersects
          // the horizontal halfline through (x, y)
          int rx = x - xPoints [i];
          int ry = y - yPoints [i];

          // Deal with edges starting or ending on the halfline
          if (yPoints [j] == y && xPoints [j] >= x)
            ySave = yPoints [i];

          if (yPoints[i] == y && xPoints[i] >= x)
            if ((ySave > y) != (yPoints[j] > y))
              hits--;

          /*
          // Tally intersections with halfline
          float s = (float)ry / (float)dy;
          if (s >= 0.0 && s <= 1.0 && (s * dx) >= rx)
            hits++;
          */

          // Changed Polygon.inside () method here without float
          if (   ry * dy >= 0
              && (   ry <= dy && ry >= 0
                  || ry >= dy && ry <= 0)
              && roundDiv (dx * ry, dy) >= rx)
            hits++;
        }

        i = j;
      }

      // Inside if number of intersections odd
      return (hits % 2) != 0;
    }

    return false;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * Strings are drawn using .pjaf font files read in <code>PJAFontData</code> objects by
   * <code>PJAGraphicsManager</code>.
   * @see com.eteks.awt.PJAGraphicsManager
   * @see PJAFontData
   * @see Graphics
   */
  public void drawString (String str, int x, int y)
  {
    checkFontLoading ();

    int charsCount = str.length();
    if (   fontPeer != null
        && charsCount > 0)
    {
      // Get general font info
      PJAFontData fontData = fontPeer.getFontData (fontSize);
      int  charHeight = fontData.getAscent () + fontData.getDescent ();
      int  baseLine   = fontData.getAscent ();
      
      // Convert to absolute coordinates
      x += translateX;
      y += translateY;
      
      // v2.3 (thanks to Fernando Echeverria)
      // New drawString () implementation with clipping 
      // and drawing optimization
      
      // Find clipped area that will be used by the string
      // The area is : x to x + stringWidth and y - ascent to y + descent

      // First vertical
      int yMin = Math.max (y - baseLine, clip.y);
      int yMax = Math.min (y - baseLine + charHeight, clip.y + clip.height) - 1;
      if (yMin > yMax)  // fully clipped out
        return;
        
      // Offset from top of character where first non-clipped pixel row starts
      int yStart = yMin - (y - baseLine);

      // Now horizontal.
      // First compute the string width (or total char advance)
      int stringWidth = 0;
      int xMin        = x;
      int xMax        = x;
      for (int i = 0; i < charsCount; ++i)
      {
        char currentChar = str.charAt (i);
        // Width in pixels used by this character (or char advance)
        int charWidth = fontData.getCharWidth (currentChar);
        // True width in pixels used by this character
        int totalCharWidth = fontData.getCharPixelsWidth (currentChar);
        // Left padding for this character
        int leftOffsetAtBaseline = fontData.getCharOffsetAtBaseline (currentChar);
        
        // To get the min x coordinnate, the left offset at baseline of italic
        // chars must be taken into account
        if (leftOffsetAtBaseline < -stringWidth)
          xMin = Math.min (xMin, x + (stringWidth + leftOffsetAtBaseline));          

        // To get the max x coordinnate, the right offset at baseline of italic
        // chars must be taken into account
        xMax = Math.max (xMax, x + stringWidth + leftOffsetAtBaseline + totalCharWidth);          

        stringWidth += charWidth;
      }

      xMin = Math.max (xMin, clip.x);
      xMax = Math.min (xMax, clip.x + clip.width) - 1;
      if (xMin > xMax)  // fully clipped out
        return;
      
      synchronized (image)
      {
        Object pixelsArray = image.getPixelsArray ();
        if (pixelsArray != null)
        {
          int imageWidth     = image.getWidth ();
          // Get the linear bit array representing the pixels in all the characters
          // in this font displayed side by side.
          int [] charBits = fontData.getCharBits();
          // Get the width (in bits) of each scanline stored in this array
          int charBitsScanSize = fontData.getCharBitsScanSize();
          
          // Draw each character. Start from the first one, skipping some if
          // clipped. This way we can easily find the first non-clipped character.
          for (int i = 0, x0 = x; i < charsCount && x0 < xMax; ++i)
          {
            char currentChar = str.charAt (i);
            // Width in pixels used by this character (or char advance)
            int charWidth = fontData.getCharWidth (currentChar);
            // v1.2 : Don't try to display and throw exception for characters
            // with no image (out of range)
            if (charWidth > 0)
            {
              // True width in pixels used by this character
              int totalCharWidth = fontData.getCharPixelsWidth (currentChar);
              // Left padding for this character
              int leftOffsetAtBaseline = fontData.getCharOffsetAtBaseline (currentChar);
              // Starting and ending horizontal coordinates for drawing this character
              int xStart = Math.max (x0 + leftOffsetAtBaseline, xMin);
              int xEnd   = Math.min (x0 + leftOffsetAtBaseline + totalCharWidth - 1, xMax);
              
              // If this character is completely clipped out, just skip it
              if (xEnd >= xMin)
              {
                int charBitsOffset = fontData.getCharBitsOffset (currentChar);
                // Index for bit within charBits array, initialized to top-left
                // clipped pixel
                int charBitsPixelIndex = charBitsOffset + yStart * charBitsScanSize;
                if (x0 + leftOffsetAtBaseline < xMin)
                  charBitsPixelIndex += xMin - x0 - leftOffsetAtBaseline;
                // Index within image pixels array, initialize to top-left clipped pix
                int pixelIndex = yMin * imageWidth + xStart;

                // v2.3 Inlined drawPoint ()
                if (pixelsArray instanceof int [])
                {
                  int  [] intPixels  = (int [])pixelsArray;
                  if (XORMode)
                  {
                    for (int yp = yMin; 
                         yp <= yMax;
                         ++yp, charBitsPixelIndex += charBitsScanSize, pixelIndex += imageWidth)
                      for (int xp = xStart, pk = pixelIndex, ck = charBitsPixelIndex; 
                           xp <= xEnd;
                           ++xp, ++pk, ++ck)
                        if ((charBits [ck >> 5] & (1 << (ck & 0x1F))) != 0)
                        // If the current pixel bit is set, set the image pixel to
                        // its mix with current color and XOR color
                          intPixels [pk] = 0xFF000000 | ((intPixels [pk] ^ ARGB) ^ XorRGB);
                  }
                  else
                    for (int yp = yMin; 
                         yp <= yMax;
                         ++yp, charBitsPixelIndex += charBitsScanSize, pixelIndex += imageWidth)
                      for (int xp = xStart, pk = pixelIndex, ck = charBitsPixelIndex; 
                           xp <= xEnd;
                           ++xp, ++pk, ++ck)
                        if ((charBits [ck >> 5] & (1 << (ck & 0x1F))) != 0)
                          // If the current pixel bit is set, set the image pixel to the current color
                          // its mix with current color and XOR color
                          intPixels [pk] = ARGB;
                }
                else
                {
                  // v2.3 (thanks to Fernando Echeverria)
                  // Added support for 8 bit color images
                  byte [] bytePixels = (byte [])pixelsArray;
                  if (XORMode)
                  {
                    for (int yp = yMin; 
                         yp <= yMax;
                         ++yp, charBitsPixelIndex += charBitsScanSize, pixelIndex += imageWidth)
                      for (int xp = xStart, pk = pixelIndex, ck = charBitsPixelIndex; 
                           xp <= xEnd;
                           ++xp, ++pk, ++ck)
                        if ((charBits [ck >> 5] & (1 << (ck & 0x1F))) != 0)
                        {
                          int oldColor = manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), bytePixels [pk] & 0xFF) ;
                          int newColor = 0xFF000000 | ((oldColor ^ ARGB) ^ XorRGB);
                          // This may not work if IndexColorModel doesn't provide all possible Xor colors
                          bytePixels [pk] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
                        }
                  }
                  else
                    if (ARGBIndex != -1)
                    {
                      for (int yp = yMin; 
                           yp <= yMax;
                           ++yp, charBitsPixelIndex += charBitsScanSize, pixelIndex += imageWidth)
                        for (int xp = xStart, pk = pixelIndex, ck = charBitsPixelIndex; 
                             xp <= xEnd;
                             ++xp, ++pk, ++ck)
                          if ((charBits [ck >> 5] & (1 << (ck & 0x1F))) != 0)
                            bytePixels [pk] = (byte)ARGBIndex;
                    }
                    else
                      for (int yp = yMin; 
                           yp <= yMax;
                           ++yp, charBitsPixelIndex += charBitsScanSize, pixelIndex += imageWidth)
                        for (int xp = xStart, pk = pixelIndex, ck = charBitsPixelIndex; 
                             xp <= xEnd;
                             ++xp, ++pk, ++ck)
                          if ((charBits [ck >> 5] & (1 << (ck & 0x1F))) != 0)
                            bytePixels [pk] = 0;
                }
              }
            }
              
            // Advance starting x-coord for next character
            x0 += charWidth;
          }
        }
      }
    }
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  // Method using Java 2 classes
  public void drawString (AttributedCharacterIterator iterator,
                          int x, int y)
  {
    // Build a StringBuffer with iterator
    StringBuffer buffer = new StringBuffer (iterator.getEndIndex () - iterator.getBeginIndex ());
    for (char c = iterator.first (); c != AttributedCharacterIterator.DONE; c = iterator.next())
      buffer.append (c);
    // Basic implementation : no use of any attributes
    drawString (buffer.toString (), x, y);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public boolean drawImage (Image img, int x, int y,
                            ImageObserver observer)
  {
    return drawImage (img, x, y, null, observer);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public boolean drawImage (final Image img, final int x, final int y,
                            final Color bgcolor,
                            final ImageObserver observer)
  {
    final int destX = x + translateX;
    final int destY = y + translateY;
    // Produce image to retrieve image data in this local ImageConsumer
    img.getSource ().startProduction (new ImageConsumer ()
      {
        private int availableInfo;
        private int width;
        private int height;

        public void setDimensions (int width, int height)
        {
          this.width  = width;
          this.height = height;
          availableInfo |= ImageObserver.WIDTH | ImageObserver.HEIGHT;
          if (observer != null)
            observer.imageUpdate (img, availableInfo, 0, 0, width, height);
        }

        public void setHints (int hints)
        {
        }

        public void setProperties (Hashtable props)
        {
          availableInfo |= ImageObserver.PROPERTIES;
          if (observer != null)
            observer.imageUpdate (img, availableInfo, 0, 0, width, height);
        }

        public void setColorModel (ColorModel model)
        {
        }

        public void setPixels (int x, int y, int width, int height,
                               ColorModel model, byte pixels [], int offset, int scansize)
        {
          // v1.1 : added synchronized
          synchronized (image)
          {
            for (int row = 0; row < height; row++, y++)
            {
              int rowOff = offset + row * scansize;
              for (int col = 0; col < width; col++)
                // v1.2 : Added & 0xFF to disable sign bit
                drawImagePoint (x + col + destX, y + destY, bgcolor, model.getRGB (pixels [rowOff + col] & 0xFF));
            }
          }
        }

        public void setPixels (int x, int y, int width, int height,
                   ColorModel model, int pixels[], int offset, int scansize)
        {
          // v1.1 : added synchronized
          synchronized (image)
          {
            for (int row = 0; row < height; row++, y++)
            {
              int rowOff = offset + row * scansize;
              for (int col = 0; col < width; col++)
                // If model == null, consider it's the default RGB model
                drawImagePoint (x + col + destX, y + destY, bgcolor, model == null
                                                                       ? pixels [rowOff + col]
                                                                       : model.getRGB (pixels [rowOff + col]));
            }
          }
        }

        public void imageComplete (int status)
        {
          if (status == IMAGEERROR)
            availableInfo = ImageObserver.ERROR;
          else if (status == IMAGEABORTED)
            availableInfo = ImageObserver.ABORT | ImageObserver.ERROR;
          else if (status == STATICIMAGEDONE)
            availableInfo |= ImageObserver.ALLBITS;
          else if (status == SINGLEFRAMEDONE)
            availableInfo |= ImageObserver.FRAMEBITS;

          if (observer != null)
            if ((availableInfo & ImageObserver.ERROR) != 0)
              observer.imageUpdate (img, availableInfo, -1, -1, -1, -1);
            else
              observer.imageUpdate (img, availableInfo, 0, 0, width, height);
        }

        private void drawImagePoint (int x, int y, Color background, int color)
        {
          if (!isClipped (x, y))
          {
            // v2.3 : added support for 8 bit color images
            Object pixelsArray = image.getPixelsArray ();
            // v1.1 : pixels may be null because of image flush ()
            if (pixelsArray != null)
            {
              int  [] intPixels  = null;
              byte [] bytePixels = null;
          
              if (pixelsArray instanceof int [])
                intPixels = (int [])pixelsArray;
              else 
                bytePixels = (byte [])pixelsArray;
          
              int imageWidth   = image.getWidth ();
              int pixelIndex   = x + y * imageWidth;
              int currentColor = background != null
                                   ? background.getRGB () | 0xFF000000
                                   : (intPixels != null 
                                        ? intPixels [x + y * imageWidth]
                                        : image.getColorModel ().getRGB (bytePixels [x + y * imageWidth] & 0xFF));
              int currentAlpha = (currentColor & 0xFF000000) >>> 24;
              int alpha        = (color & 0xFF000000) >>> 24;

              if (alpha == 0)
              {
                if (intPixels != null)
                  intPixels [pixelIndex] = currentColor;
                else
                  bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), currentColor);
              }
              else if (currentAlpha == 0 || alpha == 255)
              {
                if (intPixels != null)
                  intPixels [pixelIndex] = color;
                else
                  bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), color);
              }
              else // if (currentAlpha == 1)
              {
                // Applied formula to each color component (RGB) :
                //   newColorComp = (255 - alpha) / 255 * currentColorComp + alpha / 255 * colorComp;
                // which can be simplified into :
                //   newColorComp = currentColorComp + alpha / 255 (currentColorComp - colorComp);
                int red = (currentColor & 0xFF0000) >>> 16;
                red = Math.min (255, red  + alpha * (((color & 0xFF0000) >>> 16) - red) / 255);
                int green = (currentColor & 0xFF00) >>> 8;
                green = Math.min (255, green  + alpha * (((color & 0xFF00) >>> 8) - green) / 255);
                int blue = currentColor & 0xFF;
                blue = Math.min (255, blue  + alpha * ((color & 0xFF) - blue) / 255);
                int newColor = 0xFF000000 | red << 16 | green << 8 | blue;
                if (intPixels != null)
                  intPixels [pixelIndex] = newColor;
                else
                  bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
              }
              /* TODO ??? : Find the good algorithm to render points with different alphas
              else
              {
                int newAlpha = Math.max (alpha, currentAlpha);
                int red   =  Math.min (255,
                                       (((currentColor & 0xFF0000) >>> 16) * currentAlpha + ((color & 0xFF0000) >>> 16) * alpha) / 255);
                int green =  Math.min (255,
                                       (((currentColor &   0xFF00) >>>  8) * currentAlpha + ((color &   0xFF00) >>>  8) * alpha) / 255);
                int blue  =  Math.min (255,
                                       (( currentColor &     0xFF       ) * currentAlpha + ( color &     0xFF       ) * alpha) / 255);
                int newColor = newAlpha << 24 | red << 16 | green << 8 | blue;
                if (intPixels != null)
                  intPixels [pixelIndex] = newColor;
                else
                  bytePixels [pixelIndex] = (byte)manager.getClosestColorIndex ((IndexColorModel)image.getColorModel (), newColor);
              }
              */
            }
          }
        }
      });

    return true;
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public boolean drawImage (Image img, int x, int y,
                           int width, int height,
                           ImageObserver observer)
  {
    return drawImage (img, x, y, width, height, null, observer);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public boolean drawImage (Image img, int x, int y,
                            int width, int height,
                            Color bgcolor,
                            ImageObserver observer)
  {
    // To resize the image, just use an AreaAveragingScaleFilter
    // (not supported in Java 1.0 : should be taken out of Java 1.1 library and eliminate float use)
    return drawImage (manager.createImage (new FilteredImageSource (img.getSource (),
                                                                    new AreaAveragingScaleFilter (width, height))),
                      x, y, bgcolor, observer);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public boolean drawImage (Image img,
                            int dx1, int dy1, int dx2, int dy2,
                            int sx1, int sy1, int sx2, int sy2,
                            ImageObserver observer)
  {
    return drawImage (img, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null, observer);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public boolean drawImage (Image img,
                            int dx1, int dy1, int dx2, int dy2,
                            int sx1, int sy1, int sx2, int sy2,
                            Color bgcolor,
                            ImageObserver observer)
  {
    if (dx1 == dx2 || dy1 == dy2)
      return true;
    if (sx1 == sx2 || sy1 == sy2)
      return true;

    int widthImage;
    int heightImage;
    int xImage;
    int yImage;
    if (sx2 > sx1)
    {
      widthImage = sx2 - sx1 + 1;
      xImage = sx1;
    }
    else
    {
      widthImage = sx1 - sx2 + 1;
      xImage = sx2;
    }

    if (sy2 > sy1)
    {
      heightImage = sy2 - sy1 + 1;
      yImage = sy1;
    }
    else
    {
      heightImage = sy1 - sy2 + 1;
      yImage = sy2;
    }

    int widthDest;
    int heightDest;
    int xDest;
    int yDest;
    if (dx2 > dx1)
    {
      widthDest = dx2 - dx1 + 1;
      xDest = dx1;
    }
    else
    {
      widthDest = dx1 - dx2 + 1;
      xDest = dx2;
    }

    if (dy2 > dy1)
    {
      heightDest = dy2 - dy1 + 1;
      yDest = dy1;
    }
    else
    {
      heightDest = dy1 - dy2 + 1;
      yDest = dy2;
    }

    // Extract the image with a CropImageFilter
    Image imageArea = manager.createImage (new FilteredImageSource (img.getSource (),
                                                                    new CropImageFilter (xImage, yImage, widthImage, heightImage)));
    return drawImage (imageArea, xDest, yDest, widthDest, heightDest, bgcolor, observer);
  }

  /**
   * <code>java.awt.Graphics</code> implementation.
   * @see Graphics
   */
  public void dispose()
  {
  }
      
  // From java.awt.image.AreaAveragingScaleFilter
  // but without use of float type
  private class AreaAveragingScaleFilter extends ImageFilter 
  {
    private ColorModel rgbModel;    
    private long []    alphas;
    private long []    reds; 
    private long []    greens; 
    private long []    blues;
    
    protected int      srcWidth;
    protected int      srcHeight;
    private   int []   srcPixels;
    protected int      destWidth;
    protected int      destHeight;
    protected int []   destPixels;
    
    {
      // Test if the class java.awt.image.ColorModel can be loaded
      boolean classColorModelAccessible = PJAGraphicsManager.getDefaultGraphicsManager ().isClassAccessible ("java.awt.image.ColorModel");
      if (classColorModelAccessible)
        rgbModel = ColorModel.getRGBdefault ();
    }
    
    /**
     * Constructs an AreaAveragingScaleFilter that scales the pixels from
     * its source Image as specified by the width and height parameters.
     * @param width  the target width to scale the image
     * @param height the target height to scale the image
     */
    public AreaAveragingScaleFilter (int width, int height) 
    {
      destWidth = width;
      destHeight = height;
    }
    
    public void setDimensions (int w, int h) 
    {
      srcWidth = w;
      srcHeight = h;
      if (destWidth < 0) 
      {
        if (destHeight < 0) 
        {
          destWidth = srcWidth;
          destHeight = srcHeight;
        } 
        else 
          destWidth = srcWidth * destHeight / srcHeight;
      } 
      else if (destHeight < 0) 
        destHeight = srcHeight * destWidth / srcWidth;
        
      consumer.setDimensions (destWidth, destHeight);
    }
  
    public void setHints (int hints) 
    {
      // Images are sent entire frame by entire frame
      consumer.setHints (  (hints & (SINGLEPASS | SINGLEFRAME))
                         | TOPDOWNLEFTRIGHT);
    }
    
    public void imageComplete (int status)
    {
      if (   status == STATICIMAGEDONE
          || status == SINGLEFRAMEDONE)
        accumPixels (0, 0, srcWidth, srcHeight, rgbModel, srcPixels, 0, srcWidth);  
      consumer.imageComplete (status);
    }
    
    public void setPixels (int x, int y, int width, int height,
                           ColorModel model, byte pixels [], int offset, int scansize)
    {
      // Store pixels in srcPixels array
      if (srcPixels == null)
        srcPixels = new int [srcWidth * srcHeight];
      for (int row = 0, destRow = y * srcWidth;
           row < height;
           row++, destRow += srcWidth)
      {
        int rowOff = offset + row * scansize;
        for (int col = 0; col < width; col++)
          // v1.2 : Added & 0xFF to disable sign bit
          srcPixels [destRow + x + col] = model.getRGB (pixels [rowOff + col] & 0xFF);
      }
    }

    public void setPixels (int x, int y, int width, int height,
		           ColorModel model, int pixels[], int offset, int scansize)
    {
      // Store pixels in srcPixels array
      if (srcPixels == null)
        srcPixels = new int [srcWidth * srcHeight];
      for (int row = 0, destRow = y * srcWidth;
           row < height;
           row++, destRow += srcWidth)
      {
        int rowOff = offset + row * scansize;
        for (int col = 0; col < width; col++)
          // If model == null, consider it's the default RGB model
          srcPixels [destRow + x + col] = model == null
                                                     ? pixels [rowOff + col]
                                                     : model.getRGB (pixels [rowOff + col]);
      }
    }
    
    private int [] calcRow () 
    {
      long mult = (srcWidth * srcHeight) << 32;
      if (destPixels == null)     
        destPixels = new int [destWidth];
  
      for (int x = 0; x < destWidth; x++) 
      {
        int a = (int)roundDiv (alphas [x], mult);
        int r = (int)roundDiv (reds   [x], mult);
        int g = (int)roundDiv (greens [x], mult);
        int b = (int)roundDiv (blues  [x], mult);
        a = Math.max (Math.min (a, 255), 0);
        r = Math.max (Math.min (r, 255), 0);
        g = Math.max (Math.min (g, 255), 0);
        b = Math.max (Math.min (b, 255), 0);
        destPixels [x] = (a << 24 | r << 16 | g << 8 | b);
      }
      
      return destPixels;
    }
    
    private void accumPixels (int x, int y, int w, int h,
                              ColorModel model, int [] pixels, int off,  
                              int scansize) 
    {
      reds   = new long [destWidth];
      greens = new long [destWidth];
      blues  = new long [destWidth];
      alphas = new long [destWidth];
      
      int sy = y;
      int syrem = destHeight;
      int dy = 0;
      int dyrem = 0;      
      while (sy < y + h) 
      {
        if (dyrem == 0) 
        {
          for (int i = 0; i < destWidth; i++) 
            alphas [i] = 
            reds   [i] = 
            greens [i] = 
            blues  [i] = 0;
  
          dyrem = srcHeight;
        }
        
        int amty = Math.min (syrem, dyrem);
        int sx = 0;
        int dx = 0;
        int sxrem = 0;
        int dxrem = srcWidth;
        int a = 0, 
            r = 0, 
            g = 0, 
            b = 0;
        while (sx < w) 
        {
          if (sxrem == 0) 
          {
            sxrem = destWidth;
            int rgb = pixels [off + sx];
            a = rgb >>> 24;
            r = (rgb >> 16) & 0xFF;
            g = (rgb >>  8) & 0xFF;
            b = rgb & 0xFF;
          }
          
          int  amtx = Math.min (sxrem, dxrem);
          long mult = (amtx * amty) << 32;
          alphas [dx] += mult * a;
          reds   [dx] += mult * r;
          greens [dx] += mult * g;
          blues  [dx] += mult * b;
          
          if ((sxrem -= amtx) == 0) 
            sx++;
  
          if ((dxrem -= amtx) == 0) 
          {
            dx++;
            dxrem = srcWidth;
          }
        }
        
        if ((dyrem -= amty) == 0) 
        {
          int outpix [] = calcRow ();
          do 
          {
            consumer.setPixels (0, dy, destWidth, 1,
                                rgbModel, outpix, 0, destWidth);
            dy++;
          } 
          while ((syrem -= amty) >= amty && amty == srcHeight);
        } 
        else 
          syrem -= amty;
  
        if (syrem == 0) 
        {
          syrem = destHeight;
          sy++;
          off += scansize;
        }
      }
    }
  }
}