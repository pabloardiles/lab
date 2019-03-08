/*
 * @(#)PJAServlet.java   07/05/2000
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

import com.eteks.awt.PJAGraphicsManager;
import com.eteks.awt.PJAImage;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * This is a model of super class which tries its best to get an AWT toolkit 
 * default instance (either default system one or PJA). 
 * You must override these methods :
 * <ul>
 * <li><code>public void initPJA (ServletConfig config)</code></li>
 * <li><code>public void initPJA () throws ServletException </code></li>
 * <li><code>public String getServletInfoPJA () </code></li>
 * <li><code>public void destroyPJA () </code></li>
 * <li><code>public void servicePJA (ServletRequest req, ServletResponse resp)</code></li>
 * <li><code>protected void doGetPJA (HttpServletRequest req, HttpServletResponse resp)</code></li>
 * <li><code>protected void doPostPJA (HttpServletRequest req, HttpServletResponse resp)</code></li>
 * <li><code>protected void doPutPJA (HttpServletRequest req, HttpServletResponse resp)</code></li>
 * <li><code>protected void doDeletePJA (HttpServletRequest req, HttpServletResponse resp)</code></li>
 * <li><code>protected void doOptionsPJA (HttpServletRequest req, HttpServletResponse resp)</code></li>
 * <li><code>protected void doTracePJA (HttpServletRequest req, HttpServletResponse resp) </code></li>
 * <li><code>protected void servicePJA (HttpServletRequest req, HttpServletResponse resp) </code></li>
 * </ul>
 * instead of their counterpart standard servlet methods without the <code>PJA</code> extension. 
 * All these standard methods are overriden in this class as <code>final</code> methods to ensure 
 * they won't be overriden again in <code>PJAServlet</code>.<br>
 * From the extending servlet, servlet standard methods calls following a <code>super.</code> 
 * (like <code>super.init (config)</code>) must use also their PJA postfixed counterpart
 * methods (resulting to <code>super.initPJA (config)</code>). If not this will cause a stack overflow.<br>
 * If you want to change existing code of a servlet, the fastest way should be to postfix with PJA 
 * all standard servlet methods names (either used to override servlet methods, or used in a method call).
 * Any call to AWT methods using default toolkit can be made from all the previous PJA postfixed methods. 
 * If current security manager is too restrictive, some required properties may be impossible to set and 
 * <code>com.PJAToolkit</code> can't be loaded.
 * You can call <code>createImage (width, height)</code> to instantiate an image, or do it yourself
 * with standard AWT code.<br>  
 * Notice that in some servlet configurations the static and instance initializers,
 * as default constructor may be called twice because <code>PJAServlet</code> may need to reload and instantiate 
 * classes with a special class loader to ensure that the classes <code>java.awt.Toolkit</code>, 
 * <code>com.PJAToolkit</code>, <code>com.PJAServlet</code> and the class
 * extending <code>PJAServlet</code> shares the same classpath.
 * This implies that for example, JDBC connections should be initialized in <code>initPJA ()</code> 
 * rather than in a constructor or in an initializer.
 *
 * @version 2.1
 * @author  Emmanuel Puybaret
 * @see     com.eteks.awt.PJAToolkit
 * @see     #PJAServlet(boolean) 
 * @see     #createImage(int,int)
 * @since   PJA2.1
 */
public abstract class PJAServlet extends HttpServlet
{
  private static PJAClassLoader redirectClassLoader;

  private Object     calledServlet;
  private boolean    defaultToolkitAvailable;
  private boolean    forcePJA;
  
  private PJAGraphicsManager graphicsManager;
    
  /**
   * Creates a default <code>PJAServlet</code> instance that tries
   * its best to instantiate a default toolkit.
   * @see #PJAServlet(boolean) 
   */
  public PJAServlet () 
  {
    this (false);
  }
   
