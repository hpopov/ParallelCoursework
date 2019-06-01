package ua.kpi.iasa.parallel.course.main;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.jzy3d.maths.Coord3d;
import org.jzy3d.maths.Range;
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
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.NumberStringConverter;
import ua.kpi.iasa.parallel.course.MainApp;
import ua.kpi.iasa.parallel.course.plot.PlotController;

@Controller
public class MainController implements Initializable{
	private static final Logger log = LoggerFactory.getLogger(MainController.class);

	@FXML private ImageView conditionImage;
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
	
	@Autowired
	private EquationService equationService;

	@Autowired()
	@Qualifier("conditionImageResource")
	private InputStream conditionImageResource;

	public MainController() {
//		conditionImageResource = MainController.class.getResourceAsStream("/images/condition.png");
	}
	
	@Override
	public void initialize(URL arg0, ResourceBundle arg1) {
		Image image = new Image(conditionImageResource);
		conditionImage.setImage(image);
		
		xMin.textProperty().bindBidirectional(equationService.xMinProperty(), new NumberStringConverter());
		xMax.textProperty().bindBidirectional(equationService.xMaxProperty(), new NumberStringConverter());
		xSteps.textProperty().bindBidirectional(equationService.xStepsProperty(),
				new NumberStringConverter());
		tMin.textProperty().bindBidirectional(equationService.tMinProperty(), new NumberStringConverter());
		tMax.textProperty().bindBidirectional(equationService.tMaxProperty(), new NumberStringConverter());
		tSteps.textProperty().bindBidirectional(equationService.tStepsProperty(),
				new NumberStringConverter());
	}
	
	public void showPreciseSolution(Event e) {
		OrthonormalGrid grid = equationService.getOrthonormalGrid();
		log.info("Showing precise solution in grid {}", grid);
		String fxmlFile = "/fxml/plot.fxml";
		log.debug("Loading FXML for plot view from: {}", fxmlFile);
		FXMLLoader loader = MainApp.makeFxmlLoader();
		Parent rootNode = null;
		try {
			rootNode = loader.load(MainApp.class.getResourceAsStream(fxmlFile));
		} catch (IOException ex) {
			log.error("Unable to load view from {}. Operation will be aborted.", fxmlFile);
			return;
		}
		log.debug("Showing JFX scene");
		Scene scene = new Scene(rootNode, 500, 530);
		scene.getStylesheets().add("/styles/styles.css"); 

		PlotController plotController = loader.getController();
		plotController.addSurfaceFromFunction(grid, equationService.getPreciseSolutionFunction());
		plotController.addSceneSizeChangedListener(scene);
		plotController.setName("Precise solution");
		plotController.initializeContent();
		Stage stage = new Stage();
		stage.setTitle("Parallel calculation coursework presentation");
		stage.setScene(scene);
		stage.show();
		
		
//		log.info("Starting application");
//		String fxmlFile = "/fxml/plot.fxml";
//		log.debug("Loading FXML for main view from: {}", fxmlFile);
//		FXMLLoader loader = new FXMLLoader();
//		Parent rootNode;
//		try {
//			rootNode = loader.load(getClass().getResourceAsStream(fxmlFile));
//		} catch (IOException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//			return;
//		}
//		log.debug("Showing JFX scene");
//		Scene scene = new Scene(rootNode, 500, 530);
//		scene.getStylesheets().add("/styles/styles.css");
//
//		int size = 30;
//		float x;
//		float y;
//		float z;
//		List<Coord3d> points;
//		points = new ArrayList<>(size);
//
//		for(int i=0; i<size; i++){
//			x = i*1.5f-0.5f;
//			for (int j = 0; j<size;j++) {
//				y = j*1.5f-0.5f;
//				z = x*y;
//				points.add(new Coord3d(x, y, z));
//			}
//		}  
//
//		PlotController plotController = loader.getController();
//		OrthonormalGrid grid = new OrthonormalGrid(new Range(-3,3), 1);
//		//		plotController.addSurfaceFromPoint(points);
//		plotController.addSurfaceFromFunction(grid, (x1,y1)->x1+y1);
//		plotController.addSceneSizeChangedListener(scene);
//		plotController.setName("DFDF");
//		plotController.initializeContent();
//		Stage stage = new Stage();
//		stage.setTitle("Parallel calculation coursework presentation");
//		stage.setScene(scene);
//		stage.show();
	}
}
