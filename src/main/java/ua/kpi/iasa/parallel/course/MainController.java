package ua.kpi.iasa.parallel.course;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.Key;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.builder.concrete.OrthonormalTessellator;
import org.jzy3d.plot3d.primitives.Shape;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.concurrent.Worker;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import ua.com.kl.cmathtutor.commons.Pair;
import ua.com.kl.cmathtutor.concurrency.NonBlockedConcurrentLinkedList;
import ua.kpi.iasa.parallel.course.data.Coord3dComparator;
import ua.kpi.iasa.parallel.course.data.UniformGrid;
import ua.kpi.iasa.parallel.course.data.cache.CalculationType;
import ua.kpi.iasa.parallel.course.data.cache.PlotCacheKey;
import ua.kpi.iasa.parallel.course.services.MainParametersService;
import ua.kpi.iasa.parallel.course.services.PreciseSolutionService;
import ua.kpi.iasa.parallel.course.methods.DiffeqCalculationMethod;
import ua.kpi.iasa.parallel.course.plot.FunctionMapper;
import ua.kpi.iasa.parallel.course.plot.PlotController;
import ua.kpi.iasa.parallel.course.plot.PlotParametersService;

@Controller
public class MainController implements Initializable{
	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	@FXML private ImageView conditionImage;
	@FXML private ImageView preciseSolutionImage;
	@FXML private TextField aValue;
	@FXML private TextField bValue;
	@FXML private TextField xMin;
	@FXML private TextField xMax;
	@FXML private TextField xSteps;
	@FXML private TextField tMin;
	@FXML private TextField tMax;
	@FXML private TextField tSteps;
	@FXML private CheckBox parallelCalculationEnabled;
	@FXML private ComboBox<DiffeqCalculationMethod> calculationMethod;

	@FXML private Button buildSolutionButton;
	@FXML private ProgressIndicator buildProgressIndicator;
	@FXML private Button showPreciseSolutionButton;
	@FXML private Button showBuiltSolutionButton;
	@FXML private Button showDifferenceButton;
	@FXML private ProgressIndicator showProgressIndicator;
	@FXML private Button abortBuildButton;

	@FXML private CheckBox isWireframeDisplayed;

	@Autowired
	private MainParametersService mainParametersService;

	@Autowired
	private PreciseSolutionService preciseSolutionService;

	@Autowired
	private PlotParametersService plotParametersService;

	@Autowired
	@Qualifier("conditionImageResource")
	private InputStream conditionImageResource;
	
	@Autowired
	@Qualifier("preciseSolutionImageResource")
	private InputStream preciseSolutionImageResource;
	
	@Autowired
	@Qualifier("diffeqCalculationMethods")
	private List<DiffeqCalculationMethod> diffeqCalculationMethods;

	private final Map<PlotCacheKey, Reference<List<Coord3d>>> plotDataCache;
	private final AtomicReference<Worker<?>> currentBuildProcessWorker;
	private final List<Task<?>> runningTasks;

	public MainController() {
		plotDataCache = new HashMap<>();
		currentBuildProcessWorker = new AtomicReference<>(null);
		runningTasks = new LinkedList<>();
	}

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Image image = new Image(conditionImageResource);
		conditionImage.setImage(image);
		image = new Image(preciseSolutionImageResource);
		preciseSolutionImage.setImage(image);
		aValue.textProperty()
			.bindBidirectional(preciseSolutionService.aProperty(), new NumberStringConverter());
		bValue.textProperty()
			.bindBidirectional(preciseSolutionService.bProperty(), new NumberStringConverter());

		xMin.textProperty().bindBidirectional(mainParametersService.xMinProperty(), new NumberStringConverter());
		xMax.textProperty().bindBidirectional(mainParametersService.xMaxProperty(), new NumberStringConverter());
		xSteps.textProperty().bindBidirectional(mainParametersService.xStepsProperty(),
				new NumberStringConverter());
		tMin.textProperty().bindBidirectional(mainParametersService.tMinProperty(), new NumberStringConverter());
		tMax.textProperty().bindBidirectional(mainParametersService.tMaxProperty(), new NumberStringConverter());
		tSteps.textProperty().bindBidirectional(mainParametersService.tStepsProperty(),
				new NumberStringConverter());

