/*
 * @(#)PJAFontCapture.java  05/23/2000
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
package com.eteks.tools.fontcapture;

import com.eteks.awt.PJAFontData;
import com.eteks.awt.PJAGraphicsManager;
import com.eteks.tools.awt.GridBagConstraints2;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.Vector;

// Java 2 classes

/**
 * Pure Java AWT Font capture utility.
 * The <code>main ()</code> method of this class is used to create font files used
 * by PJA toolkit. The syntax is either :
 * <pre>java com.PJAFontCapture</pre>
 * <P>Or for a command line usage :
 * <pre>java com.PJAFontCapture fontName [-p] [-b] [-i] [-bi] size1 size2 ... sizen filedest.pjaf</pre>
 * <UL><LI><code>fontName</code> is the font you want to capture (Helvetica, Times,...)</LI>
 * <LI><code>-p</code>, <code>-b</code>, <code>-i</code> and <code>-bi</code> are styles that you want to apply
 * to the font (plain, bold, italic, bold italic). You may choose one or more of these options.
 * If no style option is present, default is set to plain. When PJA toolkit is used, missing styles are
 * matched to an other existing one. That means if you captured an italic style font without bold italic style,
 * italic style will be used instead of bold italic if required.</LI>
 * <LI><code>size1 ... sizen</code> are the sizes that you want to apply (7 10 24 or whatever). Remember that
 * big fonts take more memory. When this toolkit is used, all missing sizes are matched to the closest one.
 * That means if you captured 8 10 12 16 font sizes, 8 size will be used for any query to a font smaller than 8,
 * 12 font will be used instead of a 14 font, and 16 size will be used for any query to a font bigger than 16.</LI>
 * <LI><code>filedest.pjaf</code> is the file name where the font capture will be saved. Extension must be
 * <code>.pjaf</code> (Pure Java AWT Font) and directory the user directory, to ensure automatic detection
 * of font files by <code>PJAToolkit</code> class. When used with <code>PJAToolkit</code>, the font names
 * are taken from the logical font name recorded in the file and not from the file name.</LI></UL>
 * <P>PJAF 1.0 .pjaf font files are not compatible with PJA 1.1, please use font capture utility to produce
 * PJAF 1.1 font files.
 * <P>For better results, don't forget to disable the font antialising tool on your system. To obtain smaller files,
 * and faster strings drawing, this toolkit use fonts in a one layer bitmap.
 * <P>This program can generate font files compatible with PJA and Java 1.0, but can't be run in a Java 1.0
 * environment.
 *
 * @version   1.1
 * @author    Emmanuel Puybaret
 * @see       PJAFontData
 * @since     PJA1.0
 */
public class PJAFontCapture
{
  public static void main (String args [])
  {
    // Need of a native toolkit to capture font
    if (args.length == 0)
      showCaptureFontDialog ("Exit");
    else if (args.length < 3)
      System.out.println ("Usage : java com.PJAFontCapture fontName [-p] [-b] [-i] [-bi] size1 size2 ... sizen filedest.pjaf");
    else
    {
      // Get font name and sizes
      String fontName = args [0];

      boolean plain = false;
      boolean bold  = false;
      boolean italic = false;
      boolean bolditalic = false;

      // Get font styles
      int i = 1;
      for ( ; i < args.length; i++)
        if (args [i].equals ("-p"))
          plain = true;
        else if (args [i].equals ("-b"))
          bold = true;
        else if (args [i].equals ("-i"))
          italic = true;
        else if (args [i].equals ("-bi"))
          bolditalic = true;
        else
          break;

      // Ensure at least plain font
      if (!plain)
        plain = !(bold || italic || bolditalic);

      // Get font sizes
      int fontSizes [] = new int [args.length - i - 1];
      for (int j = 0; j < fontSizes.length; i++, j++)
        fontSizes [j] = Integer.parseInt (args [i]);

      // Get font file
      String file = args [args.length - 1];

      System.out.println ("Pure Java AWT Font capture utility. \u00a9 Copyrights 2000-2001 eTeks.");
      captureFont (fontName, fontSizes, plain, bold, italic, bolditalic, file,
                   new CaptureFontListener ()
                     {
                       public void captureNewFont (Font font)
                       {
                         System.out.println ("Computing font " + font);
                       }

                       public void captureEnded ()
                       {
                         System.out.println ("Capture font ended");
                       }

                       public void error (Exception e, String message)
                       {
                         System.out.println (message);
                       }
                     });
    }

    System.exit (0);
  }

