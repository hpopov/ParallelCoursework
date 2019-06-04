package ua.kpi.iasa.parallel.course.main.methods.strategies.uniform;

import java.util.function.DoubleUnaryOperator;

import org.jzy3d.maths.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import ua.kpi.iasa.parallel.course.data.AbstractUniformGrid;
import ua.kpi.iasa.parallel.course.data.GridValuePointer;
import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.main.ConditionService;
import ua.kpi.iasa.parallel.course.main.MainParametersService;

@Component("explicitSequentialDiffeqSolutionStrategy")
public class ExplicitUniformDiffeqSolutionStrategy
		extends AbstractUniformDiffeqSolutionStrategy {
	
	@Autowired
	protected MainParametersService mainParametersService;
	
	@Autowired
	protected ConditionService conditionService;

	@Override
	public UniformGrid solveDifferentialEquation(Range xRange, Range tRange,
			int xSteps, int tSteps) {
		UniformGrid grid = new UniformGrid(xRange, tRange, xSteps, tSteps);
		GridValuePointer gridPointer = makeGridPointerFilledWithInitialCondidions(grid);

		final DoubleUnaryOperator leftEdgeCondition = 
				conditionService.getLeftEdgeCondition();
		final DoubleUnaryOperator rightEdgeCondition = 
				conditionService.getRightEdgeCondition();
		final double dx = grid.getDx();
		final double dt = grid.getDt();
		final PointResolver resolver = 
				new PointResolver(dt, dx, mainParametersService.getAlpha());
		
		while(gridPointer.canMakePositiveTStep()) {
			gridPointer.makePositiveTStepResettingX();
			gridPointer.makePositiveXStep();
			gridPointer.setCurrentValueApplyingToT(leftEdgeCondition);
			gridPointer.makePositiveXStep();
			while(gridPointer.canMakePositiveXStep()) {
				final double wBottom = gridPointer.getRelativeValue(0, -2);
				final double wLeft = gridPointer.getRelativeValue(-1, -1);
				final double wCenter = gridPointer.getRelativeValue(0, -1);
				final double wRight = gridPointer.getRelativeValue(1, -1);
				gridPointer.setCurrentValue(resolver.wTop(wBottom, wLeft, wCenter, wRight));
				gridPointer.makePositiveXStep();
			}
			gridPointer.setCurrentValueApplyingToT(rightEdgeCondition);
		}
		return grid;
	}

	protected GridValuePointer makeGridPointerFilledWithInitialCondidions(AbstractUniformGrid grid) {
		GridValuePointer gridPointer = grid.gridValuePointer();
		if (!gridPointer.canMakePositiveTStep()) {
			return gridPointer;
		}
		gridPointer.makePositiveTStep();
		gridPointer.setValueForEachRemainingXMovingPositively(conditionService.getFirstInitialCondition());
		if (!gridPointer.canMakePositiveTStep()) {
			return gridPointer;
		}
		gridPointer.makePositiveTStep();
		
		final DoubleUnaryOperator secondInitialCondition = 
				conditionService.getSecondInitialCondition(grid.getDt());
		gridPointer.setCurrentValueApplyingToX(secondInitialCondition);
		gridPointer.setValueForEachRemainingXMovingNegatively(secondInitialCondition);
		return gridPointer;
	}
	
	protected class PointResolver {

		private final double sigma;
		private final double dx, dt;

		public PointResolver(double dt, double dx, double alpha) {
			this.dx = dx;
			this.dt = dt;
			sigma = 2*alpha*dt/(dx*dx);
		}

		public double wTop(double wBottom, double wLeft, double wCenter, double wRight) {
			return wBottom + sigma * Math.pow(wCenter, -1./3) * (
					Math.pow(wRight-wLeft, 2)/6
					+ wCenter * (wLeft - 2*wCenter + wRight)
					);
		}
	}

}
