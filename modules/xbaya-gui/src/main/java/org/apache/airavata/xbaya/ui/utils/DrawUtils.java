/**
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.airavata.xbaya.ui.utils;

import java.awt.Color;
import java.awt.GradientPaint;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.GeneralPath;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;
import java.util.List;

import org.python.modules.math;

public class DrawUtils {
	public static final int ARC_SIZE=10;
	
	public static void gradientFillShape(Graphics2D g,Color startColor, Color endColor, Shape shape) {
		initializeGraphics2D(g);
		GradientPaint gp = getGradientPaint(startColor, endColor, shape);
        g.setPaint(gp);
        g.fill(shape);
	}


	public static GradientPaint getGradientPaint(Color startColor,
			Color endColor, Shape shape) {
		GradientPaint gp = new GradientPaint((int)shape.getBounds().getX(), (int)shape.getBounds().getY(),
				startColor, (int)(shape.getBounds().getX()+shape.getBounds().getWidth()), (int)(shape.getBounds().getY()+shape.getBounds().getHeight()),
				endColor,false);
		return gp;
	}
	

	public static void initializeGraphics2D(Graphics2D g) {
		g.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	public static Shape getRoundedShape(Rectangle shape) {
		return new RoundRectangle2D.Double(shape.getX(),shape.getY(),shape.getWidth(),shape.getHeight(),DrawUtils.ARC_SIZE,DrawUtils.ARC_SIZE);
    }
	
	public static GeneralPath getRoundedShape(Polygon polygon) {
    	GeneralPath generalPath=new GeneralPath();
    	DrawUtils.setupRoundedGeneralPath(polygon, generalPath);
    	return generalPath;
    }

	public static void setupRoundedGeneralPath(Polygon polygon, GeneralPath generalPath) {
		generalPath.reset();
		List<int[]> l = new ArrayList<int[]>();
		for(int i=0; i < polygon.npoints; i++){
			l.add(new int[]{polygon.xpoints[i],polygon.ypoints[i]});
		}
		l.add(l.get(0));
		l.add(l.get(1));
		int[][] a=l.toArray(new int[][]{});
		generalPath.moveTo(a[0][0],a[0][1]);
		for(int pointIndex=1; pointIndex<a.length-1;pointIndex++){
			int[] p1=a[pointIndex-1];
			int[] p2=a[pointIndex];
			int[] p3=a[pointIndex+1];
			int[] mPoint = calculatePoint(p1, p2);
			generalPath.lineTo(mPoint[0], mPoint[1]);
			mPoint = calculatePoint(p3, p2);
			generalPath.curveTo(p2[0], p2[1], p2[0], p2[1], mPoint[0], mPoint[1]);
		}
	}
	

    private static int[] calculatePoint(int[] p1, int[] p2) {
		double d1=math.sqrt(math.pow(p1[0]-p2[0], 2)+math.pow(p1[1]-p2[1], 2));
		double per=ARC_SIZE/d1;
		
		double d_x=(p1[0]-p2[0])*per;
		double d_y=(p1[1]-p2[1])*per;
		
		int xx=(int)(p2[0]+d_x);
		int yy=(int)(p2[1]+d_y);
		
		int[] mPoint={xx,yy};
		return mPoint;
	}
}
