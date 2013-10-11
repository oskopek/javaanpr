/*
------------------------------------------------------------------------
JavaANPR - Automatic Number Plate Recognition System for Java
------------------------------------------------------------------------

This file is a part of the JavaANPR, licensed under the terms of the
Educational Community License

Copyright (c) 2006-2007 Ondrej Martinsky. All rights reserved

This Original Work, including software, source code, documents, or
other related items, is being provided by the copyright holder(s)
subject to the terms of the Educational Community License. By
obtaining, using and/or copying this Original Work, you agree that you
have read, understand, and will comply with the following terms and
conditions of the Educational Community License:

Permission to use, copy, modify, merge, publish, distribute, and
sublicense this Original Work and its documentation, with or without
modification, for any purpose, and without fee or royalty to the
copyright holder(s) is hereby granted, provided that you include the
following on ALL copies of the Original Work or portions thereof,
including modifications or derivatives, that you make:

# The full text of the Educational Community License in a location
viewable to users of the redistributed or derivative work.

# Any pre-existing intellectual property disclaimers, notices, or terms
and conditions.

# Notice of any changes or modifications to the Original Work,
including the date the changes were made.

# Any modifications of the Original Work must be distributed in such a
manner as to avoid any confusion with the Original Work of the
copyright holders.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT.
IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY
CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

The name and trademarks of copyright holder(s) may NOT be used in
advertising or publicity pertaining to the Original or Derivative Works
without specific, written prior permission. Title to copyright in the
Original Work and any associated documentation will at all times remain
with the copyright holders. 

If you want to alter upon this work, you MUST attribute it in 
a) all source files
b) on every place, where is the copyright of derivated work
exactly by the following label :

---- label begin ----
This work is a derivate of the JavaANPR. JavaANPR is a intellectual 
property of Ondrej Martinsky. Please visit http://net.sf.javaanpr.sourceforge.net 
for more info about JavaANPR. 
----  label end  ----

------------------------------------------------------------------------
                                         http://net.sf.javaanpr.sourceforge.net
------------------------------------------------------------------------
 */

package net.sf.javaanpr.imageanalysis;

import java.awt.Color;
import java.awt.image.BufferedImage;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.Set;
import java.util.Stack;
import java.util.Vector;

public class PixelMap {
	private class Point {
		int x;
		int y;

		// boolean deleted;
		Point(int x, int y) {
			this.x = x;
			this.y = y;
			// this.deleted = false;
		}

		boolean equals(Point p2) {
			if ((p2.x == x) && (p2.y == y)) {
				return true;
			}
			return false;
		}
		
		/*
		boolean equals(int x, int y) {
			if ((x == this.x) && (y == this.y)) {
				return true;
			}
			return false;
		}
		*/

		/*
		public boolean value() {
			return matrix[x][y];
		}
		*/
	}

	private class PointSet extends Stack<Point> {
		static final long serialVersionUID = 0;

		public void removePoint(Point p) {
			Point toRemove = null;
			for (Point px : this) {
				if (px.equals(p)) {
					toRemove = px;
				}
			}
			this.remove(toRemove);
		}

	}

	public class PieceSet extends Vector<Piece> {
		static final long serialVersionUID = 0;
	}

	private Piece bestPiece = null;

	public class Piece extends PointSet {
		static final long serialVersionUID = 0;
		public int mostLeftPoint;
		public int mostRightPoint;
		public int mostTopPoint;
		public int mostBottomPoint;
		public int width;
		public int height;
		public int centerX;
		public int centerY;
		public float magnitude;
		public int numberOfBlackPoints;
		public int numberOfAllPoints;

		public BufferedImage render() {
			if (numberOfAllPoints == 0) {
				return null;
			}
			BufferedImage image = new BufferedImage(width, height,
					BufferedImage.TYPE_INT_RGB);
			for (int x = mostLeftPoint; x <= mostRightPoint; x++) {
				for (int y = mostTopPoint; y <= mostBottomPoint; y++) {
					if (matrix[x][y]) {
						image.setRGB(x - mostLeftPoint, y - mostTopPoint,
								Color.BLACK.getRGB());
					} else {
						image.setRGB(x - mostLeftPoint, y - mostTopPoint,
								Color.WHITE.getRGB());
					}
				}
			}
			return image;
		}

