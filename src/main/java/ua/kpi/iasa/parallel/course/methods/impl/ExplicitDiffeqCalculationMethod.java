package ua.kpi.iasa.parallel.course.methods.impl;

import org.jzy3d.maths.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javafx.concurrent.Task;
import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.data.cache.CalculationType;
import ua.kpi.iasa.parallel.course.methods.DiffeqCalculationMethod;
import ua.kpi.iasa.parallel.course.methods.tasks.DiffeqSolutionStrategy;

@Component
public class ExplicitDiffeqCalculationMethod implements DiffeqCalculationMethod {

	@Autowired
	@Qualifier("explicitSequentialDiffeqSolutionStrategy")
	private DiffeqSolutionStrategy sequentialDiffeqSolutionStrategy;

	@Autowired
	@Qualifier("explicitParallelDiffeqSolutionStrategy")
	private DiffeqSolutionStrategy parallelDiffeqSolutionStrategy;
	
	@Override
	public String getName() {
		return "Explicit method";
	}

	@Override
	public Task<UniformGrid> getSolveDiffEquationTask(Range xRange, Range tRange,
			int xSteps, int tSteps) {
		return sequentialDiffeqSolutionStrategy.makeDiffeqSolutionTask(xRange, tRange, xSteps, tSteps);
	}

	@Override
	public Task<UniformGrid> getSolveDiffEquationConcurrentlyTask(Range xRange, Range tRange, int xSteps,
			int tSteps) {
		return parallelDiffeqSolutionStrategy.makeDiffeqSolutionTask(xRange, tRange, xSteps, tSteps);
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public CalculationType getCalculationType() {
		return CalculationType.BUILT_EXPLICIT;
	}

}
