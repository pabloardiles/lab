/*
 * @(#)PJAServletTest.java   07/05/2000
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

import com.eteks.awt.servlet.PJAServlet;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

/**
 * This servlet/program tests if graphics can work on a JVM or not, 
 * with <code>PJAServlet</code> as super class.
 * Servlet can be tested with the URL http://..../servlet/com.eteks.servlet.PJAServletTest
 * Program can be tested with the command java -classpath pja.jar com.PJAServletTest
 * (or java -Xbootclasspath/a:pja.jar com.PJAServletTest)
 * You may have to override or modify the method <code>getFontsPath ()</code> to add
 * the directory where fonts are stored.
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       com.eteks.awt.PJAToolkit
 * @see       PJAServlet
 * @see       com.eteks.awt.servlet.PJARedirectServlet
 * @since     PJA2.1
 */
public class PJAServletTest extends PJAServlet
{
  public void doGetPJA (HttpServletRequest  request,
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
      out.print ("Default Toolkit and Font class could be instantiated, you should be able to do graphics using PJAServlet as a super class.\n");
    }
    catch (Throwable e) 
    // Different type of exceptions may be thrown depending on JDK version
    {
      out.print ("Default Toolkit or Font class couldn't be instantiated.\n");
      e.printStackTrace (out);
    }    
  }

  public String getFontsPath ()
  {
    return   super.getFontsPath () + File.pathSeparator 
           + ""; // Change it if needed
  }

  public String getUserHomeDir ()
  {
    return super.getUserHomeDir (); // Change it if needed
  }
    
  public static void main (String [] args)
  {
    new PJAServletTest ().test (System.out);
    System.exit (0);
  }
}
