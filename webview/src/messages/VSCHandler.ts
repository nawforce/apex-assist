import { GraphData } from "../components/Graph";
import { Handler } from "./Handler";
import { Reciever } from "./Receiver";
import { vscodeAPI } from "./VSCodeAPI";

export class VSCHandler implements Handler {
  private vscodeAPI: vscodeAPI;
  private reciever: Reciever;

  constructor(vscodeAPI: vscodeAPI, reciever: Reciever, document: Document) {
    this.vscodeAPI = vscodeAPI;
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
    this.vscodeAPI.postMessage({ cmd: "open", identifier });
  }
}
