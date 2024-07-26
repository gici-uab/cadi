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
package CADI.Common.LogicalTarget.JPEG2000.File;

/**
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1 2012/01/05
 */
public class JPXBoxTypes extends JP2BoxTypes {
	
	// JPEG2000 SIGNATURE BOX

	// FILE TYPE BOX
	//int FILE_TYPE_BOX_TYPE		= (int)0x66747970; // 'ftyp'
	static final int FILE_TYPE_BOX_BRAND_JP2	= (int)0x6A703220; // 'jp2 '
	static final int FILE_TYPE_BOX_BRAND_JPX	= (int)0x6a707820; // 'jpx ' 
	
	// READER REQUIREMENTS BOX
	static final int READER_REQUIREMENTS_BOX_TYPE = (int)0x72726571; //'rreq'
	
	// LABEL BOX
	static final int LABEL_BOX_TYPE						= (int)0x6C626C20; // 'lbl\040'
	
	// JP2 HEADER BOX & CODESTREAM HEADER BOX
	static final int CODESTREAM_HEADER_BOX_TYPE			= (int)0x6A706368; // 'jpch' 
	
	// COMPOSITING LAYER HEADER BOX
	static final int COMPOSITION_LAYER_HEADER_BOX_TYPE  = (int)0x6A706C68; //'jplh'
	static final int COLOR_GROUP_BOX_TYPE = (int)0x63677270; // 'cgrp'
	static final int OPACITY_BOX_TYPE = (int)0x6F706374; // 'opct'
	static final int CODESTREAM_REGISTRATION_BOX_TYPE = (int)0x63726467; //'creg'
	
	// DATA REFERENCE BOX
	
  
	// CONTIGUOUS CODESTREAM BOX
	//int CONTIGUOUS_CODESTREAM_BOX_TYPE = JP2BoxTypes.CONTIGUOUS_CODESTREAM_BOX_TYPE;
	
	// MEDIA DATA BOX
	
	
	// COMPOSITION BOX

	
	// DESIRED REPRODUCTIONS BOX
	
	
	// ROI DESCRIPTION BOX
	
	
	// CROSS-REFERENCE BOX
	
	
	// ASOCIATION BOX
	
	
	// BINARY FILTER BOX
	
	
	// DIGITAL SIGNATURE BOX
	
	
	// MPEG-7 BINARY BOX
	
	
	// FREE BOX
	
	
	// XML BOX
	
	
	// UUID BOX
	
	
	// INTELLECTUAL PROPERTY RIGHTS BOX
	
	
	// UUID INFO BOX
}
