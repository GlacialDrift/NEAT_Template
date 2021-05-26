import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 * @author Mike
 * @version 1.0
 */
public class Gene {

	private Node fromNode;
	private Node toNode;
	private double weight;
	private int innoNum;
	
	
	public Gene(Node from, Node to) {
		fromNode = from;
		toNode = to;
		weight = ThreadLocalRandom.current().nextDouble(-1, 1);
		innoNum = 0;
	}
	public Gene(Node from, Node to, double w) {
		fromNode = from;
		toNode = to;
		weight = w;
		innoNum = 0;
	}
	public Gene(Node from, Node to, int in) {
		fromNode = from;
		toNode = to;
		weight = ThreadLocalRandom.current().nextDouble(-1, 1);
		innoNum = in;
	}
	public Gene(Node from, Node to, double w, int in) {
		fromNode = from;
		toNode = to;
		weight = w;
		innoNum = in;
	}
	
	public void mutateWeight() {
		double temp = Math.random();
		if(temp<0.05) { // 10% of the time totally mutate the weight
			weight = ThreadLocalRandom.current().nextDouble(-1, 1);
		}else if(temp<0.5){ // 90% of the time, slightly modify the weight and keep in bounds
			weight += ThreadLocalRandom.current().nextGaussian()/30;
			if(weight>1) weight=1;
			if(weight<1) weight=-1;
		}
	}
	
	
	
	public Gene copy() {
		Gene g = new Gene(fromNode, toNode, weight, innoNum);
		return g;
	}
	
	
	/**
	 * @return the fromNode
	 */
	public Node getFromNode() {
		return fromNode;
	}
	/**
	 * @param fromNode the fromNode to set
	 */
	public void setFromNode(Node fromNode) {
		this.fromNode = fromNode;
	}
	/**
	 * @return the toNode
	 */
	public Node getToNode() {
		return toNode;
	}
	/**
	 * @param toNode the toNode to set
	 */
	public void setToNode(Node toNode) {
		this.toNode = toNode;
	}
	/**
	 * @return the weight
	 */
	public double getWeight() {
		return weight;
	}
	/**
	 * @param weight the weight to set
	 */
	public void setWeight(double weight) {
		this.weight = weight;
	}
	/**
	 * @return the innoNum
	 */
	public int getInnoNum() {
		return innoNum;
	}
	/**
	 * @param innoNum the innoNum to set
	 */
	public void setInnoNum(int innoNum) {
		this.innoNum = innoNum;
	}
	
	
	
}