  /**
   * A simple dialog box to choose fonts to capture with a GUI.
   */
  public static void showCaptureFontDialog (String exitButtonText)
  {
    // First display a dialog box for anti-aliasing warning and copyrights
    final Frame frame = new Frame ();
    final Dialog  firstDialog = new Dialog (frame, "PJA Font capture", true);
    Button  okButton    = new Button (" Ok ");
    okButton.addActionListener (new ActionListener ()
      {
        public void actionPerformed (ActionEvent event)
        {
          firstDialog.dispose ();
        }
      });
    firstDialog.setLayout (new GridBagLayout ());
    firstDialog.add (new Label ("Pure Java AWT Font capture utility."), new GridBagConstraints2 (0, 0, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets (5, 5, 0, 5), 0, 0));
    firstDialog.add (new Label ("\u00a9 Copyright 2000-2001 eTeks <info@eteks.com>."), new GridBagConstraints2 (0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets (0, 5, 20, 5), 0, 0));
    firstDialog.add (new Label ("For better results, don't forget to disable"), new GridBagConstraints2 (0, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets (5, 10, 0, 10), 0, 0));
    firstDialog.add (new Label ("font anti-aliasing in your system configuration."), new GridBagConstraints2 (0, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets (0, 10, 0, 10), 0, 0));
    firstDialog.add (okButton, new GridBagConstraints2 (0, 4, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets (20, 0, 10, 0), 0, 0));
    firstDialog.setResizable (false);
    firstDialog.pack ();
    firstDialog.show ();

    // Build PJA Font dialog box
    // Font list
    final List fontList = new List ();
    String [ ] toolkitFontList = null;
    try
    {
      toolkitFontList = GraphicsEnvironment.getLocalGraphicsEnvironment ().getAvailableFontFamilyNames();
    }
    catch (LinkageError e)
    {
      // GraphicsEnvironment is available only with JDK > 1.2
      toolkitFontList = Toolkit.getDefaultToolkit ().getFontList ();
    }
    for (int i = 0; i < toolkitFontList.length; i++)
      fontList.addItem (toolkitFontList [i]);
    fontList.select (0);
    // Style check boxes
    final Checkbox plainCheckbox = new Checkbox ("Plain", true);
    final Checkbox boldCheckbox  = new Checkbox ("Bold");
    final Checkbox italicCheckbox = new Checkbox ("Italic");
    final Checkbox bolditalicCheckbox = new Checkbox ("Bold italic");
    // Sizes list (8 to 127) with an initial selected list
    final List sizesList = new List (8, true);
    int [ ] initSizesList = {9, 10, 12, 14, 16, 20, 24};
    for (int i = 5, j = 0; i < 128; i++)
    {
      sizesList.addItem (String.valueOf (i));
      if (   j < initSizesList.length
          && initSizesList [j] == i)
      {
        sizesList.select (sizesList.getItemCount () - 1);
        j++;
      }
    }
    // ScrollPane with example of choosen fonts
    final Canvas demoCanvas = new Canvas ()
      {
        // Returns the list of choosen styles
        public Vector getStyles ()
        {
          Vector vector = new Vector ();
          if (plainCheckbox.getState ())
            vector.addElement (new Integer (Font.PLAIN));
          if (boldCheckbox.getState ())
            vector.addElement (new Integer (Font.BOLD));
          if (italicCheckbox.getState ())
            vector.addElement (new Integer (Font.ITALIC));
          if (bolditalicCheckbox.getState ())
            vector.addElement (new Integer (Font.BOLD | Font.ITALIC));
          return vector;
        }

        public Dimension getPreferredSize ()
        {
          String    fontName = fontList.getSelectedItem ();
          String [] sizes    = sizesList.getSelectedItems ();
          Vector    styles   = getStyles ();
          int       maxWidth = 0;
          int       y        = 0;
          for (int i = 0; i < sizes.length; i++)
            for (int j = 0; j < styles.size (); j++)
            {
              int    fontStyle = ((Integer)styles.elementAt (j)).intValue ();
              Font   font = new Font (fontName, fontStyle, Integer.parseInt (sizes [i]));
              FontMetrics metrics = Toolkit.getDefaultToolkit ().getFontMetrics (font);
              String text = fontName + " " + PJAFontData.styleToString (fontStyle) + " " + sizes [i];
              int    textWidth = metrics.stringWidth (text);
              maxWidth = Math.max (maxWidth, textWidth);
              y += metrics.getHeight () + 2;
            }
          return new Dimension (maxWidth + 10, y + 2);
        }

        // Displays the font in all the choosen sizes
        public void paint (Graphics gc)
        {
          String    fontName = fontList.getSelectedItem ();
          String [] sizes    = sizesList.getSelectedItems ();
          Vector    styles   = getStyles ();
          int       maxWidth = 0;
          for (int i = 0, y = 0; i < sizes.length; i++)
            for (int j = 0; j < styles.size (); j++)
            {
              int    fontStyle = ((Integer)styles.elementAt (j)).intValue ();
              Font   font = new Font (fontName, fontStyle, Integer.parseInt (sizes [i]));
              FontMetrics metrics = Toolkit.getDefaultToolkit ().getFontMetrics (font);
              String text = fontName + " " + PJAFontData.styleToString (fontStyle) + " " + sizes [i];

              gc.setColor (Color.black);
              gc.setFont (font);
              gc.drawString (text, 5, y + metrics.getAscent () + metrics.getLeading ());

              y += metrics.getHeight () + 2;
            }
        }
      };
    demoCanvas.setBackground (Color.white);
    final ScrollPane demoScrollPane = new ScrollPane (ScrollPane.SCROLLBARS_ALWAYS);
    // Dialog buttons
    Button saveFontButton = new Button ("Save font");
    Button exitButton     = new Button (exitButtonText);

    final Dialog dialog = new Dialog (frame, "PJA Font capture", true);
    dialog.setLayout (new GridBagLayout ());
    Insets labelInsets = new Insets (5, 2, 0, 10);
    Insets compInsets  = new Insets (2, 2, 2, 5);
    dialog.add (new Label ("Fonts"),  new GridBagConstraints2 (0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, labelInsets, 0, 0));
    dialog.add (fontList,             new GridBagConstraints2 (0, 1, 1, 6, 0.2, 0., GridBagConstraints.CENTER, GridBagConstraints.BOTH, compInsets, 0, 0));
    dialog.add (new Label ("Styles"), new GridBagConstraints2 (1, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, labelInsets, 0, 0));
    dialog.add (plainCheckbox,        new GridBagConstraints2 (1, 1, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, compInsets, 0, 0));
    dialog.add (boldCheckbox,         new GridBagConstraints2 (1, 2, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, compInsets, 0, 0));
    dialog.add (italicCheckbox,       new GridBagConstraints2 (1, 3, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, compInsets, 0, 0));
    dialog.add (bolditalicCheckbox,   new GridBagConstraints2 (1, 4, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, compInsets, 0, 0));
    dialog.add (new Label ("Sizes"),  new GridBagConstraints2 (1, 5, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, labelInsets, 0, 0));
    dialog.add (sizesList,            new GridBagConstraints2 (1, 6, 1, 1, 0, 1., GridBagConstraints.CENTER, GridBagConstraints.BOTH, compInsets, 0, 0));
    dialog.add (demoScrollPane,       new GridBagConstraints2 (2, 0, 1, 7, 0.8, 0., GridBagConstraints.CENTER, GridBagConstraints.BOTH, compInsets, 0, 0));
    Panel  buttonsPanel = new Panel ();
    buttonsPanel.setLayout (new GridLayout (1, 2, 20, 0));
    buttonsPanel.add (saveFontButton);
    buttonsPanel.add (exitButton);
    dialog.add (buttonsPanel,         new GridBagConstraints2 (0, 7, 3, 1, 0., 0., GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets (10, 0, 5, 0), 0, 0));
    // Add listeners which will redraw demoCanvas
    ItemListener canvasChangeListener = new ItemListener ()
      {
        public void itemStateChanged (ItemEvent e)
        {
          demoCanvas.invalidate ();
          demoScrollPane.validate ();
          demoCanvas.repaint ();
        }
      };
    fontList.addItemListener (canvasChangeListener);
    sizesList.addItemListener (canvasChangeListener);
    plainCheckbox.addItemListener (canvasChangeListener);
    boldCheckbox.addItemListener (canvasChangeListener);
    italicCheckbox.addItemListener (canvasChangeListener);
    bolditalicCheckbox.addItemListener (canvasChangeListener);

    dialog.addWindowListener (new WindowAdapter ()
      {
        public void windowClosing (WindowEvent e)
        {
          dialog.dispose ();
        }
      });
    saveFontButton.addActionListener (new ActionListener ()
      {
        public void actionPerformed (ActionEvent e)
        {
          // Get a filename
          FileDialog fileDialog = new FileDialog (frame, "Font capture file name", FileDialog.SAVE);
          fileDialog.setDirectory (PJAGraphicsManager.getDefaultGraphicsManager ().getFontsDirectory ());
          fileDialog.setFile (fontList.getSelectedItem () + ".pjaf");
          fileDialog.show ();
          final String dir  = fileDialog.getDirectory ();
          final String file = fileDialog.getFile ();
          if (dir != null && file != null)
          {
            final Thread captureFontThread = new Thread ()
              {
                public void run ()
                {
                  final String fontName = fontList.getSelectedItem ();
                  String [] sizes    = sizesList.getSelectedItems ();
                  int []    fontSizes = new int [sizes.length];
                  for (int i = 0; i < fontSizes.length; i++)
                    fontSizes [i] = Integer.parseInt (sizes [i]);
                  captureFont (fontName, fontSizes,
                               plainCheckbox.getState (),
                               boldCheckbox.getState (),
                               italicCheckbox.getState (),
                               bolditalicCheckbox.getState (),
                               dir + File.separator + file,
                     new CaptureFontListener ()
                     {
                       // Implement a listener that displays an information dialog box
                       Dialog  captureDialog = new Dialog (frame, "Capture font " + fontName, false);
                       Label   infoLabel     = new Label ("Start capturing font " + fontName);
                       Button  button        = new Button ("Cancel");

                       {
                         button.addActionListener (new ActionListener ()
                           {
                             public void actionPerformed (ActionEvent event)
                             {
                               stop ();
                               captureDialog.dispose ();
                             }
                           });
                         captureDialog.setLayout (new GridBagLayout ());
                         captureDialog.add (infoLabel, new GridBagConstraints2 (0, 0, 1, 1, 0, 0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets (5, 5, 0, 5), 0, 0));
                         captureDialog.add (button, new GridBagConstraints2 (0, 1, 1, 1, 0, 0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets (10, 0, 5, 0), 0, 0));
                         captureDialog.setResizable (false);
                         captureDialog.pack ();
                         captureDialog.show ();
                       }

                       public void captureNewFont (Font font)
                       {
                         infoLabel.setText ("Computing font " + font.getName ()
                                                        + " " + PJAFontData.styleToString (font.getStyle ())
                                                        + " " + font.getSize () + ".");
                         captureDialog.pack ();
                       }

                       public void captureEnded ()
                       {
                         infoLabel.setText ("Capture font ended.");
                         button.setLabel (" Ok ");
                         captureDialog.pack ();
                       }

                       public void error (Exception e, String message)
                       {
                         infoLabel.setText (message + ".");
                         button.setLabel (" Ok ");
                         captureDialog.pack ();
                       }
                     });
                }
              };
            captureFontThread.start ();
          }
        }
      });
    exitButton.addActionListener (new ActionListener ()
      {
        public void actionPerformed (ActionEvent e)
        {
          dialog.dispose ();
        }
      });

    dialog.pack ();
    // Enlarge 200 pixels at right to ensure some room for demoCanvas
    dialog.setSize (dialog.getSize ().width + 200, dialog.getSize ().height);
    // demoCanvas is added only at end of because demoScrollPane has
    // no preferred size on Windows
    demoScrollPane.add (demoCanvas);
    dialog.show ();
  }

