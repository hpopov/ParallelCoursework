package ua.kpi.iasa.parallel.course.main.methods.impl;

import java.util.List;
import java.util.function.DoubleUnaryOperator;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ua.kpi.iasa.parallel.course.data.GridValuePointer;
import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.main.PreciseSolutionService;
import ua.kpi.iasa.parallel.course.main.methods.AbstractDiffeqCalculationMethod;

@Component
public class ExplicitDiffeqCalculationMethod extends AbstractDiffeqCalculationMethod {

	@Autowired
	public ExplicitDiffeqCalculationMethod(PreciseSolutionService preciseSolutionService) {
		super(preciseSolutionService);
	}

	@Override
	public String getName() {
		return "Explicit method";
	}

	@Override
	public List<Coord3d> solveDiffEquation(Range xRange, Range tRange, int xSteps, int tSteps) {
		if (xSteps < 2) {
			throw new IllegalArgumentException("The number of xSteps should be at least 2!");
		}
		if (tSteps < 2) {
			throw new IllegalArgumentException("The number of tSteps should be at least 2!");
		}
//		return solveDiffEquationOnSingleThread(xRange, tRange, xSteps, tSteps);

		UniformGrid grid = new UniformGrid(xRange, tRange, xSteps, tSteps);
		final double dx = grid.getDx();
		final double dt = grid.getDt();
		final PointResolver resolver = 
				new PointResolver(dt, dx, preciseSolutionService.getAlpha());
		GridValuePointer gridPointer = grid.gridValuePointer();
		for(int i = 0; i<2 && gridPointer.canMakePositiveTStep(); i++) {
			gridPointer.makePositiveTStepResettingX();
			final DoubleUnaryOperator initialCondition = 
					preciseSolutionService.getTimeLevelFunction(gridPointer.getCurrentT());
			gridPointer.setValueForEachRemainingX(initialCondition);
		}

		final DoubleUnaryOperator leftBorderCondition = 
				preciseSolutionService.getXLevelFunction(xRange.getMin());
		final DoubleUnaryOperator rightBorderCondition = 
				preciseSolutionService.getXLevelFunction(xRange.getMax());
		
		while(gridPointer.canMakePositiveTStep()) {
			gridPointer.makePositiveTStepResettingX();
			gridPointer.makePositiveXStep();
			gridPointer.setCurrentValueApplyingToT(leftBorderCondition);
			gridPointer.makePositiveXStep();
			while(gridPointer.canMakePositiveXStep()) {
				final double wBottom = gridPointer.getRelativeValue(0, -2);
				final double wLeft = gridPointer.getRelativeValue(-1, -1);
				final double wCenter = gridPointer.getRelativeValue(0, -1);
				final double wRight = gridPointer.getRelativeValue(1, -1);
				gridPointer.setCurrentValue(resolver.wTop(wBottom, wLeft, wCenter, wRight));
				gridPointer.makePositiveXStep();
			}
			gridPointer.setCurrentValueApplyingToT(rightBorderCondition);
		}
		return grid.getGridNodePoints();
	}

//	private List<Coord3D> solveDiffEquationOnSingleThread(final Range xRange, final Range tRange,
//			int xSteps, int tSteps) {
//		final double dx = xRange.getRange()/(xSteps-1);
//		final double dt = tRange.getRange()/(tSteps-1);
//		final PointResolver resolver = 
//				new PointResolver(dt, dx, preciseSolutionService.getAlpha());
//
//		final List<Coord3D> solutionPoints = new LinkedList<>();
//		final double[][] pointGrid = new double[xSteps][tSteps];
//		for(int i = 0; i<2; i++) {
//			double t = tRange.getMin() + i*dt;
//			final DoubleUnaryOperator initialCondition = 
//					preciseSolutionService.getTimeLevelFunction(t);
//			double x = xRange.getMin();
//			for(int j = 0; j<xSteps; j++) {
//				pointGrid[i][j] = initialCondition.applyAsDouble(x);
//				solutionPoints.add(new Coord3D(x, t, pointGrid[i][j]));
//				x += dx;
//			}
//			t += dt;
//		}
//
//		final DoubleUnaryOperator leftBorderCondition = 
//				preciseSolutionService.getXLevelFunction(xRange.getMin());
//		final DoubleUnaryOperator rightBorderCondition = 
//				preciseSolutionService.getXLevelFunction(xRange.getMax());
//		
//		double t = tRange.getMin() + 2*dt;
//		double x = xRange.getMin();
//	}

	@Override
	public List<Coord3d> solveDiffEquationConcurrently(Range xRange, Range tRange, int xSteps,
			int tSteps) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int findSuitableTSteps(Range xRange, Range tRange, int xSteps, int tSteps) {
		return tSteps;
	}

	@Override
	public String toString() {
		return getName();
	}

	private class PointResolver {

		private final double sigma;
		private final double dx, dt;

		public PointResolver(double dt, double dx, double alpha) {
			this.dx = dx;
			this.dt = dt;
			sigma = 2*alpha*dt/(dx*dx);
		}

		public double wTop(double wBottom, double wLeft, double wCenter, double wRight) {
			return wBottom 
					+ sigma * Math.pow(wCenter, -1./3) * (
							Math.pow(wRight-wLeft, 2)/6 + wCenter * (wLeft - 2*wCenter + wRight)
							);
		}
	}

}
