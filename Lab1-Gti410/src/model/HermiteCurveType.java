package model;

import java.awt.Point;
import java.util.List;

/**
 * <p>Title: HermiteCurveType</p>
 * <p>Description: ... (CurveType)</p>
 * <p>Copyright: Copyright (c) 2004 Eric Paquette</p>
 * <p>Company: (ÉTS) - École de Technologie Supérieure</p>
 * @author Eric Paquette / Toaibou Traore (modification)
 * @version $Revision: 1.3 $
 */
public class HermiteCurveType extends CurveType{

	public HermiteCurveType(String name) {
		super(name);
	}
	
	/* (non-Javadoc)
	 * @see model.CurveType#getNumberOfSegments(int)
	 */
	public int getNumberOfSegments(int numberOfControlPoints) {
		if (numberOfControlPoints >= 4) {
			return (numberOfControlPoints - 1) / 3;
		} else {
			return 0;
		}
	}

	/* (non-Javadoc)
	 * @see model.CurveType#getNumberOfControlPointsPerSegment()
	 */
	public int getNumberOfControlPointsPerSegment() {
		return 4;
	}

	/* (non-Javadoc)
	 * @see model.CurveType#getControlPoint(java.util.List, int, int)
	 */
	public ControlPoint getControlPoint(
		List controlPoints,
		int segmentNumber,
		int controlPointNumber) {
		int controlPointIndex = segmentNumber * 3 + controlPointNumber;
		return (ControlPoint)controlPoints.get(controlPointIndex);
	}

	/* (non-Javadoc)
	 * @see model.CurveType#evalCurveAt(java.util.List, double)
	 */
	public Point evalCurveAt(List controlPoints, double t) {
		Point P1 = ((ControlPoint)controlPoints.get(0)).getCenter();
		Point P2 = ((ControlPoint)controlPoints.get(1)).getCenter();
		Point P3 = ((ControlPoint)controlPoints.get(2)).getCenter();
		Point P4 = ((ControlPoint)controlPoints.get(3)).getCenter();
		
		Point R1 = premierRetour(P1,P2);
		Point R4 = deuxiemeRetour(P3,P4);
		
		List tVector = Matrix.buildRowVector4(t*t*t, t*t, t, 1);
		List gVector = Matrix.buildColumnVector4(P1, 
			P4, 
			R1,
			R4);
		
		Point p = Matrix.eval(tVector, matrix, gVector);
		return p;
	}
	
	//Retourne le point R1
	public Point premierRetour(Point P1, Point P2) {
		int xo = P2.x - P1.x;
		int yo = P2.y - P1.y;
		
		return new Point(xo,yo);
		
	}
	
	//Retourne le point R4
	public Point deuxiemeRetour(Point P3, Point P4) {
		int xp = P4.x - P3.x;
		int yp = P4.y - P3.y;
		
		return new Point(xp,yp);
	}

	private List hermiteMatrix = 
		Matrix.buildMatrix4(2,  -2, 1, 1, 
							 -3, 3,  -2, -1, 
							0,  0,  1, 0, 
							 1,  0,  0, 0);
							 
	private List matrix = hermiteMatrix;
	
}
