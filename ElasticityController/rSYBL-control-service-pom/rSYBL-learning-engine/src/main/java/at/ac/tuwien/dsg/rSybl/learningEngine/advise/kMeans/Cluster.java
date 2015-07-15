/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package at.ac.tuwien.dsg.rSybl.learningEngine.advise.kMeans;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Georgiana
 */
public class Cluster  {

    private ArrayList<NDimensionalPoint> points = new ArrayList<NDimensionalPoint>();
    private NDimensionalPoint centroid;

    public Cluster() {
    }

    public Cluster(ArrayList<NDimensionalPoint> points, NDimensionalPoint centroid) {
        this.points = points;
        this.centroid = centroid;
    }

    /**
     * @return the points
     */
    public ArrayList<NDimensionalPoint> getPoints() {
        return points;
    }

    /**
     * @param points the points to set
     */
    public void setPoints(ArrayList<NDimensionalPoint> points) {
        this.points = points;
    }

    /**
     * @return the centroid
     */
    public NDimensionalPoint getCentroid() {
        return centroid;
    }
    public NDimensionalPoint getClosestPoint(NDimensionalPoint point){
        double minDist = Double.MAX_VALUE;
        NDimensionalPoint result = null;
        for (NDimensionalPoint p:points){
            if (minDist>p.computeDistance(point, point.getValues().size())){
                minDist = p.computeDistance(point, point.getValues().size());
                result = p;
            }
        }
        return result;
    }
    /**
     * @param centroid the centroid to set
     */
    public void setCentroid(NDimensionalPoint centroid) {
        this.centroid = centroid;
    }

    public NDimensionalPoint computeCentroidAsAverage() {
        NDimensionalPoint center = new NDimensionalPoint();
        if (points != null && points.size() > 0) {
           // center.setSize(points.get(0).getValues().size());
            double max = 0;
            LinkedList<Double> myPoints = points.get(0).getValues();
            for (NDimensionalPoint point :points){
                if (point.getValues().size()>max){
                    myPoints=point.getValues();
                    max=point.getValues().size();
                }
            }
     
            for (int i=0;i<max;i++){
                myPoints.set(i, 0.0);
            }
            int nb = 0;
            for (int x = 0; x < points.size(); x++) {
                if (points.get(x).getValues().size()==points.get(0).getValues().size()){
                for (int i = 0; i < points.get(0).getValues().size(); i++) {
                    if (!points.get(x).getValues().get(i).equals(Double.NaN) || !points.get(x).getValues().get(i).equals(Double.NEGATIVE_INFINITY) ){
                    myPoints.set(i,  myPoints.get(i) + points.get(x).getValues().get(i));
                }
                }
                nb++;
                }
            }
            for (int i = 0; i < points.get(0).getValues().size(); i++) {
                myPoints.set(i, myPoints.get(i) / nb);
            }
            
            center.setValues(myPoints);
            centroid=center;
            return centroid;
        } else {
            return null;
        }

    }

    public void addPoint(NDimensionalPoint dimensionalPoint) {
        points.add(dimensionalPoint);
    }
//     def update(self, points):
//        old_centroid = self.centroid
//        self.points = points
//        self.centroid = self.calculateCentroid()
//        return getDistance(old_centroid, self.centroid)

    public double update(ArrayList<NDimensionalPoint> newPoints) {
        if (centroid != null) {
            try {
                NDimensionalPoint oldCentroid = (NDimensionalPoint) centroid.clone();
                this.points = newPoints;
                this.centroid = computeCentroidAsAverage();
                if (centroid!=null){
                return centroid.computeDistance(oldCentroid);
                }else{
                    if (centroid==null && oldCentroid==null){
                        return 0.0;}
                        else{
                                        return NDimensionalPoint.MAX_DIST;
                    }
                }
            } catch (CloneNotSupportedException ex) {
                Logger.getLogger(Cluster.class.getName()).log(Level.SEVERE, null, ex);
                return NDimensionalPoint.MAX_DIST;
            }

        } else {
            
            centroid = computeCentroidAsAverage();
            this.points = newPoints;
            if (centroid==null){
                return 0.0;
            }else{
            return NDimensionalPoint.MAX_DIST;
            }
        }
    }
    public NDimensionalPoint computeCentroidWithSmallestDistance() {
        NDimensionalPoint center = points.get(0);
        double minDist = NDimensionalPoint.MAX_DIST;
        for (NDimensionalPoint point : points) {
            double totalDist = 0.0;
            for (NDimensionalPoint toCheck : points) {
                totalDist += point.computeDistance(toCheck);
            }
            if (totalDist < minDist) {
                minDist = totalDist;
                center = point;
            }
        }
        return center;
    }
}
