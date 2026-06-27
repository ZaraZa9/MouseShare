import java.awt.MouseInfo;
import java.awt.Point;



public class CursorCapture {
    private CursorPos position;
    Point location = MouseInfo.getPointerInfo().getLocation();
    int x = location.x;
    int y = location.y;
        
    public CursorCapture(CursorPos position) {
        this.position = position;
    }
    
    public CursorPos getPosition() {
        return position;
    }
    
    public void setPosition(CursorPos position) {
        this.position = position;
    }

  
    public void updatePosition(){
        this.position = new CursorPos(x, y);
    }
}
