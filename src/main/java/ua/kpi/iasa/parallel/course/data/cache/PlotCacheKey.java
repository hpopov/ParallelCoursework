package ua.kpi.iasa.parallel.course.data.cache;

import org.jzy3d.maths.Range;

public class PlotCacheKey {

	private int xSteps;
	private int tSteps;
	private Range xRange;
	private Range tRange;
	private CalculationType calculationType;
	private boolean isDifference;
	
	public PlotCacheKey() {
		setDifference(false);
	}
	
	public PlotCacheKey(PlotCacheKey plotCacheKey) {
		this();
		setCalculationType(plotCacheKey.calculationType);
		setDifference(plotCacheKey.isDifference);
		setTRange(plotCacheKey.tRange);
		setTSteps(plotCacheKey.tSteps);
		setXRange(plotCacheKey.xRange);
		setXSteps(plotCacheKey.xSteps);
	}

	public int getXSteps() {
		return xSteps;
	}
	
	public void setXSteps(int xSteps) {
		this.xSteps = xSteps;
	}
	
	public int getTSteps() {
		return tSteps;
	}
	
	public void setTSteps(int tSteps) {
		this.tSteps = tSteps;
	}
	
	public Range getXRange() {
		return xRange;
	}
	
	public void setXRange(Range xRange) {
		this.xRange = xRange;
	}
	
	public Range getTRange() {
		return tRange;
	}
	
	public void setTRange(Range tRange) {
		this.tRange = tRange;
	}
	
	public CalculationType getPlotDataType() {
		return calculationType;
	}
	
	public void setCalculationType(CalculationType calculationType) {
		this.calculationType = calculationType;
	}

	public boolean isDifference() {
		return isDifference;
	}

	public void setDifference(boolean isDifference) {
		this.isDifference = isDifference;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (obj == null || obj.hashCode() != hashCode()) {
			return false;
		}
		if (! (obj instanceof PlotCacheKey)) {
			return false;
		}
		PlotCacheKey other = (PlotCacheKey) obj;
		return isDifference == other.isDifference && calculationType == other.calculationType 
				&& xSteps == other.xSteps && tSteps == other.tSteps
				&& rangeEquals(xRange, other.xRange) && rangeEquals(tRange, other.tRange);
	}
	
	private boolean rangeEquals(Range range1, Range range2) {
		return range1.getMin() == range2.getMin() && range1.getMax() == range2.getMax();
	}

	@Override
	public int hashCode() {
		int hash = xSteps;
		hash <<= 3;
		hash |= tSteps;
		hash <<= 3;
		hash |= (int) xRange.getRange();
		hash <<= 3;
		hash |= (int) tRange.getRange();
		hash <<= 3;
		hash |= calculationType.hashCode();
		hash <<= 1;
		hash |= isDifference? 1 : 0;
		return hash;
	}
	
	@Override
	public String toString() {
		return String.format("{calculation type: %s, xRange: [%s,%s], tRange: [%s,%s],"
				+ " xSteps:%s, tSteps:%s,"
				+ (isDifference? "FOR DIFFERENCE":"") +"}", calculationType, xRange.getMin(), xRange.getMax(),
				tRange.getMin(), tRange.getMax(), xSteps, tSteps);
	}
	

//	private int xSteps;
//	private int tSteps;
//	private Range xRange;
//	private Range tRange;
//	private CalculationType calculationType;
//	private boolean isDifference;
}
