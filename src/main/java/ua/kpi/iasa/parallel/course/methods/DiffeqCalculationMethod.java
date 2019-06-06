package ua.kpi.iasa.parallel.course.methods;

import org.jzy3d.maths.Range;

import javafx.concurrent.Task;
import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.data.cache.CalculationType;

public interface DiffeqCalculationMethod {
	String getName();
	Task<UniformGrid> getSolveDiffEquationTask(Range xRange, Range tRange, int xSteps,
			int tSteps);
	Task<UniformGrid> getSolveDiffEquationConcurrentlyTask(Range xRange, Range tRange,
			int xSteps, int tSteps);
	CalculationType getCalculationType();
}
