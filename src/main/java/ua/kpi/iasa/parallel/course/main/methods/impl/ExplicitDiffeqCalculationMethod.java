package ua.kpi.iasa.parallel.course.main.methods.impl;

import java.util.List;

import org.jzy3d.maths.Range;
import org.jzy3d.maths.doubles.Coord3D;

import ua.kpi.iasa.parallel.course.main.methods.DiffeqCalculationMethod;

public class ExplicitDiffeqCalculationMethod implements DiffeqCalculationMethod {

	@Override
	public String getName() {
		return "Explicit method";
	}

//	@Override
//	public boolean allowManualTSptepsResizing() {
//		return false;
//	}

	@Override
	public List<Coord3D> solveDiffEquation(Range xRange, Range tRange, int xSteps, int tSteps) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<Coord3D> solveDiffEquationConcurrently(Range xRange, Range tRange, int xSteps, int tSteps) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public String toString() {
		return getName();
	}

}
