package ua.kpi.iasa.parallel.course.data;

import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;

import ua.com.kl.cmathtutor.concurrency.NonBlockedConcurrentLinkedList;

public class UniformGrid extends AbstractUniformGrid {

	private final double[][] nodeValues;
	private final List<Coord3d> nodePoints;
	private final double dx;
	private final double dt;
	private boolean hasPointer = false;

	public UniformGrid(Range xRange, Range tRange, int xStepsCount, int tStepsCount) {
		super(xRange.getMin(), tRange.getMin(), xStepsCount, tStepsCount);
		nodeValues = new double[tStepsCount][xStepsCount];
		nodePoints = new NonBlockedConcurrentLinkedList<>();
		dx = xRange.getRange()/(xStepsCount-1);
		dt = tRange.getRange()/(tStepsCount-1);
	}

	@Override
	public GridValuePointer gridValuePointer() {
		if (!hasPointer) {
			hasPointer = true;
			return new UniformGridValuePointer();
		}
		throw new IllegalStateException("Current grid has already had a pointer!");
	}
	
	private void addNodePoint(Coord3d point) {
		nodePoints.add(point);
	}
	
	@Override
	public double getDx() {
		return dx;
	}

	@Override
	public double getDt() {
		return dt;
	}

	@Override
	public List<Coord3d> getGridNodePoints() {
//		return new ArrayList<>(nodePoints.values());
		return nodePoints;
	}

	@Override
	public double[][] getGridNodeValues() {
		return nodeValues;
	}

	@Override
	public AbstractUniformGrid subTGrid(int fromTInd, int tStepsCount) {
		if (fromTInd < 0 || tStepsCount > getTStepsCount()) {
			throw new IllegalArgumentException("Illegal bounds for sublist."
					+ String.format("Got fromTInd: %s, tStepsCount: %s,", fromTInd, tStepsCount)
					+ String.format(" while have fromTInd: %s, tStepsCount: %s", 0, getTStepsCount()));
		}
		return new UniformSubGrid(0, fromTInd, getXStepsCount(), tStepsCount);
	}

	@Override
	public AbstractUniformGrid subXGrid(int fromXInd, int xStepsCount) {
		if (fromXInd < 0 || xStepsCount > getXStepsCount()) {
			throw new IllegalArgumentException("Illegal bounds for sublist."
					+ String.format("Got fromXInd: %s, xStepsCount: %s,", fromXInd, xStepsCount)
					+ String.format(" while have fromXInd: %s, xStepsCount: %s", 0, getXStepsCount()));
		}
		return new UniformSubGrid(fromXInd, 0, xStepsCount, getTStepsCount());
	}
	
	private class UniformGridValuePointer extends AbstractValuePointer{
		
		public UniformGridValuePointer() {
			super();
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
		public void setCurrentValue(double value) {
			nodeValues[currentTInd][currentXInd] = value;
			if (currentPoint == null) {
				addNodePoint(currentPoint = new Coord3d(currentXCoord, currentTCoord, value));
			} else {
				currentPoint.z = (float) value;
			}
		}
	}
	
	private class UniformSubGrid extends AbstractUniformGrid {
		
		private final int minXInd;
		private final int minTInd;
		private boolean hasPointer = false;

		public UniformSubGrid(int minXInd, int minTInd, int xStepsCount, int tStepsCount) {
			super(UniformGrid.this.minX + minXInd*dx, UniformGrid.this.minT +  minTInd*dt,
					xStepsCount, tStepsCount);
			this.minXInd = minXInd;
			this.minTInd = minTInd;
		}

		@Override
		public GridValuePointer gridValuePointer() {
			if (!hasPointer) {
				hasPointer = true;
				return new UniformSubGridValuePointer();
			}
			throw new IllegalStateException("Current subGrid has already had a pointer!");
		}

		@Override
		public double getDx() {
			return dx;
		}

		@Override
		public double getDt() {
			return dt;
		}

		@Override
		public List<Coord3d> getGridNodePoints() {
			throw new UnsupportedOperationException("getGridNodePoints is unsupported in"
					+ " UniformSubGridValuePointer due to high unefficiency");
		}

		@Override
		public double[][] getGridNodeValues() {
			throw new UnsupportedOperationException("getGridNodeValues is unsupported in"
					+ " UniformSubGridValuePointer due to high unefficiency");
		}

		@Override
		public AbstractUniformGrid subXGrid(int fromXInd, int xStepsCount) {
			if (fromXInd < minXInd || xStepsCount > getXStepsCount()) {
				throw new IllegalArgumentException("Illegal bounds for sublist."
						+ String.format("Got fromXInd: %s, xStepsCount: %s,", fromXInd, xStepsCount)
						+ String.format(" while have fromXInd: %s, xStepsCount: %s",
								minXInd, getXStepsCount()));
			}
			return new UniformSubGrid(fromXInd, minTInd, xStepsCount, getTStepsCount());
		}
		
		@Override
		public AbstractUniformGrid subTGrid(int fromTInd, int tStepsCount) {
			if (fromTInd < minTInd || tStepsCount > getTStepsCount()) {
				throw new IllegalArgumentException("Illegal bounds for sublist."
						+ String.format("Got fromTInd: %s, tStepsCount: %s,", fromTInd, tStepsCount)
						+ String.format(" while have fromTInd: %s, tStepsCount: %s",
								minTInd, getTStepsCount()));
			}
			return new UniformSubGrid(minXInd, fromTInd, getXStepsCount(), tStepsCount);
		}
		
		private class UniformSubGridValuePointer extends AbstractValuePointer {

			public UniformSubGridValuePointer() {
				super(minXInd, minTInd);
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
			public void setCurrentValue(double value) {
				nodeValues[currentTInd][currentXInd] = value;
				if (currentPoint == null) {
					addNodePoint(currentPoint = new Coord3d(currentXCoord, currentTCoord, value));
				} else {
					currentPoint.z = (float) value;
				}
			}
			
		}
		
	}


}
