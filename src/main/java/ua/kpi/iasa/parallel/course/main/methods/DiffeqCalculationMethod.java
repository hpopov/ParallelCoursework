package ua.kpi.iasa.parallel.course.main.methods;

import java.util.List;

import org.jzy3d.maths.Range;
import org.jzy3d.maths.doubles.Coord3D;

public interface DiffeqCalculationMethod {
	String getName();
	List<Coord3D> solveDiffEquation(Range xRange, Range tRange, int xSteps, int tSteps);
	List<Coord3D> solveDiffEquationConcurrently(Range xRange, Range tRange, int xSteps, int tSteps);
}
