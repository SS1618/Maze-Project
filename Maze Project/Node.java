import java.util.*;
public class Node{
	int list_pos;
	int row;
	int col;
	public Node(int pos, int r, int c){
		list_pos = pos;
		row = r;
		col = c;
	}
	public int getPos(){ return list_pos; }
	public int getRow(){ return row; }
	public int getCol(){ return col; }

}