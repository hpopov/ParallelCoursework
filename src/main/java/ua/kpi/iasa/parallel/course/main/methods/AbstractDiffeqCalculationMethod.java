package ua.kpi.iasa.parallel.course.main.methods;

import ua.kpi.iasa.parallel.course.main.PreciseSolutionService;

public abstract class AbstractDiffeqCalculationMethod implements DiffeqCalculationMethod {

	protected final PreciseSolutionService preciseSolutionService;

	public AbstractDiffeqCalculationMethod(PreciseSolutionService preciseSolutionService) {
		this.preciseSolutionService = preciseSolutionService;
	}
	
	

}
