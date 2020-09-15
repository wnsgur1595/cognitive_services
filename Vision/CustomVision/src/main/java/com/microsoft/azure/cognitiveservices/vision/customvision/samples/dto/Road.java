package com.microsoft.azure.cognitiveservices.vision.customvision.samples.dto;

public class Road {
	private double x;
	private double y;
	private String tagName;
	private String fileName;
	private double tagPercent;
	
	public Road() {}
	
	public Road(double x, double y, double tagPercent, String tagName, String fileName) {
		this.x = x;
		this.y = y;
		this.tagPercent = tagPercent;
		this.tagName = tagName;
		this.fileName = fileName;
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
	public String getFileName(){
		return fileName;
	}
	public void setFileName(String fileName){
		this.fileName = fileName;
	}
	public double getTagPercent(){
		return tagPercent;
	}
	public void setTagPercent(double tagPercent){
		this.tagPercent = tagPercent;
	}
	@Override
	public String toString() {
		return "Road [x=" + x + ", y=" + y + ", tagName=" + tagName + ", fileName=" + fileName + ", tagPercent=" + tagPercent + "]";
	}
	
}
