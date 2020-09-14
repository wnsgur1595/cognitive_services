package com.microsoft.azure.cognitiveservices.vision.customvision.samples.dto;

public class Road {
	private double x;
	private double y;
	private String tagName;
	
	public Road() {}
	
	public Road(double x, double y, String tagName) {
		this.x = x;
		this.y = y;
		this.tagName = tagName;
	}
	
	public double getX() {
		return x;
	}
	public void setX(double x) {
		this.x = x;
	}
	public double getY() {
		return y;
	}
	public void setY(double y) {
		this.y = y;
	}
	public String getTagName() {
		return tagName;
	}
	public void setTagName(String tagName) {
		this.tagName = tagName;
	}
	@Override
	public String toString() {
		return "Road [x=" + x + ", y=" + y + ", tagName=" + tagName + "]";
	}
	
}