  /**
   * Creates a <code>PJAServlet</code> instance that tries to instantiate a default toolkit.<br>
   * If <code>forcePJA</code> is set to <code>false</code>, this constructor tries to instantiate 
   * a default toolkit with <code>Toolkit.getDefaultToolkit ()</code> with different procedures
   * in the following order :
   * <ul><li><code>Toolkit.getDefaultToolkit ()</code> with the current state of the JVM. If 
   *     no Display is available on a UNIX machine and <code>PJAToolkit</code> not passed at JVM 
   *     startup as default toolkit class, this won't work because the default Toolkit  
   *     class of the JVM requires a valid Display.</li>
   *     <li><code>Toolkit.getDefaultToolkit ()</code> setting all system properties required 
   *     to instantiate a <code>PJAToolkit</code> class. This won't work for servlet servers using a 
   *     different class loader (and classpath) for system and each servlet user, 
   *     if pja.jar is not added to system classpath.</li>
   *     <li><code>Toolkit.getDefaultToolkit ()</code> setting all system properties required 
   *     to instantiate a <code>PJAToolkit</code> class and loading <code>java.awt.Toolkit</code>
   *     and <code>java.awt.GraphicsEnvironment</code> classes with a special class loader that uses
   *     servlet classpath. The servlet extending this class is instantiated a second time with  
   *     this special loader, allowing <code>java.awt.Toolkit</code> and the second servlet instance 
   *     to share the same class loader and classpath.<br>
   *     <code>Toolkit.getDefaultToolkit ()</code> will be able to find <code>PJAToolkit</code> in
   *     its classpath to load and instantiate this class.</li>
   * </ul>
   * If <code>forcePJA</code> is set to <code>true</code>, <code>PJAToolkit</code> and 
   * <code>PJAImage</code> will always be used. This disables all Java2D features but ensures the 
   * same results on all platforms where subclasses of <code>PJAServlet</code> are used.
   */
  public PJAServlet (boolean forcePJA) 
  { 
    this.forcePJA = forcePJA;
    this.calledServlet = this;
    testDefaultToolkit ();
  }

  protected boolean testDefaultToolkit ()
  {
    String oldToolkit             = getSystemProperty ("awt.toolkit", null);
    String oldGraphicsEnvironment = getSystemProperty ("java.awt.graphicsenv", null);
    String oldFontsPath           = getSystemProperty ("java.awt.fonts", null);
    String oldNoJava2d            = getSystemProperty ("com.eteks.awt.nojava2d", null);
    String oldUserHomeDir         = getSystemProperty ("user.home", null);
    
    boolean instanceLoadedWithPJAClassLoader = false;
    try
    {
      instanceLoadedWithPJAClassLoader = getClass ().getClassLoader ().getClass ().getName ().equals (PJAClassLoader.class.getName ());
    }
    catch (SecurityException e)
    {
    }
      
    // Current class loader may be the default one or redirectClassLoader
    if (instanceLoadedWithPJAClassLoader)
    {
      // We are using the clone of the servlet with PJAClassLoader
      setPJASystemProperties (forcePJA);
      // Instantiate the default Toolkit and GraphicsEnvironment with the
      // servlet instance that uses redirectClassLoader
      defaultToolkitAvailable = instantiateDefaultToolkit ();
      
      // defaultToolkitAvailable may still be false because of security options
      // (couldn't load awt librairy, forbidden access to properties)
    }
    else
    {
      // Try to instantiate the default Toolkit and GraphicsEnvironment
      // with current JVM classes : this may work if a Display is available
      // or if PJA Toolkit was "intalled" in java command line
      if (   !forcePJA
          && (defaultToolkitAvailable = instantiateDefaultToolkit ()))
        return defaultToolkitAvailable;
        
      setPJASystemProperties (forcePJA); 
      
      // Try to instantiate the default Toolkit and GraphicsEnvironment
      // with PJA classes and properties : this may work if no Display is available
      // or if pja.jar is accessible in system classpath
      if (!(defaultToolkitAvailable = instantiateDefaultToolkit ()))
      {
        // Instantiate PJA special class loader
        if (redirectClassLoader == null)
        {
          ClassLoader classLoader = PJAServlet.class.getClassLoader ();
          if (classLoader != null)
            try
            {
              redirectClassLoader = new PJAClassLoader (classLoader, getUserHomeDir ());
            }
            catch (SecurityException e)
            { } // There may be security options that forbids to use other class loaders
        }

        if (redirectClassLoader != null)
          try
          {
            // Instantiate a clone of this servlet with PJAClassLoader,
            // so Toolkit and GraphicsEnvironment, PJA toolkit and servlet 
            // are loaded by the same class loader 
            // Can't cast to PJAServlet the object returned by newInstance () :
            // the class of the operator (PJAServlet) is the class loaded by 
            // the default class loader, and the class of the object returned by newInstance ()
            // is the PJAServlet class loaded by redirectClassLoader loader.
            // These two PJAServlet classes are two different instances of java.lang.Class
            // and the JVM considers you can't cast an instance of one in an instance of
            // the other, even if they come from the same resource ! 
            calledServlet = redirectClassLoader.loadClass (getClass ().getName ()).newInstance ();
          }
          catch (ClassNotFoundException e)
          { } // Impossible : redirectClassLoader parent found it
          catch (InstantiationException e)
          { } // Impossible : JVM accepted to instantiate the same class
          catch (IllegalAccessException e)
          { } // Impossible : JVM found a default constructor
      }
    }
      
    // Restore old System properties
    setSystemProperties (oldToolkit, oldGraphicsEnvironment, oldFontsPath, oldNoJava2d, oldUserHomeDir);
    return defaultToolkitAvailable;
  }
  
