/*
 * @(#)PJAFramePeer.java  05/16/2000
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
import java.awt.peer.FramePeer;

/**
 * Pure Java AWT Frame Peer. This is the mimimum peer that must exist to
 * be able to create an off screen image, when used with AWT toolkit,
 * for which a display is available.
 * <PRE>
 *   Frame frame = new Frame ();
 *   frame.addNotify ();
 *   Image image = frame.createImage (width, height);
 * </PRE>
 *
 * @version   2.4
 * @author    Emmanuel Puybaret
 * @since     PJA1.0
 */
public class PJAFramePeer extends PJAComponentPeer implements FramePeer
{
  public PJAFramePeer (Frame frame)
  {
    super (frame);
  }

  // FramePeer implementation
  public void setTitle (String title)
  {
  }

  public void setIconImage (Image im)
  {
  }

  public void setMenuBar (MenuBar mb)
  {
  }

  public void setResizable (boolean resizeable)
  {
  }

  public void setState (int state)
  {
  }

  public int getState ()
  {
    return 0;
  }
  
  // v2.4 : Added setMaximizedBounds () for JDK 1.4 support
  public void setMaximizedBounds(Rectangle bounds)
  {    
  }

  @Override
  public void setBoundsPrivate(int x, int y, int width, int height) {

  }

  @Override
  public Rectangle getBoundsPrivate() {
    return null;
  }

  @Override
  public void emulateActivation(boolean activate) {

  }
}

