package ua.kpi.iasa.parallel.course.data;

import java.util.function.DoubleUnaryOperator;

public interface GridValuePointer {
	void makePositiveXStep();
	void makePositiveTStep();
	void makeNegativeXStep();
	void makeNegativeTStep();
	void makePositiveTStepResettingX();
	
	double getCurrentValue();
	double getCurrentX();
	double getCurrentT();
	double getRelativeValue(int xSteps, int tSteps);
	
	void setCurrentValue(double value);
	
	boolean canMakePositiveXStep();
	boolean canMakePositiveTStep();
	boolean canMakeNegativeXStep();
	boolean canMakeNegativeTStep();
	

	default void setCurrentValueApplyingToX(DoubleUnaryOperator xToValue) {
		setCurrentValue(xToValue.applyAsDouble(getCurrentX()));
	}
	
	default void setCurrentValueApplyingToT(DoubleUnaryOperator tToValue) {
		setCurrentValue(tToValue.applyAsDouble(getCurrentT()));
	}
	
	default void setValueForEachRemainingXMovingPositively(DoubleUnaryOperator xToValue) {
		while(canMakePositiveXStep()) {
			makePositiveXStep();
			setCurrentValueApplyingToX(xToValue);
		}
	}
	
	default void setValueForEachRemainingXMovingNegatively(DoubleUnaryOperator xToValue) {
		while(canMakeNegativeXStep()) {
			makeNegativeXStep();
			setCurrentValueApplyingToX(xToValue);
		}
	}
}