  private boolean instantiateDefaultToolkit ()
  {
    try
    {
      // Try to instantiate the default Toolkit and GraphicsEnvironment
      Toolkit.getDefaultToolkit ();
        
      try
      {
        // Same as GraphicsEnvironment.getLocaleGraphicsEnvironment () 
        // but won't cause any problem during PJAServlet loading
        // (GraphicsEnvironment is only available in JDK >= 1.2)
        Class.forName ("java.awt.GraphicsEnvironment").getMethod ("getLocaleGraphicsEnvironment", null).invoke (null, null);
      }
      catch (ClassNotFoundException e)
      { } // It must be a JDK < 1.2
      catch (NoSuchMethodException e)
      { } // Other exceptions due to the use of reflection to call getLocaleGraphicsEnvironment
      catch (IllegalAccessException e)
      { }
      catch (IllegalArgumentException e)
      { }
      catch (InvocationTargetException e)
      { }
      
      return true;
    }
    catch (AWTError error)
    { }  // Thrown by Toolkit initialization
    catch (IllegalArgumentException e)
    { }  // If class name of default Toolkit is ""
    catch (SecurityException error)
    { }  // Toolkit may be forbidden to load
    catch (LinkageError error)
    { }  // Thrown by static initializer which requires awt library
    catch (InternalError error)
    { }  // Thrown by static initializers of GraphicsEnvironment which may require a Display
    catch (ClassCastException e)
    { }  // Thrown if super class of the loaded class aren't as required
    
    return false;
  }
  
  private void setPJASystemProperties (boolean forcePJA)
  {
    setSystemProperties ("com.PJAToolkit", "com.PJAGraphicsEnvironment",
                         getFontsPath (), String.valueOf (forcePJA), getUserHomeDir ());
  }
  
  private void setSystemProperties (String toolkit,
                                    String graphicsEnvironment,
                                    String fontsPath,
                                    String noJava2d,
                                    String userHomeDir)
  {
    try
    {
      // Change System properties usefull to instantiate default Toolkit and GraphicsEnvironment
      try 
      {
        System.setProperty ("awt.toolkit", toolkit);
        System.setProperty ("java.awt.graphicsenv", graphicsEnvironment);
        System.setProperty ("java2d.font.usePlatformFont", "false"); // Always set to false
        if (fontsPath != null)
          System.setProperty ("java.awt.fonts", fontsPath);
        if (noJava2d != null)
          System.setProperty ("com.eteks.awt.nojava2d", noJava2d);
        if (userHomeDir != null)
          System.setProperty ("user.home", userHomeDir);
      }
      catch (NoSuchMethodError e)
      {
        // This will happen with JDK < 1.2 which doesn't include the method System.setProperty ()
        Properties prop = System.getProperties ();
        prop.put ("awt.toolkit", toolkit);
        prop.put ("java.awt.graphicsenv", graphicsEnvironment);
        prop.put ("java2d.font.usePlatformFont", "false"); // Always set to false
        if (fontsPath != null)
          prop.put ("java.awt.fonts", fontsPath);
        if (noJava2d != null)
          prop.put ("com.eteks.awt.nojava2d", noJava2d);
        if (userHomeDir != null)
          prop.put ("user.home", userHomeDir);
        System.setProperties (prop);
      }
    }
    catch (SecurityException e)
    {
      // Exception thrown because the JVM didn't grant you access to getProperties () or setProperties ()
    }
  }
       
