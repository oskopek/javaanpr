package net.sf.javaanpr.intelligence.parser;

import java.util.Vector;

public class PlateForm {

    private boolean flagged = false;
    private final Vector<Position> positions;
    private final String name;

    public PlateForm(String name) {
        this.name = name;
        this.positions = new Vector<>();
    }

    public void addPosition(Position p) {
        this.positions.add(p);
    }

    public Position getPosition(int index) {
        return this.positions.elementAt(index);
    }

    public int length() {
        return this.positions.size();
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public Vector<Position> getPositions() {
        return positions;
    }

    public String getName() {
        return name;
    }

}
