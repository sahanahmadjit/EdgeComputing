package clientencryptedsearch.main;

import clientencryptedsearch.utilities.Constants;
import edu.cmu.lti.lexical_db.ILexicalDatabase;
import edu.cmu.lti.lexical_db.NictWordNet;
import edu.cmu.lti.ws4j.impl.WuPalmer;
import edu.cmu.lti.ws4j.util.WS4JConfiguration;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Line2D;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class WordRepresentation extends JPanel {



    private static double getRandomNumberInRange(int min, int max) {


        int randomNumber, difference;
        difference = max-min;

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        randomNumber = r.nextInt((max - min) + 1) + min;


        return randomNumber;
    }


    public static double getRandomNumber(){
        double x = Math.random();
        return x;
    }



    public static double getRandomDoubleBetweenRange(double min, double max){
        double x = (Math.random()*((max-min)+1))+min;
        return x;
    }

    public  void readAbstractContent(){

        HashMap<String, ArrayList<Double>> abstractPointLocation = new HashMap<String, ArrayList<Double>>();

        BufferedReader br,brPoint;
        try {
            File abstractCandiateFile = new File(Constants.markovAbstractLocation + File.separator + "Abstract_3");

            br = new BufferedReader(new FileReader(abstractCandiateFile));

            String st;



            while ((st = br.readLine()) != null) {

                if(st.length()==1){// first Line is cluster Number
                    continue;
                }

                else {
                    double x = getRandomDoubleBetweenRange(50,400);
                    double y = getRandomDoubleBetweenRange(50,400);

                    if(x<49 || y>401)
                    {
                        System.out.println("out of bound RandomNumberGenerator");
                    }
                    ArrayList<Double> pointList = new ArrayList<Double>();
                    pointList.add(x);
                    pointList.add(y);
                    abstractPointLocation.put(st,pointList);

                }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



        try {

            File file;
            file =new File(Constants.pointRecordFileLocation+ File.separator+ Constants.abractPointFileName);
                BufferedWriter bw;
                bw = new BufferedWriter(new FileWriter(file));

                for(String term:abstractPointLocation.keySet()){
                    ArrayList<Double> pointList = new ArrayList<Double>();
                    pointList = abstractPointLocation.get(term);
                    bw.write(term);
                    bw.write("|.|");
                    bw.write(String.valueOf(pointList.get(0)));
                    bw.write("|.|");
                    bw.write(String.valueOf(pointList.get(1)));
                    bw.write("\n");
                }
            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find point location file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't read can't write point location in File");
        }

      //  readSearchContent();
       // return abstractPointLocation;
    }



    public void readSearchContent(){

        HashMap<String, ArrayList<Double>> searchWordPointLocation = new HashMap<String, ArrayList<Double>>();
        HashMap<String, ArrayList<Double>> abstractPointLocation = new HashMap<String, ArrayList<Double>>();
        ArrayList<Double> pointList = new ArrayList<Double>();
        double [] pointArr = new double[2];


        BufferedReader br,brPoint;



        try {
            File clusterDataFile = new File(Constants.markovDataFileLocation + File.separator + "cluster_3");
            File abstractPointFile = new File (Constants.pointRecordFileLocation+File.separator+Constants.abractPointFileName);
            br = new BufferedReader(new FileReader(clusterDataFile));
            brPoint = new BufferedReader( new FileReader(abstractPointFile));


            String st;


            while ((st = brPoint.readLine())!=null){
                pointList = new ArrayList<Double>();
                String[] stArr = st.split("\\|.\\|");
                pointList.add(Double.valueOf(stArr[1]));
                pointList.add(Double.valueOf(stArr[2]));
                abstractPointLocation.put(stArr[0],pointList);
            }
                brPoint.close();

            while ((st = br.readLine()) != null) {

                if(st.length()==1){// first Line is cluster Number
                    continue;
                }

                else {
                       double maximumDistance = -1.0; String closestAbstract="";

                       for(String term:abstractPointLocation.keySet()){
                           double distance;
                           distance= computeWUP(term,st);
                           if(distance <=0) {
                               distance = -1;
                           }
                           else if (distance >1){
                               distance =1;
                           }


                        if(maximumDistance<= distance){
                            maximumDistance = distance;
                            closestAbstract = term;
                        }

                    }
                      ArrayList<Double>   searchWordPointList = new ArrayList<Double>();
                       searchWordPointList = abstractPointLocation.get(closestAbstract);

                       if(searchWordPointList==null){
                           break;
                       }
                       double min = -10, max =10 , randomX,randomY,x1,y1,x,y;


                    x1 = searchWordPointList.get(0).doubleValue();
                    y1 = searchWordPointList.get(1).doubleValue();


                    if (x1 <49  || x1 > 500) {
                        System.out.println("out of bound: TWO");
                    }


                    randomX = getRandomDoubleBetweenRange(min,max);
                    randomY= getRandomDoubleBetweenRange(min,max);


                    if (randomX <min-1  || randomX > max+1) {
                        System.out.println("out of bound: ONE");
                    }


                            x = x1 + maximumDistance + randomX; //+ randomX;
                            y = y1 + maximumDistance + randomY;// + randomY;

                    if (x <48  || x > 500) {
                        System.out.println("out of bound: THREE");
                    }

                       /*    x = x+ randomX;
                           y = y+ randomY;

                    if (x <30  || y > 500) {
                        System.out.println("out of bound: FOUR");
                    }
*/

                         // searchWordPointList.clear();
                          ArrayList<Double> tempList = new ArrayList<Double>();
                          tempList.add(x);
                          tempList.add(y);
                          searchWordPointLocation.put(st,tempList);
                       //   searchWordPointList.clear();

                      //  }
                    }

            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }




        try {

            File file;
            file =new File(Constants.pointRecordFileLocation+ File.separator+ Constants.searchPointFileName);
            BufferedWriter bw;
            bw = new BufferedWriter(new FileWriter(file));

            for(String term:searchWordPointLocation.keySet()){
                pointList = new ArrayList<Double>();
                pointList = searchWordPointLocation.get(term);
                bw.write(term);
                bw.write("|.|");
                bw.write(String.valueOf(pointList.get(0)));
                bw.write("|.|");
                bw.write(String.valueOf(pointList.get(1)));
                bw.write("\n");
            }
            bw.flush();
            bw.close();

        } catch (FileNotFoundException e) {
            System.out.print(this.getClass().getName() + "Can't find search Word point location file in location");
        } catch (IOException e) {
            System.out.print(this.getClass().getName() + " Can't  write search word point location in File");
        }



    }



    public  void paintComponent(Graphics graphics){
        super.paintComponent(graphics);
        HashMap<String, ArrayList<Double>> abstractPointLocation = new HashMap<String, ArrayList<Double>>();
        HashMap<String, ArrayList<Double>> searchWordPointLocation = new HashMap<String, ArrayList<Double>>();


        try{
            File abstractPointFile = new File (Constants.pointRecordFileLocation+File.separator+Constants.abractPointFileName);
            File clusterDataPointFile = new File (Constants.pointRecordFileLocation+File.separator+Constants.searchPointFileName);

            BufferedReader brClusterPoint,brAbstractPoint;


            brClusterPoint = new BufferedReader(new FileReader(clusterDataPointFile));
            brAbstractPoint = new BufferedReader( new FileReader(abstractPointFile));


            String st;
            while ((st = brAbstractPoint.readLine())!=null){
                ArrayList<Double> pointList = new ArrayList<Double>();
                String[] stArr = st.split("\\|.\\|");
                pointList.add(Double.valueOf(stArr[1]));
                pointList.add(Double.valueOf(stArr[2]));
                abstractPointLocation.put(stArr[0],pointList);
            }

            while ((st = brClusterPoint.readLine())!=null){
                ArrayList<Double> pointList = new ArrayList<Double>();
                String[] stArr = st.split("\\|.\\|");
                pointList.add(Double.valueOf(stArr[1]));
                pointList.add(Double.valueOf(stArr[2]));
                searchWordPointLocation.put(stArr[0],pointList);
            }

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(abstractPointLocation.size());

        Graphics2D graphics2D = (Graphics2D) graphics;
        graphics2D.setColor(Color.RED);

       for(String term:abstractPointLocation.keySet()){
            ArrayList <Double> pointList = abstractPointLocation.get(term);
            double x = pointList.get(0).doubleValue(); //x cordinate
            double y = pointList.get(1).doubleValue(); //y cordinate

           if (x <30  || y > 500) {
               System.out.println("out of bound: FOUR");
           }

            Shape shapePoint = new Line2D.Double(x,y,x+4,y);
            graphics2D.draw(shapePoint);
            Shape shapePoint2 = new Line2D.Double(x+2,y+2,x+2,y-2);
            graphics2D.draw(shapePoint2);
        }

        graphics2D.setColor(Color.BLUE);



        System.out.println(searchWordPointLocation.size());
        for(String term:searchWordPointLocation.keySet()) {
            ArrayList<Double> pointList = searchWordPointLocation.get(term);
            double x = pointList.get(0); //x cordinate
            double y = pointList.get(1); //y cordinate

            Shape shapePoint = new Line2D.Double(x, y, x+1 , y);
            graphics2D.draw(shapePoint);
        //    Shape shapePoint2 = new Line2D.Double(x + 2, y + 2, x + 2, y - 2);
          //  graphics2D.draw(shapePoint2);
        }

    }


    public  void drawingTest(){

        readAbstractContent();
        readSearchContent();
        WordRepresentation wordRepresentObj = new WordRepresentation();
        JFrame jf = new JFrame();
        jf.setTitle("Samplying Quality Testing");
        jf.setSize(6000,6000);
        jf.setVisible(true);
        jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        jf.add(wordRepresentObj);


    }



    private static ILexicalDatabase db = new NictWordNet();
    private double computeWUP (String word1, String word2) {
        WS4JConfiguration.getInstance().setMFS(true);
        double s = new WuPalmer(db).calcRelatednessOfWords(word1, word2);
        return s;
    }


}
