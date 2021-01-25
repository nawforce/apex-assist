import {Component, createRef, RefObject} from 'react';
import * as d3 from 'd3';
import { SimulationLinkDatum, SimulationNodeDatum } from 'd3';

interface NodeData extends SimulationNodeDatum {
    id: number
    name: string
    gender: string
}

interface LinkData extends SimulationLinkDatum<NodeData> {
}

interface GraphProps {
    nodeData: NodeData[]
    linkData: LinkData[]
}

export default class Graph extends Component<GraphProps> {

    private containerRef: RefObject<HTMLDivElement>

    constructor(props: GraphProps) {
        super(props);
        this.containerRef = createRef<HTMLDivElement>();
    }

    componentDidMount() {
        if (this.containerRef.current) {
            this.createGraph(this.containerRef.current, this.props.nodeData, this.props.linkData)
        }
    }

    render() {
        return <>
            <div ref={this.containerRef} style={{width: "100%", height: "80vh"}}></div>
        </>
    }

    private createGraph(container: HTMLDivElement, nodeData: NodeData[], linkData: LinkData[]) {
        const nodes = nodeData.map((d) => Object.assign({}, d))
        const links = linkData.map((d) => Object.assign({}, d))

        const containerRect = container.getBoundingClientRect();
        const height = containerRect.height;
        const width = containerRect.width;

        const simulation = d3
            .forceSimulation(nodes)
            .force("link", d3.forceLink(links).id(d => (d as any).id))
            .force("charge", d3.forceManyBody())
            .force("center", d3.forceCenter(width/2, height/2))
            
            //.force("x", d3.forceX())
            //.force("y", d3.forceY())

        const svg = d3
            .select(container)
            .append("svg")
            .attr("viewBox", 0 + ' ' + 0 + ' ' + width + ' ' + height)

        // Define arrow
        svg.append("svg:defs").selectAll("marker")
            .data(["end"])
            .enter().append("svg:marker") 
            .attr("id", String)
            .attr("viewBox", "0 -5 10 10")
            .attr("refX", 15)
            .attr("refY", -1.5)
            .attr("markerWidth", 6)
            .attr("markerHeight", 6)
            .attr("orient", "auto")
            .append("svg:path")
            .attr("d", "M0,-5L10,0L0,5");            

        const link = svg
            .append("g")
            .attr("stroke", "#999")
            .attr("stroke-opacity", 0.6)
            .selectAll("line")
            .data(links)
            .join("line")
            .attr("stroke-width", 2)
            .attr("marker-end", "url(#end)");

        const node = svg
            .append("g")
            .attr("stroke", "#fff")
            .attr("stroke-width", 2)
            .selectAll("circle")
            .data(nodes)
            .join("circle")
            .attr("r", 5)
            .attr("fill", "#9D00A0")

        /*            
        const label = svg.append("g")
            .attr("class", "labels")
            .selectAll("text")
            .data(nodes)
            .enter()
            .append("text")
            .attr('text-anchor', 'middle')
            .attr('dominant-baseline', 'central')
            .text(d => {return "\uf222";})
            */

        simulation.on("tick", () => {
            //update link positions
            link
                .attr("x1", d => (d.source as any).x)
                .attr("y1", d => (d.source as any).y)
                .attr("x2", d => (d.target as any).x)
                .attr("y2", d => (d.target as any).y);
        
            // update node positions
            node
                .attr("cx", d  => ' ' + d.x)
                .attr("cy", d =>  ' ' + d.y);
        
            // update label positions
            /*
            label
                .attr("x", d => { return ' ' + d.x; })
                .attr("y", d => { return ' ' + d.y; })
                */            
            });

    }
}
