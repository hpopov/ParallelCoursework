package ua.kpi.iasa.parallel.course;

import java.io.InputStream;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import javafx.fxml.FXMLLoader;
import ua.kpi.iasa.parallel.course.plot.PlotController;

@Configuration
@ComponentScan("ua.kpi.iasa.parallel.course")
public class SpringConfig {

	@Bean(name = "conditionImageResource")
	public InputStream conditionImageResource() {
		return getClass().getResourceAsStream("/images/condition.png");
	}
}
