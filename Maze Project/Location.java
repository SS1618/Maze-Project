public class Location{
	private int row;
	private int col;
	public Location(int c, int r){
		row = r;
		col = c;
	}
	public int getRow(){ return row;}
	public int getCol(){ return col;}
	public void addRow(int x) { row += x;}
	public void addCol(int x) { col += x;}
}