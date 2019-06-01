package ua.kpi.iasa.parallel.course.plot;

import java.util.function.DoubleBinaryOperator;

import org.jzy3d.plot3d.builder.Mapper;

public class FunctionMapper extends Mapper{

	private final DoubleBinaryOperator function;
	
	public FunctionMapper(final DoubleBinaryOperator function) {
		this.function = function;
	}

	@Override
	public double f(double x, double y) {
		return function.applyAsDouble(x, y);
	}
}
