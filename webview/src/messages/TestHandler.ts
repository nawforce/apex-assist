import { GraphData } from "../components/Graph";
import { Handler } from "./Handler";
import { Reciever } from "./Receiver";

export class TestHandler implements Handler {
  private reciever: Reciever;

  private dependents: GraphData = {
		"nodeData": [{
			"name": "Cars",
			"r": 10
		}, {
			"name": "fflib_QueryFactory",
			"r": 10
		}, {
			"name": "Drivers",
			"r": 10
		}, {
			"name": "fflib_SObjectDescribe",
			"r": 10
		}, {
			"name": "Contestants",
			"r": 10
		}, {
			"name": "CarsSelector",
			"r": 10
		}, {
			"name": "ContestantsSelector",
			"r": 10
		}, {
			"name": "TeamsSelector",
			"r": 10
		}, {
			"name": "fflib_SObjectUnitOfWork",
			"r": 10
		}, {
			"name": "fflib_StringBuilder",
			"r": 10
		}, {
			"name": "fflib_SObjectSelector",
			"r": 10
		}, {
			"name": "fflib_SecurityUtils",
			"r": 10
		}, {
			"name": "RacesSelector",
			"r": 10
		}, {
			"name": "fflib_SObjectDomain",
			"r": 10
		}, {
			"name": "Races",
			"r": 15,
		}, {
			"name": "DriversSelector",
			"r": 10
		}, {
			"name": "Teams",
			"r": 15,
		}, {
			"name": "RaceDataSelector",
			"r": 15,
		}, {
			"name": "Application",
			"r": 20,
		}],
		"linkData": [{
			"source": 1,
			"target": 0,
			"nature": "extends"
		}, {
			"source": 2,
			"target": 0,
			"nature": "implements"
		}, {
			"source": 3,
			"target": 2,
			"nature": "uses"
		}, {
			"source": 4,
			"target": 2,
			"nature": "uses"
		}, {
			"source": 5,
			"target": 2,
			"nature": "uses"
		}, {
			"source": 6,
			"target": 2,
			"nature": "uses"
		}, {
			"source": 7,
			"target": 2,
			"nature": "uses"
		}, {
			"source": 8,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 9,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 2,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 10,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 11,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 12,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 13,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 14,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 15,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 16,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 17,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 0,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 5,
			"target": 1,
			"nature": "uses"
		}, {
			"source": 18,
			"target": 1,
			"nature": "uses"
		}]
	}

  constructor(reciever: Reciever) {
    this.reciever = reciever;
  }

  requestDependents(identifier: string, depth: number, hide: string[]): void {
    setTimeout(() => {
      this.reciever.onTheme('vscode-dark')
      this.reciever.onDependents(this.dependents);
    }, 2000);
  }

  openIdentifier(identifier: string): void {
  }
}
