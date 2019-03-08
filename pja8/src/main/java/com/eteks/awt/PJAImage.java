/*
 * @(#)PJAImage.java   08/06/2000
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
import java.awt.image.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * Pure Java AWT Image implementation. This image stores in a buffer an offscreen
 * image using default RGB color model or another indexed color model.
 *
 * @version   2.3
 * @author    Emmanuel Puybaret
 * @since     PJA1.0
 */
public class PJAImage extends Image
{
  private int        width = -1;
  private int        height = -1;
  private int        availableInfo;
  // v2.3 (thanks to Fernando Echeverria)
  // pixels can be stored in an array of byte or int
  private Object     pixels;
  private ColorModel colorModel = null;
  private Hashtable  properties = new Hashtable ();

  private boolean       productionStarted;
  private ImageProducer initProducer;

  private PJAGraphicsManager manager;

  {
    manager = PJAGraphicsManager.getDefaultGraphicsManager ();
    // v2.0 : default color model is obtained from PJAGraphicsManager
    colorModel = manager.getColorModel ();
  }

  /**
   * Creates an initial image with a producer.
   * @param producer  An instance of a class that implements <code>ImagePoducer</code>.
   */
  public PJAImage (ImageProducer producer)
  {
    this.initProducer = producer;
  }

  /**
   * Creates an empty image of <code>width x height</code> pixels. The image is created transparent.
   * @see com.eteks.awt.PJAToolkit
   */
  public PJAImage (int width, int height)
  {
    this.width  = width;
    this.height = height;
    // pixels buffer is created only when needed
    // pixels = new int [width * height];
    productionStarted = true;

    availableInfo =   ImageObserver.WIDTH
                    | ImageObserver.HEIGHT
                    | ImageObserver.PROPERTIES
                    | ImageObserver.ALLBITS;
  }

  /** 
   * Creates an empty image of <code>width x height</code> pixels. The image is created transparent 
   * with <code>colorModel</code> color model. 
   * @since  PJA2.3
   */
  public PJAImage (int width, 
                   int height, 
                   ColorModel colorModel)
  {
    this (width, height);
    this.colorModel = colorModel;
  }

  /**
   * <code>java.awt.Image</code> implementation.
   * @see Image
   */
  public Object getProperty (String name, ImageObserver observer)
  {
    if ((availableInfo & ImageObserver.PROPERTIES) == 0)
      prepare (observer);
    return properties.get (name);
  }

  /**
   * <code>java.awt.Image</code> implementation.
   * @see Image
   */
  public int getWidth (ImageObserver observer)
  {
    if ((availableInfo & ImageObserver.WIDTH) == 0)
      prepare (observer);
    return getWidth ();
  }

  /**
   * <code>java.awt.Image</code> implementation.
   * @see Image
   */
  public int getHeight (ImageObserver observer)
  {
    if ((availableInfo & ImageObserver.HEIGHT) == 0)
      prepare (observer);
    return getHeight ();
  }

  /**
   * <code>java.awt.Image</code> implementation.
   * @see Image
   */
  public ImageProducer getSource ()
  {
    return new NBProducer ();
  }

  /**
   * <code>java.awt.Image</code> implementation. 
   * Contrary to JDK default behavior, PJA allows to retrieve
   * an instance of <code>Graphics</code> on images created with a producer.
   * This enables to draw in transparent images.
   * @see Image
   */
  public Graphics getGraphics ()
  {
    // Complete image production before drawing in it
    sync ();

    if ((availableInfo & ImageObserver.ERROR) != 0)
      return null;
    else   
      return new PJAGraphics (this);
  }

  /** 
   * Returns this image's color model 
   * @return the image's color model. <code>null</code> means the default color model.
   *
   * @since  PJA2.3
   */
  public ColorModel getColorModel()
  {
    return colorModel;
  }

  /**
   * <code>java.awt.Image</code> implementation.
   * @see Image
   */
  public void flush ()
  {
    // v1.1 : corrected implementation (inspired from sun.awt.image.Image flush () implementation)
    // In respect of JVM behavior, only images data built upon files should be discarded (filtered or not)
    // but don't know how to keep track of filtered images made upon files
    // (do nothing on images built with a MemoryImageSource instance or by createImage (width, height))
    if (   initProducer != null
        && !(initProducer instanceof MemoryImageSource)
        && !(initProducer instanceof FilteredImageSource))
      synchronized (this)
      {
        pixels  = null;
        availableInfo |= ImageObserver.ERROR;
      }
  }

