/*
 * @(#)DefaultToolkitTest.java   05/23/2000
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
package com.eteks.servlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This servlet/program tests if graphics can work on a JVM or not, using default toolkit.
 * Servlet can be tested with the URL http://..../servlet/com.eteks.servlet.DefaultToolkitTest
 * or http://..../servlet/com.eteks.awt.servlet.PJARedirectServlet?destinationServletClass=com.eteks.servlet.DefaultToolkitTest.<br>
 * Program can be tested with the command java com.DefaultToolkitTest
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAToolkit
 * @since     PJA2.1
 */
public class DefaultToolkitTest extends HttpServlet
{
  public void doGet (HttpServletRequest  request,
                     HttpServletResponse response) throws ServletException, IOException
  {
    response.setContentType ("text/plain");
    PrintStream out = new PrintStream (response.getOutputStream ());
    test (out);
  }
  
  /**
   * Tries to instantiate default toolkit and <code>Font</code> class 
   * and writes on <code>out</code> stream if it succeeded or failed.
   * This ensures that AWT toolkit is enabled.
   */ 
  public void test (PrintStream out)
  { 
    try
    {
      Toolkit.getDefaultToolkit ();
      new Font ("", Font.PLAIN, 10);       
      out.print ("Default Toolkit and Font class could be instantiated, you should be able to do graphics with this JVM.\n");
    }
    catch (Throwable e) 
    // Different type of exceptions may be thrown depending on JDK version
    {
      out.print ("Default Toolkit or Font class couldn't be instantiated.\n");
      e.printStackTrace (out);
    }    
  }

  public static void main (String [] args)
  {
    new DefaultToolkitTest ().test (System.out);
    System.exit (0);
  }
}
