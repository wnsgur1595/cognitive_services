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
	
	
	public List<Road> getRoad() {	// 모든 도로 정보를 얻어오는 함수
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
					int number = rs.getInt("number");
					
					Road road = new Road(x, y, number);
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
	
	public int setRoad(double x, double y) {
		int Count = 0;
		
		try {
			Class.forName("com.mysql.jdbc.Driver");
		}catch(ClassNotFoundException e) {
			e.printStackTrace();
		}
		
		String sql = "INSERT INTO roadtable (x, y) VALUES(?, ?)";
		
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

