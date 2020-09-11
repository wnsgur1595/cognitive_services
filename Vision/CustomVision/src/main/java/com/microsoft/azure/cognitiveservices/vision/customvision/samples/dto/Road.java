package com.microsoft.azure.cognitiveservices.vision.customvision.samples.dto;

public class Road {
	private double x;
	private double y;
	private int number;
	
	public Road() {}
	
	public Road(double x, double y, int number) {
		this.x = x;
		this.y= y;
		this.number = number;
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
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	@Override
	public String toString() {
		return "Road [x=" + x + ", y=" + y + ", number=" + number + "]";
	}
	
}
