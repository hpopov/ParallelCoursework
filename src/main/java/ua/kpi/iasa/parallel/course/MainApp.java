package ua.kpi.iasa.parallel.course;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public void start(Stage stage) throws Exception {
		log.info("Starting application");
		String fxmlFile = "/fxml/plot.fxml";
		log.debug("Loading FXML for main view from: {}", fxmlFile);
		FXMLLoader loader = new FXMLLoader();
		Parent rootNode = loader.load(getClass().getResourceAsStream(fxmlFile));

		log.debug("Showing JFX scene");
		Scene scene = new Scene(rootNode, 500, 500);
		scene.getStylesheets().add("/styles/styles.css");

		PlotController plotController = loader.getController();
		plotController.addSceneSizeChangedListener(scene);
		stage.setTitle("Parallel calculation coursework presentation");
		stage.setScene(scene);
		stage.show();
	}
}