  protected int check (ImageObserver observer)
  {
    // v2.0 : Verify if observer is null
    if (observer != null)
      observer.imageUpdate (this, availableInfo, 0, 0, width, height);
    return availableInfo;
  }

  protected boolean prepare (ImageObserver observer)
  {
    if (!productionStarted)
      loadInitImage (false, observer);
    else
      if ((availableInfo & ImageObserver.ERROR) != 0)
      {
        if (observer != null)
          observer.imageUpdate (this, availableInfo, -1, -1, -1, -1);
      }
      else
        new NBProducer (observer).startProduction (null);

    return (availableInfo & ImageObserver.ALLBITS) != 0;
  }

  protected int getWidth ()
  {
    return width;
  }

  protected int getHeight ()
  {
    return height;
  }

  /**
   * Gets the array used to store the pixels of this image.
   *
   * @return an array of <code>int</code> or <code>null</code> if the array
   *         contains <code>byte</code> or if the image was flushed.
   */
  protected int [] getPixels ()
  {
    Object pixelsArray = getPixelsArray ();
    return pixelsArray instanceof int []
             ? (int [])pixelsArray 
             : null;
  }

  /**
   * Gets the array used to store the pixels of this image.
   *
   * @return an array of <code>int</code> or <code>byte</code>.
   * @since  PJA2.3
   */
  protected Object getPixelsArray ()
  {
    if (   pixels == null
        && width >= 0
        && height >= 0
        && (availableInfo & ImageObserver.ERROR) == 0)
    {
      // v2.3 : Added support for 8 bit color images
      if (   colorModel == null
          || !(colorModel instanceof IndexColorModel))
        // Should take into account the size required to store each pixel with model.getPixelSize ()
        // but for the moment only default RGB model is used
        pixels = new int [width * height];
      else
        pixels = new byte [width * height];
    }
      
    return pixels;
  }
  
  /**
   * Gets the color at the point <code>(x,y)</code>.
   *
   * @param  x     the point coordinates.
   * @param  y 
   * @return the color of the point in default RGB model.
   * @since  PJA2.0
   */
  protected int getPixelColor (int x, int y)
  {
    if (   (availableInfo & ImageObserver.ERROR) != 0
        || pixels == null
        || x < 0
        || x >= width
        || y < 0
        || y >= height)
      return 0;
      
    // v2.3 : Added support for 8 bit color images
    Object pixelsArray = getPixelsArray ();
    if (pixelsArray instanceof int [])
      return ((int [])pixelsArray) [x + y * width];
    else if (pixelsArray instanceof byte [])
      return colorModel.getRGB (((byte [])pixelsArray) [x + y * width] & 0xFF);
    else
      return 0;
  }

  /**
   * Sets the color at the point <code>(x,y)</code>.
   *
   * @param  x     the point coordinates.
   * @param  y 
   * @param  ARGB  the color of the point in default RGB model.
   * @since  PJA2.0
   */
  protected void setPixelColor (int x, int y, int ARGB)
  {
    if (   x >= 0
        && x < width
        && y >= 0
        && y < height)
    {
      // v2.3 : Added support for 8 bit color images
      // Get the buffer with getPixelsArray () to ensure that pixels array is available
      Object pixelsArray = getPixelsArray ();
      
      if (pixelsArray instanceof int [])
        ((int [])pixelsArray) [x + y * width] = ARGB;
      else if (pixelsArray instanceof byte [])
        ((byte [])pixelsArray) [x + y * width] = 
                    (byte)manager.getClosestColorIndex ((IndexColorModel)colorModel, ARGB);
    }
  }
  
  /**
   * Synchronizes the image loading.
   * @since  PJA2.0
   */
  public void sync ()
  {
    loadInitImage (true, null);
  }
  
