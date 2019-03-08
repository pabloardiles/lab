/*
 * @(#)PJAComponentPeer.java  06/07/2000
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

import sun.awt.CausedFocusEvent;
import sun.java2d.pipe.Region;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.PaintEvent;
import java.awt.im.InputMethodRequests;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.*;
import java.io.FilenameFilter;
import java.util.List;

// Java 2 classes


/**
 * Pure Java AWT Component Peer. This implementation enables to create
 * dummy components.
 *
 * @version   2.4
 * @author    Emmanuel Puybaret
 * @see       PJAToolkit
 * @since     PJA1.0
 */
public class PJAComponentPeer implements ButtonPeer,
                                        CanvasPeer,
                                        CheckboxMenuItemPeer,
                                        CheckboxPeer,
                                        ChoicePeer,
                                        ComponentPeer,
                                        ContainerPeer,
                                        DialogPeer,
                                        FileDialogPeer,
                                        LabelPeer,
                                        ListPeer,
                                        MenuBarPeer,
                                        MenuComponentPeer,
                                        MenuItemPeer,
                                        MenuPeer,
                                        PanelPeer,
                                        PopupMenuPeer,
                                        ScrollPanePeer,
                                        ScrollbarPeer,
                                        TextAreaPeer,
                                        TextComponentPeer,
                                        TextFieldPeer,
                                        WindowPeer
{
  private Toolkit            toolkit;
  private PJAGraphicsManager manager;
  private Component          component;

  /**
   * @deprecated As of PJA version 1.2, should use 
   *             <code>PJAComponentPeer (Component)</code> constructor 
   *             or <code>PJAMenuComponentPeer (MenuComponent)</code> constructor instead.
   */
  public PJAComponentPeer ()
  {
    // toolkit is not initialized to avoid security problems
    manager = PJAGraphicsManager.getDefaultGraphicsManager ();
  }

  /**
   * This method was added to keep a link to a component and 
   * be able to get its background color.
   * @since PJA1.2
   */
  public PJAComponentPeer (Component component)
  {
    this ();
    this.component = component;
  }
  
  public Component getComponent ()
  {
    return component;
  }
  
  // ComponentPeer implementation
  public void  setVisible (boolean b)
  {
  }

  public void setEnabled(boolean b)
  {
  }

  public void paint(Graphics g)
  {
  }

  public void repaint(long tm, int x, int y, int width, int height)
  {
  }

  public void print(Graphics g)
  {
  }

  @Override
  public void setBounds(int x, int y, int width, int height, int op) {

  }

  public void setBounds (int x, int y, int width, int height)
  {
  }

  public void handleEvent (AWTEvent e)
  {
  }

  public void coalescePaintEvent (PaintEvent e)
  {
  }

  public Point getLocationOnScreen()
  {
    return new Point (0, 0);
  }

  public Dimension getPreferredSize()
  {
    return new Dimension (0, 0);
  }

  public Dimension getMinimumSize()
  {
    return new Dimension (0, 0);
  }

  public ColorModel getColorModel()
  {
    // v2.0 : color model is obtained from GraphicsManager
    return manager.getColorModel ();
  }

  public Toolkit getToolkit()
  {
    if (toolkit == null)
      toolkit = new PJAToolkit ();
    return toolkit;
  }

  public Graphics getGraphics()
  {
    return null;
  }

  public FontMetrics getFontMetrics(Font font)
  {
    return manager.getFontMetrics (font);
  }

  public void dispose()
  {
  }

  public void setForeground(Color c)
  {
  }

  public void setBackground(Color c)
  {
  }

  public void setFont(Font f)
  {
  }

  public void setCursor(Cursor cursor)
  {
  }

  public void requestFocus()
  {
  }

  public boolean isFocusTraversable()
  {
    return false;
  }


  public Image createImage (ImageProducer producer)
  {
    return manager.createImage (producer);
  }

  /**
   * Creates an image of <code>width x height</code> pixels.
   *
   */
  public Image  createImage (int width, int height)
  {
    Image image = manager.createImage (width, height);
    // v1.1 : Image if filled first with background color 
    // v1.2 : default background is white
    Graphics gc    = image.getGraphics ();
    Color background = component == null || component.getBackground () == null
                         ? Color.white
                         : component.getBackground ();
    gc.setColor (background);
    gc.fillRect (0, 0, width, height);
    return image;
  }

  public boolean prepareImage (Image img, int w, int h, ImageObserver o)
  {
    return manager.prepareImage (img, w, h, o);
  }

  public int checkImage (Image img, int w, int h, ImageObserver o)
  {
    return manager.checkImage (img, w, h, o);
  }

  // Method with Java 2 classes
  public GraphicsConfiguration getGraphicsConfiguration ()
  {
    // v2.0 : GraphicsConfiguration return from manager
    return manager.getGraphicsConfiguration ();
  }

  /**
   * DEPRECATED:  Replaced by getPreferredSize().
   */
  public Dimension preferredSize()
  {
    return getPreferredSize ();
  }

  /**
   * DEPRECATED:  Replaced by getMinimumSize().
   */
  public Dimension minimumSize()
  {
    return getMinimumSize ();
  }

  /**
   * DEPRECATED:  Replaced by setVisible(boolean).
   */
  public void show()
  {
    setVisible (true);
  }

  /**
   * DEPRECATED:  Replaced by setVisible(boolean).
   */
  public void hide()
  {
    setVisible (false);
  }

  /**
   * DEPRECATED:  Replaced by setEnabled(boolean).
   */
  public void enable()
  {
    setEnabled (true);
  }

  /**
   * DEPRECATED:  Replaced by setEnabled(boolean).
   */
  public void disable()
  {
    setEnabled (false);
  }

  /**
   * DEPRECATED:  Replaced by setBounds(int, int, int, int).
   */
  public void  reshape(int x, int y, int width, int height)
  {
    setBounds (x, y, width, height);
  }

  // v2.4 : Added isObscured () for JDK 1.4 support
  public boolean isObscured()
  {
    return false;
  }
  
  // v2.4 : Added canDetermineObscurity () for JDK 1.4 support
  public boolean canDetermineObscurity()
  {
    return false;
  }

  // v2.4 : Added updateCursorImmediately () for JDK 1.4 support
  public void updateCursorImmediately()
  {
  }

  @Override
  public boolean requestFocus(Component lightweightChild, boolean temporary, boolean focusedWindowChangeAllowed, long time, CausedFocusEvent.Cause cause) {
    return false;
  }

  // v2.4 : Added requestFocus () for JDK 1.4 support
  public boolean requestFocus(Component lightweightChild,
                              boolean temporary,
                              boolean focusedWindowChangeAllowed,
                              long time)
  {
    return false;
  }
                       
  // v2.4 : Added isFocusable () for JDK 1.4 support
  public boolean isFocusable()
  {
    return false;
  }

  // v2.4 : Added handlesWheelScrolling () for JDK 1.4 support
  public boolean handlesWheelScrolling()
  {
    return false;
  }

  // v2.4 : Added getBackBuffer () for JDK 1.4 support
  public Image getBackBuffer()
  {
    return null;
  }

  @Override
  public void flip(int x1, int y1, int x2, int y2, BufferCapabilities.FlipContents flipAction) {

  }

  // v2.4 : Added destroyBuffers () for JDK 1.4 support
  public void destroyBuffers()
  {
  }

  @Override
  public void reparent(ContainerPeer newContainer) {

  }

  @Override
  public boolean isReparentSupported() {
    return false;
  }

  @Override
  public void layout() {

  }

  @Override
  public void applyShape(Region shape) {

  }

  @Override
  public void setZOrder(ComponentPeer above) {

  }

  @Override
  public boolean updateGraphicsData(GraphicsConfiguration gc) {
    return false;
  }

  // v2.4 : Added createVolatileImage () for JDK 1.4 support
  public java.awt.image.VolatileImage createVolatileImage(int width, int height)
  {
    return null;
  }

  // v2.4 : Added flip () for JDK 1.4 support
  public void flip(BufferCapabilities.FlipContents flipAction)
  {
  }

  // v2.4 : Added createBuffers () for JDK 1.4 support
  public void createBuffers(int numBuffers, BufferCapabilities caps) throws AWTException
  {
  }

  // ButtonPeer implementation
  public void setLabel(String label)
  {
  }

  // CheckboxPeer implementation
  public void setState(boolean state)
  {
  }

  public void setCheckboxGroup(CheckboxGroup g)
  {
  }

  // Choice implementation
  public void add(String item, int index)
  {
  }

  public void remove(int index)
  {
  }

  public void select(int index)
  {
  }

  public void addItem(String item, int index)
  {
  }

  // LabelPeer implementation
  public void setText(String label)
  {
  }

  public void setAlignment(int alignment)
  {
  }

  // ListPeer implementation
  public int[] getSelectedIndexes()
  {
    return new int [0];
  }

  public void delItems(int start, int end)
  {
  }

  public void removeAll()
  {
  }

  public void deselect(int index)
  {
  }

  public void makeVisible(int index)
  {
  }

  public void setMultipleMode(boolean b)
  {
  }

  public Dimension getPreferredSize(int rows)
  {
    return new Dimension (0, 0);
  }

  public Dimension getMinimumSize(int rows)
  {
    return new Dimension (0, 0);
  }

  /**
   * DEPRECATED:  Replaced by removeAll().
   */
  public void clear()
  {
  }

  /**
   * DEPRECATED:  Replaced by setMultipleMode(boolean).
   */
  public void setMultipleSelections(boolean v)
  {
  }

  /**
   * DEPRECATED:  Replaced by getPreferredSize(int).
   */
  public Dimension preferredSize(int v)
  {
    return getPreferredSize(v);
  }

  /**
   * DEPRECATED:  Replaced by getMinimumSize(int).
   */
  public Dimension minimumSize(int v)
  {
    return getMinimumSize (v);
  }

  // ScrollbarPeer implementation
  public void setValues(int value, int visible, int minimum, int maximum)
  {
  }

  public void setLineIncrement(int l)
  {
  }

  public void setPageIncrement(int l)
  {
  }

  // ScrollPanePeer implementation
  public int getHScrollbarHeight()
  {
    return 0;
  }

  public int getVScrollbarWidth()
  {
    return 0;
  }

  public void setScrollPosition(int x, int y)
  {
  }

  public void childResized(int w, int h)
  {
  }

  public void setUnitIncrement(Adjustable adj, int u)
  {
  }

  public void setValue(Adjustable adj, int v)
  {
  }

  // TextComponentPeer implementation
  public void setEditable(boolean editable)
  {
  }

  public String getText()
  {
    return "";
  }

  public int getSelectionStart()
  {
    return 0;
  }

  public int getSelectionEnd()
  {
    return 0;
  }

  public void select(int selStart, int selEnd)
  {
  }

  public void setCaretPosition(int pos)
  {
  }

  public int getCaretPosition()
  {
    return 0;
  }

  @Override
  public InputMethodRequests getInputMethodRequests() {
    return null;
  }

  public int getIndexAtPoint(int x, int y)
  {
    return -1;
  }

  public Rectangle getCharacterBounds(int i)
  {
    return null;
  }

  public long filterEvents(long mask)
  {
    return 0;
  }

  // TextAreaPeer implementation
  public void insert(String text, int pos)
  {
  }

  public void replaceRange(String text, int start, int end)
  {
  }

  public Dimension getPreferredSize(int rows, int columns)
  {
    return new Dimension (0, 0);
  }

  public Dimension getMinimumSize(int rows, int columns)
  {
    return new Dimension (0, 0);
  }

  /**
   * DEPRECATED:  Replaced by insert(String, int).
   */
  public void insertText(String txt, int pos)
  {
  }

  /**
   * DEPRECATED:  Replaced by ReplaceRange(String, int, int).
   */
  public void replaceText(String txt, int start, int end)
  {
  }

  /**
   * DEPRECATED:  Replaced by getPreferredSize(int, int).
   */
  public Dimension preferredSize(int rows, int cols)
  {
    return getPreferredSize (rows, cols);
  }

  /**
   * DEPRECATED:  Replaced by getMinimumSize(int, int).
   */
  public Dimension minimumSize(int rows, int cols)
  {
    return getMinimumSize (rows, cols);
  }

  // TextFieldPeer implementation
  public void setEchoChar(char echoChar)
  {
  }

  /**
   * DEPRECATED:  Replaced by setEchoChar(char echoChar).
   */
  public void setEchoCharacter(char c)
  {
  }

  // ContainerPeer implementation
  public Insets getInsets()
  {
    return new Insets (0, 0, 0, 0);
  }

  public void beginValidate()
  {
  }

  public void endValidate()
  {
  }

  // v2.4 : Added beginLayout () for JDK 1.4 support
  public void beginLayout()
  {
  }

  // v2.4 : Added endLayout () for JDK 1.4 support
  public void endLayout()
  {
  }

  // v2.4 : Added isPaintPending () for JDK 1.4 support
  public boolean isPaintPending()
  {
    return false;
  }
  
  /**
   * DEPRECATED:  Replaced by getInsets().
   */
  public Insets insets()
  {
    return getInsets ();
  }

  // WindowPeer implementation
  public void toFront()
  {
  }

  public void toBack()
  {
  }

  @Override
  public void updateAlwaysOnTopState() {

  }

  @Override
  public void updateFocusableWindowState() {

  }

  @Override
  public void setModalBlocked(Dialog blocker, boolean blocked) {

  }

  @Override
  public void updateMinimumSize() {

  }

  @Override
  public void updateIconImages() {

  }

  @Override
  public void setOpacity(float opacity) {

  }

  @Override
  public void setOpaque(boolean isOpaque) {

  }

  @Override
  public void updateWindow() {

  }

  @Override
  public void repositionSecurityWarning() {

  }

  public int handleFocusTraversalEvent (KeyEvent e)
  {
    return 0;
  }

  // DialogPeer implementation
  public void setTitle(String title)
  {
  }

  public void setResizable(boolean resizeable)
  {
  }

  @Override
  public void blockWindows(List<Window> windows) {

  }

  // FileDialogPeer implementation
  public void setFile(String file)
  {
  }

  public void setDirectory(String dir)
  {
  }

  public void setFilenameFilter(FilenameFilter filter)
  {
  }

  // MenuBarPeer implementation
  public void addMenu(Menu m)
  {
  }

  public void delMenu(int index)
  {
  }

  public void addHelpMenu(Menu m)
  {
  }

  // MenuPeer implementation
  public void addSeparator()
  {
  }

  public void addItem(MenuItem item)
  {
  }

  public void delItem(int index)
  {
  }


  // PopupMenuPeer implementation
  public void show(Event e)
  {
  }

  @Override
  public GraphicsConfiguration getAppropriateGraphicsConfiguration(GraphicsConfiguration gc) {
    return null;
  }
}
