package ua.kpi.iasa.parallel.course;

import java.net.URL;
import java.util.ResourceBundle;

import org.jzy3d.chart.AWTChart;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapHotCold;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Range;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

public class PlotController implements Initializable {
	private final JavaFXChartFactory chartFactory;
	private final AWTChart chart;
	private ImageView imageView;
	
	@FXML private StackPane rootPane;

	public PlotController() {
		chartFactory = new JavaFXChartFactory();
		chart  = getDemoChart("offscreen");
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		imageView = chartFactory.bindImageView(chart);
		rootPane.getChildren().add(imageView);
	}

	private AWTChart getDemoChart(String toolkit) {
		// -------------------------------
		// Define a function to plot
		Mapper mapper = new Mapper() {
			@Override
			public double f(double x, double y) {
				return x * y;
			}
		};

		// Define range and precision for the function to plot
		Range range = new Range(-3, 3);
		int steps = 40;

		// Create the object to represent the function over the given range.
		final Shape surface = Builder.buildOrthonormal(mapper, range, steps);
		surface.setColorMapper(new ColorMapper(new ColorMapHotCold(), surface.getBounds().getZmin(), surface.getBounds().getZmax(), new Color(1, 1, 1, .5f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		surface.setWireframeDisplayed(true);

		// -------------------------------
		// Create a chart
		Quality quality = Quality.Advanced;
		//quality.setSmoothPolygon(true);
		//quality.setAnimated(true);
		// let factory bind mouse and keyboard controllers to JavaFX node
		AWTChart chart = (AWTChart) chartFactory.newChart(quality, toolkit);
		chart.getScene().getGraph().add(surface);
		return chart;
	}
	
	public void addSceneSizeChangedListener(Scene scene) {
		chartFactory.addSceneSizeChangedListener(chart, scene);
	}

}
