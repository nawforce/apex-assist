import { GraphData } from "../components/Graph";

export class Reciever {
  private setGraphData: (value: GraphData) => void;
  private setIsDark: (value: boolean) => void;

  constructor(
    setGraphData: (value: GraphData) => void,
    setIsDark: (value: boolean) => void
  ) {
    this.setGraphData = setGraphData;
    this.setIsDark = setIsDark;
  }

  onRoots(names: string[]): void {}

  onDependents(dependents: GraphData): void {
    this.setGraphData(dependents);
  }

  onTheme(newTheme: string): void {
    let prefix = "vscode-";
    if (newTheme.startsWith(prefix)) {
      let theme = newTheme.substr(prefix.length);
      if (theme === "dark" || theme === "high-contrast") this.setIsDark(true);
      else if (theme === "light") this.setIsDark(false);
    }
  }
}