  private synchronized void loadInitImage (boolean wait,
                                           final ImageObserver observer)
  {
    if (!productionStarted)
    {
      // Loads asynchronously initial image if not yet done
      new Thread ()
        {
          final ImageProducer producer = initProducer;
          public void run ()
          {
            producer.startProduction (new PJAConsumer (producer, observer));
          }    
        }.start ();

      productionStarted = true;
      // v1.2 : Moved wait () out of if (!productionStarted) block
      //        because loadInitImage () can called first with parameter wait == false (by prepareImage ())
      //        and then can be called again with parameter wait == true. In that case, 
      //        current thread must be stopped to complete image download.
    }
      
    try
    {
      // If image isn't downloaded yet
      if (   (availableInfo & ImageObserver.ERROR) == 0
          && (availableInfo & ImageObserver.ALLBITS) == 0)
        // Wait the producer notifies us when image is ready
        if (wait)
          wait ();
    }
    catch (InterruptedException e)
    {
      availableInfo = ImageObserver.ABORT | ImageObserver.ERROR;
    }
  }

  // ImageProducer implementation.
  // This producer is used to forward data to ImageConsumer instances and
  // to inform ImageObserver instances.
  private class NBProducer implements ImageProducer
  {
    private Vector        consumers = new Vector ();
    private ImageObserver observer;

    public NBProducer ()
    {
    }

    public NBProducer (ImageObserver observer)
    {
      this.observer = observer;
    }

    public synchronized void addConsumer (ImageConsumer ic)
    {
      if (   ic != null
          && isConsumer (ic))
        return;

      if ((availableInfo & ImageObserver.ERROR) == 0)
      {
        // v1.1 : forgot to check ic
        if (ic != null)
          consumers.addElement (ic);

        // Complete image production before drawing in it
        sync ();
      }

      synchronized (PJAImage.this)
      {
        if ((availableInfo & ImageObserver.ERROR) != 0)
        {
          if (ic != null)
            ic.imageComplete (ImageConsumer.IMAGEERROR);
          if (observer != null)
            observer.imageUpdate (PJAImage.this, availableInfo, -1, -1, -1, -1);
        }
        else
        {
          if (ic != null)
          {
            ic.setDimensions (width, height);
            ic.setHints (  ImageConsumer.SINGLEPASS
                         | ImageConsumer.SINGLEFRAME
                         | ImageConsumer.TOPDOWNLEFTRIGHT);
            ic.setProperties (properties);
            if (colorModel != null)
              ic.setColorModel (colorModel);
            if (pixels instanceof int [])
              ic.setPixels (0, 0, width, height, colorModel, (int [])pixels, 0, width);
            else
              ic.setPixels (0, 0, width, height, colorModel, (byte [])pixels, 0, width);
            ic.imageComplete (ImageConsumer.STATICIMAGEDONE);
          }

          if (observer != null)
            observer.imageUpdate (PJAImage.this, availableInfo, 0, 0, width, height);
        }
      }
    }

    public synchronized void removeConsumer (ImageConsumer ic)
    {
      consumers.removeElement (ic);
    }

    public synchronized boolean isConsumer (ImageConsumer ic)
    {
      return consumers.contains (ic);
    }

    public void startProduction (ImageConsumer ic)
    {
      addConsumer (ic);
    }

    public void requestTopDownLeftRightResend (ImageConsumer ic)
    {
      // Useless, already sent in that order
    }
  }

  // ImageConsumer implementation
  private class PJAConsumer implements ImageConsumer
  {
    private ImageProducer producer;
    private ImageObserver observer;

    public PJAConsumer (ImageProducer producer, ImageObserver observer)
    {
      this.producer = producer;
      this.observer = observer;
    }

    public void setDimensions (int width, int height)
    {
      PJAImage.this.width  = width;
      PJAImage.this.height = height;
      availableInfo |= ImageObserver.WIDTH | ImageObserver.HEIGHT;
      if (observer != null)
        observer.imageUpdate (PJAImage.this, availableInfo, 0, 0, width, height);
    }

    public void setHints (int hints)
    {
    }

    public void setProperties (Hashtable props)
    {
      properties = props;
      availableInfo |= ImageObserver.PROPERTIES;
      if (observer != null)
        observer.imageUpdate (PJAImage.this, availableInfo, 0, 0, width, height);
    }

    public void setColorModel (ColorModel model)
    {
    }

