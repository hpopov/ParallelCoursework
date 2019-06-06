package ua.kpi.iasa.parallel.course.data;

import java.util.Comparator;

import org.jzy3d.maths.Coord3d;

public class Coord3dComparator implements Comparator<Coord3d> {

	private static Coord3dComparator instance;

	public static Coord3dComparator getInstance() {
		if (instance == null) {
			instance = new Coord3dComparator();
		}
		return instance;
	}
	
	@Override
	public int compare(Coord3d arg0, Coord3d arg1) {
		return (int) ((arg0.x*1000000 + arg0.y) - (arg1.x*1000000 + arg1.y));
	}

}