  /**
   * Override this method to return the fonts path where .pjaf files (JDK 1.1)
   * or True Type files (JDK >= 1.2)  can be found. If you want to return more than one 
   * directory separate them with <code>File.pathSeparator</code>.<br>
   * Note : This method is called from within <code>PJAServlet</code> constructor.
   * @return <code>PJAServlet</code> implementation returns <code>"."
   *         + File.pathSeparator + getSystemProperty ("user.dir")
   *         + File.pathSeparator + getSystemProperty ("java.home")</code>
   */
  public String getFontsPath ()
  {
    String fontsPath = ".";
    String userDir  = getSystemProperty ("user.dir", null);
    if (userDir != null)
      fontsPath += File.pathSeparator + userDir;
    String javaHome = getSystemProperty ("java.home", null);
    if (javaHome != null)
      fontsPath += File.pathSeparator + javaHome;
    return fontsPath;
  }

  /**
   * Override this method to return the user home directory
   * where the file <code>lib/font.properties</code> can be found.
   * This is usefull for JDK 1.2.<br>
   * Note : This method is called from within <code>PJAServlet</code> constructor.
   * @return <code>PJAServlet</code> implementation returns <code>getSystemProperty ("user.home")</code>
   */
  public String getUserHomeDir ()
  {
    return getSystemProperty ("user.home", "");
  }
 
  /**
   * Same as <code>System.getProperty (String prop, String default)</code> but
   * <code>System.getProperty ()</code> is called in a <code>try  catch</code> block
   * to catch <code>SecurityException</code> exceptions.
   * If a <code>SecurityException</code> exception is catched, a log is done and
   * <code>defaultValue</code> is returned.
   */ 
  public String getSystemProperty (String property, String defaultValue)
  {
    try
    {
      return System.getProperty (property, defaultValue);
    }
    catch (SecurityException e)
    { 
      log ("Couldn't retrieve system property " + property);
      return defaultValue;
    }
  }
  
  /**
   * This is a convenience method to know if default toolkit is available
   * or not. It avoids to run a <code>Toolkit.getDefaultToolkit ()</code>
   * and catch all the exeptions that may be thrown.
   * <code>java.awt.Font</code> class can't be instantiated if this method 
   * returns <code>false</code>.
   * @see com.eteks.awt.PJAImage
   */
  public boolean isDefaultToolkitAvailable ()
  {
    return defaultToolkitAvailable;
  }

  /**
   * Returns an image of <code>width x height</code> pixels using default toolkit.
   * If <code>isDefaultToolkitAvailable ()</code> returns <code>false</code>,
   * an instance of <code>com.PJAImage</code> is returned.
   */
  public Image createImage (int width, int height)
  {
    if (isDefaultToolkitAvailable ())
    {
      // Dummy frame to create an image
      Frame frame = new Frame ();
      // addNotify () is required to create an image (this will call Toolkit.getDefaultToolkit ())
      frame.addNotify ();
      return frame.createImage (width, height);
    }
    else
    {
      // v2.1.1 : Added the search in fonts path before creating the image
      if (graphicsManager == null)
      {
        graphicsManager = PJAGraphicsManager.getDefaultGraphicsManager ();
        String fontPath = getFontsPath ();
        if (!"".equals (fontPath))
        {
          // Parse directly the font path
          StringTokenizer parser = new StringTokenizer (fontPath, File.pathSeparator);
          while (parser.hasMoreTokens())
            graphicsManager.loadFonts (parser.nextToken());
        }
        else
          graphicsManager.loadFonts ("");
      }

      // Create an image with PJAImage directly (this one will be transparent)
      return new PJAImage (width, height);
    }
  }

  // Methods of GenericServlet redirected to calledServlet
  /**
   * Override <code>getServletInfoPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.getServletInfo ()</code> from <code>getServletInfoPJA ()</code>.
   */
  public final String getServletInfo () 
  { 
    try
    {
      // Same as return calledServlet.getServletInfoPJA ();
      return (String)calledServlet.getClass ().getMethod ("getServletInfoPJA", null).invoke (calledServlet, null);
    }
    // Exceptions due to the use of reflection to call method
    catch (NoSuchMethodException e)
    { } 
    catch (IllegalAccessException e)
    { }
    catch (IllegalArgumentException e)
    { }
    catch (InvocationTargetException e)
    { 
      if (e.getTargetException () instanceof RuntimeException)
        throw (RuntimeException)e.getTargetException ();
      else if (e.getTargetException () instanceof Error)
        throw (Error)e.getTargetException ();
    }
    throw new InternalError ("PJA error");
  }

  /**
   * Override this method instead of the final method <code>getServletInfo ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.getServletInfo ()</code>.
   */
  public String getServletInfoPJA () 
  { 
    return super.getServletInfo ();
  }

