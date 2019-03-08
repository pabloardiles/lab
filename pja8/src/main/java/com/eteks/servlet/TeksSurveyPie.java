/*
 * @(#)TeksSurveyPie.java   05/16/2000
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
 *
 * ***********************************************************************
 *
 * Copyright (C) 1996,1998 by Jef Poskanzer <jef@acme.com>. All rights reserved.
 * (for the use of Acme.JPM.Encoders.GifEncoder)
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR AND CONTRIBUTORS ``AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHOR OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 *
 * Visit the ACME Labs Java page for up-to-date versions of this and other
 * fine Java utilities: http://www.acme.com/java/
 */
package com.eteks.servlet;

import Acme.JPM.Encoders.GifEncoder;
import com.eteks.awt.servlet.PJAServlet;
import com.eteks.filter.Web216ColorsFilter;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.image.FilteredImageSource;
import java.io.*;
import java.util.*;

/**
 * This servlet manages in a simple way a dynamic survey and returns the pie of the survey.
 * It can be called in either ways :
 * <UL><LI><code>.../servlet/com.TeksSurveyPie?survey=mySurvey&answer=myAnswer</code>
 * adds the answer <code>myAnswer</code> to the survey <code>mySurvey</code>,
 * and then returns the pie of the current state of <code>mySurvey</code>.
 * <LI><code>.../servlet/com.TeksSurveyPie?survey=mySurvey</code>
 * simply returns the pie of the current state of <code>mySurvey</code>.</UL>
 * <P>To be platform independant, the servlet uses <code>com.PJAServlet</code> as super class,
 * to have at disposal an image into which graphics operation can be performed.<BR>
 * <code>com.eteks.awt.PJAServlet</code> class and depending classes of <code>com.eteks.awt</code> packages 
 * must be in the servlet engine classpath, and at least one .pjaf font file (Pure Java AWT Font) must exist 
 * in the user directory or in the directory set in the <code>java.awt.fonts</code> system property, 
 * if JVM version <= 1.1.<BR>
 *
 * @version   2.1
 * @author    Emmanuel Puybaret
 * @see       PJAServlet
 * @since     PJA1.0
 */
public class TeksSurveyPie extends PJAServlet
{
  Properties  surveysParam = new Properties ();
  String      fontsDir       = ""; // Default value for "java.awt.fonts" .pjaf fonts directory initParameter
  String      surveysFile    = fontsDir + File.separator + "survey.txt";  // Default value for survey file initParameter

  // Colors used to fill the pie
  final Color  colors [] = {Color.blue,
                            Color.green,
                            Color.red,
                            Color.cyan,
                            Color.magenta,
                            Color.gray,
                            Color.yellow,
                            Color.pink,
                            Color.orange,
                            Color.white};
  final Color  penColor  =  Color.black;

  public void initPJA (ServletConfig config) throws ServletException
  {
    // Store the ServletConfig object and log the initialization
    super.initPJA (config);

    // Retrieves surveys file path
    String param = getInitParameter ("surveysFile");
    if (param != null)
      surveysFile = param;

    param = getInitParameter ("fontsDir");
    if (param != null)
      fontsDir = param;

    FileInputStream in = null;
    try
    {
      in = new FileInputStream (surveysFile);
      surveysParam.load (in);
    }
    catch (IOException e)
    { } // Empty properties
    finally
    {
      try
      {
        if (in != null)
          in.close ();
      }
      catch (IOException ex)
      { }
    }
  }

  public void destroyPJA ()
  {
    try
    {
      surveysParam.save (new FileOutputStream (surveysFile), "Survey file");
    }
    catch (IOException e)
    { } // Properties can't be saved
  }

  public String getFontsPath ()
  {
    return   super.getFontsPath () + File.pathSeparator 
           + fontsDir; 
  }

  public void doPostPJA (HttpServletRequest  request,
                     HttpServletResponse response)
               throws ServletException, IOException
  {
    doGetPJA (request, response);
  }

