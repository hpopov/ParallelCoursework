package ua.kpi.iasa.parallel.course.main;

import java.util.function.DoubleBinaryOperator;

import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.springframework.stereotype.Service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

import static java.lang.Math.pow;

@Service
public class EquationService {
	private static final double A = 0;
	private static final double B = 0;

	private final DoubleProperty xMinProperty;
	private final DoubleProperty xMaxProperty;
	private final IntegerProperty xStepsProperty;
	private final DoubleProperty tMinProperty;
	private final DoubleProperty tMaxProperty;
	private final IntegerProperty tStepsProperty;
	private double alpha = 1;
	
	
	public EquationService() {
		xMinProperty = new SimpleDoubleProperty(-10);
		xMaxProperty = new SimpleDoubleProperty(10);
		xStepsProperty = new SimpleIntegerProperty(20);
		tMinProperty = new SimpleDoubleProperty(-10);
		tMaxProperty = new SimpleDoubleProperty(10);
		tStepsProperty = new SimpleIntegerProperty(20);
	}
	
	private double fPrecise(double x, double t) {
		return pow(pow((x-A), 2)/(8*alpha*(B-t)),1.5);
	}
	
	public DoubleProperty xMinProperty() {
		return xMinProperty;
	}

	public DoubleProperty xMaxProperty() {
		return xMaxProperty;
	}

	public IntegerProperty xStepsProperty() {
		return xStepsProperty;
	}

	public DoubleProperty tMinProperty() {
		return tMinProperty;
	}

	public DoubleProperty tMaxProperty() {
		return tMaxProperty;
	}

	public IntegerProperty tStepsProperty() {
		return tStepsProperty;
	}
	
	public OrthonormalGrid getOrthonormalGrid() {
		Range xrange = new Range((float)xMinProperty.get(), (float)xMaxProperty.get());
		int xsteps = xStepsProperty.get();
		Range trange = new Range((float)tMinProperty.get(), (float)tMaxProperty.get());
		int tsteps = tStepsProperty.get();
		return new OrthonormalGrid(xrange, xsteps, trange, tsteps);
	}
	
	public DoubleBinaryOperator getPreciseSolutionFunction() {
		return this::fPrecise;
	}
	
	
	
}
