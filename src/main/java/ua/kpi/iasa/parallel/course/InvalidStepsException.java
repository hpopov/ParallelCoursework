package ua.kpi.iasa.parallel.course;

@SuppressWarnings("serial")
public class InvalidStepsException extends AlertableException {

	public InvalidStepsException(String exceptionHeader, String exceptionContent) {
		super(exceptionHeader, exceptionContent);
	}

}
