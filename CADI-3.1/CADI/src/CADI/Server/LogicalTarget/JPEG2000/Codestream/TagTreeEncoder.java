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
package CADI.Server.LogicalTarget.JPEG2000.Codestream;
import java.io.IOException;
import java.io.PrintStream;

import GiciException.*;

/**
 * This class codifies a matrix using a Tag Tree structure. Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; encoder<br>
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.1 2008/12/27
 */
public class TagTreeEncoder {

	/**
	 * Number of levels
	 * <p>
	 * Negative values are not allowed
	 */
	private int levels;

	/**
	 * Number of rows
	 * <p>
	 * Negative values are not allowed
	 */
	private int rows;

	/**
	 * Number of columns
	 * <p>
	 * Negative values are not allowed
	 */
	private int cols;

	/**
	 * Tag Tree values. The first value index indicates the level of the tree, where the value 0 is the matrix to encoder, and value levels-1 is the root. The second index is the rows, and the third is the columns
	 * <p>
	 * Negative values are not allowed
	 */
	private int tagTree[][][];

	/**
	 * Tag Tree States.  The first value index indicates the level of the tree, where the value 0 is the matrix to encoder, and value levels-1 is the root. The second index is the rows, and the third is the columns
	 * <p>
	 * Negative values are not allowed
	 */
	private int states[][][];

	// ============================= public methods ==============================
	/**
	 * Constructor with an empty matrix.
	 *
	 * @param rows matrix rows
	 * @param cols matrix columns
	 *
	 * @throws ErrorException when rows or cols are negative
	 */
	public TagTreeEncoder(int rows, int cols) throws ErrorException {

		//Checks
		if((rows<=0) || (cols<=0)){
			throw new ErrorException("Rows and columns must be positive.");
		}
		//Initializate dimensions
		this.rows=rows;
		this.cols=cols;
		// Number of levels
		int levelsAux = Math.max(rows,cols);
		for ( levels=1; levelsAux>1; levels++) {
			levelsAux = (int)Math.ceil(levelsAux/2D);
		}

		//Initializate tagTree and states array
		tagTree = new int [levels][][];
		states  = new int [levels][][];
		for (int i=0; i<levels; i++) {
			tagTree[i] = new int[rows][cols];
			states[i]  = new int[rows][cols];
			rows = (int)Math.ceil(rows/2.0);
			cols = (int)Math.ceil(cols/2.0);
		}
	}

	/**
	 * Constructor with a matrix.
	 *
	 * @param matrix matrix to code
	 *
	 * @throws ErrorException when rows or cols are negative
	 */
	public TagTreeEncoder(int matrix[][]) throws ErrorException {

		if (matrix == null) throw new NullPointerException();
		
		// Initialitate dimensions and number of levels

		rows = matrix.length;
		//Checks
		if(rows <= 0){
			throw new ErrorException("Rows and columns must be positive.");
		}

		cols = matrix[0].length;

		//Checks
		if(cols <= 0){
			throw new ErrorException("Rows and columns must be positive.");
		}


		int levelsAux = Math.max(rows,cols);
		for ( levels=1; levelsAux>1; levels++) {
			levelsAux = (int)Math.ceil(levelsAux/2D);
		}

		//System.out.println("Rows =" + rows + " Cols = " + cols + " --> " + levels);
		// Initializate tagTree and states array
		tagTree = new int [levels][][];
		states  = new int [levels][][];

		int rowsAux=rows, colsAux=cols;
		for(int i = 0; i < levels; i++) {
			// Reserve the memory for level i
			tagTree[i] = new int[rowsAux][colsAux];
			states[i]  = new int[rowsAux][colsAux];
			// Initialitate the states
			for(int r = 0; r < rowsAux; r++){
				for(int c = 0; c < colsAux; c++){
					states[i][r][c] = (int)0;
					tagTree[i][r][c] = Integer.MAX_VALUE;
				}
			}
			//Update rows and columns
			rowsAux = (int)Math.ceil(rowsAux/2D);
			colsAux = (int)Math.ceil(colsAux/2D);
		}

		//Assign de matrix values to tagTree and generate it
		tagTree[0]=matrix;
		rowsAux=rows; colsAux=cols;
		for(int l = 0; l < levels-1; l++) {
			for(int r = 0; r < rowsAux; r++) {
				for (int c = 0; c < colsAux; c++) {
					tagTree[l+1][(int)(r/2.0)][(int)(c/2)]= Math.min(tagTree[l+1][(int)(r/2.0)][(int)(c/2)], tagTree[l][r][c]);
				}
			}
			//Update rows and columns
			rowsAux = (int)Math.ceil(rowsAux/2.0);
			colsAux = (int)Math.ceil(colsAux/2.0);
		}
	}

