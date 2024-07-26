/*
 * CADI Software - a JPIP Client/Server framework
 * Copyright (C) 2007-2012  Group on Interactive Coding of Images (GICI)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 *
 * Group on Interactive Coding of Images (GICI)
 * Department of Information and Communication Engineering
 * Autonomous University of Barcelona
 * 08193 - Bellaterra - Cerdanyola del Valles (Barcelona)
 * Spain
 *
 * http://gici.uab.es
 * gici-info@deic.uab.es
 */
package CADI.Common.Network;

import java.io.PrintStream;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2011/03/19
 */
public class TokenBucket {

  /**
   *
   */
  private long capacity = 0;

  /**
   * Rate in bytes/sec.
   */
  private long rate = 0;

  // INTERNAL ATTRIBUTES
  /**
   *
   */
  private long tokens = 0;

  private long lastTime = 0;

  // ============================= public methods ==============================
  /**
   *
   * @param capacity
   * @param rate
   */
  public TokenBucket(long capacity, long rate) {
    // Check input parameters
    if (capacity <= 0) {
      throw new IllegalArgumentException();
    }
    if (rate <= 0) {
      throw new IllegalArgumentException();
    }

    // Copy input parameters
    this.capacity = capacity;
    this.rate = rate;

    tokens = capacity;
    lastTime = System.currentTimeMillis();
  }

  /**
   *
   * @param numTokens
   *
   * @return
   */
  public synchronized int getTokens(int numTokens) {

    assert (numTokens > 0);
    /*if (numTokens > capacity) {
      return -1;
    }*/

    long actualTime = -1;
    long diffTime = -1;
    while ((numTokens > tokens) && (tokens < capacity)) {
      actualTime = System.currentTimeMillis();
      diffTime = (actualTime - lastTime) / 1000;
      tokens = ((tokens + rate * diffTime) <= capacity)
              ? tokens + rate * diffTime
              : capacity;
      lastTime = actualTime;

      if ((numTokens > tokens) && (tokens < capacity)) {
        diffTime = (long)Math.ceil(1.0 * (Math.min(capacity, numTokens) - tokens) / rate) * 1000;
        try {
          //System.err.println("\t\t\tSlepping "+(diffTime/1000.0F)+" sec");
          Thread.sleep(diffTime);
        } catch (InterruptedException e) {
          //e.printStackTrace();
        }
      }
    }

    int availableTokens = (numTokens > tokens) ? (int)tokens : numTokens;
    tokens -= availableTokens;

    return availableTokens;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += "capacity=" + capacity;
    str += ", rate=" + rate;
    str += ", tokens=" + tokens;
    str += "]";

    return str;
  }

  /**
   * Prints this HTTP Request Reader fields out to the
   * specified output stream. This method is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Token Bucket --");
    out.println("capacity: " + capacity);
    out.println("rate: " + rate);
    out.println("tokens: " + tokens);
    out.flush();
  }
  // ============================ private methods ==============================
}
