package ua.kpi.iasa.parallel.course.plot;

import org.springframework.stereotype.Service;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

@Service
public class PlotParametersService {
	private final BooleanProperty isWireframeDisplayedProperty;
	
	public PlotParametersService() {
		isWireframeDisplayedProperty = new SimpleBooleanProperty(false);
	}
	
	public BooleanProperty isWireframeDisplayedProperty() {
		return isWireframeDisplayedProperty;
	}
}
