/*
 * @(#)PJAFontData.java  05/16/2000
 *
 * Copyright (c) 2000-2001 Emmanuel PUYBARET / eTeks <info@eteks.com>. All Rights Reserved.
 *
 * Faster access to font bits by Fernando Echeverria
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
import java.awt.image.PixelGrabber;
import java.io.*;

// See comment of this class about the use of the 3 next types
// All these classes are needed to capture the font

/**
 * Pure Java AWT Font data.
 * From version 1.1, this class keeps all the data of a font to avoid using <code>PJAFontMetrics</code> which extends
 * <code>java.awt.FontMetrics</code> (<code>java.awt.FontMetrics</code> requires awt library to be loaded).
 * This class implements <code>Serializable</code> ; it implements <code>readObject (ObjectInputStream)</code>
 * and <code>writeObject (ObjectOutputStream)</code> methods in such a way that they call the methods
 * <code>read (InputStream)</code> and <code>write (OutputStream)</code> respectively of this class. As The methods
 * <code>read ()</code> and <code>write ()</code> don't use serialization process, they can be use to restore and
 * save the state of an instance of <code>PJAFontData</code> in Java environments
 * where serialization isn't available (Java 1.0 and J2ME).
 *
 * @version   2.3
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAGraphicsManager
 * @see       com.eteks.awt.PJAFontPeer
 * @see       com.eteks.tools.fontcapture.PJAFontCapture
 * @since     PJA1.1
 */
public class PJAFontData implements Serializable
{
  public static final String PJAF1_1_FILE_HEADER = "PJAF1.1";
  public static final String PJAF1_1_FONT_HEADER = "PJAFontData_1.1";

  private String     name;     // Name of this font data
  private int        style;    // Style of this font data
  private int        size;     // Size of this font data

  private int        leading;
  private int        ascent;
  private int        descent;
  private int        maxAscent;
  private int        maxDescent;
  private int        maxAdvance;

  private int        minChar;
  private int        maxChar;

  private short []   charsWidth;
  private int   []   charsOffset;
  private short []   charsOffsetAtBaseline;
  private int        allCharsWidth;
  private FastBitSet charsBits;

  /** 
   * A bitset implementation that is much faster than <code>java.util.BitSet</code>. 
   * See <code>java.util.BitSet</code> class for detailed information about methods.
   * @author Fernando Echeverria
   * @since  PJA2.3
   */
  private static class FastBitSet
  {    
    private int    size;  // Number of bits in set    
    private int [] data;  // Array of ints containing the bits, 32 at a time  

    public FastBitSet (int size)
    {
      this.size = size;
      int intsCount = (size + 31) / (1 << 5);
      data = new int [intsCount];
    }

    public int size () 
    { 
      return size; 
    }

    public void set (int bitIndex)
    {
      data [bitIndex >> 5] |= 1 << (bitIndex & 0x1F);
    }

    public void clear (int bitIndex)
    {
      data [bitIndex >> 5] &= ~(1 << (bitIndex & 0x1F));
    }

    public boolean get (int bitIndex)
    {
      return (data [bitIndex >> 5] & (1 << (bitIndex & 0x1F))) != 0;
    }

    /** 
     * Allows access to the raw bits as an int array, for even faster access.
     * The value of bit at index bitIndex will be : <code>(rawBits [bitIndex >> 5] & (1 << (bitIndex & 0x1F)) != 0</code>
     */
    public int [] getRawBits () 
    { 
      return data; 
    }
  }

  /**
   * Creates a font data from the <code>in</code> stream
   * with <code>read ()</code> method.
   * @param in  An opened input stream.
   * @see #read
   * @exception EOFException End of <code>in</code> is reached.
   * @exception IOException  The object fields couldn't be read correctly from <code>in</code>.
   */
  public PJAFontData (InputStream in) throws IOException
  {
    read (in);
  }

