package ua.kpi.iasa.parallel.course.main.methods.impl;

import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import ua.kpi.iasa.parallel.course.data.Grid;
import ua.kpi.iasa.parallel.course.main.methods.DiffeqCalculationMethod;
import ua.kpi.iasa.parallel.course.main.methods.strategies.DiffeqSolutionStrategy;

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
	public List<Coord3d> solveDiffEquation(Range xRange, Range tRange, int xSteps, int tSteps) {
		Grid grid =
				sequentialDiffeqSolutionStrategy.solveDifferentialEquation(xRange, tRange, xSteps, tSteps);
		return grid.getGridNodePoints();
	}

	@Override
	public List<Coord3d> solveDiffEquationConcurrently(Range xRange, Range tRange, int xSteps,
			int tSteps) {
		Grid grid =
				parallelDiffeqSolutionStrategy.solveDifferentialEquation(xRange, tRange, xSteps, tSteps);
		return grid.getGridNodePoints();
	}
	
	@Override
	public int findSuitableTSteps(Range xRange, Range tRange, int xSteps, int tSteps) {
		return tSteps;
	}

	@Override
	public String toString() {
		return getName();
	}

}
