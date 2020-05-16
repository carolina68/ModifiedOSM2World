package org.osm2world.viewer.view.debug;

import java.awt.Color;
import java.util.Collection;
import java.util.Collections;

import org.osm2world.core.map_data.data.MapArea;
import org.osm2world.core.map_data.data.MapData;
import org.osm2world.core.map_data.data.MapNode;
import org.osm2world.core.map_data.data.MapWaySegment;
import org.osm2world.core.map_data.data.overlaps.MapIntersectionWW;
import org.osm2world.core.map_data.data.overlaps.MapOverlap;
import org.osm2world.core.map_data.data.overlaps.MapOverlapAA;
import org.osm2world.core.map_data.data.overlaps.MapOverlapWA;
import org.osm2world.core.math.LineSegmentXZ;
import org.osm2world.core.math.TriangleXZ;
import org.osm2world.core.math.Vector3D;
import org.osm2world.core.math.VectorXZ;
import org.osm2world.core.math.algorithms.TriangulationUtil;
import org.osm2world.core.target.common.material.ImmutableMaterial;
import org.osm2world.core.target.common.material.Material.Interpolation;
import org.osm2world.core.target.jogl.JOGLTarget;

/**
 * shows the plain {@link MapData} as a network of nodes, lines and areas
 */
public class MapDataDebugView extends DebugView {

	@Override
	public String getDescription() {
		return "shows the map data (without elevation) as a network of nodes, lines and areas";
	}

	private static final Color LINE_COLOR = Color.WHITE;
	private static final Color NODE_COLOR = Color.YELLOW;
	private static final Color INTERSECTION_COLOR = Color.RED;
	private static final Color SHARED_SEGMENT_COLOR = Color.ORANGE;
	private static final Color AREA_COLOR = new Color(0.8f, 0.8f, 1);

	private static final float HALF_NODE_WIDTH = 0.4f;

	@Override
	public boolean canBeUsed() {
		return map != null;
	}

	@Override
	public void fillTarget(JOGLTarget target) {

		for (MapArea area : map.getMapAreas()) {
			Vector3D[] vs = new Vector3D[area.getBoundaryNodes().size()];
			for (int i=0; i < area.getBoundaryNodes().size(); i++) {
				vs[i] = area.getBoundaryNodes().get(i).getPos();
			}

			Collection<TriangleXZ> triangles =
				TriangulationUtil.triangulate(area.getPolygon());

			for (TriangleXZ t : triangles) {
				target.drawTriangles(
						new ImmutableMaterial(Interpolation.FLAT, AREA_COLOR),
						Collections.singleton(t.xyz(-0.1)),
						null);
			}

		}

		for (MapWaySegment line : map.getMapWaySegments()) {
			drawArrow(target, LINE_COLOR, 0.7f,
					line.getStartNode().getPos().xyz(0),
					line.getEndNode().getPos().xyz(0));
		}

		for (MapNode node : map.getMapNodes()) {
			drawBoxAround(target, node.getPos(),
					NODE_COLOR, HALF_NODE_WIDTH);
		}

		for (MapWaySegment line : map.getMapWaySegments()) {
			for (MapIntersectionWW intersection : line.getIntersectionsWW()) {
				drawBoxAround(target, intersection.pos,
						INTERSECTION_COLOR, HALF_NODE_WIDTH);
			}
		}

		for (MapArea area : map.getMapAreas()) {
			for (MapOverlap<?, ?> overlap : area.getOverlaps()) {
				if (overlap instanceof MapOverlapWA) {
					for (VectorXZ pos : ((MapOverlapWA)overlap).getIntersectionPositions()) {
						drawBoxAround(target, pos,
								INTERSECTION_COLOR, HALF_NODE_WIDTH);
					}
					for (LineSegmentXZ seg : ((MapOverlapWA)overlap).getSharedSegments()) {
						target.drawLineStrip(SHARED_SEGMENT_COLOR, 3, seg.p1.xyz(0), seg.p2.xyz(0));
					}
					for (LineSegmentXZ seg : ((MapOverlapWA)overlap).getOverlappedSegments()) {
						target.drawLineStrip(INTERSECTION_COLOR, 3, seg.p1.xyz(0), seg.p2.xyz(0));
					}
				} else if (overlap instanceof MapOverlapAA) {
					for (VectorXZ pos : ((MapOverlapAA)overlap).getIntersectionPositions()) {
						drawBoxAround(target, pos,
								INTERSECTION_COLOR, HALF_NODE_WIDTH);
					}
				}
			}
		}

	}

}
