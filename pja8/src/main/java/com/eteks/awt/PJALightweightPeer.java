/*
 * @(#)PJALightweightPeer.java  06/07/2000
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
import java.awt.peer.LightweightPeer;

/**
 * Pure Java AWT Component Peer. This implementation enables to create
 * dummy components.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @since     PJA1.0
 */
public class PJALightweightPeer extends PJAComponentPeer implements LightweightPeer
{
  /**
   * @deprecated As of PJA version 1.2, should use 
   *             <code>PJALightweightPeer (Component)</code> constructor instead.
   */
  public PJALightweightPeer ()
  {
  }

  /**
   * This method was added to keep a link to a component and 
   * be able to get its background color.
   * @since PJA1.2
   */
  public PJALightweightPeer (Component component)
  {
    super (component);
  }
}