		Callback<ListView<DiffeqCalculationMethod>, ListCell<DiffeqCalculationMethod>>
		calculationMethodDescFactory =  param-> new ListCell<DiffeqCalculationMethod>() {

			@Override
			protected void updateItem(DiffeqCalculationMethod item, boolean empty) {
				super.updateItem(item, empty);
				if (item == null || empty) {
					setText("");
				} else {
					setText(item.getName());
				}
			}
		};
		calculationMethod.setCellFactory(calculationMethodDescFactory);
		calculationMethod.getItems().addAll(diffeqCalculationMethods);
		if (diffeqCalculationMethods.size() > 0) {
			calculationMethod.setValue(diffeqCalculationMethods.get(0));
		}

		plotParametersService.isWireframeDisplayedProperty()
		.bindBidirectional(isWireframeDisplayed.selectedProperty());
	}

	@FXML
	private void buildSolution(Event event) {
		Pair<PlotCacheKey, DiffeqCalculationMethod> keyAndMethod;
		try {
			keyAndMethod = getBuiltPlotCacheKeyAndCalculationMethod();
		} catch (InvalidStepsException e) {
			alert(AlertType.ERROR, "Building solution", e.getExceptionHeader(), e.getExceptionContent());
			return;
		}
		Task<UniformGrid> task;
		try {
			task = buildPlotPointsTask(keyAndMethod.getFirst(), keyAndMethod.getSecond());
		} catch (UnsuccessfulTaskBuildException e) {
			alert(AlertType.WARNING, "Building solution", e.getExceptionHeader(), e.getExceptionContent());
			return;
		}
		new Thread(task).start();
	}
	
	@FXML
	private void abortBuildTask(Event event) {
		Optional.ofNullable(currentBuildProcessWorker.get())
			.ifPresent(Worker::cancel);
	}
	
	private Pair<PlotCacheKey, DiffeqCalculationMethod> getBuiltPlotCacheKeyAndCalculationMethod()
			throws InvalidStepsException {
		setParametersEditingDisabled(true);
		final int xSteps = mainParametersService.getXSteps();
		final int tSteps = mainParametersService.getTSteps();
		final Range xRange = mainParametersService.getXRange();
		final Range tRange = mainParametersService.getTRange();
		DiffeqCalculationMethod method = calculationMethod.getValue();
		try {
			validateSteps(xSteps, tSteps);
		} catch (InvalidStepsException e) {
			setParametersEditingDisabled(false);
			throw e;
		}

		PlotCacheKey key = new PlotCacheKey();
		key.setCalculationType(method.getCalculationType());
		key.setTRange(tRange);
		key.setTSteps(tSteps);
		key.setXRange(xRange);
		key.setXSteps(xSteps);
		key.setParamA(preciseSolutionService.getA());
		key.setParamB(preciseSolutionService.getB());
		setParametersEditingDisabled(false);
		return new Pair<>(key, method);
	}

	private void setParametersEditingDisabled(boolean disabled) {
		aValue.setDisable(disabled);
		bValue.setDisable(disabled);
		calculationMethod.setDisable(disabled);
		parallelCalculationEnabled.setDisable(disabled);
		tMax.setDisable(disabled);
		tMin.setDisable(disabled);
		tSteps.setDisable(disabled);
		xMax.setDisable(disabled);
		xMin.setDisable(disabled);
		xSteps.setDisable(disabled);		
	}

	private void setBuildButtonsDisabled(boolean disabled) {
		buildSolutionButton.setDisable(disabled);
		//Maybe you should to add "abort" button so that 
			//you will be able to stop the calculation without closing programm?
	}

	private void validateSteps(final int xSteps, final int tSteps) throws InvalidStepsException {
		if (xSteps < 2) {
			throw new InvalidStepsException("Invalid xSteps value", "The number of xSteps should be at least 2!");
		}
		if (tSteps < 2) {
			throw new InvalidStepsException("Invalid tSteps value", "The number of tSteps should be at least 2!");
		}
	}

	private Task<UniformGrid> buildPlotPointsTask(PlotCacheKey plotCacheKey, DiffeqCalculationMethod method)
			throws UnsuccessfulTaskBuildException {
		setBuildButtonsDisabled(true);
		final int xSteps = plotCacheKey.getXSteps();
		final int tSteps = plotCacheKey.getTSteps();
		final Range xRange = plotCacheKey.getXRange();
		final Range tRange = plotCacheKey.getTRange();
		if (currentBuildProcessWorker.get() != null) {
			throw new UnsuccessfulTaskBuildException("Unable to make building task",
					"There is another running building task");
		}
		Task<UniformGrid> task = parallelCalculationEnabled.isSelected()
				? method.getSolveDiffEquationConcurrentlyTask(xRange, tRange, xSteps, tSteps)
				: method.getSolveDiffEquationTask(xRange, tRange, xSteps, tSteps);

		if (!currentBuildProcessWorker.compareAndSet(null, task)) {
			throw new UnsuccessfulTaskBuildException("Unable to launch building task",
					"There is another running building task");
		}
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, 
				e-> onBuildTaskSucceed(e, plotCacheKey));
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, 
				e-> onBuildTaskCancelled(e, plotCacheKey));
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED,
				e-> onBuildTaskFailed(e, plotCacheKey));
		buildProgressIndicator.progressProperty().unbind();
		buildProgressIndicator.progressProperty().bind(task.progressProperty());
		
		buildProgressIndicator.setVisible(true);
		abortBuildButton.setVisible(true);
		runningTasks.add(task);
		
		return task;
	}

	private void onBuildTaskSucceed(WorkerStateEvent e, PlotCacheKey plotCacheKey) {
		log.info("Building task for {} completed successfully", plotCacheKey);
		currentBuildProcessWorker.set(null);
		final UniformGrid grid = (UniformGrid) e.getSource().getValue();
		List<Coord3d> points = grid.getGridNodePoints();
//		grid.clear();
		Reference<List<Coord3d>> ref = plotDataCache.remove(plotCacheKey);
		if (ref != null) {
			log.debug("Clearing cache reference...");
			ref.clear();
		}
		plotDataCache.put(plotCacheKey, new SoftReference<>(points));
		setBuildButtonsDisabled(false);
		abortBuildButton.setVisible(false);
//		e.getSource().cancel();
		runningTasks.remove(e.getSource());
	}
	
	private void onBuildTaskCancelled(WorkerStateEvent e, PlotCacheKey plotCacheKey) {
		log.info("Building task for {} was cancelled", plotCacheKey);
		currentBuildProcessWorker.set(null);
		setBuildButtonsDisabled(false);
		buildProgressIndicator.setVisible(false);
		abortBuildButton.setVisible(false);
		runningTasks.remove(e.getSource());
	}
	
	private void onBuildTaskFailed(WorkerStateEvent e, PlotCacheKey plotCacheKey) {
		final Worker worker = e.getSource();
		final Throwable exception = worker.getException();
		String header = "Unable to complete stage";
		String content =  worker.getMessage();
		if (exception instanceof TaskFailedException) {
			TaskFailedException taskFailedException = (TaskFailedException) exception;
			header = taskFailedException.getExceptionHeader();
			content = taskFailedException.getExceptionContent();
		}
		log.info("Building task for {} failed\n. The reason: {}",
				plotCacheKey, exception.getMessage());
		alert(AlertType.INFORMATION, "Current build task failed", header, content);
		currentBuildProcessWorker.set(null);
		setBuildButtonsDisabled(false);
		buildProgressIndicator.setVisible(false);
		abortBuildButton.setVisible(false);
		runningTasks.remove(worker);
	}

	@FXML
	private void showPreciseSolution(Event e) {
		PlotCacheKey preciseSolutionPlotCacheKey;
		try {
			preciseSolutionPlotCacheKey = getPrecisePlotCacheKey();
		} catch (InvalidStepsException ex) {
			alert(AlertType.ERROR, "Presenting precise solution", ex.getExceptionHeader(), ex.getExceptionContent());
			return;
		}
		List<Coord3d> points = getOrCreatePrecisePointsCachingThemIfNecessary(preciseSolutionPlotCacheKey);
		showPlotStage(points, "Precise solution");
	}

	private PlotCacheKey getPrecisePlotCacheKey() throws InvalidStepsException {
		setParametersEditingDisabled(true);
		final int xSteps = mainParametersService.getXSteps();
		final int tSteps = mainParametersService.getTSteps();
		final Range xRange = mainParametersService.getXRange();
		final Range tRange = mainParametersService.getTRange();
		try {
			validateSteps(xSteps, tSteps);
		} catch (InvalidStepsException e) {
			setParametersEditingDisabled(false);
			throw e;
		}

		PlotCacheKey key = new PlotCacheKey();
		key.setCalculationType(CalculationType.PRECISE);
		key.setTRange(tRange);
		key.setTSteps(tSteps);
		key.setXRange(xRange);
		key.setXSteps(xSteps);
		key.setParamA(preciseSolutionService.getA());
		key.setParamB(preciseSolutionService.getB());
		setParametersEditingDisabled(false);
		return key;
	}

	private List<Coord3d> getOrCreatePrecisePointsCachingThemIfNecessary(PlotCacheKey key) {
		final Range xRange = key.getXRange();
		final int xSteps = key.getXSteps();
		final Range tRange = key.getTRange();
		final int tSteps = key.getTSteps();
		Reference<List<Coord3d>> plotPointsWeak = plotDataCache.get(key);
		List<Coord3d> points = null;
		if(plotPointsWeak == null || (points = plotPointsWeak.get()) == null) {
			log.warn("Points for a plot with parameters {} are absent in cache."
					+" Building precise solution points...", key);
			OrthonormalGrid grid = new OrthonormalGrid(xRange, xSteps, tRange, tSteps);
			points = grid.apply(new FunctionMapper(preciseSolutionService.getPreciseSolutionFunction()));
			plotDataCache.remove(key);
			plotDataCache.put(key, new SoftReference<>(points));
			log.info("Precise solution points {} were built", key);
		}
		return points;
	}

	@FXML
	private void showBuiltSolution(Event e) {
		Pair<PlotCacheKey, DiffeqCalculationMethod> keyAndMethod;
		try {
			keyAndMethod = getBuiltPlotCacheKeyAndCalculationMethod();
		} catch (InvalidStepsException ex) {
			alert(AlertType.ERROR, "Presenting built solution", ex.getExceptionHeader(), ex.getExceptionContent());
			return;
		}
		final PlotCacheKey key = keyAndMethod.getFirst();
		final DiffeqCalculationMethod method = keyAndMethod.getSecond();

		Reference<List<Coord3d>> plotPointsRef = plotDataCache.get(key);
		List<Coord3d> points = null;
		if(plotPointsRef == null || (points = plotPointsRef.get()) == null) {
			log.warn("Points for a plot with parameters {} are absent in cache. Starting the build process", key);
			Task<UniformGrid> task;
			try {
				task = buildPlotPointsTask(key, method);
			} catch (UnsuccessfulTaskBuildException ex) {
				alert(AlertType.WARNING, "Building solution", ex.getExceptionHeader(), ex.getExceptionContent());
				return;
			}
			task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED,
					e1-> showPlotStage(plotDataCache.get(key).get(), "Built solution"));
//			buildProgressIndicator.setVisible(true);
			new Thread(task).start();
		} else {
			showPlotStage(points, "Build solution");
		}		
	}

	private void showPlotStage(List<Coord3d> points, String title) {
		String fxmlFile = "/fxml/plot.fxml";
		log.debug("Loading FXML for plot view from: {}", fxmlFile);
		FXMLLoader loader = MainApp.makeFxmlLoader();
		Parent rootNode = loadRootNode(fxmlFile, loader);
		log.debug("Showing JFX scene");
		Scene scene = new Scene(rootNode, 500, 530);
		scene.getStylesheets().add("/styles/styles.css");

		PlotController plotController = loader.getController();
		log.debug("Making surface from points: {}", points);
		Task<Void> task = plotController
				.makeAddSurfaceFromPointsTask(points);
		showProgressIndicator.progressProperty().bind(task.progressProperty());
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e-> {
			plotController.addSceneSizeChangedListener(scene);
			plotController.initializeContent();
			Stage stage = new Stage();
			stage.setTitle(title);
			stage.setScene(scene);
			stage.setOnCloseRequest(event-> showProgressIndicator.setVisible(false));
			stage.show();
		});
		showProgressIndicator.setVisible(true);
		Thread th = new Thread(task);
		th.setDaemon(false);
		th.start();
	}
	
	
	private static Parent loadRootNode(String fxmlFile, FXMLLoader loader) {
		Parent rootNode = null;
		try {
			rootNode = loader.load(MainApp.class.getResourceAsStream(fxmlFile));
		} catch (IOException ex) {
			throw new RuntimeException(
					String.format("Unable to load view from %s. Operation will be aborted.", fxmlFile));
		}
		return rootNode;
	}
	
	@FXML
	private void showDifference(Event e) {
		Pair<PlotCacheKey, DiffeqCalculationMethod> keyAndMethod;
		try {
			keyAndMethod = getBuiltPlotCacheKeyAndCalculationMethod();
		} catch (InvalidStepsException ex) {
			alert(AlertType.ERROR, "Presenting difference between precise and built solutions",
					ex.getExceptionHeader(), ex.getExceptionContent());
			return;
		}
		final PlotCacheKey key = keyAndMethod.getFirst();
		final DiffeqCalculationMethod method = keyAndMethod.getSecond();
		key.setDifference(true);
		
		Reference<List<Coord3d>> plotPointsWeak = plotDataCache.get(key);
		List<Coord3d> points = null;
		if(plotPointsWeak == null || (points = plotPointsWeak.get()) == null) {
			log.warn("Points for a plot with parameters {} are absent in cache. Starting the build process", key);
			buildDifferencePlotPointsAndThenShowPlot(key, method);
		} else {
			showPlotStage(points, "Difference plot");
		}
		
	}
	
	private void buildDifferencePlotPointsAndThenShowPlot(final PlotCacheKey plotCacheKey,
			DiffeqCalculationMethod method) {
		PlotCacheKey builtPlotCacheKey = new PlotCacheKey(plotCacheKey);
		builtPlotCacheKey.setDifference(false);
		Reference<List<Coord3d>> builtPlotPointsWeak = plotDataCache.get(builtPlotCacheKey);
		List<Coord3d> points = null;
		if(builtPlotPointsWeak == null || (points = builtPlotPointsWeak.get()) == null) {
			log.warn("Points for a plot with parameters {} are absent in cache. Starting the build process",
					builtPlotCacheKey);
			buildPlotPointsAndThenGetPrecisePoints(builtPlotCacheKey, method);
		} else {
			getPrecisePointsAndShowDifferencePlot(builtPlotCacheKey, method, points);
		}
	}
	
	private void buildPlotPointsAndThenGetPrecisePoints(PlotCacheKey plotCacheKey,
			DiffeqCalculationMethod method) {
		Task<UniformGrid> task;
		try {
			task = buildPlotPointsTask(plotCacheKey, method);
		} catch (UnsuccessfulTaskBuildException ex) {
			alert(AlertType.WARNING, "Building solution", ex.getExceptionHeader(), ex.getExceptionContent());
			return;
		}
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e-> 
			getPrecisePointsAndShowDifferencePlot(plotCacheKey, method, plotDataCache.get(plotCacheKey).get()));
