/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.cognitiveservices.vision.customvision.samples;
import java.io.*;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.google.common.io.ByteStreams;

import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.Classifier;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.Domain;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.DomainType;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.ImageFileCreateBatch;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.ImageFileCreateEntry;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.Iteration;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.Project;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.Region;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.TrainProjectOptionalParameter;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.CustomVisionTrainingClient;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.Trainings;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.CustomVisionTrainingManager;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.ImagePrediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.models.Prediction;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionClient;
import com.microsoft.azure.cognitiveservices.vision.customvision.prediction.CustomVisionPredictionManager;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.Tag;

import java.util.ArrayList;
import java.util.List;
import com.microsoft.azure.cognitiveservices.vision.customvision.samples.dao.RoadDao;
import com.microsoft.azure.cognitiveservices.vision.customvision.samples.dto.Road;
import com.microsoft.azure.cognitiveservices.vision.customvision.training.models.*;
import java.lang.Object;

public class CustomVisionSamples {
    /**
     * Main entry point.
     * @param trainer the Custom Vision Training client object
     * @param predictor the Custom Vision Prediction client object
     */


    // 일단 이 부분이 실행된다.
    public static void runSample(CustomVisionTrainingClient trainer, CustomVisionPredictionClient predictor, int jobNumber) {
        try {
            // This demonstrates how to create an image classification project, upload images,
            // train it and make a prediction.
            ImageClassification_Sample(trainer, predictor, jobNumber);

            // This demonstrates how to create an object detection project, upload images,
            // train it and make a prediction.
            //ObjectDetection_Sample(trainer, predictor);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
    
    // 이 부분이 해당 리소스에 대한 프로젝트만들기 + 학습 + 예측부분이다.
    // 프로젝트는 뭐로 학습을 시키냐에 따라 달라질수있으며, 무료버전은 2개만 생성가능한것같다.
    // 학습을 시키고 예측을 할 때, 모든 태그에 대한 예측을 한다.
    // 또한, 기본적으로 폴더이름과 파일 이름을 알아야만 예측이 가능한 것 같다.
    public static void ImageClassification_Sample(CustomVisionTrainingClient trainClient, CustomVisionPredictionClient predictor, int jobNumber) {
        try {
            // <snippet_create>
	    // 프로젝트를 생성하는 부분이다.
            //System.out.println("ImageClassification Sample");
            Trainings trainer = trainClient.trainings();

            //System.out.println("Creating project...");
            //Project project = trainer.createProject()
            //    .withName("Sample Java Project")
            //    .execute();
            // </snippet_create>
	    // 현재 만들어진 Project가 없으면 만들고, 있으면
	    //현재 존재하는 project중에 이름이 Sample Java Project인 프로젝트를 가져온다.
	    List<Project> projectlist = trainer.getProjects();
	    Project project = null;
	    if(projectlist.size() == 0){
		    System.out.println("Creating project...");
		    project = trainer.createProject()
			    .withName("Sample Java Project")
			    .execute();
	    }
	    else{
		    for(int i=0;i<projectlist.size();i++){
			    project = projectlist.get(i);
			    if(project.name().equals("Sample Java Project")){
				    break;
			    }
		    }
	    }

	    // 가져온 Project의 Tag들을 가져온다. porthole tag와 crack tag를 가져왔다.
	    // 없으면 만든다.
	    List<Tag> taglist = trainer.getTags(project.id(), null);
	    Tag portholeTag = null;
	    Tag crackTag = null;
	    if(taglist.size() == 0){
		    portholeTag = trainer.createTag()
			    .withProjectId(project.id())
			    .withName("porthole")
			    .execute();
		    crackTag = trainer.createTag()
			    .withProjectId(project.id())
			    .withName("crack")
			    .execute();
	    }
	    else{
		    for(int i=0;i<taglist.size();i++){
			    portholeTag = taglist.get(i);
			    if(portholeTag.name().equals("porthole")){
				    break;
			    }
		    }
		    for(int i=0;i<taglist.size();i++){
			    crackTag = taglist.get(i);
			    if(crackTag.name().equals("crack")){
				    break;
			    }
		    }
	    }
            // <snippet_tags>
	    // 태그를 생성하는 부분
            // create porthole tag
            //Tag hemlockTag = trainer.createTag()
            //    .withProjectId(/*project.id()*/projectid)
            //    .withName("porthole")
            //    .execute();
            // create crack tag
            //Tag cherryTag = trainer.createTag()
            //    .withProjectId(/*project.id()*/projectid)
            //    .withName("crack")
            //    .execute();
            // </snippet_tags>

            // <snippet_upload
	    // 학습할 이미지를 업로드 하는 부분
	    // 원하는 태그와 이미지를 엮는다.
	    String publishedModelName = "myModel";
	    String predictionResourceId = System.getenv("AZURE_CUSTOMVISION_PREDICTION_ID");
	    if(jobNumber==0){	// Project에 이미지들 추가 + 트레이닝
	            System.out.println("Adding images...");
	
		    File portholePath = new File("/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/porthole");
	            String portholeDir = "/porthole";
	            String portholeList[] = portholePath.list(null);
	            if(portholeList == null){
	                    System.out.print("읽을 파일이 없습니다.");
	                    System.exit(0);
	            }
	            int cntFiles = portholeList.length;

	            for (int i = 0; i < cntFiles; i++) {
	                String fileName = portholeList[i];
	                byte[] contents = GetImage(portholeDir, fileName);
	                AddImageToProject(trainer, project, fileName, contents, portholeTag.id(), null);
	            }

		    File crackPath = new File("/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/crack");
		    String crackDir = "/crack";
		    String crackList[] = crackPath.list(null);
		    if(crackList == null){
			    System.out.print("일을 파일이 없습니다.");
			    System.exit(0);
		    }
		    int cntFiles2 = crackList.length;

	            for (int i = 0; i < cntFiles2; i++) {
	                String fileName = crackList[i];
	                byte[] contents = GetImage(crackDir, fileName);
	                AddImageToProject(trainer, project, fileName, contents, crackTag.id(), null);
	            }
	    
	            // </snippet_upload>

	            // <snippet_train>
		    // 학습하는 부분
	            System.out.println("Training...");
		    // 과거에 만들어놨던 iteration을 지우고 새로 학습한 iteration을 publish한다.
		    // 과거에 만들어진 iteration에 추가로 training을 못시키는 것 같다.
		    List<Iteration> iterationlist = trainer.getIterations(project.id());
		    Iteration iteration = null;
		    if(iterationlist.size() > 0 && iterationlist.get(0).status().equals("Completed")){
			    Iteration past_iteration = iterationlist.get(0);
			    trainer.unpublishIteration(project.id(), past_iteration.id());
			    trainer.deleteIteration(project.id(), past_iteration.id());
			    iteration = trainer.trainProject(project.id(), new TrainProjectOptionalParameter());
		    }
		    else if(iterationlist.size() > 0 && iterationlist.get(0).status().equals("Training")){
			    iteration = iterationlist.get(0);
		    }
		    else if(iterationlist.size() == 0){
			    iteration = trainer.trainProject(project.id(), new TrainProjectOptionalParameter());
		    }
		    //Iteration iteration = past_iteration;
	            while (iteration.status().equals("Training"))
	            {
	                System.out.println("Training Status: "+ iteration.status());
	                Thread.sleep(1000);
	                iteration = trainer.getIteration(project.id(), iteration.id());
	            }
	            System.out.println("Training Status: "+ iteration.status());
	    
        	    // The iteration is now trained. Publish it to the prediction endpoint.
		    trainer.publishIteration(project.id(), iteration.id(), publishedModelName, predictionResourceId);
	    }
            // </snippet_train>

            // use below for url
            // String url = "some url";
            // ImagePrediction results = predictor.predictions().classifyImageUrl()
            //                         .withProjectId(project.id())
            //                         .withPublishedName(publishedModelName)
            //                         .withUrl(url)
            //                         .execute();

            // <snippet_predict>
	    // 예측하는 부분이다.
	    // 폴더명과 사진파일의 이름을 명시해줘야 예측이 가능하다.
	    // 밑의 예제는 Test폴더에서 test_image.jpg와 test_image2.jpg 단 두개의 사진을 예측한 것이다.
            // load test image
            // byte[] testImage = GetImage("/Test", "test_image.jpg");
	    // 여기서 폴더내의 모든 파일들을 읽어야함.
	    if(jobNumber==1){	// 예측만함
		    File path2 = new File("/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_images");
		    //File[] fileList = path.listFiles();
		    String dirName = "/Predict_images";
		    String fileList[] = path2.list(null);
		    if(fileList == null){
			    System.out.print("읽을 파일이 없습니다.");
			    System.exit(0);
		    }
		    int cntFiles3 = fileList.length;

		    ArrayList<byte[]> testImage = new ArrayList<byte[]>();
	    
		    for(int i=0;i<cntFiles3;i++){
			    byte[] temp = GetImage(dirName, fileList[i]);
			    testImage.add(temp);
		    }
		    //System.exit(0);
		    double x = 0.0;
		    double y = 0.0;
		    int Check1=0; int Check2=0;
		    for(int i=0;i<cntFiles3;i++){
			    x++;
			    y++;
			    Check1=0; Check2=0;
			    Prediction pr1 = null; Prediction pr2 = null;
			    ImagePrediction results = predictor.predictions().classifyImage()
				    .withProjectId(project.id())
				    .withPublishedName(publishedModelName)
				    .withImageData(testImage.get(i))
				    .execute();
			    for(int j=0;j<results.predictions().size();j++){
				    Prediction prediction = results.predictions().get(j);
				    if(prediction.tagName().equals("porthole")){
					    pr1 = prediction;
					    if(prediction.probability() >= 0.9){
						    Check1 = 1;
					    }
					    else{
						    RoadDao roadDao = new RoadDao();
						    Road road = roadDao.getRoad(x,y);
						    if(road != null){
							    if(road.getTagName().equals("porthole")){
								    roadDao.deleteRoad(x,y);
							    }
						    }
					    }
				    }

				    if(prediction.tagName().equals("crack")){
					    pr2 = prediction;
					    if(prediction.probability() >= 0.9){
						    Check2 = 1;
					    }
					    else{
						    RoadDao roadDao = new RoadDao();
						    Road road = roadDao.getRoad(x,y);
						    if(road != null){
							    if(road.getTagName().equals("crack")){
								    roadDao.deleteRoad(x,y);
							    }
						    }
					    }
				    }
			    }

			    if(Check1==1 && Check2==0){
				    RoadDao roadDao = new RoadDao();
				    Road road = roadDao.getRoad(x,y);
				    if(road == null){
					    roadDao.setRoad(x,y,pr1.probability(),pr1.tagName(),fileList[i]);
				    }
				    // 파일 옮기기 ( Predict_images -> Predict_porthole )
				    String oriFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_images/" + fileList[i];
				    String copyFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_porthole/" + fileList[i];
				    String copyFilePath2 = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/porthole/" + fileList[i];

				    File oriFile = new File(oriFilePath);
				    File copyFile = new File(copyFilePath);
				    File copyFile2 = new File(copyFilePath2);

				    FileInputStream fis = new FileInputStream(oriFile);
				    FileOutputStream fos = new FileOutputStream(copyFile);
				    FileOutputStream fos2 = new FileOutputStream(copyFile2);

				    int fileByte = 0;

				    while((fileByte = fis.read()) != -1){
					    fos.write(fileByte);
					    fos2.write(fileByte);
				    }
				    fis.close();
				    fos.close();
				    fos2.close();
				    oriFile.delete();
			    }
			    else if(Check1==0 && Check2==1){
				    RoadDao roadDao = new RoadDao();
				    Road road = roadDao.getRoad(x,y);
				    if(road == null){
					    roadDao.setRoad(x,y,pr2.probability(),pr2.tagName(),fileList[i]);
				    }
				    // 파일 옮기기 ( Predict_images -> Predict_crack )
				    String oriFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_images/" + fileList[i];
				    String copyFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_crack/" + fileList[i];

				    File oriFile = new File(oriFilePath);
				    File copyFile = new File(copyFilePath);

				    FileInputStream fis = new FileInputStream(oriFile);
				    FileOutputStream fos = new FileOutputStream(copyFile);

				    int fileByte = 0;

				    while((fileByte = fis.read()) != -1){
					    fos.write(fileByte);
				    }
				    fis.close();
				    fos.close();
				    oriFile.delete();
			    }
			    else if(Check1==1 && Check2==1){
				    if(pr1.probability() >= pr2.probability()){
					    RoadDao roadDao = new RoadDao();
					    Road road = roadDao.getRoad(x,y);
					    if(road == null){
						    roadDao.setRoad(x,y,pr1.probability(),pr1.tagName(),fileList[i]);
					    }
					    else{
						    roadDao.updateRoad(x,y,pr1.probability(),pr1.tagName(),fileList[i]);
					    }
					    // 파일 옮기기 ( Predict_images -> Predict_porthole )
					    String oriFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_images/" + fileList[i];
		                            String copyFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_porthole/" + fileList[i];

		                            File oriFile = new File(oriFilePath);
		                            File copyFile = new File(copyFilePath);

	        	                    FileInputStream fis = new FileInputStream(oriFile);
		                            FileOutputStream fos = new FileOutputStream(copyFile);

		                            int fileByte = 0;
	
		                            while((fileByte = fis.read()) != -1){
		                                    fos.write(fileByte);
		                            }
		                            fis.close();
		                            fos.close();
					    oriFile.delete();
				    }
				    else{
					    RoadDao roadDao = new RoadDao();
					    Road road = roadDao.getRoad(x,y);
					    if(road == null){
						    roadDao.setRoad(x,y,pr2.probability(),pr2.tagName(),fileList[i]);
					    }
					    else{
						    roadDao.updateRoad(x,y,pr2.probability(),pr2.tagName(),fileList[i]);
					    }
					    // 파일 옮기기 ( Predict_images -> Predict_crack )
					    String oriFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_images/" + fileList[i];
		                            String copyFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_crack/" + fileList[i];
	
		                            File oriFile = new File(oriFilePath);
		                            File copyFile = new File(copyFilePath);

		                            FileInputStream fis = new FileInputStream(oriFile);
		                            FileOutputStream fos = new FileOutputStream(copyFile);
	
		                            int fileByte = 0;

		                            while((fileByte = fis.read()) != -1){
		                                    fos.write(fileByte);
		                            }
		                            fis.close();
		                            fos.close();
					    oriFile.delete();
				    }
			    }
			    else{
				    // 파일 삭제
				    String oriFilePath = "/home/whitebox/cognitive-services-java-sdk-samples-master/Vision/CustomVision/src/main/resources/Predict_images/" + fileList[i];
	
	                            File oriFile = new File(oriFilePath);
				    oriFile.delete();
			    }
		    }
	    }

            // predict
            //ImagePrediction results = predictor.predictions().classifyImage()
            //    .withProjectId(project.id())
            //    .withPublishedName(publishedModelName)
            //    .withImageData(testImage)
            //    .execute();
	    //int i=1;
	    //System.out.println(i+"번 예측");
            //for (Prediction prediction: results.predictions())
            //{
            //    System.out.println(String.format("\t%s: %.2f%%", prediction.tagName(), prediction.probability() * 100.0f));
            //}

            // </snippet_predict>
	    // 예측이 끝나고 종료해줘야 Thread에러가 생기지 않는다.
	    System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public static void ObjectDetection_Sample(CustomVisionTrainingClient trainClient, CustomVisionPredictionClient predictor)
    {
        try {
            // <snippet_od_mapping>
            // Mapping of filenames to their respective regions in the image. The coordinates are specified
            // as left, top, width, height in normalized coordinates. I.e. (left is left in pixels / width in pixels)

            // This is a hardcoded mapping of the files we'll upload along with the bounding box of the object in the
            // image. The boudning box is specified as left, top, width, height in normalized coordinates.
            //  Normalized Left = Left / Width (in Pixels)
            //  Normalized Top = Top / Height (in Pixels)
            //  Normalized Bounding Box Width = (Right - Left) / Width (in Pixels)
            //  Normalized Bounding Box Height = (Bottom - Top) / Height (in Pixels)
            HashMap<String, double[]> regionMap = new HashMap<String, double[]>();
            regionMap.put("scissors_1.jpg", new double[] { 0.4007353, 0.194068655, 0.259803921, 0.6617647 });
            regionMap.put("scissors_2.jpg", new double[] { 0.426470578, 0.185898721, 0.172794119, 0.5539216 });
            regionMap.put("scissors_3.jpg", new double[] { 0.289215684, 0.259428144, 0.403186262, 0.421568632 });
            regionMap.put("scissors_4.jpg", new double[] { 0.343137264, 0.105833367, 0.332107842, 0.8055556 });
            regionMap.put("scissors_5.jpg", new double[] { 0.3125, 0.09766343, 0.435049027, 0.71405226 });
            regionMap.put("scissors_6.jpg", new double[] { 0.379901975, 0.24308826, 0.32107842, 0.5718954 });
            regionMap.put("scissors_7.jpg", new double[] { 0.341911763, 0.20714055, 0.3137255, 0.6356209 });
            regionMap.put("scissors_8.jpg", new double[] { 0.231617644, 0.08459154, 0.504901946, 0.8480392 });
            regionMap.put("scissors_9.jpg", new double[] { 0.170343131, 0.332957536, 0.767156839, 0.403594762 });
            regionMap.put("scissors_10.jpg", new double[] { 0.204656869, 0.120539248, 0.5245098, 0.743464053 });
            regionMap.put("scissors_11.jpg", new double[] { 0.05514706, 0.159754932, 0.799019635, 0.730392158 });
            regionMap.put("scissors_12.jpg", new double[] { 0.265931368, 0.169558853, 0.5061275, 0.606209159 });
            regionMap.put("scissors_13.jpg", new double[] { 0.241421565, 0.184264734, 0.448529422, 0.6830065 });
            regionMap.put("scissors_14.jpg", new double[] { 0.05759804, 0.05027781, 0.75, 0.882352948 });
            regionMap.put("scissors_15.jpg", new double[] { 0.191176474, 0.169558853, 0.6936275, 0.6748366 });
            regionMap.put("scissors_16.jpg", new double[] { 0.1004902, 0.279036, 0.6911765, 0.477124184 });
            regionMap.put("scissors_17.jpg", new double[] { 0.2720588, 0.131977156, 0.4987745, 0.6911765 });
            regionMap.put("scissors_18.jpg", new double[] { 0.180147052, 0.112369314, 0.6262255, 0.6666667 });
            regionMap.put("scissors_19.jpg", new double[] { 0.333333343, 0.0274019931, 0.443627447, 0.852941155 });
            regionMap.put("scissors_20.jpg", new double[] { 0.158088237, 0.04047389, 0.6691176, 0.843137264 });
            regionMap.put("fork_1.jpg", new double[] { 0.145833328, 0.3509314, 0.5894608, 0.238562092 });
            regionMap.put("fork_2.jpg", new double[] { 0.294117659, 0.216944471, 0.534313738, 0.5980392 });
            regionMap.put("fork_3.jpg", new double[] { 0.09191177, 0.0682516545, 0.757352948, 0.6143791 });
            regionMap.put("fork_4.jpg", new double[] { 0.254901975, 0.185898721, 0.5232843, 0.594771266 });
            regionMap.put("fork_5.jpg", new double[] { 0.2365196, 0.128709182, 0.5845588, 0.71405226 });
            regionMap.put("fork_6.jpg", new double[] { 0.115196079, 0.133611143, 0.676470637, 0.6993464 });
            regionMap.put("fork_7.jpg", new double[] { 0.164215669, 0.31008172, 0.767156839, 0.410130739 });
            regionMap.put("fork_8.jpg", new double[] { 0.118872553, 0.318251669, 0.817401946, 0.225490168 });
            regionMap.put("fork_9.jpg", new double[] { 0.18259804, 0.2136765, 0.6335784, 0.643790841 });
            regionMap.put("fork_10.jpg", new double[] { 0.05269608, 0.282303959, 0.8088235, 0.452614367 });
            regionMap.put("fork_11.jpg", new double[] { 0.05759804, 0.0894935, 0.9007353, 0.3251634 });
            regionMap.put("fork_12.jpg", new double[] { 0.3345588, 0.07315363, 0.375, 0.9150327 });
            regionMap.put("fork_13.jpg", new double[] { 0.269607842, 0.194068655, 0.4093137, 0.6732026 });
            regionMap.put("fork_14.jpg", new double[] { 0.143382356, 0.218578458, 0.7977941, 0.295751631 });
            regionMap.put("fork_15.jpg", new double[] { 0.19240196, 0.0633497, 0.5710784, 0.8398692 });
            regionMap.put("fork_16.jpg", new double[] { 0.140931368, 0.480016381, 0.6838235, 0.240196079 });
            regionMap.put("fork_17.jpg", new double[] { 0.305147052, 0.2512582, 0.4791667, 0.5408496 });
            regionMap.put("fork_18.jpg", new double[] { 0.234068632, 0.445702642, 0.6127451, 0.344771236 });
            regionMap.put("fork_19.jpg", new double[] { 0.219362751, 0.141781077, 0.5919118, 0.6683006 });
            regionMap.put("fork_20.jpg", new double[] { 0.180147052, 0.239820287, 0.6887255, 0.235294119 });
            // </snippet_od_mapping>

            System.out.println("Object Detection Sample");
            Trainings trainer = trainClient.trainings();

            // find the object detection domain to set the project type
            Domain objectDetectionDomain = null;
            List<Domain> domains = trainer.getDomains();
            for (final Domain domain : domains) {
                if (domain.type() == DomainType.OBJECT_DETECTION) {
                    objectDetectionDomain = domain;
                    break;
                }
            }

            if (objectDetectionDomain == null) {
                System.out.println("Unexpected result; no objects were detected.");
                return;
            }

            // <snippet_create_od>
            System.out.println("Creating project...");
            // create an object detection project
            Project project = trainer.createProject()
                .withName("Sample Java OD Project")
                .withDescription("Sample OD Project")
                .withDomainId(objectDetectionDomain.id())
                .withClassificationType(Classifier.MULTILABEL.toString())
                .execute();
            // </snippet_create_od>

            // <snippet_tags_od>
            // create fork tag
            Tag forkTag = trainer.createTag()
                .withProjectId(project.id())
                .withName("fork")
                .execute();

            // create scissors tag
            Tag scissorsTag = trainer.createTag()
                .withProjectId(project.id())
                .withName("scissor")
                .execute();
            // </snippet_tags_od>

            // <snippet_upload_od>
            System.out.println("Adding images...");
            for (int i = 1; i <= 20; i++) {
                String fileName = "fork_" + i + ".jpg";
                byte[] contents = GetImage("/fork", fileName);
                AddImageToProject(trainer, project, fileName, contents, forkTag.id(), regionMap.get(fileName));
            }

            for (int i = 1; i <= 20; i++) {
                String fileName = "scissors_" + i + ".jpg";
                byte[] contents = GetImage("/scissors", fileName);
                AddImageToProject(trainer, project, fileName, contents, scissorsTag.id(), regionMap.get(fileName));
            }
            // </snippet_upload_od>

            // <snippet_train_od>
            System.out.println("Training...");
            Iteration iteration = trainer.trainProject(project.id(), new TrainProjectOptionalParameter());

            while (iteration.status().equals("Training"))
            {
                System.out.println("Training Status: "+ iteration.status());
                Thread.sleep(5000);
                iteration = trainer.getIteration(project.id(), iteration.id());
            }
            System.out.println("Training Status: "+ iteration.status());

            // The iteration is now trained. Publish it to the prediction endpoint.
            String publishedModelName = "myModel";
            String predictionResourceId = System.getenv("AZURE_CUSTOMVISION_PREDICTION_ID");
            trainer.publishIteration(project.id(), iteration.id(), publishedModelName, predictionResourceId);
            // </snippet_train_od>

            // use below for url
            // String url = "some url";
            // ImagePrediction results = predictor.predictions().detectImageUrl()
            //                         .withProjectId(project.id())
            //                         .withPublishedName(publishedModelName)
            //                         .withUrl(url)
            //                         .execute();

            // <snippet_prediction_od>
            // load test image
            byte[] testImage = GetImage("/ObjectTest", "test_image.jpg");

            // predict
            ImagePrediction results = predictor.predictions().detectImage()
                .withProjectId(project.id())
                .withPublishedName(publishedModelName)
                .withImageData(testImage)
                .execute();

            for (Prediction prediction: results.predictions())
            {
                System.out.println(String.format("\t%s: %.2f%% at: %.2f, %.2f, %.2f, %.2f",
                    prediction.tagName(),
                    prediction.probability() * 100.0f,
                    prediction.boundingBox().left(),
                    prediction.boundingBox().top(),
                    prediction.boundingBox().width(),
                    prediction.boundingBox().height()
                ));
            }
            // </snippet_prediction_od>
	    System.exit(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    // <snippet_helpers>
    private static void AddImageToProject(Trainings trainer, Project project, String fileName, byte[] contents, UUID tag, double[] regionValues)
    {
        System.out.println("Adding image: " + fileName);
        ImageFileCreateEntry file = new ImageFileCreateEntry()
            .withName(fileName)
            .withContents(contents);

        ImageFileCreateBatch batch = new ImageFileCreateBatch()
            .withImages(Collections.singletonList(file));

        // If Optional region is specified, tack it on and place the tag there, otherwise
        // add it to the batch.
        if (regionValues != null)
        {
            Region region = new Region()
                .withTagId(tag)
                .withLeft(regionValues[0])
                .withTop(regionValues[1])
                .withWidth(regionValues[2])
                .withHeight(regionValues[3]);
            file = file.withRegions(Collections.singletonList(region));
        } else {
            batch = batch.withTagIds(Collections.singletonList(tag));
        }

        trainer.createImagesFromFiles(project.id(), batch);
    }

    private static byte[] GetImage(String folder, String fileName)
    {
        try {
            return ByteStreams.toByteArray(CustomVisionSamples.class.getResourceAsStream(folder + "/" + fileName));
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    // </snippet_helpers>

    /**
     * Main entry point.
     *
     * @param args the parameters
     */
    public static void main(String[] args) {
        try {
            //=============================================================
            // Authenticate

            final String CustomVisionTrainingClientKey = System.getenv("AZURE_CUSTOMVISION_TRAINING_API_KEY");
            final String predictionApiKey = System.getenv("AZURE_CUSTOMVISION_PREDICTION_API_KEY");

            final String Endpoint = System.getenv("AZURE_CUSTOMVISION_ENDPOINT");

            CustomVisionTrainingClient trainClient = CustomVisionTrainingManager.authenticate("https://{Endpoint}/customvision/v3.0/training/", CustomVisionTrainingClientKey).withEndpoint(Endpoint);
            CustomVisionPredictionClient predictClient = CustomVisionPredictionManager.authenticate("https://{Endpoint}/customvision/v3.0/prediction/", predictionApiKey).withEndpoint(Endpoint);
	    
            runSample(trainClient, predictClient, 1);	// jobNumber = 0 -> 프로젝트에 이미지 추가하고 트레이닝, jobNumber = 1 -> 예측만
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }
}
