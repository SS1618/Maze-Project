import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
public class Maze extends JPanel implements KeyListener{
	JFrame frame;
	int col_size = 50;
	int row_size = 50;
	int win_width = 1000;
	int win_height = 600;
	int unit_dim = 10;
	int exit_row;
	int exit_col;
	boolean threeD;
	char[][] map;
	char[][] tagged_map;
	Explorer explorer;
	ArrayList<Wall> walls;
	int exit_steps;
	PathFinder bot;
	ArrayList<Node> bot_path;
	int bot_step;
	String map_file_path;
	public Maze(String map_file_path){
		this.map_file_path = map_file_path;
		setBoard();
	}
	public void setBoard(){
		File fileName = new File(map_file_path);
		try{
			BufferedReader input = new BufferedReader(new FileReader(fileName));
			String text = "";
			map = new char[row_size][col_size];
			tagged_map = new char[row_size][col_size];
			int row = 0;
			while((text = input.readLine()) != null){
				for(int c = 0; c < col_size; c++){
					map[row][c] = text.charAt(c);
				}
				row++;
			}
		}catch(Exception e){
			System.out.println(e.toString());
		}
		for(int r = 0; r < row_size; r++){
			if(map[r][col_size - 1] == ' '){
				exit_col = col_size - 1;
				exit_row = r;
				break;
			}
		}
		exit_steps = -1;
		System.out.println(exit_col + " " + exit_row);

		ArrayList<Node> Q = new ArrayList<>();
		Q.add(new Node(0, 1, 1));
		int queue_count = 1;
		int exit_node_pos = -1;
		for(int r = 0; r < row_size; r++){
			for(int c = 0; c < col_size; c++){
				if((r != 1 || c != 1) && map[r][c] == ' '){
					Q.add(new Node(queue_count, r, c));
					if(r == exit_row && c == exit_col){
						exit_node_pos = queue_count;
					}
					queue_count++;
				}
			}
		}
		bot = new PathFinder(Q, exit_node_pos);
		bot_path = bot.generatePath();
		bot_step = bot_path.size() - 1;
		threeD = false;
		explorer = new Explorer(new Location(1, 1), 0);
		frame = new JFrame("Maze");
		frame.add(this);
		frame.setSize(win_width, win_height);
		frame.addKeyListener(this);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
		Timer timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
		    public void run() {
		    	repaint();
		    	if(bot_step > 0)
		    		bot_step--;
		    }
        }, 0, 500);
	}
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D)g;
		g2.setColor(Color.BLACK);
		g2.fillRect(0, 0, frame.getWidth(), frame.getHeight());
		g2.setColor(Color.GREEN);
		g2.setFont(new Font("Serif", Font.PLAIN, 20));
		g2.drawString("STEPS: " + explorer.step_count, 700, 125);
		g2.drawString("Use arrow keys to move" , 700, 25);
		g2.drawString("Use SPACE to switch view" , 700, 50);
		g2.drawString("Use T to tag floors" , 700, 75);
		g2.drawString("Try beating the bot", 700, 100);
		if(!threeD){
			g2.setColor(Color.GRAY);
			for(int r = 0; r < row_size; r++){
				for(int c = 0; c < col_size; c++){
					if(map[r][c] == ' '){
						g2.fillRect(c * unit_dim + unit_dim, r * unit_dim + unit_dim, unit_dim, unit_dim);
					}
					else{
						g2.drawRect(c * unit_dim + unit_dim, r * unit_dim + unit_dim, unit_dim, unit_dim);
					}
				}
			}
			g2.setColor(Color.GREEN);
			g2.fillRect(explorer.getLoc().getCol() * unit_dim + unit_dim, explorer.getLoc().getRow() * unit_dim + unit_dim, unit_dim, unit_dim);
			if(bot_path.size() > 0 && bot_step >= 0){
				g2.setColor(Color.RED);
				g2.fillRect(bot_path.get(bot_step).getCol() * unit_dim + unit_dim, bot_path.get(bot_step).getRow() * unit_dim + unit_dim, unit_dim, unit_dim);
			}
		}
		else{
			int row = explorer.getLoc().getRow();
			int col = explorer.getLoc().getCol();
			int angle = explorer.getAngle();
			walls = new ArrayList<>();
			for(int pov = 0; pov < 3; pov++){
				//check for left walls
				int wall_row = row - (int)Math.sin(Math.toRadians((double)angle + 90.0));
				int wall_col = col + (int)Math.cos(Math.toRadians((double)angle + 90.0));
				if(wall_row >= 0 && wall_row < row_size && wall_col >= 0 && wall_col < col_size && map[wall_row][wall_col] == '*'){
					walls.add(getLeftBoardWall(pov));
				}
				else{
					walls.add(getLeftFloor(pov));
					walls.add(getLeftRoof(pov));
					walls.add(getLeftWall(pov + 1));
				}
				//check for right walls
				wall_row = row - (int)Math.sin(Math.toRadians((double)angle - 90.0));
				wall_col = col + (int)Math.cos(Math.toRadians((double)angle - 90.0));
				if(wall_row >= 0 && wall_row < row_size && wall_col >= 0 && wall_col < col_size && map[wall_row][wall_col] == '*'){
					walls.add(getRightBoardWall(pov));
				}
				else{
					walls.add(getRightFloor(pov));
					walls.add(getRightRoof(pov));
					walls.add(getRightWall(pov + 1));
				}
				//add floor
				if(bot_path.size() > 0 && bot_path.get(bot_step).getRow() == row && bot_path.get(bot_step).getCol() == col)
					walls.add(getBotFrontFloor(pov));
				else if (tagged_map[row][col] == '*')
					walls.add(getTaggedFrontFloor(pov));
				else
					walls.add(getFrontFloor(pov));
				walls.add(getFrontRoof(pov));
				//check for front walls
				wall_row = row - (int)Math.sin(Math.toRadians((double)angle));
				wall_col = col + (int)Math.cos(Math.toRadians((double)angle));
				if((wall_row >= 0 && wall_row < row_size && wall_col >= 0 && wall_col < col_size)){
					if(map[wall_row][wall_col] == '*'){
						walls.add(getFrontWall(pov + 1));
						break;
					}
					else{
						row = wall_row;
						col = wall_col;
					}
				}
				else if(wall_row == exit_row && wall_col == exit_col + 1){
					walls.add(getFrontExitWall(pov+1));
					break;
				}

			}

			System.out.println(walls);
			int l_wall_pos = -1;
			int r_wall_pos = -1;


			for(int i = 0; i < walls.size(); i++){
				g2.setColor(walls.get(i).getColor());
				g2.fill(walls.get(i).getWall());
			}
			g2.setColor(Color.BLACK);
			for(int i = 0; i < walls.size(); i++){
				g2.draw(walls.get(i).getWall());
			}
		}

		if(explorer.getLoc().getCol() == exit_col && explorer.getLoc().getRow() == exit_row){
			System.out.println("SUCCESS");
			g2.setColor(Color.GREEN);
			g2.setFont(new Font("Serif", Font.PLAIN, 50));
			g2.drawString("YOU GOT OUT!", 200, 250);
			if(exit_steps == -1)
				exit_steps = explorer.step_count;
		}
		if(exit_steps != -1){
			g2.setColor(Color.GREEN);
			g2.setFont(new Font("Serif", Font.PLAIN, 20));
			g2.drawString("STEPS TO EXIT: " + exit_steps, 700, 225);
		}
		if(bot_step == 0){
			g2.setColor(Color.RED);
			g2.setFont(new Font("Serif", Font.PLAIN, 20));
			g2.drawString("BOT EXITED IN " + bot_path.size() + " STEPS", 700, 250);
		}
	}
	public void keyReleased(KeyEvent e){

	}
	public void keyPressed(KeyEvent e){
		//System.out.println(e.getKeyCode());
		int pos_row = explorer.getLoc().getRow();
		int pos_col = explorer.getLoc().getCol();
		System.out.println(pos_row + " " + pos_col);
		System.out.println(explorer.getAngle());
		int code = e.getKeyCode();
		if(code == KeyEvent.VK_LEFT){
			System.out.println(code);
			explorer.move(e.getKeyCode());
		}
		else if(code == KeyEvent.VK_RIGHT){
			System.out.println(code);
			explorer.move(e.getKeyCode());
		}
		else if(code == KeyEvent.VK_UP){
			System.out.println(code);
			int dr = explorer.testDR();
			int dc = explorer.testDC();
			if(dr >= 0 && dr < row_size && dc >= 0 && dc < col_size && map[dr][dc] == ' ')
				explorer.move(e.getKeyCode());
		}
		else if(code == KeyEvent.VK_SPACE){
			threeD = !threeD;
		}
		else if(code == KeyEvent.VK_T){
			tagged_map[explorer.getLoc().getRow()][explorer.getLoc().getCol()] = '*';
			System.out.println("TAG");
		}
		repaint();
	}
	public void keyTyped(KeyEvent e){

	}
	public Wall getLeftWall(int pos){
		int[] row_arr = {10 + (50 * pos), 10 + (50 * pos), 510 - (50 * pos), 510 - (50 * pos)};
		int[] col_arr = {50 + (50 * pos), 100 + (50 * pos), 100 + (50 * pos), 50 + (50 * pos)};
		return new Wall(col_arr, row_arr, 4, pos, Wall.L_WALL_TYPE, new Color(200 - (pos * 50), 200 - (pos * 50), 200 - (pos * 50)));
	}
	public Wall getRightWall(int pos){
		int[] row_arr = {10 + (50 * pos), 10 + (50 * pos), 510 - (50 * pos), 510 - (50 * pos)};
		int[] col_arr = {600 - (50 * pos), 650 - (50 * pos), 650 - (50 * pos), 600 - (50 * pos)};
		return new Wall(col_arr, row_arr, 4, pos, Wall.R_WALL_TYPE, new Color(200 - (pos * 50), 200 - (pos * 50), 200 - (pos * 50)));
	}
	public Wall getFrontWall(int pos){
		int[] row_arr = {10 + (50 * pos), 10 + (50 * pos), 510 - (50 * pos), 510 - (50 * pos)};
		int[] col_arr = {50 + (50 * (pos+1)), 650 - (50 * (pos+1)), 650 - (50 * (pos+1)), 50 + (50 * (pos+1))};
		return new Wall(col_arr, row_arr, 4, pos + 1, Wall.F_WALL_TYPE, new Color(200 - (pos * 15), 200 - (pos * 15), 200 - (pos * 15)));
	}
	public Wall getFrontExitWall(int pos){
			int[] row_arr = {10 + (50 * pos), 10 + (50 * pos), 510 - (50 * pos), 510 - (50 * pos)};
			int[] col_arr = {50 + (50 * (pos+1)), 650 - (50 * (pos+1)), 650 - (50 * (pos+1)), 50 + (50 * (pos+1))};
			return new Wall(col_arr, row_arr, 4, pos + 1, Wall.F_WALL_TYPE, new Color(0, 200 - (pos * 15), 0));
	}
	public Wall getRightBoardWall(int pos){
		int[] row_arr = {10 + (50 * (pos+1)), 10 + (50 * pos), 510 - (50 * pos), 510 - (50 * (pos+1))};
		int[] col_arr = {550 - (50 * pos), 600 - (50 * pos), 600 - (50 * pos), 550 - (50 * pos)};
		return new Wall(col_arr, row_arr, 4, pos, Wall.RB_WALL_TYPE, new Color(200 - (pos * 15), 200 - (pos * 15), 200 - (pos * 15)));
	}
	public Wall getLeftBoardWall(int pos){
		int[] row_arr = {10 + (50 * pos), 10 + (50 * (pos+1)), 510 - (50 * (pos+1)), 510 - (50 * pos)};
		int[] col_arr = {100 + (50 * pos), 150 + (50 * pos), 150 + (50 * pos), 100 + (50 * pos)};
		return new Wall(col_arr, row_arr, 4, pos, Wall.LB_WALL_TYPE, new Color(200 - (pos * 15), 200 - (pos * 15), 200 - (pos * 15)));
	}
	public Wall getFrontFloor(int pos){
		int[] row_arr = {510 - (50 * (pos + 1)), 510 - (50 * (pos + 1)), 510 - (50 * pos), 510 - (50 * pos)};
		int[] col_arr = {150 + (50 * pos), 550 - (50 * pos), 600 - (50 * pos), 100 + (50 * pos)};
		return new Wall(col_arr, row_arr, 4, pos, Wall.F_FLOOR_TYPE, new Color(200 - (pos * 15), 200 - (pos * 15), 200 - (pos * 15)));
	}
	public Wall getTaggedFrontFloor(int pos){
			int[] row_arr = {510 - (50 * (pos + 1)), 510 - (50 * (pos + 1)), 510 - (50 * pos), 510 - (50 * pos)};
			int[] col_arr = {150 + (50 * pos), 550 - (50 * pos), 600 - (50 * pos), 100 + (50 * pos)};
			return new Wall(col_arr, row_arr, 4, pos, Wall.F_FLOOR_TYPE, new Color(0, 200 - (pos * 15), 0));
	}
	public Wall getBotFrontFloor(int pos){
		int[] row_arr = {510 - (50 * (pos + 1)), 510 - (50 * (pos + 1)), 510 - (50 * pos), 510 - (50 * pos)};
		int[] col_arr = {150 + (50 * pos), 550 - (50 * pos), 600 - (50 * pos), 100 + (50 * pos)};
		return new Wall(col_arr, row_arr, 4, pos, Wall.F_FLOOR_TYPE, new Color(200 - (pos * 15), 0, 0));
	}
	public Wall getFrontRoof(int pos){
			int[] row_arr = {10 + (50 * pos), 10 + (50 * pos), 10 + (50 * (pos+1)), 10 + (50 * (pos + 1))};
			//int[] col_arr = {150 + (50 * pos), 550 - (50 * pos), 600 - (50 * pos), 100 + (50 * pos)};
			int[] col_arr = {100 + (50 * pos), 600 - (50 * pos), 550 - (50 * pos), 150 + (50 * pos)};
			return new Wall(col_arr, row_arr, 4, pos, Wall.F_ROOF_TYPE, new Color(200 - (pos * 15), 200 - (pos * 15), 200 - (pos * 15)));
	}
	public Wall getRightFloor(int pos){
		int[] row_arr = {510 - (50 * (pos + 1)), 510 - (50 * (pos + 1)), 510 - (50 * pos)};
		int[] col_arr = {550 - (50 * pos), 600 - (50 * pos), 600 - (50 * pos)};
		return new Wall(col_arr, row_arr, 3, pos, Wall.R_FLOOR_TYPE, new Color(200 - ((pos+1) * 15), 200 - ((pos+1) * 15), 200 - ((pos+1) * 15)));
	}
	public Wall getLeftFloor(int pos){
		int[] row_arr = {510 - (50 * (pos + 1)), 510 - (50 * (pos + 1)), 510 - (50 * pos)};
		int[] col_arr = {100 + (50 * pos), 150 + (50 * pos), 100 + (50 * pos)};
		return new Wall(col_arr, row_arr, 3, pos, Wall.L_FLOOR_TYPE, new Color(200 - ((pos+1) * 15), 200 - ((pos+1) * 15), 200 - ((pos+1) * 15)));
	}
	public Wall getLeftRoof(int pos){
		int[] row_arr = {10 + (50 * pos), 10 + (50 * (pos+1)), 10 + (50 * (pos + 1))};
		int[] col_arr = {100 + (50 * pos), 150 + (50 * pos), 100 + (50 * pos)};
		return new Wall(col_arr, row_arr, 3, pos, Wall.L_ROOF_TYPE, new Color(200 - ((pos+1) * 15), 200 - ((pos+1) * 15), 200 - ((pos+1) * 15)));
	}
	public Wall getRightRoof(int pos){
		int[] row_arr = {10 + (50 * (pos+1)), 10 + (50 * pos), 10 + (50 * (pos+1))};
		int[] col_arr = {550 - (50 * pos), 600 - (50 * pos), 600 - (50 * pos)};
		return new Wall(col_arr, row_arr, 3, pos, Wall.R_ROOF_TYPE, new Color(200 - ((pos+1) * 15), 200 - ((pos+1) * 15), 200 - ((pos+1) * 15)));
	}
}