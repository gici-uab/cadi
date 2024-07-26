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
public class TrafficShaping {

	/**
	 *
	 */
	private TokenBucket tokenBucket = null;

	/**
	 *
	 */
	private LeakyBucket leakyBucket = null;

  private int algorithm = NONE;
	public static final int NONE = 0;
	public static final int TOKEN_BUCKET = 1;
	public static final int LEAKY_BUCKET = 2;

	// ============================= public methods ==============================
	/**
	 * 
	 * @param capacity
	 * @param rate
	 * @param algorithm
	 */
	public TrafficShaping(long capacity, long rate, int algorithm) {
		switch (algorithm) {
			case NONE:
        // do nothing
				break;
			case  TOKEN_BUCKET:
				tokenBucket = new TokenBucket(capacity, rate);
				break;
			case LEAKY_BUCKET:
				leakyBucket = new LeakyBucket(rate);
				break;
			default:
				throw new IllegalArgumentException();
		}

    this.algorithm = algorithm;
	}

	/**
	 *
	 * @param numTokens
	 *
	 * @return
	 */
	public synchronized int getTokens(int length) {
		if (algorithm == TOKEN_BUCKET) {
			return tokenBucket.getTokens(length);
		} else if (algorithm == LEAKY_BUCKET) {
			return leakyBucket.waitTxAccess(length);
		} else if(algorithm == NONE) {
			return length;
		} else {
      assert(true);
      return -1;
    }
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
		if (tokenBucket != null)  {
			str += tokenBucket.toString();
		} else {
			leakyBucket.toString();
		}
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

		out.println("-- Traffic Shaping --");
		if (tokenBucket != null)  {
			tokenBucket.list(out);
		} else {
			leakyBucket.list(out);
		}
		out.flush();
	}
	
	// ============================ private methods ==============================
	/**
	 * Calculates the average of bytes per second. The average is weighted
	 * following a gaussian distribution.
	 * 
	 * @param bytes is an one-dimensional array with the amount of bytes.
	 * @param times is an one-dimensional array with the time where bytes
	 * 			was sent. It is expressed in miliseconds. The first index is
	 * 			when the first dada was sent, and the last one is the most
	 * 			most recently time.
	 * 
	 * @return the average of bytes per second.
	 */
	public static float calculateAverageWeighted(long[] bytes, long[] times) {
		
		// Check input parameters
		if (bytes == null) throw new NullPointerException();
		if (times == null) throw new NullPointerException();
		if (bytes.length != times.length) throw new NullPointerException();
		for (int i = 0; i < bytes.length; i++) {
			if ( (bytes[i] < 0) || (times[i] < 0)) throw new IllegalArgumentException();
		}

		int length = bytes.length;
		
		// Time is passed to sec.
		long[] timesSec = new long[length];
		for (int i = 0; i < length; i++) {
			timesSec[i] = times[i] / 1000L;
		}
		
		// Calculates weights
		long time = System.currentTimeMillis() / 1000L;
		float[] weights = new float[times.length];
		float sigma = 1F;
		//float mu = 0;
		float factor1 = (float)Math.sqrt(2*Math.PI)*sigma;
		float factor2 = 2F*sigma*sigma;
		for (int i = 0; i < weights.length; i++) {
			weights[i] = (float)Math.exp( -(time-timesSec[i])*(time-timesSec[i]) /factor2) / factor1;
		}

		// Calculate weighted average
		float average = 0F;
		for (int i = 0; i < times.length; i++) {
			average += weights[i] * bytes[i];
		}

		return average;
	}
}
