import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

/**
 * 
 * @author Mike
 * @version 1.0
 */
public class Genome {

	private int inputs;
	private int outputs;
	private int nextNodeID;
	private int biasNodeID;
	private int layers;
	
	private ArrayList<Node> nodes = new ArrayList<Node>();
	private ArrayList<Gene> genes = new ArrayList<Gene>();
	private ArrayList<Node> network = new ArrayList<Node>();
	
	public Genome(int in, int out) {
		inputs = in;
		outputs = out;
		layers = 2;
		nextNodeID = 0;
		
		// input nodes are numbered 0 -> inputs-1
		// output nodes are numbered inputs-> inputs+outputs-1
		// bias node is numbered inputs+outputs
		for(int i=0;i<inputs+outputs+1;i++) {
			nodes.add(new Node(nextNodeID));
			nextNodeID++;
			if(i<inputs) {
				nodes.get(i).setLayer(0);
			}else if(i<inputs+outputs) {
				nodes.get(i).setLayer(1);
			}else {
				nodes.get(i).setLayer(0);
			}
		}
		biasNodeID = inputs+outputs;
	}	
	
	public void initializeNetwork() {
		int possibleConnections = (inputs+1)*outputs;
		int fraction = 4;
		possibleConnections /= fraction;
		for(int i=0;i<possibleConnections;i++) {
			addConnection();
		}
	}
	
	public void buildNetwork() {
		network.clear();
		for(int i=0;i<layers;i++) {
			for(Node n:nodes) {
				if(n.getLayer()==i) {
					network.add(n);	
				}
			}
		}
		buildConnections();
	}
	
	public double[] executeNetwork(double[] ins) {
		double[] outs = new double[outputs];
		for(int i=0;i<inputs;i++) {
			nodes.get(i).setInputs(ins[i]);
		}
		nodes.get(biasNodeID).setInputs(1.0d);
		
		for(Node n:network) {
			n.computeOut();
		}
		for(int i=0;i<outputs;i++) {
			outs[i] = nodes.get(inputs+i).getOutputs();
		}
		return outs;
	}
	
	

	/*
	 * 2% chance of trying to add a node
	 * 6% chance of trying to add a connection
	 * otherwise try to mutate a weight
	 */
	public void mutateGenome() {
		double temp = ThreadLocalRandom.current().nextDouble();
		if(temp<0.02) {
			addNode();
			buildNetwork();
		}else if(temp<0.08) {
			addConnection();
			buildNetwork();
		}else {
			int temp1 = ThreadLocalRandom.current().nextInt(genes.size());
			Gene g = genes.get(temp1);
			g.mutateWeight();
		}
	}
	
	//return the requested Node with nodeID==i. If it doesn't exist, return -1
	public int findNodeID(int i) {
		for (int j=0;j<nodes.size();j++) {
			if(nodes.get(j).getNodeID() == i) {
				return j;
			}
		}
		return -1;
	}
	
	
	
	public void addNode() {
		
		if(genes.size()<1) { // if zero genes exist, add a gene and do nothing else
			addConnection();
			return;
		}
		
		//select a random gene
		int temp = ThreadLocalRandom.current().nextInt(genes.size());
		Gene g = genes.get(temp);
		
		//make sure randomly selected gene isn't disconnecting bias connection
		if(g.getFromNode().getNodeID()!=biasNodeID) {
					
			Node n = new Node(nextNodeID);
			nextNodeID++;
			int i = g.getFromNode().getLayer()+1;
			n.setLayer(i);

			Gene g1 = new Gene(g.getFromNode(), n, 1.0d);
			Gene g2 = new Gene(n, g.getToNode(),g.getWeight());
			Gene g3 = new Gene(nodes.get(biasNodeID), n, 1.0d);
			
			if(i==g.getToNode().getLayer()) {
				incrementLayers(i);
			}
			
			nodes.add(n);
			genes.add(g1);
			genes.add(g2);
			genes.add(g3);
			g.getFromNode().removeGeneConnection(g);
			g1.getFromNode().addGeneConnection(g1);
			g2.getFromNode().addGeneConnection(g2);
			g3.getFromNode().addGeneConnection(g3);	
		}
	}
	
