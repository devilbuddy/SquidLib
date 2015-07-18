package squidpony.squidai;

import squidpony.squidgrid.Radius;
import squidpony.squidmath.Elias;
import squidpony.squidmath.LightRNG;
import squidpony.squidmath.RNG;

import java.awt.Point;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

/**
 * Line Area of Effect that affects an slightly expanded (Elias) line plus an optional radius of cells around it, while
 * respecting obstacles in its path and possibly stopping if obstructed. You can specify a seed for the RNG and a
 * fresh RNG will be used for all random expansion; the RNG will reset to the specified seed after each generation so
 * the same LineAOE can be used in different places by just changing the start and end Points and possibly the radius.
 * That said, there is very little random about this, and it's possible the seed doesn't matter. You can specify the
 * RadiusType to Radius.DIAMOND for Manhattan distance, RADIUS.SQUARE for Chebyshev, or RADIUS.CIRCLE for Euclidean.
 *
 * This will produce doubles for its getArea() method which are equal to 1.0.
 *
 * This class uses squidpony.squidmath.Elias and squidpony.squidai.DijkstraMap to create its area of effect.
 * Created by Tommy Ettinger on 7/14/2015.
 */
public class LineAOE implements AOE {
    private Point start, end;
    private int radius;
    private char[][] map;
    private DijkstraMap dijkstra;
    private long seed;
    private Radius rt;

    public LineAOE(Point start, Point end)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
        rt = Radius.SQUARE;
        this.start = start;
        this.end = end;
        this.radius = 0;
    }
    public LineAOE(Point start, Point end, int radius)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
        rt = Radius.SQUARE;
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    public LineAOE(Point start, Point end, int radius, long seed)
    {
        this.seed = seed;
        LightRNG l = new LightRNG(seed);
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    public LineAOE(Point start, Point end, int radius, Radius radiusType)
    {
        LightRNG l = new LightRNG();
        this.seed = l.getState();
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.rt = radiusType;
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND: this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE: this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default: this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    public LineAOE(Point start, Point end, int radius, Radius radiusType, long seed)
    {
        this.seed = seed;
        LightRNG l = new LightRNG(seed);
        this.dijkstra = new DijkstraMap(new RNG(l));
        this.rt = radiusType;
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND: this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE: this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default: this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
        this.start = start;
        this.end = end;
        this.radius = radius;
    }
    private double[][] initDijkstra()
    {
        List<Point> lit = Elias.getLastPath();

        dijkstra.initialize(map);
        for(Point p : lit)
        {
            dijkstra.setGoal(p);
        }
        if(radius == 0)
            return dijkstra.gradientMap;
        return dijkstra.partialScan(radius, null);
    }

    public Point getStart() {
        return start;
    }

    public void setStart(Point start) {
        this.start = start;
        dijkstra.resetMap();
        dijkstra.clearGoals();
    }

    public Point getEnd() {
        return end;
    }

    public void setEnd(Point end) {
        this.end = end;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }

    public long getSeed() {
        return seed;
    }

    public void setSeed(long seed) {
        this.seed = seed;
        dijkstra.rng.setRandomness(new LightRNG(seed));
    }
    public Radius getRadiusType()
    {
        return rt;
    }
    public void setRadiusType(Radius radiusType)
    {
        this.rt = radiusType;
        switch (radiusType)
        {
            case OCTAHEDRON:
            case DIAMOND: this.dijkstra.measurement = DijkstraMap.Measurement.MANHATTAN;
                break;
            case CUBE:
            case SQUARE: this.dijkstra.measurement = DijkstraMap.Measurement.CHEBYSHEV;
                break;
            default: this.dijkstra.measurement = DijkstraMap.Measurement.EUCLIDEAN;
                break;
        }
    }

    @Override
    public void shift(Point aim) {
        setEnd(aim);
    }

    @Override
    public boolean mayContainTarget(Set<Point> targets) {
        for (Point p : targets)
        {
            if(rt.radius(start.x, start.y, p.x, p.y) + rt.radius(end.x, end.y, p.x, p.y) -
                    rt.radius(start.x, start.y, end.x, end.y) <= 3.0 + radius)
                return true;
        }
        return false;
    }

    @Override
    public void setMap(char[][] map) {
        this.map = map;
        dijkstra.resetMap();
        dijkstra.clearGoals();
    }

    @Override
    public HashMap<Point, Double> findArea() {
        double[][] dmap = initDijkstra();
        dmap[start.x][start.y] = DijkstraMap.DARK;
        dijkstra.rng.setRandomness(new LightRNG(seed));
        return AreaUtils.dijkstraToHashMap(dmap);
    }
}