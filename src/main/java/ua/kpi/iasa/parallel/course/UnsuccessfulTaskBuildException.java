package ua.kpi.iasa.parallel.course;

@SuppressWarnings("serial")
public class UnsuccessfulTaskBuildException extends AlertableException {

	public UnsuccessfulTaskBuildException(String exceptionHeader, String exceptionContent) {
		super(exceptionHeader, exceptionContent);
	}

}
