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
package CADI.Server.Core;

import java.io.PrintStream;
import java.util.ArrayList;

/** 
 * This class implements a queue where the works to be done by a thread
 * are saved by the {@link CADI.Server.Core.Scheduler}. 
 * <p>
 * NOTICE:
 * Implementing of different kinds of priorities is very easy.
 * Using several queues with differents priorities and then doing the
 * necessary changes in the add and get methods.
 * <p>
 * Usage example:<br>
 * &nbsp; construct<br>
 * &nbsp; add<br>
 * &nbsp; get<br> 
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0   2007-2012/10/26
 */
public class TasksQueue {

	/**
	 * Contains a list of works. The <data>ArrayList</data> is not a
	 * synchronized data list, so the class methods implement the concurrence
	 * to avoid simultaneous access.
	 */
	private ArrayList<TasksQueueNode> queue = null;

  
	// ============================= public methods ==============================
	/**
	 * Constructor of RequestQueue. It initializes the queue of sets to a void queue.	 
	 */
	public TasksQueue(){
		queue = new ArrayList<TasksQueueNode>();
	}

	/**
	 * Adds new work to the queue, and it notifies it to the
	 * dispatcher requests.
	 * 
	 * @param nodeInfo
	 */
	public synchronized void add(TasksQueueNode nodeInfo) {
		queue.add(nodeInfo);
		notify();
	}

	/**
	 * Returns the first element of the queue if it isn't empty, otherwise null.
	 * 
	 * @return returns the first node of the list.
	 * 
	 * @throws InterruptedException 
	 */
	public synchronized TasksQueueNode get() throws InterruptedException {
		TasksQueueNode nodeInfo = null;

		if (isEmpty()) {
			wait();
		}

		if (!isEmpty()) {
			nodeInfo = queue.get(0);
			queue.remove(0);		
			queue.trimToSize();
		}

		return nodeInfo;
	}

	/**
	 * Returns true if the list is empty, false otherwise
	 *
	 */
	public synchronized boolean isEmpty() {
		return queue.isEmpty();
	}

	/**
	 * Returns the size of the queue.
	 * 
	 * @return the size of the queue.
	 */
	public synchronized int size() {
		return queue.size();	
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
  @Override
	public synchronized String toString() {
		String str = "";

		str = getClass().getName() + " [\n";

		for (int i = 0; i < queue.size(); i++) {
			str += "-- NODE: "+ i + " --\n";
			str += queue.get(i).toString() + "\n";
		}

		str += "]";
		return str;
	}
	
	/**
	 * Prints this Tasks Queue fields out to the
	 * specified output stream. This method is useful for debugging.
	 * 
	 * @param out an output stream.
	 */
	public synchronized void list(PrintStream out) {

		out.println("-- Tasks Queue --");		
		
		for (int i = 0; i < queue.size(); i++) {
			out.println("-- NODE: "+ i + " --");
			queue.get(i).list(out);
		}

						
		out.flush();
	}
	
}