	// takes all nodes from layer a and above and adds 1 to the layer value;
	private void incrementLayers(int a) {
		for(Node n:nodes) {
			if(n.getLayer()>=a) {
				n.setLayer(n.getLayer()+1);
			}
		}
		layers++;
	}
	
	
	/*
	 * probably need to add something related to innovation numbers here
	 */
	public void addConnection() {
		if(!isFull()) { // ensure the network isn't full
			//select two random nodes
			Node a = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
			Node b = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
		
			while(a.isConnected(b)) {
				// we need to pick two new random nodes in the network
				a = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
				b = nodes.get(ThreadLocalRandom.current().nextInt(nodes.size()));
			}
			
			Gene g;
			if(a.getLayer()>b.getLayer()) { // make sure to define which is the toNode and which is the fromNode
				g = new Gene(b,a); // set new weight with random value
				b.addGeneConnection(g);
			}else {
				g = new Gene(a,b); // set new weight with random value
				a.addGeneConnection(g);
			}
			genes.add(g);
		}
	}
	
	/*
	 * determine if there is space in the network to add a connection. We want to make sure that
	 * we're only looking forward in the network. Check each individual node
	 * - get the number of output connections from that node
	 * - determine the number of nodes that are in subsequent layers (total nodes - nodes in this and previous layers)
	 * if the node is fully connected, then those two values are equal
	 * if the node is not fully connected, then the # of output connections will be the smaller value and should return false
	 * 
	 * This guarantees that there is at least one pair of nodes that do not have an active connection.
	 * However, this does allow for connections to skip layers
	 */
	private boolean isFull() {
		int n = nodes.size();
		
		for(Node i:nodes) {
			int outs = i.getOutputGenes().size();
			int[] a = cummulativeNodesPerLayer();
			if(outs<n-a[i.getLayer()]) {
				return false;
			}
		}
		return true;
	}
	
	// return an array where each index corresponds to a layer, and each value is the number of nodes in that layer and all previous layers
	private int[] cummulativeNodesPerLayer() {
		int[] answer = numberNodesPerLayer();
		for(int i=1; i<answer.length;i++) {
			answer[i] += answer[i-1];
		}
		return answer;
	}

	//return an array where each index corresponds to a layer, and each value is the number of nodes in that layer
	private int[] numberNodesPerLayer() {
		int[] number = new int[layers];
		for (Node n:nodes) {
			number[n.getLayer()]++;
		}
		return number;
	}
	
	private void clearConnections() {
		for (Node n:nodes) {
			n.getOutputGenes().clear();
		}
	}
	
	public void buildConnections() {
		for(Gene g:genes) {
			g.getFromNode().addGeneConnection(g);
		}
	}
	
	public Genome(int in, int out, boolean crossover) {
		inputs = in;
		outputs = out;
	}
	
	public static Genome crossover(Genome a, Genome b) {
		
		return a;
	}
	

	/**
	 * @return the inputs
	 */
	public int getInputs() {
		return inputs;
	}
	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(int inputs) {
		this.inputs = inputs;
	}
	/**
	 * @return the outputs
	 */
	public int getOutputs() {
		return outputs;
	}
	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(int outputs) {
		this.outputs = outputs;
	}
	/**
	 * @return the nextNodeID
	 */
	public int getNextNodeID() {
		return nextNodeID;
	}
	/**
	 * @param nextNodeID the nextNodeID to set
	 */
	public void setNextNodeID(int nextNodeID) {
		this.nextNodeID = nextNodeID;
	}
	/**
	 * @return the biasNodeID
	 */
	public int getBiasNodeID() {
		return biasNodeID;
	}
	/**
	 * @param biasNodeID the biasNodeID to set
	 */
	public void setBiasNodeID(int biasNodeID) {
		this.biasNodeID = biasNodeID;
	}
	/**
	 * @return the nodes
	 */
	public ArrayList<Node> getNodes() {
		return nodes;
	}
	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}
	/**
	 * @return the genes
	 */
	public ArrayList<Gene> getGenes() {
		return genes;
	}
	/**
	 * @param genes the genes to set
	 */
	public void setGenes(ArrayList<Gene> genes) {
		this.genes = genes;
	}
	/**
	 * @return the network
	 */
	public ArrayList<Node> getNetwork() {
		return network;
	}
	/**
	 * @param network the network to set
	 */
	public void setNetwork(ArrayList<Node> network) {
		this.network = network;
	}

	/**
	 * @return the layers
	 */
	public int getLayers() {
		return layers;
	}

	/**
	 * @param layers the layers to set
	 */
	public void setLayers(int layers) {
		this.layers = layers;
	}
}