  /**
   * Override <code>initPJA (ServletConfig config)</code> instead of this <code>final</code> method.
   * Don't call <code>super.init (config)</code> from <code>initPJA (ServletConfig config)</code>.
   */
  public final void init (ServletConfig config) throws ServletException 
  {
    try
    {
      // Same as calledServlet.initPJA (config);
      calledServlet.getClass ().getMethod ("initPJA", new Class [] {ServletConfig.class}).invoke (calledServlet, new Object [] {config});
      return;
    }
    // Exceptions due to the use of reflection to call method
    catch (NoSuchMethodException e)
    { } 
    catch (IllegalAccessException e)
    { }
    catch (IllegalArgumentException e)
    { }
    catch (InvocationTargetException e)
    { 
      if (e.getTargetException () instanceof RuntimeException)
        throw (RuntimeException)e.getTargetException ();
      else if (e.getTargetException () instanceof Error)
        throw (Error)e.getTargetException ();
      else if (e.getTargetException () instanceof ServletException)
        throw (ServletException)e.getTargetException ();
    }
    throw new InternalError ("PJA error");    
  }

  /**
   * Override this method instead of the final method <code>init (ServletConfig config)</code>.
   * <code>PJAServlet</code> implementation calls <code>super.init (config)</code>.
   */
  public void initPJA (ServletConfig config) throws ServletException 
  {
    super.init (config);
  }

  /**
   * Override <code>initPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.init ()</code> from <code>initPJA ()</code>.
   */
  public final void init () throws ServletException 
  {
    try
    {
      // Same as calledServlet.initPJA ();
      calledServlet.getClass ().getMethod ("initPJA", null).invoke (calledServlet, null);
      return;
    }
    // Exceptions due to the use of reflection to call method
    catch (NoSuchMethodException e)
    { } 
    catch (IllegalAccessException e)
    { }
    catch (IllegalArgumentException e)
    { }
    catch (InvocationTargetException e)
    { 
      if (e.getTargetException () instanceof RuntimeException)
        throw (RuntimeException)e.getTargetException ();
      else if (e.getTargetException () instanceof Error)
        throw (Error)e.getTargetException ();
      else if (e.getTargetException () instanceof ServletException)
        throw (ServletException)e.getTargetException ();
    }
    throw new InternalError ("PJA error");    
  }
  
  /**
   * Override this method instead of the final method <code>init ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.init ()</code>.
   */
  public void initPJA () throws ServletException 
  {
    super.init ();
  }
  
  /**
   * Override <code>destroyPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.destroy ()</code> from <code>destroyPJA ()</code>.
   */
  public final void destroy () 
  {
    try
    {
      // Same as calledServlet.destroyPJA ();
      calledServlet.getClass ().getMethod ("destroyPJA", null).invoke (calledServlet, null);
      return;
    }
    // Exceptions due to the use of reflection to call method
    catch (NoSuchMethodException e)
    { } 
    catch (IllegalAccessException e)
    { }
    catch (IllegalArgumentException e)
    { }
    catch (InvocationTargetException e)
    { 
      if (e.getTargetException () instanceof RuntimeException)
        throw (RuntimeException)e.getTargetException ();
      else if (e.getTargetException () instanceof Error)
        throw (Error)e.getTargetException ();
    }
    throw new InternalError ("PJA error");    
  }
  
  /**
   * Override this method instead of the final method <code>destroy ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.destroy ()</code>.
   */
  public void destroyPJA () 
  {
    super.destroy ();
  }
  
  /**
   * Override <code>servicePJA (ServletRequest req, ServletResponse resp)</code> instead of this <code>final</code> method.
   * Don't call <code>super.service (req, resp)</code> from <code>servicePJA (ServletRequest req, ServletResponse resp)</code>.
   */
  public final void service (ServletRequest req, ServletResponse resp)
	throws ServletException, IOException
  {
    try
    {
      // Same as calledServlet.servicePJA (req, resp);
      calledServlet.getClass ().getMethod ("servicePJA", new Class [] {ServletRequest.class, ServletResponse.class}).invoke (calledServlet, new Object [] {req, resp});
      return;
    }
    // Exceptions due to the use of reflection to call method
    catch (NoSuchMethodException e)
    { } 
    catch (IllegalAccessException e)
    { }
    catch (IllegalArgumentException e)
    { }
    catch (InvocationTargetException e)
    { 
      if (e.getTargetException () instanceof RuntimeException)
        throw (RuntimeException)e.getTargetException ();
      else if (e.getTargetException () instanceof Error)
        throw (Error)e.getTargetException ();
      else if (e.getTargetException () instanceof ServletException)
        throw (ServletException)e.getTargetException ();
      else if (e.getTargetException () instanceof IOException)
        throw (IOException)e.getTargetException ();
    }
    throw new InternalError ("PJA error");            
  }

