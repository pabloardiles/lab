/*
 * @(#)PJARedirectServlet.java   06/23/2000
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
package com.eteks.awt.servlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.Hashtable;

/**
 * This servlet uses <code>PJAServlet</code> services to get a default toolkit, and redirects all servlet 
 * calls to a destination servlet ensuring to it to have a default toolkit at disposal for graphics servlet.
 * The destination servlet class can be coded in either ways :
 * <ul><li>In the init parameter named <code>com.eteks.awt.servlet.destinationservlet</code></li>
 *     <li>In the parameter named <code>com.eteks.servlet.destinationservlet</code> sent in user request.</li></ul>
 * For example, if you wanted to call a servlet with the URL http://server/servlet/com.eteks.servlet.DefaultToolkitTest?param1=value1&param2=value2
 * you have to change by http://server/servlet/com.eteks.awt.servlet.PJARedirectServlet?destinationServletClass=com.eteks.servlet.DefaultToolkitTest&param1=value1&param2=value2
 * Or if you servlet initialization file contained something like :
 * <blockquote><pre>servlet.toolkit.code=com.DefaultToolkitTest
 * servlet.toolkit.initArgs=initParam1=value1, initParam2=value2</pre></blockquote>
 * It will become :
 * <blockquote><pre>servlet.toolkit.code=com.PJARedirectServlet
 * servlet.toolkit.initArgs=initParam1=value1, initParam2=value2, destinationServletClass=com.DefaultToolkitTest, toolkitFontsPath=<i>pathWithPJAForTTFFonts</i></pre></blockquote>
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @since     PJA2.1
 */
public class PJARedirectServlet extends PJAServlet
{
  public static final String DESTINATION_SERVLET_CLASS_PARAMETER = "destinationServletClass";
  public static final String FONTS_PATH_PARAMETER                = "toolkitFontsPath";
  
  private Hashtable destinationServlets = new Hashtable ();
  private String    fontsPath;
  
  public HttpServlet instantiateDestinationServlet (String destServletClass, boolean init) throws ServletException
  {
    try
    {
      if (destServletClass == null)
        if (init)
          return null; // Let a second chance by parameter
        else
          throw new ServletException ("No " + DESTINATION_SERVLET_CLASS_PARAMETER + " defined");
      else
        return (HttpServlet)Class.forName (destServletClass).newInstance ();
    }
    catch (IllegalArgumentException e)
    { // thrown by forName () if destServletClass == ""
      throw new ServletException ("No " + DESTINATION_SERVLET_CLASS_PARAMETER + " defined");
    }
    catch (ClassNotFoundException e)
    {
      throw new ServletException (DESTINATION_SERVLET_CLASS_PARAMETER + " class not found : " + destServletClass);
    }
    catch (InstantiationException e)
    {
      throw new ServletException ("Could not instantiate " + destServletClass);
    }
    catch (IllegalAccessException e)
    {
      throw new ServletException ("Could not access " + destServletClass);
    }
    catch (ClassCastException e)
    {
      throw new ServletException (destServletClass + " not a subclass of " + HttpServlet.class.getName ());
    }  // Thrown if super class of the loaded class isn't HttpServlet
  }
  
  public HttpServlet getDestinationServlet (String destServletClass, String fontsPath, boolean init) throws ServletException
  {
    if (destServletClass == null)
      return null;
      
    HttpServlet destinationServlet = (HttpServlet)destinationServlets.get (destServletClass);
    if (destinationServlet == null)
    {
      this.fontsPath = fontsPath;    
      destinationServlet = instantiateDestinationServlet (destServletClass, init);

      // Init destination servlet with the same servletConfig
      if (destinationServlet != null)
      {
        destinationServlets.put (destServletClass, destinationServlet);
        if (   !init
            && !isDefaultToolkitAvailable ()) 
          testDefaultToolkit ();
        destinationServlet.init (getServletConfig ());
      }
    }
    
    return destinationServlet;
  } 
  
  public String getFontsPath ()
  {
    return   super.getFontsPath () 
           + (fontsPath != null ? File.pathSeparator + fontsPath : ""); 
  }

  public void initPJA (ServletConfig config) throws ServletException
  {
    super.initPJA (config);
    getDestinationServlet (getInitParameter (DESTINATION_SERVLET_CLASS_PARAMETER), getInitParameter (FONTS_PATH_PARAMETER), true);
  }

  public void destroyPJA ()
  {
    try
    {
      getDestinationServlet (getInitParameter (DESTINATION_SERVLET_CLASS_PARAMETER), getInitParameter (FONTS_PATH_PARAMETER), true)
            .destroy ();
    }
    catch (ServletException e)
    { }
  }

  public void doPostPJA (HttpServletRequest  request,
                            HttpServletResponse response)
                   throws ServletException, IOException
  {
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }

  public void doGetPJA (HttpServletRequest  request,
                           HttpServletResponse response)
                   throws ServletException, IOException
  {    
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }

  public void doPutPJA (HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
  {
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }

  public void doDeletePJA (HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
  {
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }

  public void doOptionsPJA (HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
  {
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }

  public void doTracePJA (HttpServletRequest request, HttpServletResponse response) 
	throws ServletException, IOException
  {
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }		

  public void servicePJA (HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException
  {
    getDestinationServlet (request.getParameter (DESTINATION_SERVLET_CLASS_PARAMETER), request.getParameter (FONTS_PATH_PARAMETER), false)
          .service ((ServletRequest)request, (ServletResponse)response);
  }
}
