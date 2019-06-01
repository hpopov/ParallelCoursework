package ua.kpi.iasa.parallel.course.main;

import java.util.List;

import org.jzy3d.maths.Range;
import org.jzy3d.maths.doubles.Coord3D;


public interface DiffeqCalculationMethod {
	String getName();
	boolean allowManualTSptepsResizing();
	List<Coord3D> solveDiffEquation(Range xRange, Range tRange, int xSteps, int tSteps);
}
