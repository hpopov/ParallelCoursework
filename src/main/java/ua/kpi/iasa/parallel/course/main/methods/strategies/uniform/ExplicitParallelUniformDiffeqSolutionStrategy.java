package ua.kpi.iasa.parallel.course.main.methods.strategies.uniform;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.DoubleUnaryOperator;
import java.util.stream.Collectors;

import org.jzy3d.maths.Range;
import org.springframework.stereotype.Component;

import ua.kpi.iasa.parallel.course.data.AbstractUniformGrid;
import ua.kpi.iasa.parallel.course.data.GridValuePointer;
import ua.kpi.iasa.parallel.course.data.UniformGrid;

@Component("explicitParallelDiffeqSolutionStrategy")
public class ExplicitParallelUniformDiffeqSolutionStrategy
extends ExplicitUniformDiffeqSolutionStrategy {
//	private static final Logger log = 
//			LoggerFactory.getLogger(ExplicitParallelUniformDiffeqSolutionStrategy.class)

	private final int minXStepsPerThread;
	private final int maxThreadCount;

	public ExplicitParallelUniformDiffeqSolutionStrategy() {
		this.minXStepsPerThread = 2;
		this.maxThreadCount = Runtime.getRuntime().availableProcessors();
	}

	@Override
	public UniformGrid solveDifferentialEquation(Range xRange, Range tRange,
			int xSteps, int tSteps) {
		int actualThreadCount = xSteps/minXStepsPerThread;
		if (actualThreadCount > maxThreadCount) {
			actualThreadCount = maxThreadCount;
		}
		if (actualThreadCount == 1) {
			return super.solveDifferentialEquation(xRange, tRange, xSteps, tSteps);
		}

		UniformGrid grid = new UniformGrid(xRange, tRange, xSteps, tSteps);
		ExecutorService executor = Executors.newFixedThreadPool(actualThreadCount);
		Semaphore[] semaphores = new Semaphore[(actualThreadCount-2)*2 + 2];
		for(int i = 0; i< semaphores.length; i++) {
			semaphores[i] = new Semaphore(1);
		}
		int semInd = 0;
		List<AbstractSubGridSolverRunnable> subGridSolvers = new ArrayList<>(actualThreadCount);
		final int xStepsPerThread = xSteps/actualThreadCount;

		int currentThreadFromXIndex = 0;
		subGridSolvers.add(new LeftSubGridSolverRunnable(grid.subXGrid(currentThreadFromXIndex, xStepsPerThread),
				semaphores[semInd], semaphores[semInd+1]));
		currentThreadFromXIndex += xStepsPerThread;
		for(int i = 1; i<actualThreadCount-1; i++) {
			subGridSolvers.add(new IntermediateSubGridSolverRunnable(
					grid.subXGrid(currentThreadFromXIndex, xStepsPerThread),
					semaphores[semInd], semaphores[semInd+1], semaphores[semInd+2], semaphores[semInd+3]));
			semInd+=2;
			currentThreadFromXIndex += xStepsPerThread;
		}
		subGridSolvers.add(new RightSubGridSolverRunnable(
				grid.subXGrid(currentThreadFromXIndex, xSteps - currentThreadFromXIndex),
				semaphores[semInd], semaphores[semInd+1]));
		subGridSolvers.stream().forEach(executor::execute);
		try {
			executor.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			System.err.println("executor interrupts execution!");
			throw new RuntimeException(e);
		}
		return grid;
	}

	private abstract class AbstractSubGridSolverRunnable implements Runnable {
		protected final AbstractUniformGrid grid;
		protected final PointResolver resolver;

		public AbstractSubGridSolverRunnable(final AbstractUniformGrid grid) {
			this.grid = grid;
			final double dx = grid.getDx();
			final double dt = grid.getDt();
			resolver = new PointResolver(dt, dx, mainParametersService.getAlpha());
//			setDaemon(true);
		}

		protected double calculateValue(final GridValuePointer gridPointer) {
			final double wBottom = gridPointer.getRelativeValue(0, -2);
			final double wLeft = gridPointer.getRelativeValue(-1, -1);
			final double wCenter = gridPointer.getRelativeValue(0, -1);
			final double wRight = gridPointer.getRelativeValue(1, -1);
			return resolver.wTop(wBottom, wLeft, wCenter, wRight);
		}

		protected void acquireSemaphore(Semaphore sem) {
			try {
				sem.acquire();
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
		}
	}

	private class IntermediateSubGridSolverRunnable extends AbstractSubGridSolverRunnable {

		private final Semaphore leftSemaphore;
		private final Semaphore rightSemaphore;
		private final Semaphore neighbourLeftSemaphore;
		private final Semaphore neighbourRightSemaphore;
		
		public IntermediateSubGridSolverRunnable(AbstractUniformGrid grid, Semaphore neighbourLeftSemaphore,
				Semaphore leftSemaphore, Semaphore rightSemaphore, Semaphore neighbourRightSemaphore) {
			super(grid);
			this.leftSemaphore = leftSemaphore;
			this.rightSemaphore = rightSemaphore;
			this.neighbourLeftSemaphore = neighbourLeftSemaphore;
			this.neighbourRightSemaphore = neighbourRightSemaphore;
		}
		
		@Override
		public void run() {
			final GridValuePointer gridPointer = makeGridPointerFilledWithInitialCondidions(grid);

			while(gridPointer.canMakePositiveTStep()) {
				gridPointer.makePositiveTStepResettingX();
				gridPointer.makePositiveXStep();
				acquireSemaphore(neighbourLeftSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				leftSemaphore.release();
				gridPointer.makePositiveXStep();
				while(gridPointer.canMakePositiveXStep()) {
					gridPointer.setCurrentValue(calculateValue(gridPointer));
					gridPointer.makePositiveXStep();
				}
				acquireSemaphore(neighbourRightSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				rightSemaphore.release();
				
				if (!gridPointer.canMakePositiveTStep()) {
					break;
				}
				
				gridPointer.makePositiveTStep();
				acquireSemaphore(neighbourRightSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				rightSemaphore.release();
				gridPointer.makeNegativeXStep();
				while(gridPointer.canMakeNegativeXStep()) {
					gridPointer.setCurrentValue(calculateValue(gridPointer));
					gridPointer.makeNegativeXStep();
				}
				acquireSemaphore(neighbourLeftSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				leftSemaphore.release();				
			}
		}
	}
	
	private class LeftSubGridSolverRunnable extends AbstractSubGridSolverRunnable {

		private final Semaphore rightSemaphore;
		private final Semaphore neighbourRightSemaphore;
		private final DoubleUnaryOperator leftEdgeCondition;
		
		public LeftSubGridSolverRunnable(AbstractUniformGrid abstractUniformGrid, Semaphore rightSemaphore,
				Semaphore neighbourRightSemaphore) {
			super(abstractUniformGrid);
			this.rightSemaphore = rightSemaphore;
			this.neighbourRightSemaphore = neighbourRightSemaphore;
			this.leftEdgeCondition = conditionService.getLeftEdgeCondition();
		}
		
		@Override
		public void run() {
			final GridValuePointer gridPointer = makeGridPointerFilledWithInitialCondidions(grid);

			while(gridPointer.canMakePositiveTStep()) {
				gridPointer.makePositiveTStepResettingX();
				gridPointer.makePositiveXStep();
				gridPointer.setCurrentValueApplyingToT(leftEdgeCondition);
				gridPointer.makePositiveXStep();
				while(gridPointer.canMakePositiveXStep()) {
					gridPointer.setCurrentValue(calculateValue(gridPointer));
					gridPointer.makePositiveXStep();
				}
				acquireSemaphore(neighbourRightSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				rightSemaphore.release();
				
				if (!gridPointer.canMakePositiveTStep()) {
					break;
				}
				
				gridPointer.makePositiveTStep();
				acquireSemaphore(neighbourRightSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				rightSemaphore.release();
				gridPointer.makeNegativeXStep();
				while(gridPointer.canMakeNegativeXStep()) {
					gridPointer.setCurrentValue(calculateValue(gridPointer));
					gridPointer.makeNegativeXStep();
				}
				gridPointer.setCurrentValueApplyingToT(leftEdgeCondition);			
			}
		}
	}

	private class RightSubGridSolverRunnable extends AbstractSubGridSolverRunnable {

		private final Semaphore leftSemaphore;
		private final Semaphore neighbourLeftSemaphore;
		private final DoubleUnaryOperator rightEdgeCondition;
		
		public RightSubGridSolverRunnable(AbstractUniformGrid abstractUniformGrid, Semaphore neighbourLeftSemaphore,
				Semaphore leftSemaphore) {
			super(abstractUniformGrid);
			this.leftSemaphore = leftSemaphore;
			this.neighbourLeftSemaphore = neighbourLeftSemaphore;
			this.rightEdgeCondition = conditionService.getRightEdgeCondition();
		}
		
		@Override
		public void run() {
			final GridValuePointer gridPointer = makeGridPointerFilledWithInitialCondidions(grid);

			while(gridPointer.canMakePositiveTStep()) {
				gridPointer.makePositiveTStepResettingX();
				gridPointer.makePositiveXStep();
				acquireSemaphore(neighbourLeftSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				leftSemaphore.release();
				gridPointer.makePositiveXStep();
				while(gridPointer.canMakePositiveXStep()) {
					gridPointer.setCurrentValue(calculateValue(gridPointer));
					gridPointer.makePositiveXStep();
				}
				gridPointer.setCurrentValueApplyingToT(rightEdgeCondition);
				
				if (!gridPointer.canMakePositiveTStep()) {
					break;
				}
				
				gridPointer.makePositiveTStep();
				gridPointer.setCurrentValueApplyingToT(rightEdgeCondition);
				gridPointer.makeNegativeXStep();
				while(gridPointer.canMakeNegativeXStep()) {
					gridPointer.setCurrentValue(calculateValue(gridPointer));
					gridPointer.makeNegativeXStep();
				}
				acquireSemaphore(neighbourLeftSemaphore);
				gridPointer.setCurrentValue(calculateValue(gridPointer));
				leftSemaphore.release();				
			}
		}
	}
}

