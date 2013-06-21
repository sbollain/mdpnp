package org.mdpnp.dts.statistics;

import java.util.ArrayList;
import java.util.List;

import org.mdpnp.dts.utils.UtilsDTS;

/**
 * 
 * @author dalonso@mdpnp.org
 * Implements basic functionality to get statistics of the offsets.
 * These would be offset average and Standard deviation for the regular (basic) case, 
 *  the "New Minute" case (clock doesn't display seconds, and we assume we just change to this minute XX:00)
 *  the "New Minute Eve" case (clock doesn't display seconds, and we assume is about to change to the next minute XX:59)
 *  XXX Offsets are calculated always in MILISECONDS, for consistency w/ Date.getTime() 
 *  
 * XXX Note about the Standard Deviation:
 * The standard deviation is equal to the square root of the variance.
 * The following formula is valid only if the N values we use in our calculations form the complete population.
 * If the values instead were a random sample drawn from some larger parent population, then we would have divided by (N - 1) 
 * instead of N in the denominator of the last formula, and then the quantity thus obtained would be called the sample standard deviation.
 *
 */
public class OffsetStatisticsImpl implements OffsetStatistics {
	
	//
	private List<Long> offsetList; //regular offset list
	private List<Long> offsetNewMinute; //list of offset considering new minute (XX:00)
	private List<Long> offsetNewMinuteEve; //list of offset considering new minute eve (XX:59)
	
	private long totalOffset; //total value of regular offset
	private long totalOffsetNewMinute; //total value of offset for new minute comparison 
	private long totalOffsetNewMinuteEve; //total value of offset for new minute eve comparison
	
	private long maxOffset;//maximal regular offset 
	private long maxOffsetNewMinute; //maximal offset for new minute comparison	
	private long maxOffsetNewMinuteEve; //maximal offset for new minute eve comparison
	
	private long minOffset;//minimal regular offset 
	private long minOffsetNewMinute; //minimal offset for new minute comparison	
	private long minOffsetNewMinuteEve; //minimal offset for new minute eve comparison
	
	//cons
	public OffsetStatisticsImpl(){
		offsetList = new ArrayList<Long>();
		offsetNewMinute = new ArrayList<Long>();
		offsetNewMinuteEve = new ArrayList<Long>();
				
		totalOffset = totalOffsetNewMinute = totalOffsetNewMinuteEve = 0; //init totals 
		maxOffset = maxOffsetNewMinute = maxOffsetNewMinuteEve = Long.MIN_VALUE; //init maximums
		minOffset = minOffsetNewMinute = minOffsetNewMinuteEve = Long.MAX_VALUE; //init minimuns

	}
	
//getters and setters
	/**
	 * Returns the amount of items studied 
	 * NOTE: this number should be the same for the three different time scenarios
	 */
	public int getCount() {
		return offsetList.size();
	}

	/**
	 * Returns the regular average
	 */
	public double getAvgOffset() {
		return totalOffset/offsetList.size();
	}

	/**
	 * Returns the offset average for the "New Minute" scenario
	 */
	public double getAvgOffset_newMinute() {
		return totalOffsetNewMinute/offsetNewMinute.size();
	}

	/**
	 * Returns the offset average for the "New minute Eve" Scenario
	 */
	public double getAvgOffset_NewMinuteEve() {
		return totalOffsetNewMinuteEve/offsetNewMinuteEve.size();
	}


	/**
	 * Returns the regular standard deviation
	 */
	public double getStdDev() {
		long total = 0;
		double avg = getAvgOffset();
		for(Long l : offsetList){
			total+= Math.pow(l-avg, 2);
		}
		return Math.sqrt(total/getCount());
	}
	
	/**
	 * Returns the standard deviation for the "New Minute" scenario
	 */
	public double getStdDevNewMinute() {
		long total = 0;
		double avg = getAvgOffset_newMinute();
		for(Long l : offsetNewMinute){
			total+= Math.pow(l-avg, 2);
		}
		return Math.sqrt(total/getCount());
	}
	
