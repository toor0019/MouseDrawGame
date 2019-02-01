package trail;


import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import javafx.animation.Animation;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import math.Vector;

public class AnimatorSkeleton extends AnimationTimer implements DrawInterface{

	private static final double ONE_SECOND = 1000000000L;
	private static final double HALF_SECOND = ONE_SECOND / 2F;
	private static final int MAX_VECTORS = 360;
	private final Random RAND = new Random();

	private Vector prependicularCenterX;
	private Vector prependicularCenterY;
	private Vector middle;
	private LinkedList< Vector> trailVectors;
	private GraphicsContext gc;
	private Vector lastVector;

	private String fpsDisplay;
	private Canvas canvas;
	private boolean clean;
	private int frameCount;
	private double hue;
	private double hueShift;
	private double lastTime;
	
	public AnimatorSkeleton( Canvas canvas){
		this.canvas = canvas;
		gc = canvas.getGraphicsContext2D();
	}

	public void start(){
		if( trailVectors == null)
			trailVectors = new LinkedList<>();
		trailVectors.clear();
		frameCount = 0;
		clean = false;
		lastTime = System.nanoTime();
		super.start();
	}

	public void clean(){
		clean = true;
	}

	public void addStartVector( double x, double y){
		clean = false;
		lastVector = new Vector( x, y);
	trailVectors.add(lastVector);
	}

	public void addVector(double x,double y) {
		if(trailVectors.size()>=MAX_VECTORS) {
			removeFirst(1);
		}
		
		trailVectors.addLast(new Vector(x,y));
	}
	public void removeFirst(int count) {
		for(int i=0;i<count&&!(trailVectors.isEmpty());i++) {
			trailVectors.removeFirst();
		}
		
	}
	public double rand( double min, double max){
		return min + (max - min) * RAND.nextDouble();
	}

	public double roughRand( double min, double max, double minScale, double maxScale){
		if( min > max){
			double temp = min;
			min = max;
			max = temp;
		}
		return rand( min * rand( minScale, maxScale), max * rand( minScale, maxScale));
	}

	public void clearCanvas(){
		gc.clearRect( 0, 0, canvas.getWidth(), canvas.getHeight());
	}

	public void handle(long now) {
		if(lastVector!=null) {
			calculateFPS(now);
			clearCanvas();
			shortenTrail(5);
			drawingLoop(trailVectors,(m,px,py)->draw(middle,prependicularCenterX,prependicularCenterY));
			resetColorAndVector();
			drawingLoop(trailVectors, (m,px,py)->draw(middle,prependicularCenterX,prependicularCenterY));
			resetColorAndVector();
			displayFPS();
		}
	}
	
	public void resetColorAndVector() {
		if(!(trailVectors.isEmpty())) {
			lastVector=trailVectors.getFirst();
			
		}
	}
	
	public void calculateFPS(long now) {
		if(now-lastTime>=HALF_SECOND) {
			fpsDisplay=Integer.toString(frameCount*2);
			frameCount=0;
			lastTime=now;
		}
		frameCount++;
	}
	
	public void displayFPS(){
		gc.setFont( Font.font( gc.getFont().getFamily(), FontWeight.BLACK, 24));
		gc.setStroke( Color.WHITE);
		gc.setLineWidth( 1);
		gc.strokeText( fpsDisplay, 10, 25);
	}

	public void drawPerpendicularLines( Vector middle, Vector perpendicularCenterX, Vector perpendicularCenterY){
		gc.setLineWidth( 3);
		gc.setStroke( Color.hsb( (hue += .5f) % 360, 1, 1));
		gc.strokeLine( middle.getX(), middle.getY(), perpendicularCenterX.getX(), perpendicularCenterX.getY());
		gc.strokeLine( middle.getX(), middle.getY(), perpendicularCenterY.getX(), perpendicularCenterY.getY());
	}

	public void drawCloud( Vector middle, Vector perpendicularCenterX, Vector perpendicularCenterY){
		gc.setFill( Color.hsb( (hue += .75f) % 360, 1, .7));
		for( int i = 0; i < 100; i++){
			double x = roughRand( perpendicularCenterY.getX(), perpendicularCenterX.getX(), .95, 1.05);
			double y = roughRand( perpendicularCenterY.getY(), perpendicularCenterX.getY(), .95, 1.05);
			gc.fillOval( x, y, 2, 2);
		}
	}
	
	public void drawingLoop(List<Vector> trailVectors,DrawInterface drawInterface) {
		Vector vector=null;
		for(Vector currentVector: trailVectors) {
			vector=currentVector;
			
			Vector perpendicularY =currentVector.sub(lastVector).mult(4).perpendicularY();
			Vector perpendicularX =currentVector.sub(lastVector).mult(4).perpendicularX();
			middle=lastVector= lastVector.add(currentVector).mult(0.5);
			prependicularCenterX= perpendicularX.add(middle);
			prependicularCenterY=perpendicularY.add(middle);
			draw(currentVector, prependicularCenterX, prependicularCenterY);
			
		}
		lastVector=vector;
	}

	
	public void shortenTrail(int count) {
		
		if(clean&& !(trailVectors.isEmpty())) {
			removeFirst(count);
		}
	}
	@Override
	public void draw(Vector middle, Vector perpendicularCenterX, Vector perpendicularCenterY) {
		
		drawCloud(middle, perpendicularCenterX, perpendicularCenterY);
		drawPerpendicularLines(middle, perpendicularCenterX, perpendicularCenterY);
	}
}