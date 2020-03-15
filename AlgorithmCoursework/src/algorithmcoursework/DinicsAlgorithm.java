/*=============================================================================
Name: Tony Praveen Jesuthasan
IIT ID: 2018596
UoW ID: w1743058/8
=============================================================================*/

package algorithmcoursework;

import static java.lang.Long.min;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Deque;
import java.util.List;


public class DinicsAlgorithm {
    private static class Edge{
        public int startNode,endNode;              //The starting node and ending node of an edge          
        public Edge residualEdge;                  //The residual edge instance variable.
        public long edgeFlow;                      //The initial flow of the edge, defaults to zero.
        public final long edgeCapacity;            //The initial capacity of the edge.
        
        //The constructor which initializes the starting and ending nodes and the capacity.
        public Edge(int startNode, int endNode, long edgeCapacity){
            this.startNode=startNode;
            this.endNode=endNode;
            this.edgeCapacity=edgeCapacity;
        }
        
        /*A boolean method which determines if an edge is residual or not. Since a foward edge 
        cannot have a capacity of zero, we know that the edge is residual if the capacity is zero.*/
        public boolean isResidual(){
            return edgeCapacity==0;
        }
        
        /*This method is used to determine the maximum amount of flow that
        we can push through this edge. Works when flow is positive or negative.*/
        public long edgeRemainingCapacity(){
            return edgeCapacity-edgeFlow;
        }
        
        /*Augments flow for this edge. Increases the flow on the forward edge using
        the bottleneck value, which is found in the augmenting path and decreases the
        flow along the residual edge*/
        public void augment(long bottleNeck){
            edgeFlow+=bottleNeck;
            residualEdge.edgeFlow-=bottleNeck;
        }
        
        /*A toString method that is used to display information about the edges.
        The first coloumn shows the starting and ending nodes.
        The second coloumn shows the flow running through the edge.
        The third coloumn shows the capacity of the edge.
        The fourth coloumn shows if the edge is residual or not.*/
        public String toString(int source, int sink){
            String valueOfStartNode=(startNode==source)?"s":((startNode==sink)?"t": String.valueOf(startNode));
            String valueOfEndNode=(endNode==source)?"s":((endNode==sink)?"t": String.valueOf(endNode));
            
            return String.format("Edge %s -> %s | Flow = %3d | Capacity = %3d | Is Residual: %s",
          valueOfStartNode, valueOfEndNode, edgeFlow, edgeCapacity, isResidual());
        }
    }
    
    /*The base class that is used to solve the network flow.*/
    private abstract static class NetworkFlowSolver{
        static final long INFINITY= Long.MAX_VALUE/2;           //Large constant that doesnt overflow when numbers are added to it
        
        final int noOfNodes,source,sink;                        //The input variables given by the user. The number of nodes, the index of the source node and the index of the sink node.
        
        /*A variable which checks if the NetworkFlowSolver has run.*/
        protected boolean isSolved;
        
        /*The maximum flow variable.*/
        protected long maximumFlow;
        
        /*The list that represents the flow graph*/
        protected List<Edge>[] flowGraph;
        
        /*The constructor that is used to initialize the number of nodes, the starting
        index and the sink index. This constructor is also used to initialize the flow graph.*/
        public NetworkFlowSolver(int noOfNodes, int source, int sink){
            this.noOfNodes=noOfNodes;
            this.source=source;
            this.sink=sink;
            initializeUnoccupiedFlowGraph();
        }
        
        /*This method that returns void is used to initialize an empty arraylist of edges
        for each node index including the starting node index and the ending node index. 
        This is done to avoid a NULL POINTER EXCEPTION when we try to add an edge to the graph.*/
        private void initializeUnoccupiedFlowGraph(){
            flowGraph = new List[noOfNodes];
            for (int i=0; i<noOfNodes; i++)flowGraph[i]= new ArrayList<>();
        }
        
        /*This method is used to add a directed edge and it's residual edge to the flow graph.
        The starting node index, ending node index and the capacity of the edge to the flow graph are added.*/
        public void addEdge(int startNode, int endNode, long edgeCapacity){
            if(edgeCapacity<=0) throw new IllegalArgumentException("Forward edge has to be greater than zero");          //If capacity is zero or a negative value a ILLEGAL ARGUEMENT EXCEPTION will be thrown.
            Edge directedEdge= new Edge(startNode, endNode, edgeCapacity);                                               //This is the directed edge.
            Edge residualEdge= new Edge(endNode,startNode,0);                                                            //This is the residual edge which has a capacity of zero.   
            directedEdge.residualEdge=residualEdge;                                                                      //The residual edge of the directed edge is the residual edge.
            residualEdge.residualEdge=directedEdge;                                                                      //The residual edge of the residual edge is the directed edge.
            flowGraph[startNode].add(directedEdge);
            flowGraph[endNode].add(residualEdge);
        }
        
        /*This returns the residual graph after the excecuteSolver() function has been run.*/
        public List<Edge>[] getResidualGraph(){
            executeSolver(); 
            return flowGraph;
        }
        
        /*This method is used to calculate the maximum flow from the source to the sink*/
        public long getMaximumFlow(){
            executeSolver();
            return maximumFlow;
        }
        
        /*This method is used to make sure that excecuetSolver() is excecuted only once.*/
        private void executeSolver(){
            if (isSolved) return;
            isSolved=true;
            solveNetworkFlow();
        }
        
