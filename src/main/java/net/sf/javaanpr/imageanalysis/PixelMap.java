/*
 * Copyright 2013 JavaANPR contributors
 * Copyright 2006 Ondrej Martinsky
 * Licensed under the Educational Community License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.osedu.org/licenses/ECL-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS"
 * BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package net.sf.javaanpr.imageanalysis;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Stack;

public class PixelMap {

    boolean[][] matrix;
    private Piece bestPiece = null;
    private int width;
    private int height;

    public PixelMap(Photo bi) {
        matrixInit(bi);
    }

    private void matrixInit(Photo bi) {
        width = bi.getWidth();
        height = bi.getHeight();
        matrix = new boolean[width][height];
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                matrix[x][y] = bi.getBrightness(x, y) < 0.5;
            }
        }
    }

    /**
     * Renders the whole matrix.
     *
     * @return the rendered matrix
     */
    public BufferedImage render() {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
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

    /**
     * If a point is outside, return false (they're automatically white).
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return false if point is outside the matrix, the value otherwise
     */
    private boolean getPointValue(int x, int y) {
        if ((x < 0) || (y < 0) || (x >= width) || (y >= height)) {
            return false;
        }
        return matrix[x][y];
    }

    private boolean isBoundaryPoint(int x, int y) {
        if (!getPointValue(x, y)) { // if it's white (outside points are automatically white)
            return false;
        }
        // a boundary point must have at least one neighbor point that's white
        return !getPointValue(x - 1, y - 1) || !getPointValue(x - 1, y + 1) || !getPointValue(x + 1, y - 1)
                || !getPointValue(x + 1, y + 1) || !getPointValue(x, y + 1) || !getPointValue(x, y - 1)
                || !getPointValue(x + 1, y) || !getPointValue(x - 1, y);
    }

    private int n(int x, int y) { // number of black points in the neighborhood
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

    /**
     * Number of 0-1 transitions in ordered sequence 2, 3, ..., 8, 9, 2.
     * <p/>
     * Number of 0-1 transitions proceeding transitions 2-3, 3-4, 4-5, 5-6, 6-7, 7-8, 8-9, 9-2.
     *
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the number of 0-1 transitions
     */
    private int t(int x, int y) {
        int n = 0;
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
     * Returns the value of points in the neighborhood depending on {@code i}.
     * <p>
     * See:
     * <p>
     * p9 p2 p3
     * <p>
     * p8 p1 p4
     * <p>
     * p7 p6 p5
     *
     * @param i the index of the point
     * @param x the x coordinate
     * @param y the y coordinate
     * @return the point value of the point
     */
    private boolean p(int i, int x, int y) {
        switch (i) {
            case 1:
                return getPointValue(x, y);
            case 2:
                return getPointValue(x, y - 1);
            case 3:
                return getPointValue(x + 1, y - 1);
            case 4:
                return getPointValue(x + 1, y);
            case 5:
                return getPointValue(x + 1, y + 1);
            case 6:
                return getPointValue(x, y + 1);
            case 7:
                return getPointValue(x - 1, y + 1);
            case 8:
                return getPointValue(x - 1, y);
            case 9:
                return getPointValue(x - 1, y - 1);
            default:
                return false;
        }
    }

    private boolean step1passed(int x, int y) {
        int n = n(x, y);
        return (((2 <= n) && (n <= 6)) && (t(x, y) == 1) && (!p(2, x, y) || !p(4, x, y) || !p(6, x, y))
                && (!p(4, x, y) || !p(6, x, y) || !p(8, x, y)));
    }

    private boolean step2passed(int x, int y) {
        int n = n(x, y);
        return (((2 <= n) && (n <= 6)) && (t(x, y) == 1) && (!p(2, x, y) || !p(4, x, y) || !p(8, x, y))
                && (!p(2, x, y) || !p(6, x, y) || !p(8, x, y)));
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

    /**
     * Execute skeletonization (thinning procedure).
     *
     * @return a skeletonized PixelMap
     */
    public PixelMap skeletonize() {
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

    public PixelMap reduceNoise() {
        PointSet pointsToReduce = new PointSet();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (n(x, y) < 4) {
                    pointsToReduce.add(new Point(x, y)); // recommended 4
                }
            }
        }
        // remove marked points
        for (Point p : pointsToReduce) {
            matrix[p.x][p.y] = false;
        }
        return (this);
    }

    private boolean seedShouldBeAdded(Piece piece, Point p) {
        // if it's not out of bounds
        if ((p.x < 0) || (p.y < 0) || (p.x >= width) || (p.y >= height)) {
            return false;
        }
        // if it's black
        if (!matrix[p.x][p.y]) {
            return false;
        }
        // if it's not part of the piece yet
        for (Point piecePoint : piece) {
            if (piecePoint.equals(p)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new piece, chooses points in the neighborhood and puts all chosen points into the piece.
     *
     * @param unsorted a set of "black" points to choose from
     * @return the created piece
     * @see PixelMap#seedShouldBeAdded(Piece, Point)
     */
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
        PieceSet pieces = new PieceSet();
        // put all black points into a set
        PointSet unsorted = new PointSet();
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                if (matrix[x][y]) {
                    unsorted.add(new Point(x, y));
                }
            }
        }
        while (!unsorted.isEmpty()) {
            pieces.add(createPiece(unsorted));
        }
        return pieces;
    }

    /**
     * Reduce other pieces and return the best one as part of the PixelMap.
     *
     * @see PixelMap#getBestPiece()
     */
    public void reduceOtherPieces() {
        if (bestPiece != null) {
            return; // we've got a best piece already
        }
        PieceSet pieces = findPieces();
        int maxCost = 0;
        int maxIndex = 0;
        // find the best cost
        for (int i = 0; i < pieces.size(); i++) {
            if (pieces.get(i).cost() > maxCost) {
                maxCost = pieces.get(i).cost();
                maxIndex = i;
            }
        }
        // delete the others
        for (int i = 0; i < pieces.size(); i++) {
            if (i != maxIndex) {
                pieces.get(i).bleachPiece();
            }
        }
        if (pieces.size() != 0) {
            bestPiece = pieces.get(maxIndex);
        }
    }

    private final class Point {
        private final int x;
        private final int y;

        private Point(int x, int y) {
            this.x = x;
            this.y = y;
        }

        boolean equals(Point p2) {
            return (p2.x == x) && (p2.y == y);
        }
    }

    private class PointSet extends Stack<Point> {
        private static final long serialVersionUID = 0;

        public void removePoint(Point p) {
            Point toRemove = null;
            for (Point px : this) {
                if (px.equals(p)) {
                    toRemove = px;
                }
            }
            remove(toRemove);
        }
    }

    public class PieceSet extends ArrayList<Piece> {
        private static final long serialVersionUID = 0;
    }

    public class Piece extends PointSet {
        private static final long serialVersionUID = 0;
        private int mostLeftPoint;
        private int mostRightPoint;
        private int mostTopPoint;
        private int mostBottomPoint;
        private int width;
        private int height;
        private int centerX;
        private int centerY;
        private float magnitude;
        private int numberOfBlackPoints;
        private int numberOfAllPoints;

        public BufferedImage render() {
            if (numberOfAllPoints == 0) {
                return null;
            }
            BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            for (int x = mostLeftPoint; x <= mostRightPoint; x++) {
                for (int y = mostTopPoint; y <= mostBottomPoint; y++) {
                    if (matrix[x][y]) {
                        image.setRGB(x - mostLeftPoint, y - mostTopPoint, Color.BLACK.getRGB());
                    } else {
                        image.setRGB(x - mostLeftPoint, y - mostTopPoint, Color.WHITE.getRGB());
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

        /**
         * Computes how much the piece is similar to a character.
         *
         * @return the cost
         */
        public int cost() {
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

        public int getCenterX() {
            return centerX;
        }

        public int getCenterY() {
            return centerY;
        }

        public int getHeight() {
            return height;
        }

        public float getMagnitude() {
            return magnitude;
        }

        public int getMostBottomPoint() {
            return mostBottomPoint;
        }

        public int getMostLeftPoint() {
            return mostLeftPoint;
        }

        public int getMostRightPoint() {
            return mostRightPoint;
        }

        public int getMostTopPoint() {
            return mostTopPoint;
        }

        public int getNumberOfAllPoints() {
            return numberOfAllPoints;
        }

        public int getNumberOfBlackPoints() {
            return numberOfBlackPoints;
        }

        public int getWidth() {
            return width;
        }
    }
}
