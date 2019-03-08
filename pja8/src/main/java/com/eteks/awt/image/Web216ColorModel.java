/*
 * @ (#)Web216ColorModel.java   02/21/2001
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
package com.eteks.awt.image;

import java.awt.image.IndexColorModel;

/**
 * An example of IndexColorModel which uses a fixed palette consisting
 * of the standard "safe 216 Web colors" + a tranparent color at index 216.
 * Each Red, Green, Blue component of the 216 colors palette take a value
 * among the 6 values of the following set : (0x00, 0x33, 0x66, 0x99, OxCC, OxFF).
 * 6 power 3 makes 216 total possible combinations.
 *
 * @version 2.3
 * @author  Fernando Echeverria
 * @author  Emmanuel PUYBARET
 * @since   PJA2.3
 */
public class Web216ColorModel extends IndexColorModel
{
  private static int     NCOLORS = 217;
  private static int     TRANSPARENT = NCOLORS - 1;
  private static byte [] PAL_RED     = new byte [NCOLORS];
  private static byte [] PAL_GREEN   = new byte [NCOLORS];
  private static byte [] PAL_BLUE    = new byte [NCOLORS];

  static  
  {
    // The 6 quantized color component levels used in this palette.
    final int [] LEVELS = {0x00, 0x33, 0x66, 0x99, 0xCC, 0xFF};
  
    // Make palette

    // Palette would be (RGB triplets):
    // 00,00,00 00,00,33 00,00,66 ... 00,00,ff
    // 00,33,00 00,33,33 00,33,66 ... 00,33,ff
    // ....
    // 33,00,00 ...
    // ...

    // So, for a given triplet rgb of RGB levels (0-5 each), the palette index
    // would be:
    //
    //       r*36 + g*6 + b
    //
    // And transparent would be index = 216
    int k = 0;
    for (int r = 0; r < 6; ++r)
      for (int g = 0; g < 6; ++g)
        for (int b = 0; b < 6; ++b)
        {
          PAL_RED   [k] = (byte)LEVELS [r];
          PAL_GREEN [k] = (byte)LEVELS [g];
          PAL_BLUE  [k] = (byte)LEVELS [b];
          ++k;
        }
  }

  /** 
   * Creates a color model with fixed Web216 palette.
   */
  public Web216ColorModel ()
  {
    super (8, NCOLORS, PAL_RED, PAL_GREEN, PAL_BLUE, TRANSPARENT);
  }
}
