import java.awt.event.*;
import java.lang.*;
public class Explorer{
	private Location location;
	int angle;
	int step_count;
	public Explorer(Location loc, int dir){
		location = new Location(loc.getCol(), loc.getRow());
		angle = dir;
		step_count = 0;
	}
	public int testDR(){
		return location.getRow() - (int)Math.sin(Math.toRadians((double)angle));
	}
	public int testDC(){
		return location.getCol() + (int)Math.cos(Math.toRadians((double)angle));
	}
	public void move(int key_code){
		if(key_code == KeyEvent.VK_UP){
			location.addCol((int)Math.cos(Math.toRadians((double)angle)));
			location.addRow(-(int)Math.sin(Math.toRadians((double)angle)));
			step_count++;
		}
		else if(key_code == KeyEvent.VK_RIGHT){
			angle -= 90;
			angle %= 360;
		}
		else if(key_code == KeyEvent.VK_LEFT){
			angle += 90;
			angle %= 360;
		}
	}
	public Location getLoc(){
		return location;
	}
	public int getAngle(){ return angle; }
}