	/**
	 * Returns the standard deviation for the "New Minute Eve" scenario
	 */
	public double getStdDevNewMinuteEve() {
		long total = 0;
		double avg = getAvgOffset_NewMinuteEve();
		for(Long l : offsetNewMinuteEve){
			total+= Math.pow(l-avg, 2);
		}
		return Math.sqrt(total/getCount());
	}

	/**
	 * Minimal regular offset
	 */
	public long getMinOffset() {
		return minOffset;
	}

	/**
	 * Maximum regular offset
	 */
	public long getMaxOffset() {
		return maxOffset;
	}
	
	/**
	 * Minimal "New Minute" offset
	 */
	public long getMinOffsetNewMin() {
		return minOffsetNewMinute;
	}

	/**
	 * Maximum "New Minute" offset
	 */
	public long getMaxOffsetNewMin() {
		return maxOffsetNewMinute;
	}
	
	/**
	 * Minimal "New Minute Eve" offset
	 */
	public long getMinOffsetNewMinEve() {
		return minOffsetNewMinuteEve;
	}

	/**
	 * Maximum "New Minute Eve" offset
	 */
	public long getMaxOffsetNewMinEve() {
		return maxOffsetNewMinuteEve;
	}


	/**
	 * Adds and offset to the list
	 * @param offset miliseconds
	 */
	public void addOffset(long offset){
		offsetList.add(offset);
		totalOffset += offset;
		maxOffset = maxOffset>offset? maxOffset : offset;
		minOffset = minOffset<offset? minOffset : offset;		
	}
	
	/**
	 * Adds and offset to the New Minute offset list
	 * @param offset miliseconds
	 */
	public void addOffsetNewMinute(long offset){
		offsetNewMinute.add(offset);
		totalOffsetNewMinute += offset;
		maxOffsetNewMinute = maxOffsetNewMinute>offset? maxOffsetNewMinute : offset;
		minOffsetNewMinute = minOffsetNewMinute<offset? minOffsetNewMinute : offset;		
	}
	
	/**
	 * Adds and offset to the New Minute Eve offset list
	 * @param offset miliseconds
	 */
	public void addOffsetNewMinuteEve(long offset){
		offsetNewMinuteEve.add(offset);
		totalOffsetNewMinuteEve += offset;
		maxOffsetNewMinuteEve = maxOffsetNewMinuteEve>offset? maxOffsetNewMinuteEve : offset;
		minOffsetNewMinuteEve = minOffsetNewMinuteEve<offset? minOffsetNewMinuteEve : offset;		
	}
	
	/**
	 * Calculates the offset between two dates and adds it to the proper offset list structures
	 * @param date1
	 * @param date2
	 * @param displaysSeconds
	 */
	public void addOffset(String cameraTime, String MedDeviceTime, boolean displaysSeconds){
		//1. Calculate offset between dates
		long offset = UtilsDTS.getOffsetFromDates(cameraTime, MedDeviceTime);
		addOffset(offset);
		//2. a) if the M.device displays seconds, offset is the same for the 3 scenarios
		if(displaysSeconds){
			addOffsetNewMinute(offset);
			addOffsetNewMinuteEve(offset);
		}else{
			//2. b) if not, we need to calculate the offset for the new Minute & New Minute Eve scenarios
			String newMinuteScn = UtilsDTS.getNewMinuteDate(MedDeviceTime);
			offset = UtilsDTS.getOffsetFromDates(cameraTime, newMinuteScn);
			addOffsetNewMinute(offset);
			
			String newMinuteEveScn = UtilsDTS.getNewMinuteEveDate(MedDeviceTime);
			offset = UtilsDTS.getOffsetFromDates(cameraTime, newMinuteEveScn);
			addOffsetNewMinuteEve(offset);			
		}		
	}

}