        /*This method is called to solve the network flow problem*/
        public abstract void solveNetworkFlow();
    }
    private static class DinicsAlgorithmSolver extends NetworkFlowSolver{
        private int[] nodeLevel;                                                    //This is used to keep track of the level of each node in the level graph.
        
        public DinicsAlgorithmSolver(int noOfNodes, int source, int sink){
            super(noOfNodes,source,sink);
            nodeLevel= new int[noOfNodes];                                          //This array will be the size of the number of nodes.
        }
        
        /*This method is used to carry out a breadth first search to create a level graph as the outer loop
        and the depth first search as the inner loop in order to solve the max flow problem.*/
        @Override
        public void solveNetworkFlow(){
            int[] nextNode= new int[noOfNodes];        //This array has been created to specify the next node. 
            
            while (breadthFirstSearch()){
                Arrays.fill(nextNode,0);               //Replace all the values in the array with zero.
                
                /*Find all the augmenting paths and add them together to find the maximum flow.*/
                for (long f=depthFirstSearch(source,nextNode,INFINITY); f!=0; f=depthFirstSearch(source,nextNode,INFINITY)){
                    System.out.println(f);
                    maximumFlow+=f;
                }
            }
        }
        
        /*This method is used to build a level graph and assign a level to each node in the nodeLevel array. 
        The return value is used to ensure if we can reach the sink using the breadth first search, if it cannot
        it means that the graph is fully saturated and the algorithm can stop working.*/
        private boolean breadthFirstSearch(){
            Arrays.fill(nodeLevel, -1);                                     //Marking each node as unvisited by filling the nodeLevel array with -1.
            Deque<Integer> queue= new ArrayDeque<>(noOfNodes);              //Initializing a queue for the breadth first search.
            queue.offer(source);                                            //Adding the source node to the queue because the breadth-first search starts at the source.
            nodeLevel[source]=0;                                            //Marking the source node as 0.
            
            /*This process is continued until the entire queue is empty
            and the level graph is built.*/
            while(!queue.isEmpty()){                                        //While the queue is not empty.
                int node=queue.poll();                                      //Remove the first node index from the queue.
                for(Edge edge: flowGraph[node]){                            //for loop to iterate through all the adjacent edges of that node.
                    long remCap= edge.edgeRemainingCapacity();              
                    if (remCap>0 && nodeLevel[edge.endNode]==-1){           //If the remaining capacity is greater than zero and the node is unvisited.
                        nodeLevel[edge.endNode]=nodeLevel[node]+1;          //Calculating the level for that node.
                        queue.offer(edge.endNode);                          //Add the level value into the queue.
                    }
                }
            }
            return nodeLevel[sink]!=-1;                                     //Return if we were able to reach the sink during this search.
        }
        
        
        private long depthFirstSearch(int currentNode, int[] nextNode, long minimumFlow){
            if(currentNode==sink) return minimumFlow;                                                             
            final int numOfEdges= flowGraph[currentNode].size();                                  //Finding the number of edges going out of the current node. 
        
            for(; nextNode[currentNode]<numOfEdges; nextNode[currentNode]++){                     
                Edge edge = flowGraph[currentNode].get(nextNode[currentNode]);                    //Getting the index of the current node in the nextNode array
                long remCap = edge.edgeRemainingCapacity();
                
                if (remCap > 0 && nodeLevel[edge.endNode] == nodeLevel[currentNode] + 1) {                              //If the remaining capacity is greater than zero and we have to make sure that we go up a level towards the sink.
                    long bottleNeck = depthFirstSearch(edge.endNode, nextNode, min(minimumFlow, remCap));               //The depth first search returns the bottleneck value along the augmenting path.
                    if (bottleNeck > 0) {                                                                               //If bottleneck is greater than zero it means that there is an augmenting path.
                        edge.augment(bottleNeck);                                                                       //We call the bottleneck() method from the edge class and return the bottleneck value.
                        return bottleNeck;
                        }
               }
            }
            return 0;
        }
    }
    public static void main(String[] args) {
    int n = 11;
    int s = n - 1;
    int t = n - 2;

    NetworkFlowSolver solver;
    solver = new DinicsAlgorithmSolver(n, s, t);

    // Source edges
    solver.addEdge(s, 0, 5);
    solver.addEdge(s, 1, 10);
    solver.addEdge(s, 2, 15);

    // Middle edges
    solver.addEdge(0, 3, 10);
    solver.addEdge(1, 0, 15);
    solver.addEdge(1, 4, 20);
    solver.addEdge(2, 5, 25);
    solver.addEdge(3, 4, 25);
    solver.addEdge(3, 6, 10);
    solver.addEdge(4, 2, 5);
    solver.addEdge(4, 7, 30);
    solver.addEdge(5, 7, 20);
    solver.addEdge(5, 8, 10);
    solver.addEdge(7, 8, 15);

    // Sink edges
    solver.addEdge(6, t, 5);
    solver.addEdge(7, t, 15);
    solver.addEdge(8, t, 10);

    // Prints: "Maximum flow: 30"
    System.out.printf("Maximum flow: %d\n", solver.getMaximumFlow());
    
    List<Edge>[] resultGraph = solver.getResidualGraph();

    // Displays all edges part of the resulting residual graph.
    for (List<Edge> edges : resultGraph) for (Edge e : edges) System.out.println(e.toString(s, t));
    }
}
