public class CursorPos {
    private double x;
    private double y;
    private double vectorX;
    private double vectorY;

    public CursorPos(double x, double y) {
        this.x = x;
        this.y = y;
        this.vectorX = 0;
        this.vectorY = 0;
    }

    public double getX() {
        return x;
    }
    public double getY() { 
        return y;
    }
    public double getVectorX() { 
        return vectorX;
    }
    public double getVectorY() { 
        return vectorY;
    }

    public void setX(double x) { 
        this.x = x;
    }
    public void setY(double y) { 
        this.y = y; 
    }
    public void setVectorX(double vectorX) { 
        this.vectorX = vectorX; 
    }
    public void setVectorY(double vectorY) { 
        this.vectorY = vectorY; 
    }

    @Override
    public String toString() {
        return "pos=(" + x + ", " + y + ") vec=(" + vectorX + ", " + vectorY + ")";
    }
}