    public void setPixels (int x, int y, int width, int height,
                           ColorModel model, byte pixels [], int offset, int scansize)
    {
      synchronized (PJAImage.this)
      {
        // v1.1 : check if image not flushed
        if ((availableInfo & ImageObserver.ERROR) == 0)
        {
          Object pixelsArray = getPixelsArray ();
          int  [] intPixels  = null;
          byte [] bytePixels = null;
          int     lastARGB   = 0;
          byte    lastIndex  = (byte)-1;
          
          if (pixelsArray instanceof int [])
            intPixels = (int [])pixelsArray;
          else
            bytePixels = (byte [])pixelsArray;
                      
          for (int row = 0, destRow = y * PJAImage.this.width;
               row < height;
               row++, destRow += PJAImage.this.width)
          {
            int rowOff = offset + row * scansize;
            for (int col = 0; col < width; col++)
              // v2.3 : Added support for 8 bit color images
              if (intPixels != null)
                // v1.2 : Added & 0xFF to disable sign bit
                intPixels [destRow + x + col] = model.getRGB (pixels [rowOff + col] & 0xFF);
              else if (colorModel == model)
                bytePixels [destRow + x + col] = pixels [rowOff + col];
              else 
              {
                int ARGB = model.getRGB (pixels [rowOff + col] & 0xFF);
                if (   lastIndex == -1
                    || lastARGB != ARGB)
                {                  
                  // Keep track of the last color
                  lastARGB  = ARGB;
                  lastIndex = (byte)manager.getClosestColorIndex ((IndexColorModel)colorModel, lastARGB);
                }
                bytePixels [destRow + x + col] = lastIndex;                      
              }
          }
          availableInfo |= ImageObserver.SOMEBITS;
        }
      }
    }

    public void setPixels (int x, int y, int width, int height,
		           ColorModel model, int pixels [], int offset, int scansize)
    {
      synchronized (PJAImage.this)
      {
        // v1.1 : check if image not flushed
        if ((availableInfo & ImageObserver.ERROR) == 0)
        {
          Object pixelsArray = getPixelsArray ();
          int  [] intPixels  = null;
          byte [] bytePixels = null;
          int     lastARGB   = 0;
          byte    lastIndex  = (byte)-1;
          
          if (pixelsArray instanceof int [])
            intPixels = (int [])pixelsArray;
          else
            bytePixels = (byte [])pixelsArray;
            
          for (int row = 0, destRow = y * PJAImage.this.width;
               row < height;
               row++, destRow += PJAImage.this.width)
          {
            int rowOff = offset + row * scansize;
            for (int col = 0; col < width; col++)
            {
              int ARGB = model == null
                           ? pixels [rowOff + col]
                           : model.getRGB (pixels [rowOff + col]);
              // v2.3 : Added support for 8 bit color images
              if (intPixels != null)
                // If model == null, consider it's the default RGB model
                intPixels [destRow + x + col] = ARGB;
              else 
              {
                if (   lastIndex == -1
                    || lastARGB != ARGB)
                {                  
                  // Keep track of the last color
                  lastARGB  = ARGB;
                  lastIndex = (byte)manager.getClosestColorIndex ((IndexColorModel)colorModel, lastARGB);
                }
                bytePixels [destRow + x + col] = lastIndex;
                      
              }
            }                                                
          }
          availableInfo |= ImageObserver.SOMEBITS;
        }
      }
    }

    public void imageComplete (int status)
    {
      synchronized (PJAImage.this)
      {
        if (status == IMAGEERROR)
          availableInfo = ImageObserver.ERROR;
        else if (status == IMAGEABORTED)
          availableInfo = ImageObserver.ABORT | ImageObserver.ERROR;
        else
        {
          availableInfo &= ~ImageObserver.SOMEBITS;
          if (status == STATICIMAGEDONE)
            availableInfo |= ImageObserver.ALLBITS;
          else if (status == SINGLEFRAMEDONE)
            // This implementation manages only one frame
            availableInfo |= ImageObserver.ALLBITS /*ImageObserver.FRAMEBITS*/;
        }

        if (status == IMAGEERROR || status == IMAGEABORTED)
          pixels = null;

        producer.removeConsumer (this);
        if (observer != null)
          if ((availableInfo & ImageObserver.ERROR) != 0)
            observer.imageUpdate (PJAImage.this, availableInfo, -1, -1, -1, -1);
          else
            observer.imageUpdate (PJAImage.this, availableInfo, 0, 0, width, height);

        // v1.2 : Moved synchronized to method start
        PJAImage.this.notifyAll ();
      }
    }
  }
}