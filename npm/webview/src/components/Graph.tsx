import { Component } from "react";
import * as d3 from "d3";
import { SimulationLinkDatum, SimulationNodeDatum } from "d3";
import Measure, { ContentRect } from "react-measure";

interface NodeData extends SimulationNodeDatum {
  id: number;
  name: string;
}

interface LinkData extends SimulationLinkDatum<NodeData> {}

export interface GraphData {
  nodeData: NodeData[];
  linkData: LinkData[];
}

interface GraphProps extends GraphData {
  isDark: boolean;
  onRefocus: (identifier: string) => void
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
    this.renderGraph();
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

  private timeout: NodeJS.Timeout | null = null;

  private onClick(nodeData: NodeData): void {
    if (this.timeout) clearTimeout(this.timeout);

    this.timeout = setTimeout(() => {
      this.props.onRefocus(nodeData.name)
    }, 300);
  }

  private onDblClick(nodeData: NodeData): void {
    if (this.timeout) clearTimeout(this.timeout);

    console.log("Double click" + nodeData);
  }

  private createGraph(
    container: HTMLElement,
    nodeData: NodeData[],
    linkData: LinkData[]
  ) {
    const me = this;
    const nodes = nodeData.map((d) => Object.assign({}, d));
    const links = linkData.map((d) => Object.assign({}, d));
    const darkPostfix = this.props.isDark ? "-dark" : "";

    const simulation = d3
      .forceSimulation(nodes)
      .force(
        "link",
        d3.forceLink(links).id((d) => (d as any).id)
      )
      .force("charge", d3.forceManyBody().strength(-350))
      .alphaDecay(0.015);

    const svg = d3.select(container).append("svg").attr("id", "graph-svg");

    // Define arrow
    svg
      .append("svg:defs")
      .selectAll("marker")
      .data(["end"])
      .enter()
      .append("svg:marker")
      .attr("id", String)
      .attr("viewBox", "0 -3 6 6")
      .attr("refX", 10)
      .attr("refY", 0)
      .attr("markerWidth", 6)
      .attr("markerHeight", 6)
      .attr("orient", "auto")
      .append("svg:path")
      .attr("d", "M0,-3L6,0L0,3")
      .attr("class", "graph-marker" + darkPostfix);

    const link = svg
      .append("g")
      .attr("class", "graph-link" + darkPostfix)
      .attr("stroke", "#999")
      .attr("stroke-opacity", 0.6)
      .selectAll(".link")
      .data(links)
      .join("line")
      .attr("stroke-width", 2)
      .attr("marker-end", "url(#end)");

    const node = svg
      .selectAll(".node")
      .data(simulation.nodes())
      .enter()
      .append("g");

    node
      .append("circle")
      .attr("r", 7)
      .attr("class", "graph-node" + darkPostfix)
      .on("click", function (i: any, n: NodeData) {
        me.onClick(n);
      })
      .on("dblclick", function (i: any, n: NodeData) {
        me.onClick(n);
      });

    node
      .append("text")
      .attr("dy", -10)
      .text(function (d) {
        return d.name;
      })
      .attr("class", "graph-font" + darkPostfix);

    this.resizer = new GraphResizer(svg, simulation);

    const containerRect = container.getBoundingClientRect();
    svg.attr("width", containerRect.width);
    svg.attr("height", containerRect.height);
    simulation.force(
      "center",
      d3.forceCenter(containerRect.width / 2, containerRect.height / 2)
    );

    simulation.on("tick", () => {
      //update link positions
      link
        .attr("x1", (d) => (d.source as any).x)
        .attr("y1", (d) => (d.source as any).y)
        .attr("x2", (d) => (d.target as any).x)
        .attr("y2", (d) => (d.target as any).y);

      // update node positions
      node.attr("transform", (d) => "translate(" + [d.x, d.y] + ")");

      // update label positions
      /*
            label
                .attr("x", d => { return ' ' + d.x; })
                .attr("y", d => { return ' ' + d.y; })
                */
    });
  }
}
