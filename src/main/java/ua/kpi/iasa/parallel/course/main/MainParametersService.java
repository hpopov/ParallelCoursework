package ua.kpi.iasa.parallel.course.main;

import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.springframework.stereotype.Service;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;

@Service
public class MainParametersService {
	private final DoubleProperty xMinProperty;
	private final DoubleProperty xMaxProperty;
	private final IntegerProperty xStepsProperty;
	private final DoubleProperty tMinProperty;
	private final DoubleProperty tMaxProperty;
	private final IntegerProperty tStepsProperty;
	
	
	public MainParametersService() {
		xMinProperty = new SimpleDoubleProperty(-10);
		xMaxProperty = new SimpleDoubleProperty(10);
		xStepsProperty = new SimpleIntegerProperty(20);
		tMinProperty = new SimpleDoubleProperty(-10);
		tMaxProperty = new SimpleDoubleProperty(10);
		tStepsProperty = new SimpleIntegerProperty(20);
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
		Range xrange = getXRange();
		int xsteps = getXSteps();
		Range trange = getTRange();
		int tsteps = getTSteps();
		return new OrthonormalGrid(xrange, xsteps, trange, tsteps);
	}

	public Range getXRange() {
		return new Range((float)xMinProperty.get(), (float)xMaxProperty.get());
	}
	
	public Range getTRange() {
		return new Range((float)tMinProperty.get(), (float)tMaxProperty.get());
	}

	public int getXSteps() {
		return xStepsProperty.get();
	}

	public int getTSteps() {
		return tStepsProperty.get();
	}

	public void setTSteps(int tSteps) {
		tStepsProperty.set(tSteps);
	}

		
}
