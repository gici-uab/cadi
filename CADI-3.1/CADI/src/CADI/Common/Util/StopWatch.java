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
package CADI.Common.Util;

import java.io.PrintStream;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/20
 */
public class StopWatch {
	
	/**
	 * 
	 */
	private long startTime = -1;
	
	/**
	 * 
	 */
	private long stopTime = -1;

	
	// INTERNAL ATTRIBUTES
	
	 // running states
	private static final int UNSTARTED = 0;
	private static final int RUNNING   = 1;
	private static final int STOPPED   = 2;
	private static final int SUSPENDED = 3;
	
	private int state = UNSTARTED;
	
	// ============================= public methods ==============================
	/**
	 * Constructor.
	 */
	public StopWatch() {
		reset();
	}
	
	/**
	 * 
	 */
	public void start() {
		if (state != UNSTARTED) throw new IllegalStateException("StopWatch has already started.");
		if (state == STOPPED) throw new IllegalStateException("Stopwatch is stopped. It must be resetted before being re-started.");
		
		startTime = System.currentTimeMillis();
		state = RUNNING;
	}

	/**
	 * 
	 *
	 */
	public void reset() {
		state = UNSTARTED;
		startTime = stopTime = -1;
	}
	
	/**
	 * 
	 *
	 */
	public void stop() {
		if ((state != RUNNING) && (state != SUSPENDED)) throw new IllegalStateException("Stopwatch is not running.");
		
		stopTime = System.currentTimeMillis();
	}

	/**
	 * 
	 *
	 */
	public void suspend() {
		if (state != RUNNING) throw new IllegalStateException("Stopwatch is not running to be suspended.");
		
		stopTime = System.currentTimeMillis();
		state = SUSPENDED;
	}
	
	/**
	 * 
	 *
	 */
	public void resume() {
		if (state != SUSPENDED) throw new IllegalStateException("Stopwatch must be suspended to resume. ");

		startTime += (System.currentTimeMillis() - stopTime);
		stopTime = -1;
		state = RUNNING;
	}
	
	/**
	 * 
	 * @return
	 */
	public long getTime() {
		if (state == STOPPED || state == SUSPENDED) return (stopTime - startTime);
		else if (state == UNSTARTED) return 0;
		else if (state == RUNNING) return (System.currentTimeMillis() - this.startTime);
		throw new RuntimeException("Illegal running state has occured.");
	}
	
	/**
	 * 
	 * @return
	 */
	public long getElapsedTimeMillis() {
		return (stopTime - startTime);
	}
	
	/**
	public int getState() {
		return state;
	}
	
	/**
	 * 
	 */
  @Override
	public String toString() {
		return "elapsedTimeMillis: " + Long.toString(getElapsedTimeMillis());
	}
	
	/**
	 * Prints this Stop Watch out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {
		out.println("-- Stop Watch --");		
		out.println("startTime: "+startTime);
		out.println("stopTime: "+stopTime);
		out.println("elapsed time: "+this.getElapsedTimeMillis()+" (ms)");
		out.flush();
	}
}