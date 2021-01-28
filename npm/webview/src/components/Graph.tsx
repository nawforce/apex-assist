import { Component } from "react";
import * as d3 from "d3";
import { SimulationLinkDatum, SimulationNodeDatum } from "d3";
import Measure, { ContentRect } from "react-measure";

interface NodeData extends SimulationNodeDatum {
  id: number;
  name: string;
}

interface LinkData extends SimulationLinkDatum<NodeData> {}

export interface GraphProps {
  nodeData: NodeData[];
  linkData: LinkData[];
}

class GraphResizer {
  private svg: d3.Selection<SVGSVGElement, unknown, null, undefined>;
  private simulation: d3.Simulation<NodeData, undefined>;

  constructor(
    svg: d3.Selection<SVGSVGElement, unknown, null, undefined>,
    simulation: d3.Simulation<NodeData, undefined>
  ) {
    this.svg = svg;
    this.simulation = simulation;
  }

  resize(contentRect: ContentRect) {
    let rect = contentRect.entry as DOMRectReadOnly;
    let width = rect.width;
    let height = rect.height;
    if (width && height) {
      this.svg.attr("width", width);
      this.svg.attr("height", height);
      this.simulation.force("center", d3.forceCenter(width / 2, height / 2));
      this.simulation.restart();
    }
  }
}

export default class Graph extends Component<GraphProps> {
  private resizer: GraphResizer | null = null;

  render() {
    return (
      <>
        <Measure
          onResize={(contentRect) => {
            this.onResize(contentRect);
          }}
        >
          {({ contentRect, measureRef }) => (
            <div
              ref={measureRef}
              id="graph-container"
              style={{ width: "100%", height: "90vh" }}
            ></div>
          )}
        </Measure>
      </>
    );
  }

  componentDidMount() {
    this.renderGraph();
  }

  componentDidUpdate(prevProps: any) {
    if (
      prevProps.nodeData !== this.props.nodeData ||
      prevProps.linkData !== this.props.linkData
    ) {
      this.renderGraph();
    }
  }

  onResize(contentRect: ContentRect) {
    if (this.resizer) this.resizer.resize(contentRect);
  }

  private renderGraph() {
    let svg = document.getElementById("graph-svg");
    if (svg) {
      svg.remove();
    }
    let container = document.getElementById("graph-container");
    if (container) {
      this.createGraph(container, this.props.nodeData, this.props.linkData);
    }
  }

  private createGraph(
    container: HTMLElement,
    nodeData: NodeData[],
    linkData: LinkData[]
  ) {
    const nodes = nodeData.map((d) => Object.assign({}, d));
    const links = linkData.map((d) => Object.assign({}, d));

    const simulation = d3
      .forceSimulation(nodes)
      .force(
        "link",
        d3.forceLink(links).id((d) => (d as any).id)
      )
      .force("charge", d3.forceManyBody());

    const svg = d3.select(container).append("svg").attr("id", "graph-svg");

    // Define arrow
    svg
      .append("svg:defs")
      .selectAll("marker")
      .data(["end"])
      .enter()
      .append("svg:marker")
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
      .attr("fill", "#9D00A0");

    this.resizer = new GraphResizer(svg, simulation);

    const containerRect = container.getBoundingClientRect();
    svg.attr("width", containerRect.width);
    svg.attr("height", containerRect.height);
    simulation.force(
      "center",
      d3.forceCenter(containerRect.width / 2, containerRect.height / 2)
    );

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
        .attr("x1", (d) => (d.source as any).x)
        .attr("y1", (d) => (d.source as any).y)
        .attr("x2", (d) => (d.target as any).x)
        .attr("y2", (d) => (d.target as any).y);

      // update node positions
      node.attr("cx", (d) => " " + d.x).attr("cy", (d) => " " + d.y);

      // update label positions
      /*
            label
                .attr("x", d => { return ' ' + d.x; })
                .attr("y", d => { return ' ' + d.y; })
                */
    });
  }
}