		public void createStatistics() {
			mostLeftPoint = mostLeftPoint();
			mostRightPoint = mostRightPoint();
			mostTopPoint = mostTopPoint();
			mostBottomPoint = mostBottomPoint();
			width = (mostRightPoint - mostLeftPoint) + 1;
			height = (mostBottomPoint - mostTopPoint) + 1;
			centerX = (mostLeftPoint + mostRightPoint) / 2;
			centerY = (mostTopPoint + mostBottomPoint) / 2;
			numberOfBlackPoints = numberOfBlackPoints();
			numberOfAllPoints = numberOfAllPoints();
			magnitude = magnitude();
		}

		public int cost() { // vypocita ako velmi sa piece podoba pismenku
			return numberOfAllPoints - numberOfBlackPoints();
		}

		public void bleachPiece() {
			for (Point p : this) {
				matrix[p.x][p.y] = false;
			}
		}

		private float magnitude() {
			return ((float) numberOfBlackPoints / numberOfAllPoints);
		}

		private int numberOfBlackPoints() {
			return size();
		}

		private int numberOfAllPoints() {
			return width * height;
		}

		private int mostLeftPoint() {
			int position = Integer.MAX_VALUE;
			for (Point p : this) {
				position = Math.min(position, p.x);
			}
			return position;
		}

		private int mostRightPoint() {
			int position = 0;
			for (Point p : this) {
				position = Math.max(position, p.x);
			}
			return position;
		}

		private int mostTopPoint() {
			int position = Integer.MAX_VALUE;
			for (Point p : this) {
				position = Math.min(position, p.y);
			}
			return position;
		}

		private int mostBottomPoint() {
			int position = 0;
			for (Point p : this) {
				position = Math.max(position, p.y);
			}
			return position;
		}
	}

	// row column
	boolean[][] matrix;
	private int width;
	private int height;

	public PixelMap(Photo bi) {
		matrixInit(bi);
	}

