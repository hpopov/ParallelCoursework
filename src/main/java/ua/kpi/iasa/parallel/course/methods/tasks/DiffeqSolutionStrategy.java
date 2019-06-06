package ua.kpi.iasa.parallel.course.methods.tasks;

import org.jzy3d.maths.Range;

import javafx.concurrent.Task;
import ua.kpi.iasa.parallel.course.data.UniformGrid;

public interface DiffeqSolutionStrategy {
	Task<UniformGrid> makeDiffeqSolutionTask(Range xRange, Range tRange,
			int xSteps, int tSteps);
//	Grid solveDifferentialEquation(Range xRange, Range tRange,
//			int xSteps, int tSteps);
}
