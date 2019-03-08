/*
 * @(#)PJAFontPeer.java  05/16/2000
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
import java.awt.peer.FontPeer;
import java.io.Serializable;

/**
 * Pure Java AWT Font Peer.
 * From version 1.1, <code>com.PJAFontCapture</code> is used to capture the fonts.
 * PJA 1.0 .pjaf font files are not compatible with PJA 1.1, please use font capture utility to produce
 * PJA 1.1 font files.
 * This class keeps all the data of a font in <code>PJAFontData</code> instances
 * to avoid using <code>PJAFontMetrics</code> which extends <code>java.awt.FontMetrics</code>
 * (<code>java.awt.FontMetrics</code> requires awt library to be loaded).
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAGraphicsManager
 * @see       com.eteks.tools.fontcapture.PJAFontCapture
 * @since     PJA1.0
 */
public class PJAFontPeer implements FontPeer, Serializable
{
  private String name;     // Name of this font
  private int    style;    // Style of this font

  private int    minSize;  // Minimum size available with this font
  private int    maxSize;  // Maximum size available with this font

  private PJAFontData [] originalFontData;
  private PJAFontData [] fontData;

  protected PJAFontPeer (String name, int style)
  {
    this.name  = name;
    this.style = style;
  }

  /**
   * Returns the name of this font.
   */
  public String getName ()
  {
    return name;
  }

  /**
   * Returns the style of this font.
   */
  public int getStyle ()
  {
    return style;
  }

  protected synchronized void addFontData (PJAFontData newFontData)
  {
    if (   !newFontData.getName ().equals (name)
        || newFontData.getStyle () != style)
      throw new IllegalArgumentException ("fontData and fontPeer name and style don't match");

    if (originalFontData == null)
    {
      originalFontData = new PJAFontData [1];
      originalFontData [0] = newFontData;
      minSize =
      maxSize = newFontData.getSize ();
    }
    else
    {
      if (newFontData.getSize () < minSize)
      {
        // Enlarge start of originalFontData []
        PJAFontData [] originalFontData2 = new PJAFontData [maxSize - newFontData.getSize () + 1];
        System.arraycopy (originalFontData, 0, originalFontData2, minSize - newFontData.getSize (), originalFontData.length);
        originalFontData = originalFontData2;

        originalFontData [0] = newFontData;
        minSize = newFontData.getSize ();
      }
      else if (newFontData.getSize () > maxSize)
      {
        // Enlarge end of originalFontData []
        PJAFontData [] originalFontData2 = new PJAFontData [newFontData.getSize () - minSize + 1];
        System.arraycopy (originalFontData, 0, originalFontData2, 0, originalFontData.length);
        originalFontData = originalFontData2;

        originalFontData [originalFontData.length - 1] = newFontData;
        maxSize = newFontData.getSize ();
      }
      else
        originalFontData [newFontData.getSize () - minSize] = newFontData;
    }

    // If all sizes aren't available, complete the null fonts
    // in fontData array to the nearest available one
    fontData = new PJAFontData [originalFontData.length];
    System.arraycopy (originalFontData, 0, fontData, 0, originalFontData.length);
    for (int i = 0; i < fontData.length - 1; )
    {
      // Find the next available font
      int j = i + 1;
      while (   j < fontData.length
             && fontData [j] == null)
        j++;

      if (i < j - 1)
      {
        int k = i + 1;
        while (k < (i + j + 1) / 2)
          fontData [k++] = fontData [i];
        while (k < j)
        fontData [k++] = fontData [j];
      }
      i = j;
    }
  }

  protected synchronized PJAFontData getFontData (int fontSize)
  {
    if (fontData == null)
      throw new NullPointerException ("fontPeer doesn't have any data");

    if (fontSize < minSize)
      fontSize = minSize;
    else if (fontSize > maxSize)
      fontSize = maxSize;

    return fontData [fontSize - minSize];
  }

  protected FontMetrics getFontMetrics (Font font)
  {
    return new PJAFontMetrics (font, getFontData (font.getSize ()));
  }

  protected FontMetrics getFontMetrics (int fontSize)
  {
    boolean canInstantiateFont = PJAGraphicsManager.getDefaultGraphicsManager ().isFontInstantiable ();
    return new PJAFontMetrics (canInstantiateFont ? new Font (name, style, fontSize) : null,
                               getFontData (fontSize));
  }

  /**
   * Returns <code>true</code> if this font peer and <code>object</code> have the
   * same name and style.
   */
  public boolean equals (Object object)
  {
    return    object instanceof PJAFontPeer
           && name.equals (((PJAFontPeer)object).name)
           && style == ((PJAFontPeer)object).style;
  }

  public int hashCode ()
  {
    return name.hashCode () + (style << 16);
  }

  public String toString ()
  {
    return getClass ().getName () + "[fontName=" + getName () + ", style=" + PJAFontData.styleToString (getStyle ()) + "]";
  }

  /**
   * @deprecated   As of PJA version 1.1, <code>PJAFontPeer</code> <code>main ()</code> method moved
   *               to <code>com.PJAFontCapture</code>.
   * @see com.eteks.tools.fontcapture.PJAFontCapture
   */
  public static void main (String args [])
  {
    System.out.println ("This method is deprecated. Run com.PJAFontCapture class");
  }
}
