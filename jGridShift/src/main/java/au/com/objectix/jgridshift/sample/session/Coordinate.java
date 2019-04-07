/*
 * Copyright (c) 2003, 2004 Objectix Pty Ltd  All rights reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL OBJECTIX PTY LTD BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE
 * GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package au.com.objectix.jgridshift.sample.session;

/**
 * The Coordinate object that is referenced by the JGridShiftService
 *
 * @jboss-net:xml-schema urn="jgridshift:Coordinate"
 */
public class Coordinate implements java.io.Serializable {
  private static final long serialVersionUID = 1942910817312188092L;
  private double latSeconds;
  private double lonPositiveWestSeconds;
  /**
   * @return
   */
  public double getLatSeconds() {
    return latSeconds;
  }

  /**
   * @return
   */
  public double getLonPositiveWestSeconds() {
    return lonPositiveWestSeconds;
  }

  /**
   * @param d
   */
  public void setLatSeconds(double d) {
    latSeconds = d;
  }

  /**
   * @param d
   */
  public void setLonPositiveWestSeconds(double d) {
    lonPositiveWestSeconds = d;
  }
}
