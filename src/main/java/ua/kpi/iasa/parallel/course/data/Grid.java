package ua.kpi.iasa.parallel.course.data;

import java.util.List;

import org.jzy3d.maths.Coord3d;

public interface Grid {
	List<Coord3d> getGridNodePoints();
	public GridValuePointer gridValuePointer();
	public Grid subTGrid(int fromTInd, int tStepsCount);
	public Grid subXGrid(int fromXInd, int xStepsCount);
}
