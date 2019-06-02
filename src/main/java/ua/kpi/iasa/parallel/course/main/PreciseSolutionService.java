package ua.kpi.iasa.parallel.course.main;

import static java.lang.Math.pow;

import java.util.function.DoubleBinaryOperator;
import java.util.function.DoubleFunction;
import java.util.function.DoubleUnaryOperator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

@Service
public class PreciseSolutionService {
	
	private final DoubleProperty aProperty;
	private final DoubleProperty bProperty;
	private final double alpha = 1;

	public PreciseSolutionService() {
		aProperty = new SimpleDoubleProperty(0);
		bProperty = new SimpleDoubleProperty(0);
	}

	public DoubleBinaryOperator getPreciseSolutionFunction() {
		final double A = aProperty.get();
		final double B = bProperty.get();
		return (x,t)-> pow(pow((x-A), 2)/(8*alpha*(B-t)),1.5);
	}
	
	public DoubleUnaryOperator getTimeLevelFunction(double t) {
		DoubleBinaryOperator preciseSolution = getPreciseSolutionFunction();
		return x-> preciseSolution.applyAsDouble(x, t);
	}
	
	public DoubleUnaryOperator getXLevelFunction(double x) {
		DoubleBinaryOperator preciseSolution = getPreciseSolutionFunction();
		return t-> preciseSolution.applyAsDouble(x, t);
	}
	
	public DoubleProperty aProperty() {
		return aProperty;
	}
	
	public DoubleProperty bProperty() {
		return bProperty;
	}
	
	public double getAlpha() {
		return alpha;
	}
}
