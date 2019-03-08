/*
 * @(#)PJAFontMetrics.java  05/16/2000
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
import java.io.Serializable;

/**
 * Pure Java AWT Font Metrics implementation. This class returns the metrics
 * of a given font (name + style + sizes).<BR>
 * From version 1.1, all the font data management moved to the new class <code>PJAFontData</code>
 * to avoid using <code>PJAFontMetrics</code> which extends this class in <code>PJAGraphics</code> code
 * (<code>java.awt.FontMetrics</code> requires awt library to be loaded to call <code>initIDs</code> native method).
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @since     PJA1.0
 */
public class PJAFontMetrics extends FontMetrics implements Serializable
{
  private PJAFontData fontData;

  protected PJAFontMetrics (Font        font,
                            PJAFontData fontData)
  {
    super (font);
    this.fontData = fontData;
  }

  /**
   * Sets the font for this metrics object. This protected method is used to
   * set the font afterwards a PJAFontMetrics object is created or deserialized.
   * @param        font  The matching font for this metrics.
   * @deprecated   As of PJA version 1.1, <code>PJAFontPeer</code> doesn't need it anymore.
   */
  protected void setFont (Font font)
  {
    this.font = font;
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the leading of this font.
   * @see PJAFontData
   */
  public int getLeading ()
  {
    return fontData.getLeading ();
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the ascent of this font.
   * @see PJAFontData
   */
  public int getAscent ()
  {
    return fontData.getAscent ();
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the descent of this font.
   * @see PJAFontData
   */
  public int getDescent ()
  {
    return fontData.getDescent ();
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the max ascent of this font.
   * @see PJAFontData
   */
  public int getMaxAscent ()
  {
    return fontData.getMaxAscent ();
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the max descent of this font.
   * @see PJAFontData
   */
  public int getMaxDescent ()
  {
    return fontData.getMaxDescent ();
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the max advance of this font.
   * @see PJAFontData
   */
  public int getMaxAdvance ()
  {
    return fontData.getMaxAdvance ();
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the char width of the character <code>ch</code> in this font.
   * @see PJAFontData
   */
  public int charWidth (int ch)
  {
    return fontData.getCharWidth (ch);
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the char width of the character <code>ch</code> in this font.
   * (need to override <code>charWidth (char ch)</code> of the class 
   * <code>java.awt.FontMetrics</code> to avoid stack overflow in <code>getWidths ()</code>)
   * @see PJAFontData
   * @since PJA1.2
   */
  public int charWidth (char ch)
  {
    return fontData.getCharWidth (ch);
  }

  /**
   * <code>java.awt.FontMetrics</code> implementation.
   * Returns the string width of a string in this font.
   * @see PJAFontData
   */
  public int charsWidth (char data [], int offset, int len)
  {
    return fontData.getCharsWidth (data, offset, len);
  }

  /**
   * Returns the pixels array of the character <code>ch</code>.
   * @see PJAFontData
   * @deprecated As of PJA version 1.1, replaced by PJAFontData.getCharPixels ().
   */
  public int [] getCharPixels (char ch)
  {
    return fontData.getCharPixels (ch);
  }

  /**
   * Returns the scansize of the pixels array of the character <code>ch</code>.
   * @see PJAFontData
   * @deprecated As of PJA version 1.1, replaced by PJAFontData.getCharPixelsWidth ().
   */
  public int getCharPixelsWidth (char ch)
  {
    return fontData.getCharPixelsWidth (ch);
  }

  /**
   * Returns the offset of the character <code>ch</code> to add at baseline.
   * @see PJAFontData
   * @deprecated As of PJA version 1.1, replaced by PJAFontData.getCharOffsetAtBaseline ().
   */
  public int getCharOffsetAtBaseline (char ch)
  {
    return fontData.getCharOffsetAtBaseline (ch);
  }

  // createFontMetrics () moved to PJAFontData constructor
  // static PJAFontMetrics createFontMetrics (Font font)
}