  /**
   * Override this method instead of the final method <code>service (ServletRequest req, ServletResponse resp)</code>.
   * <code>PJAServlet</code> implementation calls <code>super.service (req, resp)</code>.
   */
  public void servicePJA (ServletRequest req, ServletResponse resp)
	throws ServletException, IOException
  {
    super.service (req, resp);    
  }

  // Methods of HttpServlet redirected to redirectServlet
  /**
   * Override <code>doGetPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.doGet (req, resp)</code> from <code>doGetPJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void doGet (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    // Same as calledServlet.doGetPJA (req, resp);
    HttpMethodInvoke ("doGetPJA", req, resp);
  }

  /**
   * Override this method instead of the final method <code>doGet ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.doGet ()</code>.
   */
  public void doGetPJA (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    super.doGet (req, resp);
  }

  /**
   * Override <code>doPostPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.doPost (req, resp)</code> from <code>doPostPJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void doPost (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    // Same as calledServlet.doPostPJA (req, resp);
    HttpMethodInvoke ("doPostPJA", req, resp);
  }

  /**
   * Override this method instead of the final method <code>doPost ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.doPost ()</code>.
   */
  public void doPostPJA (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    super.doPost (req, resp);
  }

  /**
   * Override <code>doPutPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.doPut (req, resp)</code> from <code>doPutPJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void doPut (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    // Same as calledServlet.doPutPJA (req, resp);
    HttpMethodInvoke ("doPutPJA", req, resp);
  }

  /**
   * Override this method instead of the final method <code>doPut ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.doPut ()</code>.
   */
  public void doPutPJA (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    super.doPut (req, resp);
  }

  /**
   * Override <code>doDeletePJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.doDelete (req, resp)</code> from <code>doDeletePJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void doDelete (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    // Same as calledServlet.doDeletePJA (req, resp);
    HttpMethodInvoke ("doDeletePJA", req, resp);
  }

  /**
   * Override this method instead of the final method <code>doDelete ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.doDelete ()</code>.
   */
  public void doDeletePJA (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    super.doDelete (req, resp);
  }

  /**
   * Override <code>doOptionsPJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.doOptions (req, resp)</code> from <code>doOptionsPJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void doOptions (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    // Same as calledServlet.doOptionsPJA (req, resp);
    HttpMethodInvoke ("doOptionsPJA", req, resp);
  }
  
  /**
   * Override this method instead of the final method <code>doOptions ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.doOptions ()</code>.
   */
  public void doOptionsPJA (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    super.doOptions (req, resp);
  }
  
  /**
   * Override <code>doTracePJA ()</code> instead of this <code>final</code> method.
   * Don't call <code>super.doTrace (req, resp)</code> from <code>doTracetPJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void doTrace (HttpServletRequest req, HttpServletResponse resp) 
	throws ServletException, IOException
  {
    // Same as calledServlet.doTracePJA (req, resp);
    HttpMethodInvoke ("doTracePJA", req, resp);
  }		

  /**
   * Override this method instead of the final method <code>doTrace ()</code>.
   * <code>PJAServlet</code> implementation calls <code>super.doTrace ()</code>.
   */
  public void doTracePJA (HttpServletRequest req, HttpServletResponse resp) 
	throws ServletException, IOException
  {
    super.doTrace (req, resp);
  }		

  /**
   * Override <code>servicePJA (HttpServletRequest req, HttpServletResponse resp)</code> instead of this <code>final</code> method.
   * Don't call <code>super.service (req, resp)</code> from <code>servicePJA (HttpServletRequest req, HttpServletResponse resp)</code>.
   */
  protected final void service (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    // Same as calledServlet.servicePJA (req, resp);
    HttpMethodInvoke ("servicePJA", req, resp);
  }
  