	/**
	 * Encode a value of the matrix.
	 * 
	 * @param packetHeaderDataOutputStream data output stream.
	 * @param t threshold
	 * @param m row of the value to be coded
	 * @param n column of the value to be coded
	 *
	 * @throws ErrorException when invalid dimensions are passed
	 * @throws IOException 
	 */
	public void encoder(PacketHeaderDataOutputStream packetHeaderDataOutputStream, int t, int m, int n) throws ErrorException, IOException {
		//Used to propagate knowledge to descendants
		int tmin = 0;
		int mk,nk;
		int temp;
		
		// Check parameters
		if((m < 0) || (m >= rows) || (n < 0) || (n >= cols)){
			throw new ErrorException("Invalid dimensions.");
		}

		// Loop on levels
		for(int k = levels-1; k >= 0; k--){
			temp = (int) Math.pow(2,k);
			mk = (int) m/temp;
			nk = (int) n/temp;
			if(states[k][mk][nk] < tmin){
				states[k][mk][nk] = tmin;
			}

			while((tagTree[k][mk][nk] >= states[k][mk][nk]) && (states[k][mk][nk] < t)){
				states[k][mk][nk]++;
				if(tagTree[k][mk][nk] >= states[k][mk][nk]){
					//Emit a 0 bit
					packetHeaderDataOutputStream.emitTagBit((byte)0);
				}else{
					//emit a 1 bit
					packetHeaderDataOutputStream.emitTagBit((byte)1);
				}
			}
			//Update tmin
			tmin=Math.min(states[k][mk][nk],tagTree[k][mk][nk]);
		}
	}
	
	/**
	 * Sets attributes to its initial values;
	 */
	public void reset() {
		for (int l = 0; l < tagTree.length; l++) {
			for (int r = 0; r < tagTree[l].length; r++) {
				for (int c = 0; c < tagTree[l][r].length; c++) {
					tagTree[l][r][c] = 0;
					states[l][r][c] = 0;
				}
			}
		}
	}
	
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
				
		int rowsAux=rows, colsAux=cols;		
		String str = "";

		str = getClass().getName() + " [";
		
		str += "rows=" + rows + "cols= " + cols + " levels=" + levels;
		for(int l = 0; l < levels; l++) {
			for(int r = 0; r < rowsAux; r++){
				for (int c = 0; c < colsAux; c++){
					str += tagTree[l][r][c] + " ";
				}
				str += "\t";
			}
			//Update rows and columns
			rowsAux = (int)Math.ceil(rowsAux/2.0);
			colsAux = (int)Math.ceil(colsAux/2.0);
		}
		str += "]";
		
		return str;
	}
		
	/**
	 * Prints this Tag Tree out to the specified output stream. This method is
	 * useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Tag Tree --");		
		
		int rowsAux=rows, colsAux=cols;
		out.println("rows: " + rows);
		out.println("cols: " + cols);
		out.println("levels: " + levels);
	
		for(int l = 0; l < levels; l++) {
			for(int r = 0; r < rowsAux; r++){
				for (int c = 0; c < colsAux; c++){
					out.println(tagTree[l][r][c] + " ");
				}
				out.println();
			}
			//Update rows and columns
			rowsAux = (int)Math.ceil(rowsAux/2.0);
			colsAux = (int)Math.ceil(colsAux/2.0);
		}

		out.flush();
	}
	
}