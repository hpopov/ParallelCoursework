package ua.kpi.iasa.parallel.course;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.jzy3d.maths.Coord3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import ua.kpi.iasa.parallel.course.main.MainController;
import ua.kpi.iasa.parallel.course.plot.PlotController;

public class MainApp extends Application {

	private static final Logger log = LoggerFactory.getLogger(MainApp.class);

	public static void main(String[] args) throws Exception {
		launch(args);
	}

	public static AnnotationConfigApplicationContext context;
	
	static {
    	log.info("Loading Spring context");
    	context = new AnnotationConfigApplicationContext();
    	context.register(SpringConfig.class);
    	context.refresh();
	}
	
	public static FXMLLoader makeFxmlLoader() {
		FXMLLoader loader = new FXMLLoader();
        loader.setControllerFactory(MainApp.context::getBean);
        return loader;
	}

	public void start(Stage stage) throws Exception {
		log.info("Starting application");
		String fxmlFile = "/fxml/main.fxml";
		log.debug("Loading FXML for main view from: {}", fxmlFile);
		FXMLLoader loader = makeFxmlLoader();
		Parent rootNode = loader.load(getClass().getResourceAsStream(fxmlFile));
		log.debug("Showing JFX scene");
		Scene scene = new Scene(rootNode, 800, 600);
//		scene.getStylesheets().add("/styles/styles.css");
//		MainController mainController = loader.getController();
		stage.setTitle("Parallel calculation coursework presentation");
		stage.setScene(scene);
		stage.show();
	}
	
	
}