//		buildProgressIndicator.setVisible(true);
		new Thread(task).start();
	}
	
	@SuppressWarnings("unchecked")
	private void getPrecisePointsAndShowDifferencePlot(PlotCacheKey plotCacheKey,
			DiffeqCalculationMethod method, List<Coord3d> builtPoints) {
		PlotCacheKey precisePlotCacheKey = new PlotCacheKey(plotCacheKey);
		precisePlotCacheKey.setCalculationType(CalculationType.PRECISE);
		precisePlotCacheKey.setDifference(false);
		List<Coord3d> precisePoints = getOrCreatePrecisePointsCachingThemIfNecessary(precisePlotCacheKey);
		if (precisePoints.size() != builtPoints.size()) {
			alert(AlertType.ERROR, "Building difference plot", "Error making difference between points",
					"It seems that built solution is incomplete. Dimensions of precise solution points list "
					+ "and built solution points list differ!");
			return;
		}

		PlotCacheKey differencePlotCacheKey = new PlotCacheKey(plotCacheKey);
		differencePlotCacheKey.setDifference(true);
		Task<List<Coord3d>> task = new Task<List<Coord3d>>() {

			@Override
			protected List<Coord3d> call() throws Exception {
				final List<Coord3d> precisePointsSorted = 
						precisePoints.parallelStream().sorted(Coord3dComparator.getInstance())
						.sequential().collect(Collectors.toList());
				final List<Coord3d> builtPointsSorted = 
						builtPoints.parallelStream().map(coord3d-> {
							Coord3d point = new Coord3d();
							point.x = coord3d.x;
							point.y = coord3d.y;
							point.z = coord3d.z;
							return point;
							})
						.sorted(Coord3dComparator.getInstance())
						.sequential().collect(Collectors.toList());
				final Iterator<Coord3d> precisePointsIt = precisePointsSorted.iterator();
				builtPointsSorted.forEach(coord3d-> {coord3d.z -= precisePointsIt.next().z;});
				return builtPointsSorted;
			}
		};

		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e-> {
			log.info("Building task completed successfully");
			List<Coord3d> points = ((List<Coord3d>) e.getSource().getValue());
			plotDataCache.remove(differencePlotCacheKey);
			plotDataCache.put(differencePlotCacheKey, new SoftReference<>(points));
			showPlotStage(points, "Difference plot");
		});
		buildProgressIndicator.setVisible(true);
		new Thread(task).start();
		
	}

	private static void alert(AlertType alertType, String title, String header,
			String content) {
		log.debug("Showing alert of type {}, title: {}, header: {}, content: {} ",
				alertType, title, header, content);
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
