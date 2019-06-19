package ua.kpi.iasa.parallel.course.data;

import org.jzy3d.maths.Coord3d;

public abstract class AbstractUniformGrid implements Grid {
	protected final double minX;
	protected final double minT;
	private final int xStepsCount;
	private final int tStepsCount;

	public AbstractUniformGrid(double minX, double minT, int xStepsCount, int tStepsCount) {
		if (xStepsCount < 0) {
			throw new IllegalArgumentException("The number of xSteps should be positive!");
		}
		if (tStepsCount < 0) {
			throw new IllegalArgumentException("The number of tSteps should be positive!");
		}
		this.minX = minX;
		this.minT = minT;
		this.xStepsCount = xStepsCount;
		this.tStepsCount = tStepsCount;
	}
	
	public int getXStepsCount() {
		 return xStepsCount;
	}
	
	public int getTStepsCount() {
		return tStepsCount;
	}

	@Override
	public abstract AbstractUniformGrid subTGrid(int fromTInd, int tStepsCount);
	
	@Override
	public abstract AbstractUniformGrid subXGrid(int fromXInd, int xStepsCount);
	
	public abstract double[][] getGridNodeValues();
	
	public abstract double getDx();
	
	public abstract double getDt();
	
	@Override
	protected void finalize() throws Throwable {
		System.out.println(this.getClass().getSimpleName() + hashCode() + "finalized!");
		super.finalize();
	}

	protected abstract class AbstractValuePointer implements GridValuePointer{

		private final int minXInd;
		private final int minTInd;
		protected int currentXInd;
		protected double currentXCoord;
		protected int currentTInd;
		protected double currentTCoord;
		protected Coord3d currentPoint;
		
		public AbstractValuePointer() {
			this(0, 0);
		}
		
		public AbstractValuePointer(int minXInd, int minTInd) {
			if (minXInd < 0 || minTInd < 0) {
				throw new IllegalArgumentException("None of GridValuePointer minimum indices"
						+ " could be negative!");
			}
			this.minXInd = minXInd;
			this.minTInd = minTInd;
			currentXInd = minXInd - 1;
			currentTInd = minTInd - 1;
			currentXCoord = minX - getDx();
			currentTCoord = minT - getDt();
		}
		
		@Override
		public void makePositiveXStep() {
			if (!canMakePositiveXStep()) {
				throw new IllegalStateException("AbstractUniformGrid value pointer can't"
						+ " make positive x step");
			}
			currentPoint = null;
			currentXInd++;
			currentXCoord += getDx();
		}

		@Override
		public void makePositiveTStep() {
			if (!canMakePositiveTStep()) {
				throw new IllegalStateException("AbstractUniformGrid value pointer can't"
						+ " make positive t step");
			}
			currentPoint = null;
			currentTInd++;
			currentTCoord += getDt();
		}

		@Override
		public void makePositiveTStepResettingX() {
			makePositiveTStep();
			currentXInd = minXInd - 1;
			currentXCoord = minX - getDx();
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
		public boolean canMakePositiveXStep() {
			return currentXInd < minXInd + xStepsCount-1;
		}

		@Override
		public boolean canMakePositiveTStep() {
			return currentTInd < minTInd + tStepsCount-1;
		}

		@Override
		public void makeNegativeXStep() {
			if (!canMakeNegativeXStep()) {
				throw new IllegalStateException("AbstractUniformGrid value pointer can't"
						+ " make negative x step");
			}
			currentPoint = null;
			currentXInd--;
			currentXCoord -= getDx();
		}

		@Override
		public void makeNegativeTStep() {
			if (!canMakeNegativeTStep()) {
				throw new IllegalStateException("AbstractUniformGrid value pointer can't"
						+ " make negative t step");
			}
			currentPoint = null;
			currentTInd--;
			currentTCoord -= getDt();
		}

		@Override
		public boolean canMakeNegativeXStep() {
			return currentXInd > minXInd;
		}

		@Override
		public boolean canMakeNegativeTStep() {
			return currentTInd > minTInd;
		}

		@Override
		protected void finalize() throws Throwable {
			System.out.println(this.getClass().getSimpleName() + hashCode() + "finalized!");
			super.finalize();
		}
	}
}
