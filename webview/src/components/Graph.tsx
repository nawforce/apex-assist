import { Component } from "react";
import * as d3 from "d3";
import Measure, { ContentRect } from "react-measure";
import { d3adaptor, InputNode, Layout, Link } from "webcola";
const contextMenu = require("d3-context-menu");

interface NodeData extends InputNode {
  name: string;
  r: number;
  transitiveCount: number;
}

interface LinkData extends Link<number> {
  nature: string;
}

export interface GraphData {
  nodeData: NodeData[];
  linkData: LinkData[];
}

interface GraphProps extends GraphData {
  isDark: boolean;
  isCountVisible: boolean;
  focusIdentifier: string;
  onRefocus: (identifier: string) => void;
  onOpen: (identifier: string) => void;
  onHide: (identifier: string) => void;
  onToggleCount: () => void
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
      this.props.focusIdentifier !== prevProps.focusIdentifier ||
      this.props.isCountVisible !== prevProps.isCountVisible
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
    const themeSelector = this.props.isDark ? "dark" : "light";
    const isCountVisible = this.props.isCountVisible;
    const focusIdentifier = this.props.focusIdentifier;
    const containerRect = container.getBoundingClientRect();

    const menu = [
      {
        title: function (d: NodeData) {
          return `Hide ${d.name}`
        },
        action: function (d: NodeData) {
          me.props.onHide(d.name);
        },
      },
      {
        title: "Toggle Dependency Count",
        action: function () {
          me.props.onToggleCount();
        },
      }
    ];

    const layout = d3adaptor(d3)
      .avoidOverlaps(true)
      .nodes(nodes)
      .links(links)
      .linkDistance(150)
      .constraints([])
      .size([containerRect.width, containerRect.height])
      .start(50, 50, 250);

    const svg = d3
      .select(container)
      .append("svg")
      .attr("id", "graph-svg")
      .attr("width", containerRect.width)
      .attr("height", containerRect.height);

    this.arrow(svg, themeSelector, "uses");
    this.arrow(svg, themeSelector, "extends");
    this.arrow(svg, themeSelector, "implements");

    const link = svg
      .append("g")
      .selectAll(".link")
      .data(links)
      .join("path")
      .attr("class", function (l) {
        return `graph-link-${themeSelector}-${l.nature}`;
      });

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
      .attr("r", function (n) {
        return (n as NodeData).r;
      })
      .attr("class", function (n) {
        const nodeType =
          (n as any).name === focusIdentifier ? "focus-node" : "graph-node";
        return `${nodeType}-${themeSelector}`;
      })
      .on("click", function (datum, index, nodes) {
        me.onClick(datum as NodeData);
      })
      .on("dblclick", function (datum, index, nodes) {
        me.onDblClick(datum as NodeData);
      })
      .on("contextmenu", contextMenu(menu));

    node
      .append("text")
      .attr("dy", function (d: any) {
        return -3 - d.r;
      })
      .text(function (datum: any, index) {
        const d = datum as NodeData
        if (isCountVisible)
          return `${d.name} (${d.transitiveCount})`
        else
          return `${d.name}`
      })
      .attr("class", `graph-font-${themeSelector}`);

    this.resizer = new GraphResizer(svg, layout);

    layout.on("tick", () => {
      link.attr("d", this.linkArc);
      node.attr("transform", (d) => "translate(" + [d.x, d.y] + ")");
    });
  }

  arrow(
    svg: d3.Selection<SVGSVGElement, unknown, null, undefined>,
    themeSelector: string,
    name: string
  ) {
    svg
      .append("svg:defs")
      .selectAll("marker")
      .data([name])
      .enter()
      .append("svg:marker")
      .attr("id", name)
      .attr("viewBox", "0 -3 6 6")
      .attr("refX", 6)
      .attr("refY", 0)
      .attr("markerWidth", 6)
      .attr("markerHeight", 6)
      .attr("orient", "auto")
      .append("svg:path")
      .attr("d", "M0,-3L6,0L0,3")
      .attr("class", `graph-marker-${themeSelector}-${name}`);
  }

  linkArc(d: any) {
    var sourceX = d.source.x;
    var sourceY = d.source.y;
    var targetX = d.target.x;
    var targetY = d.target.y;

    var theta = Math.atan((targetX - sourceX) / (targetY - sourceY));
    var phi = Math.atan((targetY - sourceY) / (targetX - sourceX));

    var sinTheta = d.source.r * Math.sin(theta);
    var cosTheta = d.source.r * Math.cos(theta);
    var sinPhi = d.target.r * Math.sin(phi);
    var cosPhi = d.target.r * Math.cos(phi);

    // Set the position of the link's end point at the source node
    // such that it is on the edge closest to the target node
    if (d.target.y > d.source.y) {
      sourceX = sourceX + sinTheta;
      sourceY = sourceY + cosTheta;
    } else {
      sourceX = sourceX - sinTheta;
      sourceY = sourceY - cosTheta;
    }

    // Set the position of the link's end point at the target node
    // such that it is on the edge closest to the source node
    if (d.source.x > d.target.x) {
      targetX = targetX + cosPhi;
      targetY = targetY + sinPhi;
    } else {
      targetX = targetX - cosPhi;
      targetY = targetY - sinPhi;
    }

    // Draw an arc between the two calculated points
    var dx = targetX - sourceX,
      dy = targetY - sourceY,
      dr = 2 * Math.sqrt(dx * dx + dy * dy);

    return `M${sourceX},${sourceY}A${dr},${dr} 0 0,1 ${targetX},${targetY}`;
  }
}
