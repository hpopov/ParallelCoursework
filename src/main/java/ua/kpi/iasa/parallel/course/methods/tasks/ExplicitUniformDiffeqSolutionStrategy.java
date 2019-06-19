package ua.kpi.iasa.parallel.course.methods.tasks;

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
				double dWork = 1./tSteps;
				double work = 0;
				updateMessage(BUILDING_PLOT_GRID_VALUES_MESSAGE + "...");
				updateProgress(work, 1);
				UniformGrid grid = new UniformGrid(xRange, tRange, xSteps, tSteps);
				GridValuePointer gridPointer = makeGridPointerFilledWithInitialCondidions(grid);
				work = 2*dWork;
				updateProgress(work, 1);
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
						updateProgress(work, 1);
					}
				}
				logAfter(startDate, grid.getGridNodeValues());
				checkBuildNodePointsAbility(grid);
				updateProgress(1., 1.);
				return grid;
			}

			private void checkBuildNodePointsAbility(UniformGrid grid) throws TaskFailedException {
				updateProgress(-1., 1.);
				updateMessage(BUILDING_NODE_POINTS_FOR_PLOT_MESSAGE + "...");
				if (grid.getXStepsCount() * grid.getTStepsCount() > PLOT_POINTS_LIST_MAX_SIZE) {
					throw new TaskFailedException(BUILDING_NODE_POINTS_FOR_PLOT_MESSAGE + " failed",
							"There are too many plot points to gather them to list");
				}
				grid.buildNodePoints();
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