  /**
   * Override this method instead of the final method <code>service (HttpServletRequest req, HttpServletResponse resp)</code>.
   * <code>PJAServlet</code> implementation calls <code>super.service (req, resp)</code>.
   */
  public void servicePJA (HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException
  {
    super.service (req, resp);    
  }
  
  private void HttpMethodInvoke (String method, HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException
  {
    try
    {
      // Same as method (req, resp);
      calledServlet.getClass ().getMethod (method, new Class [] {HttpServletRequest.class, HttpServletResponse.class}).invoke (calledServlet, new Object [] {req, resp});
      return;
    }
    // Exceptions due to the use of reflection to call method
    catch (NoSuchMethodException e)
    { } 
    catch (IllegalAccessException e)
    { }
    catch (IllegalArgumentException e)
    { }
    catch (InvocationTargetException e)
    { 
      if (e.getTargetException () instanceof RuntimeException)
        throw (RuntimeException)e.getTargetException ();
      else if (e.getTargetException () instanceof Error)
        throw (Error)e.getTargetException ();
      else if (e.getTargetException () instanceof ServletException)
        throw (ServletException)e.getTargetException ();
      else if (e.getTargetException () instanceof IOException)
        throw (IOException)e.getTargetException ();
    }
    throw new InternalError ("PJA error");            
  }

  /**
   * This classLoader loads itself <code>java.awt.Toolkit</code> and 
   * <code>java.awt.GraphicsEnvironment</code> classes.
   * to ensure that these classes share servlet's classpath 
   * where PJA classes (pja.jar) could be.
   */
  private static class PJAClassLoader extends ClassLoader 
  {
    private ClassLoader parent;
    private String      classResourceDir;
    private Vector      checkedClasses = new Vector ();
    private String      systemClasspath;    
    
    public PJAClassLoader (ClassLoader parent, String classResourceDir) throws SecurityException
    {      
      super ();
      // Obliged to store parent value because parent class loader appeared only in JDK 1.2
      this.parent = parent; 
      this.classResourceDir = classResourceDir;
      systemClasspath = System.getProperty ("sun.boot.class.path");
      // Boot classpath didn't exist before JDK 1.2
      if (systemClasspath == null)
        systemClasspath = System.getProperty ("java.class.path");
      else
        // v2.3.2 : Added File.pathSeparator
        systemClasspath += File.pathSeparator + System.getProperty ("java.class.path");
    }

    protected Class loadClass (String name, boolean resolve) throws ClassNotFoundException
    {
      Class loadedClass = findLoadedClass (name);
      if (loadedClass == null)
        try
        {
          loadedClass = findSystemClassInternal (name);
        }
        catch (ClassNotFoundException e)
        {
          loadedClass = findClass (name);
        }        
      
      if (   loadedClass != null
          && resolve)
        resolveClass (loadedClass);
        
      return loadedClass;
    }

    protected Class findClass (String className) throws ClassNotFoundException 
    {
      try
      {
        return loadClassFromFile (className, false);
      }
      catch (ClassNotFoundException e)
      {
        // This could happen if classes can't be read directly from
        // their files (with coded class files for example)
        if (parent != null)
          return parent.loadClass (className);
        else
          return super.findClass (className);
      }
    }

    private synchronized Class findSystemClassInternal (String className) throws ClassNotFoundException
    {
      // This first call is made just to be sure className is a system class
      Class systemClass = findSystemClass (className);

      // Define with this class loader any class that may use (or are) Toolkit or GraphicsEnvironment classes
      // so all these classes can share servlet's classpath and find PJA
      Class loadedClass = findLoadedClass (className);      
      if (   loadedClass == null
          && !checkedClasses.contains (className))
      {
        checkedClasses.addElement (className);
          
        byte [] classCode = loadClassCodeFromFile (className, false);
        if (isClassToolkitSensitive (className, classCode))
          try
          {
            loadedClass = defineClass (className, classCode, 0, classCode.length);

            // If SunGraphicsEnvironment class declares validPropertiesFile () method, load it with local classpath
            // so people can provide a patch containing the correct version of SunGraphicsEnvironment that doesn't crash JVM
            if ("sun.java2d.SunGraphicsEnvironment".equals (className))
              try
              {
                loadedClass.getDeclaredMethod ("validPropertiesFile", new Class [] {String.class, String.class});
                loadedClass = loadClassFromFile (className, true);
              }
              catch (NoSuchMethodException e)
              { } // No validPropertiesFile () == no problems
              catch (ClassNotFoundException e)
              { } // Will try with default SunGraphicsEnvironment class
              
            return loadedClass;
          }
          catch (ClassFormatError e)
          { } // Will try with findSystemClass ()
      }
      
      return systemClass;
    }      
          
    
    private synchronized Class loadClassFromFile (String className, boolean resourceFromFile) throws ClassNotFoundException
    {
      byte [] classCode = loadClassCodeFromFile (className, resourceFromFile);
      return defineClass (className, classCode, 0, classCode.length);        
    }
 
    private byte [] loadClassCodeFromFile (String className, boolean resourceFromFile) throws ClassNotFoundException
    {
      InputStream input = null;
      try
      {
        // If classes come from file let say they are based at user home 
        input = resourceFromFile 
                  ? new FileInputStream (new File (classResourceDir, className.replace ('.', File.separatorChar) + ".class"))
                  : getClassFromSystemClasspath (className);
        // If not in classpath try from local resource
        if (   input == null 
            && !resourceFromFile) 
          input = getResourceAsStream (className.replace ('.', '/') + ".class");
        if (input == null)
          throw new ClassNotFoundException (className);
        return readStream (new BufferedInputStream (input));
      }
      catch (IOException e)
      {
      }
      finally
      {
        try
        {
          if (input != null)
            input.close ();
        }
        catch (IOException e)
        {
        }
      }
      throw new ClassNotFoundException (className);
    }
    
    private InputStream getClassFromSystemClasspath (String className) throws ClassNotFoundException
    {
      InputStream classStream = null;
      ZipFile     zipFile     = null;
      InputStream zipStream   = null;
      // This method replaces getSystemResourceAsStream () that can't work with class files  
      // It will work as long as classpath properties are accessible and not modified since JVM start
      StringTokenizer parser = new StringTokenizer (systemClasspath, File.pathSeparator);
      while (parser.hasMoreTokens () && classStream == null)
        try
        {
          File file = new File (parser.nextToken ());
          if (file.isDirectory ())
            // Try to read class file from directory
            classStream = new FileInputStream (new File (file, className.replace ('.', File.separatorChar) + ".class"));
          else
          {
            // Try to read class file from zip file
            zipFile = new ZipFile (file);
            ZipEntry entry = zipFile.getEntry (className.replace ('.', '/') + ".class");
            if (   entry != null
                && (zipStream = zipFile.getInputStream (entry)) != null)
              classStream = new ByteArrayInputStream (readStream (zipStream));
          }
        }
        catch (IOException e)
        {
        }
        finally
        {
          if (zipStream != null)
            try
            {
              zipStream.close ();
              zipStream = null;
            }
            catch (IOException ignored)
            {
            }
            
          if (zipFile != null)
            try
            {
              zipFile.close ();
              zipFile = null;
            }
            catch (IOException ignored)
            {
            }
        }
        
      return classStream;
    }
    
    private boolean isClassToolkitSensitive (String className, byte [] classCode)
    {
      // Returns true if class className may use java.awt.Toolkit class
      return    className.startsWith ("java.awt")
             || className.startsWith ("javax.swing")
             || doesClassCodeContainClassName (classCode, "java.awt.Toolkit")
             || doesClassCodeContainClassName (classCode, "java.awt.GraphicsEnvironment");          
    }
    
    private boolean doesClassCodeContainClassName (byte [] classCode, String className)
    {
      // It's a short cut that may be a little tricky, but as long as it works...
      return new String (classCode, 0).indexOf (className) >= 0;
    }
    
    private byte [] readStream (InputStream input) throws IOException
    {
      byte data     [] = new byte [0];
      byte readData [] = new byte [1024];
      int  nbReadBytes;

      for (int current = 0; (nbReadBytes = input.read (readData)) != -1; current += nbReadBytes)
      {
        if (data.length < current + nbReadBytes)
        {
          byte oldData [ ] = data;
          data = new byte [current + nbReadBytes];
          System.arraycopy (oldData, 0, data, 0, oldData.length);
        }
      
        System.arraycopy (readData, 0, data, current, nbReadBytes);
      }
     
      return data;
    }

    protected URL findResource (String name) 
    {
      return parent != null ? parent.getResource (name) : super.findResource (name);
    }

    public URL getResource (String name) 
    {
      return parent != null ? parent.getResource(name) : super.getResource (name);
    }

    public InputStream getResourceAsStream (String name) 
    {
      return parent != null ? parent.getResourceAsStream (name) : super.getResourceAsStream (name);
    }

    // Impossible to override protected methods with a call to parent implementation
    // Hope it won't cause problem
  }
}
