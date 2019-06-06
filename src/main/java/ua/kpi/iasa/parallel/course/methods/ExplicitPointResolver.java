package ua.kpi.iasa.parallel.course.methods;

public class ExplicitPointResolver {

	private final double sigma;
	private final double dx, dt;

	public ExplicitPointResolver(double dt, double dx, double alpha) {
		this.dx = dx;
		this.dt = dt;
		sigma = 2*alpha*dt/(dx*dx);
	}

	public double wTop(double wBottom, double wLeft, double wCenter, double wRight) {
		return wBottom + sigma * Math.pow(wCenter, -1./3) * (
				Math.pow(wRight-wLeft, 2)/6
				+ wCenter * (wLeft - 2*wCenter + wRight)
				);
	}
}
