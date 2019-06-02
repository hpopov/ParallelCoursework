package ua.kpi.iasa.parallel.course.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.Callback;
import javafx.util.converter.NumberStringConverter;
import ua.kpi.iasa.parallel.course.MainApp;
import ua.kpi.iasa.parallel.course.main.methods.DiffeqCalculationMethod;
import ua.kpi.iasa.parallel.course.plot.PlotController;
import ua.kpi.iasa.parallel.course.plot.PlotParametersService;

@Controller
public class MainController implements Initializable{
	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	@FXML private ImageView conditionImage;
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
	@FXML private Button showPreciseSolutionButton;
	@FXML private Button showBuiltSolutionButton;
	@FXML private Button showDifferenceButton;
	
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
	@Qualifier("diffeqCalculationMethods")
	private List<DiffeqCalculationMethod> diffeqCalculationMethods;

	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Image image = new Image(conditionImageResource);
		conditionImage.setImage(image);
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
	private void showPreciseSolution(Event e) {
		OrthonormalGrid grid = mainParametersService.getOrthonormalGrid();
		log.info("Showing precise solution in grid {}", grid);
		String fxmlFile = "/fxml/plot.fxml";
		log.debug("Loading FXML for plot view from: {}", fxmlFile);
		FXMLLoader loader = MainApp.makeFxmlLoader();
		Parent rootNode = loadRootNode(fxmlFile, loader);
		log.debug("Showing JFX scene");
		Scene scene = new Scene(rootNode, 500, 530);
		scene.getStylesheets().add("/styles/styles.css"); 

		PlotController plotController = loader.getController();
		plotController.addSurfaceFromFunction(grid, preciseSolutionService.getPreciseSolutionFunction());
		plotController.addSceneSizeChangedListener(scene);
		plotController.initializeContent();
		showNewStage(scene, "Precise solution");
	}

	private Parent loadRootNode(String fxmlFile, FXMLLoader loader) {
		Parent rootNode = null;
		try {
			rootNode = loader.load(MainApp.class.getResourceAsStream(fxmlFile));
		} catch (IOException ex) {
			throw new RuntimeException(
					String.format("Unable to load view from %s. Operation will be aborted.", fxmlFile));
		}
		return rootNode;
	}

	private void showNewStage(Scene scene, String title) {
		Stage stage = new Stage();
		stage.setTitle(title);
		stage.setScene(scene);
		stage.show();
	}
	
	@FXML
	private void showBuiltSolution(Event e) {
		OrthonormalGrid grid = mainParametersService.getOrthonormalGrid();
		log.info("Showing built solution in grid {}", grid);
		String fxmlFile = "/fxml/plot.fxml";
		log.debug("Loading FXML for plot view from: {}", fxmlFile);
		FXMLLoader loader = MainApp.makeFxmlLoader();
		Parent rootNode = loadRootNode(fxmlFile, loader);
		log.debug("Showing JFX scene");
		Scene scene = new Scene(rootNode, 500, 530);
		scene.getStylesheets().add("/styles/styles.css");

		PlotController plotController = loader.getController();
		List<Coord3d> points = calculationMethod.getValue()
				.solveDiffEquation(mainParametersService.getXRange(), mainParametersService.getTRange(),
						mainParametersService.getXSteps(), mainParametersService.getTSteps());
		plotController.addSurfaceFromPoints(points );
		plotController.addSceneSizeChangedListener(scene);
		plotController.initializeContent();
		showNewStage(scene, "Built solution");
	}
	
	@FXML
	private void findTStepsToBeConvergent(Event e) {
		xSteps.setDisable(true);
		tSteps.setDisable(true);
		int suitableTSteps = calculationMethod.getValue().findSuitableTSteps(mainParametersService.getXRange(),
				mainParametersService.getTRange(), mainParametersService.getXSteps(),
				mainParametersService.getTSteps());
		mainParametersService.setTSteps(suitableTSteps);
		xSteps.setDisable(false);
		tSteps.setDisable(false);
	}
}
