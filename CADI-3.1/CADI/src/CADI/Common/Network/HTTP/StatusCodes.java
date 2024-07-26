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
package CADI.Common.Network.HTTP;

/** 
 *	This interface defines all allowed values for the status code of the HTTP
 *	response.
 *
 * @author Group on Interactive Coding of Images (GICI)
 * @version 1.0   2007-2012/10/26
 */
public interface StatusCodes {
 
	/**
	 * The server should use this status code if it accepts the view-window
	 * request for processing, possibly with some modifications to the requested
	 * view-window, as indicated by additional headers included in the reply
	 */
	int OK = 200;
	
	/**
	 * Servers should issue this status code if the view-window request was
	 * acceptable, but a subsequent view-window request was found in the queue
	 * which consequently superseded the request (because wait=no). When the
	 * first request becomes irrelevant before the server is able to process and 
	 * commence transmission of a response, then the 202 status code shall be
	 * used. This is a common occurrence in practice, since an interactive user
	 * may change his/her region of interest multiple times before the server
	 * finishes responding to an earlier request, or before the server is
	 * prepared to interrupt ongoing processing.
	 */
	int ACCEPTED = 202;
	
	/**
	 * Servers should issue this status code if the request is incorrectly
	 * formatted, or contains an unrecognized field in the query string.
	 */
	int BAD_REQUEST = 400;
	
	/**
	 * This status code should be issued if the server cannot reconcile the
	 * requested resource with an issued Target ID. This may result from
	 * unauthorized access attempts or, more likely, from a time limit expiring.
	 * If the client misses this time window, due to a poor connection, it may
	 * find that the Target ID is no longer active.
	 */
	int NOT_FOUND = 404;
	
	/**
	 * This status code may be used if the single image type specified in the
	 * Image Return Type request field cannot be serviced.
	 */
	int UNSUPPORTED_MEDIA_TYPE = 415;
	
	/**
	 * This status code may be used if a portion of this Recommendation |
	 * International Standard that is required by the request cannot be serviced.
	 */
	int NOT_IMPLEMENTED = 501;
	
	/**
	 * This status code should be used if a channel id specified in the Channel
	 * ID request field is invalid.
	 */
	int SERVICE_UNAVAILABLE = 503;	
	
}