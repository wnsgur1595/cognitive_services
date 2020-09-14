package com.microsoft.azure.cognitiveservices.vision.customvision.samples.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.microsoft.azure.cognitiveservices.vision.customvision.samples.dto.Road;

public class RoadDao {
	private static String dbUrl = "jdbc:mysql://localhost:3306/Road?serverTimezone=Asia/Seoul&useSSL=false";
	private static String dbUser = "connectuser";
	private static String dbPasswd = "connect123!@#";
	
	
	public List<Road> getAllRoad() {	// 모든 도로 정보를 얻어오는 함수
		List<Road> roadlist = new ArrayList<Road>();
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String sql = "SELECT * FROM roadtable";
		
		try(Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
				PreparedStatement ps = conn.prepareStatement(sql)){
			
			try(ResultSet rs = ps.executeQuery()){
				while(rs.next()) {
					double x = rs.getDouble("x");
					double y = rs.getDouble("y");
					String tagName = rs.getString("tagName");
					
					Road road = new Road(x, y, tagName);
					roadlist.add(road);
				}
			}catch(Exception e) {
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return roadlist;
	}

	public Road getRoad(double x1, double y1){
		Road road = null;
		try{
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e){
			e.printStackTrace();
		}

		String sql = "SELECT * FROM roadtable WHERE (?=x AND ?=y)";

		try(Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
				PreparedStatement ps = conn.prepareStatement(sql)){

			try(ResultSet rs = ps.executeQuery()){
				if(rs.next()){
					double x2 = rs.getDouble("x");
					double y2 = rs.getDouble("y");
					String tagName = rs.getString("tagName");
					road = new Road(x2, y2, tagName);
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return road;
	}
	
	public int setRoad(double x, double y, String tagName) {
		int Count = 0;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String sql = "INSERT INTO roadtable (x, y, tagName) VALUES(?, ?, ?)";
		
		try(Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
				PreparedStatement ps = conn.prepareStatement(sql)){
			
			ps.setDouble(1, x);
			ps.setDouble(2, y);
			ps.setString(3, tagName);
			
			Count = ps.executeUpdate();
			
		}catch(Exception e){
			e.printStackTrace();
		}
		
		return Count;
	}

	public int setRoadTagName(double x, double y, String tagName) {
		int Count = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}

		String sql = "UPDATE roadtable SET tagName = ? WHERE (x = ? AND y = ?)";

		try(Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
				PreparedStatement ps = conn.prepareStatement(sql)){

			ps.setString(1, tagName);
			ps.setDouble(2, x);
			ps.setDouble(3, y);

			Count = ps.executeUpdate();

		}catch(Exception e){
			e.printStackTrace();
		}
		return Count;
	}

	public int deleteRoad(double x, double y) {
		int Count = 0;
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}

		String sql = "DELETE FROM roadtable WHERE (?=x AND ?=y)";

		try(Connection conn = DriverManager.getConnection(dbUrl, dbUser, dbPasswd);
				PreparedStatement ps = conn.prepareStatement(sql)){

			ps.setDouble(1, x);
			ps.setDouble(2, y);

			Count = ps.executeUpdate();

		}catch(Exception e){
			e.printStackTrace();
		}
		return Count;
	}
}

