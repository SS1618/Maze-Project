import java.awt.*;
import java.util.*;
public class Wall{
	Polygon poly;
	int pos;
	int type;
	public static int R_WALL_TYPE = 2309;
	public static int L_WALL_TYPE = 1938;
	public static int F_WALL_TYPE = 139923;
	public static int RB_WALL_TYPE = 29304;
	public static int LB_WALL_TYPE = 39;
	public static int F_FLOOR_TYPE = 203;
	public static int F_ROOF_TYPE = 293;
	public static int R_FLOOR_TYPE = 2039;
	public static int L_FLOOR_TYPE = 21023;
	public static int L_ROOF_TYPE = 1023;
	public static int R_ROOF_TYPE = 1923;
	Color color;
	public Wall(int[] rows, int[] cols, int points, int pos, int type, Color c){
		poly = new Polygon(rows, cols, points);
		this.pos = pos;
		this.type = type;
		color = c;
	}
	public Polygon getWall() { return poly; }
	public int getType() { return type; }
	public int getPos() { return pos; }
	public Color getColor() { return color; }
}