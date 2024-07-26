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
 * @version 1.0 2011/03/17
 */
public class LeakyBucket {

  /**
   * Rate in bytes/sec.
   */
  private long rate = 0;

  
  // INTERNAL ATTRIBUTES

  /**
   * 
   */
  private long nextTxTime = 0L;

  // ============================= public methods ==============================
  /**
   *
   * @param capacity
   * @param rate
   */
  public LeakyBucket(long rate) {
    // Check input parameters
    if (rate <= 0) {
      throw new IllegalArgumentException();
    }

    // Copy input parameters
    this.rate = rate;

    //lastTxTime = 0L;
    nextTxTime = 0L;
  }

  public synchronized int waitTxAccess(int length) {
    return waitTxAccess(length, true);
  }

  /**
   *
   * @param numTokens
   *
   * @return
   */
  public synchronized int waitTxAccess(int length, boolean block) {
  	
  	assert(length > 0);
  	//System.out.println(toString());
    
  	//if (length > rate) return -1;
  	
    long actualTime = System.currentTimeMillis();
    //System.out.println("\t\tactualTime="+actualTime+" nextTxTime="+nextTxTime);

    if (actualTime < nextTxTime) {
    	try {
        //System.err.println("\t\t\tWaiting for next slot "+(nextTxTime - actualTime)/1000.0F+" sec");
				Thread.sleep(nextTxTime - actualTime);
			} catch (InterruptedException e) {
				//e.printStackTrace();
			}
    }
    
    //lastTxTime = actualTime;
    nextTxTime = actualTime + (long)(length * 1000.0 / rate);
    
    return length;
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";
    str += "rate=" + rate;
    str += ", nextTxTime=" + nextTxTime;
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
    out.println("rate: " + rate);
    out.println("nextTxTime: " + nextTxTime);
    out.flush();
  }
  // ============================ private methods ==============================
}
