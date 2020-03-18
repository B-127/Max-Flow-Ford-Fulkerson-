# =============================================================================
# Name: Tony Praveen Jesuthasan
# IIT ID: 2018596
# UoW ID: w1743058/8
# =============================================================================

import networkx as nx
import matplotlib.pyplot as plt
#%matplotlib inline

#Implementing the Ford-Fulkerson Algorithm to solve the Maximum-Flow Problem.
def FordFulkerson(maxFlowGraph,source,sink,debug=None):
    flow,flowPath=0,True
    
    #searching for paths with flow reserve
    while flowPath:
        flowPath,flowReserve=DepthFirstSearch(maxFlowGraph,source,sink)
        flow+=flowReserve
    
        #increasing the flow along the path
        for currentNode, nextNode in zip(flowPath,flowPath[1:]):
            if maxFlowGraph.has_edge(currentNode,nextNode):
                maxFlowGraph[currentNode][nextNode]['flow']+=flowReserve
            else:
                maxFlowGraph[nextNode][currentNode]['flow']-=flowReserve
        
        #midway results
        if callable(debug):
            debug(maxFlowGraph,flowPath,flowReserve,flow)

#Implementing the Depth-First Search
def DepthFirstSearch(maxFlowGraph, source, sink):
    undirected= maxFlowGraph.to_undirected()        #NetworkX function to return an undirected copy of a graph.
    visited={source}                                #Adding the source node to the visted dictionary since the search should begin from the source.
    stack=[(source,0,dict(undirected[source]))]
    
    while stack:
        currentNode,_,adjacentNodes= stack[-1]
        if currentNode==sink:
            break
        
        #searching the adjacent node
        while adjacentNodes:
            nextNode,cf= adjacentNodes.popitem()
            if nextNode not in visited:
                break
        
        else:
            stack.pop()
            continue
    
        #The flow and the capacity of the edge
        hasEdge=maxFlowGraph.has_edge(currentNode,nextNode)         #NetworkX function that returns if there are edges between two nodes.
        flow= cf['flow']
        capacity=cf['capacity']
        adjacentNodes=dict(undirected[nextNode])
        
        #increase and redirect flow at edge
        if hasEdge and flow<capacity:
            stack.append((nextNode,capacity-flow,adjacentNodes))
            visited.add(nextNode)
        elif not hasEdge and flow:
            stack.append((nextNode,flow,adjacentNodes))
            visited.add(nextNode)
    
    #This is to calculate the flow reserve and to find out the path taken by the flow.
    flowReserve= min((f for _,f,_ in stack[1:]),default=0)
    flowPath= [currentNode for currentNode,_,_ in stack]
    
    return flowPath,flowReserve


maxFlowGraph = nx.DiGraph()
maxFlowGraph.add_nodes_from('ABCDEFGH')
maxFlowGraph.add_edges_from([
    ('A', 'B', {'capacity': 4, 'flow': 0}),
    ('A', 'C', {'capacity': 5, 'flow': 0}),
    ('A', 'D', {'capacity': 7, 'flow': 0}),
    ('B', 'E', {'capacity': 7, 'flow': 0}),
    ('C', 'E', {'capacity': 6, 'flow': 0}),
    ('C', 'F', {'capacity': 4, 'flow': 0}),
    ('C', 'G', {'capacity': 1, 'flow': 0}),
    ('D', 'F', {'capacity': 8, 'flow': 0}),
    ('D', 'G', {'capacity': 1, 'flow': 0}),
    ('E', 'H', {'capacity': 7, 'flow': 0}),
    ('F', 'H', {'capacity': 6, 'flow': 0}),
    ('G', 'H', {'capacity': 4, 'flow': 0}),
])
    
maxFlowLayout = {
    'A': [0, 1], 'B': [1, 2], 'C': [1, 1], 'D': [1, 0],
    'E': [2, 2], 'F': [2, 1], 'G': [2, 0], 'H': [3, 1],
}

def DrawGraph():
    plt.figure(figsize=(12, 4))
    plt.axis('off')

    nx.draw_networkx_nodes(maxFlowGraph, maxFlowLayout, node_color='royalblue', node_size=475)
    nx.draw_networkx_edges(maxFlowGraph, maxFlowLayout, edge_color='black')
    nx.draw_networkx_labels(maxFlowGraph, maxFlowLayout, font_color='white')

    for nextNode, currentNode, cf in maxFlowGraph.edges(data=True):
        label = '{}/{}'.format(cf['flow'], cf['capacity'])
        color = 'green' if cf['flow'] < cf['capacity'] else 'red'
        x = maxFlowLayout[nextNode][0] * .6 + maxFlowLayout[currentNode][0] * .4
        y = maxFlowLayout[nextNode][1] * .6 + maxFlowLayout[currentNode][1] * .4
        t = plt.text(x, y, label, size=16, color=color, 
                     horizontalalignment='center', verticalalignment='center')
        
    plt.show()

DrawGraph()
print("-------------------------------------------------------------------")

def flow_debug(maxFlowGraph,flowPath,flowReserve,flow):
    if flowPath!=[]:
        print("The Flow has expanded by", flowReserve,"in the path",flowPath,"\nThe Current Flow is: ",flow)
    elif flowPath==[]:
        print("-------------------------------------------------------------------")
        print("THE Maximum Flow is:",flow)
    DrawGraph()

FordFulkerson(maxFlowGraph,'A','H',flow_debug)