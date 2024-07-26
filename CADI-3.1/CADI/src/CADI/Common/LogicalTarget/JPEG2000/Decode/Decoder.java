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
package CADI.Common.LogicalTarget.JPEG2000.Decode;
import CADI.Client.ClientLogicalTarget.JPEG2000.CPByteStream;
import GiciException.*;
import GiciStream.*;


/**
 * This interface defines the methods that a decoder must contain.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0
 */
public interface Decoder{

	/**
	 * Decode a bit taking into account context probabilities in an artihmetic like coder.
	 *
	 * @param context context the bit
	 * @return a boolean indicating the decoded bit
	 *
	 * @throws ErrorException when some problem with the manipulation of the ByteStream happens
	 */
	public boolean decodeBit(int context) throws ErrorException;

	/**
	 * Decode a bit without taking into account context probabilities in an artihmetic like coder.
	 *
	 * @return a boolean indicating the decoded bit
	 *
	 * @throws ErrorException when some problem with the manipulation of the ByteStream happens
	 */
	public boolean decodeBit() throws ErrorException;

	/**
	 * Swaps the current inputByteStream. After calling this function and continue using the decoder you should restart the decoder with the function restart.
	 *
	 * @param inputByteStream ByteStream from where the byte are got
	 */
	public void swapInputByteStream(CPByteStream inputByteStream);

	/**
	 * Restart the internal variables of the decoder.
	 *
	 * @throws ErrorException when some problem with the manipulation of the ByteStream happens
	 */
	public void restart() throws ErrorException;

	/**
	 * Reset the context probabilities of the decoder, if any.
	 */
	public void reset();

}
