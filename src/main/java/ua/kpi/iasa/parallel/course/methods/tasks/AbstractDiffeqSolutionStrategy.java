package ua.kpi.iasa.parallel.course.methods.tasks;

import java.util.Arrays;
import java.util.Date;

import org.jzy3d.maths.Range;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javafx.concurrent.Task;
import ua.kpi.iasa.parallel.course.data.UniformGrid;

public abstract class AbstractDiffeqSolutionStrategy implements DiffeqSolutionStrategy {

	private static final Logger log = LoggerFactory.getLogger(AbstractDiffeqSolutionStrategy.class);
	
	protected static Date logBefore(String taskName) {
		Date date = new Date();
		log.info("Starting task {}", taskName);
		return date;
	}
	
	protected static void logAfter(Date beforeDate, final double[][] gridNodeValues) {
		Date currentDate = new Date();
		long millisDiff = currentDate.getTime() - beforeDate.getTime();
		int millis = (int) (millisDiff % 1000);
		int seconds = (int) ((millisDiff - millis)/1000);
		log.info("Task finished. Task duration: {}s:{}ms", seconds, millis);
		String arrayPresentation = null;
		if (gridNodeValues.length <= 10 && gridNodeValues[0].length <= 10) {
			arrayPresentation = present2DArray(gridNodeValues);
		} else {
			double[][] arrayToPresent = new double[10][10];
			double iSkipRatio = gridNodeValues.length/10.;
			double currentI = 0;
			double currentJ = 0;
			double jSkipRatio = gridNodeValues[0].length/10.;
			int presentI = 0, presentJ;
			for(int i = 0;i<gridNodeValues.length;i++) {
				if (Math.abs(currentI - i) > 0.5) {
					continue;
				}
				currentJ = 0;
				presentJ = 0;
				for(int j = 0; j<gridNodeValues[0].length; j++) {
					if (Math.abs(currentJ - j) > 0.5) {
						continue;
					}
					arrayToPresent[presentI][presentJ++] = gridNodeValues[i][j];
					currentJ += jSkipRatio;
				}
				presentI++;
				currentI += iSkipRatio;
			}
			arrayPresentation = present2DArray(arrayToPresent);
		}
		log.info("Task result:\n{}", arrayPresentation);
	}

	private static String present2DArray(double[][] arrayToPresent) {
		StringBuilder sb = new StringBuilder();
		for(double[] row : arrayToPresent) {
			sb.append(Arrays.toString(row)).append("\r\n");
		}
		return sb.toString();
	}

}
