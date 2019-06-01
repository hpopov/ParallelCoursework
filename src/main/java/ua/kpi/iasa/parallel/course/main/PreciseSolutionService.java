package ua.kpi.iasa.parallel.course.main;

import static java.lang.Math.pow;

import java.util.function.DoubleBinaryOperator;

import org.springframework.stereotype.Service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

@Service
public class PreciseSolutionService {


	private final double alpha = 1;
	
	private final DoubleProperty aProperty;
	private final DoubleProperty bProperty;

	public PreciseSolutionService() {
		aProperty = new SimpleDoubleProperty(0);
		bProperty = new SimpleDoubleProperty(0);
	}

	public DoubleBinaryOperator getPreciseSolutionFunction() {
		final double A = aProperty.get();
		final double B = bProperty.get();
		return (x,t)-> pow(pow((x-A), 2)/(8*alpha*(B-t)),1.5);
	}
	
	public DoubleProperty aProperty() {
		return aProperty;
	}
	
	public DoubleProperty bProperty() {
		return bProperty;
	}
	
}
