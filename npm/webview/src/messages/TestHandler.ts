import { GraphProps } from "../components/Graph";
import { Handler } from "./Handler";
import { Reciever } from "./Receiver";

export class TestHandler implements Handler {
  private reciever: Reciever;

  private dependents: GraphProps = {
    nodeData: [
      { id: 1, name: "A" },
      { id: 2, name: "B" },
    ],
    linkData: [
      {
        source: 1,
        target: 2,
      },
    ],
  };

  constructor(reciever: Reciever) {
    this.reciever = reciever;
  }

  requestDependents(name: string): void {
    console.log("Request dependent: " + name);
    setTimeout(() => {
      this.reciever.onDependents(this.dependents);
    }, 2000);
  }
}
