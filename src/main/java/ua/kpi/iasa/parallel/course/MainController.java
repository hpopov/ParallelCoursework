package ua.kpi.iasa.parallel.course;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.security.Key;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.WeakHashMap;
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

import javafx.concurrent.Task;
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

	private final Map<PlotCacheKey, SoftReference<List<Coord3d>>> plotDataCache;

	public MainController() {
		plotDataCache = new HashMap<>();
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
		//		calculationMethod.valueProperty().addListener((observable, prev, curr)-> {
		//			tSteps.setDisable(!curr.allowManualTSptepsResizing());
		//		});
		calculationMethod.getItems().addAll(diffeqCalculationMethods);
		if (diffeqCalculationMethods.size() > 0) {
			calculationMethod.setValue(diffeqCalculationMethods.get(0));
		}

		plotParametersService.isWireframeDisplayedProperty()
		.bindBidirectional(isWireframeDisplayed.selectedProperty());
	}

	@FXML
	private void buildSolution(Event event) {
		final int xSteps = mainParametersService.getXSteps();
		final int tSteps = mainParametersService.getTSteps();
		final Range xRange = mainParametersService.getXRange();
		final Range tRange = mainParametersService.getTRange();
		DiffeqCalculationMethod method = calculationMethod.getValue();
		if (xSteps < 2) {
			alert(AlertType.ERROR, "Building solution", "Invalid xSteps value",
					"The number of xSteps should be at least 2!");
			return;
		}
		if (tSteps < 2) {
			alert(AlertType.ERROR, "Building solution", "Invalid tSteps value",
					"The number of tSteps should be at least 2!");
			return;
		}

		PlotCacheKey key = new PlotCacheKey();
		key.setCalculationType(method.getCalculationType());
		key.setTRange(tRange);
		key.setTSteps(tSteps);
		key.setXRange(xRange);
		key.setXSteps(xSteps);
		Task<UniformGrid> task = buildPlotPointsTask(key, method);
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e-> onBuildTaskSucceed(e, key));
		buildProgressIndicator.setVisible(true);
		new Thread(task).start();

	}

	private Task<UniformGrid> buildPlotPointsTask(PlotCacheKey plotCacheKey, DiffeqCalculationMethod method) {
		final int xSteps = plotCacheKey.getXSteps();
		final int tSteps = plotCacheKey.getTSteps();
		final Range xRange = plotCacheKey.getXRange();
		final Range tRange = plotCacheKey.getTRange();
		Task<UniformGrid> task = parallelCalculationEnabled.isSelected()
				? method.getSolveDiffEquationConcurrentlyTask(xRange, tRange, xSteps, tSteps)
				: method.getSolveDiffEquationTask(xRange, tRange, xSteps, tSteps);
		buildProgressIndicator.progressProperty().unbind();
		buildProgressIndicator.progressProperty().bind(task.progressProperty());
		return task;
	}

	private void onBuildTaskSucceed(WorkerStateEvent e, PlotCacheKey plotCacheKey) {
		log.info("Building task for {} completed successfully", plotCacheKey);
		List<Coord3d> points = ((UniformGrid) e.getSource().getValue()).getGridNodePoints();
		plotDataCache.remove(plotCacheKey);
		plotDataCache.put(plotCacheKey, new SoftReference<>(points));
	}

	@FXML
	private void showPreciseSolution(Event e) {
		final Range xRange = mainParametersService.getXRange();
		final int xSteps = mainParametersService.getXSteps();
		final Range tRange = mainParametersService.getTRange();
		final int tSteps = mainParametersService.getTSteps();
		if (xSteps < 2) {
			alert(AlertType.ERROR, "Presenting precise solution", "Invalid xSteps value",
					"The number of xSteps should be at least 2!");
			return;
		}
		if (tSteps < 2) {
			alert(AlertType.ERROR, "Presenting precise solution", "Invalid tSteps value",
					"The number of tSteps should be at least 2!");
			return;
		}

		PlotCacheKey key = new PlotCacheKey();
		key.setCalculationType(CalculationType.PRECISE);
		key.setTRange(tRange);
		key.setTSteps(tSteps);
		key.setXRange(xRange);
		key.setXSteps(xSteps);
		List<Coord3d> points = createPrecisePlotPontsCachingThem(key);
		showPlotStage(points, "Precise solution");
	}

	private List<Coord3d> createPrecisePlotPontsCachingThem(PlotCacheKey key) {
		final Range xRange = key.getXRange();
		final int xSteps = key.getXSteps();
		final Range tRange = key.getTRange();
		final int tSteps = key.getTSteps();
		SoftReference<List<Coord3d>> plotPointsWeak = plotDataCache.get(key);
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
	private void showBuiltSolution(Event e) {
		final int xSteps = mainParametersService.getXSteps();
		final int tSteps = mainParametersService.getTSteps();
		final Range xRange = mainParametersService.getXRange();
		final Range tRange = mainParametersService.getTRange();
		DiffeqCalculationMethod method = calculationMethod.getValue();
		if (xSteps < 2) {
			alert(AlertType.ERROR, "Presenting built solution", "Invalid xSteps value",
					"The number of xSteps should be at least 2!");
			return;
		}
		if (tSteps < 2) {
			alert(AlertType.ERROR, "Presenting built solution", "Invalid tSteps value",
					"The number of tSteps should be at least 2!");
			return;
		}

		PlotCacheKey key = new PlotCacheKey();
		key.setCalculationType(method.getCalculationType());
		key.setTRange(tRange);
		key.setTSteps(tSteps);
		key.setXRange(xRange);
		key.setXSteps(xSteps);
		SoftReference<List<Coord3d>> plotPointsWeak = plotDataCache.get(key);
		List<Coord3d> points = null;
		if(plotPointsWeak == null || (points = plotPointsWeak.get()) == null) {
			log.warn("Points for a plot with parameters {} are absent in cache. Starting the build process", key);
			buildPlotPointsAndThenShowPlot(key, method);
		} else {
			showPlotStage(points, "Build solution");
		}
	}


	private void buildPlotPointsAndThenShowPlot(PlotCacheKey plotCacheKey, DiffeqCalculationMethod method) {
		Task<UniformGrid> task = buildPlotPointsTask(plotCacheKey, method);
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e-> {
			onBuildTaskSucceed(e, plotCacheKey);
			showPlotStage(((UniformGrid) e.getSource().getValue()).getGridNodePoints(), "Built solution");
		});
		buildProgressIndicator.setVisible(true);
		new Thread(task).start();
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
		new Thread(task).start();
	}
	
	@FXML
	private void showDifference(Event e) {
		final int xSteps = mainParametersService.getXSteps();
		final int tSteps = mainParametersService.getTSteps();
		final Range xRange = mainParametersService.getXRange();
		final Range tRange = mainParametersService.getTRange();
		DiffeqCalculationMethod method = calculationMethod.getValue();
		if (xSteps < 2) {
			alert(AlertType.ERROR, "Presenting difference between precise and built solutions", 
					"Invalid xSteps value",	"The number of xSteps should be at least 2!");
			return;
		}
		if (tSteps < 2) {
			alert(AlertType.ERROR, "Presenting difference between precise and built solutions",
					"Invalid tSteps value",	"The number of tSteps should be at least 2!");
			return;
		}

		PlotCacheKey key = new PlotCacheKey();
		key.setCalculationType(method.getCalculationType());
		key.setTRange(tRange);
		key.setTSteps(tSteps);
		key.setXRange(xRange);
		key.setXSteps(xSteps);
		key.setDifference(true);
		SoftReference<List<Coord3d>> plotPointsWeak = plotDataCache.get(key);
		List<Coord3d> points = null;
		if(plotPointsWeak == null || (points = plotPointsWeak.get()) == null) {
			log.warn("Points for a plot with parameters {} are absent in cache. Starting the build process", key);
			buildDifferencePlotPointsAndThenShowPlot(key, method);
		} else {
			showPlotStage(points, "Build solution");
		}
		
	}
	
	private void buildDifferencePlotPointsAndThenShowPlot(final PlotCacheKey plotCacheKey,
			DiffeqCalculationMethod method) {
		PlotCacheKey builtPlotCacheKey = new PlotCacheKey(plotCacheKey);
		builtPlotCacheKey.setDifference(false);
		SoftReference<List<Coord3d>> builtPlotPointsWeak = plotDataCache.get(builtPlotCacheKey);
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
		Task<UniformGrid> task = buildPlotPointsTask(plotCacheKey, method);
		task.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, e-> {
			onBuildTaskSucceed(e, plotCacheKey);
			getPrecisePointsAndShowDifferencePlot(plotCacheKey, method,
					((UniformGrid) e.getSource().getValue()).getGridNodePoints());
		});
		buildProgressIndicator.setVisible(true);
		new Thread(task).start();
	}
	
	@SuppressWarnings("unchecked")
	private void getPrecisePointsAndShowDifferencePlot(PlotCacheKey plotCacheKey,
			DiffeqCalculationMethod method, List<Coord3d> builtPoints) {
		PlotCacheKey precisePlotCacheKey = new PlotCacheKey(plotCacheKey);
		precisePlotCacheKey.setCalculationType(CalculationType.PRECISE);
		precisePlotCacheKey.setDifference(false);
		List<Coord3d> precisePoints = createPrecisePlotPontsCachingThem(precisePlotCacheKey);
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
		Alert alert = new Alert(alertType);
		alert.setTitle(title);
		alert.setHeaderText(header);
		alert.setContentText(content);
		alert.showAndWait();
	}
}
