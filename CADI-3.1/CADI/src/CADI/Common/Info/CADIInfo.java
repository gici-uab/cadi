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
package CADI.Common.Info;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0 15/09/2008
 */
public class CADIInfo {

	/**
	 * 
	 */
	private Properties cadiInfo = null;

	// ============================= public methods ==============================
	
	/**
	 * Constructor.
	 * 
	 * @throws IOException 
	 */
	public CADIInfo() throws IOException {
		cadiInfo = new Properties();
		InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
		cadiInfo.load(cadiInfoURL);
	}

	/**
	 * Prints out the short copyright.
	 */
	public void printVersion() {
		try {
			Properties cadiInfo = new Properties();
			InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
			cadiInfo.load(cadiInfoURL);
			
			System.out.println("CADI version "+cadiInfo.getProperty("version"));
		} catch (Exception e) {
			System.out.println("PARAMETERS ERROR: error reading copyright");
			System.out.println("Please report this error to: gici-dev@deic.uab.es");
		}
	}
	
	/**
	 * Prints out the "Copyright (C)" line followed by the years.
	 */
	public void printCopyrightYears() {
		try {
			Properties cadiInfo = new Properties();
			InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
			cadiInfo.load(cadiInfoURL);
			
			System.out.println("Copyright (C) "+cadiInfo.getProperty("copyrightYears"));
		} catch (Exception e) {
			System.out.println("PARAMETERS ERROR: error reading copyright");
			System.out.println("Please report this error to: gici-dev@deic.uab.es");
		}
	}
	
	/**
	 * Prints out the short copyright.
	 */
	public void printShortCopyright() {
		try {
			Properties cadiInfo = new Properties();
			InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
			cadiInfo.load(cadiInfoURL);
			
			System.out.println(cadiInfo.getProperty("shortCopyright"));
		} catch (Exception e) {
			System.out.println("PARAMETERS ERROR: error reading copyright");
			System.out.println("Please report this error to: gici-dev@deic.uab.es");
		}
	}
	
	/**
	 * Prints out the warranty.
	 */
	public void printWarranty() {
		try {
			Properties cadiInfo = new Properties();
			InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
			cadiInfo.load(cadiInfoURL);

			System.out.println("CADIClient version " + cadiInfo.getProperty("version")+"\n");
			System.out.println(cadiInfo.getProperty("disclaimerOfWarranty"));

		} catch (Exception e) {
			System.out.println("PARAMETERS ERROR: error reading the disclaimer of warranty");
			System.out.println("Please report this error to: gici-dev@deic.uab.es");
		}
	}

	/**
	 * Prints out the liability.
	 */
	public void printLiability() {
		try {			
			Properties cadiInfo = new Properties();
			InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
			cadiInfo.load(cadiInfoURL);

			System.out.println("CADIClient version " + cadiInfo.getProperty("version")+"\n");
			System.out.println(cadiInfo.getProperty("limitationsOfLiability"));

		} catch (Exception e) {
			System.out.println("PARAMETERS ERROR: error reading limitations of liability");
			System.out.println("Please report this error to: gici-dev@deic.uab.es");
		}
	}

	/**
	 * Prints out the copyright.
	 */
	public void printCopyright() {
		try {
			Properties cadiInfo = new Properties();
			InputStream cadiInfoURL = getClass().getClassLoader().getResourceAsStream("CADI/Common/Info/cadiInfo.properties");
			cadiInfo.load(cadiInfoURL);

			System.out.println("CADIClient version " + cadiInfo.getProperty("version")+"\n");
			System.out.println(cadiInfo.getProperty("copyright"));

		} catch (Exception e) {
			System.out.println("PARAMETERS ERROR: error reading copyright");
			System.out.println("Please report this error to: gici-dev@deic.uab.es");
		}
	}

	// ============================ private methods ==============================
}