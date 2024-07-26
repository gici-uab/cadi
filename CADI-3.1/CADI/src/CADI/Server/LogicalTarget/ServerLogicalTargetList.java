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
package CADI.Server.LogicalTarget;

import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

import CADI.Server.LogicalTarget.JPEG2000.JP2KServerLogicalTarget;

/**
 * This class implements a list of loaded logical targets. Logical targets
 * will be loaded and indexed the first time they are requested. Then they
 * are saved to improve the response of the next requests. 
 * <p>
 * <bold>NOTICE:</bold> Methods are not synchronized yet. It may be a
 * problem with the threads.
 * 
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0.1  2007-2012/12/04
 */
public class ServerLogicalTargetList {
	
	/**
	 * Contains the list of loaded logical targets.
	 * key: tid
	 * value: logical target
	 */
	private Map<String, JP2KServerLogicalTarget> logicalTargets = null;
	
	
	// ============================= public methods ==============================
	/**
	 * Constructor of LogicalTargetList. It initializes the list to a void list.	 
	 */
	public ServerLogicalTargetList(){
		logicalTargets = new HashMap<String, JP2KServerLogicalTarget>();
	}

	/**
	 * Appends the specific  logical target to the list of logical targets.
	 * 
	 * @param logicalTarget logical target to be appended to the list.
	 */
	public void add(JP2KServerLogicalTarget logicalTarget) {
		if ( logicalTargets.containsKey(logicalTarget.getTID()) ) {
			assert(true);
		}
		
		logicalTargets.put(logicalTarget.getTID(), logicalTarget);
	}
	
	/**
	 * Gets a logical target from the list. The logical target is idenfied by
	 * the target identifier (<code>tid</code>).
	 * <p>
	 * <bold>NOTICE:</bold> elmentents are not copied. Its reference is passed.
	 *  
	 * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
	 * 
	 * @return an object which contains the logical target information. If the
	 * 			<code>tid<code> is not in the list, it returns <code>null
	 * 			</code>
	 */
	public JP2KServerLogicalTarget getByTID(String tid) {
		return (tid == null) ? null : logicalTargets.get(tid);
	}
	
	/**
	 * Gets a logical target from the list. The logical target is idenfied by
	 * the <code>target</code>.
	 * 
	 * <p>
	 * <bold>NOTICE:</bold> elmentents are not copied. Its reference is returned.
	 * 
	 * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
	 * 
	 * @return an object which contains the logical target information. If the
	 * 			<code>target<code> is not in the list, it returns <code>null
	 * 			</code>
	 */
	public JP2KServerLogicalTarget getByTarget(String target) {
		if (target == null) return null;
		
		for (Map.Entry<String, JP2KServerLogicalTarget> entry : logicalTargets.entrySet()) {
			if (entry.getValue().getTarget().compareTo(target) == 0)
				return entry.getValue();
		}
		return null;
	}
	
	/**
	 * Check if the logical target identified 
	 * 
	 * @param target definition in {@link CADI.Common.Network.JPIP.TargetField#target}.
	 * @param tid definition in {@link CADI.Common.Network.JPIP.TargetField#tid}.
	 * 
	 * @return <code>true</code> if the <code>target</code> belongs to the
	 * 			target identifier <code>tid</code>.
	 */
	public boolean equals(String target, String tid) {
		if ((target == null) || (tid == null)) return false;
		
		if (logicalTargets.get(tid).getTarget().compareTo(target) == 0)
			return true;
		else 
			return false;
	}
	
	/**
	 * Returns <code>true</code> if the list contains no elements. Otherwise,
	 * returns false.
	 * 
	 * @return <code>true</code> if the list contains no elements.
	 */
	public boolean isEmpty() {
		return logicalTargets.isEmpty();
	}
	
	/**
	 * Returns the number of elements in the list.
	 * 
	 * @return the number of elements in the list.
	 */
	public int size() {
		return logicalTargets.size();
	}
		
	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public String toString() {
		String str = "";

		str = getClass().getName() + " [";
				
		if (isEmpty()) {
			str += " <<< List is empty >>> ";
		} else {
			for (Map.Entry<String, JP2KServerLogicalTarget> entry : logicalTargets.entrySet()) {
				str += "key="+entry.getKey();
				str += "value="+entry.getValue().toString();
			}
		}
		
		str += "]";		
		return str;
	}
	
	/**
	 * Prints this Logical Target List fields out to the specified output
	 * stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public void list(PrintStream out) {

		out.println("-- Logical Target List --");		
		
		if (isEmpty()) {
			out.println(" <<< List is empty >>> ");
		} else {
			for (Map.Entry<String, JP2KServerLogicalTarget> entry : logicalTargets.entrySet()) {
				out.println("key: "+entry.getKey());
				out.print("value: "); entry.getValue().list(out);
			}
		}
						
		out.flush();
	}
	
}