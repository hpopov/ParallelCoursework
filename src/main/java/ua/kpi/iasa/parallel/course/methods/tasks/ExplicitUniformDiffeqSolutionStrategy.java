package ua.kpi.iasa.parallel.course.methods.tasks;

import static org.junit.Assert.fail;

import java.util.Date;
import java.util.function.DoubleUnaryOperator;

import org.jzy3d.maths.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javafx.concurrent.Task;
import ua.kpi.iasa.parallel.course.TaskFailedException;
import ua.kpi.iasa.parallel.course.data.AbstractUniformGrid;
import ua.kpi.iasa.parallel.course.data.GridValuePointer;
import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.methods.ExplicitPointResolver;
import ua.kpi.iasa.parallel.course.services.ConditionService;
import ua.kpi.iasa.parallel.course.services.MainParametersService;

@Component("explicitSequentialDiffeqSolutionStrategy")
public class ExplicitUniformDiffeqSolutionStrategy extends AbstractDiffeqSolutionStrategy{

	@Autowired
	protected MainParametersService mainParametersService;

	@Autowired
	protected ConditionService conditionService;

	@Override
	public Task<UniformGrid> makeDiffeqSolutionTask(Range xRange, Range tRange,
			int xSteps, int tSteps) {
		return new Task<UniformGrid>() {

			@Override
			protected UniformGrid call() throws Exception {
				Date startDate = logBefore("explicitSequentialDiffeqSolutionStrategy");
//				long workDone = 0;
				double dWork = 1./tSteps;
				double work = 0;
//				updateProgress(0, tSteps);
				updateMessage("Building plot grid values...");
				updateProgress(work, 1.1);
				UniformGrid grid = new UniformGrid(xRange, tRange, xSteps, tSteps);
				GridValuePointer gridPointer = makeGridPointerFilledWithInitialCondidions(grid);
//				workDone = 2;
				work = 2*dWork;
//				updateProgress(workDone, tSteps);
				updateProgress(work, 1.1);
				final DoubleUnaryOperator leftEdgeCondition = 
						conditionService.getLeftEdgeCondition();
				final DoubleUnaryOperator rightEdgeCondition = 
						conditionService.getRightEdgeCondition();
				final double dx = grid.getDx();
				final double dt = grid.getDt();
				final ExplicitPointResolver resolver = 
						new ExplicitPointResolver(dt, dx, mainParametersService.getAlpha());

				int iterPerUpdate = tSteps/100;
				int iter = 0;
				while(gridPointer.canMakePositiveTStep()) {
					if (isCancelled()) {
						return null;
					}
					gridPointer.makePositiveTStepResettingX();
					gridPointer.makePositiveXStep();
					gridPointer.setCurrentValueApplyingToT(leftEdgeCondition);
					gridPointer.makePositiveXStep();
					while(gridPointer.canMakePositiveXStep()) {
						gridPointer.setCurrentValue(calculatePointValue(gridPointer, resolver));
						gridPointer.makePositiveXStep();
					}
					gridPointer.setCurrentValueApplyingToT(rightEdgeCondition);
					work += dWork;
					if (iter++ == iterPerUpdate) {
						iter = 0;
						updateProgress(work, 1.1);
					}
//					updateProgress(++workDone, tSteps);
//					Thread.yield();
				}
//				updateProgress(tSteps, tSteps);
				logAfter(startDate, grid.getGridNodeValues());
				updateMessage("Building node points for plot...");
				if (grid.getTStepsCount() * grid.getTStepsCount() > 50_000_000) {
					throw new TaskFailedException("There are too many plot points to gather them to list");
				}
				grid.buildNodePoints();
				updateProgress(1.1, 1.1);
				return grid;
			}

		};
	}
	
	protected double calculatePointValue(GridValuePointer gridPointer, ExplicitPointResolver resolver) {
		final double wBottom = gridPointer.getRelativeValue(0, -2);
		final double wLeft = gridPointer.getRelativeValue(-1, -1);
		final double wCenter = gridPointer.getRelativeValue(0, -1);
		final double wRight = gridPointer.getRelativeValue(1, -1);
		return resolver.wTop(wBottom, wLeft, wCenter, wRight);
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

}