  /**
   * Creates a font data from <code>font</code>. The pixels of all the characters between
   * <code>minChar</code> and <code>maxChar</code> are grabbed and stored in internal fields.
   *
   * @param font     A valid font object.
   * @param minChar  The first character to be grabbed.
   * @param maxChar  The last character to be grabbed.
   */
  public PJAFontData (Font font, int minChar, int maxChar)
  {
    try
    {
      this.name  = font.getName ();
      this.style = font.getStyle ();
      this.size  = font.getSize ();

      // PJAFontMetrics is initialized without font
      // to be serialized with no font object associated
      FontMetrics metrics = Toolkit.getDefaultToolkit ().getFontMetrics (font);

      this.minChar = minChar;
      this.maxChar = maxChar;
      StringBuffer allChars = new StringBuffer ();
      for (int i = this.minChar; i <= this.maxChar; i++)
        allChars.append ((char)i);

      Frame dummyFrame = new Frame ();
      dummyFrame.addNotify ();

      // Retrieve metrics of this font
      this.leading    = metrics.getLeading ();
      this.ascent     = metrics.getAscent ();
      this.descent    = metrics.getDescent ();
      this.maxAscent  = metrics.getMaxAscent ();
      this.maxDescent = metrics.getMaxDescent ();
      this.maxAdvance = metrics.getMaxAdvance ();

      this.charsWidth  = new short [this.maxChar - this.minChar + 1];
      this.charsOffset = new int [this.charsWidth.length];
      this.allCharsWidth = 0;

      int height  = this.ascent + this.descent;
      for (int i = 0; i < this.charsWidth.length; i++)
        this.charsWidth  [i] = (short)metrics.charWidth (allChars.charAt (i));

      // The capture takes into account the real width at screen which is
      // not equal to charWidth (ch)
      this.charsOffsetAtBaseline = new short [this.charsWidth.length];
      int      tempMaskWidth = this.maxAdvance * 3;
      Image    image = dummyFrame.createImage (tempMaskWidth, height);
      int      pixels [] = new int [tempMaskWidth * height];
      Graphics gc = image.getGraphics ();
      gc.setFont (font);
      for (int i = 0; i < this.charsWidth.length; i++)
      {
        gc.setColor (Color.white);
        gc.fillRect (0, 0, tempMaskWidth, height);
        gc.setColor (Color.black);
        // Drawing is done at maxAdvance in case the font uses italic style
        // which may create a negative overflow
        gc.drawString (String.valueOf (allChars.charAt (i)), this.maxAdvance, this.ascent);

        // Capture the pixels of the character
        PixelGrabber grabber = new PixelGrabber (image, 0, 0, tempMaskWidth, height,
                                                 pixels, 0, tempMaskWidth);
        grabber.grabPixels ();

        // Seek the first left pixel which is set
        int left = tempMaskWidth;
        for (int y = 0, deltaY = 0; y < height; y++, deltaY += tempMaskWidth)
          for (int x = 0; x < tempMaskWidth; x++)
            if ((pixels [x + deltaY] & 0x00FFFFFF) < 0x777777)
            {
              left = Math.min (left, x);
              break;
            }
        // Seek the last right pixel which is set
        int right = -1;
        if (left != tempMaskWidth)
          for (int y = 0, deltaY = 0; y < height; y++, deltaY += tempMaskWidth)
            for (int x = tempMaskWidth - 1; x >= 0; x--)
              if ((pixels [x + deltaY] & 0x00FFFFFF) < 0x777777)
              {
                right = Math.max (right, x);
                break;
              }

        this.charsOffset [i] = this.allCharsWidth;
        if (left != tempMaskWidth)
        {
          this.charsOffsetAtBaseline [i] = (short)(left - this.maxAdvance);
          this.allCharsWidth += right - left + 1;
        }
      }

      // Draw allChars in an image to retrieve all the pixels of each char
      image = dummyFrame.createImage (this.allCharsWidth, height);
      gc = image.getGraphics ();
      gc.setColor (Color.white);
      gc.fillRect (0, 0, this.allCharsWidth, height);
      gc.setColor (Color.black);
      gc.setFont (font);
      for (int i = 0; i < this.charsWidth.length; i++)
        gc.drawString (String.valueOf (allChars.charAt (i)),
                       this.charsOffset [i] - this.charsOffsetAtBaseline [i],
                       this.ascent);

      // Capture the pixels of the image
      pixels = new int [this.allCharsWidth * height];
      PixelGrabber grabber = new PixelGrabber (image, 0, 0, this.allCharsWidth, height,
                                               pixels, 0, this.allCharsWidth);
      grabber.grabPixels ();

      // Keep the font pixels in a BitSet to minimize font storage
      // v2.3
      // Fernando Echeverria : use FastBitSet instead of java.util.BitSet
      this.charsBits = new FastBitSet (pixels.length);
      for (int i = 0; i < pixels.length; i++)
        if ((pixels [i] & 0x00FFFFFF) < 0x777777)
          this.charsBits.set (i);
    }
    catch (InterruptedException e) // Exception raised by grabPixels ()
    {
      throw new RuntimeException ("Couldn't capture font " + font);
    }
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

  /**
   * Returns the size of this font.
   */
  public int getSize ()
  {
    return size;
  }

  /**
   * Returns the leading of this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getFontLeading
   */
  public int getLeading ()
  {
    return leading;
  }

  /**
   * Returns the ascent of this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getFontAscent
   */
  public int getAscent ()
  {
    return ascent;
  }

  /**
   * Returns the descent of this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getFontDescent
   */
  public int getDescent ()
  {
    return descent;
  }

  /**
   * Returns the max ascent of this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getFontMaxAscent
   */
  public int getMaxAscent ()
  {
    return maxAscent;
  }

  /**
   * Returns the max descent of this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getFontMaxDescent
   */
  public int getMaxDescent ()
  {
    return maxDescent;
  }

  /**
   * Returns the max advance of this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getFontMaxAdvance
   */
  public int getMaxAdvance ()
  {
    return maxAdvance;
  }

  /**
   * Returns the char width of the character <code>ch</code> in this font.
   * @param ch The character to measure.
   * @see com.eteks.awt.PJAGraphicsExtension#getCharWidth
   */
  public int getCharWidth (int ch)
  {
    // v1.2 : check the limits
    if (ch < minChar || ch > maxChar)
      return 0;
    return charsWidth [ch - minChar];
  }

  /**
   * Returns the string width of a string in this font.
   * @see com.eteks.awt.PJAGraphicsExtension#getStringWidth
   */
  public int getCharsWidth (char data [], int offset, int len)
  {
    int width = 0;
    for (int i = 0; i < len; i++)
      width += getCharWidth ((int)data [offset + i]);
    return width;
  }

  private void checkCharacterRange (char ch)
  {
    if (ch < minChar || ch > maxChar)
      throw new IllegalArgumentException ("Character " + ch + " as no image.");
  }

  /**
   * Returns the pixels array of the character <code>ch</code>.
   * @see com.eteks.awt.PJAGraphics#drawString
   * @see #getCharPixelsWidth
   */
  protected int [] getCharPixels (char ch)
  {
    int height = ascent + descent;
    int width  = getCharPixelsWidth (ch);
    int pixels [] = new int [width * height];
    // Gets the pixels of the char ch in an int array
    for (int k = 0, y = 0, deltaY = 0; y < height; y++, deltaY += allCharsWidth)
      for (int x = 0; x < width; x++)
        pixels [k++] = charsBits.get (x + charsOffset [ch - minChar] + deltaY)
                                             ? 0xFF000000
                                             : 0xFFFFFFFF;
    return pixels;
  }

  /**
   * Provides direct access to bitset array for this font's pixels.
   * @return array of ints containing a linear sequence of bits, which represent
   *         the pixels all the characters in this font displayed side by side and
   *         scanned horizontally left-to-right and then top-to-bottom
   * @since PJA2.3
   */
  protected int [] getCharBits()
  {
    return charsBits.getRawBits();
  }

  /**
   * Retrieves the offset for the bit representing top-left pixel in the
   * given char, within the bitset.
   * @since PJA2.3
   */
  protected int getCharBitsOffset (char ch)
  {
    return charsOffset [ch - minChar];
  }

  /** 
   * Retrieves the scansize of the linear bit array, i.e., the number of bits
   * in each very long scanline of pixels stored sequentially in the bitset 
   * @since PJA2.3
   */
  protected int getCharBitsScanSize()
  {
    return allCharsWidth;
  }

  /**
   * Returns the scansize of the pixels array of the character <code>ch</code>.
   * @see com.eteks.awt.PJAGraphics#drawString
   * @see #getCharPixels
   */
  protected int getCharPixelsWidth (char ch)
  {
    checkCharacterRange (ch);
    if (ch == maxChar)
      return allCharsWidth - charsOffset [maxChar - 1];
    else
      return charsOffset [ch - minChar + 1] - charsOffset [ch - minChar];
  }

  /**
   * Returns the offset of the character <code>ch</code> to add at baseline.
   * When font is italic, some characters have some pixels at the left of the x
   * coordinate of their drawing point.
   * @see com.eteks.awt.PJAGraphics#drawString
   * @see #getCharPixels
   */
  protected int getCharOffsetAtBaseline (char ch)
  {
    checkCharacterRange (ch);
    return charsOffsetAtBaseline [ch - minChar];
  }

  // To ensure a correct serialization of fontData
  private void writeObject (ObjectOutputStream out) throws IOException
  {
    write (out);
  }

  /**
   * Writes the fields of this object to <code>out</code>. It does the same
   * as serialization.
   */
  public void write (OutputStream out) throws IOException
  {
    DataOutputStream dataOut = out instanceof DataOutputStream
                                 ? (DataOutputStream)out
                                 : new DataOutputStream (out);
    // Write the version of this class
    dataOut.writeUTF (PJAF1_1_FONT_HEADER);
    // Write all the fields of this class
    dataOut.writeUTF (name);
    dataOut.writeInt (style);
    dataOut.writeInt (size);
    dataOut.writeInt (leading);
    dataOut.writeInt (ascent);
    dataOut.writeInt (descent);
    dataOut.writeInt (maxAscent);
    dataOut.writeInt (maxDescent);
    dataOut.writeInt (maxAdvance);
    dataOut.writeInt (minChar);
    dataOut.writeInt (maxChar);
    // Write the content of charsWidth table
    dataOut.writeInt (charsWidth.length);
    for (int i = 0; i < charsWidth.length; i++)
      dataOut.writeShort (charsWidth [i]);
    // Write the content of charsOffset table
    dataOut.writeInt (charsOffset.length);
    for (int i = 0; i < charsOffset.length; i++)
      dataOut.writeInt (charsOffset [i]);
    // Write the content of charsOffsetAtBaseline table
    dataOut.writeInt (charsOffsetAtBaseline.length);
    for (int i = 0; i < charsOffsetAtBaseline.length; i++)
      dataOut.writeShort (charsOffsetAtBaseline [i]);

    dataOut.writeInt (allCharsWidth);
    // Write FastBitSet object
    int charsBitsSize = charsBits.size ();
    dataOut.writeInt (charsBitsSize);
    for (int i = 0; i < charsBitsSize; )
    {
      long someBits = 0L;
      for (int j = 0; j < 64 && i < charsBitsSize; j++, i++)
        if (charsBits.get (i))
          someBits |= 1L << j;
      dataOut.writeLong (someBits);
    }
    dataOut.flush ();
  }

  // To ensure a correct deserialization of fontData
  private void readObject (ObjectInputStream in) throws IOException, ClassNotFoundException
  {
    read (in);
  }

  /**
   * Reads the fields of this object from <code>in</code>. It does the same
   * as serialization.
   */
  public void read (InputStream in) throws IOException
  {
    DataInputStream dataIn = (in instanceof DataInputStream)
                               ? (DataInputStream)in
                               : new DataInputStream (in);
    // Read the version of this class
    try
    {
      String version = dataIn.readUTF ();
      if (!PJAF1_1_FONT_HEADER.equals (version))
        throw new IOException ("Unknown PJAF data format");
    }
    catch (EOFException e)
    {
      throw e; // No problem if EOF at beginning of data
    }

    try
    {
      // Read all the fields of this class
      name  = dataIn.readUTF ();
      style = dataIn.readInt ();
      size  = dataIn.readInt ();
      leading    = dataIn.readInt ();
      ascent     = dataIn.readInt ();
      descent    = dataIn.readInt ();
      maxAscent  = dataIn.readInt ();
      maxDescent = dataIn.readInt ();
      maxAdvance = dataIn.readInt ();
      minChar    = dataIn.readInt ();
      maxChar    = dataIn.readInt ();
      // Write the content of charsWidth table
      charsWidth = new short [dataIn.readInt ()];
      for (int i = 0; i < charsWidth.length; i++)
        charsWidth [i] = dataIn.readShort ();
      // Write the content of charsOffset table
      charsOffset = new int [dataIn.readInt ()];
      for (int i = 0; i < charsOffset.length; i++)
        charsOffset [i] = dataIn.readInt ();
      // Write the content of charsOffsetAtBaseline table
      charsOffsetAtBaseline = new short [dataIn.readInt ()];
      for (int i = 0; i < charsOffsetAtBaseline.length; i++)
        charsOffsetAtBaseline [i] = dataIn.readShort ();

      allCharsWidth = dataIn.readInt ();
      // Read FastBitSet object
      int charsBitsSize = dataIn.readInt ();
      // v2.3
      // Fernando Echeverria : use FastBitSet instead of java.util.BitSet
      charsBits = new FastBitSet (charsBitsSize);
      for (int i = 0; i < charsBitsSize; )
      {
        long someBits = dataIn.readLong ();
        for (int j = 0; j < 64 && i < charsBitsSize; j++, i++)
          if (((someBits >>> j) & 0x1L) != 0)
            charsBits.set (i);
      }
    }
    catch (EOFException e)
    {
      // EOF mustn't happen while reading a font
      throw new IOException ("Invalid PJAF data format");
    }
  }

  /**
   * Returns <code>true</code> if this font peer and <code>object</code> have the
   * same name, style and size.
   */
  public boolean equals (Object object)
  {
    return    object instanceof PJAFontData
           && name.equals (((PJAFontData)object).name)
           && style == ((PJAFontData)object).style
           && size  == ((PJAFontData)object).size;
  }

  public int hashCode ()
  {
    return name.hashCode () + (style << 16) + size;
  }

  public static String styleToString (int style)
  {
    if ((style & Font.BOLD) != 0)
      return (style & Font.ITALIC) != 0 ? "bolditalic" : "bold";
    else
      return (style & Font.ITALIC) != 0  ? "italic" : "plain";
  }

  public String toString ()
  {
    return getClass ().getName () + "[fontName=" + getName () + ", style=" + styleToString (getStyle ()) + ", size=" + getSize () + "]";
  }
}
