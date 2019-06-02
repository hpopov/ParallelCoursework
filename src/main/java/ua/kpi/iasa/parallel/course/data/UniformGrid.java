package ua.kpi.iasa.parallel.course.data;

import java.util.LinkedList;
import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;

public class UniformGrid {

	private final double[][] nodeValues;
	private final List<Coord3d> nodePoints;
	private final Range xRange;
	private final Range tRange;
	private final int xStepsCount;
	private final int tStepsCount;
	private final double dx;
	private final double dt;
	private boolean hasPointer = false;

	public UniformGrid(Range xRange, Range tRange, int xStepsCount, int tStepsCount) {
		if (xStepsCount < 0) {
			throw new IllegalArgumentException("The number of xSteps should be positive!");
		}
		if (tStepsCount < 0) {
			throw new IllegalArgumentException("The number of tSteps should be positive!");
		}
		this.xRange = xRange;
		this.tRange = tRange;
		this.xStepsCount = xStepsCount;
		this.tStepsCount = tStepsCount;
		nodeValues = new double[tStepsCount][xStepsCount];
		nodePoints = new LinkedList<>();
		dx = xRange.getRange()/(xStepsCount-1);
		dt = tRange.getRange()/(tStepsCount-1);
	}

	public GridValuePointer gridValuePointer() {
		if (!hasPointer) {
			hasPointer = true;
			return new UniformGridValuePointer();
		}
		throw new IllegalStateException("Current grid has already had a pointer!");
	}
	
	public double getDx() {
		return dx;
	}
	
	public double getDt() {
		return dt;
	}
	
	public List<Coord3d> getGridNodePoints() {
		return nodePoints;
	}

	private class UniformGridValuePointer implements GridValuePointer{

		private int currentXInd;
		private double currentXCoord;
		private int currentTInd;
		private double currentTCoord;
		private Coord3d currentPoint;
		
		public UniformGridValuePointer() {
			currentXInd = -1;
			currentTInd = -1;
			currentXCoord = xRange.getMin() - dx;
			currentTCoord = tRange.getMin() - dt;
		}
		
		@Override
		public void makePositiveXStep() {
			if (!canMakePositiveXStep()) {
				throw new IllegalStateException("Grid value pointer can't make positive x step");
			}
			currentPoint = null;
			currentXInd++;
			currentXCoord += dx;
		}

		@Override
		public void makePositiveTStep() {
			if (!canMakePositiveTStep()) {
				throw new IllegalStateException("Grid value pointer can't make positive t step");
			}
			currentPoint = null;
			currentTInd++;
			currentTCoord += dt;
		}

		@Override
		public void makePositiveTStepResettingX() {
			makePositiveTStep();
			currentXInd = -1;
			currentXCoord = xRange.getMin() - dx;
		}

		@Override
		public double getCurrentValue() {
			return nodeValues[currentTInd][currentXInd];
		}

		@Override
		public double getRelativeValue(int xSteps, int tSteps) {
			return nodeValues[currentTInd + tSteps][currentXInd + xSteps];
		}

		@Override
		public double getCurrentX() {
			return currentXCoord;
		}

		@Override
		public double getCurrentT() {
			return currentTCoord;
		}

		@Override
		public void setCurrentValue(double value) {
			nodeValues[currentTInd][currentXInd] = value;
			if (currentPoint == null) {
				nodePoints.add(currentPoint = new Coord3d(currentXCoord, currentTCoord, value));
			} else {
				currentPoint.z = (float) value;
			}
		}

		@Override
		public boolean canMakePositiveXStep() {
			return currentXInd < xStepsCount-1;
		}

		@Override
		public boolean canMakePositiveTStep() {
			return currentTInd < tStepsCount-1;
		}

	}

}