  private interface CaptureFontListener
  {
    void captureNewFont(Font font);
    void captureEnded();
    void error(Exception e, String message);
  }

  public static void captureFont (String fontName, int fontSizes [],
                                  boolean plain, boolean bold, boolean italic, boolean bolditalic,
                                  String  file,
                                  CaptureFontListener listener)
  {
    int     styles [] = {Font.PLAIN, Font.BOLD, Font.ITALIC, Font.BOLD | Font.ITALIC};
    boolean stylesChoice [] = {plain, bold, italic, bolditalic};

    int minSize = Integer.MAX_VALUE;
    int maxSize = 0;
    for (int i = 0; i < fontSizes.length; i++)
    {
      minSize = Math.min (minSize, fontSizes [i]);
      maxSize = Math.max (maxSize, fontSizes [i]);
    }

    DataOutputStream output = null;
    try
    {
      output = new DataOutputStream (new BufferedOutputStream (new FileOutputStream (file)));
      // Write a PJAF1.1 header
      output.writeUTF (PJAFontData.PJAF1_1_FILE_HEADER);

      for (int styleIndex = 0; styleIndex < stylesChoice.length; styleIndex++)
        if (stylesChoice [styleIndex])
          for (int i = 0; i < fontSizes.length; i++)
          {
            Font        font = new Font (fontName, styles [styleIndex], fontSizes [i]);
            listener.captureNewFont (font);
            PJAFontData fontData = new PJAFontData (font, 0, 255);
            // Save font to file
            fontData.write (output);
          }

      output.close ();
      output = null;
      listener.captureEnded ();
    }
    catch (IOException e)
    {
      listener.error (e, "Can't save to file " + file + " " + e.getMessage ());
    }
    finally
    {
      // If file is still opened close and delete the created file
      if (output != null)
        try
        {
          output.close ();
          new File (file).delete ();
        }
        catch (IOException ex)
        { }
    }
  }
}
