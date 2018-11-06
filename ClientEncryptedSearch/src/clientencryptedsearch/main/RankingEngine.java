package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;

public class RankingEngine {


 public  double  getSemanticDistanceRadiusForTerm(String clusterNumber){

     double avgSimDistance,userInterest,clusterSize,semanticRadius;

     CalculateAverageSimilarityDistance avgSimObj = new CalculateAverageSimilarityDistance();
     UserInterest usrInrstObj = new UserInterest();
     ClusterSizeInfo clsSizeObj = new ClusterSizeInfo();

     avgSimDistance =avgSimObj.getAVGSimilartiyDistanceOfCluster(clusterNumber);
     userInterest = usrInrstObj.getUserIntersetSearchCalculation(Integer.valueOf(clusterNumber), Constants.TOTAL_NUMBER_OF_CLUSTER);
     clusterSize = clsSizeObj.getClusterSizeInfo(clusterNumber);


     semanticRadius = avgSimDistance + userInterest +clusterSize;
 return  semanticRadius;



 }



}
