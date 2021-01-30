import { GraphData } from "../components/Graph";

export class Reciever {
  private setGraphData: (value: GraphData) => void;

  constructor(setGraphData: (value: GraphData) => void) {
    this.setGraphData = setGraphData;
  }

  onRoots(names: string[]): void {}

  onDependents(dependents: GraphData): void {
    this.setGraphData(dependents);
  }
}
