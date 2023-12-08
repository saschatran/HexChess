package ch.unibas.dmi.dbis.cs108.project;

/**
 * Class to output two variables from one method
 */
class Point {
    double x;
    double y;

    /**
     * constructor
     *
     * @param x column of field
     * @param y row of field
     */
    Point(double x, double y) {
        this.x = x;
        this.y = y;
    }

    /**
     * calculates the angle as a cos between the reference line and the straight line through the two points
     *
     * @param p point of field
     * @return double: angle
     */
    double arc(Point p) {
        return ((-1) * (this.x - p.x)) / (Math.sqrt(
                this.x * this.x - 2 * p.x * this.x + this.y * this.y - 2 * p.y * this.y + p.x * p.x +
                        p.y * p.y));
    }
    /**
     * calculates all points at a spacing of t which are located on the straight line through two points
     *
     * @param p point
     * @param t spacing
     * @return point of line
     */
    Point line(Point p, double t) {
        double x = (this.x + (p.x - this.x) /
                (Math.sqrt((p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y))) * t);
        double y = (this.y + (p.y - this.y) /
                (Math.sqrt((p.x - this.x) * (p.x - this.x) + (p.y - this.y) * (p.y - this.y))) * t);
        return new Point(x, y);
    }
}