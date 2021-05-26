import java.util.ArrayList;

/**
 * 
 * @author Mike Harris
 * @version 1.0
 */
public class Node {

	private double inputs;
	private double outputs;
	private int nodeID;
	private int layer = 0;
	
	private ArrayList<Gene> outputGenes = new ArrayList<Gene>();
	
	public Node(int n) {
		nodeID = n;
		inputs = 0d;
		outputs = 0d;
	}
	
	
	public void addInputs(double i) {
		inputs += i; // add the value i to the inputs total
	}
	public void computeOut() {
		if(layer==0) {
			outputs = inputs;
		}else {
			outputs = 1/(1+Math.exp(-inputs)); // compute a sigmoid of the inputs.
		}
		
		for (Gene g:outputGenes) { // project the output value to all of the forward-connected nodes
			g.getToNode().addInputs(outputs*g.getWeight());
			//multiply the outputs by the connection weight and the enable value
			// if enable =1, no change, if enabled =0, no forward connection
		}
		reset();
	}
	
	public void reset() {
		inputs = 0d;
		outputs = 0d;
	}
	
	public boolean isConnected(Node n) {
		
		// make sure nodes in the same layer count as connected
		if(n.getLayer()==layer) {
			return true;
		}
		
		//search the output genes of Node n to look for this node
		for(Gene i:n.getOutputGenes()) {
			if(i.getToNode().equals(this)) {
				return true;
			}
		}
		
		//search the output genes of this node to look for Node n
		for (Gene i:outputGenes) {
			if(i.getToNode().equals(n)) {
				return true;
			}
		}
		return false;
	}
	
	// add an output gene to this node, making sure to set the fromNode
	public void addGeneConnection(Gene g) {
		g.setFromNode(this);
		outputGenes.add(g);
	}
	
	// remove an output gene, making sure to clear its fromNode value
	public void removeGeneConnection(Gene g) {
		g.setFromNode(null);
		outputGenes.remove(g);
	}
	
	public Node copy() {
		Node n = new Node(nodeID);
		n.setInputs(inputs);
		n.setOutputs(outputs);
		n.setLayer(layer);
		n.setOutputGenes(outputGenes);
		return n;
	}
	

	/**
	 * @return the inputs
	 */
	public double getInputs() {
		return inputs;
	}

	/**
	 * @param inputs the inputs to set
	 */
	public void setInputs(double inputs) {
		this.inputs = inputs;
	}

	/**
	 * @return the outputs
	 */
	public double getOutputs() {
		return outputs;
	}

	/**
	 * @param outputs the outputs to set
	 */
	public void setOutputs(double outputs) {
		this.outputs = outputs;
	}

	/**
	 * @return the nodeID
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * @param nodeID the nodeID to set
	 */
	public void setNodeID(int nodeID) {
		this.nodeID = nodeID;
	}

	/**
	 * @return the layer
	 */
	public int getLayer() {
		return layer;
	}
	/**
	 * @param layer the layer to set
	 */
	public void setLayer(int layer) {
		this.layer = layer;
	}

	/**
	 * @return the outputGenes
	 */
	public ArrayList<Gene> getOutputGenes() {
		return outputGenes;
	}

	/**
	 * @param outputGenes the outputGenes to set
	 */
	public void setOutputGenes(ArrayList<Gene> outputGenes) {
		this.outputGenes = outputGenes;
	}
}
