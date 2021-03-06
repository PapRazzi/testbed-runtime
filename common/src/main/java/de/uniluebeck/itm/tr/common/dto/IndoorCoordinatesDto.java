package de.uniluebeck.itm.tr.common.dto;

import javax.annotation.Nullable;

public class IndoorCoordinatesDto {

	@Nullable
	private String building;

	@Nullable
	private String floor;

	@Nullable
	private String room;

	private double x;

	private double y;

	@Nullable
	private Double z;

	@Nullable
	private String backgroundImage;

	@Nullable
	public String getBackgroundImage() {
		return backgroundImage;
	}

	public void setBackgroundImage(@Nullable final String backgroundImage) {
		this.backgroundImage = backgroundImage;
	}

	@Nullable
	public String getBuilding() {
		return building;
	}

	public void setBuilding(@Nullable final String building) {
		this.building = building;
	}

	@Nullable
	public String getFloor() {
		return floor;
	}

	public void setFloor(@Nullable final String floor) {
		this.floor = floor;
	}

	@Nullable
	public String getRoom() {
		return room;
	}

	public void setRoom(@Nullable final String room) {
		this.room = room;
	}

	public double getX() {
		return x;
	}

	public void setX(final double x) {
		this.x = x;
	}

	public double getY() {
		return y;
	}

	public void setY(final double y) {
		this.y = y;
	}

	@Nullable
	public Double getZ() {
		return z;
	}

	public void setZ(@Nullable final Double z) {
		this.z = z;
	}
}
