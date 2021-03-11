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
			"target": 0
		}, {
			"source": 2,
			"target": 0
		}, {
			"source": 3,
			"target": 2
		}, {
			"source": 4,
			"target": 2
		}, {
			"source": 5,
			"target": 2
		}, {
			"source": 6,
			"target": 2
		}, {
			"source": 7,
			"target": 2
		}, {
			"source": 8,
			"target": 1
		}, {
			"source": 9,
			"target": 1
		}, {
			"source": 2,
			"target": 1
		}, {
			"source": 10,
			"target": 1
		}, {
			"source": 11,
			"target": 1
		}, {
			"source": 12,
			"target": 1
		}, {
			"source": 13,
			"target": 1
		}, {
			"source": 14,
			"target": 1
		}, {
			"source": 15,
			"target": 1
		}, {
			"source": 16,
			"target": 1
		}, {
			"source": 17,
			"target": 1
		}, {
			"source": 0,
			"target": 1
		}, {
			"source": 5,
			"target": 1
		}, {
			"source": 18,
			"target": 1
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
