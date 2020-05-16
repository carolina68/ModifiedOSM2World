package org.osm2world.core.world.modules.common;

import org.osm2world.core.target.common.material.Material;

public class TrafficSignType {

	/** holds the rotation angle of DestinationSigns */
	public double rotation;

	/** Variable to use in {@link org.osm2world.core.world.modules.TrafficSignModule#mapSignAttributes()} */
	public String materialName;
	
	public Material material;	
	public final int numPosts;
	public double defaultHeight;
	
	public TrafficSignType(Material material, int numPosts, double height) {
		this.material = material;
		this.numPosts = numPosts;
		this.defaultHeight = height;
	}

	public TrafficSignType(String materialName, int numPosts, double height) {

		this.materialName = materialName;

		this.material = null;
		this.numPosts = numPosts;
		this.defaultHeight = height;
	}
}