	void matrixInit(Photo bi) {
		width = bi.getWidth();
		height = bi.getHeight();

		matrix = new boolean[width][height];

		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				matrix[x][y] = bi.getBrightness(x, y) < 0.5;
			}
		}
	}

	// renderuje celu maticu
	public BufferedImage render() {
		BufferedImage image = new BufferedImage(width, height,
				BufferedImage.TYPE_INT_RGB);
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (matrix[x][y]) {
					image.setRGB(x, y, Color.BLACK.getRGB());
				} else {
					image.setRGB(x, y, Color.WHITE.getRGB());
				}
			}
		}
		return image;
	}

	public Piece getBestPiece() {
		reduceOtherPieces();
		if (bestPiece == null) {
			return new Piece();
		}
		return bestPiece;
	}

	private boolean getPointValue(int x, int y) {
		// body mimo su automaticky biele
		if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
			return false;
		}
		return matrix[x][y];
	}

	private boolean isBoundaryPoint(int x, int y) {

		if (!getPointValue(x, y)) {
			return false; // ak je bod biely, return false
		}

		// konturovy bod ma aspon jeden bod v okoli biely
		if (!getPointValue(x - 1, y - 1) || !getPointValue(x - 1, y + 1)
				|| !getPointValue(x + 1, y - 1) || !getPointValue(x + 1, y + 1)
				|| !getPointValue(x, y + 1) || !getPointValue(x, y - 1)
				|| !getPointValue(x + 1, y) || !getPointValue(x - 1, y)) {
			return true;
		}

		return false;
	}

	private int n(int x, int y) { // pocet ciernych bodov v okoli
		int n = 0;
		if (getPointValue(x - 1, y - 1)) {
			n++;
		}
		if (getPointValue(x - 1, y + 1)) {
			n++;
		}
		if (getPointValue(x + 1, y - 1)) {
			n++;
		}
		if (getPointValue(x + 1, y + 1)) {
			n++;
		}
		if (getPointValue(x, y + 1)) {
			n++;
		}
		if (getPointValue(x, y - 1)) {
			n++;
		}
		if (getPointValue(x + 1, y)) {
			n++;
		}
		if (getPointValue(x - 1, y)) {
			n++;
		}
		return n;
	}

	// number of 0-1 transitions in ordered sequence 2,3,...,8,9,2
	private int t(int x, int y) {
		int n = 0; // number of 0-1 transitions
		// proceeding tranisions 2-3, 3-4, 4-5, 5-6, 6-7, 7-8, 8-9, 9-2
		for (int i = 2; i <= 8; i++) {
			if (!p(i, x, y) && p(i + 1, x, y)) {
				n++;
			}
		}
		if (!p(9, x, y) && p(2, x, y)) {
			n++;
		}
		return n;
	}

	/**
	 * okolie bodu p1 p9 p2 p3 p8 p1 p4 p7 p6 p5
	 */
	private boolean p(int i, int x, int y) {
		if (i == 1) {
			return getPointValue(x, y);
		}
		if (i == 2) {
			return getPointValue(x, y - 1);
		}
		if (i == 3) {
			return getPointValue(x + 1, y - 1);
		}
		if (i == 4) {
			return getPointValue(x + 1, y);
		}
		if (i == 5) {
			return getPointValue(x + 1, y + 1);
		}
		if (i == 6) {
			return getPointValue(x, y + 1);
		}
		if (i == 7) {
			return getPointValue(x - 1, y + 1);
		}
		if (i == 8) {
			return getPointValue(x - 1, y);
		}
		if (i == 9) {
			return getPointValue(x - 1, y - 1);
		}
		return false;
	}

	private boolean step1passed(int x, int y) {
		int n = n(x, y);
		return (((2 <= n) && (n <= 6)) && (t(x, y) == 1)
				&& (!p(2, x, y) || !p(4, x, y) || !p(6, x, y)) && (!p(4, x, y)
				|| !p(6, x, y) || !p(8, x, y)));
	}

	private boolean step2passed(int x, int y) {
		int n = n(x, y);
		return (((2 <= n) && (n <= 6)) && (t(x, y) == 1)
				&& (!p(2, x, y) || !p(4, x, y) || !p(8, x, y)) && (!p(2, x, y)
				|| !p(6, x, y) || !p(8, x, y)));
	}

	private void findBoundaryPoints(PointSet set) {
		if (!set.isEmpty()) {
			set.clear();
		}
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (isBoundaryPoint(x, y)) {
					set.add(new Point(x, y));
				}
			}
		}
	}

	public PixelMap skeletonize() { // vykona skeletonizaciu
		// thinning procedure
		PointSet flaggedPoints = new PointSet();
		PointSet boundaryPoints = new PointSet();
		boolean cont;

		do {
			cont = false;
			findBoundaryPoints(boundaryPoints);
			// apply step 1 to flag boundary points for deletion
			for (Point p : boundaryPoints) {
				if (step1passed(p.x, p.y)) {
					flaggedPoints.add(p);
				}
			}
			// delete flagged points
			if (!flaggedPoints.isEmpty()) {
				cont = true;
			}
			for (Point p : flaggedPoints) {
				matrix[p.x][p.y] = false;
				boundaryPoints.remove(p);
			}
			flaggedPoints.clear();
			// apply step 2 to flag remaining points
			for (Point p : boundaryPoints) {
				if (step2passed(p.x, p.y)) {
					flaggedPoints.add(p);
				}
			}
			// delete flagged points
			if (!flaggedPoints.isEmpty()) {
				cont = true;
			}
			for (Point p : flaggedPoints) {
				matrix[p.x][p.y] = false;
			}
			boundaryPoints.clear();
			flaggedPoints.clear();
		} while (cont);

		return (this);
	}

	// redukcia sumu /////////////////////////////

	public PixelMap reduceNoise() {
		PointSet pointsToReduce = new PointSet();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (n(x, y) < 4) {
					pointsToReduce.add(new Point(x, y)); // doporucene 4
				}
			}
		}
		// zmazemee oznacene body
		for (Point p : pointsToReduce) {
			matrix[p.x][p.y] = false;
		}
		return (this);
	}

	// reduce other pieces /////////////////////////////

	/*
	private boolean isInPieces(PieceSet pieces, int x, int y) {
		for (Piece piece : pieces) {
			// pre vsetky kusky
			for (Point point : piece) {
				// pre vsetky body na kusku
				if (point.equals(x, y)) {
					return true;
				}
			}
		}

		return false;
	}
	*/

	private boolean seedShouldBeAdded(Piece piece, Point p) {
		// ak sa nevymyka okrajom
		if ((p.x < 0) || (p.y < 0) || (p.x >= width) || (p.y >= height)) {
			return false;
		}
		// ak je cierny,
		if (!matrix[p.x][p.y]) {
			return false;
		}
		// ak este nie je sucastou ziadneho kuska
		for (Point piecePoint : piece) {
			if (piecePoint.equals(p)) {
				return false;
			}
		}
		return true;
	}

	// vytvori novy piece, najde okolie (piece) napcha donho vsetky body a vrati
	// vstupom je nejaka mnozina "ciernych" bodov, z ktorej algoritmus tie
	// body vybera
	private Piece createPiece(PointSet unsorted) {

		Piece piece = new Piece();

		PointSet stack = new PointSet();
		stack.push(unsorted.lastElement());

		while (!stack.isEmpty()) {
			Point p = stack.pop();
			if (seedShouldBeAdded(piece, p)) {
				piece.add(p);
				unsorted.removePoint(p);
				stack.push(new Point(p.x + 1, p.y));
				stack.push(new Point(p.x - 1, p.y));
				stack.push(new Point(p.x, p.y + 1));
				stack.push(new Point(p.x, p.y - 1));
			}
		}
		piece.createStatistics();
		return piece;
	}

	public PieceSet findPieces() {
		// boolean continueFlag;
		PieceSet pieces = new PieceSet();

		// vsetky cierne body na kopu.
		PointSet unsorted = new PointSet();
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				if (matrix[x][y]) {
					unsorted.add(new Point(x, y));
				}
			}
		}

		// pre kazdy cierny bod z kopy,
		while (!unsorted.isEmpty()) {
			// createPiece vytvori novy piece s tym ze vsetky pouzite body
			// vyhodi von z kopy

			pieces.add(createPiece(unsorted));
		}
		/*
		 * do { continueFlag = false; boolean loopBreak = false; for (int x = 0;
		 * x<this.width; x++) { for (int y = 0; y<this.height; y++) { // for
		 * each pixel // ak je pixel cierny, a nie je sucastou ziadneho kuska if
		 * (this.matrix[x][y] && !isInPieces(pieces,x,y)) { continueFlag = true;
		 * pieces.add(createPiece(x,y)); } }// for y } // for x } while
		 * (continueFlag);
		 */
		return pieces;
	}

	// redukuje ostatne pieces a vracia ten najlepsi
	public PixelMap reduceOtherPieces() {
		if (bestPiece != null) {
			return this; // bestPiece uz je , netreba dalej nic
		}

		PieceSet pieces = findPieces();
		int maxCost = 0;
		int maxIndex = 0;
		// najdeme najlepsi cost
		for (int i = 0; i < pieces.size(); i++) {
			if (pieces.elementAt(i).cost() > maxCost) {
				maxCost = pieces.elementAt(i).cost();
				maxIndex = i;
			}
		}

		// a ostatne zmazeme
		for (int i = 0; i < pieces.size(); i++) {
			if (i != maxIndex) {
				pieces.elementAt(i).bleachPiece();
			}
		}
		if (pieces.size() != 0) {
			bestPiece = pieces.elementAt(maxIndex);
		}
		return this;
	}
}
