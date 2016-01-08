package com.derby.nuke;

import java.util.concurrent.TimeUnit;

/**
 * 
 * @author Passyt
 *
 */
public interface IPermit {

	/**
	 * 
	 * @return ticket id
	 */
	String acquire() throws InterruptedException;

	/**
	 * 
	 * @param timeout
	 * @param unit
	 * @return ticket id
	 */
	String tryAcquire(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * 
	 */
	void release(String ticketId);

}