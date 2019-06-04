package ua.kpi.iasa.parallel.course.main.methods.strategies.uniform;

import org.jzy3d.maths.Range;

import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.main.methods.strategies.DiffeqSolutionStrategy;

public abstract class AbstractUniformDiffeqSolutionStrategy
		implements DiffeqSolutionStrategy {
	
	@Override
	public abstract UniformGrid solveDifferentialEquation(Range xRange, Range tRange,
			int xSteps, int tSteps);

}
