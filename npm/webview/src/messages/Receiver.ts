import { GraphProps } from "../components/Graph";

export class Reciever {
  private setGraphData: (value: GraphProps) => void;

  constructor(setGraphData: (value: GraphProps) => void) {
    this.setGraphData = setGraphData;
  }

  onRoots(names: string[]): void {}

  onDependents(dependents: GraphProps): void {
    this.setGraphData(dependents);
  }
}
