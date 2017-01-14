package net.sf.javaanpr.intelligence.parser;

import java.util.ArrayList;
import java.util.List;

public class PlateForm {

    private boolean flagged = false;
    private final List<Position> positions;
    private final String name;

    public PlateForm(String name) {
        this.name = name;
        positions = new ArrayList<>();
    }

    public void addPosition(Position p) {
        positions.add(p);
    }

    public Position getPosition(int index) {
        return positions.get(index);
    }

    public int length() {
        return positions.size();
    }

    public boolean isFlagged() {
        return flagged;
    }

    public void setFlagged(boolean flagged) {
        this.flagged = flagged;
    }

    public List<Position> getPositions() {
        return positions;
    }

    public String getName() {
        return name;
    }

}
