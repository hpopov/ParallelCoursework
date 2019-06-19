package ua.kpi.iasa.parallel.course.plot;

import java.util.List;
import java.util.function.DoubleBinaryOperator;

import org.jzy3d.chart.AWTChart;
import org.jzy3d.chart.factories.IChartComponentFactory.Toolkit;
import org.jzy3d.colors.Color;
import org.jzy3d.colors.ColorMapper;
import org.jzy3d.colors.colormaps.ColorMapRainbow;
import org.jzy3d.javafx.JavaFXChartFactory;
import org.jzy3d.maths.Coord3d;
import org.jzy3d.plot3d.builder.Builder;
import org.jzy3d.plot3d.builder.Mapper;
import org.jzy3d.plot3d.builder.concrete.OrthonormalGrid;
import org.jzy3d.plot3d.builder.concrete.OrthonormalTessellator;
import org.jzy3d.plot3d.primitives.Shape;
import org.jzy3d.plot3d.rendering.canvas.Quality;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

@Controller
@Scope("prototype")
public class PlotController{
	private final JavaFXChartFactory chartFactory;
	private final BooleanProperty isWireframeDisplayedProperty;
	private final AWTChart chart;
	
	@FXML private StackPane graphicPane;
//	@FXML private Label name;
	
//	private PlotParametersService plotParametersService;

	@Autowired
	public PlotController(final PlotParametersService plotParametersService) {
		chartFactory = new JavaFXChartFactory();
//		this.plotParametersService = plotParametersService;
		isWireframeDisplayedProperty = plotParametersService.isWireframeDisplayedProperty();
		System.out.println("Before making chart...");
		chart = makeChart();
		System.out.println("After making chart!");
	}

	private AWTChart makeChart() {
		Quality quality = Quality.Advanced;
		//quality.setSmoothPolygon(true);
		//quality.setAnimated(true);
		AWTChart chart = (AWTChart) chartFactory.newChart(quality, Toolkit.offscreen);
		chart.getAxeLayout().setYAxeLabel("T");
		chart.getAxeLayout().setZAxeLabel("W");
		return chart;
	}
	
	public Task<Void> makeAddSurfaceFromPointsTask(final List<Coord3d> points) {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				OrthonormalTessellator tesselator = new OrthonormalTessellator();
				final Shape surface = (Shape) tesselator.build(points);
				setupSurface(surface);
				chart.getScene().getGraph().add(surface);
				updateProgress(1, 1);
				return null;
			}
		};
	}

	private void setupSurface(final Shape surface) {
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
				surface.getBounds().getZmin(), surface.getBounds().getZmax(),
				new Color(1, 1, 1, 1.f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		surface.setWireframeDisplayed(isWireframeDisplayedProperty.get());
//		s/
		isWireframeDisplayedProperty.addListener(
				(observable, oldV, newV)-> surface.setWireframeDisplayed(newV));
	}
	
	public Task<Void> makeAddSurfaceFromFunctionTask(final OrthonormalGrid grid,
			final DoubleBinaryOperator function) {
		return new Task<Void>() {

			@Override
			protected Void call() throws Exception {
				Mapper mapper = new FunctionMapper(function);
				final Shape surface = Builder.buildOrthonormal(grid, mapper);
				setupSurface(surface);
				chart.getScene().getGraph().add(surface);
				updateProgress(1, 1);
				return null;
			}
		};
	}
	
	public void addSceneSizeChangedListener(Scene scene) {
		chartFactory.addSceneSizeChangedListener(chart, scene);
//		chartFactory.ad
	}
	
//	public void setName(String name) {
//		this.name.setText(name);
//	}
	
	public void initializeContent() {
		ImageView imageView = chartFactory.bindImageView(chart);
		graphicPane.getChildren().add(imageView);
	}

}
