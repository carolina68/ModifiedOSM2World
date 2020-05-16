package org.osm2world.viewer.view.debug;

import java.awt.Color;

import org.osm2world.core.map_elevation.data.GroundState;
import org.osm2world.core.target.jogl.JOGLTarget;

/**
 * shows information from elevation calculation
 */
public class ClearingDebugView extends DebugView {

	private static final int LINE_WIDTH = 5;
	private static final float HALF_NODE_WIDTH = 0.4f;
	private static final int NODE_COLUMN_WIDTH = 5;

	private static final Color LINE_SURFACE_COLOR = Color.LIGHT_GRAY;
	private static final Color LINE_BELOW_COLOR = Color.YELLOW;
	private static final Color LINE_ABOVE_COLOR = Color.BLUE;

	@Override
	public String getDescription() {
		return "shows information from elevation calculation";
	}

	@Override
	public boolean canBeUsed() {
		return map != null;
	}

	@Override
	public void fillTarget(JOGLTarget target) {

		//TODO useless for new elevation calculation

//		for (MapWaySegment line : map.getMapWaySegments()) {
//
//			for (WorldObject rep : line.getRepresentations()) {
//
//				WaySegmentElevationProfile profile = line.getElevationProfile();
//				List<VectorXYZ> pointsWithEle = profile.getPointsWithEle();
//				int size = pointsWithEle.size();
//
//				VectorXYZ[] linePoints = new VectorXYZ[size];
//				VectorXYZ[] upperClearingPoints = new VectorXYZ[size];
//				VectorXYZ[] lowerClearingPoints = new VectorXYZ[size];
//
//				VectorXYZ[] clearingPolygonPoints = new VectorXYZ[2*size];
//
//				for (int i = 0; i < size; i++) {
//					VectorXYZ p = pointsWithEle.get(i);
//
//					linePoints[i] = p;
//
//					final VectorXYZ pMin = p.y(p.y-rep.getClearingBelow(p.xz()));
//					final VectorXYZ pMax = p.y(p.y+rep.getClearingAbove(p.xz()));
//
//					upperClearingPoints[i] = pMin;
//					lowerClearingPoints[size-1-i] = pMax;
//
//					clearingPolygonPoints[i] = pMin;
//					clearingPolygonPoints[2*size-1-i] = pMax;
//
//					//TODO: this isn't necessarily the maximum clearing precision!
//
//				}
//
//				Color color = getColorForState(rep.getGroundState());
//
//				target.drawLineStrip(color, LINE_WIDTH, linePoints);
//				target.drawLineStrip(color, 1, upperClearingPoints);
//				target.drawLineStrip(color, 1, lowerClearingPoints);
//
//				/* TODO replace stipple effect
//				gl.glEnable(GL2.GL_POLYGON_STIPPLE);
//				gl.glPolygonStipple(STIPPLE_PATTERN, 0);
//				*/
//				target.drawConvexPolygon(
//						new ImmutableMaterial(Lighting.FLAT, color),
//						asList(clearingPolygonPoints), null);
//				/*
//				gl.glDisable(GL2.GL_POLYGON_STIPPLE);
//				*/
//
//			}
//
//		}
//
//		for (MapNode node : map.getMapNodes()) {
//
//			for (NodeWorldObject rep : node.getRepresentations()) {
//
//				NodeElevationProfile profile = node.getElevationProfile();
//				Color color = getColorForState(rep.getGroundState());
//				VectorXYZ p = profile.getPointWithEle();
//
//				drawBoxAround(target, profile.getPointWithEle(), color, HALF_NODE_WIDTH);
//
//				target.drawLineStrip(color, NODE_COLUMN_WIDTH,
//						p.y(p.y-rep.getClearingBelow(p.xz())),
//						p.y(p.y+rep.getClearingAbove(p.xz())));
//
//			}
//
//		}

	}

	private static Color getColorForState(GroundState state) {
		if (state == GroundState.ABOVE) {
			return LINE_ABOVE_COLOR;
		} else if (state == GroundState.BELOW) {
			return LINE_BELOW_COLOR;
		} else {
			return LINE_SURFACE_COLOR;
		}
	}

	private static final byte STIPPLE_PATTERN[] =
	  { (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x11, (byte) 0x11,
	    (byte) 0x11, (byte) 0x11, (byte) 0x88, (byte) 0x88, (byte) 0x88,
	    (byte) 0x88, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x11,
	    (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x11, (byte) 0x11, (byte) 0x11,
	    (byte) 0x11, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88,
	    (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x11, (byte) 0x11,
	    (byte) 0x11, (byte) 0x11, (byte) 0x88, (byte) 0x88, (byte) 0x88,
	    (byte) 0x88, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x11,
	    (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x11, (byte) 0x11, (byte) 0x11,
	    (byte) 0x11, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88,
	    (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x11, (byte) 0x11,
	    (byte) 0x11, (byte) 0x11, (byte) 0x88, (byte) 0x88, (byte) 0x88,
	    (byte) 0x88, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x11,
	    (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x11, (byte) 0x11, (byte) 0x11,
	    (byte) 0x11, (byte) 0x88, (byte) 0x88, (byte) 0x88, (byte) 0x88,
	    (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x11, (byte) 0x88,
	    (byte) 0x88, (byte) 0x88, (byte) 0x88 };

}
