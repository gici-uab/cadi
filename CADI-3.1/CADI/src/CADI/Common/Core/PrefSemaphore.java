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
package CADI.Common.Core;

import java.io.PrintStream;

/**
 * This class implements a semaphore used to control when threads that are
 * attending to client requests are busy. Therefore, while, at least, one thread
 * is attending to a client, the proxy prefetching must be sleeping, and it will
 * only be awaken when there not exist any client's request waiting. 
 * <p>
 * The semaphore can be started or stopped by means of the {@link #setOn()} and
 * {@link #setOff()} methods, respectively. They set the {@link #trigger}
 * attribute.
 * <p>
 * Moreover, it records the number of locks performed between two consecutive
 * calls to the {@link  #waitChanges()} method. This functionality is useful
 * to control that threads attending to clients perhaps a new woi has been added
 * to the history, then the woi to be prefetched should be recomputed.
 * <p>
 * Usage example in threads that attend clients:<br>
 * lock<br>
 * unlock<br>
 * <p>
 * Usage example in the prefetching thread:<br>
 * waitChanges<br>
 * await<br>
 * changes<br>
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.0 2011/10/23
 */
public class PrefSemaphore {

  /**
   * Indicates the number of threads that are blocking.
   */
  private int numThreads = 0;

  /**
   *
   */
  private boolean trigger = false;

  /**
   * Number of locks between two consecutive calls to the {@link #waitChanges()}
   * method.
   */
  private int numLocks = 0;

  // ============================= public methods ==============================
  /**
   * Constructor.
   */
  public PrefSemaphore() {
  }

  /**
   * Waits until the resource is free.
   *
   * @throws InterruptedException
   */
  public synchronized void await() {
    try {
      if (!trigger) {
        wait();
      }
      while (numThreads > 0) {
        wait();
      }
    } catch (InterruptedException e) {
    }
  }

  /**
   * Sets the trigger to <code>true</code>, which starts the semaphore.
   */
  public synchronized void setOn() {
    trigger = true;
  }

  /**
   * Sets the trigger to <code>false</code>, which stops the semaphore.
   */
  public synchronized void setOff() {
    trigger = false;
  }

  /**
   *
   */
  public synchronized void waitChanges() {
    try {
      if (numLocks == 0) {
        wait();
      }
    } catch (InterruptedException ex) {
    }
    numLocks = 0;
  }

  public synchronized boolean changes() {
    return numLocks != 0;
  }

  /**
   * Indicates that the prefetching thread must be suspended.
   */
  public synchronized void suspendPrefetching() {
    numThreads++;
    trigger = true;
    numLocks++;
  }

  /**
   * Indicates the prefetching thread can continue its execution.
   */
  public synchronized void resumePrefetching() {
    numThreads--;
    this.notify();
  }

  /**
   * 
   */
  public synchronized void acquire() {
    try {

      if (!trigger) {
        wait();
      }
      while (numThreads > 0) {
        wait();
      }

    } catch (InterruptedException e) {
    }
  }

  /**
   * 
   */
  public synchronized void release() {
  }

  /*
   * (non-Javadoc)
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    String str = "";

    str = getClass().getName() + " [";

    str += "num. threads locked=" + numThreads;
    str += ", trigger=" + trigger;
    str += ", numLocks=" + numLocks;

    str += "]";

    return str;
  }

  /**
   * Prints this Proxy Pref Semaphore out to the specified output stream. This method
   * is useful for debugging.
   *
   * @param out an output stream.
   */
  public void list(PrintStream out) {

    out.println("-- Proxy Pref Semaphore --");

    out.println("Num. threads locked: " + numThreads);
    out.println("Trigger: " + trigger);
    out.println("Num. locks: " + numLocks);

    out.flush();
  }
}
