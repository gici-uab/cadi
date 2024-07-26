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
package CADI.Common.Network.JPIP;

/**
 * This interface defines the class identifiers for the different data-bin
 * message classes.
 * <p>
 * Further information in ISO/IEC 15444-9, section A.2.2 (Table A.2)
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 2009/09/06
 */
public interface ClassIdentifiers {

	int PRECINCT = 0;
	int EXTENDED_PRECINCT = 1;
	int TILE_HEADER = 2;
	int TILE = 4;
	int EXTENDED_TILE = 5;
	int MAIN_HEADER = 6;
	int METADATA = 8;
	
}