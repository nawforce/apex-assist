import { GraphData } from "../components/Graph";
import { Handler } from "./Handler";
import { Reciever } from "./Receiver";

interface vscodeAPI {
  postMessage(message: any): void;
}

declare function acquireVsCodeApi(): vscodeAPI;

export class VSCHandler implements Handler {
  private vscodeAPI = acquireVsCodeApi();
  private reciever: Reciever;

  constructor(reciever: Reciever) {
    this.reciever = reciever;
    window.addEventListener("message", (event) => {
        console.log(event)
        this.reciever.onDependents(event.data as GraphData)
    })
  }

  requestDependents(identifier: string, depth: number): void {
    this.vscodeAPI.postMessage({ cmd: "dependents", identifier, depth });
  }
}
