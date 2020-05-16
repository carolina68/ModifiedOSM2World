package org.osm2world.core.target.common.rendering;

import static org.osm2world.core.math.VectorXYZ.Y_UNIT;

import org.osm2world.core.math.VectorXYZ;
import org.osm2world.core.math.VectorXZ;

public class Camera {

	VectorXYZ up;
	VectorXYZ pos;
	VectorXYZ lookAt;

	/** returns the view direction vector with length 1 */
	public VectorXYZ getViewDirection() {
		//TODO: (performance)? cache viewDirection
		return lookAt.subtract(pos).normalize();
	}

	/**
	 * returns the vector that is orthogonal to the connection
	 * between pos and lookAt and points to the right of it.
	 * The result has length 1.
	 */
	public VectorXYZ getRight() {
		return getViewDirection().crossNormalized(up);
	}

	public VectorXYZ getPos() {
		return pos;
	}

	public VectorXYZ getLookAt() {
		return lookAt;
	}

	public VectorXYZ getUp() {
		return up;
	}

	public void setPos(VectorXYZ pos) {
		this.pos = pos;
	}

	public void setCamera(double posX, double posY, double posZ,
			double lookAtX, double lookAtY, double lookAtZ) {
		setPos(posX, posY, posZ);
		up = Y_UNIT; // some initial setup value
		setLookAt(lookAtX, lookAtY, lookAtZ);
	}

	public void setCamera(double posX, double posY, double posZ,
			double lookAtX, double lookAtY, double lookAtZ,
			double upX, double upY, double upZ) {
		setPos(posX, posY, posZ);
		up = new VectorXYZ(upX, upY, upZ);
		setLookAt(lookAtX, lookAtY, lookAtZ);
	}

	private void setPos(double x, double y, double z) {
		this.setPos(new VectorXYZ(x, y, z));
	}

	private void setLookAt(VectorXYZ lookAt) {
		this.lookAt = lookAt;

		VectorXYZ right = getRight();
		up = right.crossNormalized(getViewDirection());
	}

	private void setLookAt(double x, double y, double z) {
		this.setLookAt(new VectorXYZ(x, y, z));
	}

	/**
	 * moves pos and lookAt in the view direction
	 * @param step  units to move forward
	 */
	public void moveForward(double step) {
		VectorXYZ d = getViewDirection();
		move(d.x * step, d.y * step, d.z * step);
	}

	/**
	 * moves pos and lookAt forward in the map plane
	 * @param step  units to move forward
	 */
	public void moveMapForward(double step) {
		VectorXYZ d = getViewDirection();
		VectorXZ md = new VectorXZ(d.x, d.z).normalize();
		move(md.x * step, 0, md.z * step);
	}

	/**
	 * moves pos and lookAt to the right, orthogonally to the view direction
	 *
	 * @param step  units to move right, negative units move to the left
	 */
	public void moveRight(double step) {
		VectorXYZ right = getRight();
		move(right.x * step, right.y * step, right.z * step);
	}

	/**
	 * moves pos and lookAt to the right in the map plane
	 *
	 * @param step  units to move right, negative units move to the left
	 */
	public void moveMapRight(double step) {
		VectorXYZ right = getRight();
		VectorXZ md = new VectorXZ(right.x, right.z).normalize();
		move(md.x * step, 0, md.z * step);
	}

	/**
	 * move pos and lookAt upwards, orthogonally to the view direction
	 *
	 * @param step units to move up, negative units move down
	 */
	public void moveUp(double step) {
		move(up.x * step, up.y * step, up.z * step);
	}

	/**
	 * move pos and lookAt upwards in respect to the map plane
	 *
	 * @param step units to move up, negative units move down
	 */
	public void moveMapUp(double step) {
		move(0, step, 0);
	}


	/** moves both pos and lookAt by the given vector */
	public void move(VectorXYZ move) {
		pos = pos.add(move);
		lookAt = lookAt.add(move);
	}

	/** moves both pos and lookAt by the given vector */
	public void move(double moveX, double moveY, double moveZ) {
		pos = pos.add(moveX, moveY, moveZ);
		lookAt = lookAt.add(moveX, moveY, moveZ);
	}

	/**
	 * moves lookAt to represent a rotation counterclockwise
	 * around the y axis on pos
	 *
	 * @param d  angle in radians
	 */
	public void rotateY(double d) {

		up = up.rotateY(d);
		VectorXYZ toOldLookAt = lookAt.subtract(pos);
		VectorXYZ toNewLookAt = toOldLookAt.rotateY(d);

		lookAt = pos.add(toNewLookAt);
	}

	/**
	 * rotates the camera around the yaw axis
	 *
	 * @param d  angle in radians
	 */
	public void yaw(double d) {

		VectorXYZ toOldLookAt = lookAt.subtract(pos);
		VectorXYZ toNewLookAt = toOldLookAt.rotateVec(d, up);

		lookAt = pos.add(toNewLookAt);
	}

	/**
	 * rolls the camera
	 *
	 * @param d  angle in radians
	 */
	public void roll(double d) {
		VectorXYZ view = getViewDirection();
		up = up.rotateVec(d, view);
	}

	/**
	 * rotates the camera around the pitch axis
	 *
	 * @param d  angle in radians
	 */
	public void pitch(double d) {
		VectorXYZ right = getRight();

		up = up.rotateVec(d, right);

		VectorXYZ toOldLookAt = lookAt.subtract(pos);
		VectorXYZ toNewLookAt = toOldLookAt.rotateVec(d, right);
		lookAt = pos.add(toNewLookAt);
	}

	/**
	 * rotates the camera around an axis orthogonal to the y axis
	 * and {@link #getViewDirection()}.
	 * The effect is similar to {@link #pitch(double)}, but independent
	 * from the current roll angle.
	 *
	 * @param d  angle in radians
	 */
	public void mapPitch(double d) {

		VectorXYZ right = getViewDirection().crossNormalized(Y_UNIT);

		up = up.rotateVec(d, right);

		VectorXYZ toOldLookAt = lookAt.subtract(pos);
		VectorXYZ toNewLookAt = toOldLookAt.rotateVec(d, right);
		lookAt = pos.add(toNewLookAt);
	}

	@Override
	public String toString() {
		return "{pos=" + pos + ", lookAt=" + lookAt + ", up=" + up + "}";
	}
}