  public void doGetPJA (HttpServletRequest  request,
                        HttpServletResponse response)
               throws ServletException, IOException
  {
    String    survey      = request.getParameter ("survey");     // Survey name
    String    answer      = request.getParameter ("answer");     // Proposed answer
    String    paramWidth  = request.getParameter ("width");
    String    paramHeight = request.getParameter ("height");

    int width  = paramWidth == null ? 200 : Integer.parseInt (paramWidth);
    int height = paramHeight  == null ? 100 : Integer.parseInt (paramHeight);

    // v1.1 : Changed code to enable image creation
    // even if PJAToolkit couldn't be loaded by Toolkit.getDefaultToolkit ()
    Image image =  createImage (width, height);

    if (survey == null)
      survey = "";

    if (answer != null)
    {
      String key = survey + "_" + answer;
      String value = surveysParam.getProperty (key);
      if (value != null)
        // If the answer already exists, increase its value
        surveysParam.put (key, String.valueOf (Integer.parseInt (value) + 1));
      else
      {
        String surveyAnswers = surveysParam.getProperty (survey);
        // Add this answer to the other ones
        if (surveyAnswers == null)
          surveysParam.put (survey, answer);
        else
          surveysParam.put (survey, surveyAnswers + "," + answer);

        surveysParam.put (key, "1");
      }
    }

    Hashtable answers       = new Hashtable ();
    Vector    sortedAnswers = new Vector ();
    synchronized (surveysParam)
    {
      String surveyAnswers = surveysParam.getProperty (survey);
      if (surveyAnswers != null)
        for (StringTokenizer tokens = new StringTokenizer (surveyAnswers, ",");
             tokens.hasMoreTokens (); )
        {
          String token = tokens.nextToken ();
          int    answerCount = Integer.parseInt (surveysParam.getProperty (survey + "_" + token));
          answers.put (token, new Integer (answerCount));
          // Seek the good place to insert this element
          int i;
          for (i = 0; i < sortedAnswers.size (); i++)
            if (answerCount > ((Integer)answers.get (sortedAnswers.elementAt (i))).intValue ())
              break;
          sortedAnswers.insertElementAt (token, i);
        }
    }

    // Compute the pies of the survey
    drawSurveyPies (image, answers, sortedAnswers, width, height);

    // Send generated image
    sendGIFImage (image, response);
  }

  /**
   * Generates a GIF image on the response stream from image.
   */
  public void sendGIFImage (Image image, HttpServletResponse response) throws ServletException, IOException
  {
    // Set content type and other response header fields first
    response.setContentType("image/gif");
    // Then write the data of the response
    OutputStream out = response.getOutputStream ();
    try
    {
      new GifEncoder (image, out).encode ();
    }
    catch (IOException e)
    {
      // GifEncoder may throw an IOException because "too many colors for a GIF" (> 256)
      // were found in the image
      // Reduce the number of colors in that case with Web216ColorsFilter basic filter
      new GifEncoder (new FilteredImageSource (image.getSource (),
                                               new Web216ColorsFilter ()),
                      out).encode ();
    }
    out.flush ();
  }

  private void drawSurveyPies (Image     image,
                               Hashtable answers,
                               Vector    sortedAnswers,
                               int       width,
                               int       height)
  {
    Graphics gc = image.getGraphics ();

    // Draw a shadow
    gc.setColor (penColor);
    gc.fillOval (1, 1, height - 3, height - 3);
    gc.fillOval (2, 2, height - 3, height - 3);

    // Compute the sum of all values
    int sum = 0;
    for (Enumeration e = answers.elements ();
         e.hasMoreElements (); )
      sum += ((Integer)e.nextElement ()).intValue ();

    for (int accum = 0, i = 0, deltaY = 0; i < sortedAnswers.size (); i++, deltaY += 15)
    {
      int answerValue = ((Integer)answers.get (sortedAnswers.elementAt (i))).intValue ();
      int startAngle  = accum  * 360 / sum;
      int angle       = answerValue * 360 / sum;

      // Fill the anwser pie
      gc.setColor (colors [i % colors.length]);
      gc.fillArc (0, 0, height - 3, height - 3, startAngle, angle);

      // Draw a separating line
      gc.setColor (penColor);
      gc.drawLine ((height - 3) / 2, (height - 3) / 2,
                   (int)((height - 3) / 2. * (1 + Math.cos (startAngle / 180. * Math.PI)) + 0.5),
                   (int)((height - 3) / 2. * (1 - Math.sin (startAngle / 180. * Math.PI)) + 0.5));
      accum += answerValue;

      if (deltaY + 15 < height)
      {
        // Add a comment
        gc.setColor (colors [i % colors.length]);
        gc.fillRect (height + 6, deltaY + 1, 13, 10);
        gc.setColor (penColor);
        gc.drawRect (height + 5, deltaY, 14, 11);
        // Draw is done with default font
        gc.drawString (String.valueOf (100 * answerValue / sum) + "% " + (String)sortedAnswers.elementAt (i),
                       height + 22, deltaY + 9);
      }
    }

    gc.drawLine ((height - 3) / 2, (height - 3) / 2, height - 3, (height - 3) / 2);

    // Draw a surrounding oval
    gc.drawOval (0, 0, height - 3, height - 3);
  }
}
