package ua.kpi.iasa.parallel.course.main.methods.strategies;

import org.jzy3d.maths.Range;

import ua.kpi.iasa.parallel.course.data.Grid;

public interface DiffeqSolutionStrategy {
	public Grid solveDifferentialEquation(Range xRange, Range tRange,
			int desiredXSteps, int desiredTSteps);
}
