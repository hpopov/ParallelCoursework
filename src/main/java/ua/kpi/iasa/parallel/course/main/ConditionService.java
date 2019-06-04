package ua.kpi.iasa.parallel.course.main;

import java.util.function.DoubleUnaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ConditionService {

	@Autowired
	private PreciseSolutionService preciseSolutionService;
	
	@Autowired
	private MainParametersService mainParametersService;
	
	public DoubleUnaryOperator getFirstInitialCondition() {
		return preciseSolutionService.getTimeLevelFunction(mainParametersService.tMinProperty().get());
	}
	
	public DoubleUnaryOperator getSecondInitialCondition(final double dt) {
		return preciseSolutionService.getTimeLevelFunction(mainParametersService.tMinProperty().get() + dt);
	}
	
	public DoubleUnaryOperator getLeftEdgeCondition() {
		return preciseSolutionService.getXLevelFunction(mainParametersService.xMinProperty().get());
	}
	
	public DoubleUnaryOperator getRightEdgeCondition() {
		return preciseSolutionService.getXLevelFunction(mainParametersService.xMaxProperty().get());
	}
}
