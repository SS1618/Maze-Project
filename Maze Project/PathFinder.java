import java.util.*;
public class PathFinder{
	ArrayList<Node> Queue;
	int exit_node_pos;
	public boolean path_found;
	public PathFinder(ArrayList<Node> n, int exit_node_pos){
		Queue = n;
		path_found = false;
		this.exit_node_pos = exit_node_pos;
	}
	public ArrayList<Node> generatePath(){
		ArrayList<Integer> dist = new ArrayList<>();
		ArrayList<Node> path = new ArrayList<>();
		dist.add(0);
		path.add(Queue.get(0));
		for(int i = 1; i < Queue.size(); i++){
			dist.add(-1);
			path.add(Queue.get(i));
		}
		ArrayList<Node> follow_path = new ArrayList<>();
		follow_path.add(Queue.get(exit_node_pos));
		int min_dist_pos = 0;
		while(Queue.size() > 0){
			min_dist_pos = 0;
			for(int i = 1; i < Queue.size(); i++){
				if(dist.get(Queue.get(min_dist_pos).getPos()) == -1 || (dist.get(Queue.get(i).getPos()) != -1 && dist.get(Queue.get(i).getPos()) < dist.get(Queue.get(min_dist_pos).getPos()))){
					min_dist_pos = i;
				}
			}
			//System.out.println("MN: " + min_dist_pos + " " + Queue.get(min_dist_pos).getRow() + " " + Queue.get(min_dist_pos).getCol() + " " + dist.get(Queue.get(min_dist_pos).getPos()));
			int adj_count = 0;
			for(int i = 0; i < Queue.size(); i++){
				if(checkAdj(Queue.get(min_dist_pos), Queue.get(i))){
					adj_count++;
					if(dist.get(Queue.get(i).getPos()) == -1 || dist.get(Queue.get(min_dist_pos).getPos()) + 1 < dist.get(Queue.get(i).getPos())){
						dist.set(Queue.get(i).getPos(), dist.get(Queue.get(min_dist_pos).getPos()) + 1);
						path.set(Queue.get(i).getPos(), Queue.get(min_dist_pos));
					}
				}
			}
			//System.out.println("ADJ: " + adj_count);
			Queue.remove(min_dist_pos);
		}
		int pos = exit_node_pos;
		while(path.get(pos).getRow() != 1 || path.get(pos).getCol() != 1){
			//System.out.println("PATH: " + pos + " " + path.get(pos).getRow() + " " + path.get(pos).getCol());
			follow_path.add(path.get(pos));
			pos = path.get(pos).getPos();
		}
		System.out.println("LENGTH: " + dist.get(exit_node_pos));
		path_found = true;
		return follow_path;
	}
	public boolean checkAdj(Node a, Node b){
		int a_r = a.getRow();
		int a_c = a.getCol();
		int b_r = b.getRow();
		int b_c = b.getCol();
		return (((a_r - b_r) * (a_r - b_r)) + ((a_c - b_c) * (a_c - b_c)) == 1);
	}
}