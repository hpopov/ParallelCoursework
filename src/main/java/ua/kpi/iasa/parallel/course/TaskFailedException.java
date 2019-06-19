package ua.kpi.iasa.parallel.course;

@SuppressWarnings("serial")
public class TaskFailedException extends AlertableException {

	public TaskFailedException(String exceptionContent) {
		super("Task execution failed", exceptionContent);
	}

	public TaskFailedException(String exceptionHeader, String exceptionContent) {
		super(exceptionHeader, exceptionContent);
	}

}
