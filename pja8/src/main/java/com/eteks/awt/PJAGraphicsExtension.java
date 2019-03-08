/*
 * @(#)PJAGraphics.java  05/16/2000
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

/**
 * Pure Java AWT Graphics extension. This interface lists extension methods implemented
 * by Graphics classes. This methods may be usefull for graphics operation when security
 * manager is restrictive.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       PJAGraphicsManager
 * @see       PJAGraphics
 * @see       java.awt.Graphics
 * @since     PJA1.1
 */
public interface PJAGraphicsExtension
{
  /**
   * Sets the graphics font to the specified font specified by its
   * name, style and size. It has the same effect as <code>setFont (Font)</code> of
   * <code>Graphics</code> class, but doesn't need to instantiate a <code>Font</code> object.
   *
   * @param  fontName   the font name.
   * @param  fontStyle  the font style.
   * @param  fontSize   the font size.
   * @see     java.awt.Graphics#setFont(Font)
   * @since   PJA1.1
   */
  public void setFont(String fontName, int fontStyle, int fontSize);

  /**
   * Returns the graphics current font name. It has the same effect as <code>getFont ().getName ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>Font</code> object.
   *
   * @return  the font name.
   * @see     java.awt.Graphics#getFont()
   * @since   PJA1.1
   */
  public String getFontName();

  /**
   * Returns the graphics current font style. It has the same effect as <code>getFont ().getStyle ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>Font</code> object.
   *
   * @return  the font style.
   * @see     java.awt.Graphics#getFont()
   * @since   PJA1.1
   */
  public int getFontStyle();

  /**
   * Returns the graphics current font size. It has the same effect as <code>getFont ().getSize ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>Font</code> object.
   *
   * @return  the font size.
   * @see     java.awt.Graphics#getFont()
   * @since   PJA1.1
   */
  public int getFontSize();

  /**
   * Sets the graphics context's color. It has the same effect as
   * <color>setColor (Color)</color> of <code>Graphics</code> class,
   * but doesn't need to instantiate a <Color>Color</code> object.
   *
   * @param  red   the red component.
   * @param  green the green component.
   * @param  blue  the blue component.
   * @see    PJAGraphicsExtension
   * @since  PJA1.1
   */
  public void setColor(int red, int green, int blue);

  /**
   * Returns the graphics current color. It has the same effect as <code>getColor ().getRGB ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>Color</code> object.
   *
   * @return  the color RGB.
   * @see     java.awt.Graphics#getFont()
   * @since   PJA1.1
   */
  public int getColorRGB();

  /**
   * Sets this graphics context's XOR color. It has the same effect as
   * <color>setXORMode (Color)</color> of <code>Graphics</code> class,
   * but doesn't need to instantiate a <Color>Color</code> object.
   *
   * @param  red   the red component.
   * @param  green the green component.
   * @param  blue  the blue component.
   * @see    PJAGraphicsExtension
   * @since  PJA1.1
   */
  public void setXORMode(int red, int green, int blue);

  /**
   * Determines the standard leading of the current font.
   * The standard leading (interline spacing) is the
   * logical amount of space to be reserved between the descent of one
   * line of text and the ascent of the next line. The height metric is
   * calculated to include this extra space.
   * It has the same effect as <code>getFontMetrics ().getLeading ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @return  the standard leading of the current font.
   * @see     java.awt.FontMetrics#getLeading
   * @since   PJA1.1
   */
  public int getFontLeading();

  /**
   * Determines the font ascent of the current font.
   * The font ascent is the distance from the font's
   * baseline to the top of most alphanumeric characters. Some
   * characters in the font may extend above the font ascent line.
   * It has the same effect as <code>getFontMetrics ().getAscent ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @return   the font ascent of the font.
   * @see      java.awt.FontMetrics#getAscent
   * @since    PJA1.1
   */
  public int getFontAscent();

  /**
   * Determines the font descent of the current font.
   * The font descent is the distance from the font's
   * baseline to the bottom of most alphanumeric characters with
   * descenders. Some characters in the font may extend below the font
   * descent line.
   * It has the same effect as <code>getFontMetrics ().getDescent ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @return   the font descent of the font.
   * @see      java.awt.FontMetrics#getDescent
   * @since    PJA1.1
   */
  public int getFontDescent();

  /**
   * Determines the maximum ascent of the current font.
   * No character extends further above the font's baseline
   * than this height.
   * It has the same effect as <code>getFontMetrics ().getMaxAscent ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @return  the maximum ascent of any character in the font.
   * @see     java.awt.FontMetrics#getMaxAscent
   * @since   PJA1.1
   */
  public int getFontMaxAscent();

  /**
   * Determines the maximum descent of the current font.
   * No character extends further below the font's baseline
   * than this height.
   * It has the same effect as <code>getFontMetrics ().getMaxDescent ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @return  the maximum descent of any character in the font.
   * @see     java.awt.FontMetrics#getMaxDescent
   * @since   PJA1.1
   */
  public int getFontMaxDescent();

  /**
   * Gets the maximum advance width of any character of the current Font.
   * The advance width is the amount by which the current point is
   * moved from one character to the next in a line of text.
   * It has the same effect as <code>getFontMetrics ().getMaxAdvance ()</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @return  the maximum advance width of any character
   *          in the font.
   * @see     java.awt.FontMetrics#getMaxAdvance
   * @since   PJA1.1
   */
  public int getFontMaxAdvance();

  /**
   * Returns the advance width of the specified character of the current font.
   * The advance width is the amount by which the current point is
   * moved from one character to the next in a line of text.
   * It has the same effect as <code>getFontMetrics ().charWidth (ch)</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @param ch the character to be measured
   * @return  the advance width of the specified <code>char</code>
   *         in the font described by this font metric.
   * @see     java.awt.FontMetrics#charWidth
   * @since   PJA1.1
   */
  public int getCharWidth(char ch);

  /**
   * Returns the total advance width for showing the specified String
   * of the current Font.
   * The advance width is the amount by which the current point is
   * moved from one character to the next in a line of text.
   * It has the same effect as <code>getFontMetrics ().stringWidth (str)</code> call with a
   * <code>Graphics</code> instance, but doesn't need to instantiate a <code>FontMetrics</code> object.
   * @param str the String to be measured
   * @return  the advance width of the specified string
   *          in the font described by this font metric.
   * @see     java.awt.FontMetrics#stringWidth
   * @since   PJA1.1
   */
  public int getStringWidth(String str);
}