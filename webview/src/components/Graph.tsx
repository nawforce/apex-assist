import { Component } from "react";
import * as d3 from "d3";
import Measure, { ContentRect } from "react-measure";
import { d3adaptor, InputNode, Layout, Link } from "webcola";

interface NodeData extends InputNode {
  name: string;
}

interface LinkData extends Link<number> {}

export interface GraphData {
  nodeData: NodeData[];
  linkData: LinkData[];
}

interface GraphProps extends GraphData {
  isDark: boolean;
  focusIdentifier: string;
  onRefocus: (identifier: string) => void;
  onOpen: (identifier: string) => void;
}

class GraphResizer {
  private svg: d3.Selection<SVGSVGElement, unknown, null, undefined>;
  private layout: Layout;

  constructor(
    svg: d3.Selection<SVGSVGElement, unknown, null, undefined>,
    layout: Layout
  ) {
    this.svg = svg;
    this.layout = layout;
  }

  resize(contentRect: ContentRect) {
    let rect = contentRect.entry as DOMRectReadOnly;
    let width = rect.width;
    let height = rect.height;
    if (width && height) {
      this.svg.attr("width", width);
      this.svg.attr("height", height);
      this.layout.size([width, height]).start(50, 50, 250);
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

  componentDidUpdate(prevProps: GraphProps) {
    if (
      this.props.nodeData !== prevProps.nodeData ||
      this.props.linkData !== prevProps.linkData ||
      this.props.isDark !== prevProps.isDark ||
      this.props.focusIdentifier !== prevProps.focusIdentifier
    )
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
      this.props.onRefocus(nodeData.name);
    }, 300);
  }

  private onDblClick(nodeData: NodeData): void {
    if (this.timeout) clearTimeout(this.timeout);

    this.props.onOpen(nodeData.name);
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
    const focusIdentifier = this.props.focusIdentifier;
    const containerRect = container.getBoundingClientRect();

    const layout = d3adaptor(d3)
      .avoidOverlaps(true)
      .nodes(nodes)
      .links(links)
      .linkDistance(100)
      .constraints([])
      .size([containerRect.width, containerRect.height])
      .start(50, 50, 250);

    const svg = d3
      .select(container)
      .append("svg")
      .attr("id", "graph-svg")
      .attr("width", containerRect.width)
      .attr("height", containerRect.height);

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
      .selectAll(".link")
      .data(links)
      .join("line")
      .attr("class", "graph-link" + darkPostfix);

    const node = svg
      .selectAll(".node")
      .data(layout.nodes())
      .enter()
      .append("g")
      .attr("x", function (d) {
        return d.x;
      })
      .attr("y", function (d) {
        return d.y;
      })
      .call(layout.drag());

    node
      .append("circle")
      .attr("r", 7)
      .attr("class", function (n) {
        return (
          ((n as any).name === focusIdentifier ? "focus-node" : "graph-node") +
          darkPostfix
        );
      })
      .on("click", function (datum, index, nodes) {
        me.onClick(datum as NodeData);
      })
      .on("dblclick", function (datum, index, nodes) {
        me.onDblClick(datum as NodeData);
      });

    node
      .append("text")
      .attr("dy", -10)
      .text(function (d) {
        return (d as any).name;
      })
      .attr("class", "graph-font" + darkPostfix);

    this.resizer = new GraphResizer(svg, layout);

    layout.on("tick", () => {
      link
        .attr("x1", (d) => (d as any).source.x)
        .attr("y1", (d) => (d as any).source.y)
        .attr("x2", (d) => (d as any).target.x)
        .attr("y2", (d) => (d as any).target.y);
      node.attr("transform", (d) => "translate(" + [d.x, d.y] + ")");
    });
  }
}
