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
import org.springframework.stereotype.Controller;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;

@Controller
public class PlotController{
	private final JavaFXChartFactory chartFactory;
	private AWTChart chart;
	
	@FXML private StackPane graphicPane;
	@FXML private Label name;

	public PlotController() {
		chartFactory = new JavaFXChartFactory();
		
		chart = makeChart();
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
	
	public void addSurfaceFromPoint(final List<Coord3d> points) {
		OrthonormalTessellator tesselator = new OrthonormalTessellator();
		final Shape surface = (Shape) tesselator.build(points);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
				surface.getBounds().getZmin(), surface.getBounds().getZmax(),
				new Color(1, 1, 1, 1.f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		surface.setWireframeDisplayed(true);
		
		chart.getScene().getGraph().add(surface);
	}
	
	public void addSurfaceFromFunction(final OrthonormalGrid grid,
			final DoubleBinaryOperator function) {
		Mapper mapper = new FunctionMapper(function);
		final Shape surface = Builder.buildOrthonormal(grid, mapper);
		surface.setColorMapper(new ColorMapper(new ColorMapRainbow(),
				surface.getBounds().getZmin(), surface.getBounds().getZmax(),
				new Color(1, 1, 1, 1.f)));
		surface.setFaceDisplayed(true);
		surface.setWireframeColor(Color.BLACK);
		surface.setWireframeDisplayed(true);
		
		chart.getScene().getGraph().add(surface);
	}
	
	public void addSceneSizeChangedListener(Scene scene) {
		chartFactory.addSceneSizeChangedListener(chart, scene);
	}
	
	public void setName(String name) {
		this.name.setText(name);
	}
	
	public void initializeContent() {
		ImageView imageView = chartFactory.bindImageView(chart);
		graphicPane.getChildren().add(imageView);
	}

}
