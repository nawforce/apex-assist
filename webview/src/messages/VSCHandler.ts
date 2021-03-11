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

  constructor(reciever: Reciever, document: Document) {
    this.reciever = reciever;
    window.addEventListener("message", (event) => {
      this.reciever.onDependents(event.data as GraphData);
    });

    reciever.onTheme(document.body.className);

    var observer = new MutationObserver((mutations) => {
      mutations.forEach((record) => {
        reciever.onTheme((record.target as Element).className);
      });
    });

    observer.observe(document.body, {
      attributes: true,
      attributeFilter: ["class"],
    });
  }

  requestDependents(identifier: string, depth: number, hide: string[]): void {
    this.vscodeAPI.postMessage({ cmd: "dependents", identifier, depth, hide });
  }

  openIdentifier(identifier: string): void {
    this.vscodeAPI.postMessage({ cmd: "open", identifier});
  }
}
