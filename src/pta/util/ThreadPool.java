/*
 * @(#)ThreadPool.java 1.0 2009/12/29
 * Copyright 2009 PHẠM Tuấn Anh. All rights reserved.
 */
package pta.util;

// import java.util.*;

public final class ThreadPool extends Thread
{
	/*
	 * private Queue<Thread> waiting = new Queue<Thread>(); private
	 * Queue<Thread> running = new Queue<Thread>();
	 * 
	 * private int capacity; private double limitRatio;
	 * 
	 * public ThreadPool (int capacity, double limitRatio) { this.capacity =
	 * capacity; this.limitRatio = limitRatio; }
	 * 
	 * public ThreadPool() { this (1024, 0.75); }
	 * 
	 * public int getCapacity() {return capacity;} public double getLimitRatio()
	 * {return limitRatio;} public int getWaitingCount() {return
	 * waiting.size();} public int getRunningCount() {return running.size();}
	 */

	public void handle (Runnable runnable)
	{
		new Thread (runnable).start();
	}

	public static void main (String[] args)
	{}
}
