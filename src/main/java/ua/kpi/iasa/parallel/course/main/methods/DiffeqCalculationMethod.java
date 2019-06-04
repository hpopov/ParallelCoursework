package ua.kpi.iasa.parallel.course.main.methods;

import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;

public interface DiffeqCalculationMethod {
	String getName();
	List<Coord3d> solveDiffEquation(Range xRange, Range tRange, int xSteps, int tSteps);
	List<Coord3d> solveDiffEquationConcurrently(Range xRange, Range tRange,
			int xSteps, int tSteps);
	int findSuitableTSteps(Range xRange, Range tRange, int xSteps, int tSteps);
}
