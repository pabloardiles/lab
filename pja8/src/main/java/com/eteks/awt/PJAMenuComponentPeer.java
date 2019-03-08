/*
 * @(#)PJAMenuComponentPeer.java  06/07/2000
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
import java.awt.peer.*;

/**
 * Pure Java AWT Menu Component Peer. This implementation enables to create
 * dummy menu components.<code>
 * It was added because menu peer interfaces don't belong to component peer 
 * interfaces hierarchy.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       PJAToolkit
 * @since     PJA1.2
 */
public class PJAMenuComponentPeer implements MenuBarPeer,
                                             MenuComponentPeer,
                                             MenuItemPeer,
                                             MenuPeer,
                                             PopupMenuPeer,
                                             CheckboxMenuItemPeer
{
  private MenuComponent  menuComponent;

  public PJAMenuComponentPeer (MenuComponent menuComponent)
  {
    this.menuComponent = menuComponent;
  }  

  public void dispose()
  {
  }

  @Override
  public void setFont(Font f) {

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

  // MenuItemPeer implementation
  public void enable ()
  {
    setEnabled (true);
  }

  public void disable ()
  {
    setEnabled (false);
  }

  public void setEnabled(boolean b)
  {
  }

  public void setLabel(String label)
  {
  }

  // PopupMenuPeer implementation
  public void show(Event e)
  {
  }

  // CheckboxMenuItemPeer implementation
  public void setState(boolean state)
  {
  }
}
